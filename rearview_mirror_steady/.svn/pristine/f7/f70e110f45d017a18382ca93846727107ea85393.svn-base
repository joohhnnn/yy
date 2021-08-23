package com.txznet.txz.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.os.Environment;
import android.text.TextUtils;

public class TXZStatisticser {
	private static final String SAVE_PATH = Environment
			.getExternalStorageDirectory().getPath() + "/txz/statistics";
	
	private static final String ENABLE_PATH = Environment
			.getExternalStorageDirectory().getPath() + "/txz/statis_enable";
	
	private static final int MAX_LENGTH = 50;
	
	private static FileWriter sWriter = null;
	private static Boolean sEnable = null;

	private static boolean enable() {
		if (sEnable == null) {
            File f = new File(ENABLE_PATH);
            if (f.exists()){
            	sEnable = true;
            }else{
            	sEnable = false;
            }
		}
		if (sEnable == null) {
			return false;
		} else {
			return sEnable;
		}
	}
	
	public static void append(String content) {
		if (!enable() || TextUtils.isEmpty(content)|| content.length() > MAX_LENGTH){
			return;
		}
		
		if (sWriter == null) {
			try {
				sWriter = new FileWriter(SAVE_PATH,  true);
			} catch (IOException e) {
			}
		}

		if (sWriter != null) {
			try {
				sWriter.write(content + "\n");
				sWriter.flush();
			} catch (IOException e) {
			}
		}
	}
}