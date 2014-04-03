package com.michelle.share.socket;

import java.io.IOException;

import com.michelle.share.ChatMessage;
import com.michelle.share.FriendsFragment.MessageTarget;
import com.michelle.share.ShareApplication;
import com.michelle.share.socket.FileTransferManager.ImageFile;

import android.app.Service;
import android.content.Context;
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
	public static final int CHAT_MSG_HANDLE = 0x400 + 2;
	public static final int FILE_READ = 0x400 + 3;
	public static final int FILE_TRANSFER_HANDLE = 0x400 + 4;
	public static final int FILE_NAME = 0x400 + 5;
	
	static final int SERVER_PORT = 4545;
	static final int SERVER_FILE_PORT = 4546;
	private static final String TAG = "Share ChatSevice";
	
	
	private ChatManager chatManager = null;
	private FileTransferManager fileTransferManager = null;
	/**
	 * @return the fileTransferManager
	 */
	public FileTransferManager getFileTransferManager() {
		return fileTransferManager;
	}

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
			intent.setAction("android.intent.action.MSG_RECEIVE");// action与接收器相同
			sendBroadcast(intent);
			break;
		case FILE_READ:
			//Log.d(TAG, (String) msg.obj);
//			Time time2 = new Time();
//			time2.setToNow();
//			ChatMessage chatMsg2 = new ChatMessage(ChatMessage.MESSAGE_FROM,
//					(String) msg.obj, time2);
//			// (chatFragment).pushMessage("Buddy: " + readMessage);
//			((ShareApplication) getApplication()).getMessages().add(chatMsg2);
//
//			Intent intent2 = new Intent();
//			intent2.putExtra("Msg", (String) msg.obj);
//			intent2.setAction("android.intent.action.MSG_RECEIVE");// action与接收器相同
//			sendBroadcast(intent2);
			Time time2 = new Time();
			time2.setToNow();
			ChatMessage chatMsg2 = new ChatMessage(ChatMessage.MESSAGE_FROM,
					msg.obj, time2);
			((ShareApplication) getApplication()).getMessages().add(chatMsg2);

			Intent intent2 = new Intent();
			//intent2.putExtra("Msg", (ImageFile) msg.obj);
			intent2.setAction("android.intent.action.MSG_RECEIVE");// action与接收器相同
			sendBroadcast(intent2);
			break;
		case FILE_NAME:
			byte[] readBuf1 = (byte[]) msg.obj;
			// construct a string from the valid bytes in the buffer
			String readMessage1 = new String(readBuf1, 0, msg.arg1);
			Log.d(TAG, "File Name:" + readMessage1);
			Time time1 = new Time();
			time1.setToNow();
			ChatMessage chatMsg1 = new ChatMessage(ChatMessage.MESSAGE_FROM,
					readMessage1, time1);
			// (chatFragment).pushMessage("Buddy: " + readMessage);
			((ShareApplication) getApplication()).getMessages().add(chatMsg1);

			Intent intent1 = new Intent();
			intent1.putExtra("Msg", readMessage1);
			intent1.setAction("android.intent.action.MSG_RECEIVE");// action与接收器相同
			sendBroadcast(intent1);
			break;
		case CHAT_MSG_HANDLE:
			Object obj = msg.obj;
			// (chatFragment).setChatManager((ChatManager) obj);
			chatManager = (ChatManager) obj;
			break;
		case FILE_TRANSFER_HANDLE:
			fileTransferManager = (FileTransferManager) msg.obj;
			((ShareApplication) getApplication()).setoStream(fileTransferManager.getoStream());
			
			break;
		default:
			break;
		}

		return true;
	}

	/**
	 * @return the chatManager
	 */
	public ChatManager getChatManager() {
		return chatManager;
	}

	@Override
	public Handler getHandler() {
		// TODO Auto-generated method stub
		return myHandler;
	}

	public class MyBinder extends Binder {

		public void sendMsg(MsgType type, Object obj) {

			switch (type) {
			case MESSAGE:
				chatManager.write(((String) obj).getBytes());
				break;
			case CONTACT:
				break;
			case PHOTO:
				fileTransferManager.write(((String)obj).getBytes());
				break;
			case MUSIC:
				break;
			case VIDEO:
				break;
			case APP:
				break;
			case OTHER_FILE:
				break;
			default:
				break;

			}
		}
		
		public void sendFileName(String fileName) {
			getFileTransferManager().write(fileName.getBytes());		
		}

		public void connect(WifiP2pInfo p2pInfo) {
			Thread Chathandler = null;
			Thread FileTransferhandler = null;

			if (p2pInfo.isGroupOwner) {
				Log.d(TAG, "Connected as group owner");
				try {
					Chathandler = new GroupOwnerSocketHandler(getHandler(),
							ShareChatService.SERVER_PORT, 0);
					Chathandler.start();
					
					FileTransferhandler = new GroupOwnerSocketHandler(getHandler(),
							ShareChatService.SERVER_FILE_PORT, 1);
					FileTransferhandler.start();
				} catch (IOException e) {
					Log.d(TAG,
							"Failed to create a server thread - "
									+ e.getMessage());
					return;
				}
			} else {
				Log.d(TAG, "Connected as peer");
				Chathandler = new ClientSocketHandler(getHandler(),
						p2pInfo.groupOwnerAddress, ShareChatService.SERVER_PORT, 0);
				Chathandler.start();
				
				FileTransferhandler = new ClientSocketHandler(getHandler(),
						p2pInfo.groupOwnerAddress, ShareChatService.SERVER_FILE_PORT, 1);
				FileTransferhandler.start();
			}
		}

	}
}
