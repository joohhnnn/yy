package com.txznet.comm.ui.theme.test.utils;

import android.view.View;
import android.view.ViewGroup;

import com.txznet.comm.ui.viewfactory.ViewFactory;

/**
 * 说明：务必给view设置tag为ExtViewAdapter
 *
 * @author xiaolin
 * create at 2020-08-24 11:44
 */
public class ExtViewAdapter extends ViewFactory.ViewAdapter {

    public enum SIZE_TYPE {
        WRAP_CONTENT,
        MATCH_PARENT
    }

    /**
     * 第二个卡片
     */
    public View extView;

    /**
     * 卡片内容高度
     * <p>
     * MATCH_PARENT：占最高
     * 1. 帮助二级列表
     * 2. 帮助，屏示图片
     * 3. 反馈结束后显示的二维码界面
     * 4. 话费充值
     * 5. 话费订单支付
     * 6. 绑定设备
     *
     * <p>
     * WRAP_CONTENT：随内容适应
     * 列表使用自适应
     */
    public SIZE_TYPE cardHeightType = SIZE_TYPE.WRAP_CONTENT;

    /**
     * 是否显示录音图标
     * <p>
     * 只有小部分界面不显示：
     * 1. 齐悟火车票/飞机票乘客信息
     * 2. 齐悟火车票/飞机票订单
     */
    public boolean showRecordView = true;

    /**
     * 事件回调
     */
    public Callback callback;

    public interface Callback{
        /**
         * 显示界面
         */
        void show();

        /**
         * 界面关闭、移除、隐藏
         */
        void dismiss();
    }

}
