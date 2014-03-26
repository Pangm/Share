package com.michelle.share.socket;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

import com.michelle.share.ShareApplication;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
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
			while (true) {
//				try {
//					// Read from the InputStream
//					bytes = iStream.read(buffer);
//					if (bytes == -1) {
//						break;
//					}
//
//					// Send the obtained bytes to the UI Activity
//					Log.d(TAG, "Rec:" + String.valueOf(buffer));
//					handler.obtainMessage(ShareChatService.FILE_NAME, bytes,
//							-1, buffer).sendToTarget();
//				} catch (IOException e) {
//					Log.e(TAG, "disconnected", e);
//				}
				try {
					// Read from the InputStream
					Log.d(FileTransferManager.TAG, "Server: connection done");
					String fileName = dataInputStream.readUTF();
					
					final File f = new File(
							Environment.getExternalStorageDirectory() + "/"
									+ "MichelleShare" + "/wifip2pshared-"
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
//						while ((len = iStream.read(buffer)) != -1) {
//							if (len == 1024 && compareBytes(endBuf, buffer)) {
//								break;
//							}
//							out.write(buffer, 0, len);	
//						}
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
					handler.obtainMessage(ShareChatService.FILE_READ, 0, -1,
							f.getAbsolutePath()).sendToTarget();
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
}
