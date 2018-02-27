package com.example.database.rabbitmq;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.example.eventbus.interaction.EInteract;
import com.example.eventbus.interaction.Message;
import com.example.eventbus.interaction.VideoInteract;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import net.arnx.jsonic.JSON;

public abstract class RabbitMQSubscriber  {
	private List<String> exchangeNames = new ArrayList<>();
	private String queueName;
	private boolean durable = true;

	public RabbitMQSubscriber() {

		this.queueName = getClass().getSimpleName();
	}

	public RabbitMQSubscriber(String queueName) {
		this.queueName = queueName;
	}

	public RabbitMQSubscriber(String queueName, boolean durable) {
		this.queueName = queueName;
		this.durable = durable;
	}

	public void addChannel(String channel) {
		this.exchangeNames.add(channel);
	}

	public void addChannel(EInteract channel) {
		this.exchangeNames.add(channel.name());
	}

	public Channel start() {

		/**
		 * create chanel queue
		 */
		Channel channel = null;
		Consumer consumer = null;

		try {
			System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
			System.out.println(this.queueName + " ===> " + this.exchangeNames);
			Connection connection = PubSubRabbitMQPool.getRabbitMQPool().getChanel().getConnection();
			channel = connection.createChannel();
			String queue = channel.queueDeclare(queueName, durable, false, false, null).getQueue();
			for (int i = 0; i < this.exchangeNames.size(); i++) {
				channel.queueBind(queue, exchangeNames.get(i), "");
			}
			consumer = new DefaultConsumer(channel) {
				public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
						byte[] body) {
					try {
						String message = new String(body, "UTF-8");
						onMessage(envelope.getExchange(), message);
					} catch (Exception e) {
						Logger.getRootLogger().error(ExceptionUtils.getStackTrace(e));
					}
				}
			};
			if ((channel != null) && (consumer != null))
				channel.basicConsume(queueName, true, consumer);
		} catch (Exception e) {
			Logger.getRootLogger().error(ExceptionUtils.getStackTrace(e));
			System.out.println("create channel error ");
			if (channel != null)
				try {
					channel.close();
					channel = null;
				} catch (Exception e2) {
					Logger.getRootLogger().error(ExceptionUtils.getStackTrace(e2));
					channel = null;
				}
		}

		return channel;
	}

	private Message parseEvent(String message, Class<? extends Message> type) {
		try {
			return (Message) JSON.decode(message, type);
		} catch (Exception e) {
			Logger.getRootLogger().error(ExceptionUtils.getStackTrace(e));
		}

		return null;
	}

	public void onMessage(String channel, String message) {
		Logger.getRootLogger().debug(channel + " " + message);

		Message event = null;
		EInteract interaction = EInteract.valueOf(channel);
		switch (interaction) {
		case like_video:
			event = parseEvent(message, VideoInteract.class);
			break;
		case update_video_info:
			event = parseEvent(message, Message.class);
			break;
		case update_user_info:
			event = parseEvent(message, Message.class);
			break;
		default:
			event = null;
			break;
		}

		if (event != null) {
			try {
				onMessage(event);
			} catch (Exception e) {
				Logger.getRootLogger().error(ExceptionUtils.getStackTrace(e));
			}
		} else {
			Logger.getRootLogger().debug(" event == null");
		}
	}

	public abstract void onMessage(Message paramMessage) throws Exception;

}
