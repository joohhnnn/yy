package com.txznet.txz.util.focus_supporter.focusfinder;

import com.txznet.txz.util.focus_supporter.FocusSupporter;
import com.txznet.txz.util.focus_supporter.interfaces.IFocusView;
import com.txznet.txz.util.focus_supporter.log.FocusLog;
import com.txznet.txz.util.focus_supporter.wrappers.IFocusWrapper;

import java.util.HashMap;

/**
 * 用于管理FocusViews
 * 提供指定ViewList内焦点的管理、切换等支持
 * Created by J on 2017/4/21.
 */

public class FocusManager {
    // rules map内的position
    private static final int POS_LEFT = 0;
    private static final int POS_UP = 1;
    private static final int POS_RIGHT = 2;
    private static final int POS_DOWN = 3;
    private static final int POS_PREV = 4;
    private static final int POS_NEXT = 5;

    private Object mCurrentFocus;
    private Object[] mFocusList;
    private IFocusFinder mFocusFinder;

    // 焦点rules map
    private HashMap<Object, Object[]> mMapRules = new HashMap<Object, Object[]>();

    public FocusManager(int mode) {
        if (FocusSupporter.NAV_MODE_TWO_WAY == mode) {
            mFocusFinder = new ComplexFocusFinder();
        } else {
            mFocusFinder = new LinearFocusFinder();
        }

    }

    public void recycle() {
        mCurrentFocus = null;
        mFocusList = null;
        mMapRules.clear();
    }

    /**
     * 执行对应的方控操作(上下左右前进后退)
     *
     * @param op 操作码
     * @return 新焦点
     */
    public Object performOperation(int op) {
        if (null == mFocusList || 0 == mFocusList.length) {
            return null;
        }

        if (op < 1017 || op > 1022) {
            FocusLog.e("performOperation: operation not supported: " + op);
            return mCurrentFocus;
        }

        // 优先判断rulesMap中是否有对应的记录，如果有以记录优先
        Object[] arrRules = mMapRules.get(mCurrentFocus);
        if (null != arrRules) {
            Object nextFocus = arrRules[op2RulesMapPosition(op)];

            if (null != nextFocus) {
                changeFocus(mCurrentFocus, nextFocus, op);
                return nextFocus;
            }
        }

        Object newFocus = mFocusFinder.findFocus(mCurrentFocus, mFocusList, op);
        if (null == newFocus) {
            FocusLog.e("performOperation: can't find proper focus in specified op");
            return mCurrentFocus;
        }

        changeFocus(mCurrentFocus, newFocus, op);

        return newFocus;
    }

    private void changeFocus(Object rawFocus, Object newFocus, int op) {
        if (mCurrentFocus == newFocus) {
            FocusLog.d("changeFocus: focus not changed");
            return;
        }

        mCurrentFocus = newFocus;

        // notify focus change
        notifyLoseNavFocus(rawFocus, newFocus, op);
        notifyGainNavFocus(newFocus, rawFocus, op);
    }

    private void notifyLoseNavFocus(Object target, Object current, int operation) {
        if (null == target) {
            return;
        }

        if (target instanceof IFocusView) {
            ((IFocusView) target).onNavLoseFocus(current, operation);
        }
    }

    private void notifyGainNavFocus(Object target, Object raw, int operation) {
        if (null == target) {
            return;
        }

        if (target instanceof IFocusView) {
            ((IFocusView) target).onNavGainFocus(raw, operation);
        }
    }

    /**
     * 获取当前焦点
     *
     * @return 当前焦点，可能为NUll
     */
    public Object getCurrentFocus() {
        return mCurrentFocus;
    }

    /**
     * 判断目标是否在方控焦点
     * <p>
     * 会忽略Wrapper带来的影响
     * e.g. 当前焦点为A，isOnFocus(new SimpleDrawableWrapper(A, ....)) 会返回true
     *
     * @param target 目标（可以是View/wrapper等）
     * @return target与当前焦点是否"相同"
     */
    public boolean isOnFocus(Object target) {
        return getContentForFocus(mCurrentFocus) == getContentForFocus(target);
    }

    /**
     * 设置当前焦点View
     *
     * @param newFocus 新焦点， 设置NULL会清除当前焦点
     * @return 是否设置成功, 新焦点不在当前focus list中时会返回false
     */
    public boolean setCurrentFocus(Object newFocus) {
        if (null == newFocus) {
            changeFocus(mCurrentFocus, null, FocusSupporter.NAV_BTN_NONE);
            return true;
        }

        // 检查focus list中是否存在newFocus
        int index = getFocusIndex(newFocus);
        if (-1 != index) {
            changeFocus(mCurrentFocus, mFocusList[index], FocusSupporter.NAV_BTN_NONE);
            return true;
        }

        return false;
    }

    private int getFocusIndex(Object target) {
        for (int i = 0, len = mFocusList.length; i < len; i++) {
            if (getContentForFocus(mFocusList[i]) == getContentForFocus(target)) {
                return i;
            }
        }

        return -1;
    }


    public void setFocusList(Object... list) {
        mFocusList = list;

        // 若当前有焦点存在，尝试保存焦点
        if (null != mCurrentFocus) {
            int index = getFocusIndex(mCurrentFocus);

            if (-1 == index) {
                changeFocus(mCurrentFocus, null, FocusSupporter.NAV_BTN_NONE);
            }
        }

        mMapRules.clear();
    }

    private Object getContentForFocus(Object focus) {
        if (focus instanceof IFocusWrapper) {
            return ((IFocusWrapper) focus).getContent();
        }

        return focus;
    }

    public boolean addRule(Object target, Object next, int op) {
        int tarIndex = getFocusIndex(target);
        int nextIndex = getFocusIndex(next);
        int mapPos = op2RulesMapPosition(op);

        // target和next必须都在FocusList中，否则设置无效
        if (-1 == tarIndex || -1 == nextIndex || -1 == mapPos) {
            return false;
        }

        if (null == mMapRules.get(mFocusList[tarIndex])) {
            mMapRules.put(mFocusList[tarIndex], new Object[]{null, null, null, null, null, null});
        }

        Object[] arrRules = mMapRules.get(mFocusList[tarIndex]);
        arrRules[mapPos] = mFocusList[nextIndex];
        mMapRules.put(mFocusList[tarIndex], arrRules);

        return true;
    }

    public void removeRule(Object target, int... opList) {
        int tarIndex = getFocusIndex(target);

        if (null == mMapRules.get(mFocusList[tarIndex])) {
            return;
        }

        if (opList == null || 0 == opList.length) {
            mMapRules.put(mFocusList[tarIndex], new Object[]{null, null, null, null, null, null});
            return;
        }

        Object[] arrRules = mMapRules.get(mFocusList[tarIndex]);
        for (int op : opList) {
            arrRules[op2RulesMapPosition(op)] = null;
        }

        mMapRules.put(mFocusList[tarIndex], arrRules);
    }

    private int op2RulesMapPosition(int op) {
        switch (op) {
            case FocusSupporter.NAV_BTN_LEFT:
                return POS_LEFT;

            case FocusSupporter.NAV_BTN_UP:
                return POS_UP;

            case FocusSupporter.NAV_BTN_RIGHT:
                return POS_RIGHT;

            case FocusSupporter.NAV_BTN_DOWN:
                return POS_DOWN;

            case FocusSupporter.NAV_BTN_PREV:
                return POS_PREV;

            case FocusSupporter.NAV_BTN_NEXT:
                return POS_NEXT;
        }

        return -1;
    }
}
