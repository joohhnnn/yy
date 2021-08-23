package com.txznet.music.model;

import android.os.Environment;

import com.txznet.comm.err.Error;
import com.txznet.comm.util.StringUtils;
import com.txznet.music.Constant;
import com.txznet.music.ErrCode;
import com.txznet.music.action.ActionType;
import com.txznet.music.data.entity.AudioV5;
import com.txznet.music.data.source.TXZMusicDataSource;
import com.txznet.music.util.AudioUtils;
import com.txznet.rxflux.RxAction;
import com.txznet.rxflux.RxWorkflow;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * @author telen
 * @date 2018/12/13,15:06
 */
public class LyricModel extends RxWorkflow {

    public LyricModel() {
    }

    @Override
    public void onAction(RxAction action) {
        switch (action.type) {
            case ActionType.ACTION_LYRIC_GET:
                //跟后台要数据
                getLyricData(action, (AudioV5) action.data.get(Constant.LyricConstant.KEY_LYRIC_AUDIO));
                break;
            default:
                break;

        }

    }

    private void getLyricData(RxAction action, AudioV5 audioV5) {
        //如果文件存在的话,则不用请求了.
        File lrcFile = new File(Environment.getExternalStorageDirectory(), "txz/audio/lrc/" + audioV5.sid + AudioUtils.UNDERLINE + audioV5.id + ".lrc");
        if (lrcFile.exists()) {
            action.data.put(Constant.LyricConstant.KEY_LYRIC_FILE, lrcFile);
            postRxData(action);
            return;
        }


        Disposable subscribe = TXZMusicDataSource.get().getLyric(audioV5).subscribeOn(Schedulers.io()).observeOn(Schedulers.io()).flatMap(msg -> {
            if (StringUtils.isEmpty(msg)) {
                //没有歌词
                throw new Error(ErrCode.ERROR_CLIENT_NET_EMPTY_DATA);
            }
            //有歌词
            if (!lrcFile.exists()) {
                lrcFile.getParentFile().mkdirs();
                try {
                    lrcFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try (FileWriter fileWriter = new FileWriter(lrcFile)) {
                //写文件
                fileWriter.write(msg);
                fileWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return Observable.just(lrcFile);

        }).observeOn(AndroidSchedulers.mainThread()).subscribe(file -> {
            if (file == null) {
//                if (BuildConfig.DEBUG) {
//                    sendTestData(action, audioV5);
//                } else {
                postRxError(action, new Error(ErrCode.ERROR_CLIENT_NET_EMPTY_DATA));
//                }
            } else {
                action.data.put(Constant.LyricConstant.KEY_LYRIC_FILE, file);
                postRxData(action);
            }
        }, throwable -> {
//            if (BuildConfig.DEBUG) {
//                sendTestData(action, audioV5);
//            } else {
            postRxError(action, throwable);
//            }
        });

        addRxAction(action, subscribe);
    }


    /**
     * 发送假数据
     */
    private void sendTestData(RxAction action, AudioV5 audioV5) {
        File lrcFile = new File(Environment.getExternalStorageDirectory(), "txz/audio/lrc/" + audioV5.sid + AudioUtils.UNDERLINE + audioV5.id + ".lrc");
//这里写一个假数据
        String data = "[ti:情意结]\n" +
                "[ar:陈慧娴]\n" +
                "[al:196440]\n" +
                "[offset:0]\n" +
                "[00:00.10]情意结 - 陈慧娴\n" +
                "[00:00.20]词：林夕\n" +
                "[00:00.30]曲：陈辉阳\n" +
                "[00:00.60]\n" +
                "[00:12.88]为何每次早餐 仍然魂离魄散\n" +
                "[00:19.32]原来 那朝分手都要啜泣中上班\n" +
                "[00:25.88]明明能够过得这关 赢回旁人盛赞\n" +
                "[00:32.68]原来 顽强自爱这样难\n" +
                "[00:39.34]难得的激情总枉费 残忍的好人都美丽\n" +
                "[00:46.04]别怕 你将无人会代替\n" +
                "[00:52.24]你把玻璃放低请给我跪 愿这便和你有新话题\n" +
                "[00:59.45]然而别叫我小心身体 放过这回忆奴隶\n" +
                "[01:06.05]用你假的叹息当真的安慰 再爱一次再离开都抵\n" +
                "[01:12.84]难得的激情总枉费 残忍的好人都美丽\n" +
                "[01:19.68]让我 绑好这死结才矜贵\n" +
                "[01:42.77]明明芥蒂很多 为何还来探我\n" +
                "[01:49.13]然而 痛苦可想你便不必痛楚\n" +
                "[01:55.58]沦为朋友了解更多 为何仍前嫌未过\n" +
                "[02:02.34]原来 馀情未了的是我\n" +
                "[02:08.89]你把玻璃放低请给我跪 愿这便和你有新话题\n" +
                "[02:15.50]然而别叫我小心身体 放过这回忆奴隶\n" +
                "[02:21.63]用你假的叹息当真的安慰 再爱一次再离开都抵\n" +
                "[02:28.49]难得的激情总枉费 残忍的好人都美丽\n" +
                "[02:35.17]让我 绑好这死结才矜贵\n" +
                "[02:41.83]难堪的关系不可畏 离开的恋人都\n" +
                "[02:48.58]都会像命运 缠绵我一世\n" +
                "[02:52.63]\n" +
                "\n" +
                "\n";
        if (!lrcFile.exists()) {
            lrcFile.getParentFile().mkdirs();
            try {
                lrcFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //有歌词
        try (FileWriter fileWriter = new FileWriter(lrcFile)) {
            //写文件
            fileWriter.write(data);
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        action.data.put(Constant.LyricConstant.KEY_LYRIC_FILE, lrcFile);
        postRxData(action);
    }

}
