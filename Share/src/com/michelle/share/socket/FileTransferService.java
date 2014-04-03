package com.michelle.share.socket;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.Arrays;

import com.michelle.share.ChatActivity;
import com.michelle.share.Contact;
import com.michelle.share.ShareApplication;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class FileTransferService extends IntentService {
	public static final String ACTION_SEND_FILE = "com.michelle.share.socket.SEND_FILE";
	public static final String EXTRAS_FILE_PATH = "file_url";
    public static final String EXTRAS_OSTREAM = "ostream";
	private static final String TAG = "FileTransferService";
	public static final String ACTION_SEND_CONTACT = "com.michelle.share.socket.SEND_CONTACT";
	public static final String CONTACT = "contact";
    
	public FileTransferService(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}
	
	public FileTransferService() {
        super("FileTransferService");
    }

	@Override
	protected void onHandleIntent(Intent intent) {
		Context context = getApplicationContext();
        if (intent.getAction().equals(ACTION_SEND_FILE)) {
            String fileUri = intent.getExtras().getString(EXTRAS_FILE_PATH);
            
            try {
            	OutputStream oStream = ((ShareApplication) getApplication()).getoStream();
//            	OutputStream oStream = ((ShareApplication) getApplication()).getTransferSocket().getOutputStream();
            	DataOutputStream dataOutStream = new DataOutputStream(oStream);
            	dataOutStream.writeUTF("image" + System.currentTimeMillis());
            	dataOutStream.flush();
                ContentResolver cr = context.getContentResolver();
                InputStream iStream = null;
                try {
                	iStream = cr.openInputStream(Uri.parse(fileUri));
                } catch (FileNotFoundException e) {
                    Log.d(ChatActivity.TAG, e.toString());
                }
                
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
//        else if (intent.getAction().equals(ACTION_SEND_CONTACT)) {
//        	Contact contact = (Contact) intent.getExtras().getSerializable(CONTACT);
//        	
//        	try {
//        		//is = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));  
//                ObjectOutputStream os = new ObjectOutputStream(((ShareApplication) getApplication()).getoStream());
//                os.writeUTF("contact");
//                os.flush();
//                
//                os.writeObject(contact);
//                os.flush();                
//        	} catch (Exception e) {
//        		Log.e(ChatActivity.TAG, e.getMessage());
//        	}
//        }
	}

}
