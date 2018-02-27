package com.example.database.redis;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.example.database.redis.base.BaseMasterRedisPool;
import com.example.database.redis.base.BaseRedisConfig;
import com.example.utils.ConfigUtils;

public class VideoRedisPool {
	private static final String defaultMasterHost = "192.168.9.91";
	private static final int defaultPort = 9502;
	private static final int defaultMaxPool = 1000;
	private static final String defaultSlaveHost = "192.168.9.92,192.168.9.93";
	
	private static final String keySlaveHost = "video_stats.slave";
	private static final String keyMasterHost = "video_stats.host";
	private static final String keyPort = "video_stats.port";
	private static final String keyMaxPool = "video_stats.max_pool";

	private static BaseMasterRedisPool _baseRedisPool;

	public static synchronized void createBaseRedisPool() {
		if (_baseRedisPool == null) {
			try {
				_baseRedisPool = new BaseMasterRedisPool(
						BaseRedisConfig.loadMasterConfig(ConfigUtils.redisConfigFile, keyMasterHost, keyPort,
								keyMaxPool, defaultMasterHost, defaultPort, defaultMaxPool),
						BaseRedisConfig.loadSlaverConfig(ConfigUtils.redisConfigFile, keySlaveHost, keyPort, keyMaxPool,
								defaultSlaveHost, defaultPort, defaultMaxPool));
			} catch (Exception e) {
				Logger.getRootLogger().error(ExceptionUtils.getStackTrace(e));
			}
		}
	}

	public static BaseMasterRedisPool getJedisPool() {
		if (_baseRedisPool == null)
			createBaseRedisPool();
		return _baseRedisPool;
	}
}
