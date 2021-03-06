package com.michelle.share;

import java.text.DecimalFormat;
import java.util.List;

import com.michelle.share.cache.ImageLoader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ReceivedFilesAdapter extends BaseAdapter {
	
	private static final String TAG = "ReceivedFilesAdapter";
	private boolean mBusy = false;
	
	public void setFlagBusy(boolean busy) {
		this.mBusy = busy;
	}

	private ImageLoader mImageLoader;
	private Context mContext;
	private List<ShareFile> files;
	
	public ReceivedFilesAdapter(Context context, List<ShareFile> files) {
        this.mContext = context;  
        this.files = files;  
        mImageLoader = new ImageLoader(context);  
    }  
	
	public void Update(List<ShareFile> files) {
		this.files = files;
		this.notifyDataSetChanged();
	}
	
	public ImageLoader getImageLoader(){  
        return mImageLoader;  
    } 
	
	@Override
	public int getCount() {
		return files.size();
	}

	@Override
	public Object getItem(int position) {
		return files.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		 ViewHolder viewHolder = null;  
	        if (convertView == null) {  
	            convertView = LayoutInflater.from(mContext).inflate(  
	                    R.layout.history_file_item, null);  
	            viewHolder = new ViewHolder();  
	            viewHolder.mFileNameTV = (TextView) convertView  
	                    .findViewById(R.id.file_name);  
	            viewHolder.mFileImageIV = (ImageView) convertView  
	                    .findViewById(R.id.file_avatar);
	            viewHolder.mFileSizeTV = (TextView) convertView
	            		.findViewById(R.id.chat_content_size);
	            viewHolder.mReceivedTimeTV = (TextView) convertView
	            		.findViewById(R.id.file_receive_time);
	            convertView.setTag(viewHolder);  
	        } else {  
	            viewHolder = (ViewHolder) convertView.getTag();  
	        }
	        
	        viewHolder.mFileImageIV.setImageResource(R.drawable.ic_photo);  
	        
	        ShareFile shareFile = files.get(position);
	  
	        switch (shareFile.getType()) {
	        case 0:
	        	if (!mBusy) {  
		            mImageLoader.DisplayImage(shareFile.getPath(), viewHolder.mFileImageIV, false);  
		            viewHolder.mFileSizeTV.setText("--" + position  
		                    + "--IDLE ||TOUCH_SCROLL");  
		        } else {  
		            mImageLoader.DisplayImage(shareFile.getPath(), viewHolder.mFileImageIV, true);          
		            viewHolder.mFileSizeTV.setText("--" + position + "--FLING");  
		        }  
	        	break;
	        case 1:
	        	viewHolder.mFileImageIV.setImageResource(R.drawable.ic_music);
				break;
			case 2:
				viewHolder.mFileImageIV.setImageResource(R.drawable.ic_video);
				break;
	        default:
	        	break;
	        }
	        
	        viewHolder.mFileNameTV.setText(shareFile.getName());
	        float size = shareFile.getSize();
	        
	        // set value of size text view according size value. 
	        if (size <= 512) {
	        	viewHolder.mFileSizeTV.setText(new  DecimalFormat(".##").format(size) + " KB");
			} else {
				size /= 1024;
				viewHolder.mFileSizeTV.setText(new  DecimalFormat(".##").format(size) + " MB");
			}
	        
	        viewHolder.mReceivedTimeTV.setText(shareFile.getTime().format("%Y-%m-%d %H:%M:%S"));
	        return convertView;  
	}
	
	static class ViewHolder {  
		public TextView mReceivedTimeTV;
		public TextView mFileSizeTV;
		public ImageView mFileImageIV;
		public TextView mFileNameTV;
	}  

}
