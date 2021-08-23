package com.example.app3.service;

import android.util.Log;

import com.example.app3.tool.VolumeTool;
import com.example.app3.util.JsonUtil;

public class MessageProcess {
    private static final String TAG = "MessageProcess";
    private static MessageProcess instance;

    private MessageProcess() {
    }

    public static MessageProcess getInstance() {
        if (null == instance) {
            synchronized (MessageProcess.class) {
                if (null == instance) {
                    instance = new MessageProcess();
                }
            }
        }
        return instance;
    }

    public byte[] processMessage(int key, String command, byte[] data) {
        if (command.startsWith("async")) {
            Log.d(TAG, "this data is client async sent!");
            //暂时没有此需求，有异步请求客户端时在此处接收数据
            command = command.replace("async.", "");
        }
        Log.d(TAG, key + ": " + command);
        switch (key){
            case 1030:
                if ("volume.up".equals(command)){
                    VolumeTool.getInstance().incVolume();
                    return null;
                }
                if ("volume.down".equals(command)){
                    VolumeTool.getInstance().decVolume();
                    return null;
                }
                if ("volume.max".equals(command)){
                    VolumeTool.getInstance().setMaxVolume();
                    return null;
                }
                if ("volume.min".equals(command)){
                    VolumeTool.getInstance().setMinVolume();
                    return null;
                }
                break;
            case 1100:
                if ("nav.begin".equals(command)){
                    String name = JsonUtil.getStringFromJson("name", data, "");
                    String addr = JsonUtil.getStringFromJson("addr", data, "");
                    double lat = JsonUtil.getDoubleFromJson("lat", data, 0);
                    double lng = JsonUtil.getDoubleFromJson("lng", data, 0);

                    Log.d(TAG, "begin: name: " + name + ", addr: " + addr + ", lat: " + lat + ", lng: " + lng);
                }
                break;
        }
        return new byte[0];
    }

}
