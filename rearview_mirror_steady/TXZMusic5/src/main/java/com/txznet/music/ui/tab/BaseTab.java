package com.txznet.music.ui.tab;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

/**
 * @author zackzhou
 * @date 2019/1/29,16:25
 */

public abstract class BaseTab {

    public abstract void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder);

    public abstract void onViewAttachedToWindow(boolean includeGlide);

    public abstract void onViewDetachedFromWindow(boolean includeGlide, boolean cancelReq);
}
