package com.txznet.audio.bean;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LocalBuffer implements Serializable {
	private static final long serialVersionUID = 4846734313705367005L;
	public static final long CRC32 = 0;
	public final static int MIN_BUFFER_SIZE = 512* 1024;
	public final static int MAX_BUFFER_SIZE = 1*1024 * 1024;//最大5M

	private long mFrom;
	private long mTo;
	private float mFromP;
	private float mToP;
	private long mTotalSize;
	private long mCrc32;
	private byte[] bufferData;

	private LocalBuffer() {
	}

	public static LocalBuffer buildFull(long total) {
		LocalBuffer buffer = new LocalBuffer();
		buffer.mTotalSize = total;
		buffer.setFrom(0);
		buffer.setTo(total - 1);
		return buffer;
	}

	public static LocalBuffer build(long from, long to, long totalSize,
			long crc32) {
		LocalBuffer buffer = new LocalBuffer();
		buffer.mTotalSize = totalSize;
		buffer.setFrom(from);
		buffer.setTo(to);
		buffer.setCrc32(crc32);
		return buffer;
	}

	public static LocalBuffer build(long from, long to, long totalSize,
			long crc32, byte[] bufferData) {
		LocalBuffer buffer = new LocalBuffer();
		buffer.mTotalSize = totalSize;
		buffer.setFrom(from);
		buffer.setTo(to);
		buffer.setCrc32(crc32);
		buffer.setBufferData(bufferData);
		return buffer;
	}
	
	public static LocalBuffer build(float mFromP, float mToP, long totalSize,
			byte[] bufferData) {
		LocalBuffer buffer = new LocalBuffer();
		buffer.mTotalSize = totalSize;
		buffer.setFromP(mFromP);
		buffer.setToP(mToP);
		buffer.setCrc32(LocalBuffer.CRC32);
		buffer.setBufferData(bufferData);
		return buffer;
	}

	public long getTotal() {
		return mTotalSize;
	}

	public long getFrom() {
		return mFrom;
	}

	public void setFrom(long from) {
		if (from < 0)
			from = 0;
		this.mFrom = from;
		mFromP = from * 1f / mTotalSize;
		if (mFromP < 0) {
			mFromP = 0;
		}
	}

	public long getTo() {
		return mTo;
	}

	public void setTo(long to) {
		if (to < 0 || to >= mTotalSize)
			to = mTotalSize - 1;
		this.mTo = to;
		mToP = (to + 1) * 1f / mTotalSize;
		if (mToP > 1) {
			mToP = 1;
		}
	}

	public float getFromP() {
		return mFromP;
	}

	public float getToP() {
		return mToP;
	}

	public long getCrc32() {
		return mCrc32;
	}

	public void setCrc32(long mCrc32) {
		this.mCrc32 = mCrc32;
	}

	public void setFromP(float mFromP) {
		this.mFromP = mFromP;
	}

	public void setToP(float mToP) {
		this.mToP = mToP;
	}

	public void setTotal(long mTotalSize) {
		this.mTotalSize = mTotalSize;
	}

	public static byte[] toBytes(Collection<LocalBuffer> buffers) {
		ByteBuffer bb = ByteBuffer.allocate(32 * buffers.size());
		for (LocalBuffer b : buffers) {
			bb.putLong(b.getTotal());
			bb.putLong(b.getFrom());
			bb.putLong(b.getTo());
			bb.putLong(b.getCrc32());
		}
		return bb.array();
	}

	public static List<LocalBuffer> buildList(byte[] data, int offset, int len) {
		if (len % 32 != 0)
			return null;
		List<LocalBuffer> lst = new ArrayList<LocalBuffer>();
		ByteBuffer bb = ByteBuffer.wrap(data, offset, len);
		while (bb.hasRemaining()) {
			LocalBuffer buffer = new LocalBuffer();
			buffer.mTotalSize = bb.getLong();
			buffer.setFrom(bb.getLong());
			buffer.setTo(bb.getLong());
			buffer.setCrc32(bb.getLong());
			lst.add(buffer);
		}
		return lst;
	}

	public static List<LocalBuffer> buildList(byte[] data) {
		return buildList(data, 0, data.length);
	}

	@Override
	public String toString() {
		return "LocalBuffer [mFrom=" + mFrom + ", mTo=" + mTo + ", mFromP="
				+ mFromP + ", mToP=" + mToP + ", mTotalSize=" + mTotalSize
				+ ", mCrc32=" + mCrc32 + "]";
	}

	public void setBufferData(byte[] bufferData) {
		this.bufferData = bufferData;
	}

	public byte[] getBufferData() {
		return bufferData;
	}

}
