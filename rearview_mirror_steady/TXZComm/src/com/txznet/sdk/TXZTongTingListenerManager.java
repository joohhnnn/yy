package com.txznet.sdk;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.sdk.tongting.IConstantCmd;
import com.txznet.sdk.tongting.IConstantData;
import com.txznet.sdk.tongting.TongTingAudio;
import com.txznet.sdk.tongting.TongTingPlayItem;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;


/**
 * 同听监听器管理
 */
public class TXZTongTingListenerManager implements IConstantCmd, IConstantData {


    //##创建一个单例类##
    private volatile static TXZTongTingListenerManager singleton;

    private TXZTongTingListenerManager() {
        registerPlayStatsListener();
    }

    public static TXZTongTingListenerManager getInstance() {
        if (singleton == null) {
            synchronized (TXZTongTingListenerManager.class) {
                if (singleton == null) {
                    singleton = new TXZTongTingListenerManager();
                }
            }
        }
        return singleton;
    }

    private List<INotifyInfoListener> mStatusListener = new ArrayList<INotifyInfoListener>();
    ////////////////////状态回调

    public interface INotifyInfoListener {
        void notifyPlaylist(List<TongTingAudio> audios, boolean isAdded);//播放列表


        /**
         * @param state
         * @see com.txznet.sdk.tongting.ITongTingPlayState
         */
        void notifyState(int state); //播放状态

        /**
         *
         * @param favourState
         * @see com.txznet.sdk.tongting.TongTingUtils
         */
        void notifyFavour(int favourState); //收藏状态

        void notifyPlayInfo(TongTingPlayItem audio); //  播放信息

    }


    public void addStatusListener(INotifyInfoListener listener) {
        mStatusListener.add(listener);
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, SEND_CMD_ADDLISTENER, null, null);
    }

    private void notifyPlaylist(List<TongTingAudio> audios, boolean isAdded) {
        for (INotifyInfoListener iNotifyInfoListener : mStatusListener) {
            iNotifyInfoListener.notifyPlaylist(audios, isAdded);
        }
    }

    private void notifyState(int state) {
        for (INotifyInfoListener iNotifyInfoListener : mStatusListener) {
            iNotifyInfoListener.notifyState(state);
        }
    }//; //播放状态

    private void notifyFavour(int favourState) {
        for (INotifyInfoListener iNotifyInfoListener : mStatusListener) {
            iNotifyInfoListener.notifyFavour(favourState);
        }
    }//; //收藏状态

    private void notifyPlayInfo(TongTingPlayItem playItem) {
        for (INotifyInfoListener iNotifyInfoListener : mStatusListener) {
            iNotifyInfoListener.notifyPlayInfo(playItem);
        }
    }//; //  播放信息


    private void registerPlayStatsListener() {
        TXZService.setCommandProcessor(REC_CALLBACK_PERFIX, new TXZService.CommandProcessor() {

            @Override
            public byte[] process(String packageName, String command, byte[] data) {
                JSONBuilder jsonBuilder = new JSONBuilder(data);
                if (CALLBACK_ONPLAYINFOUPDATED.equals(command)) {
                    String title = jsonBuilder.getVal(KEY_TITLE, String.class, "");
                    long id = jsonBuilder.getVal(KEY_ID, Long.class, 0L);
                    int sid = jsonBuilder.getVal(KEY_SID, Integer.class, 0);
                    String artistst = jsonBuilder.getVal(KEY_ARTISTS, String.class, "");
                    String albumName = jsonBuilder.getVal(KEY_ALBUMNAME, String.class, "");
                    String logo = jsonBuilder.getVal(KEY_LOGO, String.class, "");
                    String sourceFrom = jsonBuilder.getVal(KEY_SOURCE_FROM, String.class, "");
                    int flag = jsonBuilder.getVal(KEY_FLAG, Integer.class, 0);
                    int playState = jsonBuilder.getVal(KEY_STATE, Integer.class, 0);

                    TongTingPlayItem playItem = new TongTingPlayItem(sid, id, title, logo, sourceFrom, artistst, albumName, flag, playState);
                    notifyPlayInfo(playItem);

                } else if (CALLBACK_ONPROGRESSUPDATED.equals(command)) {
                    jsonBuilder.getVal(KEY_PROGRESS, Integer.class, 0);
                    jsonBuilder.getVal(KEY_DURATION, Integer.class, 0);
                } else if (CALLBACK_ONPLAYERMODEUPDATED.equals(command)) {
                    jsonBuilder.getVal(KEY_AUDIO_MODE, Integer.class, 0);
                } else if (CALLBACK_ONPLAYERSTATUSUPDATED.equals(command)) {
                    int state = jsonBuilder.getVal(KEY_STATE, Integer.class, 0);
                    notifyState(state);
                } else if (CALLBACK_ONBUFFERPROGRESSUPDATED.equals(command)) {

                } else if (CALLBACK_ONFAVOURSTATUSUPDATED.equals(command)) {
                    int favourState = jsonBuilder.getVal(KEY_FAVOUR, Integer.class, 0);
                    notifyFavour(favourState);
                } else if (CALLBACK_ONPLAYLISTCHANGED.equals(command)) {

                    JSONArray val = jsonBuilder.getVal(KEY_DATA, JSONArray.class);

                    if (val != null) {
                        try {
                            List<TongTingAudio> audios = TongTingAudio.createAudios(val);
                            notifyPlaylist(audios, false);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }

                return new byte[0];
            }
        });

    }

}
