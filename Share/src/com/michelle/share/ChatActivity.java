package com.michelle.share;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.michelle.share.socket.FileTransferService;
import com.michelle.share.socket.MsgType;
import com.michelle.share.socket.ShareChatService;
import com.michelle.share.socket.ShareChatService.MyBinder;

import android.net.Uri;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.provider.MediaStore.MediaColumns;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.support.v4.app.NavUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class ChatActivity extends Activity {

	public static final String TAG = "ChatActivity";
	protected static final int CHOOSE_FILE_RESULT_CODE = 20;
	protected static final int CHOOSE_PHOTO_RESULT_CODE = 21;
	protected static final int CHOOSE_MUSIC_RESULT_CODE = 22;
	protected static final int CHOOSE_VIDEO_RESULT_CODE = 23;
	protected static final int CHOOSE_CONTACT_RESULT_CODE = 24;
	private ListView chatList = null;
	private ChatAdapter chatAdapter = null;
	private List<ChatMessage> messages;
	private Button sendBtn;
	private EditText mgsText;
	private MyBinder myBinder = null;
	private MyReceiver receiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_chat);
		
		Intent bindIntent = new Intent(this, ShareChatService.class);  
        bindService(bindIntent, new ServiceConnection() {  
        	  
            @Override  
            public void onServiceDisconnected(ComponentName name) {  
            }

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				myBinder = (MyBinder) service; 
				
			}}, BIND_AUTO_CREATE);
		
        receiver = new MyReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.intent.action.MSG_RECEIVE");
		registerReceiver(receiver,filter);
        
		// set chatList adapter
		chatList = (ListView) findViewById(R.id.chat_list);
		messages = ((ShareApplication) getApplication()).getMessages();
		chatAdapter = new ChatAdapter(this, messages);
		chatList.setAdapter(chatAdapter);
		
		sendBtn = (Button) findViewById(R.id.btn_send);
		mgsText = (EditText) findViewById(R.id.msg_text);
		sendBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String str = mgsText.getText().toString();// ��ȡ��ǰ��������
				String msg;
				if (str != null
						&& (msg = str.trim().replaceAll("\r", "")
								.replaceAll("\t", "").replaceAll("\n", "")
								.replaceAll("\f", "")) != "") {
					sendMessage(msg);

				}
				mgsText.setText("");
			}
			
			// Send message
			private void sendMessage(String msg) {
				Time time = new Time("GMT+8");
				time.setToNow();
				messages.add(new ChatMessage(ChatMessage.MESSAGE_TO, msg, time));
				myBinder.sendMsg(MsgType.MESSAGE, msg);				
				chatAdapter.notifyDataSetChanged();
			}
		});
		
		
		final GridView fileUnits = (GridView) findViewById(R.id.file_units);
		initFileUnits(fileUnits);
		fileUnits.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				fileUnits.setVisibility(View.GONE);
				TextView text = (TextView) view.findViewById(R.id.file_unit_text);
				
				if (text.getText().equals("ͼƬ")) {
					Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(intent, CHOOSE_PHOTO_RESULT_CODE);
				} else if (text.getText().equals("����")) {
					Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("audio/*");
                    startActivityForResult(intent, CHOOSE_MUSIC_RESULT_CODE);
				} else if (text.getText().equals("��Ƶ")) {
					Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
	                intent.setType("video/*");
	                startActivityForResult(intent, CHOOSE_VIDEO_RESULT_CODE);
				} else if (text.getText().equals("��ϵ��")) {
					Intent intent = new Intent(Intent.ACTION_PICK);
					intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
	                startActivityForResult(intent, CHOOSE_CONTACT_RESULT_CODE);
				}
				
				 
			}
		});
		
		mgsText.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (View.VISIBLE == fileUnits.getVisibility()) {
					fileUnits.setVisibility(View.GONE);
				}
			}
		});
		mgsText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (View.VISIBLE == fileUnits.getVisibility()) {
					fileUnits.setVisibility(View.GONE);
				}
			}
		});
		
		ImageView imageV = (ImageView) this.findViewById(R.id.file_imgview);
		imageV.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {				
				InputMethodManager imm = (InputMethodManager) getApplicationContext().
						getSystemService(INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mgsText.getWindowToken(), 0);
				fileUnits.setVisibility(View.VISIBLE);
			}
		});
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // User has picked an image. Transfer it to group owner i.e peer using
        // FileTransferService.
        Uri uri = data.getData();
        if (requestCode == CHOOSE_PHOTO_RESULT_CODE) {
        	//myBinder.sendMsg(MsgType.PHOTO, uri.getPath());
        	
        	Intent serviceIntent = new Intent(this, FileTransferService.class);
            serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
            serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, uri.toString());
            startService(serviceIntent);
        }
//        String filePath;  
//        String[] filePathColumn = {MediaColumns.DATA};
//        Log.d(ChatActivity.TAG, "Intent----------- " + uri);
//        Cursor cursor = getContentResolver().query(data.getData(), filePathColumn, null, null, null);
//        cursor.moveToFirst();
//        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);  
//        filePath = cursor.getString(columnIndex);  
//        cursor.close();
//        myBinder.sendFileName(filePath);
        //myBinder.sendFileName(uri.toString());
//        Context context = getApplicationContext();
//        ContentResolver cr = context.getContentResolver();
//        InputStream is = null;
//        try {
//            is = cr.openInputStream(Uri.parse(uri.toString()));
//        } catch (FileNotFoundException e) {
//            Log.d(ChatActivity.TAG, e.toString());
//        }
    }
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(receiver);
	}

	private void initFileUnits(GridView fileUnits) {
		ArrayList<Map<String, Object>> fileUnitsList = new ArrayList<Map<String, Object>>();
		HashMap<String, Object> unit = new HashMap<String, Object>();

		unit.put("unitImg", R.drawable.ic_photo);
		unit.put("unitText", "ͼƬ");
		fileUnitsList.add(unit);
		
		unit = new HashMap<String, Object>();
		unit.put("unitImg", R.drawable.ic_music);
		unit.put("unitText", "����");
		fileUnitsList.add(unit);
		
		unit = new HashMap<String, Object>();
		unit.put("unitImg", R.drawable.ic_txt);
		unit.put("unitText", "�ĵ�");
		fileUnitsList.add(unit);
		
		unit = new HashMap<String, Object>();
		unit.put("unitImg", R.drawable.ic_receive_files);
		unit.put("unitText", "�ռ���");
		fileUnitsList.add(unit);
		
		unit = new HashMap<String, Object>();
		unit.put("unitImg", R.drawable.ic_video);
		unit.put("unitText", "��Ƶ");
		fileUnitsList.add(unit);
		
		unit = new HashMap<String, Object>();
		unit.put("unitImg", R.drawable.ic_apk);
		unit.put("unitText", "Ӧ�ó���");
		fileUnitsList.add(unit);
		
		unit = new HashMap<String, Object>();
		unit.put("unitImg", R.drawable.ic_tel_rom);
		unit.put("unitText", "��ϵ��");
		fileUnitsList.add(unit);
		
		unit = new HashMap<String, Object>();
		unit.put("unitImg", R.drawable.ic_sd_rom);
		unit.put("unitText", "SD�ڴ�");
		fileUnitsList.add(unit);
		
	
//		
//		unit = new HashMap<String, Object>();
//		unit.put("unitImg", R.drawable.apk);
//		unit.put("unitText", "Ӧ�ó���");
//		fileUnitsList.add(unit);	
	
		SimpleAdapter dataAdapter = new SimpleAdapter(
			this,
			fileUnitsList,
			R.layout.file_unit,
			new String[]{"unitImg", "unitText"},
			new int[] {R.id.file_unit_img, R.id.file_unit_text});
	
		fileUnits.setAdapter(dataAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.chat, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    // Respond to the action bar's Up/Home button
	    case android.R.id.home:
	        NavUtils.navigateUpFromSameTask(this);
	        return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
	
	public class MyReceiver extends BroadcastReceiver {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			System.out.println("OnReceiver");
			// notify receive a message. 
			chatAdapter.notifyDataSetChanged(); 
		}
		public MyReceiver(){
			System.out.println("MyReceiver");
		} 
	}
}
