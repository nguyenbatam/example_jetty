package com.example.database.rabbitmq;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.example.database.rabbitmq.base.RabbitMQPool;
import com.example.utils.ConfigUtils;

public class PubSubRabbitMQPool {
	private static final String defaultHost = "192.168.9.91";
	private static final int defaultPort = 5672;
	private static final String defaultUser = "guest";
	private static final String defaultPassword = "guest";
	private static final String keyHost = "host";
	private static final String keyPort = "port";
	private static final String keyUser = "user";
	private static final String keyPassword = "password";
	public static Map<String, RabbitMQPool> mapRabbitMQPool = new HashMap<>();
	public static RabbitMQPool rabbitMQPool;

	public static synchronized RabbitMQPool getRabbitMQPool(String exchangeName) {
		try {
			if (mapRabbitMQPool.containsKey(exchangeName)) {
				return (RabbitMQPool) mapRabbitMQPool.get(exchangeName);
			}
			createRabbitMQPool(exchangeName);
		} catch (Exception e) {
			Logger.getRootLogger().error(ExceptionUtils.getStackTrace(e));
		}

		return null;
	}

	public static RabbitMQPool getRabbitMQPool() {
		try {
			if (rabbitMQPool == null) {
				rabbitMQPool = new RabbitMQPool(ConfigUtils.rabbitConfigFile, keyHost, keyPort, keyUser, keyPassword,
						defaultHost, defaultPort, defaultUser, defaultPassword);
			}

			return rabbitMQPool;
		} catch (Exception e) {
			Logger.getRootLogger().error(ExceptionUtils.getStackTrace(e));
		}

		return null;
	}

	public static RabbitMQPool createRabbitMQPool(String exchangeName) {
		try {
			RabbitMQPool rabbitMQPool = new RabbitMQPool(ConfigUtils.rabbitConfigFile, keyHost, keyPort, keyUser,
					keyPassword, defaultHost, defaultPort, defaultUser, defaultPassword);
			mapRabbitMQPool.put(exchangeName, rabbitMQPool);
			return rabbitMQPool;
		} catch (Exception e) {
			Logger.getRootLogger().error(ExceptionUtils.getStackTrace(e));
		}

		return null;
	}

}
