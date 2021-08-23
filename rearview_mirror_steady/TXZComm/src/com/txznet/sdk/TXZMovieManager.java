package com.txznet.sdk;


import android.text.TextUtils;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;


/**
 * 电影场景管理
 */

public class TXZMovieManager {

    private static TXZMovieManager sInstance = new TXZMovieManager();

    private TXZMovieManager() {
    }

    public static TXZMovieManager getInstance() {
        return sInstance;
    }

    private MovieTool mMoiveTool;
    private int mOutTime;
    private boolean enable;
    private boolean isEnable = false;

    public void setIsEnable(boolean isEnable){
        this.isEnable = isEnable;
    }

    /**
     * 重连时需要重新通知同行者的操作放这里。
     */
    void onReconnectTXZ() {
        if(mMoiveTool != null){
            setMovieSearchTool(mMoiveTool,mOutTime);
        }
        if(isEnable){
            enableTheFilmSence(enable);
        }
    }

    /**
     * 搜索电影资源，初始化成功后调用
     *
     * @param movieTool
     *            电影工具，实现searchMovie接口进行电影资源替换操作。
     *
     * @param outTime 超时时长设置，单位：ms。所替换的电影资源必须在该时间段内完成替换。
     */
    public void setMovieSearchTool(MovieTool movieTool, int outTime){
        mMoiveTool = movieTool;
        mOutTime = outTime;
        TXZService.setCommandProcessor("tool.movie.", new TXZService.CommandProcessor() {
            @Override
            public byte[] process(String packageName, String command, byte[] data) {

                if ("search".equals(command)) {
                    LogUtil.logd("movie tool search");
                    if (data == null) {
                        return null;
                    }
                    String json = new String(data);
                    String newMovieJson = mMoiveTool.searchMovie(json);
                    if(TextUtils.isEmpty(newMovieJson)){
                        return null;
                    }
                    return newMovieJson.getBytes();
                }
                else if("onCancel".equals(command)){
                    mMoiveTool.onCancel();
                }
            return null;
            }
        });
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,"txz.tool.movie.setTool", String.valueOf(outTime).getBytes(), null);
    }

    /**
     * 在皮肤包中调用，将当前的皮肤包主题通知到core
     *
     * @param ThemeStyle
     *            当前皮肤包的风格。
     *            1 表示全屏
     *            2 表示半屏
     *            3 表示无屏
     *
     */
    public void sendCurrentThemeStyle(int ThemeStyle){

        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,"txz.themeStyle.movie.getTheme", String.valueOf(ThemeStyle).getBytes(), null);
    }

    /*
    *使用皮肤包UI1.0时，该接口才会生效
    *
    * 使用UI1.0时，需要将该接口设置为true，电影票场景才能使用
    *
    * */
    public void  enableTheFilmSence(Boolean enable){
        isEnable = true;
        this.enable = enable;
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,"txz.control.film.enable", String.valueOf(this.enable).getBytes(), null);
    }

    public interface MovieTool {

        /**
         * 搜索电影资源
         *
         * @param json
         *            原有电影资源
         *
         * @return 替换后的电影资源，需要按照规定的json格式返回。
         */
        public String searchMovie(String json);

        /*
         * core内部清除本次电影搜索动作，如关闭声控、返回声控等
         * */
        public void onCancel();
    }


}
