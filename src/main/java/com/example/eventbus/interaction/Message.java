package com.example.eventbus.interaction;

import net.arnx.jsonic.JSON;

public class Message {
	public long actionAuthorId;
	public long timeStamp;
	public String content;
	public EInteract interaction;

	public String toString() {
		String result = "{}";
		try {
			result = JSON.encode(this);
		} catch (Exception e) {
		}

		return result;
	}
}

