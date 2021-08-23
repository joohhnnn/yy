package com.txznet.audio.player.queue;

import com.txznet.audio.player.entity.Audio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayQueue implements IPlayQueue {

    public interface OnPlayQueueChangeListener {
        void onChanged(List<Audio> audioList);
    }

    private List<Audio> mQueue = new ArrayList<>();
    private List<Audio> mHistory = new ArrayList<>();
    private int mRepeatMode = REPEAT_MODE_ALL;
    private int mShuffleMode = SHUFFLE_MODE_NONE;
    private int mCurrPos = -1;
    private OnPlayQueueChangeListener mOnPlayQueueChangeListener;

    public void setOnPlayQueueChangeListener(OnPlayQueueChangeListener listener) {
        mOnPlayQueueChangeListener = listener;
    }

    @Override
    public synchronized void setQueue(List<Audio> audioList) {
        Audio currItem = getCurrentItem();
        mCurrPos = -1;
        mQueue.clear();
        mHistory.clear();
        mQueue.addAll(audioList);
        if (SHUFFLE_MODE_ALL == mShuffleMode) {
            mHistory.addAll(mQueue);
            doShuffle(mHistory);
        }
        setCurrentItem(currItem);
        if (mOnPlayQueueChangeListener != null) {
            mOnPlayQueueChangeListener.onChanged(mQueue);
        }
    }

    @Override
    public List<Audio> getQueue() {
        return mQueue;
    }

    @Override
    public List<Audio> getRandomQueue() {
        return mHistory;
    }

    @Override
    public synchronized void clearQueue() {
        mQueue.clear();
        mHistory.clear();
        mCurrPos = -1;
        if (mOnPlayQueueChangeListener != null) {
            mOnPlayQueueChangeListener.onChanged(mQueue);
        }
    }

    @Override
    public synchronized int getSize() {
        return mQueue.size();
    }

    @Override
    public boolean isEmpty() {
        return getSize() == 0;
    }

    @Override
    public synchronized int indexOf(Audio audio) {
        return mQueue.indexOf(audio);
    }

    @Override
    public synchronized void moveQueueItem(int from, int to) {
        if (from < 0 || from > getSize() - 1) {
            return;
        }
        if (to < 0 || to > getSize() - 1) {
            return;
        }
        Audio currItem = getCurrentItem();
        Audio audio = mQueue.remove(from);
        if (audio != null) {
            mQueue.add(to, audio);
        }
        setCurrentItem(currItem);
        if (mOnPlayQueueChangeListener != null) {
            mOnPlayQueueChangeListener.onChanged(mQueue);
        }
    }

    @Override
    public synchronized void addToQueue(Audio audio) {
        mQueue.add(audio);
        if (SHUFFLE_MODE_ALL == mShuffleMode) { // 当前是洗切模式
            mHistory.add(audio);
        }
        if (mOnPlayQueueChangeListener != null) {
            mOnPlayQueueChangeListener.onChanged(mQueue);
        }
    }

    @Override
    public synchronized void addToQueue(Audio audio, int position) {
        Audio currItem = getCurrentItem();
        if (position > mQueue.size() - 1) {
            mQueue.add(audio);
        } else {
            mQueue.add(position, audio);
        }
        if (SHUFFLE_MODE_ALL == mShuffleMode) { // 当前是洗切模式
            mHistory.add((int) (Math.random() * getSize()), audio);
        }
        setCurrentItem(currItem);
        if (mOnPlayQueueChangeListener != null) {
            mOnPlayQueueChangeListener.onChanged(mQueue);
        }
    }

    @Override
    public synchronized void addToQueue(List<Audio> audioList) {
        mQueue.addAll(audioList);
        if (SHUFFLE_MODE_ALL == mShuffleMode) { // 当前是洗切模式
            List<Audio> tmp = new ArrayList<>(audioList);
            doShuffle(tmp);
            mHistory.addAll(tmp);
        }
        if (mOnPlayQueueChangeListener != null) {
            mOnPlayQueueChangeListener.onChanged(mQueue);
        }
    }

    @Override
    public void addToQueue(List<Audio> audioList, int position) {
        Audio currItem = getCurrentItem();
        mQueue.addAll(position, audioList);
        if (SHUFFLE_MODE_ALL == mShuffleMode) { // 当前是洗切模式
            List<Audio> tmp = new ArrayList<>(audioList);
            doShuffle(tmp);
            mHistory.addAll(position, tmp);
        }
        setCurrentItem(currItem);
        if (mOnPlayQueueChangeListener != null) {
            mOnPlayQueueChangeListener.onChanged(mQueue);
        }
    }

    @Override
    public synchronized Audio getItem(int position) {
        if (position >= 0 && position < mQueue.size()) {
            return mQueue.get(position);
        } else {
            return null;
        }
    }

    @Override
    public synchronized Audio getPreviousItem() {
        return getItem(getPreviousPosition());
    }

    @Override
    public synchronized int getPreviousPosition() {
        int pos = -1;
        if (SHUFFLE_MODE_ALL == mShuffleMode) { // 随机播放
            if (getCurrentItem() != null) {
                int index = mHistory.indexOf(getCurrentItem()) - 1;
                if (index < 0) {
                    index = mHistory.size() - 1;
                }
                pos = mQueue.indexOf(mHistory.get(index));
            }
            return pos;
        }
        switch (mRepeatMode) {
            case REPEAT_MODE_NONE: // 不循环
                pos = mCurrPos - 1;
                break;
            case REPEAT_MODE_ONE: // 单曲循环，主动切歌规则跟列表循环一致，被动切歌由onCompletion处理
            case REPEAT_MODE_ALL: // 列表循环
                pos = mCurrPos - 1;
                if (pos < 0) {
                    pos = getSize() - 1;
                }
                break;
        }
        return pos;
    }

    @Override
    public synchronized int getNextPosition() {
        int pos = -1;
        if (SHUFFLE_MODE_ALL == mShuffleMode) { // 随机播放
            if (getCurrentItem() == null && mHistory.size() > 0) {
                pos = mQueue.indexOf(mHistory.get(0));
            } else if (getCurrentItem() != null) {
                int index = mHistory.indexOf(getCurrentItem()) + 1;
                pos = mQueue.indexOf(mHistory.get(index % mHistory.size()));
            }
            return pos;
        }

        switch (mRepeatMode) {
            case REPEAT_MODE_NONE: // 不循环
                pos = mCurrPos + 1;
                if (pos > getSize() - 1) {
                    pos = -1;
                }
                break;
            case REPEAT_MODE_ONE: // 单曲循环，主动切歌规则跟列表循环一致，被动切歌由onCompletion处理
            case REPEAT_MODE_ALL: // 列表循环
                pos = mCurrPos + 1;
                if (pos > getSize() - 1) {
                    pos = 0;
                }
                break;
        }
        return pos;
    }

    @Override
    public synchronized Audio getNextItem() {
        return getItem(getNextPosition());
    }

    @Override
    public synchronized int getCurrentPosition() {
        return mCurrPos;
    }

    @Override
    public synchronized void setCurrentItem(Audio audio) {
        int index = mQueue.indexOf(audio);
        if (index != -1) {
            setCurrentPosition(index);
        }
    }

    @Override
    public synchronized void setCurrentPosition(int position) {
        if (position < 0 || position > getSize() - 1) {
            return;
        }
        mCurrPos = position;
    }

    @Override
    public synchronized Audio getCurrentItem() {
        if (mQueue != null && mCurrPos >= 0 && mCurrPos <= getSize() - 1) {
            return mQueue.get(mCurrPos);
        }
        return null;
    }

    @Override
    public synchronized Audio getFirstItem() {
        if (mQueue != null && mQueue.size() > 0) {
            return mQueue.get(0);
        }
        return null;
    }

    @Override
    public synchronized Audio getLastItem() {
        if (mQueue != null && mQueue.size() > 0) {
            return mQueue.get(mQueue.size() - 1);
        }
        return null;
    }

    @Override
    public synchronized Audio getRandomItem() {
        if (mQueue != null && mQueue.size() > 0) {
            return mQueue.get((int) (Math.random() * mQueue.size()));
        }
        return null;
    }

    @Override
    public synchronized Audio pickItem() {
        if (SHUFFLE_MODE_ALL == mShuffleMode) {
            return getRandomItem();
        } else {
            return getFirstItem();
        }
    }

    @Override
    public synchronized void setRepeatMode(@RepeatMode int mode) {
        mRepeatMode = mode;
    }

    @Override
    public @RepeatMode
    int getRepeatMode() {
        return mRepeatMode;
    }

    @Override
    public synchronized void setShuffleMode(@ShuffleMode int mode) {
        mShuffleMode = mode;
        if (SHUFFLE_MODE_ALL == mode) { // 切换到随机模式，生成随机队列
            mHistory.clear();
            mHistory.addAll(mQueue);
            doShuffle(mHistory);
        }
    }

    @Override
    public @ShuffleMode
    int getShuffleMode() {
        return mShuffleMode;
    }


    @Override
    public synchronized void remove(Audio audio) {
        removeItem(audio);
    }

    @Override
    public synchronized void removeItem(Audio... audios) {
        Audio currItem = getCurrentItem();
        for (Audio audio : audios) {
            mQueue.remove(audio);
            if (SHUFFLE_MODE_ALL == mShuffleMode) {
                mHistory.remove(audio);
            }
        }
        if (getQueue().contains(currItem)) {
            setCurrentItem(currItem);
        }
        if (mOnPlayQueueChangeListener != null) {
            mOnPlayQueueChangeListener.onChanged(mQueue);
        }
    }

    @Override
    public synchronized void removeItem(List<Audio> audios) {
        Audio currItem = getCurrentItem();
        mQueue.removeAll(audios);
        if (SHUFFLE_MODE_ALL == mShuffleMode) {
            mHistory.removeAll(audios);
        }
        if (getQueue().contains(currItem)) {
            setCurrentItem(currItem);
        }
        if (mOnPlayQueueChangeListener != null) {
            mOnPlayQueueChangeListener.onChanged(mQueue);
        }
    }

    // 洗切
    private void doShuffle(List<Audio> list) {
        Collections.shuffle(list);
    }

    public static void main(String[] args) {
        Audio audio1 = new Audio();
        audio1.name = "1";

        Audio audio2 = new Audio();
        audio2.name = "2";

        Audio audio3 = new Audio();
        audio3.name = "3";

        Audio audio4 = new Audio();
        audio4.name = "4";

        PlayQueue queue = new PlayQueue();
        queue.addToQueue(audio1);
        queue.addToQueue(audio2);
        queue.addToQueue(audio3);
        queue.addToQueue(audio4);

        queue.setShuffleMode(PlayQueue.SHUFFLE_MODE_ALL);
        queue.setRepeatMode(PlayQueue.REPEAT_MODE_ALL);

        queue.setCurrentItem(queue.pickItem());
        System.out.println("curr play -> " + queue.getCurrentItem());

        queue.setCurrentItem(queue.getNextItem());
        System.out.println("next play -> " + queue.getCurrentItem());

        queue.setCurrentItem(queue.getNextItem());
        System.out.println("next play -> " + queue.getCurrentItem());

        queue.setCurrentItem(queue.getPreviousItem());
        System.out.println("prev play -> " + queue.getCurrentItem());
    }
}
