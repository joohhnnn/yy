package com.txznet.txz.util.focus_supporter.focusfinder;

/**
 * FocusFinder接口
 * 通过实现此接口来定制焦点切换的规则
 * Created by J on 2017/4/25.
 */

public interface IFocusFinder {
    /**
     * 返回对应指定操作的下一个焦点
     * @param currentFocus 当前焦点
     * @param focusList 查找焦点的范围，不应返回该List外的对象
     * @param op 方控操作
     * @return 下个焦点
     */
    Object findFocus(Object currentFocus, Object[] focusList, int op);
}
