package com.txznet.txz.util;

public class UIDUtils {

	public  static  String  genarateUid(long uid){
		return MD5Util.generateMD5(uid + "wzQLbBnyj3IN5a9naUDWXpuOJKvkWl");
	}
	
	public  static  String  genarateAppid(String  appid){
		return MD5Util.generateMD5(appid+ "Zodj6gI8E9WjiJLBxISqnHkU4ttSlxYx");
	}
	
	
}
