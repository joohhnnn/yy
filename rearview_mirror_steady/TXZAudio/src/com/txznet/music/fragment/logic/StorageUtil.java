package com.txznet.music.fragment.logic;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.storage.StorageManager;

/**
 * 
 * @author ASUS User
 *
 */
public class StorageUtil {

	public static  List<String> getVolumeState(Context ctx){
		List<String> volumePaths=new ArrayList<String>();
		StorageManager sm = (StorageManager) ctx.getSystemService(Context.STORAGE_SERVICE);
		try {
			String[] paths = (String[]) sm.getClass().getMethod("getVolumePaths", null).invoke(sm, null);
			for (int i = 0; i < paths.length; i++) {
				String status = (String) sm.getClass().getMethod("getVolumeState", String.class).invoke(sm, paths[i]);
				if (status.equals(android.os.Environment.MEDIA_MOUNTED)) {
					volumePaths.add(paths[i]);
				}
			}
		} catch (Exception e) {
			
		}
		return volumePaths;
	}
}
