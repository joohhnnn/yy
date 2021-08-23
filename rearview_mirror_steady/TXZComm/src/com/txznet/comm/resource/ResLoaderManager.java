package com.txznet.comm.resource;

public class ResLoaderManager {

	private static ResLoaderManager sInstance = new ResLoaderManager();
	
	private ResLoader resLoader;
	
	private ResLoaderManager(){
	}
	
	
	public  static ResLoaderManager getInstance(){
		return sInstance;
	}
	
	public void reloadRes(){
		if(resLoader == null){
			resLoader = new ResLoaderImpl();
		}
		resLoader.reloadResources();
	}
}
