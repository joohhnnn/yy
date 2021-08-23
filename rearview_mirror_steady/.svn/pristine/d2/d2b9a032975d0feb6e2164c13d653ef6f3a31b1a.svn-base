package com.txznet.feedback.service;

import java.util.ArrayList;
import java.util.List;

import com.txznet.feedback.data.Message;
import com.txznet.feedback.dtool.MessageDao;

public class MsgService {
	
	private MessageDao mDao = MessageDao.getInstance();
	
	private static MsgService instance = new MsgService();
	
	private List<Message> mAllMsgList = new ArrayList<Message>();
	
	private boolean mIsNewIn = false;
	
	private MsgService(){ }
	
	public static MsgService getInstance(){
		return instance;
	}
	
	/**
	 * 获取所有的消息
	 * @return
	 */
	public List<Message> getMessageList(){
		List<Message> list = mDao.getMessage();
		mAllMsgList.clear();
		mAllMsgList.addAll(list);
		return list;
	}
	
	public String getMessageIdByFilePathName(String filePathName){
		for(Message msg:mAllMsgList){
			if(msg.msg.equals(filePathName)){
				return String.valueOf(msg.id);
			}
		}
		
		return "";
	}
	
	public long addMessage(Message msg){
		boolean isExist = mDao.isExist(String.valueOf(msg.time));
		if(isExist){
			return -1;
		}
		
		if(msg.type == Message.TYPE_NET){
			mIsNewIn = true;
		}
		
		return mDao.addMessage(msg);
	}
	
	public void setIsNewIn(boolean newIn){
		this.mIsNewIn = newIn;
	}
	
	public boolean isNewIn(){
		return mIsNewIn;
	}
	
	public boolean deleteMsg(long id){
		return mDao.deleteMsg(id);
	}
}