package com.txznet.music.data.http.api.txz;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.txznet.music.data.entity.AudioV5;
import com.txznet.music.data.http.api.txz.entity.req.Location;
import com.txznet.music.data.http.api.txz.entity.req.TXZReqAudio;
import com.txznet.music.data.http.api.txz.entity.req.TXZReqError;
import com.txznet.music.data.http.api.txz.entity.req.TXZReqFake;
import com.txznet.music.data.http.api.txz.entity.req.TXZReqFavour;
import com.txznet.music.data.http.api.txz.entity.req.TXZReqFavourOperation;
import com.txznet.music.data.http.api.txz.entity.req.TXZReqPageData;
import com.txznet.music.data.http.api.txz.entity.req.TXZReqPlayConf;
import com.txznet.music.data.http.api.txz.entity.req.TXZReqPreProcessing;
import com.txznet.music.data.http.api.txz.entity.req.TXZReqSearch;
import com.txznet.music.data.http.api.txz.entity.resp.PushResponse;
import com.txznet.music.data.http.api.txz.entity.resp.TXZRespAlbum;
import com.txznet.music.data.http.api.txz.entity.resp.TXZRespAudio;
import com.txznet.music.data.http.api.txz.entity.resp.TXZRespCategory;
import com.txznet.music.data.http.api.txz.entity.resp.TXZRespFake;
import com.txznet.music.data.http.api.txz.entity.resp.TXZRespFavour;
import com.txznet.music.data.http.api.txz.entity.resp.TXZRespGetTime;
import com.txznet.music.data.http.api.txz.entity.resp.TXZRespHistory;
import com.txznet.music.data.http.api.txz.entity.resp.TXZRespLyricData;
import com.txznet.music.data.http.api.txz.entity.resp.TXZRespPageData;
import com.txznet.music.data.http.api.txz.entity.resp.TXZRespPlayConf;
import com.txznet.music.data.http.api.txz.entity.resp.TXZRespPreProcessing;
import com.txznet.music.data.http.api.txz.entity.resp.TXZRespSearch;
import com.txznet.music.data.http.api.txz.entity.resp.TXZRespTag;

import io.reactivex.Observable;

public interface TXZMusicApi {

    String GET_PAGE_DATA = "/album/get"; // 获取主页数据
    String GET_CATEGORY = "/category/get";// 首页获取分类数据
    String GET_ALBUM_LIST = "/album/list";// 从分类进入歌单
    String GET_ALBUM_AUDIO = "/album/audio";// 根据歌单获取歌曲数据
    String GET_MUSIC_INTEREST_TAG = "/music/tag";//查询音乐的兴趣标签
    String GET_FM_INTEREST_TAG = "/fm/tag";//查询电台的兴趣标签
    String GET_HISTORY = "/album/history";//查询历史
    String GET_CONFIG = "/conf/check";// 获取后台下发配置项
    String GET_ALBUM_INFO = "/album/info";//获取专辑的信息
    String GET_CAR_FM_CUR = "/fm/SuperFm";// 获取当前时段
    String GET_SEARCH = "/text/search";// 搜索歌曲

    String GET_PROCESSING = "/text/preprocessing";// 预请求
    String GET_FAKE_SEARCH = "/text/fake_request";// 假请求
    String GET_REPORT = "/report/report";// 上报数据给服务器
    String GET_REPORT_ERROR = "/report/abnormal";// 上报错误数据
    String GET_TIME = "/conf/get_time"; //获取服务器时间

    String GET_LYRIC = "/music/lyric"; //获取歌词(需要修改)


    String GET_UPON_FAVOUR = "/report/storeOper"; //上传收藏内容
    String GET_FAVOUR_LIST = "/album/historyStore"; //获取收藏内容

    /**
     * 请求主页数据
     *
     * @param reqPageData 10推荐，20音乐，30电台
     */
    Observable<TXZRespPageData> getPageData(TXZReqPageData reqPageData);

    /**
     * 请求分类
     *
     * @param categoryId 各个类型的子类型, 首页0，音乐1，电台2
     * @param bAll       是否获取该类所有的分类（子类的子类）
     */
    @Deprecated
    Observable<TXZRespCategory> getCategory(int categoryId, boolean bAll);

    /**
     * 获取分类下的专辑
     *
     * @param categoryId 分类id
     * @param pageId     页数
     */
    Observable<TXZRespAlbum> getAlbum(int sid, long categoryId, @IntRange(from = 1) int pageId, Integer offset);

    /**
     * 获取专辑下的歌曲
     */
    Observable<TXZRespAudio> getAudios(@NonNull TXZReqAudio reqAudioParam);

    /**
     * 操作音乐的兴趣标签
     *
     * @param action get和set
     * @param tagIds 当action选择set的时候这个值才有意义
     */
    @Deprecated
    Observable<TXZRespTag> ctrlMusicTag(@NonNull String action, @Nullable String[] tagIds);

    /**
     * 操作电台的兴趣标签
     */
    @Deprecated
    Observable<TXZRespTag> ctrlFmTag(@NonNull String action, @Nullable String[] tagIds);


    /**
     * 获取后台配置项
     */
    Observable<TXZRespPlayConf> getPlayConfig(@NonNull TXZReqPlayConf reqCheck);

    /**
     * 查询历史
     *
     * @param type   1表明小说，0表明全部
     * @param page   页码，起始1
     * @param offset 偏移量
     */
    @Deprecated
    Observable<TXZRespHistory> findHistory(@IntRange(from = 0, to = 1) int type, @IntRange(from = 1) int page, int offset);


    /**
     * 操作电台的兴趣标签
     */
    Observable<TXZRespSearch> findSearch(@NonNull TXZReqSearch reqSearch);

    /**
     * 获取同行者音频的实际播放路径(QQ_Item)
     */
    Observable<TXZRespPreProcessing> getTXZPlayUrl(@NonNull TXZReqPreProcessing reqPreProcessing);

    /**
     * 获取收藏数据
     */
    Observable<TXZRespFavour> getFavours(TXZReqFavour favour);

    Observable<TXZReqFavourOperation> favourAudio(TXZReqFavourOperation favour);

    /**
     * 订阅
     *
     * @param sendData 订阅的集合
     * @return observable对象
     */
    Observable<TXZReqFavourOperation> subscribeAlbum(TXZReqFavourOperation sendData);


    /**
     * 获取订阅数据
     *
     * @return 订阅的请求
     */
    Observable<TXZRespFavour> getSubscribe(TXZReqFavour reqdata);


    /**
     * 获取服务器时间
     */
    Observable<TXZRespGetTime> getSeverTime();

    /**
     * 获取歌词
     */
    Observable<TXZRespLyricData> getLyricData(AudioV5 audioV5);

    /**
     * 获取推送内容
     */
    Observable<PushResponse> getPushData(String url, Location location);


    /**
     * 假请求
     */
    Observable<TXZRespFake> fakeRequest(TXZReqFake reqFake);

    /**
     * 不存在回话关系，可能涉及别的地方同步过来状态
     */
    void getWxLinkQRCode();

    /**
     * 上报错误
     */
    void reportError(TXZReqError txzReqError);
}
