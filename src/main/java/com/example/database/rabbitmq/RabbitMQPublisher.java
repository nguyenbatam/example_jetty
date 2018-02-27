package com.example.database.rabbitmq;

import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.example.database.rabbitmq.base.RabbitMQPool;
import com.example.eventbus.interaction.Message;
import com.rabbitmq.client.Channel;

public class RabbitMQPublisher {
	public static void publishEvent(Message message) {
		RabbitMQPool rabbitMQPool = PubSubRabbitMQPool.getRabbitMQPool(message.interaction.name());
		if (rabbitMQPool == null)
			rabbitMQPool = PubSubRabbitMQPool.getRabbitMQPool(message.interaction.name());
		Channel channel = rabbitMQPool.getChanel();
		if (channel == null)
			channel = PubSubRabbitMQPool.getRabbitMQPool(message.interaction.name()).getChanel();
		if (channel == null)
			return;
		try {
			channel.basicPublish(message.interaction.name(), "", null, message.toString().getBytes());
		} catch (Exception e) {
			Logger.getRootLogger().error(ExceptionUtils.getStackTrace(e));
		} finally {
			PubSubRabbitMQPool.getRabbitMQPool(message.interaction.name()).returnChanel(channel);
		}
	}

	public static void publishEvent(List<Message> messages) {
		for (Message message : messages)
			try {
				publishEvent(message);
			} catch (Exception e) {
				Logger.getRootLogger().error(ExceptionUtils.getStackTrace(e));
			}
	}
}
