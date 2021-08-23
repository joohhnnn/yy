package com.txznet.comm.ui.theme.test.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.txznet.resholder.R;

/**
 * 说明：单词池
 * <p>
 * 按顺序一个个排，排不完转到下一行
 *
 * @author xiaolin
 * create at 2020-11-04 19:46
 */
public class WordPoolLayout extends ViewGroup {

    private static final String TAG = "WordPoolLayout";

    private String[] mWords = new String[0];
    private int itemHeight;// 子布局高度，这这自动计算

    private int maxLines = 3;// 最多显示多少行
    private int itemLayoutId;// 子布局
    private int itemLayoutTextViewId;// 子布局里TextView的ID
    private int itemVerPadding;// 子布局之间垂直间距
    private int itemHorPadding;// 子布局之间水平间距

    private int mLastWidgetWidth = -1;
    private int mRealHeight = 0;

    public WordPoolLayout(Context context) {
        super(context);
        init(null);
    }

    public WordPoolLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public WordPoolLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.WordPoolLayout);
            itemLayoutId = typedArray.getResourceId(R.styleable.WordPoolLayout_item_layout, 0);
            itemLayoutTextViewId = typedArray.getResourceId(R.styleable.WordPoolLayout_item_text_view_id, 0);
            maxLines = typedArray.getInt(R.styleable.WordPoolLayout_max_lines, 3);
            itemHorPadding = (int) typedArray.getDimension(R.styleable.WordPoolLayout_item_padding_hor, 0);
            itemVerPadding = (int) typedArray.getDimension(R.styleable.WordPoolLayout_item_padding_ver, 0);
            typedArray.recycle();
        }

        View itemView = LayoutInflater.from(getContext()).inflate(itemLayoutId, (ViewGroup) null);
        itemView.measure(0, 0);
        itemHeight = itemView.getMeasuredHeight();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = mRealHeight;
        if (mLastWidgetWidth != w) {// 宽度改变
            mLastWidgetWidth = w;
            removeAllViews();

            int line = 1;// 第几行
            int left = 0;// 左边坐标
            for (String str : mWords) {
                View itemView = LayoutInflater.from(getContext()).inflate(itemLayoutId, (ViewGroup) null);
                TextView tv = itemView.findViewById(itemLayoutTextViewId);
                tv.setText(str);
                itemView.measure(MeasureSpec.makeMeasureSpec(w, MeasureSpec.AT_MOST), 0);

                int itemMeasureWidth = itemView.getMeasuredWidth();

                if (itemMeasureWidth > w) {// 一行放一个词放不下
                    if (left == 0) {// 放在当前行
                        line++;
                    } else {// 放在下一行
                        line += 2;
                    }
                    left = 0;
                } else if (left + itemMeasureWidth > w) {// 放不下,换到下一行
                    left = itemMeasureWidth + itemHorPadding;
                    line++;
                } else {// 当前行能放下
                    left += itemMeasureWidth + itemHorPadding;
                }

                if (line > maxLines) {
                    line = maxLines;
                    break;
                } else {
                    addView(itemView);
                }
            }

            h = line * itemHeight + Math.max(0, line - 1) * itemVerPadding;
            mRealHeight = h;
        }
        setMeasuredDimension(w, h);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();
        int left = 0;
        int top = 0;
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);

            int itemMeasureWidth = childView.getMeasuredWidth();
            if (itemMeasureWidth > getMeasuredWidth()) {// 一行放一个词放不下
                if (left == 0) {
                    childView.layout(0, top, getMeasuredWidth(), top + itemHeight);
                    top = top + itemHeight + itemVerPadding;
                } else {
                    childView.layout(0, top, getMeasuredWidth(), top + itemHeight);
                    top = top + itemHeight + itemVerPadding;
                    top = top + itemHeight + itemVerPadding;
                }
                left = 0;
            } else if (left + itemMeasureWidth > getMeasuredWidth()) {// 放不下
                top = top + itemHeight + itemVerPadding;
                childView.layout(0, top, itemMeasureWidth, top + itemHeight);
                left = itemMeasureWidth + itemHorPadding;
            } else {
                childView.layout(left, top, left + itemMeasureWidth, top + itemHeight);
                left = left + itemMeasureWidth + itemHorPadding;
            }
        }
    }

    public void setWords(String... words) {
        this.mWords = words;
        requestLayout();
    }
}
