package com.txznet.feedback.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.txznet.comm.remote.util.LogUtil;

/**
 * type 表示类型，官方消息和本地我的语音
 * object 携带的信息
 * msg 具体的消息，官方消息携带内容，我的语音携带本地的路径
 *
 */
public class Message {
	public static final int TYPE_NET = 1;
	public static final int TYPE_SELF = 2;
	
	public long id;
	public int type;
	public String msg;
	public String note;
	public long time;       // 时间戳
	public boolean read;
	
	public static Message newMessage(String msg){
		Message message = new Message();
		message.id = System.currentTimeMillis();
		message.type = Message.TYPE_NET;
		message.msg = msg;
		message.time = message.id;
		return message;
	}
	
	public static Message parseJsonObject(JSONObject jo){
		try {
			String time = jo.getString("time");
			String result = jo.getString("result");
			String state = jo.getString("state");
			Message msg = new Message();
			msg.time = Long.parseLong(time);
			msg.type = Message.TYPE_NET;
			msg.note = state;
			msg.msg = result;
			msg.id = msg.time;
			return msg;
		} catch (JSONException e) {
			LogUtil.loge(e.toString());
			return null;
		}
	}
}
