package com.michelle.share;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.michelle.share.socket.ShareChatService;
import com.michelle.share.socket.ShareChatService.MyBinder;

import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.support.v4.app.NavUtils;
import android.text.format.Time;
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

public class ChatActivity extends Activity {

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
		filter.addAction("android.intent.action.test");
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
				String str = mgsText.getText().toString();// 获取当前输入内容
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
		unit.put("unitText", "图片");
		fileUnitsList.add(unit);
		
		unit = new HashMap<String, Object>();
		unit.put("unitImg", R.drawable.ic_music);
		unit.put("unitText", "音乐");
		fileUnitsList.add(unit);
		
		unit = new HashMap<String, Object>();
		unit.put("unitImg", R.drawable.ic_txt);
		unit.put("unitText", "文档");
		fileUnitsList.add(unit);
		
		unit = new HashMap<String, Object>();
		unit.put("unitImg", R.drawable.ic_receive_files);
		unit.put("unitText", "收件夹");
		fileUnitsList.add(unit);
		
		unit = new HashMap<String, Object>();
		unit.put("unitImg", R.drawable.ic_video);
		unit.put("unitText", "视频");
		fileUnitsList.add(unit);
		
		unit = new HashMap<String, Object>();
		unit.put("unitImg", R.drawable.ic_apk);
		unit.put("unitText", "应用程序");
		fileUnitsList.add(unit);
		
		unit = new HashMap<String, Object>();
		unit.put("unitImg", R.drawable.ic_tel_rom);
		unit.put("unitText", "手机内存");
		fileUnitsList.add(unit);
		
		unit = new HashMap<String, Object>();
		unit.put("unitImg", R.drawable.ic_sd_rom);
		unit.put("unitText", "SD内存");
		fileUnitsList.add(unit);
		
	
//		
//		unit = new HashMap<String, Object>();
//		unit.put("unitImg", R.drawable.apk);
//		unit.put("unitText", "应用程序");
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
		//自定义一个广播接收器
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			System.out.println("OnReceiver");
			Bundle bundle=intent.getExtras();
			String a = bundle.getString("Msg");
			chatAdapter.notifyDataSetChanged(); 
		}
		public MyReceiver(){
			System.out.println("MyReceiver");
			//构造函数，做一些初始化工作，本例中无任何作用
		}
 
	}
}
