package com.example.database.api.data;

import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.example.database.api.model.BaseVideo;
import com.example.database.common.EVideoStatus;
import com.example.database.redis.VideoRedisPool;
import com.example.database.redis.base.RedisConnector;
import com.example.database.redis.base.RedisKey;

public class RedisVideoAPI {

	public static BaseVideo getVideoByIdFromMaster(String videoId) {
		Map<String, String> map = RedisConnector.hgetAll(VideoRedisPool.getJedisPool(),
				RedisKey.videoInfoHash(videoId));
		return getVideoById(videoId, map);
	}

	public static BaseVideo getVideoById(String videoId) {
		Map<String, String> map = RedisConnector.hgetAll(VideoRedisPool.getJedisPool(),
				RedisKey.videoInfoHash(videoId));
		return getVideoById(videoId, map);
	}

	public static long getLikeCountsByVideoId(String videoId) {
		return RedisConnector.scard(VideoRedisPool.getJedisPool(), RedisKey.userLikeVideoSet(videoId));
	}

	public static BaseVideo getVideoById(String videoId, Map<String, String> map) {
		if (map.size() == 0)
			return null;
		try {
			BaseVideo video = new BaseVideo();
			video.id = videoId;
			video.title = map.get("title");
			video.authorId = Long.valueOf(map.get("user_id"));
			video.videoStatus = EVideoStatus.getEUserStatus((String) map.get("status"));
			return video;
		} catch (Exception e) {
			Logger.getRootLogger().error(ExceptionUtils.getStackTrace(e));
		}
		return null;
	}

}

