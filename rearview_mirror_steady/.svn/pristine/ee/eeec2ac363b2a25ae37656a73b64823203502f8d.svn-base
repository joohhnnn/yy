package com.txznet.txz.component.tts.yunzhisheng_3_0;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/*
 * 要点：1、循环读写。 2、块读写。 3、已满则覆盖写。4、空则阻塞
 */
public class BlockDataLoopCache{
	private static final int CAPACITY = 64*1024;
	private static final int SIGNAL_OK = 0;
	private static final int SIGNAL_INTERRUPT = 0;
	private byte[] mBufferCache = null;
	private int mHead = 0;
	private int mTail = 0;// 不包括该位
	private int mSize = 0;
	private PipedInputStream mInPipe = null;
	private PipedOutputStream mOutPipe = null;

	public BlockDataLoopCache(){
		mBufferCache = new byte[CAPACITY];
		buildSignalPipe();
	}
	
	public BlockDataLoopCache(int nCapacity){
		if (nCapacity <= 0){
			nCapacity = CAPACITY;
		}
		mBufferCache = new byte[nCapacity];
		buildSignalPipe();
	}
	
	private void buildSignalPipe() {
		mInPipe = new PipedInputStream();
		mOutPipe = new PipedOutputStream();
		try {
			mInPipe.connect(mOutPipe);
		} catch (IOException e) {
		}
	}
	
	private void sendSignal(int nSignal){
		try {
			mOutPipe.write(nSignal);
		} catch (IOException e) {
		}
	}
	
	private void flushSignal(){
		try {
			mOutPipe.flush();
		} catch (IOException e) {
		}
	}
	
	private int recvSignal(){
		int nSignal = -1;
		try {
			nSignal = mInPipe.read();
		} catch (IOException e) {
		}
		return nSignal;
	}
	
	public synchronized void clear(){
		mHead = 0;
		mTail = 0;
		mSize = 0;
		flushSignal();
	}
	
	public synchronized int write(byte[] data){
		int free = mBufferCache.length - mTail;
		if (free >= data.length) {
			System.arraycopy(data, 0, mBufferCache, mTail, data.length);
			mTail += data.length;
			if (mTail == mBufferCache.length) {
				mTail = 0;
			}
		} else {
			System.arraycopy(data, 0, mBufferCache, mTail, free);
			System.arraycopy(data, free, mBufferCache, 0, data.length - free);
			mTail = data.length - free;
			//写满了则覆盖
			if (mTail >= mHead) {
				mHead = mTail;
			}
		}
		if (mTail > mHead){
			mSize = mTail - mHead;
		}else{
			mSize = mBufferCache.length - (mHead - mTail);
		}
		sendSignal(SIGNAL_OK);
		return 0;
	}
	
	public synchronized int write(byte[] data, int length){
		int free = mBufferCache.length - mTail;
		if (free >= length) {
			System.arraycopy(data, 0, mBufferCache, mTail, length);
			mTail += length;
			if (mTail == mBufferCache.length) {
				mTail = 0;
			}
		} else {
			System.arraycopy(data, 0, mBufferCache, mTail, free);
			System.arraycopy(data, free, mBufferCache, 0, length - free);
			mTail = length - free;
			//写满了则覆盖
			if (mTail >= mHead) {
				mHead = mTail;
			}
		}
		if (mTail > mHead){
			mSize = mTail - mHead;
		}else{
			mSize = mBufferCache.length - (mHead - mTail);
		}
		sendSignal(SIGNAL_OK);
		return 0;
	}
	
	public int read(byte[] data, int size){
		if (isEmpty()) {
			// waiting...
			if (recvSignal() != SIGNAL_OK){
				return 0;
			}
		}
		return readFromCache(data, size);
	}
	public synchronized int readFromCache(byte[] data, int size){
		if (mSize <= 0){
			return 0;
		}
		//最多读取mSize个字节
		if (mSize < size) {
			size = mSize;
		}
        int newHead = mHead + size;
		if (newHead <= mBufferCache.length) {
			System.arraycopy(mBufferCache, mHead, data, 0, size);
			if (newHead == mBufferCache.length) {
				mHead = 0;
			}else{
				mHead = newHead;
			}
		} else {
			int free = size - (mBufferCache.length - mHead);
			System.arraycopy(mBufferCache, mHead, data, 0, size - free);
			System.arraycopy(mBufferCache, 0, data, size - free, free);
			mHead = free;
		}
		mSize -= size;
		return size;
	}
	
	public synchronized boolean isEmpty(){
		return mSize == 0;
	}
	
	public synchronized int size(){
		return mSize;
	}
}

