package com.txznet.comm.ui.theme.test.view;

import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.TextUtil;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.config.StyleConfig;
import com.txznet.comm.ui.theme.test.config.ViewParamsUtil;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ViewUtils;
import com.txznet.comm.ui.view.AutoSplitTextView;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.ConstellationFortuneData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IConstellationFortuneView;
import com.txznet.comm.ui.viewfactory.view.defaults.ConstellationMatchingView;
import com.txznet.comm.util.TextViewUtil;

public class ConstellationFortuneView extends IConstellationFortuneView {

    private static ConstellationFortuneView sInstance = new ConstellationFortuneView();
    private int mTvDescWidth;
    private int mTvDescHeight;
    private int mTvDescHorMargin;

    private int countHeight;    //内容高度
    private int ivNameWidth;    //星座图片宽度
    private int ivNameBottomMaigin;    //星座图片下边距
    private int tvNameSize;    //星座名字字体大小
    private int tvNameColor;    //星座名字字体颜色
    private int tvNameHeight;    //星座名字字体行高
    private int iconStarSide;    //一个星星的大小
    private int iconStarInterval;    //星星间的间隔
    private int iconStarTopMaigin;    //星星上边距
    private int iconStarBottomMargin;    //星星下边距
    private int tvDescSize;    //详情介绍字体大小
    private int tvDescHeight;    //详情介绍字体行高
    private int tvDescColor;    //详情介绍字体颜色
    private int dividerHeight;    //分隔线高度

    private ConstellationFortuneView() {
    }

    public static ConstellationFortuneView getInstance() {
        return sInstance;
    }

    @Override
    public ViewAdapter getView(ViewData data) {
        ConstellationFortuneData constellationFortuneData = (ConstellationFortuneData) data;
        View view = null;
        WinLayout.getInstance().vTips = constellationFortuneData.vTips;
        switch (StyleConfig.getInstance().getSelectStyleIndex()) {
            case StyleConfig.STYLE_ROBOT_FULL_SCREES:
                view = createViewFull(constellationFortuneData);
                break;
            case StyleConfig.STYLE_ROBOT_HALF_SCREES:
                if (WinLayout.isVertScreen){
                    view = createViewFull(constellationFortuneData);
                }else {
                    view = createViewHalf(constellationFortuneData);
                }
                break;
            case StyleConfig.STYLE_ROBOT_NONE_SCREES:
                view = createViewNone(constellationFortuneData);
            default:
                break;
        }
        ViewAdapter adapter = new ViewAdapter();
        adapter.type = data.getType();
        adapter.view = view;
        adapter.view.setTag(data.getType());
        adapter.object = BindDeviceView.getInstance();
        return adapter;
    }

    private View createViewFull(ConstellationFortuneData constellationFortuneData) {
        LinearLayout root = new LinearLayout(GlobalContext.get());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_VERTICAL);
//        root.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, ((int) LayouUtil.getDimen("y480"))));

//        LinearLayout titleView = new LinearLayout(GlobalContext.get());
//        titleView.setGravity(Gravity.CENTER_VERTICAL);
//        LinearLayout.LayoutParams titleViewLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//        titleViewLayoutParams.topMargin = (int) LayouUtil.getDimen("y32");
//        titleView.setLayoutParams(titleViewLayoutParams);
//        ImageView starImageView = new ImageView(GlobalContext.get());
//        starImageView.setImageDrawable(LayouUtil.getDrawable("icon_star"));
//        starImageView.setLayoutParams(new ViewGroup.LayoutParams((int) LayouUtil.getDimen("m40"), (int) LayouUtil.getDimen("m40")));
//        TextView tvTitle = new TextView(GlobalContext.get());
//        tvTitle.setText("星座运势");
//        tvTitle.setTextSize(LayouUtil.getDimen("m23"));
//        tvTitle.setTextColor(Color.parseColor("#FF89898A"));
//        tvTitle.setGravity(Gravity.CENTER);
//        titleView.addView(starImageView);
//        titleView.addView(tvTitle);
        ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(null, "constellation", "星座");
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, SizeConfig.titleHeight);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        root.addView(titleViewAdapter.view, layoutParams);

        LinearLayout contentView = new LinearLayout(GlobalContext.get());
//        contentView.setBackground(LayouUtil.getDrawable("constellation_background"));
        contentView.setBackground(LayouUtil.getDrawable("white_range_layout"));
        contentView.setOrientation(LinearLayout.VERTICAL);
        contentView.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams contentViewLayoutParam = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,countHeight);
//        contentViewLayoutParam.topMargin = (int) LayouUtil.getDimen("y8");
        contentView.setLayoutParams(contentViewLayoutParam);

        ImageView ivName = new ImageView(GlobalContext.get());
        ivName.setImageDrawable(LayouUtil.getDrawable(ConstellationMatchingView.getConstellationPictureByName(constellationFortuneData.name)));
//        LinearLayout.LayoutParams ivNameLayoutParam = new LinearLayout.LayoutParams((int) LayouUtil.getDimen("m80"), (int) LayouUtil.getDimen("m80"));
//        ivNameLayoutParam.bottomMargin = (int) LayouUtil.getDimen("y8");
//        ivNameLayoutParam.topMargin = (int) LayouUtil.getDimen("y21");
        LinearLayout.LayoutParams ivNameLayoutParam = new LinearLayout.LayoutParams(ivNameWidth, ivNameWidth);
        ivNameLayoutParam.bottomMargin = ivNameBottomMaigin;
        ivName.setLayoutParams(ivNameLayoutParam);
        contentView.addView(ivName);

        TextView tvName = new TextView(GlobalContext.get());
        LinearLayout.LayoutParams tvNameLayoutParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//        tvNameLayoutParam.bottomMargin = (int) LayouUtil.getDimen("y8");
        tvName.setLayoutParams(tvNameLayoutParam);
        tvName.setGravity(Gravity.CENTER);
//        tvName.setTextSize(LayouUtil.getDimen("m23"));
//        tvName.setTextColor(Color.WHITE);
        tvName.setText(constellationFortuneData.name + constellationFortuneData.fortuneType);
        contentView.addView(tvName);


        LinearLayout scoreLayout = new LinearLayout(GlobalContext.get());
        int width = iconStarSide * 5 + iconStarInterval * 4;
        LinearLayout.LayoutParams scoreLayoutParam = new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.WRAP_CONTENT);
        scoreLayoutParam.topMargin = iconStarTopMaigin;
        scoreLayoutParam.bottomMargin = iconStarBottomMargin;
        scoreLayout.setLayoutParams(scoreLayoutParam);
//        for (int i = 0; i < constellationFortuneData.level; i++) {
//            ImageView imageView = new ImageView(GlobalContext.get());
//            imageView.setImageDrawable(LayouUtil.getDrawable("star_enable"));
//            layoutParams = new LinearLayout.LayoutParams(iconStarSide, iconStarSide);
//            layoutParams.rightMargin = iconStarInterval;
//            imageView.setLayoutParams(layoutParams);
//            scoreLayout.addView(imageView);
//        }
//        for (int i = 0; i < 5 - constellationFortuneData.level; i++) {
//            ImageView imageView = new ImageView(GlobalContext.get());
//            imageView.setImageDrawable(LayouUtil.getDrawable("star_disable"));
//            layoutParams = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams((int) LayouUtil.getDimen("m32"), (int) LayouUtil.getDimen("m32")));
//            layoutParams.rightMargin = (int) LayouUtil.getDimen("x24");
//            imageView.setLayoutParams(layoutParams);
//            scoreLayout.addView(imageView);
//        }
        for (int i = 0;i < 5;i++){
            ImageView imageView = new ImageView(GlobalContext.get());
            layoutParams = new LinearLayout.LayoutParams(iconStarSide, iconStarSide);
            layoutParams.rightMargin = iconStarInterval;
            imageView.setLayoutParams(layoutParams);
            imageView.setImageDrawable(LayouUtil.getDrawable(i < constellationFortuneData.level?"star_enable":"star_disable"));
            scoreLayout.addView(imageView);
        }
        contentView.addView(scoreLayout);

        final AutoSplitTextView tvDesc = new AutoSplitTextView(GlobalContext.get());
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = mTvDescHorMargin;
        layoutParams.rightMargin = mTvDescHorMargin;
        tvDesc.setLayoutParams(layoutParams);
//        tvDesc.setTextSize(LayouUtil.getDimen("m19"));
//        tvDesc.setTextColor(Color.WHITE);
        tvDesc.setEllipsize(TextUtils.TruncateAt.END);
        tvDesc.setText(constellationFortuneData.desc);
        tvDesc.setLineSpacing(tvDescHeight,0);
//        tvDesc.setMaxLines(mTvDescHeight / tvDesc.getLineHeight());
        tvDesc.setMaxLines(mTvDescHeight / tvDescHeight);
        contentView.addView(tvDesc);

        TextViewUtil.setTextSize(tvName,tvNameSize);
        TextViewUtil.setTextSize(tvName,tvNameColor);
        TextViewUtil.setTextSize(tvDesc,tvDescSize);
        TextViewUtil.setTextSize(tvDesc,tvDescColor);

//        root.addView(titleView);
        root.addView(contentView);
        return root;
    }

    private View createViewHalf(ConstellationFortuneData constellationFortuneData) {
        LinearLayout root = new LinearLayout(GlobalContext.get());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_VERTICAL);

        ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(null, "constellation", "星座");
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, SizeConfig.titleHeight);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        root.addView(titleViewAdapter.view, layoutParams);

        LinearLayout contentView = new LinearLayout(GlobalContext.get());
        contentView.setOrientation(LinearLayout.HORIZONTAL);
        contentView.setBackground(LayouUtil.getDrawable("white_range_layout"));
//        contentView.setOrientation(LinearLayout.HORIZONTAL);
        contentView.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams contentViewLayoutParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        contentViewLayoutParam.topMargin = (int) LayouUtil.getDimen("y8");
        contentView.setLayoutParams(contentViewLayoutParam);

        LinearLayout leftContentView = new LinearLayout(GlobalContext.get());
        leftContentView.setId(ViewUtils.generateViewId());
        leftContentView.setOrientation(LinearLayout.VERTICAL);
        leftContentView.setGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams leftContentViewLayoutParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        leftContentViewLayoutParam.gravity = Gravity.CENTER_VERTICAL;
        leftContentView.setLayoutParams(leftContentViewLayoutParam);

        ImageView ivName = new ImageView(GlobalContext.get());
        ivName.setImageDrawable(LayouUtil.getDrawable(ConstellationMatchingView.getConstellationPictureByName(constellationFortuneData.name)));
        LinearLayout.LayoutParams ivNameLayoutParam = new LinearLayout.LayoutParams(ivNameWidth, ivNameWidth);
        ivNameLayoutParam.bottomMargin = ivNameBottomMaigin;
        ivName.setLayoutParams(ivNameLayoutParam);
        leftContentView.addView(ivName);

        TextView tvName = new TextView(GlobalContext.get());
        LinearLayout.LayoutParams tvNameLayoutParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, tvNameHeight);
        tvName.setLayoutParams(tvNameLayoutParam);
        tvName.setGravity(Gravity.CENTER);
//        tvName.setTextSize(LayouUtil.getDimen("m22"));
//        tvName.setTextColor(Color.WHITE);
        tvName.setText(constellationFortuneData.name + constellationFortuneData.fortuneType);
        leftContentView.addView(tvName);


        LinearLayout scoreLayout = new LinearLayout(GlobalContext.get());
        int width = iconStarSide * 5 + iconStarInterval * 4;
        LinearLayout.LayoutParams scoreLayoutParam = new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.WRAP_CONTENT);
        scoreLayoutParam.topMargin = iconStarTopMaigin;
        scoreLayout.setLayoutParams(scoreLayoutParam);
        for (int i = 0;i < 5;i++){
            ImageView imageView = new ImageView(GlobalContext.get());
            layoutParams = new LinearLayout.LayoutParams(iconStarSide, iconStarSide);
            layoutParams.rightMargin = iconStarInterval;
            imageView.setLayoutParams(layoutParams);
            imageView.setImageDrawable(LayouUtil.getDrawable(i < constellationFortuneData.level?"star_enable":"star_disable"));
            scoreLayout.addView(imageView);
        }
        leftContentView.addView(scoreLayout);

        AutoSplitTextView tvDesc = new AutoSplitTextView(GlobalContext.get());
        LinearLayout.LayoutParams tvDescLayoutParams = new LinearLayout.LayoutParams(mTvDescWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvDescLayoutParams.leftMargin = ViewParamsUtil.unit * 4;
        tvDescLayoutParams.gravity = Gravity.CENTER_VERTICAL;
//        tvDescLayoutParams.addRule(RelativeLayout.RIGHT_OF, leftContentView.getId());
//        tvDescLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        tvDesc.setLayoutParams(tvDescLayoutParams);
//        tvDesc.setTextSize(LayouUtil.getDimen("m19"));
//        tvDesc.setTextColor(Color.WHITE);
        tvDesc.setEllipsize(TextUtils.TruncateAt.END);
        tvDesc.setText(constellationFortuneData.desc);
        tvDesc.setLineSpacing(tvDescHeight,0);
        tvDesc.setMaxLines(mTvDescHeight / tvDescHeight);

        contentView.addView(leftContentView);
        contentView.addView(tvDesc);
//        root.addView(titleView);
        root.addView(contentView);

        TextViewUtil.setTextSize(tvName,tvNameSize);
        TextViewUtil.setTextSize(tvName,tvNameColor);
        TextViewUtil.setTextSize(tvDesc,tvDescSize);
        TextViewUtil.setTextSize(tvDesc,tvDescColor);

        return root;
    }

    private View createViewNone(ConstellationFortuneData constellationFortuneData) {
        LinearLayout root = new LinearLayout(GlobalContext.get());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//        root.setBackground(LayouUtil.getDrawable("constellation_background"));

        ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(null, "constellation", "星座");
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, SizeConfig.titleHeight);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        root.addView(titleViewAdapter.view, layoutParams);

        View divider = new View(GlobalContext.get());
        divider.setBackground(LayouUtil.getDrawable("line"));
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dividerHeight);
        root.addView(divider, layoutParams);

        LinearLayout contentView = new LinearLayout(GlobalContext.get());
        contentView.setOrientation(LinearLayout.VERTICAL);
        contentView.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams contentViewLayoutParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        contentViewLayoutParam.topMargin = (int) LayouUtil.getDimen("y8");
        contentView.setLayoutParams(contentViewLayoutParam);

        LinearLayout topLayout = new LinearLayout(GlobalContext.get());
        topLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams topLayoutParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        topLayoutParam.gravity = Gravity.CENTER_HORIZONTAL;
        topLayout.setLayoutParams(topLayoutParam);

        ImageView ivName = new ImageView(GlobalContext.get());
        ivName.setImageDrawable(LayouUtil.getDrawable(ConstellationMatchingView.getConstellationPictureByName(constellationFortuneData.name)));
        LinearLayout.LayoutParams ivNameLayoutParam = new LinearLayout.LayoutParams(ivNameWidth, ivNameWidth);
        ivNameLayoutParam.gravity = Gravity.CENTER_VERTICAL;
        ivName.setLayoutParams(ivNameLayoutParam);
        topLayout.addView(ivName);

        LinearLayout topRightLayout = new LinearLayout(GlobalContext.get());
        topRightLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams topRightLayoutParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        topRightLayoutParam.gravity = Gravity.CENTER_VERTICAL;
        topRightLayoutParam.leftMargin = ViewParamsUtil.unit * 2;
        topRightLayout.setLayoutParams(topRightLayoutParam);

        TextView tvName = new TextView(GlobalContext.get());
        LinearLayout.LayoutParams tvNameLayoutParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, tvNameHeight);
        tvName.setLayoutParams(tvNameLayoutParam);
        tvName.setGravity(Gravity.CENTER);
//        tvName.setTextSize(LayouUtil.getDimen("m18"));
//        tvName.setTextColor(Color.WHITE);
        tvName.setText(constellationFortuneData.name + constellationFortuneData.fortuneType);
        topRightLayout.addView(tvName);

        LinearLayout scoreLayout = new LinearLayout(GlobalContext.get());
        scoreLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams scoreLayoutParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        scoreLayoutParam.gravity = Gravity.CENTER_HORIZONTAL;
        scoreLayout.setLayoutParams(scoreLayoutParam);
        for (int i = 0; i < 5; i++) {
            ImageView imageView = new ImageView(GlobalContext.get());
            layoutParams = new LinearLayout.LayoutParams(iconStarSide, iconStarSide);
            layoutParams.rightMargin = iconStarInterval;
            imageView.setImageDrawable(LayouUtil.getDrawable(i < constellationFortuneData.level?"star_enable":"star_disable"));
            imageView.setLayoutParams(layoutParams);
            scoreLayout.addView(imageView);
        }
        topRightLayout.addView(scoreLayout);

        topLayout.addView(topRightLayout);

        AutoSplitTextView tvDesc = new AutoSplitTextView(GlobalContext.get());
//        LinearLayout.LayoutParams tvDescLayoutParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mTvDescHeight);
        LinearLayout.LayoutParams tvDescLayoutParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvDescLayoutParam.topMargin = ViewParamsUtil.unit * 2;
        tvDescLayoutParam.leftMargin = mTvDescHorMargin;
        tvDescLayoutParam.rightMargin = mTvDescHorMargin;
        tvDesc.setLayoutParams(tvDescLayoutParam);
//        tvDesc.setTextSize(LayouUtil.getDimen("m18"));
//        tvDesc.setTextColor(Color.WHITE);
        tvDesc.setText(constellationFortuneData.desc);
        tvDesc.setEllipsize(TextUtils.TruncateAt.END);
        tvDesc.setLineSpacing(tvDescHeight,0);
        tvDesc.setMaxLines(mTvDescHeight / tvDescHeight);

        contentView.addView(topLayout);
        contentView.addView(tvDesc);

//        root.addView(titleView);
        root.addView(contentView);

        TextViewUtil.setTextSize(tvName,tvNameSize);
        TextViewUtil.setTextSize(tvName,tvNameColor);
        TextViewUtil.setTextSize(tvDesc,tvDescSize);
        TextViewUtil.setTextSize(tvDesc,tvDescColor);

        return root;
    }

    @Override
    public void init() {
        super.init();
        dividerHeight = 1;
    }

    private int unit;

    //切换模式修改布局参数
    public void onUpdateParams(int styleIndex) {
        unit = ViewParamsUtil.unit;
        switch (styleIndex) {
            case StyleConfig.STYLE_ROBOT_FULL_SCREES:
                initFull();
                break;
            case StyleConfig.STYLE_ROBOT_HALF_SCREES:
                initHalf();
                break;
            case StyleConfig.STYLE_ROBOT_NONE_SCREES:
                initNone();
                break;
            default:
                break;
        }
        
    }

    private void initFull() {
        if (WinLayout.isVertScreen) {
            countHeight = 46 * unit;
            ivNameWidth = 10 * unit;    //星座图片宽度
            ivNameBottomMaigin = unit;    //星座图片下边距
            tvNameSize = ViewParamsUtil.h3;    //星座名字字体大小
            tvNameHeight = ViewParamsUtil.h3Height;    //星座名字字体行高
            tvNameColor = Color.parseColor(LayouUtil.getString("color_main_title"));    //星座名字字体颜色
            iconStarSide = 4 * unit;    //一个星星的大小
            iconStarInterval = 3 * unit;    //星星间的间隔
            iconStarTopMaigin = unit;    //星星上边距
            iconStarBottomMargin = 3 * unit;    //星星下边距
            tvDescSize = ViewParamsUtil.h5;    //详情介绍字体大小
            tvDescHeight = ViewParamsUtil.h5Height;    //详情介绍字体行高
            tvDescColor = Color.parseColor(LayouUtil.getString("color_main_title"));    //详情介绍字体颜色
            mTvDescHorMargin = (int)(6.4 * unit);
            mTvDescHeight = 17 * unit;
        } else {
            countHeight = SizeConfig.pagePoiCount * SizeConfig.itemHeight;
            ivNameWidth = 10 * unit;    //星座图片宽度
            ivNameBottomMaigin = unit;    //星座图片下边距
            tvNameSize = ViewParamsUtil.h3;    //星座名字字体大小
            tvNameHeight = ViewParamsUtil.h3Height;    //星座名字字体行高
            tvNameColor = Color.parseColor(LayouUtil.getString("color_main_title"));    //星座名字字体颜色
            iconStarSide = 4 * unit;    //一个星星的大小
            iconStarInterval = 3 * unit;    //星星间的间隔
            iconStarTopMaigin = unit;    //星星上边距
            iconStarBottomMargin = 3 * unit;    //星星下边距
            tvDescSize = ViewParamsUtil.h5;    //详情介绍字体大小
            tvDescHeight = ViewParamsUtil.h5Height;    //详情介绍字体行高
            tvDescColor = Color.parseColor(LayouUtil.getString("color_main_title"));    //详情介绍字体颜色
//            mTvDescWidth = 60 * unit;
            mTvDescHorMargin = (int)(6.4 * unit);
//            mTvDescHeight = 17 * unit;
            mTvDescHeight = (int) (23.8 * unit);
            if (SizeConfig.screenHeight < 480){
                mTvDescHeight -= SizeConfig.itemHeight;
            }

        }
        
    }

    private void initHalf() {
        if (WinLayout.isVertScreen) {
            countHeight = 46 * unit;
            ivNameWidth = 10 * unit;    //星座图片宽度
            ivNameBottomMaigin = unit;    //星座图片下边距
            tvNameSize = ViewParamsUtil.h3;    //星座名字字体大小
            tvNameHeight = ViewParamsUtil.h3Height;    //星座名字字体行高
            tvNameColor = Color.parseColor(LayouUtil.getString("color_main_title"));    //星座名字字体颜色
            iconStarSide = 4 * unit;    //一个星星的大小
            iconStarInterval = 3 * unit;    //星星间的间隔
            iconStarTopMaigin = unit;    //星星上边距
            iconStarBottomMargin = 3 * unit;    //星星下边距
            tvDescSize = ViewParamsUtil.h5;    //详情介绍字体大小
            tvDescHeight = ViewParamsUtil.h5Height;    //详情介绍字体行高
            tvDescColor = Color.parseColor(LayouUtil.getString("color_main_title"));    //详情介绍字体颜色
            mTvDescHorMargin = (int)(6.4 * unit);
            mTvDescHeight = 17 * unit;
        } else {
            countHeight = SizeConfig.pagePoiCount * SizeConfig.itemHeight;
            mTvDescWidth = (int)(54.4 * unit);
//            mTvDescHeight = 21 * unit;
            mTvDescHeight = (int) (27.2 * unit);
            if (SizeConfig.screenHeight < 480){
                mTvDescHeight -= SizeConfig.itemHeight;
            }
            ivNameWidth = 10 * unit;    //星座图片宽度
            ivNameBottomMaigin = unit;    //星座图片下边距
            tvNameSize = ViewParamsUtil.h3;    //星座名字字体大小
            tvNameHeight = ViewParamsUtil.h3Height;    //星座名字字体行高
            tvNameColor = Color.parseColor(LayouUtil.getString("color_main_title"));    //星座名字字体颜色
            iconStarSide = 4 * unit;    //一个星星的大小
            iconStarInterval = 2 * unit;    //星星间的间隔
            iconStarTopMaigin = unit;    //星星上边距
            iconStarBottomMargin = 3 * unit;    //星星下边距
            tvDescSize = ViewParamsUtil.h5;    //详情介绍字体大小
            tvDescHeight = ViewParamsUtil.h5Height;    //详情介绍字体行高
            tvDescColor = Color.parseColor(LayouUtil.getString("color_main_title"));    //详情介绍字体颜色
        }
    }

    private void initNone() {
        if (WinLayout.isVertScreen) {
            ivNameWidth = 8 * unit;    //星座图片宽度
            ivNameBottomMaigin = unit;    //星座图片下边距
            tvNameSize = ViewParamsUtil.h5;    //星座名字字体大小
            tvNameHeight = ViewParamsUtil.h5Height;    //星座名字字体行高
            tvNameColor = Color.parseColor(LayouUtil.getString("color_main_title"));    //星座名字字体颜色
            iconStarSide = 3 * unit;    //一个星星的大小
            iconStarInterval = unit;    //星星间的间隔
            iconStarTopMaigin = unit;    //星星上边距
            iconStarBottomMargin = 3 * unit;    //星星下边距
            tvDescSize = ViewParamsUtil.h6;    //详情介绍字体大小
            tvDescHeight = ViewParamsUtil.h6Height;    //详情介绍字体行高
            tvDescColor = Color.parseColor(LayouUtil.getString("color_main_title"));    //详情介绍字体颜色
//            mTvDescWidth = 60 * unit;
            mTvDescHorMargin = (int)(6.4 * unit);
            mTvDescHeight = 17 * unit;

        } else {
            ivNameWidth = 8 * unit;    //星座图片宽度
            ivNameBottomMaigin = unit;    //星座图片下边距
            tvNameSize = ViewParamsUtil.h5;    //星座名字字体大小
            tvNameHeight = ViewParamsUtil.h5Height;    //星座名字字体行高
            tvNameColor = Color.parseColor(LayouUtil.getString("color_main_title"));    //星座名字字体颜色
            iconStarSide = 3 * unit;    //一个星星的大小
            iconStarInterval = unit;    //星星间的间隔
            iconStarTopMaigin = unit;    //星星上边距
            iconStarBottomMargin = 3 * unit;    //星星下边距
            tvDescSize = ViewParamsUtil.h6;    //详情介绍字体大小
            tvDescHeight = ViewParamsUtil.h6Height;    //详情介绍字体行高
            tvDescColor = Color.parseColor(LayouUtil.getString("color_main_title"));    //详情介绍字体颜色
//            mTvDescWidth = 60 * unit;
            mTvDescHorMargin = (int)(6.4 * unit);
            mTvDescHeight = 17 * unit;
        }
    }

}
