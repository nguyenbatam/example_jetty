package com.example.database.redis.base;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

/**
 * execute command with redis master , salve </br>
 * insert and change data to master </br>
 * get data from slave
 * 
 * @author tamnb
 *
 */
public class RedisConnector {

	public static Map<String, String> hgetAll(BaseMasterRedisPool baseRedisPool, String key) {
		RedisPoolModel jedisPool = baseRedisPool.getSlaveJedisPool();
		Jedis jedis = getResource(jedisPool);
		if (jedis == null)
			return null;
		try {
			return jedis.hgetAll(key);
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				jedis.close();
			} catch (Exception e) {
				Logger.getRootLogger().error(ExceptionUtils.getStackTrace(e));
			}
		}
	}

	public static Long scard(BaseMasterRedisPool baseRedisPool, String key) {
		RedisPoolModel jedisPool = baseRedisPool.getSlaveJedisPool();
		Jedis jedis = getResource(jedisPool);
		if (jedis == null)
			return null;
		try {
			return jedis.scard(key).longValue();
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				jedis.close();
			} catch (Exception e) {
				Logger.getRootLogger().error(ExceptionUtils.getStackTrace(e));
			}
		}
	}

	public static Long sadd(BaseMasterRedisPool baseRedisPool, String key, String... members) {
		RedisPoolModel jedisPool = baseRedisPool.getMasterJedisPool();
		Jedis jedis = getResource(jedisPool);
		if (jedis == null)
			return null;
		try {
			return jedis.sadd(key, members);
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				jedis.close();
			} catch (Exception e) {
				Logger.getRootLogger().error(ExceptionUtils.getStackTrace(e));
			}
		}
	}

	public static Double zincrby(BaseMasterRedisPool baseRedisPool, String key, double score, String member) {
		RedisPoolModel jedisPool = baseRedisPool.getMasterJedisPool();
		Jedis jedis = getResource(jedisPool);
		if (jedis == null)
			return null;
		try {
			return jedis.zincrby(key, score, member);
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				jedis.close();
			} catch (Exception e) {
				Logger.getRootLogger().error(ExceptionUtils.getStackTrace(e));
			}
		}
	}

	public static Set<Tuple> zrevrangeByScoreWithScores(BaseMasterRedisPool baseRedisPool, String key, double max,
			double min, int offset, int count) {
		RedisPoolModel jedisPool = baseRedisPool.getSlaveJedisPool();
		Jedis jedis = getResource(jedisPool);
		if (jedis == null)
			return null;
		try {
			return jedis.zrevrangeByScoreWithScores(key, max, min, offset, count);
		} catch (Exception e) {
			Logger.getRootLogger()
					.error("error when run command = zrevrangeByScoreWithScores with key = " + key + " \t max = " + max
							+ " & min = " + min + " & offset = " + offset + " & count = " + count + " & host = "
							+ jedisPool.config.host + " & port = " + jedisPool.config.port);
			return null;
		} finally {
			try {
				jedis.close();
			} catch (Exception e) {
				Logger.getRootLogger().error(ExceptionUtils.getStackTrace(e));
			}
		}
	}

	public static Jedis getResource(RedisPoolModel jedisPool) {
		if (jedisPool == null)
			return null;
		if (jedisPool.jedisPool == null)
			return null;
		for (int i = 0; i < 3; i++) {
			try {
				Jedis jedis = jedisPool.jedisPool.getResource();
				if (jedis != null)
					return jedis;
				else {
					Logger.getRootLogger().error("try connect redis i = " + i + "\t host= " + jedisPool.config.host
							+ "\t port =" + jedisPool.config.port);
				}
			} catch (Exception e) {
				Logger.getRootLogger().error("try connect redis i = " + i + "\t host= " + jedisPool.config.host
						+ "\t port =" + jedisPool.config.port);
			}
		}
		Logger.getRootLogger().error("cannot create connect to redis \t host= " + jedisPool.config.host + "\t port ="
				+ jedisPool.config.port);
		return null;
	}

}
