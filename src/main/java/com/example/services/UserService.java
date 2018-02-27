package com.example.services;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.example.database.api.model.BaseVideo;
import com.example.database.redis.VideoRedisPool;
import com.example.database.redis.base.RedisConnector;
import com.example.database.redis.base.RedisKey;
import com.example.eventbus.interaction.EInteract;
import com.example.eventbus.interaction.VideoInteract;
import com.example.eventbus.publish.InteractPublisher;
import com.example.services.Cache.CacheManager;

public class UserService {
	public static boolean likeVideo(String videoId, long userId) {
		try {
			BaseVideo baseVideo = CacheManager.getBaseVideo(videoId);
			if (baseVideo == null)
				return false;

			long timestamp = System.currentTimeMillis();
			VideoInteract interact = new VideoInteract();
			// 1st : we add user to set user like this video and check data is new and added
			// to set
			// if duplicate data response will response 0
			if (RedisConnector.sadd(VideoRedisPool.getJedisPool(), RedisKey.userLikeVideoSet(videoId),
					String.valueOf(userId), "1") == 1) {
				// pub event like video and subscribers will process it
				interact.actionAuthorId = userId;
				interact.timeStamp = timestamp;
				interact.interaction = EInteract.like_video;
				interact.videoId = videoId;
				interact.videoAuthorId = baseVideo.authorId;
				InteractPublisher.publishEvent(interact);
			}
			return true;
		} catch (Exception e) {
			Logger.getRootLogger().error(ExceptionUtils.getStackTrace(e));
		}
		return false;
	}

}
