package com.txznet.txz.util.recordcenter.cache;

import java.io.IOException;
import java.io.OutputStream;

public interface DataWriter {
	public final static int BUFFER_WRITE_ALIGN = 4; // 写入对齐为4个字节

	/**
	 * 写入数据
	 * 
	 * @param data
	 * @param offset
	 * @param len
	 * @return 写入的数据量，出错抛出异常
	 * @throws IOException
	 */
	public int writeData(byte[] data, int offset, int len) throws IOException;

	/**
	 * 输出流
	 *
	 */
	public static class OutputStreamDataWriter implements DataWriter {
		private OutputStream out;

		public OutputStreamDataWriter(OutputStream out) {
			this.out = out;
		}

		@Override
		public int writeData(byte[] data, int offset, int len)
				throws IOException {
			out.write(data, offset, len);
			return len;
		}
	}
}