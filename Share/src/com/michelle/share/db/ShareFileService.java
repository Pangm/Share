package com.michelle.share.db;

import java.util.ArrayList;
import java.util.List;

import com.michelle.share.ShareFile;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.format.Time;

public class ShareFileService {
	private DBOpenHelper dbOpenHelper;
	
	public ShareFileService(Context context) {
        dbOpenHelper = new DBOpenHelper(context, "share.db", 1);  
    }  
  
    public void save(ShareFile shareFile) {  
        SQLiteDatabase database = dbOpenHelper.getWritableDatabase();  
        database.beginTransaction();  
        database.execSQL("insert into sharefile(name, size, path, time, type)values(?,?,?,?,?)",  
                new Object[] { 
		    		shareFile.getName(), 
		    		shareFile.getSize(), 
		    		shareFile.getPath(),
		    		shareFile.getTime().format2445(),
		    		shareFile.getType()});  
        database.setTransactionSuccessful();  
        database.endTransaction();  
  
    }  
  
    public void update(ShareFile shareFile) {  
        SQLiteDatabase database = dbOpenHelper.getWritableDatabase();  
        database.execSQL(  
                "update sharefile set name=?,size=?,path=?,time=?,type=? where sharefileid=?",  
                new Object[] { 
                		shareFile.getName(), 
    		    		shareFile.getSize(), 
    		    		shareFile.getPath(),
    		    		shareFile.getTime().format2445(),
    		    		shareFile.getType(),
    		    		shareFile.getId()});  
    }  
  
    public ShareFile find(int id) {  
        SQLiteDatabase database = dbOpenHelper.getReadableDatabase();  
        Cursor cursor = database.rawQuery(  
                "select * from sharefile where sharefileid=?",  
                new String[] { String.valueOf(id) }); 
        
        if (cursor.moveToNext()) {  
        	Time time = new Time();
            time.parse(cursor.getString(4));
            ShareFile shareFile = new ShareFile(
            		cursor.getInt(0),
            		cursor.getString(1),
            		cursor.getFloat(2),
            		cursor.getString(3),
            		time);
            shareFile.setType(cursor.getInt(5));
            return shareFile;
        }  
        return null;  
    }  
  
    public void delete(Integer... ids) {  
        if (ids.length > 0) {  
            StringBuffer sb = new StringBuffer();  
            for (Integer id : ids) {  
                sb.append('?').append(',');  
            }  
            sb.deleteCharAt(sb.length() - 1);  
            SQLiteDatabase database = dbOpenHelper.getWritableDatabase();  
            database.execSQL(  
                    "delete from sharefile where sharefileid in(" + sb.toString()  
                            + ")", ids);  
        }  
    }  
  
    public List<ShareFile> getScrollData(int startResult, int maxResult) {  
        List<ShareFile> imageFiles = new ArrayList<ShareFile>();  
        SQLiteDatabase database = dbOpenHelper.getReadableDatabase();  
        Cursor cursor = database.rawQuery(  
                "select * from sharefile limit ?,?",  
                new String[] { String.valueOf(startResult),  
                        String.valueOf(maxResult) });  
        while (cursor.moveToNext()) {  
        	Time time = new Time();
            time.parse(cursor.getString(4));
            ShareFile shareFile = new ShareFile(
            		cursor.getInt(0),
            		cursor.getString(1),
            		cursor.getFloat(2),
            		cursor.getString(3),
            		time);
            shareFile.setType(cursor.getInt(5));
        	imageFiles.add(shareFile);  
        }  
        return imageFiles;  
    }  
  
    // 获取分页数据，提供给SimpleCursorAdapter使用。  
    public Cursor getRawScrollData(int startResult, int maxResult) {  
      //  List<ImageFile> imageFiles = new ArrayList<ImageFile>();  
        SQLiteDatabase database = dbOpenHelper.getReadableDatabase();  
        return database.rawQuery(  
                "select sharefileid as _id ,name,size,path,time,type from sharefile limit ?,?",  
                new String[] { String.valueOf(startResult),  
                        String.valueOf(maxResult) });  
  
    }  

    public long getCount() {  
        SQLiteDatabase database = dbOpenHelper.getReadableDatabase();  
        Cursor cursor = database.rawQuery("select count(*) from sharefile", null);  
        if (cursor.moveToNext()) {  
            return cursor.getLong(0);  
        }  
        return 0;  
    } 
}
