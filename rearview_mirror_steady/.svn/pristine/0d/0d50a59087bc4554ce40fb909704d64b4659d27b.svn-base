package com.txznet.comm.ui.theme.test.view;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.theme.ThemeConfigManager;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.config.StyleConfig;
import com.txznet.comm.ui.theme.test.config.ViewParamsUtil;
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
import com.txznet.txz.util.LanguageConvertor;

import java.util.ArrayList;
import java.util.List;

public class MovieTheaterListView extends IMovieTheaterView {

    private static MovieTheaterListView sInstance = new MovieTheaterListView();

    public static MovieTheaterListView getInstance(){
        return sInstance;
    }

    private List<View> mItemViews;

    private int tvNumSide;    //序号宽高
    private int tvNumHorMargin;    //序号左右边距
    private int tvNumSize;    //序号字体大小
    private int tvNumColor;    //序号字体颜色
    private int tvContentSize;    //内容字体大小
    private int tvContentHeight;    //内容行高
    private int tvContentColor;    //内容字体颜色
    private int centerInterval;    //内容到距离的间距
    private int tvDistanceSize;    //距离字体大小
    private int tvDistanceHeight;    //距离行高
    private int tvDistanceColor;    //距离字体颜色
    private int tvDescSize;    //地址字体大小
    private int tvDescHeight;    //地址行高
    private int tvDescColor;    //地址字体颜色
    private int tvDescLeftMargin;    //地址左边距
    private int dividerHeight;


    @Override
    public void init() {
        super.init();
        tvNumColor = Color.parseColor(LayouUtil.getString("color_main_title"));
        tvContentColor =  Color.parseColor(LayouUtil.getString("color_main_title"));
        tvDistanceColor = Color.parseColor(LayouUtil.getString("color_main_title"));
        tvDescColor = Color.parseColor(LayouUtil.getString("color_vice_title"));
        dividerHeight = (int) Math.ceil(ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_DIVIDER_HEIGHT));
    }

    //切换模式修改布局参数
    public void onUpdateParams(int styleIndex) {
        int unit = ViewParamsUtil.unit;
        tvNumSide = 6 * unit;
        tvNumHorMargin = WinLayout.isVertScreen?unit:2 * unit;
        tvDescLeftMargin = unit;
        tvNumSize = ViewParamsUtil.h0;
        tvContentSize = ViewParamsUtil.h4;
        tvContentHeight = ViewParamsUtil.h4Height;
        tvDistanceSize = ViewParamsUtil.h7;
        tvDistanceHeight = ViewParamsUtil.h7Height;
        tvDescSize = ViewParamsUtil.h6;
        tvDescHeight = ViewParamsUtil.h6Height;
        centerInterval = ViewParamsUtil.centerInterval;
    }

    private MovieTheaterListView(){}

    @Override
    public ViewFactory.ViewAdapter getView(ViewData data) {
        MovieTheaterListViewData movieTheaterListViewData = (MovieTheaterListViewData) data;
        WinLayout.getInstance().vTips = movieTheaterListViewData.vTips;
        View view;
        switch (StyleConfig.getInstance().getSelectStyleIndex()) {
            case StyleConfig.STYLE_ROBOT_FULL_SCREES:
            case StyleConfig.STYLE_ROBOT_HALF_SCREES:
                view = createViewFull(movieTheaterListViewData);
                break;
            case StyleConfig.STYLE_ROBOT_NONE_SCREES:
            default:
                view = createViewNone(movieTheaterListViewData);
                break;
        }
        ViewFactory.ViewAdapter viewAdapter = new ViewFactory.ViewAdapter();
        viewAdapter.type = data.getType();
        viewAdapter.view = view;
        viewAdapter.isListView = true;
        viewAdapter.object = MovieTheaterListView.getInstance();
        return viewAdapter;
    }

    private View createViewFull(MovieTheaterListViewData movieTheaterListViewData){
        ViewFactory.ViewAdapter titleViewAdapter = com.txznet.comm.ui.theme.test.view.ListTitleView.getInstance().getView(movieTheaterListViewData,
                "movie","电影院");
        LinearLayout llLayout = new LinearLayout(GlobalContext.get());
        llLayout.setGravity(Gravity.CENTER_VERTICAL);
        llLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.titleHeight);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        llLayout.addView(titleViewAdapter.view,layoutParams);
        LinearLayout llC = new LinearLayout(GlobalContext.get());
        llC.setOrientation(LinearLayout.HORIZONTAL);
        llC.setBackground(LayouUtil.getDrawable("movie_list_rang_bg"));
        layoutParams = new  LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.pageCount * SizeConfig.itemHeight);
        llLayout.addView(llC,layoutParams);
        LinearLayout llContents = new LinearLayout(GlobalContext.get());
        llContents.setOrientation(LinearLayout.VERTICAL);
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
        return llLayout;
    }

    private View createViewNone(MovieTheaterListViewData movieTheaterListViewData){
        LinearLayout llLayout = new LinearLayout(GlobalContext.get());
        llLayout.setGravity(Gravity.CENTER_VERTICAL);
        llLayout.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout llC = new LinearLayout(GlobalContext.get());
        llC.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new  LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT,1);
        llLayout.addView(llC,layoutParams);

        LinearLayout llPager = new PageView(GlobalContext.get(),movieTheaterListViewData.mTitleInfo.curPage,movieTheaterListViewData.mTitleInfo.maxPage);
        layoutParams = new LinearLayout.LayoutParams(SizeConfig.pageWidth,LinearLayout.LayoutParams.MATCH_PARENT);
        llLayout.addView(llPager,layoutParams);

        ViewFactory.ViewAdapter titleViewAdapter = com.txznet.comm.ui.theme.test.view.ListTitleView.getInstance().getView(movieTheaterListViewData,
                "movie","电影院");
        layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,SizeConfig.titleHeight);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        llC.addView(titleViewAdapter.view,layoutParams);

        View divider = new View(GlobalContext.get());
        divider.setBackground(LayouUtil.getDrawable("line"));
        layoutParams = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,dividerHeight);
        llC.addView(divider, layoutParams);

        LinearLayout llContents = new LinearLayout(GlobalContext.get());
        llContents.setOrientation(LinearLayout.VERTICAL);
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1);
        llC.addView(llContents,layoutParams);

        mItemViews = new ArrayList<View>();
        for (int i = 0; i < movieTheaterListViewData.count; i++) {
            layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    SizeConfig.itemHeight);
            View itemView = createItemView(i,movieTheaterListViewData.getData().get(i),i != SizeConfig.pageCount - 1);
            llContents.addView(itemView, layoutParams);
            mItemViews.add(itemView);
        }

        return llLayout;
    }

    @SuppressLint("NewApi")
    private View createItemView(int position, MovieTheaterListViewData.MovieTheaterBean movieTheaterBean, boolean showDivider){
        Log.d("jack", "createItemView: "+movieTheaterBean.locationType +"   " + movieTheaterBean.cinemaFlag);

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
        LinearLayout.LayoutParams mLayoutParams = new LinearLayout.LayoutParams(tvNumSide, tvNumSide);
        mLayoutParams.leftMargin = tvNumHorMargin;
        mLayoutParams.rightMargin = tvNumHorMargin;
        tvNum.setText(String.valueOf(position + 1));
        llItem.addView(tvNum, mLayoutParams);

        LinearLayout contents = new LinearLayout(GlobalContext.get());
        contents.setOrientation(LinearLayout.VERTICAL);
        contents.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams mRLayoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT,1);
        llItem.addView(contents,mRLayoutParams);

        LinearLayout firstContentS = new LinearLayout(GlobalContext.get());
        firstContentS.setOrientation(LinearLayout.HORIZONTAL);
        mRLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        mRLayoutParams.bottomMargin = centerInterval;
        contents.addView(firstContentS,mRLayoutParams);

        TextView tvCinemaName = new TextView(GlobalContext.get());
        tvCinemaName.setGravity(Gravity.CENTER_VERTICAL);
        tvCinemaName.setSingleLine();
        tvCinemaName.setEllipsize(TextUtils.TruncateAt.END);
        tvCinemaName.setId(ViewUtils.generateViewId());
        tvCinemaName.setSingleLine();
        tvCinemaName.setEllipsize(TextUtils.TruncateAt.END);
        String sCinemaName = movieTheaterBean.cinemaName;
//        if(sCinemaName.length() > 10){
//            sCinemaName = sCinemaName.substring(0,10)+"...";
//        }
        tvCinemaName.setText(sCinemaName);
        LinearLayout.LayoutParams tvLayoutParams = new LinearLayout.LayoutParams(0,tvContentHeight,1);
        firstContentS.addView(tvCinemaName, tvLayoutParams);

//        RelativeLayout rl = new RelativeLayout(GlobalContext.get());
//        RelativeLayout.LayoutParams mRelativeLayout = new  RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
//        mRelativeLayout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//        firstContentS.addView(rl,mRelativeLayout);

        TextView tvLocationType= new TextView(GlobalContext.get());
        tvLocationType.setId(ViewUtils.generateViewId());
        tvLocationType.setGravity(Gravity.CENTER_VERTICAL);
        tvLocationType.setText(movieTheaterBean.locationType);
        tvLocationType.setSingleLine();
        LinearLayout.LayoutParams mRelativeLayout = new  LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        tvLocationType.setPadding(0,0,(int) ViewParamsUtil.getDimen("x4"),0);
        TextViewUtil.setTextColor(tvLocationType, Color.parseColor("#85868B"));
        firstContentS.addView(tvLocationType,mRelativeLayout);

        TextView tvCinemaFlag = new TextView(GlobalContext.get());
        tvCinemaFlag.setGravity(Gravity.CENTER_VERTICAL);
        tvCinemaFlag.setText(movieTheaterBean.cinemaFlag);
        tvCinemaFlag.setSingleLine();
        mRelativeLayout = new  LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        tvCinemaFlag.setPadding(0,0,(int) ViewParamsUtil.getDimen("x4"),0);
        TextViewUtil.setTextColor(tvCinemaFlag, Color.parseColor("#85868B"));
        firstContentS.addView(tvCinemaFlag,mRelativeLayout);

        LinearLayout content = new LinearLayout(GlobalContext.get());
        content.setOrientation(LinearLayout.HORIZONTAL);
        tvLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        contents.addView(content,tvLayoutParams);

        TextView tvDistance = new TextView(GlobalContext.get());
        tvDistance.setId(ViewUtils.generateViewId());
        String sDistance = movieTheaterBean.distance;
        tvDistance.setText(LanguageConvertor.toLocale(sDistance));
        tvDistance.setBackground(LayouUtil.getDrawable("poi_item_distant_bg"));
        tvLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        llLayoutParams.gravity = Gravity.TOP;
        content.addView(tvDistance,tvLayoutParams);

        TextView tvAdress = new TextView(GlobalContext.get());
        tvAdress.setId(ViewUtils.generateViewId());
        tvAdress.setSingleLine();
        tvAdress.setEllipsize(TextUtils.TruncateAt.END);
        String sAdress =  movieTheaterBean.address;
        tvAdress.setText(sAdress);
        tvLayoutParams.rightMargin=(int) ViewParamsUtil.getDimen("m8");
        tvLayoutParams.gravity = Gravity.TOP;
        tvLayoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT,1);
        content.addView(tvAdress,tvLayoutParams);

        View divider = new View(GlobalContext.get());
        divider.setBackground(LayouUtil.getDrawable("line"));
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,dividerHeight);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        divider.setVisibility(showDivider ? View.VISIBLE : View.INVISIBLE);
        itemView.addView(divider, layoutParams);

        TextViewUtil.setTextSize(tvNum,tvNumSize);
        TextViewUtil.setTextColor(tvNum,tvNumColor);
        TextViewUtil.setTextSize(tvCinemaName,tvContentSize);
        TextViewUtil.setTextColor(tvCinemaName,tvContentColor);
        TextViewUtil.setTextSize(tvLocationType,tvDescSize);
        TextViewUtil.setTextColor(tvLocationType,tvDescColor);
        TextViewUtil.setTextSize(tvCinemaFlag,tvDescSize);
        TextViewUtil.setTextColor(tvCinemaFlag,tvDescColor);
        TextViewUtil.setTextSize(tvDistance,tvDistanceSize);
        TextViewUtil.setTextColor(tvDistance,tvDistanceColor);
        TextViewUtil.setTextSize(tvAdress,tvDescSize);
        TextViewUtil.setTextColor(tvAdress,tvDescColor);

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
