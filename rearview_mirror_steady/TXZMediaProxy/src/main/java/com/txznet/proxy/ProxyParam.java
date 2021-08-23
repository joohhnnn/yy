package com.txznet.proxy;

import com.txznet.proxy.cache.LocalBuffer;

import java.io.File;
import java.util.List;

public class ProxyParam {
    public String tag;
    public byte[] info;  // 资源描述数据，序列化到tmd文件中
    public String cacheId; // 缓存id，影响缓存文件命名
    public File cacheDir; // 缓存路径
    public File finalFile; // 最终文件存放路径
    public IProxyCallback callback; // 回调
    public boolean needMoreData;// 是否需要请求下一片内容，否则受代理框架策略影响
    public boolean needMoreWriteData; // 是否需要写入下一片内容，否则受代理框架策略影响

    public interface IProxyCallback {

        /**
         * 代理服务出错
         *
         * @param errorCode 错误码
         * @param desc      错误描述
         * @param hint      错误提示
         */
        void onError(int errorCode, String desc, String hint);

        /**
         * 缓冲进度更新
         *
         * @param buffers 缓冲
         */
        void onBufferingUpdate(List<LocalBuffer> buffers);

        /**
         * 资源下载完毕
         */
        void onDownloadComplete();

        /**
         * 当前播放的进度？百分比
         */
        float getPlayPercent();

        /**
         * 当前音频总时长
         */
        long getDuration();

        /**
         * 是否处于播放中
         */
        boolean isPlaying();
    }
}
