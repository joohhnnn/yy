package com.txznet.cldfm.util;

import java.lang.ref.WeakReference;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class WeakHandler extends Handler{
	private final WeakReference<MessageHandler> mMessageHandler;

	public WeakHandler(MessageHandler handler){
		this.mMessageHandler = new WeakReference(handler);
	}

	public WeakHandler(MessageHandler handler, Handler.Callback callback){
		super(callback);
		this.mMessageHandler = new WeakReference(handler);
	}

	public WeakHandler(MessageHandler handler, Looper looper, Handler.Callback callback){
		super(looper, callback);
		this.mMessageHandler = new WeakReference(handler);
	}

	public WeakHandler(MessageHandler handler, Looper looper){
		super(looper);
		this.mMessageHandler = new WeakReference(handler);
	}

	public void handleMessage(Message msg){
		MessageHandler handler = (MessageHandler)this.mMessageHandler.get();
		if (handler != null) {
			handler.handleMessage(msg);
		}
	}
}