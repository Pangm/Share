package com.michelle.share.socket;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.Arrays;

import com.michelle.share.Contact;
import com.michelle.share.ShareApplication;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.text.format.Time;
import android.util.Log;

public class FileTransferManager implements Runnable {
	private Socket socket = null;
	private Handler handler;

	private InputStream iStream;
	private OutputStream oStream;
	/**
	 * @return the oStream
	 */
	public OutputStream getoStream() {
		return oStream;
	}

	private static final String TAG = "FileTransferHandler";

	public FileTransferManager(Socket socket, Handler handler) {
		super();
		this.socket = socket;
		this.handler = handler;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		try {
			iStream = socket.getInputStream();
			oStream = socket.getOutputStream();			
			byte[] buffer = new byte[1024];
			byte[] endBuf = new byte[1024];
			Arrays.fill(endBuf, (byte) 0);
			int bytes;
			handler.obtainMessage(ShareChatService.FILE_TRANSFER_HANDLE, this)
					.sendToTarget();
			DataInputStream dataInputStream = new DataInputStream(iStream);
			ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(iStream)); 
			while (true) {
				try {
					// Read from the InputStream
					Log.d(FileTransferManager.TAG, "Server: connection done");
					String fileName = dataInputStream.readUTF();
					try {
						if (fileName.equals("contact")) {
							 Object obj = ois.readObject();  
							 Contact contact = (Contact) obj;
						}
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					final File f = new File(
							Environment.getExternalStorageDirectory() + "/"
									+ "MichelleShare" + "/"
									+ fileName + ".jpg");
//									+ System.currentTimeMillis() + ".jpg");

					File dirs = new File(f.getParent());
					if (!dirs.exists())
						dirs.mkdirs();
					f.createNewFile();

					Log.d(FileTransferManager.TAG,
							"server: copying files " + f.toString());
					//copyFile(iStream, new FileOutputStream(f));
					FileOutputStream out = new FileOutputStream(f);
					int len;
					try {
						int byteCount = -1;
						byte buf[] = new byte[1024 * 60];
						while ((byteCount = dataInputStream.readInt()) != 0) {
							dataInputStream.readFully(buf, 0, byteCount);
							out.write(buf, 0, byteCount);
						}
						out.close();
					} catch (IOException e) {
						Log.d(FileTransferManager.TAG, e.toString());
					}
					// Send the obtained bytes to the UI Activity
					float size = f.length() / (1024);
					String name = f.getName();
					String path = f.getAbsolutePath();
					Time time = new Time();
					time.setToNow();
					
					ImageFile imageFile = new ImageFile(name, size, path, time);
					
//					handler.obtainMessage(ShareChatService.FILE_READ, 0, -1,
//							f.getAbsolutePath()).sendToTarget();
					handler.obtainMessage(ShareChatService.FILE_READ, 0, -1,
							imageFile).sendToTarget();
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

	public void write(byte[] buffer) {
		try {
			oStream.write(buffer);
			System.out.println(buffer.length);
			oStream.flush();
		} catch (IOException e) {
			Log.e(TAG, "Exception during write", e);
		}
	}
	
	static public Boolean compareBytes(byte[] buf1, byte[] buf2) {
		if (buf1.length == buf1.length) {
			int i = 0;
			while (i < buf1.length) {
				if (buf1[i] != buf2[i]) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	public class ImageFile implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 2480574030755199339L;
		private float size;
		private String name;
		private String path;
		private Time time;
		
		public ImageFile(String name, float size, String path,
				Time time) {
			this.size = size;
			this.name = name;
			this.path = path;
			this.time = time;
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

	/**
	 * @return the socket
	 */
	public Socket getSocket() {
		return socket;
	}
}
