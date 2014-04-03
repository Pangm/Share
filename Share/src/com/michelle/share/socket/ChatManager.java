package com.michelle.share.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import android.content.Context;
import android.os.Handler;
import android.text.format.Time;
import android.util.Log;

public class ChatManager implements Runnable {

	private Socket socket = null;
	private Handler handler;

	private InputStream iStream;
	private OutputStream oStream;
	private static final String TAG = "ChatHandler";

	public ChatManager(Socket socket, Handler handler) {
		super();
		this.socket = socket;
		this.handler = handler;
	}

	@Override
	public void run() {
		try {

			iStream = socket.getInputStream();
			oStream = socket.getOutputStream();
			byte[] buffer = new byte[1024];
			int bytes;
			handler.obtainMessage(ShareChatService.CHAT_MSG_HANDLE, this)
					.sendToTarget();

			while (true) {
				try {
					// Read from the InputStream
					bytes = iStream.read(buffer);
					if (bytes == -1) {
						break;
					}

					// Send the obtained bytes to the UI Activity
					Log.d(TAG, "Rec:" + String.valueOf(buffer));
					handler.obtainMessage(ShareChatService.MESSAGE_READ, bytes,
							-1, buffer).sendToTarget();
				} catch (IOException e) {
					Log.e(TAG, "disconnected", e);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @return the socket
	 */
	public Socket getSocket() {
		return socket;
	}

	public void write(byte[] buffer) {
		try {
			oStream.write(buffer);
			System.out.println(buffer.length);
			oStream.flush();
		} catch (IOException e) {
			Log.e(TAG, "Exception during write", e);
		}
	}

}
