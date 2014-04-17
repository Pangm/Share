package com.michelle.share.socket;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

import com.michelle.share.ChatActivity;
import com.michelle.share.ChatMessage;
import com.michelle.share.Contact;
import com.michelle.share.ImageFile;
import com.michelle.share.ShareApplication;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.MediaColumns;
import android.text.format.Time;
import android.util.Log;

public class TransferService extends IntentService {
	public static final String ACTION_SEND_FILE = "com.michelle.share.socket.SEND_FILE";
	public static final String EXTRAS_FILE_PATH = "file_url";
    public static final String EXTRAS_OSTREAM = "ostream";
	private static final String TAG = "FileTransferService";
	public static final String ACTION_SEND_CONTACT = "com.michelle.share.socket.SEND_CONTACT";
	public static final String CONTACT = "contact";
    
	public TransferService(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}
	
	public TransferService() {
        super("TransferService");
    }

	@Override
	protected void onHandleIntent(Intent intent) {
		Context context = getApplicationContext();
        if (intent.getAction().equals(ACTION_SEND_FILE)) {
            String fileUri = intent.getExtras().getString(EXTRAS_FILE_PATH);
            
            try {
            	OutputStream oStream = ((ShareApplication) getApplication()).getoStream();
            	DataOutputStream dataOutStream = new DataOutputStream(oStream);
            	String fileName = ("image" + System.currentTimeMillis());
            	Float fileSize = 0f;
            	String filePath = fileUri;
            	Time time = new Time();
            	dataOutStream.writeUTF(fileName);
            	dataOutStream.flush();
                ContentResolver cr = context.getContentResolver();
                InputStream iStream = null;
                try {
                	iStream = cr.openInputStream(Uri.parse(fileUri));
                	fileSize = (float) (iStream.available() / 1024);
                	dataOutStream.writeInt(iStream.available());
                	dataOutStream.flush();
                } catch (FileNotFoundException e) {
                    Log.d(ChatActivity.TAG, e.toString());
                } catch (IOException e) {
                	Log.d(ChatActivity.TAG, e.toString());
                }
                
                time.setToNow();
                ImageFile imageFile = new ImageFile(fileName, fileSize, filePath, time);
                
                // notify the UI that we are goting to send a file.
                List<ChatMessage> messages = ((ShareApplication) getApplication()).getMessages();
                messages.add(new ChatMessage(ChatMessage.MESSAGE_TO, imageFile, imageFile.getTime()));
                
                intent = new Intent();
    			intent.setAction("android.intent.action.MSG_RECEIVE");// action与接收器相同
    			sendBroadcast(intent);
    			
    			// write the file content
                byte buf[] = new byte[1024 * 60];
        		int byteCount = 0;
        		try {
        			while ((byteCount = iStream.read(buf)) != -1) {
        				dataOutStream.writeInt(byteCount);
        				dataOutStream.flush();
        				dataOutStream.write(buf, 0, byteCount);
        				dataOutStream.flush();
        			}
        			dataOutStream.writeInt(0);
        			dataOutStream.flush();
        			iStream.close();
        		} catch (IOException e) {
        			Log.d(TAG, e.toString());
        		}
                Log.d(ChatActivity.TAG, "Client: Data written");
            } catch (Exception e) {
                Log.e(ChatActivity.TAG, e.getMessage());
            }
        } 
        else if (intent.getAction().equals(ACTION_SEND_CONTACT)) {
        	Contact contact = (Contact) intent.getExtras().getSerializable(CONTACT);
        	Time time = new Time();
        	time.setToNow();
        	
        	List<ChatMessage> messages = ((ShareApplication) getApplication()).getMessages();
            messages.add(new ChatMessage(ChatMessage.MESSAGE_TO, contact, time));
            
        	intent = new Intent();
			intent.setAction("android.intent.action.MSG_RECEIVE");// action与接收器相同
			sendBroadcast(intent);
        	
        	try {
        		Socket socket = ((ShareApplication) getApplication()).getContactTransferSocket();
                ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
                os.writeObject(contact);
                os.flush();                
        	} catch (Exception e) {
        		Log.e(ChatActivity.TAG, e.getMessage());
        	}
        }
	}
	
	/**
	 * Gets the corresponding path to a file from the given content:// URI
	 * @param selectedVideoUri The content:// URI to find the file path from
	 * @param contentResolver The content resolver to use to perform the query.
	 * @return the file path as a string
	 */
	public static String getFilePathFromContentUri(Uri selectedVideoUri,
	        ContentResolver contentResolver) {
	    String filePath;
	    String[] filePathColumn = {MediaColumns.DATA};

	    Cursor cursor = contentResolver.query(selectedVideoUri, filePathColumn, null, null, null);
	    
	    cursor.moveToFirst();

	    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
	    filePath = cursor.getString(columnIndex);
	    cursor.close();
	    return filePath;
	}

}
