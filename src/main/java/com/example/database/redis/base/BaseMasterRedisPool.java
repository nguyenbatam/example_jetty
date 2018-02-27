package com.example.database.redis.base;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.example.utils.ConfigUtils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class BaseMasterRedisPool {
	public static final int time_out = 100000;
	private RedisPoolModel _master_instance = null;
	private List<RedisPoolModel> _slave_instance = null;

	public BaseRedisConfig masterConfig;
	private List<BaseRedisConfig> slaveConfigs;

	public BaseMasterRedisPool(BaseRedisConfig masterConfig, List<BaseRedisConfig> slaveConfigs) {
		this.masterConfig = masterConfig;
		this._master_instance = createJedisPool(masterConfig, "master");
		this.slaveConfigs = slaveConfigs;
		this._slave_instance = createSlaveJedisPool(slaveConfigs);
	}

	public RedisPoolModel createJedisPool(BaseRedisConfig redisConfig, String name) {
		if (redisConfig == null)
			return null;
		JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
		jedisPoolConfig.setMaxTotal(redisConfig.maxPool);
		jedisPoolConfig.setTestWhileIdle(true);
		JedisPool instance = new JedisPool(jedisPoolConfig, redisConfig.host.trim(), redisConfig.port,
				ConfigUtils.redisTimeout);
		Jedis jedis = null;
		try {
			jedis = RedisConnector.getResource(new RedisPoolModel(instance, redisConfig));
			if (jedis.isConnected()) {
				jedis.close();
				System.out.println("create pool " + name + " success " + redisConfig.host + "\t" + redisConfig.port);
				return new RedisPoolModel(instance, redisConfig);
			} else
				return null;

		} catch (Exception e) {
			System.out.println("create pool " + name + " fail " + redisConfig.host + "\t" + redisConfig.port);
			try {
				instance.destroy();
				instance.close();
			} catch (Exception e2) {
				Logger.getRootLogger().error(ExceptionUtils.getStackTrace(e));
			}
			Logger.getRootLogger().error(ExceptionUtils.getStackTrace(e));
			return null;
		} finally {
			if (jedis != null)
				try {
					jedis.close();
				} catch (Exception e) {
					Logger.getRootLogger().error(ExceptionUtils.getStackTrace(e));
				}
		}
	}

	public RedisPoolModel getMasterJedisPool() {
		try {
			if (_master_instance == null) {
				_master_instance = createJedisPool(masterConfig, "master");
			}
			return _master_instance;
		} catch (Exception e) {
			Logger.getRootLogger().error(ExceptionUtils.getStackTrace(e));
		}
		return null;
	}

	public List<RedisPoolModel> createSlaveJedisPool(List<BaseRedisConfig> slaveConfigs) {
		if ((slaveConfigs == null) || (slaveConfigs.size() == 0))
			return null;
		try {
			List<RedisPoolModel> slavePools = new ArrayList<>();
			for (BaseRedisConfig slaveConfig : slaveConfigs)
				try {
					RedisPoolModel redisPoolModel = createJedisPool(slaveConfig, "slave");
					if (redisPoolModel != null)
						slavePools.add(redisPoolModel);
				} catch (Exception e) {
					Logger.getRootLogger().error(ExceptionUtils.getStackTrace(e));
				}
			return slavePools;
		} catch (Exception e) {
			Logger.getRootLogger().error(ExceptionUtils.getStackTrace(e));
		}
		return null;
	}

	public RedisPoolModel getSlaveJedisPool() {
		if ((_slave_instance == null) || (_slave_instance.size() == 0)) {
			_slave_instance = createSlaveJedisPool(slaveConfigs);
		}

		if ((_slave_instance == null) || (_slave_instance.size() == 0)) {
			return _master_instance;
		} else {
			int index = ThreadLocalRandom.current().nextInt(_slave_instance.size());
			return _slave_instance.get(index);
		}
	}
}

