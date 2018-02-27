package com.example.database.redis;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.example.database.redis.base.BaseMasterRedisPool;
import com.example.database.redis.base.BaseRedisConfig;
import com.example.utils.ConfigUtils;

public class TopRedisPool {

	private static final String defaultMasterHost = "192.168.9.91";
	private static final int defaultPort = 9708;
	private static final int defaultMaxPool = 1000;
	private static final String defaultSlaveHost = "192.168.9.92,192.168.9.93";
	
	
	private static final String keySlaveHost = "top_list.slave";
	private static final String keyMasterHost = "top_list.host";
	private static final String keyPort = "top_list.port";
	private static final String keyMaxPool = "top_list.max_pool";
	

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

	public static BaseMasterRedisPool getPoolConnection() {
		if (_baseRedisPool == null)
			createBaseRedisPool();
		return _baseRedisPool;
	}
}
