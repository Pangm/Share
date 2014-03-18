package com.michelle.share;

import android.text.format.Time;

public class ChatMessage {
	public static final int MESSAGE_FROM = 0;
	public static final int MESSAGE_TO = 1;

	private int direction;
	private String content;
	private Time time;

	public ChatMessage(int direction, String content, Time time) {
		super();
		this.direction = direction;
		this.content = content;
		this.time = time;
	}

	/**
	 * @return the time
	 */
	public Time getTime() {
		return time;
	}

	/**
	 * @param time the time to set
	 */
	public void setTime(Time time) {
		this.time = time;
	}

	public int getDirection() {
		return direction;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public CharSequence getContent() {
		return content;
	}
}
