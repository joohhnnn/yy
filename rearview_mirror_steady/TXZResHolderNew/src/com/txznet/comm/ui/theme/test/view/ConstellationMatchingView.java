package com.txznet.comm.ui.theme.test.view;

import android.graphics.Color;
import android.text.TextUtils;
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
import com.txznet.comm.ui.theme.test.config.StyleConfig;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ViewUtils;
import com.txznet.comm.ui.view.AutoSplitTextView;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.ConstellationMatchingData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IConstellationMatchingView;

import java.util.HashMap;

public class ConstellationMatchingView extends IConstellationMatchingView {

    private static ConstellationMatchingView sInstance = new ConstellationMatchingView();
    private final String mTvDescTopMargin = "y16";
    private int mTvDescWidth;
    private int mTvDescHeight;
    private int mTvDescLeftMargin;


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
                view = createViewHalf(constellationMatchingData);
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
        root.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, ((int) LayouUtil.getDimen("y480"))));
        root.setBackground(LayouUtil.getDrawable("constellation_background"));

        LinearLayout titleView = new LinearLayout(GlobalContext.get());
        titleView.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams titleViewLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        titleView.setLayoutParams(titleViewLayoutParams);
        ImageView starImageView = new ImageView(GlobalContext.get());
        starImageView.setImageDrawable(LayouUtil.getDrawable("icon_star"));
        starImageView.setLayoutParams(new ViewGroup.LayoutParams((int)LayouUtil.getDimen("m40"), (int)LayouUtil.getDimen("m40")));
        TextView tvTitle = new TextView(GlobalContext.get());
        tvTitle.setText("????????????");
        tvTitle.setTextSize(LayouUtil.getDimen("m20"));
        tvTitle.setTextColor(Color.parseColor("#FF89898A"));
        tvTitle.setGravity(Gravity.CENTER);
        titleView.addView(starImageView);
        titleView.addView(tvTitle);
        root.addView(titleView);

        RelativeLayout contentView = new RelativeLayout(GlobalContext.get());
        RelativeLayout.LayoutParams contentViewLayoutParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, (int) LayouUtil.getDimen("y384"));
        contentViewLayoutParam.topMargin = (int) LayouUtil.getDimen("y8");
        contentView.setLayoutParams(contentViewLayoutParam);

        TextView tvMatchTitle = new TextView(GlobalContext.get());
        RelativeLayout.LayoutParams tvMatchTitleLayoutParam = new RelativeLayout.LayoutParams(((int) LayouUtil.getDimen("x120")), ViewGroup.LayoutParams.WRAP_CONTENT);
        tvMatchTitleLayoutParam.topMargin = (int) LayouUtil.getDimen("y16");
        tvMatchTitleLayoutParam.addRule(RelativeLayout.CENTER_HORIZONTAL);
        tvMatchTitle.setGravity(Gravity.CENTER);
        tvMatchTitle.setId(ViewUtils.generateViewId());
        tvMatchTitle.setTextSize(LayouUtil.getDimen("m16"));
        tvMatchTitle.setText("?????????");
        tvMatchTitle.setLayoutParams(tvMatchTitleLayoutParam);

        TextView tvMatch = new TextView(GlobalContext.get());
        RelativeLayout.LayoutParams tvMatchLayoutParam = new RelativeLayout.LayoutParams(((int) LayouUtil.getDimen("x120")), ViewGroup.LayoutParams.WRAP_CONTENT);
        tvMatchLayoutParam.addRule(RelativeLayout.CENTER_HORIZONTAL);
        tvMatchLayoutParam.addRule(RelativeLayout.BELOW, tvMatchTitle.getId());
        tvMatch.setId(ViewUtils.generateViewId());
        tvMatch.setGravity(Gravity.CENTER);
        tvMatch.setText(constellationMatchingData.level + "%");
        tvMatch.setTextSize(LayouUtil.getDimen("m23"));
        tvMatch.setTextColor(Color.parseColor("#FFFFCA00"));
        tvMatch.setLayoutParams(tvMatchLayoutParam);

        ImageView ivName = new ImageView(GlobalContext.get());
        ivName.setId(ViewUtils.generateViewId());
        ivName.setImageDrawable(LayouUtil.getDrawable(ConstellationMatchingView.getConstellationPictureByName(constellationMatchingData.name)));
        RelativeLayout.LayoutParams ivNameRelativeLayoutParam = new RelativeLayout.LayoutParams((int) LayouUtil.getDimen("m64"), (int) LayouUtil.getDimen("m64"));
        ivNameRelativeLayoutParam.leftMargin = (int) LayouUtil.getDimen("y32");
        ivNameRelativeLayoutParam.topMargin = (int) LayouUtil.getDimen("y16");
        ivName.setLayoutParams(ivNameRelativeLayoutParam);

        TextView tvName = new TextView(GlobalContext.get());
        RelativeLayout.LayoutParams tvNameLayoutParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, (int) LayouUtil.getDimen("m64"));
        tvNameLayoutParam.leftMargin = (int) LayouUtil.getDimen("x16");
        tvNameLayoutParam.topMargin = (int) LayouUtil.getDimen("y16");
        tvNameLayoutParam.addRule(RelativeLayout.RIGHT_OF, ivName.getId());
        tvName.setId(ViewUtils.generateViewId());
        tvName.setTextSize(LayouUtil.getDimen("m16"));
        tvName.setTextColor(Color.WHITE);
        tvName.setText(constellationMatchingData.name);
        tvName.setGravity(Gravity.CENTER);
        tvName.setLayoutParams(tvNameLayoutParam);

        ImageView ivMatchName = new ImageView(GlobalContext.get());
        ivMatchName.setId(ViewUtils.generateViewId());
        ivMatchName.setImageDrawable(LayouUtil.getDrawable(com.txznet.comm.ui.viewfactory.view.defaults.ConstellationMatchingView.getConstellationPictureByName(constellationMatchingData.matchName)));
        RelativeLayout.LayoutParams ivMatchNameRelativeLayoutParam = new RelativeLayout.LayoutParams((int) LayouUtil.getDimen("m64"), (int) LayouUtil.getDimen("m64"));
        ivMatchNameRelativeLayoutParam.rightMargin = (int) LayouUtil.getDimen("y32");
        ivMatchNameRelativeLayoutParam.addRule(RelativeLayout.ALIGN_TOP, ivName.getId());
        ivMatchNameRelativeLayoutParam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        ivMatchName.setLayoutParams(ivMatchNameRelativeLayoutParam);

        TextView tvMatchName = new TextView(GlobalContext.get());
        RelativeLayout.LayoutParams tvMatchNameLayoutParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, (int) LayouUtil.getDimen("m64"));
        tvMatchNameLayoutParam.addRule(RelativeLayout.LEFT_OF, ivMatchName.getId());
        tvMatchNameLayoutParam.addRule(RelativeLayout.ALIGN_TOP, tvName.getId());
        tvMatchName.setId(ViewUtils.generateViewId());
        tvMatchNameLayoutParam.rightMargin = (int) LayouUtil.getDimen("x8");
        tvMatchName.setTextSize(LayouUtil.getDimen("m16"));
        tvMatchName.setTextColor(Color.WHITE);
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
        RelativeLayout.LayoutParams tvDescLayoutParam = new RelativeLayout.LayoutParams(mTvDescWidth, mTvDescHeight);
        tvDescLayoutParam.topMargin = (int) LayouUtil.getDimen("y16");
        tvDescLayoutParam.addRule(RelativeLayout.BELOW, ivName.getId());
        tvDescLayoutParam.addRule(RelativeLayout.CENTER_HORIZONTAL);
        tvDesc.setLayoutParams(tvDescLayoutParam);
        tvDesc.setTextSize(LayouUtil.getDimen("m18"));
        tvDesc.setTextColor(Color.WHITE);
        tvDesc.setText(constellationMatchingData.desc);
        tvDesc.setMaxLines(mTvDescHeight/tvDesc.getLineHeight());
        tvDesc.setEllipsize(TextUtils.TruncateAt.END);
        contentView.addView(tvDesc);

        root.addView(contentView);
        return root;
    }

    private View createViewHalf(ConstellationMatchingData constellationMatchingData) {
        LinearLayout root = new LinearLayout(GlobalContext.get());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, ((int) LayouUtil.getDimen("y480"))));

        LinearLayout titleView = new LinearLayout(GlobalContext.get());
        titleView.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams titleViewLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        titleViewLayoutParams.topMargin = (int) LayouUtil.getDimen("y32");
        titleView.setLayoutParams(titleViewLayoutParams);
        ImageView starImageView = new ImageView(GlobalContext.get());
        starImageView.setImageDrawable(LayouUtil.getDrawable("icon_star"));
        starImageView.setLayoutParams(new ViewGroup.LayoutParams((int) LayouUtil.getDimen("m40"), (int) LayouUtil.getDimen("m40")));
        TextView tvTitle = new TextView(GlobalContext.get());
        tvTitle.setText("????????????");
        tvTitle.setTextSize(LayouUtil.getDimen("m23"));
        tvTitle.setTextColor(Color.parseColor("#FF89898A"));
        tvTitle.setGravity(Gravity.CENTER);
        titleView.addView(starImageView);
        titleView.addView(tvTitle);
        root.addView(titleView);

        RelativeLayout contentView = new RelativeLayout(GlobalContext.get());
        contentView.setBackground(LayouUtil.getDrawable("constellation_background"));
        contentView.setGravity(Gravity.CENTER);
        RelativeLayout.LayoutParams contentViewLayoutParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        contentViewLayoutParam.topMargin = (int) LayouUtil.getDimen("y8");
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
        RelativeLayout.LayoutParams ivNameRelativeLayoutParam = new RelativeLayout.LayoutParams((int) LayouUtil.getDimen("m80"), (int) LayouUtil.getDimen("m80"));
        ivName.setLayoutParams(ivNameRelativeLayoutParam);

        TextView tvName = new TextView(GlobalContext.get());
        RelativeLayout.LayoutParams tvNameLayoutParam = new RelativeLayout.LayoutParams((int) LayouUtil.getDimen("m80"), ViewGroup.LayoutParams.WRAP_CONTENT);
        tvNameLayoutParam.topMargin = (int) LayouUtil.getDimen("y8");
        tvNameLayoutParam.addRule(RelativeLayout.BELOW, ivName.getId());
        tvNameLayoutParam.addRule(RelativeLayout.ALIGN_LEFT, ivName.getId());
        tvName.setId(ViewUtils.generateViewId());
        tvName.setTextSize(LayouUtil.getDimen("m16"));
        tvName.setTextColor(Color.WHITE);
        tvName.setText(constellationMatchingData.name);
        tvName.setGravity(Gravity.CENTER);
        tvName.setLayoutParams(tvNameLayoutParam);

        LinearLayout llMatch = new LinearLayout(GlobalContext.get());
        int llMatchWidth = (int) (LayouUtil.getDimen("m80") * 2 + LayouUtil.getDimen("x54"));
        llMatch.setId(ViewUtils.generateViewId());
        RelativeLayout.LayoutParams llMatchLayoutParam = new RelativeLayout.LayoutParams(llMatchWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
        llMatchLayoutParam.addRule(RelativeLayout.BELOW, tvName.getId());
        llMatchLayoutParam.topMargin = (int) LayouUtil.getDimen("y16");
        llMatch.setGravity(Gravity.CENTER_HORIZONTAL);
        llMatch.setOrientation(LinearLayout.VERTICAL);
        llMatch.setLayoutParams(llMatchLayoutParam);

        TextView tvMatchTitle = new TextView(GlobalContext.get());
        RelativeLayout.LayoutParams tvMatchTitleLayoutParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvMatchTitle.setGravity(Gravity.CENTER);
        tvMatchTitle.setId(ViewUtils.generateViewId());
        tvMatchTitle.setTextSize(LayouUtil.getDimen("m19"));
        tvMatchTitle.setText("?????????");
        tvMatchTitle.setLayoutParams(tvMatchTitleLayoutParam);

        TextView tvMatch = new TextView(GlobalContext.get());
        RelativeLayout.LayoutParams tvMatchLayoutParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvMatch.setId(ViewUtils.generateViewId());
        tvMatch.setGravity(Gravity.CENTER);
        tvMatch.setText(constellationMatchingData.level + "%");
        tvMatch.setTextSize(LayouUtil.getDimen("m39"));
        tvMatch.setTextColor(Color.parseColor("#FFFFCA00"));
        tvMatch.setLayoutParams(tvMatchLayoutParam);

        ImageView ivMatchName = new ImageView(GlobalContext.get());
        ivMatchName.setId(ViewUtils.generateViewId());
        ivMatchName.setImageDrawable(LayouUtil.getDrawable(com.txznet.comm.ui.viewfactory.view.defaults.ConstellationMatchingView.getConstellationPictureByName(constellationMatchingData.matchName)));
        RelativeLayout.LayoutParams ivMatchNameRelativeLayoutParam = new RelativeLayout.LayoutParams((int) LayouUtil.getDimen("m80"), (int) LayouUtil.getDimen("m80"));
        ivMatchNameRelativeLayoutParam.leftMargin = (int) LayouUtil.getDimen("x54");
        ivMatchNameRelativeLayoutParam.addRule(RelativeLayout.RIGHT_OF, ivName.getId());
        ivMatchNameRelativeLayoutParam.addRule(RelativeLayout.ALIGN_TOP, ivName.getId());
        ivMatchName.setLayoutParams(ivMatchNameRelativeLayoutParam);

        TextView tvMatchName = new TextView(GlobalContext.get());
        RelativeLayout.LayoutParams tvMatchNameLayoutParam = new RelativeLayout.LayoutParams((int) LayouUtil.getDimen("m80"), ViewGroup.LayoutParams.WRAP_CONTENT);
        tvMatchNameLayoutParam.topMargin = (int) LayouUtil.getDimen("y8");
        tvMatchNameLayoutParam.addRule(RelativeLayout.BELOW, ivMatchName.getId());
        tvMatchNameLayoutParam.addRule(RelativeLayout.ALIGN_LEFT, ivMatchName.getId());
        tvMatchName.setId(ViewUtils.generateViewId());
        tvMatchName.setTextSize(LayouUtil.getDimen("m16"));
        tvMatchName.setTextColor(Color.WHITE);
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
        RelativeLayout.LayoutParams tvDescLayoutParam = new RelativeLayout.LayoutParams(mTvDescWidth, mTvDescHeight);
        tvDescLayoutParam.leftMargin = mTvDescLeftMargin;
        tvDescLayoutParam.addRule(RelativeLayout.RIGHT_OF, leftContent.getId());
        tvDescLayoutParam.addRule(RelativeLayout.CENTER_VERTICAL);
        tvDesc.setLayoutParams(tvDescLayoutParam);
        tvDesc.setTextSize(LayouUtil.getDimen("m19"));
        tvDesc.setTextColor(Color.WHITE);
        tvDesc.setText(constellationMatchingData.desc);
        tvDesc.setMaxLines(mTvDescHeight/tvDesc.getLineHeight());
        tvDesc.setEllipsize(TextUtils.TruncateAt.END);
        contentView.addView(tvDesc);

        return root;
    }

    private View createViewFull(ConstellationMatchingData constellationMatchingData) {
        LinearLayout root = new LinearLayout(GlobalContext.get());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, ((int) LayouUtil.getDimen("y480"))));

        LinearLayout titleView = new LinearLayout(GlobalContext.get());
        titleView.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams titleViewLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        titleViewLayoutParams.topMargin = (int) LayouUtil.getDimen("y32");
        titleView.setLayoutParams(titleViewLayoutParams);
        ImageView starImageView = new ImageView(GlobalContext.get());
        starImageView.setImageDrawable(LayouUtil.getDrawable("icon_star"));
        starImageView.setLayoutParams(new ViewGroup.LayoutParams((int) LayouUtil.getDimen("m40"), (int) LayouUtil.getDimen("m40")));
        TextView tvTitle = new TextView(GlobalContext.get());
        tvTitle.setText("????????????");
        tvTitle.setTextSize(LayouUtil.getDimen("m23"));
        tvTitle.setTextColor(Color.parseColor("#FF89898A"));
        tvTitle.setGravity(Gravity.CENTER);
        titleView.addView(starImageView);
        titleView.addView(tvTitle);
        root.addView(titleView);

        LinearLayout contentView = new LinearLayout(GlobalContext.get());
        contentView.setBackground(LayouUtil.getDrawable("constellation_background"));
        RelativeLayout.LayoutParams contentViewLayoutParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, (int) LayouUtil.getDimen("y384"));
        contentViewLayoutParam.topMargin = (int) LayouUtil.getDimen("y8");
        contentView.setGravity(Gravity.CENTER_HORIZONTAL);
        contentView.setOrientation(LinearLayout.VERTICAL);
        contentView.setLayoutParams(contentViewLayoutParam);

        RelativeLayout topLayout = new RelativeLayout(GlobalContext.get());
        topLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        RelativeLayout.LayoutParams topLayoutLayoutParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        topLayout.setLayoutParams(topLayoutLayoutParam);


        ImageView ivName = new ImageView(GlobalContext.get());
        ivName.setId(ViewUtils.generateViewId());
        ivName.setImageDrawable(LayouUtil.getDrawable(com.txznet.comm.ui.viewfactory.view.defaults.ConstellationMatchingView.getConstellationPictureByName(constellationMatchingData.name)));
        RelativeLayout.LayoutParams ivNameRelativeLayoutParam = new RelativeLayout.LayoutParams((int) LayouUtil.getDimen("m80"), (int) LayouUtil.getDimen("m80"));
        ivNameRelativeLayoutParam.topMargin = (int) LayouUtil.getDimen("y41");
        ivName.setLayoutParams(ivNameRelativeLayoutParam);

        TextView tvName = new TextView(GlobalContext.get());
        RelativeLayout.LayoutParams tvNameLayoutParam = new RelativeLayout.LayoutParams((int) LayouUtil.getDimen("m80"), ViewGroup.LayoutParams.WRAP_CONTENT);
        tvNameLayoutParam.topMargin = (int) LayouUtil.getDimen("y8");
        tvNameLayoutParam.addRule(RelativeLayout.BELOW, ivName.getId());
        tvNameLayoutParam.addRule(RelativeLayout.ALIGN_LEFT, ivName.getId());
        tvName.setId(ViewUtils.generateViewId());
        tvName.setTextSize(LayouUtil.getDimen("m16"));
        tvName.setTextColor(Color.WHITE);
        tvName.setText(constellationMatchingData.name);
        tvName.setGravity(Gravity.CENTER);
        tvName.setLayoutParams(tvNameLayoutParam);

        LinearLayout llMatch = new LinearLayout(GlobalContext.get());
        llMatch.setId(ViewUtils.generateViewId());
        RelativeLayout.LayoutParams llMatchLayoutParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        llMatchLayoutParam.addRule(RelativeLayout.RIGHT_OF, ivName.getId());
        llMatchLayoutParam.addRule(RelativeLayout.ALIGN_TOP, ivName.getId());
        llMatchLayoutParam.leftMargin = (int) LayouUtil.getDimen("x32");
        llMatch.setGravity(Gravity.CENTER_HORIZONTAL);
        llMatch.setOrientation(LinearLayout.VERTICAL);
        llMatch.setLayoutParams(llMatchLayoutParam);

        TextView tvMatchTitle = new TextView(GlobalContext.get());
        RelativeLayout.LayoutParams tvMatchTitleLayoutParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvMatchTitle.setId(ViewUtils.generateViewId());
        tvMatchTitle.setGravity(Gravity.CENTER);
        tvMatchTitle.setTextSize(LayouUtil.getDimen("m19"));
        tvMatchTitle.setText("?????????");
        tvMatchTitle.setLayoutParams(tvMatchTitleLayoutParam);

        TextView tvMatch = new TextView(GlobalContext.get());
        RelativeLayout.LayoutParams tvMatchLayoutParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvMatchLayoutParam.topMargin = (int) LayouUtil.getDimen("y8");
        tvMatch.setId(ViewUtils.generateViewId());
        tvMatch.setGravity(Gravity.CENTER);
        tvMatch.setText(constellationMatchingData.level + "%");
        tvMatch.setTextSize(LayouUtil.getDimen("m38"));
        tvMatch.setTextColor(Color.parseColor("#FFFFCA00"));
        tvMatch.setLayoutParams(tvMatchLayoutParam);

        llMatch.addView(tvMatchTitle);
        llMatch.addView(tvMatch);

        ImageView ivMatchName = new ImageView(GlobalContext.get());
        ivMatchName.setId(ViewUtils.generateViewId());
        ivMatchName.setImageDrawable(LayouUtil.getDrawable(com.txznet.comm.ui.viewfactory.view.defaults.ConstellationMatchingView.getConstellationPictureByName(constellationMatchingData.matchName)));
        RelativeLayout.LayoutParams ivMatchNameRelativeLayoutParam = new RelativeLayout.LayoutParams((int) LayouUtil.getDimen("m80"), (int) LayouUtil.getDimen("m80"));
        ivMatchNameRelativeLayoutParam.leftMargin = (int) LayouUtil.getDimen("x32");
        ivMatchNameRelativeLayoutParam.addRule(RelativeLayout.RIGHT_OF, llMatch.getId());
        ivMatchNameRelativeLayoutParam.addRule(RelativeLayout.ALIGN_TOP, llMatch.getId());
        ivMatchName.setLayoutParams(ivMatchNameRelativeLayoutParam);

        TextView tvMatchName = new TextView(GlobalContext.get());
        RelativeLayout.LayoutParams tvMatchNameLayoutParam = new RelativeLayout.LayoutParams((int) LayouUtil.getDimen("m80"), ViewGroup.LayoutParams.WRAP_CONTENT);
        tvMatchNameLayoutParam.topMargin = (int) LayouUtil.getDimen("y8");
        tvMatchNameLayoutParam.addRule(RelativeLayout.BELOW, ivMatchName.getId());
        tvMatchNameLayoutParam.addRule(RelativeLayout.ALIGN_LEFT, ivMatchName.getId());
        tvMatchName.setId(ViewUtils.generateViewId());
        tvMatchName.setTextSize(LayouUtil.getDimen("m16"));
        tvMatchName.setTextColor(Color.WHITE);
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
        LinearLayout.LayoutParams tvDescLayoutParam = new LinearLayout.LayoutParams(mTvDescWidth, mTvDescHeight);
        tvDescLayoutParam.topMargin = (int) LayouUtil.getDimen(mTvDescTopMargin);
        tvDesc.setLayoutParams(tvDescLayoutParam);
        tvDesc.setTextSize(LayouUtil.getDimen("m19"));
        tvDesc.setTextColor(Color.WHITE);
        tvDesc.setText(constellationMatchingData.desc);
        tvDesc.setMaxLines(mTvDescHeight/tvDesc.getLineHeight());
        tvDesc.setEllipsize(TextUtils.TruncateAt.END);
        contentView.addView(tvDesc);

        return root;
    }

    @Override
    public void init() {
        super.init();

    }

    private static HashMap<String, String> mMap = new HashMap();

    static {
        mMap.put("?????????", "aries");
        mMap.put("?????????", "taurus");
        mMap.put("?????????", "gemini");
        mMap.put("?????????", "cancer");
        mMap.put("?????????", "leo");
        mMap.put("?????????", "virgo");
        mMap.put("?????????", "libra");
        mMap.put("?????????", "scorpio");
        mMap.put("?????????", "sagittarius");
        mMap.put("?????????", "capricorn");
        mMap.put("?????????", "aquarius");
        mMap.put("?????????", "pisces");
    }

    public static String getConstellationPictureByName(String name) {
        return mMap.get(name);
    }


    public void onUpdateParams(int styleIndex) {
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
            unit = (int) LayouUtil.getDimen("vertical_unit");
            mTvDescWidth = (int)(48.3 * unit);
            mTvDescHeight = (int) (13.3 * unit);

        } else {
            unit = (int) LayouUtil.getDimen("unit");
            mTvDescWidth = (int)(58 * unit);
            mTvDescHeight = 16 * unit;
        }
    }

    private void initHalf() {
        if (WinLayout.isVertScreen) {
            unit = (int) LayouUtil.getDimen("vertical_unit");
            mTvDescHeight = (int) (22.6 * unit);
            mTvDescWidth = (int)(45.3 * unit);
            mTvDescLeftMargin = (int) (4.25 * unit);
        } else {
            unit = (int) LayouUtil.getDimen("unit");
            mTvDescHeight = (int) (27.2 * unit);
            mTvDescWidth = (int)(54.4 * unit);
            mTvDescLeftMargin = (int) (5.1 * unit);
        }
    }

    private void initFull() {
        if (WinLayout.isVertScreen) {
            unit = (int) LayouUtil.getDimen("vertical_unit");
            mTvDescWidth = (int)(50 * unit);
            mTvDescHeight = (int) (19.8 * unit);
        } else {
            unit = (int) LayouUtil.getDimen("unit");
            mTvDescWidth = (int)(60 * unit);
            mTvDescHeight = (int) (23.8 * unit);
        }
    }
}
