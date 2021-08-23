package com.example.week2;


import android.util.Log;

import com.example.week2.jsontool.JsonUtil;


public class MessageParser {
    String info;
    private static final String TAG = "MessageParser";
    private static MessageParser instance;

    public String getInfo() {
        return info;
    }

    private MessageParser() {
    }

    public static MessageParser getInstance() {
        if (null == instance) {
            synchronized (MessageParser.class) {
                if (null == instance) {
                    instance = new MessageParser();
                }
            }
        }
        return instance;
    }

    public byte[] processMessage(int key, String command, byte[] data) {

        switch (key) {


            case 1400:
                if ("ComReg".equals(command)) {

                    info = JsonUtil.getStringFromJson("cr", data, "传输失败");
                    Log.e("1400jieshou", info);


                }
                break;
        }
        return new byte[0];
    }
}

