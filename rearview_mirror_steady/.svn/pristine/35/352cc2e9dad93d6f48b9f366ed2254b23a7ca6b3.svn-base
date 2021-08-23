package com.txznet.sdk;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.util.JSONBuilder;

import static com.txznet.sdk.music.MusicInvokeConstants.*;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;


/**
 * 同听推送相关的逻辑
 */
public class TXZTongTingPushManager {


    //##创建一个单例类##
    private volatile static TXZTongTingPushManager singleton;

    private TXZTongTingPushManager() {
    }

    public static TXZTongTingPushManager getInstance() {
        if (singleton == null) {
            synchronized (TXZTongTingPushManager.class) {
                if (singleton == null) {
                    singleton = new TXZTongTingPushManager();
                }
            }
        }
        return singleton;
    }

    ///
    private PushStatusListener mPushStatusListener;
    private PushInfoListener mPushInfoListener;

    /**
     * 推送的状态回掉监听
     */
    public interface PushStatusListener {

        /**
         * 状态改变的回掉
         *
         * @param chage
         */
        void onStatusChange(int chage);


        /**
         * 播放进度该表的回掉
         *
         * @param progress 当前播放的进度（单位s）
         * @param duration 总长
         */
        void onProgressChange(long progress, long duration);


        /**
         * 播放信息改变
         *
         * @param datas 播放的信息
         */
        void onInfoChange(String datas);


    }

    public interface PushInfoListener {

        /**
         * 接下来播放的音频列表，最大转递10个音频
         *
         * @param audios
         */
        void onNextAudios(List<TXZMusicManager.MusicModel> audios);

    }

//    public interface PushOperationListener {
//
//        /**
//         * 点击收听
//         */
//        void onClickListener();
//
//        /**
//         * 点击取消
//         */
//        void onClickCancle();
//    }


    /**
     * 推送工具
     */
    public abstract static class PushTool {

        /**
         * 拦截全部
         */
//        public final static int INTERCEPT_ALL = 0;
        /**
         * 新闻推送
         */
        public final static int INTERCEPT_NEWS = 1;
        /**
         * 专辑更新提醒
         */
        public final static int INTERCEPT_UPDATE = 1 << 1;
        /**
         * 微信推动
         */
        public final static int INTERCEPT_AUDIO = 1 << 2;

        /**
         * 设置拦截类型
         *
         * @return INTERCEPT_NEWS  &  INTERCEPT_UPDATE  &  INTERCEPT_AUDIO 表示拦截全部
         */
        public abstract int getInterceptType();

        /**
         * 表明是否拦截，显示界面的逻辑，true表示拦截界面的显示
         *
         * @return true表示拦截
         */
        public boolean showView() {
            //展示视图

            return false;
        }

        public void dismissView() {
            //用户可以选择不用关系同听调过来的该方法，可以自己取把控何时取消
        }

        /**
         * 将数据返回，用户自行进行判断是否需要拦截，一旦返回true，则之后的所有逻辑都不会走到，false则表明不会拦截处理
         *
         * @return
         */
        public boolean showData(TXZMusicManager.MusicModel data) {

            return false;
        }

        public boolean getNeedWait() {
            return false;
        }
    }

    public void setPushStatusListener(PushStatusListener pushStatusListener) {
        this.mPushStatusListener = pushStatusListener;

        TXZService.setCommandProcessor(INVOKE_PREFIX_TONGTING_PUSH_CALLBACK_STATUS, new TXZService.CommandProcessor() {
            @Override
            public byte[] process(String packageName, String command, byte[] data) {
                JSONBuilder jsonBuilder = new JSONBuilder(data);
                if (PUSH_INFO.equals(command)) {
                    mPushStatusListener.onInfoChange(jsonBuilder.getVal(KEY_INFO, String.class));
                    return null;
                }
                if (PUSH_STATUS.equals(command)) {
                    mPushStatusListener.onStatusChange(jsonBuilder.getVal(KEY_STATUS, int.class, 0));
                    return null;
                }
                if (PUSH_PROGRESS.equals(command)) {
//                    long a = jsonBuilder.getVal(KEY_PROGRESS, Long.class, 0L);
//                    long b = jsonBuilder.getVal(KEY_DURATION, Long.class, 0L);
                    mPushStatusListener.onProgressChange(jsonBuilder.getVal(KEY_PROGRESS, Long.class, 0L), jsonBuilder.getVal(KEY_DURATION, Long.class, 0L));
                    return null;
                }
                return null;
            }

        });
    }

    public void setPushInfoListener(PushInfoListener pushInfoListener) {
        this.mPushInfoListener = pushInfoListener;

        TXZService.setCommandProcessor(INVOKE_PREFIX_TONGTING_PUSH_NEXT_AUDIOS, new TXZService.CommandProcessor() {
            @Override
            public byte[] process(String packageName, String command, byte[] data) {
                JSONBuilder jsonBuilder = new JSONBuilder(data);
                if (PUSH_NEXT_AUDIOS.equals(command)) {
                    JSONArray val = jsonBuilder.getVal(KEY_NEXT_AUDIOS, JSONArray.class);
                    ArrayList<TXZMusicManager.MusicModel> musicModels = new ArrayList<TXZMusicManager.MusicModel>();

                    for (int i = 0; i < val.length(); i++) {
                        try {
                            JSONBuilder jsonData = new JSONBuilder(val.getString(i));
                            TXZMusicManager.MusicModel model = new TXZMusicManager.MusicModel();
                            model.title = jsonData.getVal(KEY_TITLE, String.class);
                            musicModels.add(model);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    mPushInfoListener.onNextAudios(musicModels);
                    return null;
                }
                return null;
            }

        });
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put(KEY_PUSH_NEED_MORE_AUDIOS, true);
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.tongting.push.tool.need.audios", jsonBuilder.toBytes(), null);
    }

//    public void setPushOperationListener(PushOperationListener mPushOperationListener) {
//        this.mPushOperationListener = mPushOperationListener;
//    }

    private PushTool mPushTool;

    public void setPushTool(PushTool tool) {
        mPushTool = tool;
        if (mPushTool == null) {
            clearTool();
            return;
        }
        TXZService.setCommandProcessor(INVOKE_PREFIX_TONGTING_PUSH, new TXZService.CommandProcessor() {
            @Override
            public byte[] process(String packageName, String command, byte[] data) {
                JSONBuilder jsonBuilder = new JSONBuilder(data);
                if (PUSH_SHOW_VIEW.equals(command)) {
                    return String.valueOf(mPushTool.showView()).getBytes();
                }
                if (PUSH_DISMISS_VIEW.equals(command)) {
                    mPushTool.dismissView();
                    return null;
                }
                if (PUSH_IS_NEED_WAIT.equals(command)) {
                    return (mPushTool.getNeedWait() + "").getBytes();
                }

                if (PUSH_SHOW_DATA.equals(command)) {
                    TXZMusicManager.MusicModel model = new TXZMusicManager.MusicModel();
                    model.title = jsonBuilder.getVal(KEY_TITLE, String.class);
                    model.album = jsonBuilder.getVal(KEY_ALBUM_NAME, String.class);
//                    model.artist=jsonBuilder.getVal(KEY_ARTISTS,JSONArray.class)
                    model.subCategory = jsonBuilder.getVal(KEY_SUB_TITLE, String.class);
                    model.path = jsonBuilder.getVal(KEY_PUSH_ICON, String.class);
                    boolean b = mPushTool.showData(model);
                    return String.valueOf(b).getBytes();
                }

                return new byte[0];
            }
        });
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put(KEY_PUSH_VERSION, 1);
        jsonBuilder.put(KEY_PUSH_INTERCEPT, mPushTool.getInterceptType());
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.tongting.push.tool.set", jsonBuilder.toBytes(), null);
    }

    /**
     * 恢复播报
     */
    public void resumePlay() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.tongting.push.tool.resume.play", null, null);
    }

    public void clearTool() {
        mPushTool = null;
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.tongting.push.tool.clear", null, null);
    }

//    private boolean isInstance(Object obj, Class clazz) {
//        if (obj != null && clazz != null) {
//            return obj instanceof obj;
//        }
//
//        return false;
//    }

    /**
     * 点击收听
     */
    public void onClickContinue() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.tongting.push.click.continue", null, null);
    }

    /**
     * 点击取消
     */
    public void onClickCancel() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.tongting.push.click.cancel", null, null);
    }
}
