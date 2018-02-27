package com.example.database.rabbitmq.base;

import org.apache.commons.pool.BasePoolableObjectFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class RabbitMQPoolFactory<T> extends BasePoolableObjectFactory<T> {
	private String host;
	private int port;
	private String user;
	private String password;
	private String exchangeName;

	public RabbitMQPoolFactory(String host, int port, String user, String password) {
		this.host = host;
		this.port = port;
		this.user = user;
		this.password = password;
		this.exchangeName = null;
	}

	public RabbitMQPoolFactory(String host, int port, String user, String password, String exchangeName) {
		this.host = host;
		this.port = port;
		this.user = user;
		this.password = password;
		this.exchangeName = exchangeName;
	}

	public T makeObject() throws Exception {
		ConnectionFactory factory = new ConnectionFactory();

		factory.setHost(this.host.trim());
		factory.setPort(this.port);
		factory.setUsername(this.user.trim());
		factory.setPassword(this.password.trim());

		Connection connection = factory.newConnection();

		Channel channel = connection.createChannel();

		if (this.exchangeName != null)
			channel.exchangeDeclare(this.exchangeName, "fanout", false, false, false, null);
		return (T) channel;
	}

	public boolean validateObject(T obj) {
		Channel channel = (Channel) obj;
		if (channel != null) {
			try {
				return channel.isOpen();
			} catch (Exception e) {
			}
		}
		return false;
	}

	public void destroyObject(T obj) throws Exception {
		Channel channel = (Channel) obj;
		if (channel != null) {
			channel.close();
			channel.getConnection().close();
		}
	}

}

