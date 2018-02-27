package com.example.eventbus.publish;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.example.database.rabbitmq.RabbitMQPublisher;
import com.example.eventbus.interaction.Message;

/**
 * in here it create a single thread for publishing event
 * @author tamnb
 *
 */
public class InteractPublisher {
	static ThreadPublisher threadPublisher = null;
	public static int waitTimePerPublisher = 1000;

	public static void initThreadPublisher() {
		threadPublisher = new ThreadPublisher();
		new Thread(threadPublisher).start();
	}

	public static void publishEvent(Message message) {
		if ((message != null) && (message.interaction.name() == null))
			try {
				if (threadPublisher != null)
					threadPublisher.add(message);
				else
					RabbitMQPublisher.publishEvent(message);
			} catch (Exception e) {
				Logger.getRootLogger().error(ExceptionUtils.getStackTrace(e));
			}
	}

}
