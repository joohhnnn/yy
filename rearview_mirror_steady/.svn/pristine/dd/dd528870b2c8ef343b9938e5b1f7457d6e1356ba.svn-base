package com.txznet.txz.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.jni.JNIHelper;

class ApkFile {

	static void unzipFile(ZipFile zipFile, ZipEntry entry, String dst) {
		JNIHelper.logd("begin unzip " + entry.getName() + ": size="
				+ entry.getCompressedSize() + "/" + entry.getSize() + ",time="
				+ entry.getTime() + ",crc=" + entry.getCrc());
		try {
			File f = new File(dst);
			f.getParentFile().mkdirs();
			FileOutputStream out = new FileOutputStream(f);
			InputStream in = zipFile.getInputStream(entry);
			do {
				if (ProjectCfg.enableOfflineCallGrammar() == false) {
					if (entry.getName().equals("data/txz.bnf")) {
						byte[] buf = new byte[(int) entry.getSize()];
						in.read(buf);
						String s = new String(buf);
						out.write(s.replace("|<callStart>|", "|").getBytes());
						break;
					}
					if (entry.getName().equals("data/service_contacts.json")) {
						out.write("[]".getBytes());
					}
				}

				byte[] buf = new byte[1024];
				int l = 0;
				while ((l = in.read(buf)) > 0) {
					out.write(buf, 0, l);
				}
			} while (false);
			out.close();
			in.close();
			f = new File(dst);
			f.setLastModified(entry.getTime());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static void unzip() {
		String[] dataPrefix = new String[] { "assets/data/",
				"assets/tts_yunzhisheng/", "assets/tts_ifly/" };
		String[] dataUnzip = new String[] { "/data/", "/tts_yunzhisheng/",
				"/tts_ifly/" };
		String appDir = GlobalContext.get().getApplicationInfo().dataDir;
		try {
			ZipFile zipFile = new ZipFile(GlobalContext.get()
					.getApplicationInfo().sourceDir);
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) entries.nextElement();
				try {
					for (int i = 0; i < dataPrefix.length; ++i) {
						if (entry.getName().startsWith(dataPrefix[i])) {
							String dataName = entry.getName().substring(
									dataPrefix[i].length());
							String dataTarName = appDir + dataUnzip[i]
									+ dataName;
							File fTar = new File(dataTarName);
							if (fTar.exists() // 目标文件存在
									&& fTar.length() == entry.getSize()// 尺寸相同
									&& fTar.lastModified() == entry.getTime() // 最后修改时间相同
							) {
								JNIHelper.logd("no need unzip: "
										+ entry.getName());
								break;
							}
							unzipFile(zipFile, entry, dataTarName);
							break;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			zipFile.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
