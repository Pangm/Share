package com.michelle.share;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.michelle.share.db.ImageFileService;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
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

	public HistoryFilesFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_history_files,
				container, false);
		ImageFileService imageFileService = new ImageFileService(this.getActivity());  
        ListView listView = (ListView) rootView.findViewById(R.id.listview);  
        final LayoutInflater mInflater = inflater ;
    //    List<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();  
        // HashMap<String, String> title = new HashMap<String, String>();  
        // title.put("personid", "编号");  
        // title.put("name", "姓名");  
        // title.put("age", "年龄");  
        // data.add(title);  
  
        // 适配器有：  
        // ArrayAdapter<T>  
        // simpAdapter  
        // SimpleCursorAdapter  
        Cursor cursor = imageFileService.getRawScrollData(0, 10);  
        CursorAdapter adapter = new CursorAdapter(getActivity(), cursor, true)
        {//重写两个方法
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent)
            {//找到布局和控件
                ViewHolder holder = new ViewHolder();
                View item = mInflater.inflate(R.layout.history_file_item, null);
                holder.fileName = (TextView) item.findViewById(R.id.file_name);
                holder.fileSize = (TextView) item.findViewById(R.id.chatting_content_size);
                holder.fileImageView = (ImageView) item.findViewById(R.id.file_avaster);
                //holder.filePath
                item.setTag(holder);
                return item;//返回的view传给bindView。
            }
                                                             
            @Override
            public void bindView(View view, Context context, Cursor cursor)
            {//复用布局。
//                把数据设置到界面上
                ViewHolder holder = (ViewHolder) view.getTag();
                String name = cursor.getString(cursor.getColumnIndex("name"));
                String path = cursor.getString(cursor.getColumnIndex("path"));
                Float size = cursor.getFloat(cursor.getColumnIndex("size"));
                if (size <= 512) {
					holder.fileSize.setText(size + " KB");
				} else {
					holder.fileSize.setText(size/1000 + " MB");
				}
                holder.fileName.setText(name);
                Bitmap img = BitmapFactory.decodeFile(path);
				holder.fileImageView.setImageBitmap(img);
            }
                                                         
        };
        
        listView.setAdapter(adapter);  
  
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {  
  
            @Override  
            // parent即为你点击的listView  
            // view为listview的外面布局  
            public void onItemClick(AdapterView<?> parent, View view,  
                    int position, long id) {  
                ListView listView = (ListView) parent;  
                Cursor cursor = (Cursor) listView.getItemAtPosition(position);  
                //String personid = String.valueOf(cursor.getInt(0));  
                String name = String.valueOf(cursor.getString(1)); 
                Log.i(TAG, view.getClass().getName()); 
                Toast.makeText(getActivity(), name, Toast.LENGTH_LONG).show();  
            }
        });
            
		return rootView;
	}
	
	private class ViewHolder {

		protected ImageView fileImageView;
		protected TextView fileName;
		protected TextView fileSize;
		
	}
}
