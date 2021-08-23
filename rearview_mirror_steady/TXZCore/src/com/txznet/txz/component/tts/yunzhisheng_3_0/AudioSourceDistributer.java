package com.txznet.txz.component.tts.yunzhisheng_3_0;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.txznet.txz.module.record.Recorder;

public class AudioSourceDistributer{
	private static AudioSourceDistributer sInstance = null;
	
	private AudioSourceDistributer(){	
	}
	
	public static AudioSourceDistributer getIntance(){
		if (sInstance == null){
			synchronized (AudioSourceDistributer.class) {
				if (sInstance == null){
					sInstance = new AudioSourceDistributer();
				}
			}
		}
		return sInstance;
	}
	private Set<Recorder> recCoderSet = new HashSet<Recorder>();
	
	public synchronized void addRecorder(Recorder recorder) {
		recCoderSet.add(recorder);
	}

	public synchronized void delRecorder(Recorder recorder) {
		recCoderSet.remove(recorder);
	}

	public synchronized void distribute(byte[] data, int len, int channel) {
		if (recCoderSet.isEmpty() || data == null){
			return;
		}
		byte[] buf = null;
		int bufSize = 0;
		if (channel == 1 && len > 0){
			bufSize = len;
			buf = new byte[bufSize];
			for(int i = 0; i < bufSize; i++){
				buf[i] = data[i];
			}
		}else if (channel == 2 && len > 1){
			bufSize = len/2;
			buf = new byte[bufSize];
			int j = 0;
			int k = 0;
			for(j = 0; j < bufSize; j = j + 2, k = k + 4){
				buf[j] = data[k];
				buf[j + 1] = data[k + 1];
			}
		}else if (channel == 3 && len > 3){
			bufSize = len/2;
			buf = new byte[bufSize];
			int j = 0;
			int k = 2;
			for(j = 0; j < bufSize; j = j + 2, k = k + 4){
				buf[j] = data[k];
				buf[j + 1] = data[k + 1];
			}
		}else{
			return;
		}
		Iterator<Recorder> it = recCoderSet.iterator();
		while (it.hasNext()) {
			Recorder recorder = it.next();
			recorder.write(buf, bufSize);
		}
	}

}
