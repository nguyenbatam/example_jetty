package com.example.utils;

import java.io.FileInputStream;
import java.util.Properties;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

public class Utils {

	public static Properties loadPropertiesResource(String file) {
		Properties props = new Properties();
		if (file == null)
			return props;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			props.load(fis);
		} catch (Exception e) {
			Logger.getRootLogger().error(ExceptionUtils.getStackTrace(e));
		} finally {
			if (fis != null)
				try {
					fis.close();
				} catch (Exception e2) {
				}
		}
		return props;
	}
}

