package com.example.database.rabbitmq.base;

import java.util.Properties;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.pool.impl.GenericObjectPoolFactory;
import org.apache.log4j.Logger;

import com.example.utils.Utils;
import com.rabbitmq.client.Channel;

public class RabbitMQPool {
	public ObjectPool<Channel> pool;
	public String host;
	public int port;
	public String db;
	public String user;
	public String password;
	public int maxConnectionActive = 100;

	public String exchangeName;

	public RabbitMQPool(String configFile, String keyHost, String keyPort, String keyUser, String keyPassword,
			String defaultHost, int defaultPort, String defaultUser, String defaultPassword) {
		try {
			Properties properties = Utils.loadPropertiesResource(configFile);
			this.host = properties.getProperty(keyHost, defaultHost).trim();
			this.port = Integer.valueOf(properties.getProperty(keyPort, defaultPort + "")).intValue();
			this.user = properties.getProperty(keyUser, defaultUser).trim();
			this.password = properties.getProperty(keyPassword, defaultPassword).trim();
		} catch (Exception e) {
			Logger.getRootLogger().error(ExceptionUtils.getStackTrace(e));
			this.host = defaultHost;
			this.port = defaultPort;
			this.user = defaultUser;
			this.password = defaultPassword;
		}

		System.out.println(this.host + "\t" + this.port + "\t" + this.user + "\t" + this.password);
		PoolableObjectFactory<Channel> mySqlPoolableObjectFactory = new RabbitMQPoolFactory<Channel>(this.host, this.port,
				this.user, this.password, this.exchangeName);

		GenericObjectPool.Config config = new GenericObjectPool.Config();
		config.maxActive = this.maxConnectionActive;
		config.testOnBorrow = true;
		config.testWhileIdle = true;
		config.timeBetweenEvictionRunsMillis = 10000L;
		config.minEvictableIdleTimeMillis = 60000L;
		GenericObjectPoolFactory<Channel> genericObjectPoolFactory = new GenericObjectPoolFactory<Channel>(
				mySqlPoolableObjectFactory, config);

		this.pool = genericObjectPoolFactory.createPool();
	}

	public RabbitMQPool(String configFile, String keyHost, String keyPort, String keyUser, String keyPassword,
			String defaultHost, int defaultPort, String defaultUser, String defaultPassword, String chanelName) {
		try {
			Properties properties = Utils.loadPropertiesResource(configFile);
			this.host = properties.getProperty(keyHost, defaultHost).trim();
			this.port = Integer.valueOf(properties.getProperty(keyPort, defaultPort + "")).intValue();
			this.user = properties.getProperty(keyUser, defaultUser).trim();
			this.password = properties.getProperty(keyUser, defaultPassword).trim();
			this.exchangeName = chanelName;
		} catch (Exception e) {
			Logger.getRootLogger().error(ExceptionUtils.getStackTrace(e));
			this.host = defaultHost;
			this.port = defaultPort;
			this.user = defaultUser;
			this.password = defaultPassword;
		}

		System.out.println(this.host + "\t" + this.port + "\t" + this.exchangeName);
		PoolableObjectFactory<Channel> mySqlPoolableObjectFactory = new RabbitMQPoolFactory<Channel>(this.host, this.port,
				this.user, this.password, this.exchangeName);

		GenericObjectPool.Config config = new GenericObjectPool.Config();
		config.maxActive = this.maxConnectionActive;
		config.testOnReturn = true;
		config.testOnBorrow = true;
		config.testWhileIdle = true;
		config.timeBetweenEvictionRunsMillis = 10000L;
		config.minEvictableIdleTimeMillis = 60000L;
		GenericObjectPoolFactory<Channel> genericObjectPoolFactory = new GenericObjectPoolFactory<Channel>(
				mySqlPoolableObjectFactory, config);

		this.pool = genericObjectPoolFactory.createPool();
	}

	public Channel getChanel() {
		try {
			return (Channel) this.pool.borrowObject();
		} catch (Exception e) {
			Logger.getRootLogger().error(ExceptionUtils.getStackTrace(e));
		}
		return null;
	}

	public void returnChanel(Channel channel) {
		try {
			this.pool.returnObject(channel);
		} catch (Exception e) {
			Logger.getRootLogger().error(ExceptionUtils.getStackTrace(e));
		}
	}
}
