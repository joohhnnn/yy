package com.txznet.music.ui.base;

import android.support.v4.app.DialogFragment;

import com.txznet.music.Constant;
import com.txznet.music.util.Logger;

import java.util.Stack;

/**
 * 对话框栈
 *
 * @author zackzhou
 * @date 2019/1/3,11:26
 */

public class DialogFragmentStack {
    private Stack<DialogFragment> stack;
    private static DialogFragmentStack sInstance = new DialogFragmentStack();

    private DialogFragmentStack() {
        stack = new Stack<>();
    }

    public static DialogFragmentStack get() {
        return sInstance;
    }

    public void pop(DialogFragment dialogFragment) {
        if (dialogFragment != null) {
            stack.remove(dialogFragment);
            Logger.d(Constant.LOG_TAG_FRAGMENT, "pop->" + dialogFragment.getClass().getSimpleName());
            if (mListener != null) {
                mListener.onStackChanged();
            }
        }
    }

    public void push(DialogFragment dialogFragment) {
        if (dialogFragment != null) {
            stack.add(dialogFragment);
            Logger.d(Constant.LOG_TAG_FRAGMENT, "push->" + dialogFragment.getClass().getSimpleName());
            if (mListener != null) {
                mListener.onStackChanged();
            }
        }
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public void exit() {
        for (int i = 0; i < stack.size(); i++) {
            DialogFragment dialogFragment = stack.get(i);
            stack.remove(i);
            i--;
            if (dialogFragment != null) {
                dialogFragment.dismissAllowingStateLoss();
            }
        }
    }

    //判断一个fragment是否已经在展示，防止展示多个
    public boolean isHadShow(BaseFragment fragment) {
        for (int i = stack.size() - 1; i >= 0; i--) {
            if (stack.get(i).getClass().getSimpleName().equals(fragment.getClass().getSimpleName())) {
                return true;
            }
        }
        return false;
    }

    private OnStackChangeListener mListener;

    public void setOnStackChangeListener(OnStackChangeListener listener) {
        this.mListener = listener;
    }

    public interface OnStackChangeListener {
        void onStackChanged();
    }
}
