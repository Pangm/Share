package com.michelle.share;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.michelle.share.db.ImageFileService;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class HistoryFilesFragment extends Fragment {
	public static final String ARG_SECTION_NUMBER = null;
	protected static final String TAG = "HistoryFilesFragment";
	private CursorAdapter adapter;
	private ListView listView;
	
	public HistoryFilesFragment() {
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ImageFileService imageFileService = new ImageFileService(this.getActivity());  
//        ListView listView = (ListView) this.(R.id.listview);  
        final LayoutInflater mInflater = getLayoutInflater(savedInstanceState);//inflater ;
        Cursor cursor = imageFileService.getRawScrollData(0, 10);  
        adapter = new CursorAdapter(getActivity(), cursor, true)
        {//��д��������
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent)
            {//�ҵ����ֺͿؼ�
                ViewHolder holder = new ViewHolder();
                View item = mInflater.inflate(R.layout.history_file_item, null);
                holder.fileName = (TextView) item.findViewById(R.id.file_name);
                holder.fileSize = (TextView) item.findViewById(R.id.chatting_content_size);
                holder.fileImageView = (ImageView) item.findViewById(R.id.file_avaster);
                holder.fileReceiveTime = (TextView) item.findViewById(R.id.file_receive_time);
                //holder.filePath
                item.setTag(holder);
                return item;//���ص�view����bindView��
            }
                                                             
            @Override
            public void bindView(View view, Context context, Cursor cursor)
            {//���ò��֡�
//                ���������õ�������
                ViewHolder holder = (ViewHolder) view.getTag();
                String name = cursor.getString(cursor.getColumnIndex("name"));
                String path = cursor.getString(cursor.getColumnIndex("path"));
                Time time = new Time();
                time.parse(cursor.getString(cursor.getColumnIndex("time")));
                
                Float size = cursor.getFloat(cursor.getColumnIndex("size"));
                if (size <= 512) {
					holder.fileSize.setText(size + " KB");
				} else {
					holder.fileSize.setText(size/1000 + " MB");
				}
                holder.fileName.setText(name);
                holder.fileReceiveTime.setText(time.format("%Y-%m-%d %H:%M:%S"));
                Bitmap img = getImageThumbnail(path, 160, 160);
				holder.fileImageView.setImageBitmap(img);
            }
                                                         
        };
        
        
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_history_files,
				container, false);
		
		listView = (ListView) rootView.findViewById(R.id.listview);
		listView.setAdapter(adapter);  
		  
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {  
  
            @Override  
            // parent��Ϊ������listView  
            // viewΪlistview�����沼��  
            public void onItemClick(AdapterView<?> parent, View view,  
                    int position, long id) {  
                ListView listView = (ListView) parent;  
                Cursor cursor = (Cursor) listView.getItemAtPosition(position);  
                String name = String.valueOf(cursor.getString(1)); 
                Log.i(TAG, view.getClass().getName()); 
                Toast.makeText(getActivity(), name, Toast.LENGTH_LONG).show();  
            }
        });
		return rootView;
	}
	
	 private Bitmap getImageThumbnail(String imagePath, int width, int height) {  
	        Bitmap bitmap = null;  
	        BitmapFactory.Options options = new BitmapFactory.Options();  
	        options.inJustDecodeBounds = true;  
	        // ��ȡ���ͼƬ�Ŀ�͸ߣ�ע��˴���bitmapΪnull  
	        bitmap = BitmapFactory.decodeFile(imagePath, options);  
	        options.inJustDecodeBounds = false; // ��Ϊ false  
	        // �������ű�  
	        int h = options.outHeight;  
	        int w = options.outWidth;  
	        int beWidth = w / width;  
	        int beHeight = h / height;  
	        int be = 1;  
	        if (beWidth < beHeight) {  
	            be = beWidth;  
	        } else {  
	            be = beHeight;  
	        }  
	        if (be <= 0) {  
	            be = 1;  
	        }  
	        options.inSampleSize = be;  
	        // ���¶���ͼƬ����ȡ���ź��bitmap��ע�����Ҫ��options.inJustDecodeBounds ��Ϊ false  
	        bitmap = BitmapFactory.decodeFile(imagePath, options);  
	        // ����ThumbnailUtils����������ͼ������Ҫָ��Ҫ�����ĸ�Bitmap����  
	        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,  
	                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);  
	        return bitmap;  
	    } 
	 
	 
	private class ViewHolder {

		protected TextView fileReceiveTime;
		protected ImageView fileImageView;
		protected TextView fileName;
		protected TextView fileSize;
		
	}
}
