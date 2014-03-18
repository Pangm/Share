package com.michelle.share.socket;

import java.io.IOException;

import com.michelle.share.ChatMessage;
import com.michelle.share.FriendsFragment.MessageTarget;
import com.michelle.share.ShareApplication;

import android.app.Service;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.format.Time;
import android.util.Log;

public class ShareChatService extends Service implements Handler.Callback,
 		MessageTarget {

	public static final int MESSAGE_READ = 0x400 + 1;
	public static final int MY_HANDLE = 0x400 + 2;
	static final int SERVER_PORT = 4545;
	private static final String TAG = "Share ChatSevice";

	private ChatManager chatManager = null;
	Handler myHandler = new Handler(this);
	private MyBinder mBinder = new MyBinder();

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBinder;
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case MESSAGE_READ:
			byte[] readBuf = (byte[]) msg.obj;
			// construct a string from the valid bytes in the buffer
			String readMessage = new String(readBuf, 0, msg.arg1);
			Log.d(TAG, readMessage);
			Time time = new Time();
			time.setToNow();
			ChatMessage chatMsg = new ChatMessage(ChatMessage.MESSAGE_FROM,
					readMessage, time);
			// (chatFragment).pushMessage("Buddy: " + readMessage);
			((ShareApplication) getApplication()).getMessages().add(chatMsg);
			
			Intent intent = new Intent();
			intent.putExtra("Msg", readMessage);
			intent.setAction("android.intent.action.MSG_RECEIVE");//action与接收器相同
			sendBroadcast(intent);
			
			break;

		case MY_HANDLE:
			Object obj = msg.obj;
			// (chatFragment).setChatManager((ChatManager) obj);
			setChatManager((ChatManager) obj);

		}

		return true;
	}

	/**
	 * @return the chatManager
	 */
	public ChatManager getChatManager() {
		return chatManager;
	}

	/**
	 * @param chatManager
	 *            the chatManager to set
	 */
	public void setChatManager(ChatManager chatManager) {
		this.chatManager = chatManager;
	}

	@Override
	public Handler getHandler() {
		// TODO Auto-generated method stub
		return myHandler;
	}
	
	public class MyBinder extends Binder {  
		  
		public void sendMsg(String msg) {
			chatManager.write(msg.getBytes());
		}
		
	    public void  connect(WifiP2pInfo p2pInfo) {
	    	Thread handler = null;
	    	
	    	if (p2pInfo.isGroupOwner) {
	            Log.d(TAG, "Connected as group owner");
	            try {
	                handler = new GroupOwnerSocketHandler(
	                        getHandler());
	                handler.start();
	            } catch (IOException e) {
	                Log.d(TAG,
	                        "Failed to create a server thread - " + e.getMessage());
	                return;
	            }
	        } else {
	            Log.d(TAG, "Connected as peer");
	            handler = new ClientSocketHandler(
	                    getHandler(),
	                    p2pInfo.groupOwnerAddress,
	                    ShareChatService.SERVER_PORT);
	            handler.start();
	        }
	    }  
	  
	} 
}
