package com.txznet.music.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

/**
 * https://bbs.csdn.net/topics/392037353
 *
 * @author telen
 * @date 2019/1/19,14:15
 */
public class MyText extends android.support.v7.widget.AppCompatTextView {


    public MyText(Context context) {
        super(context);
    }

    public MyText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //得到Drawable集合  分别对应 左上右下
        Drawable[] drawables = getCompoundDrawables();
        //获取右边图片
        Drawable drawableRight = drawables[2];
        if (drawableRight != null) {
            //获取文字占用长宽
            int textWidth = (int) getPaint().measureText(getText().toString());
            int textHeight = (int) getPaint().getTextSize();
            //获取图片实际长宽
            int drawableWidth = drawableRight.getIntrinsicWidth();
            int drawableHeight = drawableRight.getIntrinsicHeight();
            //setBounds修改Drawable在View所占的位置和大小,对应参数同样的 左上右下()
            drawableRight.setBounds(((textWidth - getWidth()) / 2), (textHeight - drawableHeight) / 2, ((textWidth - getWidth()) / 2) + drawableWidth, (textHeight + drawableHeight) / 2);
        }
        super.onDraw(canvas);
    }
}