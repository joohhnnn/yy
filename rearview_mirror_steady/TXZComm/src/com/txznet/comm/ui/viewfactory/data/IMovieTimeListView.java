package com.txznet.comm.ui.viewfactory.data;

import com.txznet.comm.ui.viewfactory.view.IListView;

public abstract class IMovieTimeListView extends IListView {

    @Override
    public void init(){
        mViewType = ViewData.TYPE_FULL_MOVIE_TIME_LIST;
    }

}
