package com.example.eventbus.subcribers;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.example.database.rabbitmq.RabbitMQSubscriber;
import com.example.database.redis.TopRedisPool;
import com.example.database.redis.base.RedisConnector;
import com.example.database.redis.base.RedisKey;
import com.example.eventbus.interaction.EInteract;
import com.example.eventbus.interaction.Message;
import com.example.eventbus.interaction.VideoInteract;

public class StatisticSubscriber extends RabbitMQSubscriber {

	static Map<String, Double> videoViews = new HashMap<>();

	public StatisticSubscriber() {
		/**
		 * list exchanges rabbit it will subscribe
		 */
		addChannel(EInteract.like_video.name());
	}

	public void onMessage(Message message) {
		Logger.getRootLogger().debug("Process : " + message);

		VideoInteract videoInteract = (VideoInteract) message;
		switch (message.interaction) {
		case like_video:
			processVideoEvents(videoInteract.videoId);
			break;

		default:
			break;
		}
	}

	private void processVideoEvents(String videoId) {
		// increase score of video in top video
		RedisConnector.zincrby(TopRedisPool.getPoolConnection(), RedisKey.topVideoZSet(), 1L, videoId);
	}

}
