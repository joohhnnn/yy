package com.txznet.comm.ui.view;

import android.content.Context;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

import com.txznet.comm.remote.util.LogUtil;

public class AutoSplitTextView extends TextView {
    public AutoSplitTextView(Context context) {
        super(context);
    }

    public AutoSplitTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    private boolean mEnabled = true;


    public AutoSplitTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setAutoSplitEnabled(boolean enabled) {
        mEnabled = enabled;
    }
    public boolean onPreDraw() {
        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY
                && MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY
                && getWidth() > 0
                && getHeight() > 0
                && mEnabled) {
            String newText = autoSplitText(this);
            if (!TextUtils.isEmpty(newText)) {
                setText(newText);
            }
            LogUtil.d("skyward getText " + getText());
        } else {
            LogUtil.e("skyward widthMeasureSpec " + MeasureSpec.getMode(widthMeasureSpec));
            LogUtil.e("skyward heightMeasureSpec " + MeasureSpec.getMode(heightMeasureSpec));
            LogUtil.e("skyward getWidth " + getWidth());
            LogUtil.e("skyward getHeight " + getHeight());
            LogUtil.e("skyward mEnabled " + mEnabled);
        }
        unregisterForPreDraw();
        return true;
    }

    private boolean mPreDrawRegistered;
    private boolean mPreDrawListenerDetached;

    private void unregisterForPreDraw() {
        getViewTreeObserver().removeOnPreDrawListener(this);
        mPreDrawRegistered = false;
        mPreDrawListenerDetached = false;
    }
    private void registerForPreDraw() {
        if (!mPreDrawRegistered) {
            getViewTreeObserver().addOnPreDrawListener(this);
            mPreDrawRegistered = true;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mPreDrawListenerDetached) {
            getViewTreeObserver().addOnPreDrawListener(this);
            mPreDrawListenerDetached = false;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mPreDrawRegistered) {
            getViewTreeObserver().removeOnPreDrawListener(this);
            mPreDrawListenerDetached = true;
        }
        super.onDetachedFromWindow();
    }

    private int widthMeasureSpec;
    private int heightMeasureSpec;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        this.widthMeasureSpec = widthMeasureSpec;
        this.heightMeasureSpec = heightMeasureSpec;
        registerForPreDraw();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private String autoSplitText(final TextView tv) {
        final String rawText = tv.getText().toString(); //????????????
        final Paint tvPaint = tv.getPaint(); //paint????????????????????????
        final float tvWidth = tv.getWidth() - tv.getPaddingLeft() - tv.getPaddingRight(); //??????????????????

        //???????????????????????????
        String [] rawTextLines = rawText.replaceAll("\r", "").split("\n");
        StringBuilder sbNewText = new StringBuilder();
        for (String rawTextLine : rawTextLines) {
            if (tvPaint.measureText(rawTextLine) <= tvWidth) {
                //???????????????????????????????????????????????????????????????
                sbNewText.append(rawTextLine);
            } else {
                //????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                float lineWidth = 0;
                for (int cnt = 0; cnt != rawTextLine.length(); ++cnt) {
                    char ch = rawTextLine.charAt(cnt);
                    lineWidth += tvPaint.measureText(String.valueOf(ch));
                    if (lineWidth <= tvWidth) {
                        sbNewText.append(ch);
                    } else {
                        sbNewText.append("\n");
                        lineWidth = 0;
                        --cnt;
                    }
                }
            }
            sbNewText.append("\n");
        }

        //??????????????????\n??????
        if (!rawText.endsWith("\n")) {
            sbNewText.deleteCharAt(sbNewText.length() - 1);
        }

        return sbNewText.toString();
    }
}
