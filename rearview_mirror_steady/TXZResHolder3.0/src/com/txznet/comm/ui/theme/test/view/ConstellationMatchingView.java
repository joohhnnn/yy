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
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.config.StyleConfig;
import com.txznet.comm.ui.theme.test.config.ViewParamsUtil;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ViewUtils;
import com.txznet.comm.ui.view.AutoSplitTextView;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.ConstellationMatchingData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IConstellationMatchingView;
import com.txznet.comm.util.TextViewUtil;

import java.util.HashMap;

public class ConstellationMatchingView extends IConstellationMatchingView {

    private static ConstellationMatchingView sInstance = new ConstellationMatchingView();
    private int mTvDescWidth;
    private int mTvDescHeight;
    private int mTvDescLeftMargin;

    private int countHeight;    //内容高度
    private int ivNameWidth;    //星座图片宽度
    private int ivNameBottomMaigin;    //星座图片下边距
    private int tvNameSize;    //星座名字字体大小
    private int tvNameColor;    //星座名字字体颜色
    private int tvNameHeight;    //星座名字字体行高
    private int llMatchLeftMargin;    //匹配度布局左边距
    private int tvMatchTitleSize;    //匹配度标题字体大小
    private int tvMatchTitleHeight;    //匹配度标题字体行高
    private int tvMatchTitleColor;    //匹配度标题字体颜色
    private int tvMatchSize;    //匹配度字体大小
    private int tvMatchHeight;    //匹配度字体行高
    private int tvMatchColor;    //匹配度字体颜色
    private int tvDescSize;    //详情介绍字体大小
    private int tvDescHeight;    //详情介绍字体行高
    private int tvDescColor;    //详情介绍字体颜色
    private int dividerHeight;    //分隔线高度


    private ConstellationMatchingView() {
    }

    public static ConstellationMatchingView getInstance() {
        return sInstance;
    }

    @Override
    public ViewAdapter getView(ViewData data) {
        ConstellationMatchingData constellationMatchingData = (ConstellationMatchingData) data;
        WinLayout.getInstance().vTips = constellationMatchingData.vTips;
        View view = null;
        switch (StyleConfig.getInstance().getSelectStyleIndex()) {
            case StyleConfig.STYLE_ROBOT_FULL_SCREES:
                view = createViewFull(constellationMatchingData);
                break;
            case StyleConfig.STYLE_ROBOT_HALF_SCREES:
                if (WinLayout.isVertScreen){
                    view = createViewFull(constellationMatchingData);
                }else {
                    view = createViewHalf(constellationMatchingData);
                }
                break;
            case StyleConfig.STYLE_ROBOT_NONE_SCREES:
                view = createViewNone(constellationMatchingData);
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

    private View createViewNone(ConstellationMatchingData constellationMatchingData) {
        LinearLayout root = new LinearLayout(GlobalContext.get());
        root.setOrientation(LinearLayout.VERTICAL);
//        root.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, ((int) LayouUtil.getDimen("y480"))));
//        root.setBackground(LayouUtil.getDrawable("constellation_background"));
        root.setOrientation(LinearLayout.VERTICAL);
//        root.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(null, "constellation", "星座");
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, SizeConfig.titleHeight);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        root.addView(titleViewAdapter.view, layoutParams);

        View divider = new View(GlobalContext.get());
        divider.setBackground(LayouUtil.getDrawable("line"));
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dividerHeight);
        root.addView(divider, layoutParams);

        int unit = ViewParamsUtil.unit;
        RelativeLayout contentView = new RelativeLayout(GlobalContext.get());
        LinearLayout.LayoutParams contentViewLayoutParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0,1);
//        contentViewLayoutParam.topMargin = (int) LayouUtil.getDimen("y8");
        contentView.setLayoutParams(contentViewLayoutParam);

        TextView tvMatchTitle = new TextView(GlobalContext.get());
        RelativeLayout.LayoutParams tvMatchTitleLayoutParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvMatchTitleLayoutParam.topMargin = (int)(2.5 * unit);
        tvMatchTitleLayoutParam.addRule(RelativeLayout.CENTER_HORIZONTAL);
        tvMatchTitle.setGravity(Gravity.CENTER);
        tvMatchTitle.setId(ViewUtils.generateViewId());
//        tvMatchTitle.setTextSize(LayouUtil.getDimen("m16"));
        tvMatchTitle.setText("匹配度");
        tvMatchTitle.setLayoutParams(tvMatchTitleLayoutParam);

        TextView tvMatch = new TextView(GlobalContext.get());
        RelativeLayout.LayoutParams tvMatchLayoutParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, tvMatchHeight);
        tvMatchLayoutParam.addRule(RelativeLayout.CENTER_HORIZONTAL);
        tvMatchLayoutParam.addRule(RelativeLayout.BELOW, tvMatchTitle.getId());
        tvMatch.setId(ViewUtils.generateViewId());
        tvMatch.setGravity(Gravity.CENTER);
        tvMatch.setText(constellationMatchingData.level + "%");
//        tvMatch.setTextSize(LayouUtil.getDimen("m23"));
//        tvMatch.setTextColor(Color.parseColor("#FFFFCA00"));
        tvMatch.setLayoutParams(tvMatchLayoutParam);

        ImageView ivName = new ImageView(GlobalContext.get());
        ivName.setId(ViewUtils.generateViewId());
        ivName.setImageDrawable(LayouUtil.getDrawable(ConstellationMatchingView.getConstellationPictureByName(constellationMatchingData.name)));
        RelativeLayout.LayoutParams ivNameRelativeLayoutParam = new RelativeLayout.LayoutParams(ivNameWidth, ivNameWidth);
        ivNameRelativeLayoutParam.leftMargin = 3 * unit;
        ivNameRelativeLayoutParam.topMargin = (int)(2.5 * unit);
        ivName.setLayoutParams(ivNameRelativeLayoutParam);

        TextView tvName = new TextView(GlobalContext.get());
        RelativeLayout.LayoutParams tvNameLayoutParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ivNameWidth);
        tvNameLayoutParam.leftMargin = 2 * unit;
        tvNameLayoutParam.topMargin = (int)(2.5 * unit);
        tvNameLayoutParam.addRule(RelativeLayout.RIGHT_OF, ivName.getId());
        tvName.setId(ViewUtils.generateViewId());
//        tvName.setTextSize(LayouUtil.getDimen("m16"));
//        tvName.setTextColor(Color.WHITE);
        tvName.setText(constellationMatchingData.name);
        tvName.setGravity(Gravity.CENTER);
        tvName.setLayoutParams(tvNameLayoutParam);

        ImageView ivMatchName = new ImageView(GlobalContext.get());
        ivMatchName.setId(ViewUtils.generateViewId());
        ivMatchName.setImageDrawable(LayouUtil.getDrawable(com.txznet.comm.ui.viewfactory.view.defaults.ConstellationMatchingView.getConstellationPictureByName(constellationMatchingData.matchName)));
        RelativeLayout.LayoutParams ivMatchNameRelativeLayoutParam = new RelativeLayout.LayoutParams(ivNameWidth, ivNameWidth);
        ivMatchNameRelativeLayoutParam.rightMargin = 3 * unit;
//        ivMatchNameRelativeLayoutParam.topMargin = (int)(2.5 * unit);
        ivMatchNameRelativeLayoutParam.addRule(RelativeLayout.ALIGN_TOP, ivName.getId());
        ivMatchNameRelativeLayoutParam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        ivMatchName.setLayoutParams(ivMatchNameRelativeLayoutParam);

        TextView tvMatchName = new TextView(GlobalContext.get());
        RelativeLayout.LayoutParams tvMatchNameLayoutParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ivNameWidth);
        tvMatchNameLayoutParam.addRule(RelativeLayout.LEFT_OF, ivMatchName.getId());
        tvMatchNameLayoutParam.addRule(RelativeLayout.ALIGN_TOP, tvName.getId());
        tvMatchName.setId(ViewUtils.generateViewId());
        tvMatchNameLayoutParam.rightMargin = 2 * unit;
//        tvMatchName.setTextSize(LayouUtil.getDimen("m16"));
//        tvMatchName.setTextColor(Color.WHITE);
        tvMatchName.setText(constellationMatchingData.matchName);
        tvMatchName.setGravity(Gravity.CENTER);
        tvMatchName.setLayoutParams(tvMatchNameLayoutParam);

        contentView.addView(ivName);
        contentView.addView(tvName);
        contentView.addView(ivMatchName);
        contentView.addView(tvMatchName);
        contentView.addView(tvMatchTitle);
        contentView.addView(tvMatch);

        AutoSplitTextView tvDesc = new AutoSplitTextView(GlobalContext.get());
        RelativeLayout.LayoutParams tvDescLayoutParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mTvDescHeight);
        tvDescLayoutParam.bottomMargin = (int)(2.5 * unit);
        tvDescLayoutParam.leftMargin = 3 * unit;
        tvDescLayoutParam.rightMargin = 3 * unit;
//        tvDescLayoutParam.addRule(RelativeLayout.BELOW, ivName.getId());
        tvDescLayoutParam.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        tvDescLayoutParam.addRule(RelativeLayout.CENTER_HORIZONTAL);
        tvDesc.setLayoutParams(tvDescLayoutParam);
//        tvDesc.setTextSize(LayouUtil.getDimen("m18"));
//        tvDesc.setTextColor(Color.WHITE);\
        tvDesc.setText(constellationMatchingData.desc);
        tvDesc.setGravity(Gravity.CENTER);
        tvDesc.setLineSpacing(tvDescHeight,0);
        tvDesc.setMaxLines(mTvDescHeight/tvDescHeight);
        tvDesc.setEllipsize(TextUtils.TruncateAt.END);
        contentView.addView(tvDesc);

        TextViewUtil.setTextSize(tvName,tvNameSize);
        TextViewUtil.setTextColor(tvName,tvNameColor);
        TextViewUtil.setTextSize(tvMatchTitle,tvMatchTitleSize);
        TextViewUtil.setTextColor(tvMatchTitle,tvMatchTitleColor);
        TextViewUtil.setTextSize(tvMatch,tvMatchSize);
        TextViewUtil.setTextColor(tvMatch,tvMatchColor);
        TextViewUtil.setTextSize(tvDesc,tvDescSize);
        TextViewUtil.setTextColor(tvDesc,tvDescColor);

        root.addView(contentView);
        return root;
    }

    private View createViewHalf(ConstellationMatchingData constellationMatchingData) {
        LinearLayout root = new LinearLayout(GlobalContext.get());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_VERTICAL);

        ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(null, "constellation", "星座");
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, SizeConfig.titleHeight);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        root.addView(titleViewAdapter.view, layoutParams);

        RelativeLayout contentView = new RelativeLayout(GlobalContext.get());
        contentView.setBackground(LayouUtil.getDrawable("white_range_layout"));
        contentView.setGravity(Gravity.CENTER);
        RelativeLayout.LayoutParams contentViewLayoutParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        contentViewLayoutParam.topMargin = (int) LayouUtil.getDimen("y8");
        contentView.setLayoutParams(contentViewLayoutParam);

        RelativeLayout leftContent = new RelativeLayout(GlobalContext.get());
        leftContent.setId(ViewUtils.generateViewId());
        RelativeLayout.LayoutParams leftContentLayoutParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        leftContent.setGravity(Gravity.CENTER);
        leftContentLayoutParam.addRule(RelativeLayout.CENTER_VERTICAL);
        leftContent.setLayoutParams(leftContentLayoutParam);

        ImageView ivName = new ImageView(GlobalContext.get());
        ivName.setId(ViewUtils.generateViewId());
        ivName.setImageDrawable(LayouUtil.getDrawable(com.txznet.comm.ui.viewfactory.view.defaults.ConstellationMatchingView.getConstellationPictureByName(constellationMatchingData.name)));
        RelativeLayout.LayoutParams ivNameRelativeLayoutParam = new RelativeLayout.LayoutParams(ivNameWidth, ivNameWidth);
        ivName.setLayoutParams(ivNameRelativeLayoutParam);

        TextView tvName = new TextView(GlobalContext.get());
        RelativeLayout.LayoutParams tvNameLayoutParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, tvNameHeight);
        tvNameLayoutParam.topMargin = unit;
        tvNameLayoutParam.addRule(RelativeLayout.BELOW, ivName.getId());
        tvNameLayoutParam.addRule(RelativeLayout.ALIGN_LEFT, ivName.getId());
        tvName.setId(ViewUtils.generateViewId());
//        tvName.setTextSize(LayouUtil.getDimen("m16"));
//        tvName.setTextColor(Color.WHITE);
        tvName.setText(constellationMatchingData.name);
        tvName.setGravity(Gravity.CENTER);
        tvName.setLayoutParams(tvNameLayoutParam);

        LinearLayout llMatch = new LinearLayout(GlobalContext.get());
//        int llMatchWidth = (int) (LayouUtil.getDimen("m80") * 2 + LayouUtil.getDimen("x54"));
        llMatch.setId(ViewUtils.generateViewId());
        RelativeLayout.LayoutParams llMatchLayoutParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        llMatchLayoutParam.addRule(RelativeLayout.BELOW, tvName.getId());
        llMatchLayoutParam.addRule(RelativeLayout.CENTER_HORIZONTAL);
        llMatchLayoutParam.topMargin = 2 * unit;
        llMatch.setGravity(Gravity.CENTER_HORIZONTAL);
        llMatch.setOrientation(LinearLayout.VERTICAL);
        llMatch.setLayoutParams(llMatchLayoutParam);

        TextView tvMatchTitle = new TextView(GlobalContext.get());
        RelativeLayout.LayoutParams tvMatchTitleLayoutParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvMatchTitle.setGravity(Gravity.CENTER);
        tvMatchTitle.setId(ViewUtils.generateViewId());
//        tvMatchTitle.setTextSize(LayouUtil.getDimen("m19"));
        tvMatchTitle.setText("匹配度");
        tvMatchTitle.setLayoutParams(tvMatchTitleLayoutParam);

        TextView tvMatch = new TextView(GlobalContext.get());
        RelativeLayout.LayoutParams tvMatchLayoutParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvMatch.setId(ViewUtils.generateViewId());
        tvMatch.setGravity(Gravity.CENTER);
        tvMatch.setText(constellationMatchingData.level + "%");
//        tvMatch.setTextSize(LayouUtil.getDimen("m39"));
//        tvMatch.setTextColor(Color.parseColor("#FFFFCA00"));
        tvMatch.setLayoutParams(tvMatchLayoutParam);

        ImageView ivMatchName = new ImageView(GlobalContext.get());
        ivMatchName.setId(ViewUtils.generateViewId());
        ivMatchName.setImageDrawable(LayouUtil.getDrawable(com.txznet.comm.ui.viewfactory.view.defaults.ConstellationMatchingView.getConstellationPictureByName(constellationMatchingData.matchName)));
        RelativeLayout.LayoutParams ivMatchNameRelativeLayoutParam = new RelativeLayout.LayoutParams(ivNameWidth, ivNameWidth);
        ivMatchNameRelativeLayoutParam.leftMargin = 8 * unit;
        ivMatchNameRelativeLayoutParam.addRule(RelativeLayout.RIGHT_OF, ivName.getId());
        ivMatchNameRelativeLayoutParam.addRule(RelativeLayout.ALIGN_TOP, ivName.getId());
        ivMatchName.setLayoutParams(ivMatchNameRelativeLayoutParam);

        TextView tvMatchName = new TextView(GlobalContext.get());
        RelativeLayout.LayoutParams tvMatchNameLayoutParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, tvNameHeight);
        tvMatchNameLayoutParam.topMargin = unit;
        tvMatchNameLayoutParam.addRule(RelativeLayout.BELOW, ivMatchName.getId());
        tvMatchNameLayoutParam.addRule(RelativeLayout.ALIGN_LEFT, ivMatchName.getId());
        tvMatchName.setId(ViewUtils.generateViewId());
//        tvMatchName.setTextSize(LayouUtil.getDimen("m16"));
//        tvMatchName.setTextColor(Color.WHITE);
        tvMatchName.setText(constellationMatchingData.matchName);
        tvMatchName.setGravity(Gravity.CENTER);
        tvMatchName.setLayoutParams(tvMatchNameLayoutParam);

        llMatch.addView(tvMatchTitle);
        llMatch.addView(tvMatch);

        leftContent.addView(ivName);
        leftContent.addView(tvName);
        leftContent.addView(ivMatchName);
        leftContent.addView(tvMatchName);
        leftContent.addView(llMatch);
        contentView.addView(leftContent);
        root.addView(contentView);

        AutoSplitTextView tvDesc = new AutoSplitTextView(GlobalContext.get());
        RelativeLayout.LayoutParams tvDescLayoutParam = new RelativeLayout.LayoutParams(mTvDescWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvDescLayoutParam.leftMargin = 4 * unit;
        tvDescLayoutParam.addRule(RelativeLayout.RIGHT_OF, leftContent.getId());
        tvDescLayoutParam.addRule(RelativeLayout.CENTER_VERTICAL);
        tvDesc.setLayoutParams(tvDescLayoutParam);
//        tvDesc.setTextSize(LayouUtil.getDimen("m19"));
//        tvDesc.setTextColor(Color.WHITE);
        tvDesc.setText(constellationMatchingData.desc);
        tvDesc.setMaxHeight((int)(27.2 * unit));
        tvDesc.setLineSpacing(tvDescHeight,0);
        tvDesc.setMaxLines(mTvDescHeight/tvDescHeight);
        tvDesc.setEllipsize(TextUtils.TruncateAt.END);
        contentView.addView(tvDesc);

        TextViewUtil.setTextSize(tvName,tvNameSize);
        TextViewUtil.setTextColor(tvName,tvNameColor);
        TextViewUtil.setTextSize(tvMatchTitle,tvMatchTitleSize);
        TextViewUtil.setTextColor(tvMatchTitle,tvMatchTitleColor);
        TextViewUtil.setTextSize(tvMatch,tvMatchSize);
        TextViewUtil.setTextColor(tvMatch,tvMatchColor);
        TextViewUtil.setTextSize(tvMatchName,tvNameSize);
        TextViewUtil.setTextColor(tvMatchName,tvNameColor);
        TextViewUtil.setTextSize(tvDesc,tvDescSize);
        TextViewUtil.setTextColor(tvDesc,tvDescColor);

        return root;
    }

    private View createViewFull(ConstellationMatchingData constellationMatchingData) {
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
//        root.addView(titleView);
        ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(null, "constellation", "星座");
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, SizeConfig.titleHeight);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        root.addView(titleViewAdapter.view, layoutParams);

        LinearLayout contentView = new LinearLayout(GlobalContext.get());
        contentView.setBackground(LayouUtil.getDrawable("white_range_layout"));
        RelativeLayout.LayoutParams contentViewLayoutParam = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, countHeight);
//        contentViewLayoutParam.topMargin = (int) LayouUtil.getDimen("y8");
        contentView.setGravity(Gravity.CENTER);
        contentView.setOrientation(LinearLayout.VERTICAL);
        contentView.setLayoutParams(contentViewLayoutParam);

        RelativeLayout topLayout = new RelativeLayout(GlobalContext.get());
        topLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        RelativeLayout.LayoutParams topLayoutLayoutParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        topLayout.setLayoutParams(topLayoutLayoutParam);


        ImageView ivName = new ImageView(GlobalContext.get());
        ivName.setId(ViewUtils.generateViewId());
        ivName.setImageDrawable(LayouUtil.getDrawable(com.txznet.comm.ui.viewfactory.view.defaults.ConstellationMatchingView.getConstellationPictureByName(constellationMatchingData.name)));
        RelativeLayout.LayoutParams ivNameRelativeLayoutParam = new RelativeLayout.LayoutParams(ivNameWidth, ivNameWidth);
//        ivNameRelativeLayoutParam.topMargin = (int) LayouUtil.getDimen("y41");
        ivNameRelativeLayoutParam.bottomMargin = ivNameBottomMaigin;
        ivName.setLayoutParams(ivNameRelativeLayoutParam);

        TextView tvName = new TextView(GlobalContext.get());
        RelativeLayout.LayoutParams tvNameLayoutParam = new RelativeLayout.LayoutParams(ivNameWidth, tvNameHeight);
//        tvNameLayoutParam.topMargin = (int) LayouUtil.getDimen("y8");
        tvNameLayoutParam.addRule(RelativeLayout.BELOW, ivName.getId());
        tvNameLayoutParam.addRule(RelativeLayout.ALIGN_LEFT, ivName.getId());
        tvName.setId(ViewUtils.generateViewId());
//        tvName.setTextSize(LayouUtil.getDimen("m16"));
//        tvName.setTextColor(Color.WHITE);
        tvName.setText(constellationMatchingData.name);
        tvName.setGravity(Gravity.CENTER);
        tvName.setLayoutParams(tvNameLayoutParam);

        LinearLayout llMatch = new LinearLayout(GlobalContext.get());
        llMatch.setId(ViewUtils.generateViewId());
        RelativeLayout.LayoutParams llMatchLayoutParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        llMatchLayoutParam.addRule(RelativeLayout.RIGHT_OF, ivName.getId());
        llMatchLayoutParam.addRule(RelativeLayout.CENTER_IN_PARENT);
//        llMatchLayoutParam.addRule(RelativeLayout.ALIGN_TOP, ivName.getId());
//        llMatchLayoutParam.leftMargin = (int) LayouUtil.getDimen("x32");
        llMatchLayoutParam.leftMargin = llMatchLeftMargin;
        llMatchLayoutParam.rightMargin = llMatchLeftMargin;
        llMatch.setGravity(Gravity.CENTER);
        llMatch.setOrientation(LinearLayout.VERTICAL);
        llMatch.setLayoutParams(llMatchLayoutParam);

        TextView tvMatchTitle = new TextView(GlobalContext.get());
        RelativeLayout.LayoutParams tvMatchTitleLayoutParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvMatchTitleLayoutParam.bottomMargin = unit;
        tvMatchTitle.setId(ViewUtils.generateViewId());
        tvMatchTitle.setGravity(Gravity.CENTER);
//        tvMatchTitle.setTextSize(LayouUtil.getDimen("m19"));
        tvMatchTitle.setText("匹配度");
        tvMatchTitle.setLayoutParams(tvMatchTitleLayoutParam);

        TextView tvMatch = new TextView(GlobalContext.get());
        RelativeLayout.LayoutParams tvMatchLayoutParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        tvMatchLayoutParam.topMargin = (int) LayouUtil.getDimen("y8");
        tvMatch.setId(ViewUtils.generateViewId());
        tvMatch.setGravity(Gravity.CENTER);
        tvMatch.setText(constellationMatchingData.level + "%");
//        tvMatch.setTextSize(LayouUtil.getDimen("m38"));
//        tvMatch.setTextColor(Color.parseColor("#FFFFCA00"));
        tvMatch.setLayoutParams(tvMatchLayoutParam);

        llMatch.addView(tvMatchTitle);
        llMatch.addView(tvMatch);

        ImageView ivMatchName = new ImageView(GlobalContext.get());
        ivMatchName.setId(ViewUtils.generateViewId());
        ivMatchName.setImageDrawable(LayouUtil.getDrawable(com.txznet.comm.ui.viewfactory.view.defaults.ConstellationMatchingView.getConstellationPictureByName(constellationMatchingData.matchName)));
        RelativeLayout.LayoutParams ivMatchNameRelativeLayoutParam = new RelativeLayout.LayoutParams(ivNameWidth,ivNameWidth);
//        ivMatchNameRelativeLayoutParam.leftMargin = (int) LayouUtil.getDimen("x32");
        ivMatchNameRelativeLayoutParam.bottomMargin = ivNameBottomMaigin;
        ivMatchNameRelativeLayoutParam.addRule(RelativeLayout.RIGHT_OF, llMatch.getId());
        ivMatchNameRelativeLayoutParam.addRule(RelativeLayout.ALIGN_TOP, llMatch.getId());
        ivMatchName.setLayoutParams(ivMatchNameRelativeLayoutParam);

        TextView tvMatchName = new TextView(GlobalContext.get());
        RelativeLayout.LayoutParams tvMatchNameLayoutParam = new RelativeLayout.LayoutParams(ivNameWidth, tvNameHeight);
//        tvMatchNameLayoutParam.topMargin = (int) LayouUtil.getDimen("y8");
        tvMatchNameLayoutParam.addRule(RelativeLayout.BELOW, ivMatchName.getId());
        tvMatchNameLayoutParam.addRule(RelativeLayout.ALIGN_LEFT, ivMatchName.getId());
        tvMatchName.setId(ViewUtils.generateViewId());
//        tvMatchName.setTextSize(LayouUtil.getDimen("m16"));
//        tvMatchName.setTextColor(Color.WHITE);
        tvMatchName.setText(constellationMatchingData.matchName);
        tvMatchName.setGravity(Gravity.CENTER);
        tvMatchName.setLayoutParams(tvMatchNameLayoutParam);

        topLayout.addView(ivName);
        topLayout.addView(tvName);
        topLayout.addView(ivMatchName);
        topLayout.addView(tvMatchName);
        topLayout.addView(llMatch);
        contentView.addView(topLayout);

        root.addView(contentView);

        AutoSplitTextView tvDesc = new AutoSplitTextView(GlobalContext.get());
        LinearLayout.LayoutParams tvDescLayoutParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvDescLayoutParam.topMargin = 2 * unit;
        tvDescLayoutParam.leftMargin = (int)(6.4 * unit);
        tvDescLayoutParam.rightMargin = (int)(6.4 * unit);
        tvDesc.setLayoutParams(tvDescLayoutParam);
//        tvDesc.setTextSize(LayouUtil.getDimen("m19"));
//        tvDesc.setTextColor(Color.WHITE);
        tvDesc.setText(constellationMatchingData.desc);
        tvDesc.setLineSpacing(tvDescHeight,0);
        tvDesc.setMaxLines(mTvDescHeight/tvDescHeight);
        tvDesc.setEllipsize(TextUtils.TruncateAt.END);
        contentView.addView(tvDesc);

        TextViewUtil.setTextSize(tvName,tvNameSize);
        TextViewUtil.setTextColor(tvName,tvNameColor);
        TextViewUtil.setTextSize(tvMatchTitle,tvMatchTitleSize);
        TextViewUtil.setTextColor(tvMatchTitle,tvMatchTitleColor);
        TextViewUtil.setTextSize(tvMatch,tvMatchSize);
        TextViewUtil.setTextColor(tvMatch,tvMatchColor);
        TextViewUtil.setTextSize(tvMatchName,tvNameSize);
        TextViewUtil.setTextColor(tvMatchName,tvNameColor);
        TextViewUtil.setTextSize(tvDesc,tvDescSize);
        TextViewUtil.setTextColor(tvDesc,tvDescColor);

        return root;
    }

    @Override
    public void init() {
        super.init();

    }

    private static HashMap<String, String> mMap = new HashMap();

    static {
        mMap.put("白羊座", "aries");
        mMap.put("金牛座", "taurus");
        mMap.put("双子座", "gemini");
        mMap.put("巨蟹座", "cancer");
        mMap.put("狮子座", "leo");
        mMap.put("处女座", "virgo");
        mMap.put("天秤座", "libra");
        mMap.put("天蝎座", "scorpio");
        mMap.put("射手座", "sagittarius");
        mMap.put("摩羯座", "capricorn");
        mMap.put("水瓶座", "aquarius");
        mMap.put("双鱼座", "pisces");
    }

    public static String getConstellationPictureByName(String name) {
        return mMap.get(name);
    }


    public void onUpdateParams(int styleIndex) {
        dividerHeight = 1;    //分隔线高度
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
    private int unit;
    private void initNone() {
        if (WinLayout.isVertScreen) {
            ivNameWidth = 10 * unit;
            ivNameBottomMaigin = unit;    //星座图片下边距
            tvNameSize = ViewParamsUtil.h3;    //星座名字字体大小
            tvNameColor = Color.parseColor(LayouUtil.getString("color_main_title"));    //星座名字字体颜色
            tvNameHeight = ViewParamsUtil.h3Height;    //星座名字字体行高
            llMatchLeftMargin = 4 * unit;    //匹配度布局左边距
            tvMatchTitleSize = ViewParamsUtil.h5;    //匹配度标题字体大小
            tvMatchTitleHeight = ViewParamsUtil.h5Height;    //匹配度标题字体行高
            tvMatchTitleColor = Color.parseColor(LayouUtil.getString("color_main_title"));    //匹配度标题字体颜色
            tvMatchSize = (int)(4.8 * unit);    //匹配度字体大小
            tvMatchHeight = tvMatchSize + 10;    //匹配度字体行高
            tvMatchColor = Color.parseColor("#FFFFCA00");    //匹配度字体颜色
            tvDescSize = ViewParamsUtil.h5;    //详情介绍字体大小
            tvDescHeight = ViewParamsUtil.h5Height;    //详情介绍字体行高
            tvDescColor = Color.parseColor(LayouUtil.getString("color_main_title"));    //详情介绍字体颜色
            mTvDescHeight = 15 * unit;
        } else {
            ivNameWidth = 8 * unit;
            ivNameBottomMaigin = unit;    //星座图片下边距
            tvNameSize = ViewParamsUtil.h5;    //星座名字字体大小
            tvNameColor = Color.parseColor(LayouUtil.getString("color_main_title"));    //星座名字字体颜色
            tvNameHeight = ViewParamsUtil.h5Height;    //星座名字字体行高
            llMatchLeftMargin = 4 * unit;    //匹配度布局左边距
            tvMatchTitleSize = ViewParamsUtil.h7;    //匹配度标题字体大小
            tvMatchTitleHeight = ViewParamsUtil.h7Height;    //匹配度标题字体行高
            tvMatchTitleColor = Color.parseColor(LayouUtil.getString("color_main_title"));    //匹配度标题字体颜色
            tvMatchSize = ViewParamsUtil.h1;    //匹配度字体大小
            tvMatchHeight = ViewParamsUtil.h1Height;    //匹配度字体行高
            tvMatchColor = Color.parseColor("#FFFFCA00");    //匹配度字体颜色
            tvDescSize = ViewParamsUtil.h6;    //详情介绍字体大小
            tvDescHeight = ViewParamsUtil.h6Height;    //详情介绍字体行高
            tvDescColor = Color.parseColor(LayouUtil.getString("color_main_title"));    //详情介绍字体颜色
            mTvDescHeight = 15 * unit;
        }
    }

    private void initHalf() {
        if (WinLayout.isVertScreen) {
            countHeight = 46 * unit;
            ivNameWidth = 10 * unit;
            ivNameBottomMaigin = unit;    //星座图片下边距
            tvNameSize = ViewParamsUtil.h3;    //星座名字字体大小
            tvNameColor = Color.parseColor(LayouUtil.getString("color_main_title"));    //星座名字字体颜色
            tvNameHeight = ViewParamsUtil.h3Height;    //星座名字字体行高
            llMatchLeftMargin = 4 * unit;    //匹配度布局左边距
            tvMatchTitleSize = ViewParamsUtil.h5;    //匹配度标题字体大小
            tvMatchTitleHeight = ViewParamsUtil.h5Height;    //匹配度标题字体行高
            tvMatchTitleColor = Color.parseColor(LayouUtil.getString("color_main_title"));    //匹配度标题字体颜色
            tvMatchSize = (int)(4.8 * unit);    //匹配度字体大小
            tvMatchHeight = tvMatchSize + 10;    //匹配度字体行高
            tvMatchColor = Color.parseColor("#FFFFCA00");    //匹配度字体颜色
            tvDescSize = ViewParamsUtil.h5;    //详情介绍字体大小
            tvDescHeight = ViewParamsUtil.h5Height;    //详情介绍字体行高
            tvDescColor = Color.parseColor(LayouUtil.getString("color_main_title"));    //详情介绍字体颜色
            mTvDescHeight = 17 * unit;
        } else {
            countHeight = SizeConfig.pagePoiCount * SizeConfig.itemHeight;
            ivNameWidth = 10 * unit;
            ivNameBottomMaigin = unit;    //星座图片下边距
            tvNameSize = ViewParamsUtil.h3;    //星座名字字体大小
            tvNameColor = Color.parseColor(LayouUtil.getString("color_main_title"));    //星座名字字体颜色
            tvNameHeight = ViewParamsUtil.h3Height;    //星座名字字体行高
            llMatchLeftMargin = 4 * unit;    //匹配度布局左边距
            tvMatchTitleSize = ViewParamsUtil.h5;    //匹配度标题字体大小
            tvMatchTitleHeight = ViewParamsUtil.h5Height;    //匹配度标题字体行高
            tvMatchTitleColor = Color.parseColor(LayouUtil.getString("color_main_title"));    //匹配度标题字体颜色
            tvMatchSize = (int)(4.8 * unit);    //匹配度字体大小
            tvMatchHeight = tvMatchSize + 10;    //匹配度字体行高
            tvMatchColor = Color.parseColor("#FFFFCA00");    //匹配度字体颜色
            tvDescSize = ViewParamsUtil.h5;    //详情介绍字体大小
            tvDescHeight = ViewParamsUtil.h5Height;    //详情介绍字体行高
            tvDescColor = Color.parseColor(LayouUtil.getString("color_main_title"));    //详情介绍字体颜色
            mTvDescWidth = 52 * unit;
            mTvDescHeight = (int) (27.2 * unit);
            if (SizeConfig.screenHeight < 480){
                mTvDescHeight -= SizeConfig.itemHeight;
            }
        }
    }

    private void initFull() {
        if (WinLayout.isVertScreen) {
            countHeight = 46 * unit;
            ivNameWidth = 10 * unit;
            ivNameBottomMaigin = unit;    //星座图片下边距
            tvNameSize = ViewParamsUtil.h3;    //星座名字字体大小
            tvNameColor = Color.parseColor(LayouUtil.getString("color_main_title"));    //星座名字字体颜色
            tvNameHeight = ViewParamsUtil.h3Height;    //星座名字字体行高
            llMatchLeftMargin = 4 * unit;    //匹配度布局左边距
            tvMatchTitleSize = ViewParamsUtil.h5;    //匹配度标题字体大小
            tvMatchTitleHeight = ViewParamsUtil.h5Height;    //匹配度标题字体行高
            tvMatchTitleColor = Color.parseColor(LayouUtil.getString("color_main_title"));    //匹配度标题字体颜色
            tvMatchSize = (int)(4.8 * unit);    //匹配度字体大小
            tvMatchHeight = tvMatchSize + 10;    //匹配度字体行高
            tvMatchColor = Color.parseColor("#FFFFCA00");    //匹配度字体颜色
            tvDescSize = ViewParamsUtil.h5;    //详情介绍字体大小
            tvDescHeight = ViewParamsUtil.h5Height;    //详情介绍字体行高
            tvDescColor = Color.parseColor(LayouUtil.getString("color_main_title"));    //详情介绍字体颜色
            mTvDescHeight = 17 * unit;
        } else {
            countHeight = SizeConfig.pagePoiCount * SizeConfig.itemHeight;
//            mTvDescWidth = (int)(60 * unit);
//            mTvDescHeight = (int) (23.8 * unit);
            ivNameWidth = 10 * unit;
            ivNameBottomMaigin = unit;    //星座图片下边距
            tvNameSize = ViewParamsUtil.h3;    //星座名字字体大小
            tvNameColor = Color.parseColor(LayouUtil.getString("color_main_title"));    //星座名字字体颜色
            tvNameHeight = ViewParamsUtil.h3Height;    //星座名字字体行高
            llMatchLeftMargin = 4 * unit;    //匹配度布局左边距
            tvMatchTitleSize = ViewParamsUtil.h5;    //匹配度标题字体大小
            tvMatchTitleHeight = ViewParamsUtil.h5Height;    //匹配度标题字体行高
            tvMatchTitleColor = Color.parseColor(LayouUtil.getString("color_main_title"));    //匹配度标题字体颜色
            tvMatchSize = (int)(4.8 * unit);    //匹配度字体大小
            tvMatchHeight = tvMatchSize + 10;    //匹配度字体行高
            tvMatchColor = Color.parseColor("#FFFFCA00");    //匹配度字体颜色
            tvDescSize = ViewParamsUtil.h5;    //详情介绍字体大小
            tvDescHeight = ViewParamsUtil.h5Height;    //详情介绍字体行高
            tvDescColor = Color.parseColor(LayouUtil.getString("color_main_title"));    //详情介绍字体颜色
            mTvDescHeight = (int) (23.8 * unit);
            if (SizeConfig.screenHeight < 480){
                mTvDescHeight -= SizeConfig.itemHeight;
            }
        }
    }
}
