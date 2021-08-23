package com.txznet.music.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.txznet.music.R;

/**
 * Created by Terry on 2017/4/27.
 */

public class TypeItemLayout extends RelativeLayout {

    /**
     * 正常状态
     */
    public static final int STATE_NORMAL = 1;

    /**
     * 焦点框正在当前位置时的状态
     */
    public static final int STATE_FOCUSED = 2;

    /**
     * 该项被选中但是已经没有焦点框的状态
     */
    public static final int STATE_SELECTED = 3;


    private Context mContext;
    private ImageView mIcon;

    private Drawable mDrawableIconNor;
    private Drawable mDrawableIconSelected;



    public TypeItemLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TypeItemLayout);

        mDrawableIconNor = ta.getDrawable(R.styleable.TypeItemLayout_itemNormal);
        mDrawableIconSelected = ta.getDrawable(R.styleable.TypeItemLayout_itemSelected);
    }



	public void updateState(int state) {
        switch (state) {
            case STATE_NORMAL:
                setBackgroundColor(mContext.getResources().getColor(R.color.home_type_item_bg_nor));
                mIcon.setBackground(mDrawableIconNor);
                break;
            case STATE_FOCUSED:
                setBackgroundColor(mContext.getResources().getColor(R.color.home_type_item_bg_nor));
                mIcon.setBackground(mDrawableIconNor);
                break;
            case STATE_SELECTED:
                setBackgroundColor(mContext.getResources().getColor(R.color.home_type_item_bg_nor));
                mIcon.setBackground(mDrawableIconSelected);
                break;
        }
    }

}
