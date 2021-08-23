package com.txznet.comm.ui.theme.test.view;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.ui.theme.ThemeConfigManager;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ViewUtils;
import com.txznet.comm.ui.view.RippleView;
import com.txznet.comm.ui.viewfactory.ViewFactory;
import com.txznet.comm.ui.viewfactory.data.IMovieTheaterView;
import com.txznet.comm.ui.viewfactory.data.MovieTheaterListViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.defaults.ListTitleView;
import com.txznet.comm.util.TextViewUtil;

import java.util.ArrayList;
import java.util.List;

public class MovieTheaterListView extends IMovieTheaterView {

    private static MovieTheaterListView sInstance = new MovieTheaterListView();

    public static MovieTheaterListView getInstance(){
        return sInstance;
    }

    private List<View> mItemViews;

    private int tvNumWidth;
    private int tvNumHeight;
    private int tvNumMarginLeft;
    private int dividerHeight;


    @Override
    public void init() {
        super.init();
        tvNumWidth = (int)ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_TXTNUM_WIDTH);
        tvNumHeight = (int)ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_TXTNUM_HEIGHT);
        tvNumMarginLeft = (int) ThemeConfigManager.getX(ThemeConfigManager.LIST_ITEM_TXTNUM_MARGINLEFT);
        dividerHeight = (int) Math.ceil(ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_DIVIDER_HEIGHT));

    }

    private MovieTheaterListView(){}

    @Override
    public ViewFactory.ViewAdapter getView(ViewData data) {
            MovieTheaterListViewData movieTheaterListViewData = (MovieTheaterListViewData) data;
            ViewFactory.ViewAdapter titleViewAdapter = com.txznet.comm.ui.theme.test.view.ListTitleView.getInstance().getView(movieTheaterListViewData,
                                            "movie","为你找到如下结果");
            WinLayout.getInstance().vTips = movieTheaterListViewData.vTips;
            LinearLayout llLayout = new LinearLayout(GlobalContext.get());
            llLayout.setGravity(Gravity.CENTER_VERTICAL);
            llLayout.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,ListTitleView.getInstance().getTitleHeight());
            layoutParams.gravity = Gravity.CENTER_VERTICAL;
            llLayout.addView(titleViewAdapter.view,layoutParams);
            LinearLayout llC = new LinearLayout(GlobalContext.get());
            llC.setOrientation(LinearLayout.HORIZONTAL);
            llC.setBackground(LayouUtil.getDrawable("movie_list_rang_bg"));
            layoutParams = new  LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.pageCount * SizeConfig.itemHeight);
            llLayout.addView(llC,layoutParams);
            LinearLayout llContents = new LinearLayout(GlobalContext.get());
            llContents.setOrientation(LinearLayout.VERTICAL);
            //llContents.setBackground(LayouUtil.getDrawable("movie_list_rang_bg"));
            layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
            llC.addView(llContents,layoutParams);
            mItemViews = new ArrayList<View>();
            for (int i = 0; i < movieTheaterListViewData.count; i++) {
                layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        SizeConfig.itemHeight);
                View itemView = createItemView(i,movieTheaterListViewData.getData().get(i),i != SizeConfig.pageCount - 1);
                llContents.addView(itemView, layoutParams);
                mItemViews.add(itemView);
            }
            LinearLayout llPager = new PageView(GlobalContext.get(),movieTheaterListViewData.mTitleInfo.curPage,movieTheaterListViewData.mTitleInfo.maxPage);
            layoutParams = new LinearLayout.LayoutParams(SizeConfig.pageWidth,LinearLayout.LayoutParams.MATCH_PARENT);
            llC.addView(llPager,layoutParams);
            ViewFactory.ViewAdapter viewAdapter = new ViewFactory.ViewAdapter();
            viewAdapter.type = data.getType();
            viewAdapter.view = llLayout;
            viewAdapter.isListView = true;
            viewAdapter.object = MovieTheaterListView.getInstance();
            return viewAdapter;
    }

    @SuppressLint("NewApi")
    private View createItemView(int position, MovieTheaterListViewData.MovieTheaterBean movieTheaterBean, boolean showDivider){

        RippleView itemView = new RippleView(GlobalContext.get());
        LinearLayout llItem = new LinearLayout(GlobalContext.get());
        llItem.setOnClickListener(ListTitleView.getInstance().getOnItemClickListener());
        llItem.setTag(position);
        llItem.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_UP:
                        showSelectItem((int)v.getTag());
                        break;
                }
                return false;
            }
        });
        llItem.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        itemView.addView(llItem, llLayoutParams);
        llItem.setGravity(Gravity.CENTER_VERTICAL);

        TextView tvNum = new TextView(GlobalContext.get());
        tvNum.setId(ViewUtils.generateViewId());
        tvNum.setBackground(LayouUtil.getDrawable("poi_item_circle_bg"));
        tvNum.setGravity(Gravity.CENTER);
        tvNum.setIncludeFontPadding(false);
        LinearLayout.LayoutParams mLayoutParams = new LinearLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.MATCH_PARENT);
        mLayoutParams.leftMargin = tvNumMarginLeft;
        mLayoutParams.rightMargin = tvNumMarginLeft;
        tvNum.setText(String.valueOf(position + 1));
        TextViewUtil.setTextSize(tvNum,(int)LayouUtil.getDimen("h2"));
        llItem.addView(tvNum, mLayoutParams);

        LinearLayout contents = new LinearLayout(GlobalContext.get());
        contents.setOrientation(LinearLayout.VERTICAL);
        contents.setGravity(Gravity.CENTER_VERTICAL);
        RelativeLayout.LayoutParams mRLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        mRLayoutParams.addRule(RelativeLayout.RIGHT_OF, tvNum.getId());
        llItem.addView(contents,mRLayoutParams);

        LinearLayout firstContentS = new LinearLayout(GlobalContext.get());
        firstContentS.setOrientation(LinearLayout.HORIZONTAL);
        mRLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        contents.addView(firstContentS,mRLayoutParams);

        TextView tvCinemaName = new TextView(GlobalContext.get());
        tvCinemaName.setTextColor(Color.WHITE);
       // tvCinemaName.setTextSize(LayouUtil.getDimen("m23"));
        TextViewUtil.setTextSize(tvCinemaName,LayouUtil.getDimen("m23"));
        tvCinemaName.setSingleLine();
        tvCinemaName.setEllipsize(TextUtils.TruncateAt.END);
        tvCinemaName.setId(ViewUtils.generateViewId());
        LinearLayout.LayoutParams tvLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        firstContentS.addView(tvCinemaName, tvLayoutParams);
        tvCinemaName.setSingleLine();
        String sCinemaName = movieTheaterBean.cinemaName;
        if(sCinemaName.length() > 10){
            sCinemaName = sCinemaName.substring(0,10)+"...";
        }
        tvCinemaName.setText(sCinemaName);

        RelativeLayout rl = new RelativeLayout(GlobalContext.get());
        RelativeLayout.LayoutParams mRelativeLayout = new  RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        firstContentS.addView(rl,mRelativeLayout);

        mRelativeLayout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        TextView tvLocationType= new TextView(GlobalContext.get());
        tvLocationType.setId(ViewUtils.generateViewId());
        tvLocationType.setTextColor(Color.WHITE);
        TextViewUtil.setTextSize(tvLocationType,LayouUtil.getDimen("m18"));
        tvLocationType.setText(movieTheaterBean.locationType);
        tvLocationType.setSingleLine();
        mRelativeLayout = new  RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        mRelativeLayout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        mRelativeLayout.addRule(RelativeLayout.ALIGN_BASELINE,tvCinemaName.getId());
        tvLocationType.setPadding(0,0,(int) LayouUtil.getDimen("m4"),0);
        TextViewUtil.setTextColor(tvLocationType, Color.parseColor("#85868B"));
        rl.addView(tvLocationType,mRelativeLayout);
        TextView tvCinemaFlag = new TextView(GlobalContext.get());
        tvCinemaFlag.setTextColor(Color.WHITE);
        TextViewUtil.setTextSize(tvCinemaFlag,LayouUtil.getDimen("m18"));
        tvCinemaFlag.setText(movieTheaterBean.cinemaFlag);
        tvCinemaFlag.setSingleLine();
        mRelativeLayout = new  RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        //mRelativeLayout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        mRelativeLayout.addRule(RelativeLayout.ALIGN_BASELINE,tvCinemaName.getId());
        mRelativeLayout.addRule(RelativeLayout.LEFT_OF,tvLocationType.getId());
        tvCinemaFlag.setPadding(0,0,(int) LayouUtil.getDimen("m4"),0);
        TextViewUtil.setTextColor(tvCinemaFlag, Color.parseColor("#85868B"));
        rl.addView(tvCinemaFlag,mRelativeLayout);
        LinearLayout content = new LinearLayout(GlobalContext.get());
        content.setOrientation(LinearLayout.HORIZONTAL);
        contents.addView(content,tvLayoutParams);

        TextView tvDistance = new TextView(GlobalContext.get());
        tvDistance.setId(ViewUtils.generateViewId());
        tvDistance.setTextColor(Color.WHITE);
        TextViewUtil.setTextSize(tvDistance,LayouUtil.getDimen("m14"));
        String sDistance = movieTheaterBean.distance;
        tvDistance.setText(sDistance);
        tvDistance.setBackground(LayouUtil.getDrawable("cinema_adress_bg"));
        tvDistance.setPadding((int)LayouUtil.getDimen("m7"),
                (int)LayouUtil.getDimen("m2"),
                (int)LayouUtil.getDimen("m7"),
                (int)LayouUtil.getDimen("m2"));
        tvLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        content.addView(tvDistance,tvLayoutParams);

        TextView tvAdress = new TextView(GlobalContext.get());
        tvAdress.setId(ViewUtils.generateViewId());
        TextViewUtil.setTextSize(tvAdress,LayouUtil.getDimen("m18"));
        tvAdress.setSingleLine();
        String sAdress =  movieTheaterBean.address;
        if (sAdress.length() > 10){
            sAdress = sAdress.substring(0,10)+"...";
        }
        tvAdress.setText(sAdress);
        TextViewUtil.setTextColor(tvAdress, Color.parseColor("#85868B"));
        tvLayoutParams.rightMargin=(int) LayouUtil.getDimen("m8");
        content.addView(tvAdress,tvLayoutParams);



        View divider = new View(GlobalContext.get());
        divider.setBackground(LayouUtil.getDrawable("line"));
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,dividerHeight);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        divider.setVisibility(showDivider ? View.VISIBLE : View.INVISIBLE);
        itemView.addView(divider, layoutParams);


        return itemView;
    }

    @Override
    public void updateProgress(int i, int i1) {

    }

    @Override
    public void snapPage(boolean b) {

    }

    @Override
    public void updateItemSelect(int i) {
        showSelectItem(i);
    }

    private void showSelectItem(int index){
        index = index % SizeConfig.pageCount;
        for (int i = 0;i< mItemViews.size();i++){
            if (i == index){
                mItemViews.get(i).setBackground(LayouUtil.getDrawable("item_setlected"));
            }else {
                mItemViews.get(i).setBackground(null);
            }
        }
    }

}
