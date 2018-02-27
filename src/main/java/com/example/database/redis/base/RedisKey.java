package com.example.database.redis.base;

public class RedisKey {
	public static String videoInfoHash(String videoId) {
		StringBuilder sb = new StringBuilder("v::");
		sb.append(videoId);
		return sb.toString();
	}

	public static final String topVideoZSet() {
		return "top:trend:video:total";
	}

	public static final String userInfoHash(long userId) {
		StringBuilder sb = new StringBuilder("u::");
		sb.append(userId);
		return sb.toString();
	}

	public static String userLikeVideoSet(String videoId) {
		StringBuilder sb = new StringBuilder("v::");
		sb.append(videoId);
		sb.append("::l");
		return sb.toString();
	}

}
