package com.txznet.txz.cfg;

import java.io.File;
import android.os.Environment;

public class DebugCfg {
	public static File getDebugRoot() {
		try {
			return new File(Environment.getExternalStorageDirectory(), "txz");
		} catch (Exception e) {
			return new File(".");
		}
	}

	public static boolean YZS_LOG_DEBUG = new File(getDebugRoot(),
			"yzs_log.debug").exists();

	public static boolean SAVE_RAW_PCM_CACHE = new File(getDebugRoot(),
			"pcm_enable.debug").exists();

	public static boolean debug_yzs() {
		return YZS_LOG_DEBUG;
	}
}
