package com.txznet.music.helper;

import android.text.TextUtils;

import com.txznet.audio.player.entity.Audio;
import com.txznet.music.Constant;
import com.txznet.music.config.Configuration;
import com.txznet.music.data.entity.AudioV5;
import com.txznet.music.data.entity.BeSendData;
import com.txznet.music.data.entity.BlackListAudio;
import com.txznet.music.data.entity.FavourAudio;
import com.txznet.music.data.entity.HistoryAudio;
import com.txznet.music.data.entity.LocalAudio;
import com.txznet.music.data.entity.PushItem;
import com.txznet.music.data.entity.TmdInfo;
import com.txznet.music.data.http.api.txz.entity.TXZAudio;
import com.txznet.music.report.entity.ReportAudio;
import com.txznet.music.util.JsonHelper;
import com.txznet.music.util.Logger;
import com.txznet.music.util.MediaMetadataUtils;
import com.txznet.music.util.TimeManager;
import com.txznet.proxy.cache.TmdFile;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static com.txznet.music.data.entity.AudioV5.SOURCE_ID_LOCAL;

public class AudioConverts {
    private static final String TAG = Constant.LOG_TAG_UTILS + ":AudioConverts";

    private AudioConverts() {

    }

    public static Audio convert2MediaAudio(AudioV5 audioV5) {
        Audio result = new Audio();
        result.id = audioV5.id;
        result.name = audioV5.name;
        result.albumId = audioV5.albumId;
        result.albumSid = audioV5.albumSid;
        result.albumName = audioV5.albumName;
        result.artist = audioV5.artist;
        result.logo = audioV5.logo;
        result.resLen = audioV5.resLen;
        result.duration = audioV5.duration;
        result.sourceUrl = audioV5.sourceUrl;
        result.sid = audioV5.sid;
        result.extra = audioV5.extra;
        result.setExtraKey(Constant.AudioExtra.REPORT, audioV5.announce);
        return result;
    }

    public static Audio convert2MediaAudio(TXZAudio txzAudio) {
        if (txzAudio == null) {
            return null;
        }

        Audio audio = new Audio();
        if (txzAudio.albumId != null && txzAudio.albumId.length() > 0) {
            audio.albumId = Long.parseLong(txzAudio.albumId);
        }
        audio.albumSid = txzAudio.albumSid;
        audio.albumName = txzAudio.albumName;
        audio.logo = txzAudio.albumPic;
        audio.id = txzAudio.id;
        audio.sid = txzAudio.sid;
        audio.name = txzAudio.name;
        audio.sourceUrl = TXZUri.fromParts(txzAudio.strProcessingUrl, txzAudio.strDownloadUrl, txzAudio.downloadType, txzAudio.processIsPost, txzAudio.processHeader).toString();
        if (txzAudio.arrArtistName != null) {
            audio.artist = txzAudio.arrArtistName.toArray(new String[0]);
        }
        audio.setExtraKey(Constant.AudioExtra.SEARCH_WAKE_UP, txzAudio.wakeUp);
        audio.setExtraKey(Constant.AudioExtra.SVR_DATA, txzAudio.svrData);
        audio.setExtraKey(Constant.AudioExtra.REPORT, txzAudio.report);
        return audio;
    }


    public static AudioV5 convert2Audio(Audio audio) {
        if (audio == null) {
            return null;
        }
        AudioV5 result = new AudioV5();
        result.id = audio.id;
        result.name = audio.name;
        result.albumId = audio.albumId;
        result.albumSid = audio.albumSid;
        result.albumName = audio.albumName;
        result.artist = audio.artist;
        result.logo = audio.logo;
        result.resLen = audio.resLen;
        result.duration = audio.duration;
        result.sourceUrl = audio.sourceUrl;
        result.sid = audio.sid;
        result.extra = audio.extra;
        result.announce = audio.getExtraKey(Constant.AudioExtra.REPORT);
        return result;
    }

    public static TXZAudio convert2TXZAudio(Audio audio) {
        TXZAudio result = new TXZAudio();
        if (!TextUtils.isEmpty(audio.sourceUrl)) {
            if (audio.sourceUrl.startsWith("http")) {
                result.downloadType = TXZAudio.DOWNLOADTYPE_DIRECT;
                result.strDownloadUrl = audio.sourceUrl;
            } else if (audio.sourceUrl.startsWith("txz")) {
                TXZUri uri = TXZUri.parse(audio.sourceUrl);
                result.downloadType = uri.downloadType;
                result.strDownloadUrl = uri.downloadUrl;
                result.strProcessingUrl = uri.progressUrl;
                result.processIsPost = uri.processIsPost;
                result.processHeader = uri.processHeader;
            } else {
                // 本地路径
                result.downloadType = TXZAudio.DOWNLOADTYPE_DIRECT;
                result.strDownloadUrl = audio.sourceUrl;
            }
        }
        result.albumId = audio.albumId + "";
        result.albumSid = audio.albumSid;
        result.albumPic = audio.logo;
        result.sid = audio.sid;
        result.name = audio.name;
        if (audio.artist != null) {
            result.arrArtistName = Arrays.asList(audio.artist);
        }
        result.id = audio.id;
        result.report = audio.getExtraKey(Constant.AudioExtra.REPORT);
        return result;
    }

    public static TmdInfo convert2TmdInfo(Audio audio) {
        TmdInfo result = new TmdInfo();
        if (!TextUtils.isEmpty(audio.sourceUrl)) {
            if (audio.sourceUrl.startsWith("http")) {
                result.setDownloadType(TXZAudio.DOWNLOADTYPE_DIRECT);
                result.setStrDownloadUrl(audio.sourceUrl);
            } else if (audio.sourceUrl.startsWith("txz")) {
                TXZUri uri = TXZUri.parse(audio.sourceUrl);
                result.setDownloadType(uri.downloadType);
                result.setStrDownloadUrl(uri.downloadUrl);
                result.setStrProcessingUrl(uri.progressUrl);
            }
        }
        result.setSid(audio.sid);
        result.setName(audio.name);
        if (audio.artist != null) {
            result.setArrArtistName(Arrays.asList(audio.artist));
        }
        if (audio.artist != null) {
            result.setArrArtistName(Arrays.asList(audio.artist));
        }
        result.setAlbumId(audio.id);
        result.setAlbumSid(audio.sid);
        result.setId(audio.id);
        return result;
    }

    public static LocalAudio convert2LocalAudio(TmdInfo audio) {
        LocalAudio result = new LocalAudio();
        result.sid = audio.getSid();
        result.name = audio.getName();
        result.artist = audio.getArrArtistName() == null ? null : audio.getArrArtistName().toArray(new String[0]);
        result.duration = audio.getDuration();
        result.id = audio.getId();
        result.albumId = audio.getAlbumId();
        result.albumSid = audio.getAlbumSid();
        if (audio.getStrDownloadUrl() != null && audio.getStrDownloadUrl().endsWith(".tmd")) {
            result.sourceUrl = audio.getStrDownloadUrl();
        } else {
            result.sourceUrl = TXZUri.fromParts(audio.getStrProcessingUrl(), audio.getStrDownloadUrl(), audio.getDownloadType()).toString();
        }
        return result;
    }

    public static LocalAudio convert2LocalAudio(Audio audio) {
        LocalAudio result = new LocalAudio();
        result.sid = audio.sid;
        result.name = audio.name;
        result.artist = audio.artist;
        result.duration = audio.duration;
        result.id = audio.id;
        result.albumId = audio.albumId;
        result.albumSid = audio.albumSid;
        result.sourceUrl = audio.sourceUrl;
        result.announce = audio.getExtraKey(Constant.AudioExtra.REPORT);
        return result;
    }

    public static LocalAudio convert2LocalAudio(AudioV5 audio) {
        LocalAudio result = new LocalAudio();
        result.sid = audio.sid;
        result.name = audio.name;
        result.artist = audio.artist;
        result.duration = audio.duration;
        result.id = audio.id;
        result.albumId = audio.albumId;
        result.albumSid = audio.albumSid;
        result.sourceUrl = audio.sourceUrl;
        result.announce = audio.announce;
        return result;
    }

    public static BlackListAudio convert2BlackListAudio(AudioV5 audio) {
        BlackListAudio result = new BlackListAudio();
        result.sid = audio.sid;
        result.name = audio.name;
        result.artist = audio.artist;
        result.duration = audio.duration;
        result.id = audio.id;
        result.albumId = audio.albumId;
        result.albumSid = audio.albumSid;
        result.sourceUrl = audio.sourceUrl;
        result.announce = audio.announce;
        return result;
    }

    public static LocalAudio convert2LocalAudio(File file) {
        LocalAudio audio = null;
        TmdFile openFile = null;
        // 如果是tmd文件的情况下
        if (file.getAbsolutePath().endsWith(".tmd")) {
            try {
                openFile = TmdFile.openFile(file, -1, false);
                if (openFile == null) {
                    // FIXME: 2018/11/6 这里不会为空
//                    audio = PlayEngineFactory.getEngine().getCurrentAudio();
                    throw new RuntimeException("tmd open failed, path=" + file);
                } else {
                    audio = convert2LocalAudio(JsonHelper.fromJson(new String(openFile.loadInfo()), TmdInfo.class));
                    audio.path = file.getAbsolutePath();
                }
            } catch (Exception e) {
                Logger.e(TAG, "path=" + file.getAbsolutePath(), e);
            } finally {
                if (openFile != null) {
                    openFile.closeQuitely();
                }
            }
        } else {
            audio = new LocalAudio();
            audio.sid = SOURCE_ID_LOCAL;
            audio.sourceUrl = file.getAbsolutePath();
            boolean isThird = false;
            // 第三方音频，跳过解码
            // 如第三方QQ音乐，读取时长一首2秒左右
            for (String path : Configuration.ThirdPath.PATHS) {
                if (file.getAbsolutePath().startsWith(path)) {
                    isThird = true;
                    break;
                }
            }
            if (!isThird) {
                MediaMetadataUtils.getMediaMetadata(audio, file);
            }
            if (TextUtils.isEmpty(audio.name)) {
                audio.name = file.getName().substring(0, file.getName().lastIndexOf("."));
            }
        }
        if (audio == null) {
            return null;
        }
        if (audio.id == 0) {
            audio.id = Math.abs(audio.name.hashCode() + Arrays.hashCode(audio.artist));// 使用标题和歌手名称作为id
        }
        //历史原因:版本3.0,属于和考拉共同开发的,数据来源属于考拉.考拉对id进行修改了(暂且认为全部id都在前面加了100000).故认为是本地音乐,否则对收藏有影响
        if (String.valueOf(audio.id).startsWith("100000")) {
            audio.sid = 0;
        }
//                audio.setPinyin(PinYinUtil.getPinYin(audio.getName()));
        // 小写 p 是 property 的意思，表示 Unicode 属性，用于 Unicode
        // 正表达式的前缀。中括号内的“P”表示Unicode 字符集七个字符属性之一：标点字符。
//        audio.desc = audio.name.replaceAll("[\\p{P}]", "");
        return audio;
    }

    public static AudioV5 convert2Audio(TXZAudio txzAudio) {
        if (txzAudio == null) {
            return null;
        }

        AudioV5 audio = new AudioV5();
        if (txzAudio.albumId != null && txzAudio.albumId.length() > 0) {
            audio.albumId = Long.parseLong(txzAudio.albumId);
        }
        audio.albumSid = txzAudio.albumSid;
        audio.albumName = txzAudio.albumName;
        audio.logo = txzAudio.albumPic;
        audio.id = txzAudio.id;
        audio.sid = txzAudio.sid;
        audio.name = txzAudio.name;
        audio.announce = txzAudio.report;
        if (txzAudio.arrArtistName != null) {
            audio.artist = txzAudio.arrArtistName.toArray(new String[0]);
        }
        audio.setExtraKey(Constant.AudioExtra.SEARCH_WAKE_UP, txzAudio.wakeUp);
        audio.sourceUrl = TXZUri.fromParts(txzAudio.strProcessingUrl, txzAudio.strDownloadUrl, txzAudio.downloadType, txzAudio.processIsPost, txzAudio.processHeader).toString();
        audio.setExtraKey(Constant.AudioExtra.SVR_DATA, txzAudio.svrData);
        audio.announce = txzAudio.report;
        return audio;
    }

    public static ReportAudio convert2Report(AudioV5 audioV5) {
        ReportAudio audio = new ReportAudio();
        audio.albumSid = audioV5.albumSid;
        audio.albumId = audioV5.albumId;
        audio.audioId = audioV5.id;
        audio.audioSid = audioV5.sid;
        audio.svrData = audioV5.getExtraKey(Constant.AudioExtra.SVR_DATA, "");
        return audio;
    }

    public interface Convert<T, V> {
        V convert(T t);
    }

    public static <T, V> void convert2List(List<T> src, List<V> dest, Convert<T, V> convert) {
        if (dest == null || src == null) {
            return;
        }

        if (src.size() > 0) {
            for (T t : src) {
                dest.add(convert.convert(t));
            }
        }
    }

    public static <T, V> List<V> convert2List(List<T> src, Convert<T, V> convert) {
        if (src == null) {
            return null;
        }
        List<V> dest = new LinkedList<>();
        convert2List(src, dest, convert);
        return dest;
    }


    public static AudioV5 convertLocalAudio2Audio(LocalAudio audio) {
        return JsonHelper.fromJson(JsonHelper.toJson(audio), AudioV5.class);
    }

    public static FavourAudio convertAudio2FavourAudio(AudioV5 audioV5, long timestamp) {
        FavourAudio favourAudio = JsonHelper.fromJson(JsonHelper.toJson(audioV5), FavourAudio.class);
        if (timestamp != 0) {
            favourAudio.timestamp = timestamp;
        } else {
            favourAudio.timestamp = TimeManager.getInstance().getTimeMillis();//SystemClock.currentThreadTimeMillis();
        }
        return favourAudio;
    }

    public static FavourAudio convertAudio2FavourAudio(AudioV5 audioV5) {
        return convertAudio2FavourAudio(audioV5, 0);
    }

    public static BeSendData convert2BeSendData(FavourAudio favourAudio, @BeSendData.OperationType int operationType) {
        BeSendData beSendData = new BeSendData();
        beSendData.operation = operationType;
        beSendData.id = favourAudio.id;
        beSendData.sid = favourAudio.sid;
        beSendData.name = favourAudio.name;
        beSendData.artist = favourAudio.artist;
        beSendData.timestamp = favourAudio.timestamp;
        return beSendData;
    }

    public static HistoryAudio convert2HistoryAudio(AudioV5 audioV5) {
        return JsonHelper.fromJson(JsonHelper.toJson(audioV5), HistoryAudio.class);
    }

    public static AudioV5 convert2AudioV5(HistoryAudio historyAudio) {
        return JsonHelper.fromJson(JsonHelper.toJson(historyAudio), AudioV5.class);
    }

    public static PushItem convert2PushItem(TXZAudio txzAudio, @PushItem.Status int read) {
        PushItem pushItem = JsonHelper.fromJson(JsonHelper.toJson(AudioConverts.convert2Audio(txzAudio)), PushItem.class);
        // FIXME: 2018/12/27 如果改为正确的时间戳
        pushItem.timestamp = TimeManager.getInstance().getTimeMillis();
        pushItem.status = read;
        return pushItem;
    }


}
