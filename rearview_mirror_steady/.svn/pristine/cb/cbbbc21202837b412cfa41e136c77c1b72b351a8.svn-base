package com.txznet.sdk.bean;

/**
 * 微信消息
 *
 * @deprecated 已随 {@link com.txznet.sdk.TXZWechatManager} 一同弃用,
 * 新版本见 {@link com.txznet.sdk.bean.WechatMessageV2}
 */
public class WechatMessage {
	public final static int MSG_TYPE_TEXT = 1; //文本消息
	public final static int MSG_TYPE_HTML = 2; //HTML消息
	public final static int MSG_TYPE_IMG = 3; //图片消息
	public final static int MSG_TYPE_VOICE = 4; //语音消息
	public final static int MSG_TYPE_LOCATION = 5; // 位置消息

	private String mid;
	private String mSenderId;
	private String mSessionId;
	private int mType;
	private String mContent;


	public WechatMessage() {

	}

	public WechatMessage(String id, String sessionId, String senderId, int type, String content) {
		setId(id);
		setSessionId(sessionId);
		setSenderId(senderId);

		setContent(content);
	}

	public String getId(){
		return mid;
	}

	public void setId(String id){
		mid = id;
	}

	public String getSenderId(){
		return mSenderId;
	}

	public void setSenderId(String id){
		mSenderId = id;
	}

	public String getSessionId(){
		return mSessionId;
	}

	public void setSessionId(String id){
		mSessionId = id;
	}

	public int getType(){
		return mType;
	}

	public void setType(int type){
		this.mType = type;
	}

	public String getContent(){
		return mContent;
	}

	public void setContent(String content){
		mContent = content;
	}
}
