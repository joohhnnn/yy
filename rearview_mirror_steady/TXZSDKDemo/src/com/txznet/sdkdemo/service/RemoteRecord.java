package com.txznet.sdkdemo.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.txznet.txz.extaudiorecord.IAudioCallback;
import com.txznet.txz.extaudiorecord.ITXZAudioRecord;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Author: link
 * Create: 2019-2019/11/6-15:37
 * Changes (from 2019/11/6)
 * 2019/11/6 : Create RemoteRecord.java (link);
 **/
public class RemoteRecord extends Service {


    private IAudioCallback callBack;
    private AudioRecord audioRecord;

    private int inpuSource = MediaRecorder.AudioSource.DEFAULT;
    private int sampleRate = 16000;
    private int pcm16bit = AudioFormat.ENCODING_PCM_16BIT;

    private FileOutputStream fileOutputStream = null;

    private String TAG = "RemoteRecord";
    private ITXZAudioRecord.Stub itxzAudioRecord = new ITXZAudioRecord.Stub() {
        @Override
        public void open(){
            Log.d(TAG, "open: ");
            audioRecord.startRecording();
            try {
                fileOutputStream = new FileOutputStream("/sdcard/record_data/2chanel16k.pcm");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void close() throws RemoteException {
            audioRecord.stop();
            audioRecord.release();
        }

        @Override
        public void registerCallback(IAudioCallback callBack) throws RemoteException {
            Log.d(TAG, "registerCallback: callBack "+ String.valueOf(callBack==null));
            RemoteRecord.this.callBack = callBack;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    byte []audioBuffer = new byte[4096];
                    int size = 0;
                    int flag = 0;
                    while ((size = audioRecord.read(audioBuffer,size,4096-size))>0){
                        if (size < 4096){
                            if (flag > 5){
                                flag++;
                                return;
                            }
                            continue;
                        }
                        try {

                            byte data[] = new byte[2048];
                            for (int i =0;i<2048;i = i+4){
                                System.arraycopy(audioBuffer,i*2,data,i,2);
                                System.arraycopy(audioBuffer,i*2+4,data,i+2,2);
                            }
                            fileOutputStream.write(data);
                            fileOutputStream.flush();
                            if (RemoteRecord.this.callBack != null){
                                RemoteRecord.this.callBack.onCallBack(data);
                            }
//                            Log.d(TAG, "run: size = "+size);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        flag = 0;
                        size = 0;
                    }
                    try {
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        @Override
        public void unregisterCallback(IAudioCallback callBack) throws RemoteException {

        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        int CHANNELS = 0x8000000f;
        audioRecord = new AudioRecord(inpuSource, sampleRate, CHANNELS, pcm16bit,AudioRecord.getMinBufferSize(sampleRate, CHANNELS,pcm16bit));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return itxzAudioRecord;
    }
}
