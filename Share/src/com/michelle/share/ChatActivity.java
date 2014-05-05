package com.michelle.share;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.michelle.share.socket.TransferService;
import com.michelle.share.socket.MsgType;
import com.michelle.share.socket.ShareChatService;
import com.michelle.share.socket.ShareChatService.MyBinder;
import com.michelle.share.widget.RefreshListView;
import com.michelle.share.widget.RefreshListView.IOnLoadMoreListener;
import com.michelle.share.widget.RefreshListView.IOnRefreshListener;

import android.net.Uri;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.MediaStore.MediaColumns;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.OperationApplicationException;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.Toast;

public class ChatActivity extends Activity implements IOnRefreshListener,
		IOnLoadMoreListener {

	public static final String TAG = "ChatActivity";
	protected static final int CHOOSE_FILE_RESULT_CODE = 20;
	protected static final int CHOOSE_PHOTO_RESULT_CODE = 21;
	protected static final int CHOOSE_MUSIC_RESULT_CODE = 22;
	protected static final int CHOOSE_VIDEO_RESULT_CODE = 23;
	protected static final int CHOOSE_CONTACT_RESULT_CODE = 24;
	// [content://com.android.contacts/raw_contacts]
	private static final Uri RAW_CONTACTS_URI = ContactsContract.RawContacts.CONTENT_URI;
	// [content://com.android.contacts/data]
	private static final Uri DATA_URI = ContactsContract.Data.CONTENT_URI;

	private static final String ACCOUNT_TYPE = ContactsContract.RawContacts.ACCOUNT_TYPE;
	private static final String ACCOUNT_NAME = ContactsContract.RawContacts.ACCOUNT_NAME;

	private static final String RAW_CONTACT_ID = ContactsContract.Data.RAW_CONTACT_ID;
	private static final String MIMETYPE = ContactsContract.Data.MIMETYPE;

	private static final String NAME_ITEM_TYPE = ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE;
	private static final String DISPLAY_NAME = ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME;

	private static final String PHONE_ITEM_TYPE = ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE;
	private static final String PHONE_NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
	private static final String PHONE_TYPE = ContactsContract.CommonDataKinds.Phone.TYPE;
	private static final int PHONE_TYPE_HOME = ContactsContract.CommonDataKinds.Phone.TYPE_HOME;
	private static final int PHONE_TYPE_MOBILE = ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE;

	private static final String EMAIL_ITEM_TYPE = ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE;
	private static final String EMAIL_DATA = ContactsContract.CommonDataKinds.Email.DATA;
	private static final String EMAIL_TYPE = ContactsContract.CommonDataKinds.Email.TYPE;
	private static final int EMAIL_TYPE_HOME = ContactsContract.CommonDataKinds.Email.TYPE_HOME;
	private static final int EMAIL_TYPE_WORK = ContactsContract.CommonDataKinds.Email.TYPE_WORK;

	private static final String AUTHORITY = ContactsContract.AUTHORITY;

	private RefreshListView chatList = null;
	private ChatAdapter chatAdapter = null;
	private List<ChatMessage> messages;
	private LinkedList<ChatMessage> chatData;
	private Button sendBtn;
	private EditText mgsText;
	private MyBinder myBinder = null;
	private MyReceiver receiver;

	private RefreshDataAsynTask mRefreshAsynTask;

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

			}
		}, BIND_AUTO_CREATE);

		receiver = new MyReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.intent.action.MSG_RECEIVE");
		registerReceiver(receiver, filter);

		// set chatList adapter
		chatList = (RefreshListView) findViewById(R.id.chat_list);
		messages = ((ShareApplication) getApplication()).getMessages();

		chatData = new LinkedList<ChatMessage>();

		int listSize = messages.size();
		if (listSize < 6) {
			chatData.addAll(messages);
		} else {
			chatData.addAll(messages.subList(messages.size() - 5,
					messages.size()));
		}

		chatAdapter = new ChatAdapter(this, chatData);
		chatList.setAdapter(chatAdapter);
		chatList.setOnRefreshListener(this);

		chatList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, final View view,
					int position, long id) {
				// ChatMessage msg = messages.get(position);

				ListView listView = (ListView) parent;
				ChatMessage msg = (ChatMessage) parent
						.getItemAtPosition(position);

				if (msg.getDirection() == ChatMessage.MESSAGE_TO) {
					// the message was sent by ourselves
					return;
				}

				Object content = msg.getContent();

				if (content instanceof ImageFile) {
					String path = ((ImageFile) content).getPath();
					try {
						Intent intent = new Intent();
						intent.setAction(android.content.Intent.ACTION_VIEW);
						intent.setDataAndType(Uri.parse("file://" + path),
								"image/*");
						startActivity(intent);
					} catch (Exception e) {
						Toast.makeText(view.getContext(),
								"对不起， " + path + " 已被删除。", Toast.LENGTH_LONG)
								.show();
					}
				} else if (content instanceof Contact) {
					// the received message contained contact
					final Contact contact = (Contact) content;
					AlertDialog.Builder builder = new Builder(view.getContext());
					builder.setMessage("确认导入此联系人吗？");

					builder.setTitle("友情提示");
					
					builder.setNegativeButton("取消", new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							
							dialog.dismiss();
						}
					});
					builder.setPositiveButton("确认", new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							ContentValues values = new ContentValues();

							Uri rawContactUri = getContentResolver().insert(
									RawContacts.CONTENT_URI, values);
							long rawContactId = ContentUris.parseId(rawContactUri);

							values.clear();
							values.put(Data.RAW_CONTACT_ID, rawContactId);

							values.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
							values.put(StructuredName.GIVEN_NAME, contact.getName());

							getContentResolver().insert(
									android.provider.ContactsContract.Data.CONTENT_URI,
									values);

							for (String phoneNumer : contact.getPhones()) {
								values.clear();
								values.put(Data.RAW_CONTACT_ID, rawContactId);
								values.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
								values.put(Phone.NUMBER, phoneNumer);
								values.put(Phone.TYPE, Phone.TYPE_MOBILE);
								getContentResolver()
										.insert(android.provider.ContactsContract.Data.CONTENT_URI,
												values);
							}

							for (String email : contact.getMails()) {
								values.clear();
								values.put(Data.RAW_CONTACT_ID, rawContactId);
								values.put(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);
								values.put(Email.DATA, email);
								values.put(Email.TYPE, Email.TYPE_WORK);
								getContentResolver()
										.insert(android.provider.ContactsContract.Data.CONTENT_URI,
												values);
							}

							values.clear();
							values.put(Data.RAW_CONTACT_ID, rawContactId);
							values.put(Data.MIMETYPE, Photo.CONTENT_ITEM_TYPE);
							values.put(Photo.DATA15, contact.getContactPhotoBytes());
							getContentResolver().insert(
									android.provider.ContactsContract.Data.CONTENT_URI,
									values);

							Toast.makeText(view.getContext(),
									contact.getName() + " 联系人已插入成功！", Toast.LENGTH_LONG)
									.show();
							dialog.dismiss();
						}
					});

					builder.create().show();
				}

			}

		});

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
				// Time time = new Time("GMT+8");
				Time time = new Time();
				time.setToNow();
				messages.add(new ChatMessage(ChatMessage.MESSAGE_TO, msg, time));
				myBinder.sendMsg(MsgType.MESSAGE, msg);
				chatData.add(messages.get(messages.size() - 1));
				chatAdapter.refreshData(chatData);
				chatList.onRefreshComplete();
			}
		});

		final GridView fileUnits = (GridView) findViewById(R.id.file_units);
		initFileUnits(fileUnits);
		fileUnits.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				fileUnits.setVisibility(View.GONE);
				TextView text = (TextView) view
						.findViewById(R.id.file_unit_text);

				if (text.getText().equals("图片")) {
					Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
					intent.setType("image/*");
					startActivityForResult(intent, CHOOSE_PHOTO_RESULT_CODE);
				} else if (text.getText().equals("音乐")) {
					Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
					intent.setType("audio/*");
					startActivityForResult(intent, CHOOSE_MUSIC_RESULT_CODE);
				} else if (text.getText().equals("视频")) {
					Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
					intent.setType("video/*");
					startActivityForResult(intent, CHOOSE_VIDEO_RESULT_CODE);
				} else if (text.getText().equals("联系人")) {
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
				InputMethodManager imm = (InputMethodManager) getApplicationContext()
						.getSystemService(INPUT_METHOD_SERVICE);
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
		if (resultCode == Activity.RESULT_CANCELED || data == null) {
			return;
		}
		Uri uri = data.getData();
		if (requestCode == CHOOSE_PHOTO_RESULT_CODE) {
			// myBinder.sendMsg(MsgType.PHOTO, uri.getPath());

			Intent serviceIntent = new Intent(this, TransferService.class);
			serviceIntent.setAction(TransferService.ACTION_SEND_FILE);
			serviceIntent.putExtra(TransferService.EXTRAS_FILE_PATH,
					uri.toString());
			startService(serviceIntent);

			// messages.add(new ChatMessage(ChatMessage.MESSAGE_TO, ))
		} else if (requestCode == CHOOSE_CONTACT_RESULT_CODE) {
			uri = data.getData();
			Cursor cursor = getContentResolver().query(uri, null, null, null,
					null);
			if (cursor.moveToFirst()) {
				String name = cursor
						.getString(cursor
								.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				String id = cursor.getString(cursor
						.getColumnIndex(ContactsContract.Contacts._ID));
				Bitmap img = null;
				long photoId = cursor.getLong(cursor
						.getColumnIndex(ContactsContract.Contacts.PHOTO_ID));
				if (photoId > 0) {
					ContentUris
							.withAppendedId(
									ContactsContract.Contacts.CONTENT_URI,
									cursor.getColumnIndex(ContactsContract.Contacts._ID));
					InputStream input = ContactsContract.Contacts
							.openContactPhotoInputStream(getContentResolver(),
									uri);
					img = BitmapFactory.decodeStream(input);
				}
				Cursor phones = getContentResolver().query(
						ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
						null,
						ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "="
								+ id, null, null);
				ArrayList<String> phoneDetails = new ArrayList<String>();
				while (phones.moveToNext()) {
					String phone = phones
							.getString(phones
									.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
					String phoneType = phones
							.getString(phones
									.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA2));
					phoneDetails.add(phone);
				}
				phones.close();
				ArrayList<String> emailDetails = new ArrayList<String>();
				Cursor emails = getContentResolver().query(
						ContactsContract.CommonDataKinds.Email.CONTENT_URI,
						null,
						ContactsContract.CommonDataKinds.Email.CONTACT_ID + "="
								+ id, null, null);
				while (emails.moveToNext()) {
					String emailAddress = emails
							.getString(emails
									.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
					emailDetails.add(emailAddress);
				}
				emails.close();
				cursor.close();

				try {
					Contact contact = new Contact(name, phoneDetails,
							emailDetails, null);
					if (img != null) {
						ByteArrayOutputStream stream = new ByteArrayOutputStream();
						img.compress(Bitmap.CompressFormat.JPEG, 100, stream);
						byte[] byteArray = stream.toByteArray();
						contact.setContactPhotoBytes(byteArray);
						contact.setContactPhotoBitmap(null);
					}
					Intent serviceIntent = new Intent(this,
							TransferService.class);
					serviceIntent
							.setAction(TransferService.ACTION_SEND_CONTACT);
					serviceIntent.putExtra(TransferService.CONTACT, contact);

					startService(serviceIntent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
	}

	/*
	 * (non-Javadoc)
	 * 
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
		unit.put("unitText", "联系人");
		fileUnitsList.add(unit);

		unit = new HashMap<String, Object>();
		unit.put("unitImg", R.drawable.ic_sd_rom);
		unit.put("unitText", "SD内存");
		fileUnitsList.add(unit);

		SimpleAdapter dataAdapter = new SimpleAdapter(this, fileUnitsList,
				R.layout.file_unit, new String[] { "unitImg", "unitText" },
				new int[] { R.id.file_unit_img, R.id.file_unit_text });

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
			chatData.add(messages.get(messages.size() - 1));
			chatAdapter.notifyDataSetChanged();
		}

		public MyReceiver() {
			System.out.println("MyReceiver");
		}
	}

	@Override
	public void OnLoadMore() {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnRefresh() {
		// TODO Auto-generated method stub
		mRefreshAsynTask = new RefreshDataAsynTask();
		mRefreshAsynTask.execute();
	}

	private int index = 5;

	class RefreshDataAsynTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			int count = messages.size() - chatData.size();
			if (count > 5) {
				Iterator iterator = messages.listIterator(count);
				chatData.addFirst(messages.get(count - 1));
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub

			chatAdapter.refreshData(chatData);
			chatList.onRefreshComplete();
		}
	}
}
