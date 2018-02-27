package com.example.database.api.data;

import com.example.database.redis.TopRedisPool;
import com.example.database.redis.base.RedisConnector;
import com.example.database.redis.base.RedisKey;

import redis.clients.jedis.Tuple;

public class RedisTopVideoAPI {

	public static Tuple[] getTopLikeVideoByScore(double startScore, int length) {
		return RedisConnector.zrevrangeByScoreWithScores(TopRedisPool.getPoolConnection(), RedisKey.topVideoZSet(),
				startScore, 0.0D, 0, length).toArray(new Tuple[0]);
	}

}

