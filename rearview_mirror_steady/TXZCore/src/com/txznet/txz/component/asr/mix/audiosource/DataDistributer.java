package com.txznet.txz.component.asr.mix.audiosource;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import com.txznet.txz.module.record.Recorder;
import com.txznet.txz.util.recordcenter.TXZAudioRecorder;

public class DataDistributer{
	private static DataDistributer sInstance = null;
	
	private DataDistributer(){	
	}
	
	public static DataDistributer getIntance(){
		if (sInstance == null){
			synchronized (DataDistributer.class) {
				if (sInstance == null){
					sInstance = new DataDistributer();
				}
			}
		}
		return sInstance;
	}
	private Set<Recorder> recCoderSet = new HashSet<Recorder>();
	
	public void addRecorder(Recorder recorder) {
		synchronized (recCoderSet) {
			recCoderSet.add(recorder);
			start();
		}
	}

	public void delRecorder(Recorder recorder) {
		synchronized (recCoderSet) {
			recCoderSet.remove(recorder);
			if (recCoderSet.isEmpty()) {
				stop();
			}
		}
	}

	public void distribute(byte[] data, int len, int channel) {
		synchronized (recCoderSet) {
			if (recCoderSet.isEmpty() || data == null) {
				return;
			}
			byte[] buf = null;
			int bufSize = 0;
			if (channel == 1 && len > 0) {
				bufSize = len;
				buf = new byte[bufSize];
				for (int i = 0; i < bufSize; i++) {
					buf[i] = data[i];
				}
			} else if (channel == 2 && len > 1) {
				bufSize = len / 2;
				buf = new byte[bufSize];
				int j = 0;
				int k = 0;
				for (j = 0; j < bufSize; j = j + 2, k = k + 4) {
					buf[j] = data[k];
					buf[j + 1] = data[k + 1];
				}
			} else if (channel == 3 && len > 3) {
				bufSize = len / 2;
				buf = new byte[bufSize];
				int j = 0;
				int k = 2;
				for (j = 0; j < bufSize; j = j + 2, k = k + 4) {
					buf[j] = data[k];
					buf[j + 1] = data[k + 1];
				}
			} else {
				return;
			}
			Iterator<Recorder> it = recCoderSet.iterator();
			while (it.hasNext()) {
				Recorder recorder = it.next();
				recorder.write(buf, bufSize);
			}
		}
	}
	
   
   private TXZAudioRecorder mAudioRecorder = null;
	private final int BUFFER_SIZE = 1200;
	private byte[] data_buffer = new byte[BUFFER_SIZE];
	private boolean bRecording = false;
	private Runnable recordingTask = new Runnable(){
		@Override
		public void run() {
			recording();
		}
	};
	
	//lock this method to avoid mutil thread to join in
	private synchronized void recording(){
		//set follow two variable  true here, then ensure next thread join after last thread exits completely
		bRecording = true;
		mAudioRecorder = new TXZAudioRecorder(false);
		mAudioRecorder.startRecording();
		//loop read data
		while(bRecording){
			if (mAudioRecorder != null){
				int read = mAudioRecorder.read(data_buffer, 0, data_buffer.length);
				if (read > 0){
					distribute(data_buffer, read, 1);
				}
			}else{
				break;
			}
		}
		bRecording = false;
		mAudioRecorder.stop();
		mAudioRecorder.release();
		mAudioRecorder = null;
	}
	
	private void  start(){
		if (bRecording){
			return;
		}
		new Thread(recordingTask, "wx_recording").start();
	}
	
	private void stop() {
		bRecording = false;
	}

}
