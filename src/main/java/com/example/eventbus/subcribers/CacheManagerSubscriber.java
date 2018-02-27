package com.example.eventbus.subcribers;

import java.io.IOException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.example.database.rabbitmq.RabbitMQSubscriber;
import com.example.eventbus.interaction.EInteract;
import com.example.eventbus.interaction.Message;
import com.example.services.Cache.CacheManager;
/**
 * it subscribe info change data 
 * @author tamnb
 *
 */
public class CacheManagerSubscriber extends RabbitMQSubscriber {

	public CacheManagerSubscriber(String queueName) {
		super(queueName, false);
		/**
		 * list exchanges rabbit it will subscribe 
		 */
		addChannel(EInteract.update_video_info);
		addChannel(EInteract.update_user_info);
		addChannel(EInteract.like_video);
	}

	public void onMessage(Message message) {
		try {
			EInteract interact = message.interaction;
			switch (interact) {

			case update_user_info:
				CacheManager.updateBaseUser(Long.valueOf(message.content));
				break;
			case update_video_info:
				CacheManager.updateBaseVideo(message.content);
				break;
			case like_video:
//				CacheManager.updateLikeCount(message.content);
				break;
			default:
				break;
			}
		} catch (Exception e) {
			Logger.getRootLogger().error(ExceptionUtils.getStackTrace(e));
		}
	}

}
