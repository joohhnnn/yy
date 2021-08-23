package com.txznet.launcher.domain.help;

import com.txznet.launcher.domain.BaseManager;
import com.txznet.launcher.utils.Conditions;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by daviddai on 2018/8/27
 * 帮助相关指令的处理类
 */
public class HelpManager extends BaseManager {

    private static HelpManager sInstance;


    public static HelpManager getInstance() {
        if (null == sInstance) {
            synchronized (HelpManager.class) {
                if (null == sInstance) {
                    sInstance = new HelpManager();
                }
            }
        }
        return sInstance;
    }

    private List<CommandListener> mListeners = new CopyOnWriteArrayList<>();

    public boolean addMusicInfoChangedListener(CommandListener listener) {
        Conditions.assertMainThread("addMusicInfoChangedListener");
        if (mListeners.contains(listener)) {
            return false;
        }
        return mListeners.add(listener);
    }

    public boolean removeMusicInfoChangedListener(CommandListener listener) {
        Conditions.assertMainThread("removeMusicInfoChangedListener");
        if (!mListeners.contains(listener)) {
            return false;
        }
        return mListeners.remove(listener);
    }

    public void nextPage() {
        for (CommandListener listener : mListeners) {
            listener.nextPage();
        }
    }

    public void prePage() {
        for (CommandListener listener : mListeners) {
            listener.prePage();
        }
    }

    public void finishHelp() {
        for (CommandListener listener : mListeners) {
            listener.finishHelp();
        }
    }

    public interface CommandListener {
        void nextPage();

        void prePage();

        void finishHelp();
    }

}
