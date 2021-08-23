package com.txznet.feedback.dtool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.feedback.AppLogic;
import com.txznet.feedback.data.Message;
import com.txznet.feedback.util.TimeComparator;

public class MessageDao {
	private static final int NO_READ = 0;
	private static final int READ = 1;
	
	DBHelper helper = null;
	
	static MessageDao instance = new MessageDao();
	
	private MessageDao() {
		helper = new DBHelper(AppLogic.getApp());
	}
	
	public static MessageDao getInstance(){
		return instance;
	}
	
	public long addMessage(Message msg){
		long rowId = -1;
		try {
			SQLiteDatabase sd = helper.getWritableDatabase();
			if(sd == null){
				return rowId;
			}
			ContentValues values = new ContentValues();
			values.put("_id", msg.id);
			values.put("_type", msg.type);
			values.put("_msg", msg.msg);
			values.put("_note", msg.note);
			values.put("_time", msg.time);
			if(msg.read){
				values.put("_read", READ);
			}else {
				values.put("_read", NO_READ);
			}
			
			rowId = sd.insert("t_msg", null, values);
		} catch (SQLException e) {
			LogUtil.loge(e.toString());
		}
		return msg.id;
	}
	
	public boolean isExist(String time){
		Cursor cursor = null;
		try {
			SQLiteDatabase sd = helper.getReadableDatabase();
			if(sd == null){
				return true;
			}
			
			String sql = "select * from t_msg where _time=?";
			cursor = sd.rawQuery(sql, new String[]{time});
			if(cursor.moveToFirst()){
				return true;
			}
		} catch (Exception e) {
			LogUtil.loge(e.toString());
		} finally{
			if(cursor != null){
				cursor.close();
			}
		}
		
		return false;
	}
	
	/**
	 * 获取所有的消息
	 * @return
	 */
	public List<Message> getMessage(){
		Cursor cursor = null;
		List<Message> msgList = new ArrayList<Message>();
		try {
			SQLiteDatabase sd = helper.getReadableDatabase();
			if(sd == null){
				return null;
			}
			
			String sql = "select _id,_type,_msg,_note,_time,_read from t_msg";
			cursor = sd.rawQuery(sql, null);
			while (cursor.moveToNext()) {
				Message msg = new Message();
				msg.id = cursor.getLong(cursor.getColumnIndex("_id"));
				msg.type = cursor.getInt(cursor.getColumnIndex("_type"));
				msg.msg = cursor.getString(cursor.getColumnIndex("_msg"));
				msg.note = cursor.getString(cursor.getColumnIndex("_note"));
				msg.time = Long.parseLong(cursor.getString(cursor.getColumnIndex("_time")));
				
				int read = cursor.getInt(cursor.getColumnIndex("_read"));
				if(read == NO_READ){
					msg.read = false;
				}else {
					msg.read = true;
				}
				msgList.add(msg);
			}
		} catch (Exception e) {
			LogUtil.loge(e.toString());
		} finally{
			try {
				if(cursor != null){
					cursor.close();
				}
			} catch (Exception e) {
				LogUtil.loge(e.toString());
			}
		}
		
		Collections.sort(msgList, new TimeComparator());
		return msgList;
	}
	
	/**
	 * 删除一条记录
	 * @param id
	 * @return
	 */
	public boolean deleteMsg(long id){
		try {
			SQLiteDatabase sd = helper.getWritableDatabase();
			int result = sd.delete("t_msg", " _id=?", new String[]{String.valueOf(id)});
			return result != -1 && result != 0;
		} catch (Exception e) {
			LogUtil.loge(e.toString());
		}
		
		return false;
	}
}
