package com.txznet.txz.util.focus_supporter.focusfinder;

import android.view.View;

import com.txznet.txz.util.focus_supporter.FocusSupporter;
import com.txznet.txz.util.focus_supporter.wrappers.IFocusWrapper;

/**
 * FocusFinder基类
 * Created by J on 2017/5/22.
 */

public abstract class AbsFocusFinder implements IFocusFinder {
    public abstract Object findNextFocus(Object currentFocus, Object[] focusList, int op);

    @Override
    public Object findFocus(Object currentFocus, Object[] focusList, int op) {
        if (null == currentFocus) {
            return focusList[0];
        }

        // 如果View有设置nextFocusXXXId， 以设置的值优先
        Object userSpecifiedFocus = getUserSpecifiedFocus(currentFocus, focusList, op);
        if (null != userSpecifiedFocus) {
            return userSpecifiedFocus;
        }

        return findNextFocus(currentFocus, focusList, op);
    }

    private Object getUserSpecifiedFocus(Object currentFocus, Object[] focusList, int op) {
        View v = getRealView(currentFocus);
        if (null == v) {
            return null;
        }

        int specifiedId = View.NO_ID;
        switch (op) {
            case FocusSupporter.NAV_BTN_LEFT:
                specifiedId = v.getNextFocusLeftId();
                break;

            case FocusSupporter.NAV_BTN_RIGHT:
                specifiedId = v.getNextFocusRightId();
                break;

            case FocusSupporter.NAV_BTN_UP:
                specifiedId = v.getNextFocusUpId();
                break;

            case FocusSupporter.NAV_BTN_DOWN:
                specifiedId = v.getNextFocusDownId();
                break;
        }

        return getFocusForSpecifiedId(specifiedId, focusList);
    }

    private Object getFocusForSpecifiedId(int id, Object[] focusList) {
        if (View.NO_ID == id) {
            return null;
        }

        for (int i = 0, len = focusList.length; i < len; i++) {
            View v = getRealView(focusList[i]);

            if (null != v && v.getId() == id) {
                return focusList[i];
            }
        }

        return null;
    }

    /**
     * 获取View对象
     *
     * @param object
     * @return
     */
    protected View getRealView(Object object) {
        if (object instanceof View) {
            return (View) object;
        }

        if (object instanceof IFocusWrapper) {
            return ((IFocusWrapper) object).getContent();
        }

        return null;
    }
}
