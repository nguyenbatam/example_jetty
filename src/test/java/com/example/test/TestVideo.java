package com.example.test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.example.services.VideoService;
import com.example.services.model.ListVideosResponse;

public class TestVideo {
	static long userId = 290L;

	@Test
	public void test() {
		/**
		 * test list video
		 */
//		ListVideosResponse listVideosResponse = VideoService.getTopViewVideosByScore(-2, 20, userId);
//		assertTrue("get not enough video",listVideosResponse.videos.size() != 20);
	}

}
