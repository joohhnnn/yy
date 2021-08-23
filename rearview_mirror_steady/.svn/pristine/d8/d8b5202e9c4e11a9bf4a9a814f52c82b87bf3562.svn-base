package com.txznet.txz.module.help;

import com.google.gson.Gson;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.ui.win.help.HelpPreferenceUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class JinshouzhiManager implements IHelpTips {
    private static int mForeScene = 0;
    private static int mBkgdScene = 0;
    private static int mTemporaryForeScene = 0;

    public static final int TYPE_HOME = 1;
    public static final int TYPE_MUSIC = 2;
    public static final int TYPE_RADIO = 3;
    public static final int TYPE_NAV = 4;
    public static final int TYPE_WX = 5;
    public static final int TYPE_OTHER = 6;
    public static final String HOME = "HOME";
    public static final String MUSIC = "MUSIC";
    public static final String RADIO = "RADIO";
    public static final String NAV = "NAV";
    public static final String WX = "WX";
    public static final String OTHER = "OTHER";//其它场景

    private static JinshouzhiManager manager = null;
    private Map<String, TipsInfo> mDateMap = new HashMap<String, TipsInfo>();
    private List<String> mWpList = new ArrayList<String>();
    private static final String TAG = "JinshouzhiManager";

    private JinshouzhiManager() {

    }

    public static JinshouzhiManager getInstance() {
        if (manager == null) {
            synchronized (JinshouzhiManager.class) {
                if (manager == null) {
                    manager = new JinshouzhiManager();
                }
            }
        }
        return manager;
    }

    @Override
    public void registerTipsInfo(String msg) {
        Gson gson = new Gson();
        TipsInfo info = gson.fromJson(msg, TipsInfo.class);
        if (mBkgdScene == info.getForeScene()) {
            mBkgdScene = 0;
        }
        mForeScene = info.getForeScene() != 0 ? info.getForeScene() : mForeScene;
        mBkgdScene = info.getBackgroundScene() != 0 ? info.getBackgroundScene() : mBkgdScene;
        mDateMap.remove(getSceneName(info.getForeScene() + info.getBackgroundScene()));
        mDateMap.put(getSceneName(info.getForeScene() + info.getBackgroundScene()), info);
    }

    /**
     * 注册临时的场景
     * @param msg
     */
    @Override
    public void registerTemporaryTipsInfo(String msg) {
        Gson gson = new Gson();
        TipsInfo info = gson.fromJson(msg, TipsInfo.class);
        mTemporaryForeScene = info.getForeScene();
        mDateMap.remove(getSceneName(mTemporaryForeScene));
        mDateMap.put(getSceneName(mTemporaryForeScene), info);
    }

    @Override
    public void unRegisterTipsInfo(String msg) {
        Gson gson = new Gson();
        TipsInfo info = gson.fromJson(msg, TipsInfo.class);
        mDateMap.remove(info.getForeScene() + info.getBackgroundScene());
        if (info.getForeScene() != 0 && info.getForeScene() == mForeScene) {
            mForeScene = 0;
        } else if (info.getBackgroundScene() != 0 && info.getBackgroundScene() == mBkgdScene) {
            mBkgdScene = 0;
        }
    }

    private void refreshScene() {
        // TODO Auto-generated method stub
        int scene = mTemporaryForeScene != 0 ? mTemporaryForeScene : mForeScene;
        if (isShowOneShot()) {
            switch (scene) {
                case TYPE_HOME:
                    homeSceneHighHelp();
                    break;
                case TYPE_MUSIC:
                    musicSceneHighHelp();
                    break;
                case TYPE_RADIO:
                    radioSceneHighHelp();
                case TYPE_NAV:
                    navSceneHighHelp();
                    break;
//                case TYPE_WX:
//                    wxSceneHighHelp(scene);
//                    break;
                case TYPE_OTHER:
                    otherScene();
                    break;
                default:
                    break;
            }
        } else {
            switch (scene) {
                case TYPE_HOME:
                    homeSceneHelp();
                    break;
                case TYPE_MUSIC:
                    musicSceneHelp();
                    break;
                case TYPE_RADIO:
                    radioSceneHelp();
                case TYPE_NAV:
                    navSceneHelp();
                    break;
//                case TYPE_WX:
//                    wxSceneHelp(scene);
//                    break;
                case TYPE_OTHER:
                    otherScene();
                    break;
                default:
                    break;
            }
        }
    }

    private void homeSceneHelp() {
        mWpList.clear();
        if (mBkgdScene != 0) {
            if (mDateMap.get(getSceneName(mBkgdScene)) != null) {
                setRandomWpList(mDateMap.get(getSceneName(mBkgdScene)).getWpLists(), 3);
            }
            if(mDateMap.get(HOME) != null){
                setRandomWpList(mDateMap.get(HOME).getWpLists(), 1);
            }
        } else {
            if(mDateMap.get(HOME) != null){
                setRandomWpList(mDateMap.get(HOME).getWpLists(), 4);
            }
        }
    }

    /**
     * 高级模式下的场景
     */
    private void homeSceneHighHelp() {
        mWpList.clear();
        if (mBkgdScene != 0) {
            if (mDateMap.get(getSceneName(mBkgdScene)) != null) {
                setRandomWpList(mDateMap.get(getSceneName(mBkgdScene)).getWpLists(), 2);
            }
            if (mDateMap.get(HOME) != null) {
                setRandomWpList(mDateMap.get(HOME).getWpLists(), 1);
            }
            setRandomWpList(getOneShotKws(), 1);
        } else {
            if (mDateMap.get(HOME) != null) {
                setRandomWpList(mDateMap.get(HOME).getWpLists(), 3);
            }
            setRandomWpList(getOneShotKws(), 1);
        }
    }


    private void musicSceneHelp() {
        mWpList.clear();
        if (mDateMap.get(MUSIC) != null) {
            setRandomWpList(mDateMap.get(MUSIC).getWpLists(), 3);
        }
        if (mDateMap.get(HOME) != null) {
            setRandomWpList(mDateMap.get(HOME).getWpLists(), 1);
        }
    }

    /**
     * 高级模式下的音乐场景
     */
    private void musicSceneHighHelp() {
        mWpList.clear();
        if (mDateMap.get(MUSIC) != null) {
            setRandomWpList(mDateMap.get(MUSIC).getWpLists(), 2);
        }
        if (mDateMap.get(HOME) != null) {
            setRandomWpList(mDateMap.get(HOME).getWpLists(), 1);
        }
        setRandomWpList(getOneShotKws(), 1);
    }

    private void radioSceneHelp() {
        mWpList.clear();
        if (mDateMap.get(RADIO) != null) {
            setRandomWpList(mDateMap.get(RADIO).getWpLists(), 3);
        }
        if (mDateMap.get(HOME) != null) {
            setRandomWpList(mDateMap.get(HOME).getWpLists(), 1);
        }
    }

    /**
     * 高级模式下的电台场景
     */
    private void radioSceneHighHelp() {
        mWpList.clear();
        if (mDateMap.get(RADIO) != null) {
            setRandomWpList(mDateMap.get(RADIO).getWpLists(), 2);
        }
        if (mDateMap.get(HOME) != null) {
            setRandomWpList(mDateMap.get(HOME).getWpLists(), 1);
        }
        setRandomWpList(getOneShotKws(), 1);
    }

    private void navSceneHelp() {
        mWpList.clear();
        //导航前台
        if (mBkgdScene == 0) {
            if (mDateMap.get(NAV) != null && mDateMap.get(NAV).isNav()) {
                setRandomWpList(mDateMap.get(NAV).getWpLists(), 3);
                if (mDateMap.get(HOME) != null) {
                    setRandomWpList(mDateMap.get(HOME).getWpLists(), 1);
                }
            } else {
                if (mDateMap.get(NAV) != null) {
                    setRandomWpList(mDateMap.get(NAV).getWpLists(), 2);
                }
                if (mDateMap.get(HOME) != null) {
                    setRandomWpList(mDateMap.get(HOME).getWpLists(), 2);
                }
            }
        } else {
            if (mDateMap.get(NAV)!= null && mDateMap.get(NAV).isNav()) {
                if (mDateMap.get(NAV) != null) {
                    setRandomWpList(mDateMap.get(NAV).getWpLists(), 2);
                }
                if (mDateMap.get(getSceneName(mBkgdScene)) != null) {
                    setRandomWpList(mDateMap.get(getSceneName(mBkgdScene)).getWpLists(), 1);
                }
                if (mDateMap.get(HOME) != null) {
                    setRandomWpList(mDateMap.get(HOME).getWpLists(), 1);
                }
            } else {
                if (mDateMap.get(NAV) != null) {
                    setRandomWpList(mDateMap.get(NAV).getWpLists(), 1);
                }
                if (mDateMap.get(getSceneName(mBkgdScene)) != null) {
                    setRandomWpList(mDateMap.get(getSceneName(mBkgdScene)).getWpLists(), 2);
                }
                if (mDateMap.get(HOME) != null) {
                    setRandomWpList(mDateMap.get(HOME).getWpLists(), 1);
                }
            }
        }
    }

    /**
     * 高级模式下的导航场景
     */
    private void navSceneHighHelp() {
        mWpList.clear();
        //导航前台
        if (mBkgdScene == 0) {
            if (mDateMap.get(NAV) != null && mDateMap.get(NAV).isNav()) {
                setRandomWpList(mDateMap.get(NAV).getWpLists(), 3);
                if (mDateMap.get(HOME) != null) {
                    setRandomWpList(getOneShotKws(), 1);
                }
            } else {
                if (mDateMap.get(NAV) != null) {
                    setRandomWpList(mDateMap.get(NAV).getWpLists(), 2);
                }
                if (mDateMap.get(HOME) != null) {
                    setRandomWpList(mDateMap.get(HOME).getWpLists(), 1);
                }
                setRandomWpList(getOneShotKws(), 1);
            }
        } else {
            if (mDateMap.get(NAV) != null && mDateMap.get(NAV).isNav()) {
                setRandomWpList(mDateMap.get(NAV).getWpLists(), 2);
                if (mDateMap.get(getSceneName(mBkgdScene)) != null) {
                    setRandomWpList(mDateMap.get(getSceneName(mBkgdScene)).getWpLists(), 1);
                }
                if (mDateMap.get(HOME) != null) {
                    setRandomWpList(mDateMap.get(HOME).getWpLists(), 1);
                }
            } else {
                if (mDateMap.get(NAV) != null) {
                    setRandomWpList(mDateMap.get(NAV).getWpLists(), 1);
                }
                if (mDateMap.get(getSceneName(mBkgdScene)) != null) {
                    setRandomWpList(mDateMap.get(getSceneName(mBkgdScene)).getWpLists(), 2);
                }
                if (mDateMap.get(HOME) != null) {
                    setRandomWpList(mDateMap.get(HOME).getWpLists(), 1);
                }
            }
        }
    }

    private void otherScene(){
        mWpList.clear();
        if (mDateMap.get(OTHER) != null) {
            setRandomWpList(mDateMap.get(OTHER).getWpLists(), 2);
        }
    }

//    private void wxSceneHelp(int scene) {
//        mWpList.clear();
//        if (mDateMap.get(getSceneName(scene)).isGetWXInfo()) {
//            if(mDateMap.get(getSceneName(scene)) != null){
//                setWpList(mDateMap.get(getSceneName(scene)).getWpLists(), 4);
//            }
//        } else if (mDateMap.get(getSceneName(scene)).isRecording()) {
//            if(mDateMap.get(getSceneName(scene)) != null){
//                setWpList(mDateMap.get(getSceneName(scene)).getWpLists(), 2);
//            }
//        } else if (mBkgdScene == 0) {
//            if(mDateMap.get(HOME) != null){
//                setRandomWpList(mDateMap.get(HOME).getWpLists(), 4);
//            }
//        } else {
//            if (mDateMap.get(getSceneName(mBkgdScene)) != null) {
//                setRandomWpList(mDateMap.get(getSceneName(mBkgdScene)).getWpLists(), 3);
//            }
//            if (mDateMap.get(HOME) != null) {
//                setRandomWpList(mDateMap.get(HOME).getWpLists(), 1);
//            }
//        }
//    }
//
//    /**
//     * 高级模式下的微信场景
//     */
//    private void wxSceneHighHelp(int scene) {
//        mWpList.clear();
//        if (mDateMap.get(getSceneName(scene)).isGetWXInfo()) {
//            if(mDateMap.get(getSceneName(scene)) != null){
//                mDateMap.get(getSceneName(scene)).setGetWXInfo(false);
//                setWpList(mDateMap.get(getSceneName(scene)).getWpLists(), 4);
//            }
//        } else if (mDateMap.get(getSceneName(scene)).isRecording()) {
//            if(mDateMap.get(getSceneName(scene)) != null){
//                setWpList(mDateMap.get(getSceneName(scene)).getWpLists(), 2);
//            }
//        } else if (mBkgdScene == 0) {
//            if (mDateMap.get(HOME) != null) {
//                setRandomWpList(mDateMap.get(HOME).getWpLists(), 3);
//            }
//            setRandomWpList(getOneShotKws(), 1);
//        } else {
//            if (mDateMap.get(getSceneName(mBkgdScene)) != null) {
//                setRandomWpList(mDateMap.get(getSceneName(mBkgdScene)).getWpLists(), 2);
//            }
//            if (mDateMap.get(HOME) != null) {
//                setRandomWpList(mDateMap.get(HOME).getWpLists(), 1);
//            }
//            setRandomWpList(getOneShotKws(), 1);
//        }
//    }

    private void setRandomWpList(List<String> list, int len) {
        List<String> tmpList = new ArrayList<String>();
        tmpList.addAll(list);
        if (tmpList.size() < len) {
            len = tmpList.size();
        }
        for (int i = 0; i < len; i++) {
            int idx = new Random().nextInt(tmpList.size());
            String cmd = tmpList.remove(idx);
            mWpList.add(cmd);
        }
    }

    private void setWpList(List<String> list, int len) {
        List<String> tmpList = new ArrayList<String>();
        tmpList.addAll(list);
        if (tmpList.size() < len) {
            len = tmpList.size();
        }
        for (int i = 0; i < len; i++) {
            mWpList.add(tmpList.get(i));
        }
    }

    public List<String> getWpList() {
        refreshScene();
        return mWpList;
    }

    private String getSceneName(int key) {
        String sceneName = null;
        switch (key) {
            case 1:
                sceneName = HOME;
                break;
            case 2:
                sceneName = MUSIC;
                break;
            case 3:
                sceneName = RADIO;
                break;
            case 4:
                sceneName = NAV;
                break;
            case 5:
                sceneName = WX;
                break;
            case 6:
                sceneName = OTHER;
                break;
            default:
                break;
        }
        return sceneName;
    }

    private boolean isShowOneShot() {
        if (HelpPreferenceUtil.getInstance().getString(HelpPreferenceUtil.KEY_HIGH_TIPS, "0").equals("1") &&
                WakeupManager.getInstance().getWakeupOneShotKws() != null && WakeupManager.getInstance().getWakeupOneShotKws().size() != 0) {
            return true;
        }
        return false;
    }

    private List<String> getOneShotKws() {
        return WakeupManager.getInstance().getWakeupOneShotKws();
    }

    public int getForeScene(){
        return mForeScene;
    }

    /**
     * 把临时前台场景清空
     */
    public void clearTemporaryForeScene(){
        mTemporaryForeScene = 0;
    }
}
