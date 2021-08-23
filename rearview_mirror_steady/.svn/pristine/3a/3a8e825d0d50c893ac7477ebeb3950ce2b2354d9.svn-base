package com.txznet.sdk;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.sdk.tongting.IConstantCmd;
import com.txznet.sdk.tongting.IConstantData;
import com.txznet.sdk.tongting.TongTingAlbum;
import com.txznet.sdk.tongting.ITongTingDataError;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TXZTongTingDataManager implements IConstantCmd, IConstantData {

    private volatile static TXZTongTingDataManager singleton;

    //创建单例
    private TXZTongTingDataManager() {
        getAlbumListMap = new HashMap<Integer, ICallback<List<TongTingAlbum>>>();
        registerCommand();
    }

    public static TXZTongTingDataManager getInstance() {
        if (singleton == null) {
            synchronized (TXZTongTingDataManager.class) {
                if (singleton == null) {
                    singleton = new TXZTongTingDataManager();
                }
            }
        }
        return singleton;
    }


    public static interface ICallback<T> {
        void onSuccess(T data);

        void onError(int code);
    }

    ////////////////////////////AlbumList 的回调
    Map<Integer, ICallback<List<TongTingAlbum>>> getAlbumListMap = null;

    private void notifyAlbumList(List<TongTingAlbum> listData, int sequenceId, int errorCode) {
        if (getAlbumListListener(sequenceId) != null) {
            if (errorCode == 0) {
                getAlbumListListener(sequenceId).onSuccess(listData);
            } else {
                getAlbumListListener(sequenceId).onError(errorCode);
            }
        }
    }

    private void setAlbumListListener(int sequenceId, ICallback<List<TongTingAlbum>> callback) {
        getAlbumListMap.put(sequenceId, callback);
    }

    private ICallback<List<TongTingAlbum>> getAlbumListListener(int sequenceId) {
        return getAlbumListMap.get(sequenceId);
    }
    //////////////////////////////////////

    private static int sequenceId = 0;

    private synchronized int getSequence() {
        if (++sequenceId >= Integer.MAX_VALUE) {
            sequenceId = 0;
        }
        return sequenceId;
    }


    /**
     * 获取推荐的专辑列表
     *
     * @param albumId   上下拉取数据的时候需要带的临近的一个参数（向上滑动，则需带上列表中最后一个数据的专辑Id，向下滑动，反之）
     * @param limit     一次获取的个数
     * @param direction 上拉（0），还是下拉（1）
     * @param callback
     */
    public void getRecommendList(long albumId, int limit, int direction, final ICallback<List<TongTingAlbum>> callback) {
        int sequence = getSequence();

        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put(KEY_ALBUM_ID, albumId);
        jsonBuilder.put(KEY_LIMIT, limit);
        jsonBuilder.put(KEY_DIRECTION, direction);
        jsonBuilder.put(KEY_SEQUENCE, sequence);
        jsonBuilder.put(KEY_TYPE, PARAM_TYPE_RECOMMAND);

        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, SEND_CMD_GETRECOMMENDALBUM, jsonBuilder.toBytes(), null);

        setAlbumListListener(sequence, callback);
    }

    /**
     * 获取收藏的专辑列表
     *
     * @param albumId  上下拉取数据的时候需要带的临近的一个参数（向上滑动，则需带上列表中最后一个数据的专辑Id，向下滑动，反之）
     * @param limit    一次获取的个数
     * @param up       向上滑动（0），还是下拉（1）
     * @param callback
     */
    public void getSubscribeList(long albumId, int limit, int up, final ICallback<List<TongTingAlbum>> callback) {
        int sequence = getSequence();

        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put(KEY_ALBUM_ID, albumId);
        jsonBuilder.put(KEY_LIMIT, limit);
        jsonBuilder.put(KEY_DIRECTION, up);
        jsonBuilder.put(KEY_SEQUENCE, sequence);
        jsonBuilder.put(KEY_TYPE, PARAM_TYPE_SUBSCRIBE);

        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, SEND_CMD_GETRECOMMENDALBUM, jsonBuilder.toBytes(), null);

        setAlbumListListener(sequence, callback);
    }


    /**
     * 播放指定的音频（必须处于播放列表中）
     *
     * @param id
     * @param sid
     */
    public void playAudio(long id, int sid) {
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put(KEY_ID, id);
        jsonBuilder.put(KEY_SID, sid);
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, SEND_CMD_PLAY_AUDIO, jsonBuilder.toBytes(), null);
    }

    /**
     * 播放指定的专辑
     *
     * @param albumId    专辑ID
     * @param albumSid   专辑的来源id
     * @param categoryId 专辑的分类Id
     */
    public void playAlbum(long albumId, int albumSid, long categoryId) {
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put(KEY_ID, albumId);
        jsonBuilder.put(KEY_SID, albumSid);
        jsonBuilder.put(KEY_CATEGORYID, categoryId);
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, SEND_CMD_PLAY_ALBUM, jsonBuilder.toBytes(), null);
    }


    private void registerCommand() {
        TXZService.setCommandProcessor(REC_CMD_PERFIX, new TXZService.CommandProcessor() {
            @Override
            public byte[] process(String packageName, String command, byte[] data) {
                JSONBuilder jsonBuilder = new JSONBuilder(data);
                if (REC_CMD_GETRECOMMENDALBUM.equals(command)) {
                    int sequenceId = jsonBuilder.getVal(KEY_SEQUENCE, Integer.class, 0);
                    List<TongTingAlbum> list = null;
                    int errorCode = jsonBuilder.getVal(KEY_ERRORCODE, Integer.class, 0);
                    if (errorCode == 0) {
                        //没有发生错误
                        JSONArray jsonArray = jsonBuilder.getVal(KEY_DATA, JSONArray.class, new JSONArray());
                        try {
                            list = TongTingAlbum.createAlbums(jsonArray);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            errorCode = ITongTingDataError.CODE_JSON_ERROR;
                        }
                    }
                    notifyAlbumList(list, sequenceId, errorCode);
                }
                return new byte[0];
            }
        });
    }


}
