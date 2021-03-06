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
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		View rowView = convertView;
		ChatMessage message = chatMessages.get(position);
		
		// 找到相应的视图
		if (rowView == null 
				|| ((ViewHolder) rowView.getTag()).flag != message.getDirection()) {

			holder = new ViewHolder();

			if (message.getDirection() == ChatMessage.MESSAGE_FROM) {
				rowView = LayoutInflater.from(context).inflate(R.layout.media_msg_from, null);
			} else {
				rowView = LayoutInflater.from(context).inflate(R.layout.media_msg_to, null);
			}
			
			holder.flag = message.getDirection();
			holder.userImageView = (ImageView) rowView.findViewById(R.id.user_avatar);
			holder.text = (TextView) rowView.findViewById(R.id.file_name);
			holder.imageView = (ImageView) rowView.findViewById(R.id.file_avatar);
			holder.size = (TextView) rowView.findViewById(R.id.chat_content_size);
			holder.time = (TextView) rowView.findViewById(R.id.chat_time_tv);
			holder.progressBar = (ProgressBar) rowView.findViewById(R.id.progress);
			
			holder.chatMessage = message;
			rowView.setTag(holder);
		} else {
			holder = (ViewHolder) rowView.getTag();
			
			holder.chatMessage.setProgressBar(null);
			holder.chatMessage = message;
			holder.chatMessage.setProgressBar(holder.progressBar);
		}
		
		Object content = message.getContent();
		
		// 头像负值
		if (holder.flag == ChatMessage.MESSAGE_FROM) {
			holder.userImageView.setImageResource(R.drawable.avatar4);
		} else {
			holder.userImageView.setImageResource(R.drawable.avatar7);
		}		
		
		// 负值
		holder.time.setText(message.getTime().format("%Y-%m-%d %H:%M:%S"));
		if (content instanceof String) {
			holder.progressBar.setVisibility(View.GONE);
			holder.imageView.setVisibility(View.GONE);
			holder.size.setVisibility(View.GONE);
			
			holder.text.setText((String) content);
		} else if (content instanceof ShareFile) {
			ShareFile imageFile = (ShareFile) content;
			
			holder.imageView.setVisibility(View.VISIBLE);
			holder.size.setVisibility(View.VISIBLE);
			
			if (message.getProgressValue() != 100) {
				holder.progressBar.setVisibility(View.VISIBLE);
				holder.progressBar.setMax(100);
				holder.progressBar.setProgress(message.getProgressValue());
				holder.progressBar.invalidate();
			} else {
				holder.progressBar.setVisibility(ProgressBar.GONE);
			}

			holder.text.setText(imageFile.getName());
			
			float fileSize = imageFile.getSize();
			if (imageFile.getSize() <= 512) {
				holder.size.setText(new  DecimalFormat(".##").format(fileSize) + " KB");
			} else {
				fileSize /= 1024f;
				holder.size.setText(new  DecimalFormat(".##").format(fileSize) + " MB");
			}
			
			int fileType = imageFile.getType();
			
			switch (fileType) {
			case 0:
				try
				{
					Bitmap img = BitmapFactory.decodeFile(imageFile.getPath());
					if (img == null) {
						InputStream is;
						try {
							is = context.getContentResolver().
									openInputStream(Uri.parse(imageFile.getPath()));
							img = BitmapFactory.decodeStream(is);
							is.close();
						} catch (FileNotFoundException e1) {
							e1.printStackTrace();
						}
					}

					holder.imageView.setImageBitmap(img);
				} catch (Exception e) {				
					e.printStackTrace();
				}
				break;
			case 1:
				holder.imageView.setImageResource(R.drawable.ic_music);
				break;
			case 2:
				holder.imageView.setImageResource(R.drawable.ic_video);
				break;
			default:
				break;
			}
				
			
		} else if (content instanceof Contact) {
			Contact contact = (Contact) content;
			
			holder.progressBar.setVisibility(View.GONE);
			holder.imageView.setVisibility(View.VISIBLE);
			holder.size.setVisibility(View.GONE);
			
			holder.text.setText(contact.getName());
			
			if (contact.getContactPhotoBytes() != null) {
				Bitmap bmp = BitmapFactory.decodeByteArray(contact.getContactPhotoBytes(), 
						0, contact.getContactPhotoBytes().length);
				holder.imageView.setImageBitmap(bmp);
			} else {
				Drawable drawable = holder.imageView.getResources().getDrawable(R.drawable.contact_default);
				holder.imageView.setImageDrawable(drawable);
			}
		}
		
		return rowView;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.BaseAdapter#getItemViewType(int)
	 */
	@Override
	public int getItemViewType(int position) {
		return 2;
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
		public ChatMessage chatMessage;
		ProgressBar progressBar;
		ImageView userImageView;
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

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

}
