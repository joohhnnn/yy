package com.txznet.txz.jni;

import java.util.Arrays;

import com.txznet.comm.remote.util.LogUtil;

public class TXZMediaUtil {
	
	static{
        System.loadLibrary("TXZCoreCodec");
    }
	
	public static boolean saveFrame(String inFile , String outFile,  long time) {
		// ffmpeg -ss 00:01:00 -i /sdcard/metadata/V.mp4 -y -f image2 -vframes 1 /sdcard/metadata/pic/frame1
		String[] commands = new String[11];
        commands[0] = "ffmpeg";
        // 指定时间，单位s
        commands[1] = "-ss";
        commands[2] = (double)time/1000 + "";
        // 输入
        commands[3] = "-i";
        commands[4] = inFile;
        // 覆盖输出
        commands[5] = "-y";
        // 采用格式
        commands[6] = "-f";
        commands[7] = "image2";
        commands[8] = "-vframes";
        commands[9] = "1";
        //输出文件
        commands[10] = outFile;
		LogUtil.logd("save frame:" + Arrays.toString(commands));
		int result = ffmpegMain(commands);
		LogUtil.logd("save frame result: " + result);
		if (result == 0) {
			return true;
		}
		return false;
	}
	
	public native static int ffmpegMain(String[] commands);

}
