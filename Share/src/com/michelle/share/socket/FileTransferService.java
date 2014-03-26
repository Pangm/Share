package com.michelle.share.socket;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import com.michelle.share.ChatActivity;
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
                ContentResolver cr = context.getContentResolver();
                InputStream iStream = null;
                try {
                	iStream = cr.openInputStream(Uri.parse(fileUri));
                } catch (FileNotFoundException e) {
                    Log.d(ChatActivity.TAG, e.toString());
                }
                
                //FileTransferManager.copyFile(iStream, oStream);
                
                byte buf[] = new byte[1024];
        		int len;
        		try {
        			while ((len = iStream.read(buf)) != -1) {
        				oStream.write(buf, 0, len);
        			}
        			Arrays.fill(buf, (byte) 0);
        			oStream.write(buf);
        			iStream.close();
        		} catch (IOException e) {
        			Log.d(TAG, e.toString());
        		}
                Log.d(ChatActivity.TAG, "Client: Data written");
            } catch (Exception e) {
                Log.e(ChatActivity.TAG, e.getMessage());
            }
        }
	}

}
