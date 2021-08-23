package com.txznet.txz.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.zip.CRC32;

import android.os.Environment;

public class TtsSynUtil {
  public static final String TTS_DIR = Environment.getExternalStorageDirectory() + "/txz/tts/";
  public static final int WAV_LIMIT_SIZE = 128*1024;
  private static boolean bEnable = false;
  
  public static boolean isEnable(){
	  return bEnable;
  }
  
  public static void enable(boolean b){
	  bEnable = b;
  }
  
  public static void pcm2wav(String pcmParentDir, String destWavPath){
	  if (!bEnable){
		  return;
	  }
	  
	  File dir = new File(pcmParentDir);
	  if (!dir.isDirectory() || !dir.exists()){
		  return;
	  }
	  String[] files = dir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				if (filename.endsWith(".pcm")) {
					return true;
				}
				return false;
			}
		});
		for (int i = 0; i < files.length; i++) {
			 File f = new File(pcmParentDir + "/" + files[i]);
			 if (f.exists() && f.length() < WAV_LIMIT_SIZE){
				 Pcm2Wav.encode(pcmParentDir + "/" + files[i],  destWavPath,  16*1000);
			 }
			 f.delete();
		}
		
		dir.delete();
  }
  
	public static String filterText(String sText) {
		if (!bEnable){
			return null;
		}
		
		CRC32 crc32 = new CRC32();
		crc32.update(sText.getBytes());
		long crc32Value = crc32.getValue();
		
		String destPath = TTS_DIR + crc32Value + ".wav";
		
		File f = new File(destPath);
		if (f.exists()){
			return destPath;
		}
		
		return null;
	}
}
