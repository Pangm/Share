package com.michelle.share;

import java.io.Serializable;

import android.text.format.Time;

public class ImageFile implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2480574030755199339L;
	private int id = 0;
	private float size;
	private String name;
	private String path;
	private Time time;
	private int type;
	
	public ImageFile(int id, String name, float size, String path, Time time) {
		super();
		imageFile(id, name, size, path, time);
	}
	
	public ImageFile(String name, float size, String path, Time time) {
		imageFile(0, name, size, path, time);
	}
	
	private void imageFile(int id, String name, float size, String path,
			Time time) {
		this.id = id;
		this.size = size;
		this.name = name;
		this.path = path;
		this.time = time;
		this.type = 0;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the size
	 */
	public float getSize() {
		return size;
	}

	/**
	 * @param size the size to set
	 */
	public void setSize(float size) {
		this.size = size;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
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

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
}
