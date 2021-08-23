package com.txznet.txz.util.focus_supporter.helper;

import com.txznet.txz.util.focus_supporter.FocusSupporter;
import com.txznet.txz.util.focus_supporter.interfaces.IFocusOperationPresenter;
import com.txznet.txz.util.focus_supporter.interfaces.IFocusView;
import com.txznet.txz.util.focus_supporter.log.FocusLog;

/**
 * 用于支援List焦点控制的Helper
 * <p>
 * Created by J on 2017/5/7.
 */

public abstract class ListFocusHelper implements IFocusView, IFocusOperationPresenter {
    /**
     * 无效的焦点位置
     */
    public static final int FOCUS_POSITION_INVALID = -1;
    /**
     * 标识水平方向优先的Layout(列数固定，向垂直方向延伸)
     * 0 1 2 3
     * 4 5 6 7
     * . . . .
     * . . . .
     */
    public static final int LAYOUT_ORIENTATION_VERTICAL = 0x01;
    /**
     * 标识垂直方向优先的Layout(行数固定，向水平方向延伸)
     * 0 3 6 . .
     * 1 4 7 . .
     * 2 5 8 . .
     */
    public static final int LAYOUT_ORIENTATION_HORIZONTAL = 0x10;

    private int mNumColumn; // 列数
    private int mNumRow; // 行数
    private int mLayoutOrientation; // List的滑动方向

    private int mFocusPosition = FOCUS_POSITION_INVALID; // 当前焦点Position

    /**
     * 获取当前Item总数
     *
     * @return Item总数
     */
    protected abstract int getItemCount();

    /**
     * 通知List刷新
     */
    protected abstract void notifyDatasetChanged();

    /**
     * 方控的点击事件
     *
     * @param position
     */
    protected abstract boolean onFocusClick(int position);

    /**
     * 方控的长按事件
     *
     * @param position
     */
    protected abstract boolean onFocusLongClick(int position);

    /**
     * 方控返回按键
     */
    protected abstract boolean onFocusReturn();

    /**
     * --Constructor
     * <p>
     * note: 对于只允许一个方向上滑动的List，rowCount或columnCount会被忽略, 填入任意值即可（建议填0）
     *
     * @param rowCount    行数
     * @param columnCount 列数
     * @param orientation layout方向
     */
    public ListFocusHelper(int rowCount, int columnCount, int orientation) {
        this.mNumColumn = columnCount;
        this.mNumRow = rowCount;
        this.mLayoutOrientation = orientation;
    }

    public boolean isOnFocus(final int position) {
        return position == mFocusPosition;
    }

    public int getCurrentFocusPosition() {
        return mFocusPosition;
    }

    public boolean setCurrentFocusPosition(int position) {
        if (position >= -1 && position < getItemCount()) {
            mFocusPosition = position;
            notifyDatasetChanged();

            return true;
        }

        return false;
    }

    protected int getNextFocusPosition(int position, int op) {
        int result = position;

        // next和prev操作不用考虑行列排布的影响，直接处理
        if (FocusSupporter.NAV_BTN_NEXT == op) {
            result++;
        } else if (FocusSupporter.NAV_BTN_PREV == op) {
            result--;
        } else {
            // left/right/up/down操作需要通过行列计算处理
            result = getNextPositionFormOp(position, op);
        }

        return result;
    }

    private int getNextPositionFormOp(int position, int op) {
        int curRow, curColumn;
        int rowCount, columnCount;

        if (LAYOUT_ORIENTATION_VERTICAL == mLayoutOrientation) {
            curRow = position / mNumColumn;
            curColumn = position % mNumColumn;
            rowCount = (getItemCount() + mNumColumn - 1) / mNumColumn;
            columnCount = mNumColumn;
        } else {
            curRow = position % mNumRow;
            curColumn = position / mNumRow;
            rowCount = mNumRow;
            columnCount = (getItemCount() + mNumRow - 1) / mNumRow;
        }

        switch (op) {
            case FocusSupporter.NAV_BTN_LEFT:
                curColumn--;
                break;

            case FocusSupporter.NAV_BTN_RIGHT:
                curColumn++;
                break;

            case FocusSupporter.NAV_BTN_UP:
                curRow--;
                break;

            case FocusSupporter.NAV_BTN_DOWN:
                curRow++;
                break;
        }

        if (curRow >= 0 && curRow < rowCount && curColumn >= 0 && curColumn < columnCount) {
            return getPositionForRowAndColumn(curRow, curColumn);
        }

        return FOCUS_POSITION_INVALID;
    }

    private int getPositionForRowAndColumn(int row, int column) {
        if (LAYOUT_ORIENTATION_VERTICAL == mLayoutOrientation) {
            return row * mNumColumn + column;
        } else {
            return column * mNumRow + row;
        }
    }

    @Override
    public boolean showDefaultSelectIndicator() {
        return false;
    }

    @Override
    public void onNavGainFocus(Object rawFocus, int operation) {
        mFocusPosition = 0;
        notifyDatasetChanged();
    }

    @Override
    public void onNavLoseFocus(Object newFocus, int operation) {
        mFocusPosition = FOCUS_POSITION_INVALID;
        notifyDatasetChanged();
    }

    @Override
    public boolean onNavOperation(int operation) {
        if (FocusSupporter.NAV_BTN_CLICK == operation) {
            return onFocusClick(mFocusPosition);
        }

        if (FocusSupporter.NAV_BTN_LONG_CLICK == operation) {
            return onFocusLongClick(mFocusPosition);
        }

        if (FocusSupporter.NAV_BTN_BACK == operation) {
            return onFocusReturn();
        }

        int nextFocusPosition = getNextFocusPosition(mFocusPosition, operation);
        FocusLog.i("operation = " + operation + ", nextPosition = " + nextFocusPosition);

        if (nextFocusPosition >= 0 && nextFocusPosition < getItemCount()) {
            mFocusPosition = nextFocusPosition;
            notifyDatasetChanged();
            return true;
        }

        return false;
    }
}
