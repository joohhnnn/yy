//package com.txznet.music.soundControlModule.logic;
//
//
//import com.txznet.comm.remote.GlobalContext;
//import com.txznet.comm.remote.util.LogUtil;
//import com.txznet.comm.remote.util.MonitorUtil;
//import com.txznet.comm.remote.util.TtsUtil;
//import com.txznet.comm.util.CollectionUtils;
//import com.txznet.comm.util.JSONBuilder;
//import com.txznet.loader.AppLogic;
//import com.txznet.music.albumModule.bean.Album;
//import com.txznet.music.albumModule.bean.Audio;
//import com.txznet.music.baseModule.Constant;
//import com.txznet.music.baseModule.bean.EnumState;
//import com.txznet.music.baseModule.dao.DBManager;
//import com.txznet.music.net.NetManager;
//import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
//import com.txznet.music.search.SearchEngine;
//import com.txznet.music.service.MusicInteractionWithCore;
//import com.txznet.music.soundControlModule.bean.BaseAudio;
//import com.txznet.music.soundControlModule.logic.net.request.ReqSearch;
//import com.txznet.music.soundControlModule.logic.net.response.ResponseSearch;
//import com.txznet.music.utils.JsonHelper;
//import com.txznet.music.utils.SharedPreferencesUtils;
//import com.txznet.music.utils.StringUtils;
//import com.txznet.music.utils.Utils;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//
//
///**
// * 声控搜索
// * Created by telenewbie on 2016/12/23.
// */
//
//public class SoundSearch implements ISoundSearch {
//    private static final String TAG = "music::sound::callback";
//    private static final int LENGTH = 20;
//    private ReqSearch reqData = null;
//    private JSONArray array = new JSONArray();
//    private List<Audio> audios = new ArrayList<Audio>();
//
//    //##创建一个单例类##
//    private volatile static SoundSearch singleton;
//    private String rawText;
//
//    private SoundSearch() {
//    }
//
//    public String getRawText() {
//        return rawText;
//    }
//
//    public static SoundSearch getInstance() {
//        if (singleton == null) {
//            synchronized (SoundSearch.class) {
//                if (singleton == null) {
//                    singleton = new SoundSearch();
//                }
//            }
//        }
//        return singleton;
//    }
//
//
//    /**
//     * 声控处理
//     */
//    @Override
//    public void searchContent(String soundData) {
//        MonitorUtil.monitorCumulant(Constant.M_SOUND_FIND);
//        array = new JSONArray();
//        // 声控获取
//        reqData = new ReqSearch();
////		soundData.replaceAll(" ", "");
//        LogUtil.logd(TAG + "sound.find:::" + soundData);
//
//        JSONBuilder jsonBuilder = new JSONBuilder(soundData);
//        reqData.setAudioName(jsonBuilder.getVal("title", String.class));
//        reqData.setAlbumName(jsonBuilder.getVal("album", String.class));
//        reqData.setArtist(StringUtils.toString(jsonBuilder.getVal("artist", String[].class)));
//        reqData.setCategory(StringUtils.StringFilter(StringUtils.toString(jsonBuilder.getVal("keywords", String[].class))));
//        reqData.setKeywords(StringUtils.StringFilter(StringUtils.toString(jsonBuilder.getVal("keywords", String[].class))));
//        reqData.setField(jsonBuilder.getVal("field", int.class, 0));// 1。表示歌曲，2.表示电台
//        reqData.setSubCategory(jsonBuilder.getVal("subcategory", String.class));
//        rawText = jsonBuilder.getVal("text", String.class);
//        reqData.setText(jsonBuilder.getVal("text", String.class));
//        reqData.setQualitytype(SharedPreferencesUtils.getQulityMode() + "");
//        // 后台说:只认category
//        if (StringUtils.isNotEmpty(reqData.getSubCategory())) {
//            reqData.setCategory(reqData.getSubCategory());
//        }
//
//        if (StringUtils.isEmpty(reqData.getCategory())) {
//            reqData.setCategory(jsonBuilder.getVal("category", String.class));
//        }
//
//        LogUtil.logd(TAG + "sound.find:::ReqSearch::" + reqData.toString());
//
//
////        if (CollectionUtils.isNotEmpty(audios)) {
////            LogUtil.logd(TAG + "sound.find:::localMusic::" + audios.toString());
//        audios.clear();
//        if (!Utils.isNetworkConnected(GlobalContext.get())) {// 没有网络则
//
//            StringBuffer sbBuffer = new StringBuffer();
//            if (StringUtils.isNotEmpty(reqData.getArtist())) {
//                //我要听周杰伦的歌
//                sbBuffer.append(" " + BaseDaoImpl.TABLE_ARTISTS + " ");
//                sbBuffer.append(" like '%");
//                sbBuffer.append(reqData.getArtist());
//                sbBuffer.append("%' ");
//            }
//            if (StringUtils.isNotEmpty(reqData.getAudioName())
//                    && (StringUtils.isNotEmpty(reqData.getArtist()) || StringUtils
//                    .isNotEmpty(reqData.getAlbumName()))) {
//                sbBuffer.append(" and ");
//            }
//            if (StringUtils.isNotEmpty(reqData.getAudioName())) {
//                sbBuffer.append(" name ");
//                sbBuffer.append(" like '%");
//                sbBuffer.append(reqData.getAudioName());
//                sbBuffer.append("%' ");
//            }
//
//            if (StringUtils.isEmpty(reqData.getArtist())
//                    && StringUtils.isEmpty(reqData.getAudioName())
//                    && StringUtils.isNotEmpty(reqData.getAlbumName())) {
//                sbBuffer.append(" name ");
//                sbBuffer.append(" like '%");
//                sbBuffer.append(reqData.getAlbumName());
//                sbBuffer.append("%' ");
//            }
//            if (StringUtils.isNotEmpty(sbBuffer.toString())) {
//                LogUtil.logd("search::Local::music::" + sbBuffer.toString());
//                audios = LocalAudioDBHelper.getInstance().findAll(Audio.class,
//                        sbBuffer.toString(), null, null);
//            } else {
//                audios.clear();
//            }
//            if (CollectionUtils.isEmpty(audios)) {
//                TtsUtil.speakTextOnRecordWin("RS_VOICE_SPEAK_NETNOTCON_TIPS",
//                        Constant.RS_VOICE_SPEAK_LOCAL_NO_SONGS_TIPS, false, null);
//                return;
//            }
//            // SharedPreferencesUtils.setCurrentAlbumID(0);
//            if (audios.size() == 1) {// 就只有一条数据
//                StringBuilder speakString = new StringBuilder();
//                if (CollectionUtils.isNotEmpty(audios.get(0).getArrArtistName())
//                        && !"未知艺术家".equals(audios.get(0).getArrArtistName().get(0))) {
//                    speakString.append(StringUtils.toString(audios.get(0).getArrArtistName()));
//                    if (speakString.length() > 0) {
//                        speakString.append("的");
//                    }
//                }
//                String name = speakString.toString() + audios.get(0).getName();
//                TtsUtil.speakTextOnRecordWin("RS_VOICE_SPEAK_WILL_PLAY",
//                        StringUtils.replace(Constant.RS_VOICE_SPEAK_WILL_PLAY, name),
//                        new String[]{Constant.PLACEHODLER, name}, true,
//                        new Runnable() {
//                            @Override
//                            public void run() {
//                                List<Audio> localAudios = LocalAudioDBHelper.getInstance().findAll(Audio.class);
//                                //TODO:设置数据并播放
//                                PlayEngineFactory.getEngine().playAudioList(EnumState.OperState.sound,localAudios,0,null);
////                                PlayerControlManager.getInstance().playAudioList(localAudios, localAudios.indexOf(audios.get(0)), Constants.RESOURCES_TYPE_HISTORY_MUSIC_INT);
//                            }
//                        });
//            } else {
//                JSONArray array = new JSONArray();
//                for (int i = 0; i < audios.size(); i++) {
//                    JSONObject jsonObject = new JSONObject();
//                    try {
//                        jsonObject.put("title", audios.get(i).getName());
//                        jsonObject.put("name", StringUtils.toString(audios
//                                .get(i).getArrArtistName()));
//                        jsonObject.put("id", audios.get(i).getId());
//                        array.put(jsonObject);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//                MusicInteractionWithCore.sendToCore("txz.music.syncmusiclist",
//                        array.toString().getBytes());
//            }
//            return;
//        }
////        } else if (StringUtils.isNotEmpty(reqData.getAudioName()) || StringUtils.isNotEmpty(reqData.getCategory())) {
////            //TODO: 从历史数据中查找数据
////        }
//        if (Constant.SoundSessionID == -1) {
//            LogUtil.logd(TAG + "cancle so can't do net request");
//            return;
//        } else {
//            LogUtil.logd(TAG + "soundID=" + Constant.SoundSessionID);
//        }
//        if (!Utils.isNetworkConnected(GlobalContext.get())) {
//            TtsUtil.speakTextOnRecordWin("RS_VOICE_SPEAK_NETNOTCON_TIPS",
//                    Constant.RS_VOICE_SPEAK_NETNOTCON_TIPS, false, null);
//        } else {
//            TtsUtil.speakTextOnRecordWin("RS_VOICE_SPEAK_SEARCHDATA_TIPS",
//                    Constant.RS_VOICE_SPEAK_SEARCHDATA_TIPS, null, false,
//                    false, new Runnable() {
//
//                        @Override
//                        public void run() {
//                            Constant.SoundSessionID = MusicInteractionWithCore.requestData("txz.music.dataInterface", Constant.GET_SEARCH, JsonHelper.toJson(reqData).getBytes());
//                        }
//                    });
//        }
//    }
//
//    private ResponseSearch responseSearch = null;
//    public boolean isSearchingData = false;// 声控搜索中
//
//    @Override
//    public void searchResult(byte[] data) {
//        try {
//            responseSearch = JsonHelper.toObject(ResponseSearch.class, new String(data));
//        } catch (Exception e) {
//            e.printStackTrace();
//            TtsUtil.speakResource("RS_VOICE_SPEAK_JSONERR_TIPS", Constant.RS_VOICE_SPEAK_JSONERR_TIPS);
//            return;
//        }
//        if (CollectionUtils.isEmpty(responseSearch.getArrAudio())
//                && CollectionUtils.isEmpty(responseSearch.getArrAlbum())
//                && CollectionUtils.isEmpty(responseSearch.getArrMix())
//                && CollectionUtils.isEmpty(audios)) {
//            String tips = Constant.RS_VOICE_SPEAK_NODATAFOUND_TIPS;
//            if (StringUtils.isNotEmpty(reqData.getArtist())
//                    && StringUtils.isNotEmpty(reqData.getAudioName())) {
//                tips = Constant.RS_VOICE_SPEAK_NODATAFOUND_WITH_TIPS;
//            }
//            MonitorUtil.monitorCumulant(Constant.M_EMPTY_SOUND);
//            TtsUtil.speakTextOnRecordWin("RS_VOICE_SPEAK_NODATAFOUND_TIPS",
//                    tips, false, null);
//            return;
//        }
////        if (CollectionUtils.isNotEmpty(audios)) {// 本地搜索出来的结果
////            if (CollectionUtils.isNotEmpty(responseSearch.getArrAudio())) {
////                // 剔除相同歌名和歌手的数据
////                List<Audio> arrAudio = responseSearch.getArrAudio();
////                for (Audio audio : audios) {
////                    for (int i = arrAudio.size() - 1; i >= 0; i--) {
////                        if (audio.getId() == arrAudio.get(i).getId()) {
////                            arrAudio.remove(i);
////                        }
////                    }
////                }
////                responseSearch.getArrAudio().addAll(0, audios);
////            } else {
////                responseSearch.setArrAudio(audios);
////            }
////        }
//
//        synchronized (array) {
//
//            // Core 只需要三个数据：title，name,id
//            int length = LENGTH;
//            int countAudio = 0;
//            int countAlbum = 0;
//
//            if (CollectionUtils.isNotEmpty(responseSearch.getArrMix())) {
//                if (responseSearch.getArrMix().size() < LENGTH) {
//                    length = responseSearch.getArrMix().size();
//                }
//
//                for (int i = 0; i < length; i++) {
//                    countAudio++;
//                    JSONObject jsonObject = new JSONObject();
//                    BaseAudio baseAudio = responseSearch.getArrMix().get(i);
//                    if (null == baseAudio) {
//                        continue;
//                    }
//                    try {
//                        if (baseAudio.getAlbum() != null) {
//                            Album album = baseAudio.getAlbum();
//                            jsonObject.put("title", album.getName());
//                            jsonObject.put("name", StringUtils.toString(album
//                                    .getArrArtistName()));
//                            jsonObject.put("id", album.getId());
//                            jsonObject.put("report", album.getReport());
//                            if (responseSearch.getPlayType() == ResponseSearch.DELAYPLAY) {
//                                jsonObject.put("delayTime",
//                                        responseSearch.getDelayTime());
//                            }
//                        } else if (baseAudio.getAudio() != null) {
//                            Audio audio = baseAudio.getAudio();
//                            jsonObject.put("title", audio.getName());
//                            jsonObject.put("name", StringUtils.toString(audio
//                                    .getArrArtistName()));
//                            jsonObject.put("id", audio.getId());
//                            jsonObject.put("report", audio.getReport());
//                            if (responseSearch.getPlayType() == ResponseSearch.DELAYPLAY) {
//                                jsonObject.put("delayTime",
//                                        responseSearch.getDelayTime());
//                            }
//                        }
//
//                        array.put(jsonObject);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//
//            if (CollectionUtils.isNotEmpty(responseSearch.getArrAudio())) {
//                if (responseSearch.getArrAudio().size() < LENGTH) {
//                    length = responseSearch.getArrAudio().size();
//                }
//
//                for (int i = 0; i < length; i++) {
//                    countAudio++;
//                    JSONObject jsonObject = new JSONObject();
//                    Audio audio = responseSearch.getArrAudio().get(i);
//                    if (null == audio) {
//                        continue;
//                    }
//                    try {
//                        jsonObject.put("title", audio.getName());
//                        jsonObject.put("name", StringUtils.toString(audio.getArrArtistName()));
//                        jsonObject.put("id", audio.getId());
//                        jsonObject.put("report", audio.getReport());
//                        if (responseSearch.getPlayType() == ResponseSearch.DELAYPLAY) {
//                            jsonObject.put("delayTime", responseSearch.getDelayTime());
//                        }
//                        array.put(jsonObject);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//            if (CollectionUtils.isNotEmpty(responseSearch.getArrAlbum())) {
//                if (responseSearch.getArrAlbum().size() < LENGTH) {
//                    length = responseSearch.getArrAlbum().size();
//                }
//                for (int i = 0; i < length; i++) {
//                    countAlbum++;
//                    JSONObject jsonObject = new JSONObject();
//                    Album album = responseSearch.getArrAlbum().get(i);
//                    if (album == null) {// 服务器有可能返回null对象
//                        continue;
//                    }
//                    try {
//                        jsonObject.put("title", album.getName());
//                        jsonObject.put("name",
//                                StringUtils.toString(album.getArrArtistName()));
//                        jsonObject.put("id", album.getId());
//                        jsonObject.put("report", album.getReport());
//                        if (responseSearch.getPlayType() == ResponseSearch.DELAYPLAY) {
//                            jsonObject.put("delayTime",
//                                    responseSearch.getDelayTime());
//                        }
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                    array.put(jsonObject);
//                }
//            }
//
//
//            // 直接播放
//            if (responseSearch.getPlayType() == ResponseSearch.GOPLAY) {
//
//                String name = Constant.RS_VOICE_SPEAK_PARSE_ERROR;
//                try {
//                    if (responseSearch.getReturnType() == 1) {
//                        Audio returnAudio = responseSearch.getArrAudio().get(
//                                responseSearch.getPlayIndex());
//                        if (CollectionUtils.isNotEmpty(returnAudio
//                                .getArrArtistName())) {
//                            name = returnAudio.getArrArtistName().get(0) + "的"
//                                    + returnAudio.getName();
//                        } else {
//                            name = returnAudio.getName();
//                        }
//                    } else if (responseSearch.getReturnType() == 2) {
//                        name = responseSearch.getArrAlbum()
//                                .get(responseSearch.getPlayIndex()).getName();
//                    } else if (responseSearch.getReturnType() == 3) {
//                        if (responseSearch.getArrMix()
//                                .get(responseSearch.getPlayIndex()).getType() == 1) {
//
//                            Audio returnAudio = responseSearch.getArrMix()
//                                    .get(responseSearch.getPlayIndex())
//                                    .getAudio();
//                            if (CollectionUtils.isNotEmpty(returnAudio
//                                    .getArrArtistName())) {
//                                name = returnAudio.getArrArtistName().get(0)
//                                        + "的" + returnAudio.getName();
//                            } else {
//                                name = returnAudio.getName();
//                            }
//                        } else {
//                            name = responseSearch.getArrMix()
//                                    .get(responseSearch.getPlayIndex())
//                                    .getAlbum().getName();
//                        }
//                    } else {
//                        TtsUtil.speakTextOnRecordWin("RS_VOICE_SPEAK_PARSE_ERROR", name, true, null);
//                        return;
//                    }
//                } catch (Exception e) {
//                    LogUtil.loge(TAG + ":parse error:" + e.toString());
//                    TtsUtil.speakTextOnRecordWin("RS_VOICE_SPEAK_PARSE_ERROR", name, true, null);
//                    return;
//                }
//                searchPreloadIndex(responseSearch.getPlayIndex());
//                TtsUtil.speakTextOnRecordWinWithCancle("RS_VOICE_SPEAK_WILL_PLAY",
//                        StringUtils.replace(Constant.RS_VOICE_SPEAK_WILL_PLAY, name), new String[]{Constant.PLACEHODLER, name}, false, new Runnable() {
//                            @Override
//                            public void run() {
//                                //TODO:修改TXZ-6456:后期应该增加一个先加载数据但不播放,直到重新获取音频焦点的时候在播放.
//                                searchChoiceIndex(responseSearch.getPlayIndex());
//                            }
//                        });
//
//            } else if (responseSearch.getPlayType() == ResponseSearch.DELAYPLAY) {
//                MusicInteractionWithCore.sendToCore("txz.music.syncmusiclist", array.toString().getBytes());
//                // 延时播放
//            } else if (responseSearch.getPlayType() == ResponseSearch.SELECTPLAY) {
//                // 选择播放
//                MusicInteractionWithCore.sendToCore("txz.music.syncmusiclist", array.toString().getBytes());
//            }
//        }
//    }
//
//    @Override
//    public void searchChoiceIndex(final int position) {
//        AppLogic.runOnUiGround(new Runnable() {
//            @Override
//            public void run() {
//                choiceIndex(position);
//            }
//        }, 0);
//    }
//
//    @Override
//    public void searchPreloadIndex(int position) {
//        LogUtil.logd(Constant.PRELOAD_TAG + ":sound:preload:" + position);
//        Audio audio = null;
//        if (responseSearch.getReturnType() == 1) {
//            audio = responseSearch.getArrAudio().get(position);
//        } else if (responseSearch.getReturnType() == 3) {
//            BaseAudio baseAudio = responseSearch.getArrMix().get(position);
//            if (baseAudio.getAudio() != null) {
//                audio = baseAudio.getAudio();
//            }
//        }
//
//        //只有音频才缓存
////        if (audio != null) {
////            if (Utils.needPreload(audio.getSid(), audio.getAudioType())) {
////                LogUtil.logd(Constant.PRELOAD_TAG + ":sound:audio:" + position + "," + audio.getName());
////                DataInterfaceBroadcastHelper.sendStartPreloadNextAudioInfo(audio);
////            } else {
////                LogUtil.logd(Constant.PRELOAD_TAG + ":sound:notSupport:audio:" + position + "," + audio.getName());
////            }
////        } else {
////            LogUtil.logd(Constant.PRELOAD_TAG + ":sound:album:" + position + ",can't load");
////        }
//    }
//
//    /**
//     * 选中第几个
//     *
//     * @param index 第几个
//     */
//    private void choiceIndex(int index) {
//        try {
//            //本地离线的情况下的情况
//            if (CollectionUtils.isNotEmpty(audios)) {
//                List<Audio> localAudios = LocalAudioDBHelper.getInstance().findAll(Audio.class);
//                //TODO:设置数据并播放
//                PlayEngineFactory.getEngine().playAudioList(EnumState.OperState.sound,localAudios,localAudios.indexOf(audios.get(index)),null);
//                return;
//            }
//
//            if (null != responseSearch) {
//                SharedPreferencesUtils.setAudioSource(Constant.TYPE_SOUND);
//                if (responseSearch.getReturnType() == 2) {// 专辑
//                    Album album = responseSearch.getArrAlbum().get(index);
////                    doSelectAlbum(album, index);
//                    doSelectAlbum(album);
//                } else if (responseSearch.getReturnType() == 1) {
//                    doSelectAudio(responseSearch.getArrAudio().get(index),
//                            responseSearch.getArrAudio(), index);
//                } else
//                    // 有可能有音频或专辑
//                    if (responseSearch.getReturnType() == 3) {
//                        BaseAudio baseAudio = responseSearch.getArrMix().get(index);
//                        // Audio
//                        if (baseAudio.getType() == BaseAudio.MUSIC_TYPE) {
//                            List<Audio> searchAudios = new ArrayList<>();
//                            for (int i = 0; i < responseSearch.getArrMix().size(); i++) {
//                                if (responseSearch.getArrMix().get(i).getType() == BaseAudio.MUSIC_TYPE) {
//                                    searchAudios.add(responseSearch.getArrMix()
//                                            .get(i).getAudio());
//                                }
//                            }
//                            //指定第几个
//                            int playIndex = index;
//                            playIndex = searchAudios.indexOf(baseAudio.getAudio());
//                            doSelectAudio(baseAudio.getAudio(), searchAudios, playIndex >= 0 ? playIndex : index);
//                        } else
//                            // Album
//                            if (baseAudio.getType() == BaseAudio.ALBUM_TYPE) {
//                                doSelectAlbum(baseAudio.getAlbum());
//                            }
//                    }
//            }
//        } finally {
//            isSearchingData = false;
//            // 打开界面
//            Utils.jumpTOMediaPlayerAct(false);
//        }
//    }
//
//
//    private void checkPlayList(List<Audio> playList, Audio audio) {
//        if (audio == null || playList == null || playList.size() == 0) {
//            return;
//        }
//        Iterator<Audio> iterator = playList.iterator();
//        while (iterator.hasNext()) {
//            Audio next = iterator.next();
//            if (next.getId() == audio.getId() && next.getSid() == audio.getSid()) {
//                iterator.remove();
//            }
//        }
//    }
//
//    /**
//     * 播放音频
//     *
//     * @param audio    带歌曲的时候直接播放该歌曲 播放列表为历史播放+该歌曲，否则为声控里面所有的歌曲
//     * @param playList 播放列表 当带歌曲的时候可以为null
//     * @param index    播放播放列表中的第几个歌曲
//     */
//    private void doSelectAudio(Audio audio, List<Audio> playList, int index) {
//        // SharedPreferencesUtils.setCurrentAlbumID(0);
//        if (reqData != null && StringUtils.isNotEmpty(reqData.getAudioName())) {
//            List<Integer> sids = Utils.getSongSid();
//            List<Audio> localMusic = DBManager.getInstance().findHistoryAudioBySid(sids);
//            localMusic.add(0, audio);
//            PlayEngineFactory.getEngine().setAudios(EnumState.OperState.sound, localMusic, null, 0);
//        } else {
//            PlayEngineFactory.getEngine().setAudios(EnumState.OperState.sound, playList, null, index);
//        }
//        PlayEngineFactory.getEngine().playOrPause(EnumState.OperState.sound);
//        NetManager.getInstance().sendReportData(ReqDataStats.Action.INDEX_SOUND);
//    }
//    /**
//     * 播放选中的专辑
//     */
//    private void doSelectAlbum(Album album) {
//        if (PlayEngineFactory.getEngine().getCurrentAlbum() == null ||
//                PlayEngineFactory.getEngine().getCurrentAlbum().getId() != album
//                        .getId()) {
//            // 取第一个
//            // 保存导数据库
//            DBManager.getInstance().saveAlbum(album);
//            if (CollectionUtils.isNotEmpty(album.getArrCategoryIds())) {
////                RequestHelpe.reqAudio(album, album.getArrCategoryIds().get(0));
//                NetManager.getInstance().requestAudio(album, album.getArrCategoryIds().get(0));
//                NetManager.getInstance().sendReportData(ReqDataStats.Action.INDEX_SOUND);
//            } else {
//                LogUtil.loge(TAG + "[Select][Album]Album's arrCategoryIDs is null ");
//            }
//        }
//    }
//
//
//}
