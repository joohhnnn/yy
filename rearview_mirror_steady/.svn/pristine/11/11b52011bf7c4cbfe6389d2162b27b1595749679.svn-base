package com.txznet.txz.component.nav;

/**
 * 路况查询接口
 */
public interface INavInquiryRoadTraffic {

    /**
     * 路况查询结果监听器
     */
    public interface OnInquiryRoadTrafficResultListener {

        /**
         * 路况查询结果
         *
         * @param result  1 成功 2 网络异常 3 没有路况信息 4 所在城市路况未开通 5 失败
         * @param message 路况信息
         */
        void onInquiryRoadTrafficResult(int result, String message);

        /**
         * 前方路况查询结果
         *
         * @param info 前方路况信息
         */
        void onInquiryRoadTrafficByFrontResult(String info);
    }

    /**
     * 查询前方路况
     */
    boolean inquiryRoadTrafficByFront();

    /**
     * 依据周边查询路况
     */
    boolean inquiryRoadTrafficByNearby(String city, String keywords);

    /**
     * 依据Poi查询路况
     */
    boolean inquiryRoadTrafficByPoi(String city, String keywords);

    /**
     * 是否支持路况查询
     */
    boolean isInquiryRoadTrafficSupported();

    /**
     * 注册监听器
     */
    void registerInquiryRoadTrafficResultListener(OnInquiryRoadTrafficResultListener listener);

    /**
     * 反注册监听器
     */
    void unregisterInquiryRoadTrafficResultListener(OnInquiryRoadTrafficResultListener listener);
}
