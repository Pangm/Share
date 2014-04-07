package com.michelle.share.db;

import java.util.ArrayList;
import java.util.List;

import com.michelle.share.ImageFile;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.format.Time;

public class ImageFileService {
	private DBOpenHelper dbOpenHelper;
	
	public ImageFileService(Context context) {
        dbOpenHelper = new DBOpenHelper(context, "share.db", 1);  
    }  
  
    public void save(ImageFile imageFile) {  
        SQLiteDatabase database = dbOpenHelper.getWritableDatabase();  
        database.beginTransaction();  
        database.execSQL("insert into imagefile(name, size, path, time)values(?,?,?,?)",  
                new Object[] { 
		    		imageFile.getName(), 
		    		imageFile.getSize(), 
		    		imageFile.getPath(),
		    		imageFile.getTime().format2445()});  
        // database.close();可以不关闭数据库，他里面会缓存一个数据库对象，如果以后还要用就直接用这个缓存的数据库对象。但通过  
        // context.openOrCreateDatabase(arg0, arg1, arg2)打开的数据库必须得关闭  
        database.setTransactionSuccessful();  
        database.endTransaction();  
  
    }  
  
    public void update(ImageFile imageFile) {  
        SQLiteDatabase database = dbOpenHelper.getWritableDatabase();  
        database.execSQL(  
                "update imagefile set name=?,size=?,path=?,time=? where imagefileid=?",  
                new Object[] { 
                		imageFile.getName(), 
    		    		imageFile.getSize(), 
    		    		imageFile.getPath(),
    		    		imageFile.getTime().format2445(),
    		    		imageFile.getId()});  
    }  
  
    public ImageFile find(int id) {  
        SQLiteDatabase database = dbOpenHelper.getReadableDatabase();  
        Cursor cursor = database.rawQuery(  
                "select * from imagefile where imagefileid=?",  
                new String[] { String.valueOf(id) }); 
        
        if (cursor.moveToNext()) {  
        	Time time = new Time();
            time.parse(cursor.getString(4));
            return new ImageFile(
            		cursor.getInt(0),
            		cursor.getString(1),
            		cursor.getFloat(2),
            		cursor.getString(3),
            		time);
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
                    "delete from imagefile where imagefileid in(" + sb.toString()  
                            + ")", ids);  
        }  
    }  
  
    public List<ImageFile> getScrollData(int startResult, int maxResult) {  
        List<ImageFile> imageFiles = new ArrayList<ImageFile>();  
        SQLiteDatabase database = dbOpenHelper.getReadableDatabase();  
        Cursor cursor = database.rawQuery(  
                "select * from imagefile limit ?,?",  
                new String[] { String.valueOf(startResult),  
                        String.valueOf(maxResult) });  
        while (cursor.moveToNext()) {  
        	Time time = new Time();
            time.parse(cursor.getString(4));
        	imageFiles.add(new ImageFile(
            		cursor.getInt(0),
            		cursor.getString(1),
            		cursor.getFloat(2),
            		cursor.getString(3),
            		time));  
        }  
        return imageFiles;  
    }  
  
    // 获取分页数据，提供给SimpleCursorAdapter使用。  
    public Cursor getRawScrollData(int startResult, int maxResult) {  
      //  List<ImageFile> imageFiles = new ArrayList<ImageFile>();  
        SQLiteDatabase database = dbOpenHelper.getReadableDatabase();  
        return database.rawQuery(  
                "select imagefileid as _id ,name,size,path,time from imagefile limit ?,?",  
                new String[] { String.valueOf(startResult),  
                        String.valueOf(maxResult) });  
  
    }  

    public long getCount() {  
        SQLiteDatabase database = dbOpenHelper.getReadableDatabase();  
        Cursor cursor = database.rawQuery("select count(*) from imagefile", null);  
        if (cursor.moveToNext()) {  
            return cursor.getLong(0);  
        }  
        return 0;  
    } 
}
