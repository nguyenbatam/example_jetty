package com.example.services.Cache;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.example.database.api.data.RedisUserAPI;
import com.example.database.api.data.RedisVideoAPI;
import com.example.database.api.model.BaseUser;
import com.example.database.api.model.BaseVideo;
import com.example.eventbus.subcribers.CacheManagerSubscriber;
import com.example.utils.ConfigUtils;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheStats;
import com.google.common.cache.LoadingCache;
import com.rabbitmq.client.Channel;

/**
 * some kinds of data will cached in guava 
 * if not exist it will load from db 
 * @author tamnb
 *
 */
public class CacheManager {
	public static LoadingCache<Long, BaseUser> baseUsers;
	public static LoadingCache<String, BaseVideo> baseVideos;
	public static LoadingCache<String, Long> videoLikeCounts;
	private static CacheManagerSubscriber jettyServiceSubscriber;

	public static BaseUser getBaseUser(long userId) {
		try {
			if (userId > 0L)
				return baseUsers.get(Long.valueOf(userId));
		} catch (Exception e) {
			Logger.getRootLogger().debug("Cache Guava get null userId=" + userId);
		}
		return null;
	}

	public static void updateBaseUser(long userId) {
		try {
			if (userId > 0L)
				baseUsers.refresh(Long.valueOf(userId));
		} catch (Exception e) {
			Logger.getRootLogger().debug("Cache Guava get null userId=" + userId);
		}
	}

	public static BaseVideo getBaseVideo(String videoId) {
		try {
			if ((videoId != null) && (videoId.length() > 0))
				return baseVideos.get(videoId);
		} catch (Exception e) {
			Logger.getRootLogger().debug("Cache Guava get null videoId=" + videoId);
		}
		return null;
	}

	public static boolean updateBaseVideo(String videoId) {
		try {
			if ((videoId != null) && (videoId.length() > 0))
				baseVideos.refresh(videoId);
			return true;
		} catch (Exception e) {
			Logger.getRootLogger().debug("Cache Guava get null videoId=" + videoId);
		}
		return false;
	}

	public static long getLikeCount(String videoId) {
		try {
			if ((videoId != null) && (videoId.length() > 0))
				return videoLikeCounts.get(videoId);
		} catch (Exception e) {
			Logger.getRootLogger().debug("Cache Guava get null videoId=" + videoId);
		}
		return 0;
	}

	public static void updateLikeCount(String videoId) {
		try {
			if ((videoId != null) && (videoId.length() > 0))
				videoLikeCounts.refresh(videoId);
		} catch (Exception e) {
			Logger.getRootLogger().debug("Cache Guava get null videoId=" + videoId);
		}
	}

	public static void start(String nameProcess) {
		
		baseUsers = CacheBuilder.newBuilder().maximumSize(ConfigUtils.limitCacheUser)
				.build(new CacheLoader<Long, BaseUser>() {
					@Override
					public BaseUser load(Long userId) {
						try {
							return RedisUserAPI.getUserById(userId.longValue());
						} catch (Exception e) {
							Logger.getRootLogger().debug(ExceptionUtils.getStackTrace(e));
						}
						return null;
					}

				});
		baseVideos = CacheBuilder.newBuilder().maximumSize(ConfigUtils.limitCacheVideo)
				.build(new CacheLoader<String, BaseVideo>() {
					@Override
					public BaseVideo load(String videoId) {
						try {
							return RedisVideoAPI.getVideoById(videoId);
						} catch (Exception e) {
							Logger.getRootLogger().debug(ExceptionUtils.getStackTrace(e));
						}
						return null;
					}

				});

		videoLikeCounts = CacheBuilder.newBuilder().maximumSize(ConfigUtils.limitCacheVideo)
				.build(new CacheLoader<String, Long>() {
					@Override
					public Long load(String videoId) {
						try {
							return RedisVideoAPI.getLikeCountsByVideoId(videoId);
						} catch (Exception e) {
							Logger.getRootLogger().debug(ExceptionUtils.getStackTrace(e));
						}
						return null;
					}

				});

		try {
			jettyServiceSubscriber = new CacheManagerSubscriber(nameProcess);
			new Thread(new Runnable() {
				@Override
				public void run() {
					Channel channel = null;
					while (!Thread.interrupted()) {
						if (channel == null)
							channel = jettyServiceSubscriber.start();
						try {
							Thread.sleep(10000);
						} catch (Exception e) {
						}

						if (channel != null)
							if (!channel.isOpen())
								try {
									System.out.println("try close and restart channel ");
									channel.close();
									channel = null;
								} catch (Exception e) {
									e.printStackTrace();
									channel = null;
								}
					}
				}
			}).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			new Thread(new Runnable() {
				@Override
				public void run() {
					while (true) {
						try {
							Thread.sleep(2 * 60 * 1000);
							System.out.println(CacheManager.stats());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}).start();
			;
		} catch (Exception e) {
		}
	}

	public static void start() {
		start("");
	}

	public static String stats() {
		Map<String, LoadingCache> caches = new HashMap<>();
		caches.put("baseUsers", baseUsers);
		caches.put("baseVideos", baseVideos);

		StringBuilder sb = new StringBuilder();
		sb.append("cached statistic : ");

		for (String nameCache : caches.keySet()) {
			CacheStats cacheStats = caches.get(nameCache).stats();
			sb.append(nameCache + " [ ");
			sb.append("  hit  = " + cacheStats.hitCount());
			sb.append(" , miss = " + cacheStats.missCount());
			sb.append(" , size = " + caches.get(nameCache).size());
			sb.append(" , eviction = " + cacheStats.evictionCount());
			sb.append(" ] \t\t");
		}
		return sb.toString();
	}

}

