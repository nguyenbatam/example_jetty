package com.example.eventbus.publish;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.example.database.rabbitmq.RabbitMQPublisher;
import com.example.eventbus.interaction.Message;

public class ThreadPublisher implements Runnable {
	public List<Message> messages = new ArrayList<>();

	public void run() {
		while (true) {
			try {
				List<Message> publishMessages = getMessges();
				if (publishMessages.size() > 0) {
					RabbitMQPublisher.publishEvent(publishMessages);
					removeMessgesDone(publishMessages);
				} else
					try {
						Thread.sleep(InteractPublisher.waitTimePerPublisher);
					} catch (Exception e) {
						Logger.getRootLogger().error(ExceptionUtils.getStackTrace(e));
					}
			} catch (Exception e) {
				Logger.getRootLogger().error(ExceptionUtils.getStackTrace(e));
			}
		}
	}

	public void add(Message message) {
		try {
			synchronized (this.messages) {
				this.messages.add(message);
			}
		} catch (Exception e) {
			Logger.getRootLogger().error(ExceptionUtils.getStackTrace(e));
		}
	}

	public List<Message> getMessges() {
		List<Message> publishMessages = new ArrayList<>();
		try {
			synchronized (this.messages) {
				publishMessages.addAll(this.messages);
			}
		} catch (Exception e) {
			Logger.getRootLogger().error(ExceptionUtils.getStackTrace(e));
		}
		return publishMessages;
	}

	public void removeMessgesDone(List<Message> publishMessages) {
		try {
			synchronized (this.messages) {
				this.messages.removeAll(publishMessages);
			}
		} catch (Exception e) {
			Logger.getRootLogger().error(ExceptionUtils.getStackTrace(e));
		}
	}
}

