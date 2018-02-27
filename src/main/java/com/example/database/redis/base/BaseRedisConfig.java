package com.example.database.redis.base;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.example.utils.Utils;

public class BaseRedisConfig {
	public String host;
	public int port;
	public int maxPool;

	public BaseRedisConfig(String host, int port, int maxPool) {
		this.host = host;
		this.port = port;
		this.maxPool = maxPool;
	}

	public static BaseRedisConfig loadMasterConfig(String configFile, String keyHost, String keyPort, String keyMaxPool,
			String defaultHost, int defaultPort, int defaultMaxPool) {
		try {
			Properties properties = Utils.loadPropertiesResource(configFile);
			int maxPool = Integer.valueOf(properties.getProperty(keyMaxPool, defaultMaxPool + ""));
			String host = properties.getProperty(keyHost, defaultHost).trim();
			if (host.length() == 0)
				return null;
			int port = Integer.valueOf(properties.getProperty(keyPort, defaultPort + "").trim());
			return new BaseRedisConfig(host, port, maxPool);
		} catch (Exception e) {
			Logger.getRootLogger().error(ExceptionUtils.getStackTrace(e));
		}
		return null;
	}

	public static List<BaseRedisConfig> loadSlaverConfig(String configFile, String keyHost, String keyPort,
			String keyMaxPool, String defaultHost, int defaultPort, int defaultMaxPool) {
		try {
			Properties properties = Utils.loadPropertiesResource(configFile);
			int maxPool = Integer.valueOf(properties.getProperty(keyMaxPool, defaultMaxPool + "")).intValue();
			String listAddress = properties.getProperty(keyHost, defaultHost);
			int port = Integer.valueOf(properties.getProperty(keyPort, defaultPort + "").trim());
			List<BaseRedisConfig> redisConfigs = new ArrayList<>();
			for (String host : listAddress.split(","))
				if (host.trim().length() > 0) {
					redisConfigs.add(new BaseRedisConfig(host.trim(), port, maxPool));
				}
			return redisConfigs;
		} catch (Exception e) {
			Logger.getRootLogger().error(ExceptionUtils.getStackTrace(e));
		}
		return null;
	}
}
