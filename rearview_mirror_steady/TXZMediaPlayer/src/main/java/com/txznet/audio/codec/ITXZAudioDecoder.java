package com.txznet.audio.codec;

public interface ITXZAudioDecoder {

    /**
     * 创建解码器
     *
     * @param obj 回调监听，obj实现ITXZDecoderCallBack
     * @return sessionId
     * @see ITXZDecoderCallBack
     */
    long createDecoder(Object obj);

    /**
     * 销毁解码器
     */
    int destroyDecoder(long sessionId);

    /**
     * 开始解码
     */
    int startDecoder(long sessionId, String path);

    /**
     * 结束解码
     */
    int stopDecoder(long sessionId);

    int readDecoder(long sessionId, int[] params, byte[] data, int offset);

    /**
     *
     * @param sessionId
     * @param seekTime
     * @param seekPosition 文件总长/ 音频总时长*跳转的时间点（s）
     * @return
     */
    int seekDecoder(long sessionId, long seekTime, long seekPosition);

    /**
     *
     * @param duration
     */
    void onGetDuration(long duration);

    interface ITXZDecoderCallBack {

        /**
         * 当前时长
         *
         * @param duration 不一定准确（有可能解码不到duration字段，考拉aac）
         */
        void onGetDuration(long duration);


        /**
         * 拖动完成
         */
        void onSeekCompleteListener(long seekTime);

    }

}
