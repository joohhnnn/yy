package com.txznet.comm.ui.viewfactory.view.defaults;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.ui.theme.ThemeConfigManager;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ViewUtils;
import com.txznet.comm.ui.view.RippleView;
import com.txznet.comm.ui.viewfactory.ViewFactory;
import com.txznet.comm.ui.viewfactory.data.IMovieTheaterView;
import com.txznet.comm.ui.viewfactory.data.MovieTheaterListViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.util.TextViewUtil;

public class DefaultMovieTheaterListView extends IMovieTheaterView {

    private static DefaultMovieTheaterListView sInstance = new DefaultMovieTheaterListView();

    public static DefaultMovieTheaterListView getInstance(){
        return sInstance;
    }

    private int tvNumWidth;
    private int tvNumHeight;
    private int tvNumMarginLeft;
    private int dividerHeight;

    @Override
    public void init() {
        super.init();
    }

    private DefaultMovieTheaterListView(){}

    @SuppressLint("NewApi")
    @Override
    public ViewFactory.ViewAdapter getView(ViewData data) {
        MovieTheaterListViewData movieTheaterListViewData = (MovieTheaterListViewData) data;
        ViewFactory.ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(movieTheaterListViewData);
        LinearLayout llLayout = new LinearLayout(GlobalContext.get());
        llLayout.setGravity(Gravity.CENTER_VERTICAL);
        llLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,ListTitleView.getInstance().getTitleHeight());
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        llLayout.addView(titleViewAdapter.view,layoutParams);
        LinearLayout llContents = new LinearLayout(GlobalContext.get());
        llContents.setOrientation(LinearLayout.VERTICAL);
        llContents.setBackground(LayouUtil.getDrawable("movie_list_rang_bg"));
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ConfigUtil.getDisplayLvItemH(false) * ConfigUtil.getVisbileCount());
        llLayout.addView(llContents,layoutParams);
        for (int i = 0; i < movieTheaterListViewData.count; i++) {
            layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,ConfigUtil.getDisplayLvItemH(false));
            View itemView = createItemView(i,movieTheaterListViewData.getData().get(i),i != ConfigUtil.getVisbileCount() - 1);
            llContents.addView(itemView, layoutParams);
        }

        ViewFactory.ViewAdapter viewAdapter = new ViewFactory.ViewAdapter();
        viewAdapter.type = data.getType();
        viewAdapter.view = llLayout;
        viewAdapter.isListView = true;
        viewAdapter.object = DefaultMovieTheaterListView.getInstance();
        return viewAdapter;
    }

    @SuppressLint("NewApi")
    public View createItemView(int position, MovieTheaterListViewData.MovieTheaterBean movieTheaterBean, boolean showDivider){
        tvNumWidth = (int) ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_TXTNUM_WIDTH);
        tvNumHeight = (int)ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_TXTNUM_HEIGHT);
        if (tvNumWidth == 0) {
            tvNumWidth = (int) LayouUtil.getDimen("y44");
        }
        if (tvNumHeight == 0) {
            tvNumHeight = (int) LayouUtil.getDimen("y44");
        }
        tvNumMarginLeft = (int) ThemeConfigManager.getX(ThemeConfigManager.LIST_ITEM_TXTNUM_MARGINLEFT);
        dividerHeight = (int) Math.ceil(ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_DIVIDER_HEIGHT));
        RippleView itemView = new RippleView(GlobalContext.get());
        LinearLayout llItem = new LinearLayout(GlobalContext.get());
        llItem.setOnClickListener(ListTitleView.getInstance().getOnItemClickListener());
        llItem.setTag(position);
        llItem.setOnTouchListener(ListTitleView.getInstance().getOnTouchListener());
        llItem.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        itemView.addView(llItem, llLayoutParams);
        llItem.setGravity(Gravity.CENTER_VERTICAL);

        TextView tvNum = new TextView(GlobalContext.get());
        tvNum.setId(ViewUtils.generateViewId());
        tvNum.setBackground(LayouUtil.getDrawable("poi_item_circle_bg"));
        tvNum.setGravity(Gravity.CENTER);
        tvNum.setIncludeFontPadding(false);
        LinearLayout.LayoutParams mLayoutParams = new LinearLayout.LayoutParams(tvNumWidth,tvNumHeight);
        mLayoutParams.leftMargin = tvNumMarginLeft;
        mLayoutParams.rightMargin = tvNumMarginLeft;
        tvNum.setText(String.valueOf(position + 1));
        TextViewUtil.setTextSize(tvNum,(int)LayouUtil.getDimen("m28"));
        llItem.addView(tvNum, mLayoutParams);

        LinearLayout contents = new LinearLayout(GlobalContext.get());
        contents.setOrientation(LinearLayout.VERTICAL);
        contents.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams mRLayoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT,1);
        llItem.addView(contents,mRLayoutParams);

        LinearLayout firstContentS = new LinearLayout(GlobalContext.get());
        firstContentS.setOrientation(LinearLayout.HORIZONTAL);
        mRLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        contents.addView(firstContentS,mRLayoutParams);

        TextView tvCinemaName = new TextView(GlobalContext.get());
//        tvCinemaName.setTextColor(Color.WHITE);
        // tvCinemaName.setTextSize(LayouUtil.getDimen("m23"));
        TextViewUtil.setTextSize(tvCinemaName,LayouUtil.getDimen("m23"));
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
        TextViewUtil.setTextColor(tvCinemaName, Color.WHITE);
        LinearLayout.LayoutParams tvLayoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1);
        firstContentS.addView(tvCinemaName, tvLayoutParams);

        TextView tvLocationType= new TextView(GlobalContext.get());
        tvLocationType.setId(ViewUtils.generateViewId());
        tvLocationType.setGravity(Gravity.CENTER_VERTICAL);
//        tvLocationType.setTextColor(Color.WHITE);
        TextViewUtil.setTextSize(tvLocationType,LayouUtil.getDimen("m18"));
        tvLocationType.setText(movieTheaterBean.locationType);
        tvLocationType.setSingleLine();
        LinearLayout.LayoutParams mRelativeLayout = new  LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        tvLocationType.setPadding(0,0,(int) LayouUtil.getDimen("x4"),0);
        TextViewUtil.setTextColor(tvLocationType, Color.parseColor("#85868B"));
        firstContentS.addView(tvLocationType,mRelativeLayout);

        TextView tvCinemaFlag = new TextView(GlobalContext.get());
        TextViewUtil.setTextSize(tvCinemaFlag,LayouUtil.getDimen("m18"));
        tvCinemaFlag.setGravity(Gravity.CENTER_VERTICAL);
        tvCinemaFlag.setText(movieTheaterBean.cinemaFlag);
        tvCinemaFlag.setSingleLine();
        mRelativeLayout = new  LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        tvCinemaFlag.setPadding(0,0,(int) LayouUtil.getDimen("x4"),0);
        TextViewUtil.setTextColor(tvCinemaFlag, Color.parseColor("#85868B"));
        firstContentS.addView(tvCinemaFlag,mRelativeLayout);

        LinearLayout content = new LinearLayout(GlobalContext.get());
        content.setOrientation(LinearLayout.HORIZONTAL);
        tvLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
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
        tvAdress.setSingleLine();
        tvAdress.setEllipsize(TextUtils.TruncateAt.END);
        String sAdress =  movieTheaterBean.address;
//        if (sAdress.length() > 10){
//            sAdress = sAdress.substring(0,10)+"...";
//        }
        tvAdress.setText(sAdress);
        TextViewUtil.setTextSize(tvAdress,LayouUtil.getDimen("m18"));
        TextViewUtil.setTextColor(tvAdress, Color.parseColor("#85868B"));
        tvLayoutParams.rightMargin=(int) LayouUtil.getDimen("m8");
        tvLayoutParams.gravity = Gravity.TOP;
        tvLayoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT,1);
        content.addView(tvAdress,tvLayoutParams);

        View divider = new View(GlobalContext.get());
        divider.setVisibility(View.GONE);
        divider.setBackgroundColor(Color.parseColor("#4c4c4c"));
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,dividerHeight);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        itemView.addView(divider, layoutParams);
        divider.setVisibility(showDivider ? View.VISIBLE : View.INVISIBLE);

        return itemView;
    }

    @Override
    public void updateProgress(int progress, int selection) {

    }

    @Override
    public void snapPage(boolean next) {

    }

    @Override
    public void updateItemSelect(int selection) {

    }
}
