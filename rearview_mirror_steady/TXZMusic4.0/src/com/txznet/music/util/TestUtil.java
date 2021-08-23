package com.txznet.music.util;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.CollectionUtils;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.Constant;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by telenewbie on 2017/8/18.
 */

public class TestUtil {

    private static final String TAG = "music:test:map:";

    public static <T, V> void printMap(String tag, Map<T, V> values) {
        if (Constant.ISTEST) {
            LogUtil.logd(Constant.OOM_TAG + TAG + tag + ":map:size:" + values.size());
            if (values.size() > 0) {
                Iterator<T> iterator = values.keySet().iterator();
                while (iterator.hasNext()) {
                    T next = iterator.next();
                    LogUtil.logd(Constant.OOM_TAG + TAG + tag + ":map:key=" + next + ",map:value=" + values.get(next));
                }
            }
        }

    }

    public static <T> void printList(String tag, List<T> values) {
        if (Constant.ISTEST) {
            StringBuilder sb = new StringBuilder();
            if (CollectionUtils.isNotEmpty(values)) {
                for (int i = 0; i < values.size(); i++) {
                    T value = values.get(i);
                    sb.append("第" + i + "项:");
                    if (value != null) {
                        sb.append(value.toString());
                    } else {
                        sb.append("没有数据");
                    }
                    LogUtil.logd(tag + "" + sb.toString());
                    sb.delete(0, sb.length());
                }
            } else {
                sb.append("数组为空");
                LogUtil.logd(tag + "" + sb.toString());
            }
        }
    }
}
