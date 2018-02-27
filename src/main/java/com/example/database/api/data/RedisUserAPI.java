package com.example.database.api.data;

import java.util.Map;

import com.example.database.api.model.BaseUser;
import com.example.database.common.EUserStatus;
import com.example.database.redis.UserRedisPool;
import com.example.database.redis.base.RedisConnector;
import com.example.database.redis.base.RedisKey;

public class RedisUserAPI {

	public static BaseUser getUserById(long userId) {
		Map<String, String> data = RedisConnector.hgetAll(UserRedisPool.getJedisPool(), RedisKey.userInfoHash(userId));
		return getUserById(userId, data);
	}

	public static BaseUser getUserById(long userId, Map<String, String> data) {
		if ((data == null) || (data.size() == 0))
			return null;
		BaseUser user = new BaseUser();
		user.id = userId;
		user.name = data.get("name");
		if (data.containsKey("status"))
			user.accountStatus = EUserStatus.getEUserStatus(data.get("status"));
		else
			user.accountStatus = EUserStatus.nomarl;
		return user;
	}

}
