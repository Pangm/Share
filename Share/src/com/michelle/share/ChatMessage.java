package com.michelle.share;

import android.text.format.Time;

public class ChatMessage {
	public static final int MESSAGE_FROM = 0;
	public static final int MESSAGE_TO = 1;

	private int direction;
	private Object content;
	private Time time;
	private int progressValue;

	public ChatMessage(int direction, Object content, Time time) {
		super();
		this.direction = direction;
		this.content = content;
		this.time = time;
		this.progressValue = 100;
	}

	public int getProgressValue() {
		return progressValue;
	}

	public void setProgressValue(int progressValue) {
		if (progressValue <= 100 || progressValue >= 0) {
			this.progressValue = progressValue;
		}
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

	public void setContent(Object content) {
		this.content = content;
	}

	public Object getContent() {
		return content;
	}
}
