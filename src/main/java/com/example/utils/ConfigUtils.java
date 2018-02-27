package com.example.utils;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

public class ConfigUtils {
	public static String redisConfigFile = "redis.properties";
	public static String rabbitConfigFile = "rabbit.properties";

	public static double firstGet=-1;
	public static double endData=-1; 
	public static int redisTimeout = 1000;
	public static int limitCacheVideo = 100000;
	public static int limitCacheUser = 100000;
	
	public static int limitTimeDelayProcessEventStatistic = 30000;

	public static long timeReloadConfig = 30000L;
	public static long lastModified = 0L;

	public static void reloadConfigParam(String configFile) {

		File file = null;
		FileInputStream fis = null;
		try {
			if (configFile == null) {
				System.out.println("WARN : configFile = null ");
				return;
			}
			file = new File(configFile);
			if (!file.exists()) {
				System.out.println("WARN : " + file.getAbsolutePath() + " isn't exist . Using default or old config!");
				return;
			}
			fis = new FileInputStream(file);
		} catch (Exception e) {
			Logger.getRootLogger().error(ExceptionUtils.getStackTrace(e));
		}

		if (file != null)
			try {
				if (file.lastModified() != lastModified) {
					Properties properties = new Properties();
					properties.load(fis);
					System.out.println("====>   reload config = " + properties);

					try {
						limitCacheVideo = Integer.valueOf(properties.getProperty("cache.video", String.valueOf(limitCacheVideo)));
					} catch (Exception e) {
						Logger.getRootLogger().debug(ExceptionUtils.getStackTrace(e));
					}
					
					try {
						limitCacheUser = Integer.valueOf(properties.getProperty("cache.user", String.valueOf(limitCacheUser)));
					} catch (Exception e) {
						Logger.getRootLogger().debug(ExceptionUtils.getStackTrace(e));
					}

					lastModified = file.lastModified();
				}
			} catch (Exception e) {
				Logger.getRootLogger().error(ExceptionUtils.getStackTrace(e));
			}
		if (fis != null)
			try {
				fis.close();
			} catch (Exception e) {
				Logger.getRootLogger().error(ExceptionUtils.getStackTrace(e));
			}
	}

	public static void initcheckingThread() {
		System.out.println(
				"Command Run \n java  -Dconfig= -Di_c= -Dmysql= -Drabbit= -Dredis= -Dssdb= -Dlog_folder= -Dlog_file=  -jar *.jar args\n\n\n\n\n Help : \n-Dconfig= dir config file \n-Di_c= dir illegal comment file \n-Dmysql= dir config mysql  \n-Drabbit= dir config rabbit  \n-Dredis= dir config redis  \n-Dredis= dir config redis  \n-Dlog_folder= dir log folder  \n-Dlog_file= dir name log file  \n");

		final String configFile = System.getProperty("config");
		rabbitConfigFile = System.getProperty("rabbit");
		redisConfigFile = System.getProperty("redis");

		System.out.println("config File = " + configFile);
		System.out.println("rabbit Config File = " + rabbitConfigFile);
		System.out.println("redis Config File = " + redisConfigFile);

		if (configFile != null) {
			File file = new File(configFile);
			if (!file.exists()) {
				System.out.println("WARN config File : " + file.getAbsolutePath() + " isn't exist !");
			}
		}

		if (rabbitConfigFile != null) {
			File file = new File(rabbitConfigFile);
			if (!file.exists()) {
				System.out.println("WARN rabbit Config File : " + file.getAbsolutePath() + " isn't exist !");
			}
		}

		if (redisConfigFile != null) {
			File file = new File(redisConfigFile);
			if (!file.exists()) {
				System.out.println("WARN redis Config File : " + file.getAbsolutePath() + " isn't exist !");
			}
		}

		ConfigUtils.reloadConfigParam(configFile);
		new Thread(new Runnable() {
			public void run() {
				while (true) {
					try {
						Thread.sleep(30000L);
					} catch (Exception e) {
						Logger.getRootLogger().error(ExceptionUtils.getStackTrace(e));
					}
					if (configFile != null) {
						ConfigUtils.reloadConfigParam(configFile);
					}
				}
			}
		}).start();
	}

}
