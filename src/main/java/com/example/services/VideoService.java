package com.example.services;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.example.database.api.data.RedisTopVideoAPI;
import com.example.database.api.model.BaseUser;
import com.example.database.api.model.BaseVideo;
import com.example.database.common.EUserStatus;
import com.example.database.common.EVideoStatus;
import com.example.services.Cache.CacheManager;
import com.example.services.model.ListVideosResponse;
import com.example.services.model.VideoDetailInfo;
import com.example.utils.ConfigUtils;

import net.arnx.jsonic.JSON;
import redis.clients.jedis.Tuple;

public class VideoService {

	public static ListVideosResponse getTopLikeVideosByScore(long last, int length, long viewId) {
		try {
			ListVideosResponse response = new ListVideosResponse();
			double start;
			if (last <= ConfigUtils.firstGet) {
				start = Long.MAX_VALUE;
			} else {
				start = last;
			}
			// get list id video & rank from db , in this list video is ranked with number of total like 
			Tuple[] tuples = RedisTopVideoAPI.getTopLikeVideoByScore(start, length);

			if (tuples == null)
				return null;
			// if not enough video get , we scrolled all video in list
			if (tuples.length == length) {
				// next request we will get with last = less than total like of video in bottom of the list
				// we can miss data but it's good than duplicate data if more videos has same total like 
				response.last = tuples[tuples.length - 1].getScore() -1;
			} else {
				// notify to client we scrolled all video in list
				response.last = ConfigUtils.endData;
			}
			List<VideoDetailInfo> videos = new ArrayList<>();

			for (int i = 0; i < tuples.length - 1; i++)
				try {
					String videoId = tuples[i].getElement();
					// get base info video form cache if not exist get data from db
					BaseVideo baseVideo = CacheManager.getBaseVideo(videoId);
					// filter video is removed and fail when get info
					if ((baseVideo == null) || (baseVideo.videoStatus == EVideoStatus._delete))
						continue;
					// filter hidden video , only author video can see this video
					if ((baseVideo.videoStatus == EVideoStatus._hidden) && (baseVideo.authorId != viewId))
						continue;

					// get base info user form cache if not exist get data from db
					BaseUser baseUser = CacheManager.getBaseUser(baseVideo.authorId);
					// filter user is banned and fail when get info
					if ((baseUser == null) || (baseUser.accountStatus == EUserStatus.ban_account))
						continue;

					VideoDetailInfo videoInfo = new VideoDetailInfo();
					videoInfo.baseVideo = baseVideo;
					videoInfo.baseUser = baseUser;
					videoInfo.likeCount = CacheManager.getLikeCount(videoId); // get number total like of video form
																				// cache if not exist get data from db
					videos.add(videoInfo);
				} catch (Exception e) {
					Logger.getRootLogger().error(ExceptionUtils.getStackTrace(e));
				}
			response.videos = videos;
			return response;
		} catch (Exception e) {
			Logger.getRootLogger().error(ExceptionUtils.getStackTrace(e));
		}
		return null;
	}

}
