package com.txznet.music.service;

/**
 * 创建监听器
 * @author telenewbie
 *
 */
public abstract class CreateCallbackFactory {

	public abstract  IEngineCallBack getEngine(int type);
	
	
	public  IEngineCallBack createCallback(int type){
		return getEngine(type);
	}
}
