package com.txznet.comm.ui.viewfactory.view.defaults;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
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

    private ConstellationMatchingView() {
    }

    public static ConstellationMatchingView getInstance() {
        return sInstance;
    }

    @Override
    public ViewAdapter getView(ViewData data) {
        ConstellationMatchingData constellationMatchingData = (ConstellationMatchingData) data;
        LinearLayout root = new LinearLayout(GlobalContext.get());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, ((int) LayouUtil.getDimen("y480"))));

        LinearLayout titleView = new LinearLayout(GlobalContext.get());
        titleView.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams titleViewLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        titleViewLayoutParams.topMargin = (int)LayouUtil.getDimen("y32");
        titleView.setLayoutParams(titleViewLayoutParams);
        ImageView starImageView = new ImageView(GlobalContext.get());
        starImageView.setImageDrawable(LayouUtil.getDrawable("icon_star"));
        starImageView.setLayoutParams(new ViewGroup.LayoutParams((int)LayouUtil.getDimen("m40"), (int)LayouUtil.getDimen("m40")));
        TextView tvTitle = new TextView(GlobalContext.get());
        tvTitle.setText("星座运势");
        TextViewUtil.setTextSize(tvTitle, LayouUtil.getDimen("m22"));
        tvTitle.setTextColor(Color.parseColor("#FF89898A"));
        tvTitle.setGravity(Gravity.CENTER);
        titleView.addView(starImageView);
        titleView.addView(tvTitle);

        root.addView(titleView);


        RelativeLayout contentView = new RelativeLayout(GlobalContext.get());
        contentView.setBackground(LayouUtil.getDrawable("constellation_background"));
        contentView.setGravity(Gravity.CENTER_HORIZONTAL);
        RelativeLayout.LayoutParams contentViewLayoutParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, (int) LayouUtil.getDimen("y384"));
        contentViewLayoutParam.topMargin = (int) LayouUtil.getDimen("y8");
        contentView.setLayoutParams(contentViewLayoutParam);

        ImageView ivName = new ImageView(GlobalContext.get());
        ivName.setId(ViewUtils.generateViewId());
        ivName.setImageDrawable(LayouUtil.getDrawable(ConstellationMatchingView.getConstellationPictureByName(constellationMatchingData.name)));
        RelativeLayout.LayoutParams ivNameRelativeLayoutParam = new RelativeLayout.LayoutParams((int) LayouUtil.getDimen("m80"), (int) LayouUtil.getDimen("m80"));
        ivNameRelativeLayoutParam.topMargin = (int) LayouUtil.getDimen("y41");
        ivNameRelativeLayoutParam.leftMargin = (int) LayouUtil.getDimen("x75");
        ivName.setLayoutParams(ivNameRelativeLayoutParam);

        TextView tvName = new TextView(GlobalContext.get());
        RelativeLayout.LayoutParams tvNameLayoutParam = new RelativeLayout.LayoutParams((int) LayouUtil.getDimen("m80"), ViewGroup.LayoutParams.WRAP_CONTENT);
        tvNameLayoutParam.topMargin = (int) LayouUtil.getDimen("y8");
        tvNameLayoutParam.addRule(RelativeLayout.BELOW, ivName.getId());
        tvNameLayoutParam.addRule(RelativeLayout.ALIGN_LEFT, ivName.getId());
        tvName.setId(ViewUtils.generateViewId());
        TextViewUtil.setTextSize(tvName, LayouUtil.getDimen("m16"));
        tvName.setTextColor(Color.WHITE);
        tvName.setText(constellationMatchingData.name);
        tvName.setGravity(Gravity.CENTER);
        tvName.setLayoutParams(tvNameLayoutParam);

        TextView tvMatchTitle = new TextView(GlobalContext.get());
        RelativeLayout.LayoutParams tvMatchTitleLayoutParam = new RelativeLayout.LayoutParams(((int) LayouUtil.getDimen("x141")), ViewGroup.LayoutParams.WRAP_CONTENT);
        tvMatchTitleLayoutParam.leftMargin = (int) LayouUtil.getDimen("x32");
        tvMatchTitleLayoutParam.addRule(RelativeLayout.RIGHT_OF, ivName.getId());
        tvMatchTitleLayoutParam.addRule(RelativeLayout.ALIGN_TOP, ivName.getId());
        tvMatchTitle.setGravity(Gravity.CENTER);
        tvMatchTitle.setId(ViewUtils.generateViewId());
        TextViewUtil.setTextSize(tvMatchTitle, LayouUtil.getDimen("m19"));
        tvMatchTitle.setText("匹配度");
        tvMatchTitle.setLayoutParams(tvMatchTitleLayoutParam);

        TextView tvMatch = new TextView(GlobalContext.get());
        RelativeLayout.LayoutParams tvMatchLayoutParam = new RelativeLayout.LayoutParams(((int) LayouUtil.getDimen("x141")), ViewGroup.LayoutParams.WRAP_CONTENT);
        tvMatchLayoutParam.leftMargin = (int) LayouUtil.getDimen("x32");
        tvMatchLayoutParam.topMargin = (int) LayouUtil.getDimen("y8");
        tvMatchLayoutParam.addRule(RelativeLayout.RIGHT_OF, ivName.getId());
        tvMatchLayoutParam.addRule(RelativeLayout.BELOW, tvMatchTitle.getId());
        tvMatch.setId(ViewUtils.generateViewId());
        tvMatch.setGravity(Gravity.CENTER);
        tvMatch.setText(constellationMatchingData.level + "%");
        TextViewUtil.setTextSize(tvMatch, LayouUtil.getDimen("m39"));
        tvMatch.setTextColor(Color.parseColor("#FFFFCA00"));
        tvMatch.setLayoutParams(tvMatchLayoutParam);

        ImageView ivMatchName = new ImageView(GlobalContext.get());
        ivMatchName.setId(ViewUtils.generateViewId());
        ivMatchName.setImageDrawable(LayouUtil.getDrawable(ConstellationMatchingView.getConstellationPictureByName(constellationMatchingData.matchName)));
        RelativeLayout.LayoutParams ivMatchNameRelativeLayoutParam = new RelativeLayout.LayoutParams((int) LayouUtil.getDimen("m80"), (int) LayouUtil.getDimen("m80"));
        ivMatchNameRelativeLayoutParam.leftMargin = (int) LayouUtil.getDimen("x32");
        ivMatchNameRelativeLayoutParam.addRule(RelativeLayout.RIGHT_OF, tvMatchTitle.getId());
        ivMatchNameRelativeLayoutParam.addRule(RelativeLayout.ALIGN_TOP, tvMatchTitle.getId());
        ivMatchName.setLayoutParams(ivMatchNameRelativeLayoutParam);

        TextView tvMatchName = new TextView(GlobalContext.get());
        RelativeLayout.LayoutParams tvMatchNameLayoutParam = new RelativeLayout.LayoutParams((int) LayouUtil.getDimen("m80"), ViewGroup.LayoutParams.WRAP_CONTENT);
        tvMatchNameLayoutParam.topMargin = (int) LayouUtil.getDimen("y8");
        tvMatchNameLayoutParam.addRule(RelativeLayout.BELOW, ivMatchName.getId());
        tvMatchNameLayoutParam.addRule(RelativeLayout.ALIGN_LEFT, ivMatchName.getId());
        tvMatchName.setId(ViewUtils.generateViewId());
        TextViewUtil.setTextSize(tvMatchName, LayouUtil.getDimen("m16"));
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
        root.addView(contentView);


        AutoSplitTextView tvDesc = new AutoSplitTextView(GlobalContext.get());
        int tvDescHeight = (int) LayouUtil.getDimen("y191");
        RelativeLayout.LayoutParams tvDescLayoutParam = new RelativeLayout.LayoutParams((int) LayouUtil.getDimen("x469"), tvDescHeight);

        tvDescLayoutParam.topMargin = (int) LayouUtil.getDimen("y16");
        tvDescLayoutParam.addRule(RelativeLayout.BELOW, tvMatchName.getId());
        tvDesc.setLayoutParams(tvDescLayoutParam);
        TextViewUtil.setTextSize(tvDesc, LayouUtil.getDimen("m19"));
        tvDesc.setTextColor(Color.WHITE);
        tvDesc.setText(constellationMatchingData.desc);
        tvDesc.setMaxLines(tvDescHeight/tvDesc.getLineHeight());
        tvDesc.setEllipsize(TextUtils.TruncateAt.END);
        contentView.addView(tvDesc);

        ViewAdapter adapter = new ViewAdapter();
        adapter.type = data.getType();
        adapter.view = root;
        adapter.object = ConstellationMatchingView.getInstance();
        return adapter;
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






}
