package com.txznet.record.keyevent;

import java.util.List;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.viewfactory.ViewBase;
import com.txznet.comm.ui.viewfactory.view.IListView;
import com.txznet.loader.AppLogicBase;
import com.txznet.record.adapter.ChatDisplayAdapter;
import com.txznet.record.keyevent.KeyEventManagerUI1.FocusSupportListener;
import com.txznet.record.lib.R;
import com.txznet.sdk.TXZRecordWinManager.RecordWin2.RecordWinController;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;


public class KeyEventDispatcherUI1 {

    private List<View> mChatFocusViews; // 聊天内容中需要获取焦点的View，例如POI列表等
    private int mCurChatFocusIndex = -1;
    private boolean mSupportKeyEvent = false; // 默认不支持按键操控

    public static final int KEYCODE_LEFT = KeyEvent.KEYCODE_DPAD_LEFT;
    public static final int KEYCODE_RIGHT = KeyEvent.KEYCODE_DPAD_RIGHT;
    public static final int KEYCODE_UP = KeyEvent.KEYCODE_DPAD_UP;
    public static final int KEYCODE_DOWN = KeyEvent.KEYCODE_DPAD_DOWN;
    public static final int KEYCODE_OK = KeyEvent.KEYCODE_DPAD_CENTER;
    public static final int KEYCODE_BACK = KeyEvent.KEYCODE_BACK;
    public static final int KEYCODE_HOME = KeyEvent.KEYCODE_HOME;

    private ChatDisplayAdapter mChatDisplayAdapter;
    private int mFocusControlMode = 0; // 0:传ViewList进来，通过requestFocus进行更改 1：传ChatDisplayAdapter进来，通过updateFocusView进行更改
    public static final int MODE_VIEW = 0;
    public static final int MODE_ADAPTER = 1;

    private int mLastPageKey = KEYCODE_DOWN; // 翻页时所按的键，当按上键翻页时默认焦点是最后一个，按下键翻页时默认焦点是第一个

    private static KeyEventDispatcherUI1 sInstance = new KeyEventDispatcherUI1();
    private Object mViewOperateLock = new Object();

    private KeyEventDispatcherUI1() {
        KeyEventManagerUI1.getInstance().addFocusSupportListener(new FocusSupportListener() {
            @Override
            public void onStateChanged(boolean support) {

            }
        });
    }

    public static KeyEventDispatcherUI1 getInstance() {
        return sInstance;
    }


    public void onBLEStateChanged(boolean support) {
        if (mSupportKeyEvent != support) {
            mSupportKeyEvent = support;
            synchronized (mViewOperateLock) {
                AppLogicBase.runOnUiGround(new Runnable() {
                    @Override
                    public void run() {
                        if (mSupportKeyEvent) {
                            mCurChatFocusIndex = 0;
                            updateFocus();
                        } else {
                            if (mCurChatFocusIndex != -1) {
                                mCurChatFocusIndex = -1;
                                updateFocus();
                            }
                        }
                    }
                }, 0);
            }
        }
    }

    public void release() {
        if (mChatFocusViews != null) {
            mChatFocusViews.clear();
            mCurChatFocusIndex = 0;
        }
    }

    public void updateFocusViews(List<View> itemViews) {
        LogUtil.logd("[UI1.0] updateFocusViews :" + (itemViews == null ? "null" : itemViews.size()));
        synchronized (mViewOperateLock) {
            mCurChatFocusIndex = -1;
            if (mChatDisplayAdapter != null) {
                mChatDisplayAdapter.setFocusIndex(-1);
            }
            mBgNormal = null;
            mFocusControlMode = MODE_VIEW;
            mChatFocusViews = itemViews;
            if (mChatFocusViews != null && mChatFocusViews.size() > 0) {
                if (!mSupportKeyEvent) {
                    return;
                }
                mCurChatFocusIndex = getFocusIndex();
                updateFocus();
            }
        }
    }

    private Drawable mBgNormal = null;

    /**
     * 更新当前View正常状态下的背景图
     *
     * @param drawableNor
     */
    public void updateNormalBg(Drawable drawableNor) {
        mBgNormal = drawableNor;
    }

    public void updateListAdapter(ChatDisplayAdapter chatDisplayAdapter) {
        synchronized (mViewOperateLock) {
            mCurChatFocusIndex = -1;
            if (mChatFocusViews != null) {
                mChatFocusViews.clear();
            }
            mBgNormal = null;
            mFocusControlMode = MODE_ADAPTER;
            if (mChatDisplayAdapter != null) {
                mChatDisplayAdapter.setFocusIndex(-1);
            }
            mChatDisplayAdapter = chatDisplayAdapter;
            if (mChatDisplayAdapter != null && mChatDisplayAdapter.getCount() > 0) {
                if (!mSupportKeyEvent) {
                    return;
                }
                mCurChatFocusIndex = getFocusIndex();
                updateFocus();
            }
        }
    }


    private boolean mIsTurningPage = false;
    private boolean mIsTurningPre = false;
    private boolean mIsTurningNext = false;

    Runnable mTaskResetTurning = new Runnable() {
        @Override
        public void run() {
            mIsTurningPage = false;
            mIsTurningNext = false;
            mIsTurningPre = false;
        }
    };

    private void tryPageByKeyEvent(boolean isUp) {
        mIsTurningPage = true;
        if (isUp) {
            mIsTurningPre = true;
        } else {
            mIsTurningNext = true;
        }
        AppLogicBase.removeBackGroundCallback(mTaskResetTurning);
        AppLogicBase.runOnBackGround(mTaskResetTurning, 500);
    }


    public int getFocusIndex() {
        if (isTurningPage() && mLastPageKey == KEYCODE_UP && mIsTurningPre) {
            if (mFocusControlMode == MODE_VIEW && mChatFocusViews != null) {
                return mChatFocusViews.size() - 1;
            } else if (mFocusControlMode == MODE_ADAPTER && mChatDisplayAdapter != null) {
                return mChatDisplayAdapter.getCount() - 1;
            }
        }
        return 0;
    }

    /**
     * 是否是翻页导致的内容改变
     */
    public boolean isTurningPage() {
        if (mIsTurningPage) {
            return true;
        }
        return false;
    }


    public void onUpdateProgress(int selection, int value) {
    }

    private void clearProgress() {
        RecordWin2Manager.getInstance().sendEventToCore(RecordWin2Manager.EVENT_EVENT_CLEAR_PROGRESS);
    }

    /**
     * 收到按键事件时
     */
    public boolean onKeyEvent(int keyEvent) {
        LogUtil.logd("[UI1.0] onKeyEvent:" + keyEvent);
        mSupportKeyEvent = true;
        mLastPageKey = KEYCODE_DOWN;
        if (mFocusControlMode == MODE_ADAPTER) {
            if (mChatDisplayAdapter == null || mChatDisplayAdapter.getCount() == 0) {
                return false;
            }
            switch (keyEvent) {
                case KEYCODE_LEFT:
                    break;
                case KEYCODE_RIGHT:
                    break;
                case KEYCODE_UP:
                    if (mCurChatFocusIndex > 0) {
                        mCurChatFocusIndex--;
                    } else if (mCurChatFocusIndex == 0) {
                        mLastPageKey = KEYCODE_UP;
                        RecordWin2Manager.getInstance().operateView(RecordWinController.OPERATE_CLICK,
                                RecordWinController.VIEW_LIST_PREPAGE, 0, 0, RecordWinController.OPERATE_SOURCE_NAVCONTROL);
                        tryPageByKeyEvent(true);
                    } else {
                        mCurChatFocusIndex = mChatDisplayAdapter.getCount() - 1;
                    }
                    clearProgress();
                    updateFocus();
                    return true;
                case KEYCODE_DOWN:
                    if (mCurChatFocusIndex >= 0 && mCurChatFocusIndex < mChatDisplayAdapter.getCount() - 1) {
                        mCurChatFocusIndex++;
                    } else if (mCurChatFocusIndex == mChatDisplayAdapter.getCount() - 1) {
                        mLastPageKey = KEYCODE_DOWN;
                        RecordWin2Manager.getInstance().operateView(RecordWinController.OPERATE_CLICK,
                                RecordWinController.VIEW_LIST_NEXTPAGE, 0, 0, RecordWinController.OPERATE_SOURCE_NAVCONTROL);
                        tryPageByKeyEvent(false);
                    } else {
                        mCurChatFocusIndex = 0;
                    }
                    clearProgress();
                    updateFocus();
                    return true;
                case KEYCODE_OK:
                    if (mCurChatFocusIndex != -1) {
                        RecordWin2Manager.getInstance().operateView(RecordWinController.OPERATE_CLICK,
                                RecordWinController.VIEW_LIST_ITEM, 0, mCurChatFocusIndex, RecordWinController.OPERATE_SOURCE_NAVCONTROL);
                    }
                    return true;
                default:
                    break;
            }
            return false;
        }


        if (mChatFocusViews == null || mChatFocusViews.size() == 0) {
            LogUtil.logd("[UI1.0] onKeyEvent mChatFocusViews null");
            return false;
        }

        if (mCurChatFocusIndex > mChatFocusViews.size()) {
            mCurChatFocusIndex = 0;
        }
        switch (keyEvent) {
            case KEYCODE_LEFT:
                break;
            case KEYCODE_RIGHT:
                break;
            case KEYCODE_UP:
                if (mCurChatFocusIndex > 0) {
                    mCurChatFocusIndex--;
                } else if (mCurChatFocusIndex == 0) {
                    mLastPageKey = KEYCODE_UP;
                    RecordWin2Manager.getInstance().operateView(RecordWinController.OPERATE_CLICK,
                            RecordWinController.VIEW_LIST_PREPAGE, 0, 0, RecordWinController.OPERATE_SOURCE_NAVCONTROL);
                    tryPageByKeyEvent(true);
                } else {
                    mCurChatFocusIndex = mChatFocusViews.size() - 1;
                }
                clearProgress();
                updateFocus();
                return true;
            case KEYCODE_DOWN:
                if (mCurChatFocusIndex >= 0 && mCurChatFocusIndex < mChatFocusViews.size() - 1) {
                    mCurChatFocusIndex++;
                } else if (mCurChatFocusIndex == mChatFocusViews.size() - 1) {
                    mLastPageKey = KEYCODE_DOWN;
                    RecordWin2Manager.getInstance().operateView(RecordWinController.OPERATE_CLICK,
                            RecordWinController.VIEW_LIST_NEXTPAGE, 0, 0, RecordWinController.OPERATE_SOURCE_NAVCONTROL);
                    tryPageByKeyEvent(false);
                } else {
                    mCurChatFocusIndex = 0;
                }
                clearProgress();
                updateFocus();
                return true;
            case KEYCODE_OK:
                if (mCurChatFocusIndex != -1) {
                    RecordWin2Manager.getInstance().operateView(RecordWinController.OPERATE_CLICK,
                            RecordWinController.VIEW_LIST_ITEM, 0, mCurChatFocusIndex, RecordWinController.OPERATE_SOURCE_NAVCONTROL);
                }
                return true;
            default:
                break;
        }
        return false;
    }


    private void clearAllFocus() {
        synchronized (mViewOperateLock) {
            if (mChatFocusViews == null) {
                return;
            }
            for (int i = 0; i < mChatFocusViews.size(); i++) {
                mChatFocusViews.get(i).setFocusable(false);
                mChatFocusViews.get(i).setFocusableInTouchMode(false);
            }
        }
    }

    private void updateFocus() {
        LogUtil.logd("[UI1.0] onKeyEvent updateFocus:" + mCurChatFocusIndex);
        synchronized (mViewOperateLock) {
            if (mFocusControlMode == MODE_ADAPTER) {
                if (mChatDisplayAdapter != null) {
                    mChatDisplayAdapter.setFocusIndex(mCurChatFocusIndex);
                    mChatDisplayAdapter.notifyDataSetChanged();
                    return;
                }
                LogUtil.loge("[UI1.0] mode is adapter control but adapter is null!");
            } else {
                if (mChatFocusViews == null || mChatFocusViews.size() < mCurChatFocusIndex) {
                    LogUtil.loge("[UI1.0] update focus failed mChatFocusViews is null");
                    return;
                }
                for (int i = 0; i < mChatFocusViews.size(); i++) {
                    View view = mChatFocusViews.get(i);
                    if (view == null) {
                        continue;
                    }
                    if (i != mCurChatFocusIndex) {
                        if (mBgNormal != null) {
                            view.setBackground(mBgNormal);
                        } else {
                            view.setBackgroundColor(Color.TRANSPARENT);
                        }
                    } else {
                        view.setBackgroundColor(Color.parseColor("#4AA5FA"));
                    }
                }
            }
        }
    }

}
