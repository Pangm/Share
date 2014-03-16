package com.michelle.share;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;

public class ChatActivity extends Activity {

	private ListView chatList = null;
	private ChatAdapter chatAdapter = null;
	private List<ChatMessage> messages = new ArrayList<ChatMessage>();
	private Button sendBtn;
	private EditText mgsText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_chat);
		
		// set chatList adapter
		chatList = (ListView) findViewById(R.id.chat_list);
		chatAdapter = new ChatAdapter(this, messages);
		chatList.setAdapter(chatAdapter);
		
		sendBtn = (Button) findViewById(R.id.btn_send);
		mgsText = (EditText) findViewById(R.id.msg_text);
		sendBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
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
				messages.add(new ChatMessage(ChatMessage.MESSAGE_TO, msg));
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
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	private void initFileUnits(GridView fileUnits) {
		// TODO Auto-generated method stub
		
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

}
