package com.txznet.txz.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Pcm2Wav {

	public static class WaveHeader {

		private char fileID[] = { 'R', 'I', 'F', 'F' };
		private int fileLength;
		private char wavTag[] = { 'W', 'A', 'V', 'E' };
		private char fmtHdrID[] = { 'f', 'm', 't', ' ' };
		private int fmtHdrLeth = 16;
		private short formatTag = 1;
		public short channels = 1;
		public short sampleRate = 16000;
		public short bitsPerSample = 16;
		private short blockAlign = (short) (channels * bitsPerSample / 8);
		private int avgBytesPerSec = blockAlign * sampleRate;
		private char dataHdrID[] = { 'd', 'a', 't', 'a' };
		private int dataHdrLeth;

		public WaveHeader(int fileLength) {
			this.fileLength = fileLength + (44 - 8);
			dataHdrLeth = fileLength;
		}

		public WaveHeader(int fileLength, int channels, int sampleRate,
				int bitsPerSample) {
			this.fileLength = fileLength + (44 - 8);
			dataHdrLeth = fileLength;
			this.channels = (short) channels;
			this.sampleRate = (short) sampleRate;
			this.bitsPerSample = (short) bitsPerSample;
			blockAlign = (short) (channels * bitsPerSample / 8);
			avgBytesPerSec = blockAlign * sampleRate;
		}

		/**
		 * @return byte[] 44个字节
		 * @throws IOException
		 */
		public byte[] getHeader() throws IOException {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			WriteChar(bos, fileID);
			WriteInt(bos, fileLength);
			WriteChar(bos, wavTag);
			WriteChar(bos, fmtHdrID);
			WriteInt(bos, fmtHdrLeth);
			WriteShort(bos, formatTag);
			WriteShort(bos, channels);
			WriteInt(bos, sampleRate);
			WriteInt(bos, avgBytesPerSec);
			WriteShort(bos, blockAlign);
			WriteShort(bos, bitsPerSample);
			WriteChar(bos, dataHdrID);
			WriteInt(bos, dataHdrLeth);
			bos.flush();
			byte[] r = bos.toByteArray();
			bos.close();
			return r;
		}

		private void WriteShort(ByteArrayOutputStream bos, int s)
				throws IOException {
			byte[] mybyte = new byte[2];
			mybyte[1] = (byte) ((s << 16) >> 24);
			mybyte[0] = (byte) ((s << 24) >> 24);
			bos.write(mybyte);
		}

		private void WriteInt(ByteArrayOutputStream bos, int n)
				throws IOException {
			byte[] buf = new byte[4];
			buf[3] = (byte) (n >> 24);
			buf[2] = (byte) ((n << 8) >> 24);
			buf[1] = (byte) ((n << 16) >> 24);
			buf[0] = (byte) ((n << 24) >> 24);
			bos.write(buf);
		}

		private void WriteChar(ByteArrayOutputStream bos, char[] id) {
			for (int i = 0; i < id.length; i++) {
				char c = id[i];
				bos.write(c);
			}
		}
	}
	
	public static boolean encode(String sourcePath,String outPath, int sampleRate) {
		File f = new File(sourcePath);
		if (!f.exists())
			return false;
		WaveHeader header = new WaveHeader((int)f.length(), 1, sampleRate, 16);
		FileOutputStream outStream  = null;
		FileInputStream in = null;
		try {
			outStream = new FileOutputStream(outPath);
			outStream.write(header.getHeader());
			in = new FileInputStream(f);
			byte[] buf = new byte[4096];
			while(in.available() > 0) {
				int len = in.read(buf);
				outStream.write(buf, 0, len);
			}
			outStream.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (outStream != null)
					outStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if (in != null)
					in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}
}
