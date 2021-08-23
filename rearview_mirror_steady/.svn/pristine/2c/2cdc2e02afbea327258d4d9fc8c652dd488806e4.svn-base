package com.txznet.launcher.domain.notification.data;

/**
 * 今日贴士要用到的数据类，定义了数据的加载方法等。业务类主需要调用这些方法就可以管理数据了。
 * 这里使用接口代替了继承方式。
 */
public interface INoticeData {

    int DATA_TYPE_WEATHER = 0x0;

    /**
     * 预加载
     */
    void prepare();

    /**
     * 是否加载完毕
     */
    boolean isLoaded();

    /**
     * 数据类型
     */
    int getType();

    /**
     * 获取数据
     */
    String getData();

    /**
     * 是否依赖于同行者初始化
     */
    boolean isDependOnTXZ();

    /**
     * 清空数据
     */
    void release();
}
