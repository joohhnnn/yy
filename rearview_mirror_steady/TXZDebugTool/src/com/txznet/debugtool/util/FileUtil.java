package com.txznet.debugtool.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class FileUtil {
	public static void copyFile(File f1, File f2) {
		try {
			int length = 2097152;
			FileInputStream in = new FileInputStream(f1);
			FileOutputStream out = new FileOutputStream(f2);
			byte[] buffer = new byte[length];
			while (true) {
				int ins = in.read(buffer);
				if (ins == -1) {
					in.close();
					out.flush();
					out.close();
				} else
					out.write(buffer, 0, ins);
			}
		} catch (Exception e) {
		}
	}
}
