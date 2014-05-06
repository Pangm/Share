package com.michelle.share;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.michelle.share.db.ImageFileService;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
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
	private ReceivedFilesAdapter receivedFilesAdapter;
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
        final LayoutInflater mInflater = getLayoutInflater(savedInstanceState);//inflater ;
        receivedFilesAdapter = new ReceivedFilesAdapter(this.getActivity(), 
        		imageFileService.getScrollData(0, 10));
        
        
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_history_files,
				container, false);
		
		listView = (ListView) rootView.findViewById(R.id.listview);
		listView.setAdapter(receivedFilesAdapter); 
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {  
  
            @Override
            public void onItemClick(AdapterView<?> parent, View view,  
                    int position, long id) {  
                ListView listView = (ListView) parent;
                ImageFile imageFile = (ImageFile) parent.getItemAtPosition(position);  
                String name = imageFile.getName();
                String path = imageFile.getPath();
                Log.i(TAG, view.getClass().getName()); 
                
                try {
					Intent intent = new Intent();
					intent.setAction(android.content.Intent.ACTION_VIEW);
					intent.setDataAndType(Uri.parse("file://" + path), "image/*");
					startActivity(intent);
                } catch (Exception e) {
                	Toast.makeText(getActivity(), "对不起， " + name + " 已被删除。", Toast.LENGTH_LONG).show(); 
                }
            }
        });
        final ImageFileService imageFileService = new ImageFileService(this.getActivity());
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				ListView listView = (ListView) parent;
                ImageFile imageFile = (ImageFile) parent.getItemAtPosition(position);
                
                File file = new File(imageFile.getPath());
                
                if (file.exists() && file.isFile() && file.delete()) {
        			Toast.makeText(getActivity(), imageFile.getName() + " 已被删除。", Toast.LENGTH_LONG).show();
        			imageFileService.delete(imageFile.getId());
                    receivedFilesAdapter.Update(imageFileService.getScrollData(0, 10));
                }
                
				return true;
			}
		});
        return rootView;
        
	}
}
