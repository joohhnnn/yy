package com.txznet.music.data.http.api.txz.entity.req;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.music.config.Configuration;
import com.txznet.txz.util.TXZFileConfigUtil;

import java.util.ArrayList;
import java.util.List;

public class TXZReqPlayConf extends TXZReqBase {
    public long logoTag; // tag, 客户端tag
    public List<Integer> arrApp = new ArrayList<>(); // 安装的音乐源列表,默认所有的。
    public int version; // 版本
    public int width;//当前屏幕的宽
    public int height;//当前屏幕的高

    public TXZReqPlayConf() {
        WindowManager wm = (WindowManager) GlobalContext.get().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;
        version = TXZFileConfigUtil.getIntSingleConfig(Configuration.Key.TXZ_SEARCH_VERSION, Configuration.DefVal.SEARCH_VERSION);
//        if (QQMusicUtil.checkQQMusicInstalled()) {
//            arrApp.add(1);
//        }
    }
}
