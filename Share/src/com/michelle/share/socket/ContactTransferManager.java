/**
 * 
 */
package com.michelle.share.socket;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

import com.michelle.share.Contact;
import com.michelle.share.ImageFile;

import android.os.Environment;
import android.os.Handler;
import android.text.format.Time;
import android.util.Log;

/**
 * @author Weizhu
 *
 */
public class ContactTransferManager implements Runnable {

	private static final String TAG = "ContactTransferManager";
	private Socket socket;
	private Handler handler;
	private InputStream iStream;
	private OutputStream oStream;

	public ContactTransferManager(Socket socket, Handler handler) {
		super();
		this.socket = socket;
		this.handler = handler;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			iStream = socket.getInputStream();
			oStream = socket.getOutputStream();
			byte[] endBuf = new byte[1024];
			Arrays.fill(endBuf, (byte) 0);
			
			handler.obtainMessage(ShareChatService.CONTACT_TRANSFER_HANDLE, this)
					.sendToTarget();
			ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(iStream)); 
			while (true) {
				try {
					// Read from the InputStream
					Log.d(this.TAG, "Server: connection done");
					Object obj = ois.readObject();  
					Contact contact = (Contact) obj;
			
					Time time = new Time();
					time.setToNow();
					
					handler.obtainMessage(ShareChatService.CONTACT_READ, 0, -1,
							contact).sendToTarget();
				} catch (IOException e) {
					Log.e(TAG, "disconnected", e);
				} 
			}
		} catch (Exception e) {
			
		}
		

	}

	public Socket getSocket() {
		return socket;
	}

}
