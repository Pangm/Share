package com.michelle.share;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ChatAdapter extends BaseAdapter {

	private Context context;
	private List<ChatMessage> chatMessages;

	public ChatAdapter(Context context, List<ChatMessage> messages) {
		super();
		this.context = context;
		this.chatMessages = messages;
	}

	public void refreshData(List<ChatMessage> messages) {
		this.chatMessages = messages;
		notifyDataSetChanged();
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
		if (convertView == null
				|| (holder = (ViewHolder) convertView.getTag()).flag != message
						.getDirection()) {
			holder = new ViewHolder();

			Object object = message.getContent();

			if (object instanceof String) {
				if (message.getDirection() == ChatMessage.MESSAGE_FROM) {
					holder.flag = ChatMessage.MESSAGE_FROM;

					convertView = LayoutInflater.from(context).inflate(
							R.layout.chat_item_from, null);
					ImageView imageView_chatfrom = (ImageView) convertView
							.findViewById(R.id.user_avatar);
					imageView_chatfrom.setImageDrawable(getImgDrawable(
							imageView_chatfrom, R.drawable.avatar_to,
							R.drawable.avatar4));

				} else {
					holder.flag = ChatMessage.MESSAGE_TO;
					convertView = LayoutInflater.from(context).inflate(
							R.layout.chat_item_to, null);
					ImageView imageView_chatto = (ImageView) convertView
							.findViewById(R.id.chatto_image);
					imageView_chatto.setImageDrawable(getImgDrawable(
							imageView_chatto, R.drawable.avatar_from,
							R.drawable.avatar7));
				}

				holder.text = (TextView) convertView
						.findViewById(R.id.chatting_content_size);
				holder.time = (TextView) convertView
						.findViewById(R.id.chat_time_tv);
				convertView.setTag(holder);
				holder.time.setText(message.getTime().format(
						"%Y-%m-%d %H:%M:%S"));
				holder.text.setText((String) message.getContent());
			} else if (object instanceof ImageFile) {
				ImageFile imageFile = (ImageFile) message.getContent();
				if (message.getDirection() == ChatMessage.MESSAGE_FROM) {
					holder.flag = ChatMessage.MESSAGE_FROM;
					convertView = LayoutInflater.from(context).inflate(
							R.layout.media_msg_from, null);
					ImageView userAvatar = (ImageView) convertView
							.findViewById(R.id.user_avatar);
					userAvatar.setImageDrawable(getImgDrawable(userAvatar,
							R.drawable.avatar_to, R.drawable.avatar4));
				} else {
					holder.flag = ChatMessage.MESSAGE_TO;
					convertView = LayoutInflater.from(context).inflate(
							R.layout.media_msg_to, null);
					ImageView userAvatar = (ImageView) convertView
							.findViewById(R.id.user_avatar);
					userAvatar.setImageDrawable(getImgDrawable(userAvatar,
							R.drawable.avatar_from, R.drawable.avatar7));
				}
				holder.text = (TextView) convertView
						.findViewById(R.id.file_name);
				holder.imageView = (ImageView) convertView
						.findViewById(R.id.file_avatar);
				holder.size = (TextView) convertView
						.findViewById(R.id.chatting_content_size);
				holder.time = (TextView) convertView
						.findViewById(R.id.chat_time_tv);
				holder.progress = (ProgressBar) convertView
						.findViewById(R.id.progress);

				convertView.setTag(holder);
				holder.time.setText(imageFile.getTime().format(
						"%Y-%m-%d %H:%M:%S"));
				holder.text.setText(imageFile.getName());
				float fileSize = imageFile.getSize();
				if (imageFile.getSize() <= 512) {
					holder.size.setText(fileSize + " KB");
				} else {
					fileSize /= 1024;
					holder.size.setText(new DecimalFormat(".##")
							.format(fileSize) + " MB");
				}

				if (message.getProgressValue() != 100) {
					holder.progress.setMax(100);
					holder.progress.setProgress(message.getProgressValue());
				} else {
					holder.progress.setVisibility(ProgressBar.GONE);
				}
				if (message.getDirection() == ChatMessage.MESSAGE_TO
						|| message.getProgressValue() == 100) {
					Bitmap img = BitmapFactory.decodeFile(imageFile.getPath());
					holder.imageView.setImageBitmap(img);
					if (img == null) {
						InputStream is;
						try {
							is = context.getContentResolver().openInputStream(
									Uri.parse(imageFile.getPath()));
							img = BitmapFactory.decodeStream(is);
							holder.imageView.setImageBitmap(img);
						} catch (FileNotFoundException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}

			} else if (object instanceof Contact) {
				Contact contact = (Contact) message.getContent();
				if (message.getDirection() == ChatMessage.MESSAGE_FROM) {
					holder.flag = ChatMessage.MESSAGE_FROM;
					convertView = LayoutInflater.from(context).inflate(
							R.layout.media_msg_from, null);
					ImageView imageView_chatfrom = (ImageView) convertView
							.findViewById(R.id.user_avatar);
					imageView_chatfrom.setImageDrawable(getImgDrawable(
							imageView_chatfrom, R.drawable.avatar_to,
							R.drawable.avatar4));
				} else {
					holder.flag = ChatMessage.MESSAGE_TO;
					convertView = LayoutInflater.from(context).inflate(
							R.layout.media_msg_to, null);
					ImageView imageView_chatto = (ImageView) convertView
							.findViewById(R.id.user_avatar);
					imageView_chatto.setImageDrawable(getImgDrawable(
							imageView_chatto, R.drawable.avatar_from,
							R.drawable.avatar7));
				}
				holder.text = (TextView) convertView
						.findViewById(R.id.file_name);
				holder.imageView = (ImageView) convertView
						.findViewById(R.id.file_avatar);
				holder.size = (TextView) convertView
						.findViewById(R.id.chatting_content_size);
				holder.time = (TextView) convertView
						.findViewById(R.id.chat_time_tv);

				convertView.setTag(holder);
				holder.time.setText(message.getTime().format(
						"%Y-%m-%d %H:%M:%S"));
				holder.text.setText(contact.getName());
				holder.size.setVisibility(View.GONE);
				// Bitmap img = BitmapFactory.decodeFile(imageFile.getPath());
				if (contact.getContactPhotoBytes() != null) {

					Bitmap bmp = BitmapFactory.decodeByteArray(
							contact.getContactPhotoBytes(), 0,
							contact.getContactPhotoBytes().length);
					holder.imageView.setImageBitmap(bmp);
				} else {
					Drawable drawable = holder.imageView.getResources()
							.getDrawable(R.drawable.contact_default);
					holder.imageView.setImageDrawable(drawable);
				}

			}
		}

		return convertView;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.BaseAdapter#getItemViewType(int)
	 */
	@Override
	public int getItemViewType(int position) {
		// TODO Auto-generated method stub
		return super.getItemViewType(position);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.BaseAdapter#getViewTypeCount()
	 */
	@Override
	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return super.getViewTypeCount();
	}

	static class ViewHolder {
		ProgressBar progress;
		TextView text;
		int flag;
		TextView time;
		ImageView imageView;
		TextView size;
	}

	public Drawable getImgDrawable(ImageView imgView, int avatar_back,
			int avatar_front) {
		Drawable d1 = imgView.getResources().getDrawable(avatar_back);
		Drawable d2 = imgView.getResources().getDrawable(avatar_front);

		Drawable[] array = new Drawable[] { d1, d2 };
		LayerDrawable ld = new LayerDrawable(array);
		return ld;
	}

}
