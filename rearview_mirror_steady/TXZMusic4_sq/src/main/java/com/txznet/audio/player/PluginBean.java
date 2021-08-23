package com.txznet.audio.player;

/**
 * Created by ASUS User on 2016/11/29.
 */

public class PluginBean {
    String cmd;//传递的命令字
    Object obj;//传递的参数

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }
}
