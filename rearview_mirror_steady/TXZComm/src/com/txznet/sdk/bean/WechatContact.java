package com.txznet.sdk.bean;

import com.txznet.comm.util.JSONBuilder;

/**
 * 微信联系人
 *
 * @deprecated 已随 {@link com.txznet.sdk.TXZWechatManager} 一同弃用,
 * 新版本见 {@link com.txznet.sdk.bean.WechatContactV2}
 */
@Deprecated
public class WechatContact {
	protected String mId;
	protected String mIcon;
	protected String mNick;
	protected boolean mBlocked;
	protected boolean mIsGroup;
	
	public WechatContact(){
		
	}
	
	public WechatContact(byte[] json){
		JSONBuilder builder = new JSONBuilder(json);
		
		setId(builder.getVal("id", String.class));
		setIcon(builder.getVal("icon", String.class));
		setNick(builder.getVal("nick", String.class));
		setBlocked(builder.getVal("blocked", Boolean.class));
		setIsGroup(builder.getVal("isgroup", Boolean.class));
	}
	
	public String getId() {
		return mId;
	}
	public void setId(String id) {
		this.mId = id;
	}
	public String getIcon() {
		return mIcon;
	}
	public void setIcon(String icon) {
		this.mIcon = icon;
	}
	public String getNick() {
		return mNick;
	}
	public void setNick(String nick) {
		this.mNick = nick;
	}
	public boolean isBlocked() {
		return mBlocked;
	}
	public void setBlocked(boolean blocked) {
		this.mBlocked = blocked;
	}
	public boolean isIsGroup() {
		return mIsGroup;
	}
	public void setIsGroup(boolean isGroup) {
		this.mIsGroup = isGroup;
	}
}
