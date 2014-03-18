/**
 * 
 */
package com.michelle.share;

import java.util.ArrayList;
import java.util.List;

import android.app.Application;

/**
 * @author Weizhu
 *
 */
public class ShareApplication extends Application {

	private List<ChatMessage> messages;
	private boolean isConnected = false;
	
	/* (non-Javadoc)
	 * @see android.app.Application#onCreate()
	 */
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
		messages = new ArrayList<ChatMessage>();
	}

	/**
	 * @return the messages
	 */
	public List<ChatMessage> getMessages() {
		return messages;
	}

	/* (non-Javadoc)
	 * @see android.app.Application#onTerminate()
	 */
	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub
		super.onTerminate();
	}

	/* (non-Javadoc)
	 * @see android.app.Application#onLowMemory()
	 */
	@Override
	public void onLowMemory() {
		// TODO Auto-generated method stub
		super.onLowMemory();
	}

	/**
	 * @return the isConnected
	 */
	public Boolean getIsConnected() {
		return isConnected;
	}

	/**
	 * @param isConnected the isConnected to set
	 */
	public void setIsConnected(boolean isConnected) {
		this.isConnected = isConnected;
	}


}
