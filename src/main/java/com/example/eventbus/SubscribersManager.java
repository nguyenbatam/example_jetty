package com.example.eventbus;

import com.example.database.common.ESubcriber;
import com.example.database.rabbitmq.RabbitMQSubscriber;
import com.example.eventbus.subcribers.StatisticSubscriber;
import com.example.services.Cache.CacheManager;
import com.example.utils.ConfigUtils;
import com.rabbitmq.client.Channel;

/**
 * 
 * it manage subscribers as statistic,delivery(add video to list : liked video
 * by user , uploaded video by idols),...
 * 
 * @author tamnb
 *
 */
public class SubscribersManager {

	static RabbitMQSubscriber subscriber;
	static Channel channel;

	public static void main(String[] args) throws Exception {

		ESubcriber subcriber = ESubcriber.test;
		try {
			subcriber = ESubcriber.valueOf(System.getProperty("subscriber"));
		} catch (Exception localException1) {
		}
		ConfigUtils.initcheckingThread();

		switch (subcriber) {
		case statistic:
			subscriber = new StatisticSubscriber();
			CacheManager.start("cache_service_" + subscriber.getClass().getSimpleName());
			break;
		default:
			System.exit(0);
			break;
		}
		channel = subscriber.start();

		System.out.println("wait shut down");

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("Shutdown hook ran!");
				try {
					channel.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
