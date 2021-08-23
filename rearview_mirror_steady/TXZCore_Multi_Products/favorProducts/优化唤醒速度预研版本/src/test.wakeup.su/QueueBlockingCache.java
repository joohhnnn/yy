package com.txznet.test.wakeup.su;


/*
 * 要点：1、循环读写。 2、块读写。 3、已满则覆盖写。4、空则阻塞
 * 写满则丢掉前面的数组
 */
public class QueueBlockingCache{
	private static final int CAPACITY = 64*1024;
	private byte[] mBufferCache = null;
	private int mSize = 0;
	private boolean mEnable = true;
	public QueueBlockingCache(){
		mBufferCache = new byte[CAPACITY];
	}
	
	public QueueBlockingCache(int nCapacity){
		if (nCapacity <= 0){
			nCapacity = CAPACITY;
		}
		mBufferCache = new byte[nCapacity];
		mSize = 0;
		mEnable = true;
	}
	
	public synchronized void enable(){
		mSize = 0;
		mEnable = true;
	}
	
	public synchronized void disable() {
		mEnable = false;
		notifyAll();
	}
	private static enum InterruptType{
		INTERRUP_TTYPE_IDEL,
		INTERRUP_TTYPE_WAIT,
		INTERRUP_TTYPE_INTERRUPED
		
	}
	private InterruptType mInterruptType = InterruptType.INTERRUP_TTYPE_IDEL;
	public synchronized void interrupt() {
		if (mInterruptType == InterruptType.INTERRUP_TTYPE_WAIT) {
			mInterruptType = InterruptType.INTERRUP_TTYPE_INTERRUPED;
			notifyAll();
		}
	}
	
	//覆盖写
	public synchronized int write(byte[] data, int offset, int length){
		int newSize = mSize + length;
		int newOffset = newSize - mBufferCache.length;
		if (newOffset > 0){
			mSize -= newOffset;
			System.arraycopy(mBufferCache, newOffset, mBufferCache, 0, mSize);
		}
		System.arraycopy(data, offset, mBufferCache, mSize, length);
		mSize+= length;
		notifyAll();
		return 0;
	}
	
	public synchronized int read(byte[] data, int offset, int size){		
		// 空则阻塞等待
		while (mEnable && mSize <= 0) {
			try {
				mInterruptType = InterruptType.INTERRUP_TTYPE_WAIT;
				wait(1000);
			} catch (InterruptedException e) {
				mInterruptType = InterruptType.INTERRUP_TTYPE_IDEL;
				return 0;
			}
			// interrupt则不继续等待数据
			if (mInterruptType == InterruptType.INTERRUP_TTYPE_INTERRUPED) {
				mInterruptType = InterruptType.INTERRUP_TTYPE_IDEL;
				return 0;
			}
			mInterruptType = InterruptType.INTERRUP_TTYPE_IDEL;
		}
		
		//最多读取mSize个字节
		if (mSize < size) {
			size = mSize;
		}
		
		System.arraycopy(mBufferCache, 0, data, offset, size);
		mSize -= size;
		if (mSize > 0){
			System.arraycopy(mBufferCache, size, mBufferCache, offset, mSize);
		}
		return size;
	}
	
	public boolean isEmpty(){
		return mSize == 0;
	}
	
	public int size(){
		return mSize;
	}
}

