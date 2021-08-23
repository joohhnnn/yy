package com.txznet.txz.module.record;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.List;

import android.os.HandlerThread;

import com.txznet.txz.util.TXZHandler;

public class SimpleRecorder extends Recorder{
	private DatagramSocket mClientSocket = null;
	private List<InetAddressInfo> mTargets = new LinkedList<InetAddressInfo>();
	private HandlerThread workerThread;
	private TXZHandler workerHandler;
	
    private  class InetAddressInfo{
    	String mIp;
    	int mPort;
    }
    
	public SimpleRecorder() {
		try {
			mClientSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		if (mClientSocket != null) {
			workerThread = new HandlerThread("SimpleRecorderThread");
			workerThread.start();
			workerHandler = new TXZHandler(workerThread.getLooper());
		}
	}
   
	@Override
	public void close() {
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				closeInner();
			}
		};
		workerHandler.postDelayed(oRun, 0);
	}
    
	@Override
	public void write(final byte[] data, final int len) {
		byte[] buffer = new byte[len + 8];
		long currTime = System.currentTimeMillis();
		buffer[7] = (byte) (currTime & 0xff);// 最低位   
		buffer[6] = (byte) ((currTime >> 8) & 0xff);// 次低位   
		buffer[5] = (byte) ((currTime >> 16) & 0xff);// 次高位   
		buffer[4] = (byte) ((currTime >> 24) & 0xff);// 最高位,无符号右移。
		buffer[3] = (byte) ((currTime  >> 32) & 0xff);// 最低位   
		buffer[2] = (byte) ((currTime >> 40) & 0xff);// 次低位   
		buffer[1] = (byte) ((currTime >> 48) & 0xff);// 次高位   
		buffer[0] = (byte) ((currTime >> 56) & 0xff);// 最高位,无符号右移。
		
//		for (int ix = 0; ix < 8; ++ix) {
//			int offset = 64 - (ix + 1) * 8;
//			buffer[ix] = (byte) ((currTime >> offset) & 0xff);
//		}

		for(int i = 0; i < len; ++i){
			buffer[i + 8] = data[i];
		}
		final byte[] buf = buffer;
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				try {
					send(buf, len);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		};
		if (workerHandler != null){
		     workerHandler.postDelayed(oRun, 0);
		}
	}
   
   private synchronized void send(byte[] data, int len) throws Exception{
		if (null == data || len == 0 || null == mClientSocket
				|| null == mTargets) {
			return;
		}
	   for (InetAddressInfo addrInfo : mTargets){
		    InetAddress addr = InetAddress.getByName(addrInfo.mIp);
	        DatagramPacket sendPacket  = new DatagramPacket(data ,len , addr , addrInfo.mPort);
	        mClientSocket.send(sendPacket);
	   }
   }
   
   public void addInetAddr(final String ip, final int port){
	   Runnable oRun = new Runnable(){
		@Override
		public void run() {
			addInetAddrInner(ip, port);
		} 
	   };
	   workerHandler.postDelayed(oRun, 0);
   }
   
   public void removeInetAddr(final String ip, final int port){
	   Runnable oRun = new Runnable(){
		@Override
		public void run() {
			removeInetAddrInner(ip, port);
		} 
	   };
	   workerHandler.postDelayed(oRun, 0);
   }
   
   private synchronized void addInetAddrInner(String ip, int port){
	   mTargets.clear();//暂时只支持一个port口
	   InetAddressInfo addrInfo = new InetAddressInfo();
	   addrInfo.mIp = ip;
	   addrInfo.mPort = port;
	   if (!mTargets.contains(addrInfo)){
	        mTargets.add(addrInfo);
	   }
   }
   
   private synchronized void removeInetAddrInner(String ip, int port){
	   InetAddressInfo addrInfo = new InetAddressInfo();
	   addrInfo.mIp = ip;
	   addrInfo.mPort = port;
	   mTargets.remove(addrInfo);
	   mTargets.clear();//暂时只支持一个port口
   }
   
   private synchronized void closeInner(){
	   if (mClientSocket != null){
		   mClientSocket.close();
		   mClientSocket = null;
	   }
	   mTargets = null;
   }
}
