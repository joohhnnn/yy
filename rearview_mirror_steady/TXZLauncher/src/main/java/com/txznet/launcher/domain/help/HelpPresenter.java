package com.txznet.launcher.domain.help;

import com.txznet.launcher.R;
import com.txznet.launcher.domain.help.bean.HelpCommand;
import com.txznet.launcher.module.help.IHelpView;
import com.txznet.loader.AppLogicDefault;

/**
 * Created by daviddai on 2018/8/27
 */
public class HelpPresenter implements IHelpPresenter {

    /**
     * 展示的数据，数据都是写死的
     */
    private static final HelpCommand[] datas = new HelpCommand[]{
            new HelpCommand("导航", "导航到东方明珠、我要回家／去公司", R.drawable.icon_navigation_type),
            new HelpCommand("生活周边", "附近的停车场、我想吃肯德基", R.drawable.icon_surrounding_type),
            new HelpCommand("音乐&电台", "我要听音乐、播放郭德纲的相声", R.drawable.icon_music_type),
            new HelpCommand("微信助手", "打开微信、发微信给XXX、退出微信", R.drawable.icon_wechat_type),
            new HelpCommand("安吉星客服", "联系客服、联系紧急客服、拨打客服电话", R.drawable.icon_anjixing_customer_support_type),
            new HelpCommand("安吉星账号", "切换安吉星账号、退出登录", R.drawable.icon_anjixing_accout_type),
            new HelpCommand("天气", "明天天气怎么样、今天适合洗车吗", R.drawable.icon_weather_type),
            new HelpCommand("股票", "上汽集团的股票、上证指数", R.drawable.icon_stock_type),
            new HelpCommand("百科", "刘德华是谁、世界第一高的山峰", R.drawable.icon_wikipedia_type),
            new HelpCommand("笑话", "讲个笑话", R.drawable.icon_joke_type),
            new HelpCommand("WIFI热点", "打开WIFI热点、查看WIFI热点密码", R.drawable.icon_wifi_type),
            new HelpCommand("FM发射", "打开FM发射、调频到100（87.5-108）", R.drawable.icon_fm_type),
            new HelpCommand("系统信息查询", "查询设备号、查询系统信息", R.drawable.icon_system_type),
            new HelpCommand("本地控制", "关闭屏幕、增大亮度、降低音量", R.drawable.icon_local_control_type),
            new HelpCommand("其他", "切换声音、返回桌面、退出", R.drawable.icon_other_type),
    };

    // 每一页展示的数量
    private static final int PAGE_COUNT = 3;

    // 第一页、最后一页、当前页
    private int firstPage = 1;
    private int lastPage = 5;
    private int currentPage = 1;

    // 用来装展示数据的数组，减少重复创建的次数
    private HelpCommand[] tempCommands = new HelpCommand[PAGE_COUNT];

    private IHelpView mHelpView;
    private Runnable showRunnable;

    public HelpPresenter(IHelpView helpView) {
        mHelpView = helpView;
        showRunnable = new Runnable() {
            @Override
            public void run() {
                showCommand(tempCommands);
            }
        };
    }

    /**
     * 支持展示当前页。就是当前是第三页，执行展示第三页。依旧会执行。
     * @param specifiedPage 指定页
     */
    @Override
    public void showSpecifiedPage(int specifiedPage) {
        if (specifiedPage < firstPage || specifiedPage > lastPage) {
            return;
        }
        // 根据页数获取数据
        int index = (specifiedPage - 1) * PAGE_COUNT;
        System.arraycopy(datas, index, tempCommands, 0, PAGE_COUNT);

        // 修改变量中的当前页
        currentPage = specifiedPage;

        // view展示
        AppLogicDefault.runOnUiGround(showRunnable);
    }

    @Override
    public void nextPage() {
        showSpecifiedPage(currentPage + 1);
    }

    @Override
    public void prePage() {
        showSpecifiedPage(currentPage - 1);
    }

    @Override
    public boolean isFirstPage() {
        return currentPage == firstPage;
    }

    @Override
    public boolean isLastPage() {
        return currentPage == lastPage;
    }

    @Override
    public int getCurrentPage() {
        return currentPage;
    }

    @Override
    public int getPageCount() {
        return lastPage;
    }

    @Override
    public void showCommand(HelpCommand[] commands) {
        mHelpView.showPage(commands, currentPage, firstPage, lastPage);
    }

    @Override
    public void attach() {
        currentPage = 1;
    }

    @Override
    public void detach() {
    }
}
