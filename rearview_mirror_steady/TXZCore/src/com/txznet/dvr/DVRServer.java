package com.txznet.dvr;

import android.os.IBinder;
import android.os.RemoteException;

import com.txznet.txz.jni.TXZMediaUtil;

public class DVRServer  extends IDVRInterface.Stub {
	
	@Override
	public boolean getFrameAtTime(String inFile, String outFile, long time)
			throws RemoteException {
		return TXZMediaUtil.saveFrame(inFile, outFile, time);
	}
		
	public DVRServer() {
		super();
	}
	
	public IBinder getBinder() {
		return this.asBinder();
	}

}
