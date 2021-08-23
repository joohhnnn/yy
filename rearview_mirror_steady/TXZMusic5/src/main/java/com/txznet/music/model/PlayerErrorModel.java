package com.txznet.music.model;

import android.text.TextUtils;

import com.txznet.comm.err.Error;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.util.NetworkUtil;
import com.txznet.loader.AppLogic;
import com.txznet.music.Constant;
import com.txznet.music.ErrCode;
import com.txznet.music.action.ActionType;
import com.txznet.music.data.db.DBUtils;
import com.txznet.music.data.db.dao.PlayUrlInfoDao;
import com.txznet.music.data.entity.Album;
import com.txznet.music.data.entity.AudioV5;
import com.txznet.music.data.entity.PlayUrlInfo;
import com.txznet.music.data.http.api.txz.entity.req.TXZReqError;
import com.txznet.music.data.sp.SharedPreferencesUtils;
import com.txznet.music.helper.PlayHelper;
import com.txznet.music.helper.TXZNetRequest;
import com.txznet.music.helper.TXZUri;
import com.txznet.music.util.AudioUtils;
import com.txznet.music.util.JsonHelper;
import com.txznet.music.util.Logger;
import com.txznet.music.util.ToastUtils;
import com.txznet.music.util.TtsHelper;
import com.txznet.rxflux.Operation;
import com.txznet.rxflux.RxAction;
import com.txznet.rxflux.RxWorkflow;

import java.io.File;

import static com.txznet.music.data.http.api.txz.TXZMusicApi.GET_REPORT_ERROR;

/**
 * 播放错误处理
 *
 * @author zackzhou
 * @date 2019/1/3,15:33
 */

public class PlayerErrorModel extends RxWorkflow {
    public static final String TAG = Constant.LOG_TAG_ERROR;
    public static boolean hasTipDiskSpaceInsufficient; // 是否提醒过空间不足


    @Override
    public void onAction(RxAction action) {
        switch (action.type) {
            case ActionType.ACTION_PROXY_ERROR:
            case ActionType.ACTION_PLAYER_ON_ERROR:
                Error error = (Error) action.data.get(Constant.PlayConstant.KEY_ERROR);
                if (error != null) {
                    AppLogic.runOnBackGround(() -> {
                        Logger.e(TAG, String.format("error=%s", error));
                        int errCode = error.errorCode;

                        // 网络引起的io异常
                        if (ErrCode.ERROR_CLIENT_MEDIA_ERR_IO == errCode) {
                            // 当前离线状态，跳过
                            if (!NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
                                return;
                            }
                            if ("connect status error".equals(error.desc)) {
                                return;
                            }
                            if (error.desc != null && error.desc.lastIndexOf("UnresolvedAddressException") != -1) {
                                return;
                            }
                            if (error.desc != null && error.desc.lastIndexOf("ClosedChannelException") != -1) {
                                return;
                            }
                        }

                        // 代理缓存异常
                        if (ErrCode.ERROR_CLIENT_MEDIA_DISK_SPACE_INSUFFICIENT == errCode || ErrCode.ERROR_CLIENT_MEDIA_CACHE_SIZE_LIMIT == errCode) {
                            if (!hasTipDiskSpaceInsufficient) {
                                hasTipDiskSpaceInsufficient = true;
                                int tipCount = SharedPreferencesUtils.getDiskSpaceInsufficientTipCount();
                                if (tipCount < 3) {
                                    TtsHelper.speakResource("RS_VOICE_MUSIC_DISK_SPACE_INSUFFICIENT", Constant.RS_VOICE_MUSIC_DISK_SPACE_INSUFFICIENT);
                                    SharedPreferencesUtils.setDiskSpaceInsufficientTipCount(++tipCount);
                                    Logger.d(Constant.LOG_TAG_PROXY, "current disk space insufficient count=" + tipCount);
                                }
                            }
                        }

                        // 播放链接发生改变
                        if (ErrCode.ERROR_CLIENT_MEDIA_URL_CHANGE == errCode) {
                            clearPlayInfoCache();
                            PlayHelper.get().replay();
                            return;
                        }

                        // tmd文件校验异常
                        if (ErrCode.ERROR_CLIENT_MEDIA_FILE_CHECK_FAIL == errCode) {
                            ToastUtils.showShortOnUI(error.hint);
                            PlayHelper.get().deleteCacheAndNext(Operation.ERROR);
                            return;
                        }

                        // 文件不存在
                        if (ErrCode.ERROR_CLIENT_MEDIA_NOT_FOUND == errCode
                                || ErrCode.ERROR_CLIENT_MEDIA_WRONG_URL == errCode
                                || ErrCode.ERROR_CLIENT_MEDIA_BAD_REQUEST == errCode
                                || ErrCode.ERROR_CLIENT_MEDIA_FILE_FORBIDDEN == errCode
                                || ErrCode.ERROR_MEDIA_SYS_PLAYER == errCode
                                || ErrCode.ERROR_CLIENT_MEDIA_ERR_IO == errCode) {
                            Album album = PlayHelper.get().getCurrAlbum();
                            AudioV5 audioV5 = PlayHelper.get().getCurrAudio();
                            if (audioV5 == null) {
                                return;
                            }

                            TXZReqError txzReqError = new TXZReqError();
                            if (album != null) {
                                txzReqError.albumId = album.id;
                                txzReqError.albumSid = album.sid;
                            }

                            // 非内核播放异常
                            if (ErrCode.ERROR_MEDIA_SYS_PLAYER != errCode) {
                                clearPlayInfoCache();
                                if (!AudioUtils.isLocalSong(audioV5.sid)) {
                                    txzReqError.audioId = audioV5.id;
                                    txzReqError.sourceId = audioV5.sid;
                                    txzReqError.strName = audioV5.name;
                                    TXZUri uri = TXZUri.parse(audioV5.sourceUrl);
                                    if (uri == null) {
                                        txzReqError.strUrl = audioV5.sourceUrl;
                                    } else {
                                        txzReqError.strUrl = TextUtils.equals("1", uri.downloadType) ? uri.progressUrl : uri.downloadUrl;
                                    }
                                    txzReqError.artist = JsonHelper.toJson(audioV5.artist);
                                    txzReqError.errCode = errCode;
                                    Logger.e(Constant.LOG_TAG_LOGIC, "[Audio]error:" + error.toString());
                                    String json = JsonHelper.toJson(txzReqError);
                                    TXZNetRequest.get().sendSeqRequestToCore(GET_REPORT_ERROR, json.getBytes(), null);


                                    if (ErrCode.ERROR_CLIENT_MEDIA_FILE_FORBIDDEN == errCode || ErrCode.ERROR_CLIENT_MEDIA_NOT_FOUND == errCode) {
                                        int retryCount = 0;
                                        try {
                                            retryCount = audioV5.getExtraKey(Constant.AudioExtra.RETRY_COUNT, 0);
                                        } catch (Exception e) {
                                        }
                                        if (retryCount < 2) {
                                            Logger.e(TAG, "media file abnormal, retry time=" + (retryCount + 1));
                                            audioV5.setExtraKey(Constant.AudioExtra.RETRY_COUNT, ++retryCount);
                                            PlayHelper.get().replay(audioV5);
                                            return;
                                        }
                                    }
                                }
                            } else {
                                // 内核seekTo状态异常引起
                                if ("-10000-1".equals(error.desc)) {
                                    PlayHelper.get().replay();
                                }
                                if ("-10000-0".equals(error.desc)) {
                                    int retryCount = 0;
                                    try {
                                        retryCount = audioV5.getExtraKey(Constant.AudioExtra.RETRY_COUNT, 0);
                                    } catch (Exception e) {
                                    }
                                    if (retryCount > 1) {
                                        File file = AudioUtils.getAudioTMDFile(audioV5);
                                        if (file != null && file.exists()) {
                                            file.delete();
                                        }
                                    }
                                    if (retryCount > 4) {
                                        clearPlayInfoCache();
                                    }
                                    if (retryCount > 8) {
                                        audioV5.setExtraKey(Constant.AudioExtra.RETRY_COUNT, 0);
                                        PlayHelper.get().cleanNotExistMediaAndNext(Operation.ERROR);
                                        return;
                                    }
                                    Logger.e(TAG, "media file -10000, retry time=" + (retryCount + 1));
                                    audioV5.setExtraKey(Constant.AudioExtra.RETRY_COUNT, ++retryCount);
                                    PlayHelper.get().replay();
                                }
                                return;
                            }
                            PlayHelper.get().cleanNotExistMediaAndNext(Operation.ERROR);
                        }
                    });
                }
                break;
        }
    }

    private void clearPlayInfoCache() {
        AudioV5 audioV5 = PlayHelper.get().getCurrAudio();
        if (audioV5 != null) {
            PlayUrlInfoDao infoDao = DBUtils.getDatabase(GlobalContext.get()).getPlayUrlInfoDao();
            PlayUrlInfo playUrlInfo = infoDao.findBySidAndId(audioV5.sid, audioV5.id);
            if (playUrlInfo != null) {
                infoDao.delete(playUrlInfo);
            }
        }
    }
}
