package com.txznet.audio.player.queue;

import com.txznet.audio.player.entity.Audio;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

public interface IPlayQueue {
    int REPEAT_MODE_NONE = 0; // 播放完后不会继续播放
    int REPEAT_MODE_ONE = 1; // 单曲循环
    int REPEAT_MODE_ALL = 2; // 列表循环
    int SHUFFLE_MODE_NONE = 0; // 不洗切
    int SHUFFLE_MODE_ALL = 1; // 洗切

    @Retention(RetentionPolicy.SOURCE)
    @interface RepeatMode {
    }

    @Retention(RetentionPolicy.SOURCE)
    @interface ShuffleMode {
    }

    /**
     * 设置播放队列，覆盖方式
     *
     * @param audioList 播放队列
     */
    void setQueue(List<Audio> audioList);

    /**
     * 获取当前播放队列
     * fixme 提供一个镜像代理，对删除插入操作进行监听级联修改乱序列表
     */
    List<Audio> getQueue();

    /**
     * 获取乱序播放列表，随机模式下有用
     */
    List<Audio> getRandomQueue();

    /**
     * 清空播放队列
     */
    void clearQueue();

    /**
     * 获取大小
     */
    int getSize();

    /**
     * 播放队列是否为空
     */
    boolean isEmpty();

    /**
     * 检索指定音频位于播放队列中的索引
     *
     * @param audio 音频
     */
    int indexOf(Audio audio);

    /**
     * 移动队列子项
     *
     * @param from 从该索引处
     * @param to   到此索引
     */
    void moveQueueItem(int from, int to);

    /**
     * 把指定音频插入到队尾
     *
     * @param audio 音频
     */
    void addToQueue(Audio audio);

    /**
     * 把指定音频插入到队列中
     *
     * @param audio    音频
     * @param position 对应索引
     */
    void addToQueue(Audio audio, int position);

    /**
     * 把指定音频队列插入到队列末尾
     *
     * @param audioList 音频队列
     */
    void addToQueue(List<Audio> audioList);

    /**
     * 把制定音频插入到队列中
     *
     * @param audioList 音频队列
     * @param position 对应索引
     */
    void addToQueue(List<Audio> audioList, int position);

    /**
     * 删除指定音频
     *
     * @param audio 音频
     */
    void remove(Audio audio);

    /**
     * 获取指定索引出的音频
     *
     * @param position 索引
     */
    Audio getItem(int position);

    /**
     * 获取下一个音频
     */
    Audio getPreviousItem();

    /**
     * 获取上一个播放的音频索引
     */
    int getPreviousPosition();

    /**
     * 根据当前播放模式获取下一个音频索引
     */
    int getNextPosition();

    /**
     * 获取下一个音频
     */
    Audio getNextItem();

    /**
     * 获取当前队列索引
     */
    int getCurrentPosition();

    /**
     * 设置当前音频项目
     *
     * @param audio 音频
     */
    void setCurrentItem(Audio audio);

    /**
     * 设置当前队列索引
     *
     * @param position 索引
     */
    void setCurrentPosition(int position);

    /**
     * 获取当前索引的音频
     */
    Audio getCurrentItem();

    /**
     * 获取当前队列第一个音频
     */
    Audio getFirstItem();

    /**
     * 获取当前队列最后一个音频
     */
    Audio getLastItem();

    /**
     * 随机获取一个队列中的音频
     */
    Audio getRandomItem();

    /**
     * 挑选队列中的一个音频，若队列为空，则返回null，否则当不洗切时，返回队列首位，洗切时，返回队列任意一个
     */
    Audio pickItem();

    /**
     * 从队列中移除音频
     */
    void removeItem(Audio... audios);

    /**
     * 从队列中移除音频
     */
    void removeItem(List<Audio> audios);

    /**
     * 设置洗切模式
     */
    void setShuffleMode(@ShuffleMode int mode);

    /**
     * 获取洗切模式
     */
    @ShuffleMode
    int getShuffleMode();

    /**
     * 设置重复模式
     */
    void setRepeatMode(@RepeatMode int mode);

    /**
     * 获取重复模式
     */
    @RepeatMode
    int getRepeatMode();
}
