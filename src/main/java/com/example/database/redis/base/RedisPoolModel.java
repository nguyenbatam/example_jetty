package com.example.database.redis.base;

import redis.clients.jedis.JedisPool;

public class RedisPoolModel {
	public JedisPool jedisPool;
	public BaseRedisConfig config;

	public RedisPoolModel(JedisPool jedisPool,BaseRedisConfig config) {
		this.jedisPool=jedisPool;
		this.config=config;
	}

}

