package com.txznet.music.service;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.RequiresApi;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.music.baseModule.logic.ServiceEngine;
import com.txznet.music.utils.PlayerCommunicationManager;
import com.txznet.txz.service.IService;

import static com.txznet.music.utils.PlayerCommunicationManager.EXTRA_KEY_ARTISTS;
import static com.txznet.music.utils.PlayerCommunicationManager.EXTRA_KEY_DURATION;
import static com.txznet.music.utils.PlayerCommunicationManager.EXTRA_KEY_IS_SONG;
import static com.txznet.music.utils.PlayerCommunicationManager.EXTRA_KEY_LOGO;
import static com.txznet.music.utils.PlayerCommunicationManager.EXTRA_KEY_PERCENT;
import static com.txznet.music.utils.PlayerCommunicationManager.EXTRA_KEY_PROGRESS;
import static com.txznet.music.utils.PlayerCommunicationManager.EXTRA_KEY_STATUS;
import static com.txznet.music.utils.PlayerCommunicationManager.EXTRA_KEY_TITLE;

public class MyService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return new SampleBinder();
    }

    public class SampleBinder extends IService.Stub {
        @Override
        public byte[] sendInvoke(final String packageName, final String command, final byte[] data) throws RemoteException {
            if ("music.setListener".equals(command)) {
                PlayerCommunicationManager.getInstance().setListener(new PlayerCommunicationManager.ResultListener() {
                    @Override
                    public void onState(Bundle bundle) {
                        JSONBuilder jsonBuilder = new JSONBuilder();
                        jsonBuilder.put(EXTRA_KEY_STATUS, bundle.get(EXTRA_KEY_STATUS));
                        ServiceManager.getInstance().sendInvoke(packageName, "data.music.state", jsonBuilder.toBytes(), null);
                    }

                    @Override
                    public void onPlayInfo(Bundle bundle) {
                        JSONBuilder jsonBuilder = new JSONBuilder();
                        jsonBuilder.put(EXTRA_KEY_TITLE, bundle.getString(EXTRA_KEY_TITLE));
                        jsonBuilder.put(EXTRA_KEY_ARTISTS, bundle.getString(EXTRA_KEY_ARTISTS));
                        jsonBuilder.put(EXTRA_KEY_LOGO, bundle.getString(EXTRA_KEY_LOGO));
                        jsonBuilder.put(EXTRA_KEY_IS_SONG, bundle.getBoolean(EXTRA_KEY_IS_SONG, true));
                        ServiceManager.getInstance().sendInvoke(packageName, "data.music.playInfo", jsonBuilder.toBytes(), null);
                    }

                    @Override
                    public void onProgress(Bundle bundle) {
                        JSONBuilder jsonBuilder = new JSONBuilder();
                        jsonBuilder.put(EXTRA_KEY_PROGRESS, bundle.getLong(EXTRA_KEY_PROGRESS));
                        jsonBuilder.put(EXTRA_KEY_DURATION, bundle.getLong(EXTRA_KEY_DURATION));
                        jsonBuilder.put(EXTRA_KEY_PERCENT, bundle.getFloat(EXTRA_KEY_PERCENT));
                        ServiceManager.getInstance().sendInvoke(packageName, "data.music.progress", jsonBuilder.toBytes(), null);
                    }
                });
                return null;
            }
            return ServiceEngine.getInstance().sendInvoke(packageName, command, data);
//            byte[] ret = null;
//
//            if ("music.play".equals(command)) {
//                PlayEngineFactory.getEngine().play(EnumState.Operation.manual);
//            } else if ("music.pause".equals(command)) {
//                PlayEngineFactory.getEngine().pause(EnumState.Operation.manual);
//            } else if ("music.next".equals(command)) {
//                PlayEngineFactory.getEngine().next(EnumState.Operation.manual);
//            } else if ("music.prev".equals(command)) {
//                PlayEngineFactory.getEngine().last(EnumState.Operation.manual);
//            } else if ("music.isPlaying".equals(command)) {
//                ret = String.valueOf(PlayEngineFactory.getEngine().isPlaying()).getBytes();
//            } else if ("music.setListener".equals(command)) {
//                PlayerCommunicationManager.getInstance().setListener(new PlayerCommunicationManager.ResultListener() {
//                    @Override
//                    public void onState(Bundle bundle) {
//                        JSONBuilder jsonBuilder = new JSONBuilder();
//                        jsonBuilder.put(EXTRA_KEY_STATUS, bundle.get(EXTRA_KEY_STATUS));
//                        ServiceManager.getInstance().sendInvoke(packageName, "data.music.state", jsonBuilder.toBytes(), null);
//                    }
//
//                    @Override
//                    public void onPlayInfo(Bundle bundle) {
//                        JSONBuilder jsonBuilder = new JSONBuilder();
//                        jsonBuilder.put(EXTRA_KEY_TITLE, bundle.getString(EXTRA_KEY_TITLE));
//                        jsonBuilder.put(EXTRA_KEY_ARTISTS, bundle.getString(EXTRA_KEY_ARTISTS));
//                        jsonBuilder.put(EXTRA_KEY_LOGO, bundle.getString(EXTRA_KEY_LOGO));
//                        ServiceManager.getInstance().sendInvoke(packageName, "data.music.playInfo", jsonBuilder.toBytes(), null);
//                    }
//
//                    @Override
//                    public void onProgress(Bundle bundle) {
//                        JSONBuilder jsonBuilder = new JSONBuilder();
//                        jsonBuilder.put(EXTRA_KEY_PROGRESS, bundle.getLong(EXTRA_KEY_PROGRESS));
//                        jsonBuilder.put(EXTRA_KEY_DURATION, bundle.getLong(EXTRA_KEY_DURATION));
//                        jsonBuilder.put(EXTRA_KEY_PERCENT, bundle.getFloat(EXTRA_KEY_PERCENT));
//                        ServiceManager.getInstance().sendInvoke(packageName, "data.music.progress", jsonBuilder.toBytes(), null);
//                    }
//                });
//            } else if (command.startsWith("music.")) {
//                ret = ThirdHelper.getInstance().invokeMusic(packageName, command.substring("music.".length()), data);
//            }
//
//
//            return ret;
        }

    }

    private byte[] invokeMusic(final String packageName, String substring, byte[] data) {
        return ThirdHelper.getInstance().invokeMusic(packageName, substring, data);
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onCreate() {
        super.onCreate();
    }


}
