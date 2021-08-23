package com.txznet.music.data.source;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import com.txznet.music.data.entity.Album;
import com.txznet.music.data.entity.AudioV5;
import com.txznet.music.data.entity.Category;
import com.txznet.music.data.entity.MusicPageData;
import com.txznet.music.data.entity.PlayConfs;
import com.txznet.music.data.entity.PlayUrlInfo;
import com.txznet.music.data.entity.RadioPageData;
import com.txznet.music.data.entity.RecommendPageData;
import com.txznet.music.data.entity.SearchResult;

import java.util.List;

import io.reactivex.Observable;

/**
 * 公共乐库接口
 */
public interface IMusicDataSource {

    /**
     * 获取主页-推荐数据
     */
    Observable<RecommendPageData> getRecommendPageData();

    /**
     * 获取主页-音乐数据
     */
    Observable<MusicPageData> getMusicPageData();

    /**
     * 获取主页-电台数据
     */
    Observable<RadioPageData> getRadioPageData();

    /**
     * 获取乐库下的所有音乐分类
     */
    @Deprecated
    Observable<List<Category>> listMusicCategory();

    /**
     * 获取乐库下所有的电台分类
     */
    @Deprecated
    Observable<List<Category>> listRadioCategory();

    /**
     * 获取分类下的专辑
     *
     * @param sid      sid
     * @param category 分类
     * @param pageId   页数 - 如果不支持分页，则pageId>1时，返回空
     * @param pagesize 页码
     */
    Observable<List<Album>> listAlbum(int sid, Category category, @IntRange(from = 1) int pageId, int pagesize);

    /**
     * 搜索
     *
     * @param keywordJson 搜索的关键字，此处采用的是json进行传递
     * @param searchType  搜索的类型 默认为全部搜索
     */
    Observable<SearchResult> listSearch(String keywordJson, int searchType);


    /**
     * 获取配置项
     */
    Observable<PlayConfs> getPlayConf();


    /**
     * 列出专辑下的音频
     *
     * @param album    专辑
     * @param audioId  从这个id音频之后开始拉取,同行者是direction(拉取方向,拉取方式 默认 0 向下 1 向上)-sid-id 的格式，其他来源的格式可以自定义
     * @param pagesize 请求个数
     * @return 可取消的请求
     */
    Observable<List<AudioV5>> listAudios(Album album, AudioV5 audioV5, String audioId, int pagesize);

    /**
     * 列出专辑下的音频
     *
     * @param album     专辑
     * @param audioId   从这个id音频之后开始拉取,同行者是direction(拉取方向,拉取方式 默认 0 向下 1 向上)-sid-id 的格式，其他来源的格式可以自定义
     * @param pagesize  请求个数
     * @param fromFirst 从第一个音频开始返回
     * @return 可取消的请求
     */
    Observable<List<AudioV5>> listAudios(Album album, AudioV5 audioV5, String audioId, int pagesize, boolean fromFirst);

    /**
     * 获取音频的播放地址
     */
    Observable<PlayUrlInfo> getAudioPlayUrls(@NonNull AudioV5 audio);

    /**
     * 获取服务器时间戳
     */
    Observable<Long> getServerTime();

    /**
     * 获取歌词
     */
    Observable<String> getLyric(@NonNull AudioV5 audio);
}
