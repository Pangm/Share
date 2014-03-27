package com.michelle.share;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ChatAdapter extends BaseAdapter {

	private Context context;
	private List<ChatMessage> chatMessages;
	
	public ChatAdapter(Context context, List<ChatMessage> messages) {
		super();
		this.context = context;
		this.chatMessages = messages;
	}

	@Override
	public int getCount() {
		return chatMessages.size();
	}

	@Override
	public Object getItem(int position) {
		return chatMessages.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		ChatMessage message = chatMessages.get(position);
		if (convertView == null || (holder = (ViewHolder) convertView.getTag()).flag != message.getDirection()) {

			holder = new ViewHolder();
			if (message.getDirection() == ChatMessage.MESSAGE_FROM) {
				holder.flag = ChatMessage.MESSAGE_FROM;

				convertView = LayoutInflater.from(context).inflate(R.layout.chat_item_from, null);
			} else {
				holder.flag = ChatMessage.MESSAGE_TO;
				convertView = LayoutInflater.from(context).inflate(R.layout.chat_item_to, null);
			}

			holder.text = (TextView) convertView.findViewById(R.id.chatting_content_size);
			holder.time = (TextView) convertView.findViewById(R.id.chat_time_tv);
			convertView.setTag(holder);
		}
		holder.time.setText(message.getTime().format("%Y-%m-%d %H:%M:%S"));
		holder.text.setText(message.getContent());

		return convertView;
	}
	
	static class ViewHolder {
		TextView text;
		int flag;
		TextView time;
	}

}
