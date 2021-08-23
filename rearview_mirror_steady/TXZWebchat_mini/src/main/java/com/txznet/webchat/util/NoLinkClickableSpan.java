package com.txznet.webchat.util;

import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.ClickableSpan;

/**
 * Created by ASUS User on 2015/8/10.
 */
public abstract class NoLinkClickableSpan extends ClickableSpan {

    @Override
    public void updateDrawState(TextPaint ds) {
        ds.setColor(Color.BLACK);
        ds.setUnderlineText(false); //去掉下划线
    }
}
