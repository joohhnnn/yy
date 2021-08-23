package com.txznet.comm.ui.theme.test.view;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.theme.ThemeConfigManager;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.config.StyleConfig;
import com.txznet.comm.ui.theme.test.config.ViewParamsUtil;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ListViewItemAnim;
import com.txznet.comm.ui.view.GradientProgressBar;
import com.txznet.comm.ui.view.RippleView;
import com.txznet.comm.ui.view.RoundImageView;
import com.txznet.comm.ui.viewfactory.IViewStateListener;
import com.txznet.comm.ui.viewfactory.ViewFactory;
import com.txznet.comm.ui.viewfactory.data.NavAppListViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.INavAppListView;
import com.txznet.comm.util.StringUtils;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.sdk.TXZWechatManager;
import com.txznet.txz.util.LanguageConvertor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by JackPan on 2019/10/23
 * Describe:
 */
public class NavAppListView extends INavAppListView {
    private static NavAppListView instance;

    private List<View> mItemViews = new ArrayList<View>();

    public static NavAppListView getInstance() {
        if (instance == null) {
            synchronized (NavAppListView.class) {
                if (instance == null) {
                    instance = new NavAppListView();
                }
            }
        }
        return instance;
    }

    private int tvNumSide;    //序号宽高
    private int tvNumHorMargin;    //序号左右边距
    private int tvNumSize;    //序号字体大小
    private int tvNumColor;    //序号字体颜色
    private int ivIconSide;    //图标大小
    private int ivIconRightMargin;    //内容字体右边距
    private int tvContentSize;    //内容字体大小
    private int tvContentColor;    //内容字体颜色

    private int dividerHeight;

    @Override
    public void init() {
        super.init();

        dividerHeight = (int) Math.ceil(ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_DIVIDER_HEIGHT));
        tvNumColor = Color.parseColor(LayouUtil.getString("color_main_title"));
        tvContentColor =  Color.parseColor(LayouUtil.getString("color_main_title"));
        int unit = ViewParamsUtil.unit;
        tvNumSide = 6 * unit;
        tvNumSize = ViewParamsUtil.h0;
        tvNumHorMargin = 2 * unit;
        ivIconSide = 7 * unit;
        ivIconRightMargin = unit;
        tvContentSize = ViewParamsUtil.h2;
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

    @Override
    public void release() {
        super.release();
        if (mItemViews != null) {
            mItemViews.clear();
        }
    }

    @Override
    public List<View> getFocusViews() {
        // TODO Auto-generated method stub
        return super.getFocusViews();
    }

    @Override
    public ViewFactory.ViewAdapter getView(ViewData viewData) {
        NavAppListViewData data = (NavAppListViewData) viewData;
        WinLayout.getInstance().vTips = data.vTips;
        LogUtil.logd(WinLayout.logTag+ "weChatListViewData.vTips: "+data.vTips);

        View view;

        switch (StyleConfig.getInstance().getSelectStyleIndex()) {
            case StyleConfig.STYLE_ROBOT_FULL_SCREES:
            case StyleConfig.STYLE_ROBOT_HALF_SCREES:
                view = createViewFull(data);
                break;
            case StyleConfig.STYLE_ROBOT_NONE_SCREES:
            default:
                view = createViewNone(data);
                break;
        }
        ViewFactory.ViewAdapter viewAdapter = new ViewFactory.ViewAdapter();
        viewAdapter.isListView = true;
        viewAdapter.object = this;
        viewAdapter.view = view;
        viewAdapter.type = data.getType();
        return viewAdapter;
    }

    private View createViewFull(NavAppListViewData data){
        ViewFactory.ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(data,"nav_choice","地图");
        LinearLayout llLayout = new LinearLayout(GlobalContext.get());
        llLayout.setGravity(Gravity.CENTER_VERTICAL);
        llLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, SizeConfig.titleHeight);
        llLayout.addView(titleViewAdapter.view,layoutParams);

        LinearLayout llContents = new LinearLayout(GlobalContext.get());
        llContents.setOrientation(LinearLayout.HORIZONTAL);
        llContents.setBackground(LayouUtil.getDrawable("white_range_layout"));

        LinearLayout llContent = new LinearLayout(GlobalContext.get());
        llContent.setOrientation(LinearLayout.VERTICAL);
        layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
        llContents.addView(llContent,layoutParams);

        LinearLayout llPager = new PageView(GlobalContext.get(),data.mTitleInfo.curPage,data.mTitleInfo.maxPage);
        layoutParams = new LinearLayout.LayoutParams(SizeConfig.pageWidth,LinearLayout.LayoutParams.MATCH_PARENT);
        llContents.addView(llPager,layoutParams);

        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.pagePoiCount * SizeConfig.itemHeight);
        llLayout.addView(llContents,layoutParams);

        llContent.setLayoutAnimation(ListViewItemAnim.getAnimationController());
        llContent.setLayoutAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (mViewStateListener != null) {
                    mViewStateListener.onAnimateStateChanged(animation, IViewStateListener.STATE_ANIM_ON_START);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                if (mViewStateListener != null) {
                    mViewStateListener.onAnimateStateChanged(animation, IViewStateListener.STATE_ANIM_ON_REPEAT);
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (mViewStateListener != null) {
                    mViewStateListener.onAnimateStateChanged(animation, IViewStateListener.STATE_ANIM_ON_END);
                }
            }
        });
        //progressBars.clear();
        mItemViews = new ArrayList<View>();
        for (int i = 0; i < data.count; i++) {
            layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.itemHeight);
            View itemView = createItemView(i,data.getData().get(i),i != SizeConfig.pageCount - 1);
            llContent.addView(itemView, layoutParams);
            mItemViews.add(itemView);
        }

        return llLayout;
    }

    private View createViewNone(NavAppListViewData data){
        ViewFactory.ViewAdapter titleViewAdapter = com.txznet.comm.ui.theme.test.view.ListTitleView.getInstance().getView(data,"nav_choice","地图");
        LinearLayout llLayout = new LinearLayout(GlobalContext.get());
        llLayout.setGravity(Gravity.CENTER_VERTICAL);
        llLayout.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout llContents = new LinearLayout(GlobalContext.get());
        llContents.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
        llLayout.addView(llContents,layoutParams);

        LinearLayout llPager = new PageView(GlobalContext.get(),data.mTitleInfo.curPage,data.mTitleInfo.maxPage);
        layoutParams = new LinearLayout.LayoutParams(SizeConfig.pageWidth,LinearLayout.LayoutParams.MATCH_PARENT);
        llLayout.addView(llPager,layoutParams);

        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.titleHeight);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        llContents.addView(titleViewAdapter.view,layoutParams);

        View divider = new View(GlobalContext.get());
        divider.setBackground(LayouUtil.getDrawable("line"));
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,dividerHeight);
        llContents.addView(divider, layoutParams);

        LinearLayout llContent = new LinearLayout(GlobalContext.get());
        llContent.setOrientation(LinearLayout.VERTICAL);
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.pagePoiCount * SizeConfig.itemHeight);
        llContents.addView(llContent,layoutParams);

        llContent.setLayoutAnimation(ListViewItemAnim.getAnimationController());
        llContent.setLayoutAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (mViewStateListener != null) {
                    mViewStateListener.onAnimateStateChanged(animation, IViewStateListener.STATE_ANIM_ON_START);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                if (mViewStateListener != null) {
                    mViewStateListener.onAnimateStateChanged(animation, IViewStateListener.STATE_ANIM_ON_REPEAT);
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (mViewStateListener != null) {
                    mViewStateListener.onAnimateStateChanged(animation, IViewStateListener.STATE_ANIM_ON_END);
                }
            }
        });
        //progressBars.clear();
        mItemViews = new ArrayList<View>();
        for (int i = 0; i < data.count; i++) {
            //layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,ConfigUtil.getDisplayLvItemH(false));
            layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.itemHeight);
            View itemView = createItemView(i,data.getData().get(i),i != SizeConfig.pageCount - 1);
            llContent.addView(itemView, layoutParams);
            mItemViews.add(itemView);
        }

        return llLayout;
    }

    private View createItemView(int position, NavAppListViewData.NavAppBean poi, boolean showDivider) {
        RippleView itemView = new RippleView(GlobalContext.get());
        itemView.setTag(position);
        ListTitleView.getInstance().mItemViews = mItemViews;
        itemView.setOnTouchListener(com.txznet.comm.ui.theme.test.view.ListTitleView.getInstance().getOnTouchListener());
        itemView.setOnClickListener(com.txznet.comm.ui.theme.test.view.ListTitleView.getInstance().getOnItemClickListener());

        FrameLayout flContent = new FrameLayout(GlobalContext.get());
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
        itemView.addView(flContent,layoutParams);

        LinearLayout llContent = new LinearLayout(GlobalContext.get());
        llContent.setOrientation(LinearLayout.HORIZONTAL);
        llContent.setGravity(Gravity.CENTER_VERTICAL);
        FrameLayout.LayoutParams mFLayoutParams  = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        mFLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        flContent.addView(llContent, mFLayoutParams);

        TextView tvNum = new TextView(GlobalContext.get());
        tvNum.setBackground(LayouUtil.getDrawable("poi_item_circle_bg"));
        tvNum.setGravity(Gravity.CENTER);
        tvNum.setIncludeFontPadding(false);
        tvNum.setPadding(0, 0, 0, 0);
        LinearLayout.LayoutParams mLLayoutParams = new LinearLayout.LayoutParams(tvNumSide,tvNumSide);
        mLLayoutParams.leftMargin = tvNumHorMargin;
        mLLayoutParams.rightMargin = tvNumHorMargin;
        mLLayoutParams.gravity = Gravity.CENTER;
        llContent.addView(tvNum,mLLayoutParams);

        final RoundImageView roundImageView = new RoundImageView(GlobalContext.get());
        mLLayoutParams = new LinearLayout.LayoutParams(ivIconSide,ivIconSide);
        mLLayoutParams.rightMargin = ivIconRightMargin;
        mLLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        llContent.addView(roundImageView,mLLayoutParams);

        TextView tvContent = new TextView(GlobalContext.get());
        tvContent.setEllipsize(TextUtils.TruncateAt.END);
        tvContent.setSingleLine();
        tvContent.setGravity(Gravity.BOTTOM);
        mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        mLLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        llContent.addView(tvContent,mLLayoutParams);

        View divider = new View(GlobalContext.get());
        divider.setVisibility(View.GONE);
        divider.setBackground(LayouUtil.getDrawable("line"));
        layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,dividerHeight);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        itemView.addView(divider, layoutParams);

        TextViewUtil.setTextSize(tvNum,tvNumSize);
        TextViewUtil.setTextColor(tvNum,tvNumColor);
        TextViewUtil.setTextSize(tvContent,tvContentSize);
        TextViewUtil.setTextColor(tvContent,tvContentColor);

        tvNum.setText(String.valueOf(position + 1));
        tvContent.setText(StringUtils.isEmpty(poi.title) ?""  : LanguageConvertor.toLocale(poi.title));
        roundImageView.setImageDrawable(getDrawableByPkn(itemView.getContext(), poi.navPkn));

        divider.setVisibility(showDivider ? View.VISIBLE : View.INVISIBLE);

        return itemView;
    }

    private Drawable getDrawableByPkn(Context mContext, String navPkn) {
        PackageManager pm = mContext.getPackageManager();
        try {
            return pm.getApplicationIcon(navPkn);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
