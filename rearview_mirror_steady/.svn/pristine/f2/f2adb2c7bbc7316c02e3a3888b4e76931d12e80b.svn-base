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
import com.txznet.comm.ui.theme.test.config.StyleConfig;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ViewUtils;
import com.txznet.comm.ui.view.AutoSplitTextView;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.ConstellationFortuneData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IConstellationFortuneView;
import com.txznet.comm.ui.viewfactory.view.defaults.ConstellationMatchingView;

public class ConstellationFortuneView extends IConstellationFortuneView {

    private static ConstellationFortuneView sInstance = new ConstellationFortuneView();
    private int mTvDescWidth;
    private int mTvDescHeight;
    private int mTvDescLeftMargin;

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
                view = createViewHalf(constellationFortuneData);
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
        tvTitle.setText("星座运势");
        tvTitle.setTextSize(LayouUtil.getDimen("m23"));
        tvTitle.setTextColor(Color.parseColor("#FF89898A"));
        tvTitle.setGravity(Gravity.CENTER);
        titleView.addView(starImageView);
        titleView.addView(tvTitle);


        LinearLayout contentView = new LinearLayout(GlobalContext.get());
        contentView.setBackground(LayouUtil.getDrawable("constellation_background"));
        contentView.setOrientation(LinearLayout.VERTICAL);
        contentView.setGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams contentViewLayoutParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) LayouUtil.getDimen("y384"));
        contentViewLayoutParam.topMargin = (int) LayouUtil.getDimen("y8");
        contentView.setLayoutParams(contentViewLayoutParam);
        ImageView ivName = new ImageView(GlobalContext.get());
        ivName.setImageDrawable(LayouUtil.getDrawable(ConstellationMatchingView.getConstellationPictureByName(constellationFortuneData.name)));
        LinearLayout.LayoutParams ivNameLayoutParam = new LinearLayout.LayoutParams((int) LayouUtil.getDimen("m80"), (int) LayouUtil.getDimen("m80"));
        ivNameLayoutParam.bottomMargin = (int) LayouUtil.getDimen("y8");
        ivNameLayoutParam.topMargin = (int) LayouUtil.getDimen("y21");
        ivName.setLayoutParams(ivNameLayoutParam);
        contentView.addView(ivName);

        TextView tvName = new TextView(GlobalContext.get());
        LinearLayout.LayoutParams tvNameLayoutParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tvNameLayoutParam.bottomMargin = (int) LayouUtil.getDimen("y8");
        tvName.setLayoutParams(tvNameLayoutParam);
        tvName.setGravity(Gravity.CENTER);
        tvName.setTextSize(LayouUtil.getDimen("m23"));
        tvName.setTextColor(Color.WHITE);
        tvName.setText(constellationFortuneData.name + constellationFortuneData.fortuneType);
        contentView.addView(tvName);


        LinearLayout scoreLayout = new LinearLayout(GlobalContext.get());
        int width = (int) (LayouUtil.getDimen("m32") * 5 + LayouUtil.getDimen("x24") * 4);
        LinearLayout.LayoutParams scoreLayoutParam = new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.WRAP_CONTENT);
        scoreLayoutParam.bottomMargin = (int) LayouUtil.getDimen("y24");
        scoreLayout.setLayoutParams(scoreLayoutParam);
        for (int i = 0; i < constellationFortuneData.level; i++) {
            ImageView imageView = new ImageView(GlobalContext.get());
            imageView.setImageDrawable(LayouUtil.getDrawable("star_enable"));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams((int) LayouUtil.getDimen("m32"), (int) LayouUtil.getDimen("m32")));
            layoutParams.rightMargin = (int) LayouUtil.getDimen("x24");
            imageView.setLayoutParams(layoutParams);
            scoreLayout.addView(imageView);
        }
        for (int i = 0; i < 5 - constellationFortuneData.level; i++) {
            ImageView imageView = new ImageView(GlobalContext.get());
            imageView.setImageDrawable(LayouUtil.getDrawable("star_disable"));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams((int) LayouUtil.getDimen("m32"), (int) LayouUtil.getDimen("m32")));
            layoutParams.rightMargin = (int) LayouUtil.getDimen("x24");
            imageView.setLayoutParams(layoutParams);
            scoreLayout.addView(imageView);
        }
        contentView.addView(scoreLayout);

        final AutoSplitTextView tvDesc = new AutoSplitTextView(GlobalContext.get());
        tvDesc.setLayoutParams(new LinearLayout.LayoutParams(mTvDescWidth, mTvDescHeight));
        tvDesc.setTextSize(LayouUtil.getDimen("m19"));
        tvDesc.setTextColor(Color.WHITE);
        tvDesc.setEllipsize(TextUtils.TruncateAt.END);
        tvDesc.setText(constellationFortuneData.desc);
        tvDesc.setMaxLines(mTvDescHeight / tvDesc.getLineHeight());
        contentView.addView(tvDesc);

        root.addView(titleView);
        root.addView(contentView);
        return root;
    }

    private View createViewHalf(ConstellationFortuneData constellationFortuneData) {
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
        tvTitle.setText("星座运势");
        tvTitle.setTextSize(LayouUtil.getDimen("m23"));
        tvTitle.setTextColor(Color.parseColor("#FF89898A"));
        tvTitle.setGravity(Gravity.CENTER);
        titleView.addView(starImageView);
        titleView.addView(tvTitle);


        RelativeLayout contentView = new RelativeLayout(GlobalContext.get());
        contentView.setBackground(LayouUtil.getDrawable("constellation_background"));
//        contentView.setOrientation(LinearLayout.HORIZONTAL);
        contentView.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams contentViewLayoutParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        contentViewLayoutParam.topMargin = (int) LayouUtil.getDimen("y8");
        contentView.setLayoutParams(contentViewLayoutParam);

        LinearLayout leftContentView = new LinearLayout(GlobalContext.get());
        leftContentView.setId(ViewUtils.generateViewId());
        leftContentView.setOrientation(LinearLayout.VERTICAL);
        leftContentView.setGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams leftContentViewLayoutParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        leftContentView.setLayoutParams(leftContentViewLayoutParam);

        ImageView ivName = new ImageView(GlobalContext.get());
        ivName.setImageDrawable(LayouUtil.getDrawable(ConstellationMatchingView.getConstellationPictureByName(constellationFortuneData.name)));
        LinearLayout.LayoutParams ivNameLayoutParam = new LinearLayout.LayoutParams((int) LayouUtil.getDimen("m80"), (int) LayouUtil.getDimen("m80"));
        ivNameLayoutParam.bottomMargin = (int) LayouUtil.getDimen("y8");
        ivName.setLayoutParams(ivNameLayoutParam);
        leftContentView.addView(ivName);

        TextView tvName = new TextView(GlobalContext.get());
        LinearLayout.LayoutParams tvNameLayoutParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tvNameLayoutParam.bottomMargin = (int) LayouUtil.getDimen("y8");
        tvName.setLayoutParams(tvNameLayoutParam);
        tvName.setGravity(Gravity.CENTER);
        tvName.setTextSize(LayouUtil.getDimen("m22"));
        tvName.setTextColor(Color.WHITE);
        tvName.setText(constellationFortuneData.name + constellationFortuneData.fortuneType);
        leftContentView.addView(tvName);


        LinearLayout scoreLayout = new LinearLayout(GlobalContext.get());
        int width = (int) (LayouUtil.getDimen("m32") * 5 + LayouUtil.getDimen("x16") * 4);
        LinearLayout.LayoutParams scoreLayoutParam = new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.WRAP_CONTENT);
        scoreLayout.setLayoutParams(scoreLayoutParam);
        for (int i = 0; i < constellationFortuneData.level; i++) {
            ImageView imageView = new ImageView(GlobalContext.get());
            imageView.setImageDrawable(LayouUtil.getDrawable("star_enable"));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams((int) LayouUtil.getDimen("m32"), (int) LayouUtil.getDimen("m32")));
            layoutParams.rightMargin = (int) LayouUtil.getDimen("x16");
            imageView.setLayoutParams(layoutParams);
            scoreLayout.addView(imageView);
        }
        for (int i = 0; i < 5 - constellationFortuneData.level; i++) {
            ImageView imageView = new ImageView(GlobalContext.get());
            imageView.setImageDrawable(LayouUtil.getDrawable("star_disable"));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams((int) LayouUtil.getDimen("m32"), (int) LayouUtil.getDimen("m32")));
            if (i != 5 - constellationFortuneData.level - 1) {
                layoutParams.rightMargin = (int) LayouUtil.getDimen("x16");
            }
            imageView.setLayoutParams(layoutParams);
            scoreLayout.addView(imageView);
        }
        leftContentView.addView(scoreLayout);

        AutoSplitTextView tvDesc = new AutoSplitTextView(GlobalContext.get());
        RelativeLayout.LayoutParams tvDescLayoutParams = new RelativeLayout.LayoutParams(mTvDescWidth, mTvDescHeight);
        tvDescLayoutParams.leftMargin = mTvDescLeftMargin;
        tvDescLayoutParams.addRule(RelativeLayout.RIGHT_OF, leftContentView.getId());
        tvDesc.setLayoutParams(tvDescLayoutParams);

        tvDesc.setTextSize(LayouUtil.getDimen("m19"));
        tvDesc.setTextColor(Color.WHITE);
        tvDesc.setEllipsize(TextUtils.TruncateAt.END);
        tvDesc.setText(constellationFortuneData.desc);
        tvDesc.setMaxLines(mTvDescHeight / tvDesc.getLineHeight());
        contentView.addView(leftContentView);
        contentView.addView(tvDesc);
        root.addView(titleView);
        root.addView(contentView);
        return root;
    }

    private View createViewNone(ConstellationFortuneData constellationFortuneData) {
        LinearLayout root = new LinearLayout(GlobalContext.get());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        root.setBackground(LayouUtil.getDrawable("constellation_background"));

        LinearLayout titleView = new LinearLayout(GlobalContext.get());
        titleView.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams titleViewLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        titleView.setLayoutParams(titleViewLayoutParams);
        ImageView starImageView = new ImageView(GlobalContext.get());
        starImageView.setImageDrawable(LayouUtil.getDrawable("icon_star"));
        starImageView.setLayoutParams(new ViewGroup.LayoutParams((int) LayouUtil.getDimen("m40"), (int) LayouUtil.getDimen("m40")));
        TextView tvTitle = new TextView(GlobalContext.get());
        tvTitle.setText("星座运势");
        tvTitle.setTextSize(LayouUtil.getDimen("m20"));
        tvTitle.setTextColor(Color.parseColor("#FF89898A"));
        tvTitle.setGravity(Gravity.CENTER);
        titleView.addView(starImageView);
        titleView.addView(tvTitle);


        LinearLayout contentView = new LinearLayout(GlobalContext.get());

        contentView.setOrientation(LinearLayout.VERTICAL);
        contentView.setGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams contentViewLayoutParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        contentViewLayoutParam.topMargin = (int) LayouUtil.getDimen("y8");
        contentView.setLayoutParams(contentViewLayoutParam);

        LinearLayout topLayout = new LinearLayout(GlobalContext.get());
        topLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams topLayoutParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        topLayout.setLayoutParams(topLayoutParam);

        ImageView ivName = new ImageView(GlobalContext.get());
        ivName.setImageDrawable(LayouUtil.getDrawable(ConstellationMatchingView.getConstellationPictureByName(constellationFortuneData.name)));
        LinearLayout.LayoutParams ivNameLayoutParam = new LinearLayout.LayoutParams((int) LayouUtil.getDimen("m64"), (int) LayouUtil.getDimen("m64"));
        ivName.setLayoutParams(ivNameLayoutParam);
        topLayout.addView(ivName);

        LinearLayout topRightLayout = new LinearLayout(GlobalContext.get());
        topRightLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams topRightLayoutParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        topRightLayoutParam.leftMargin = (int) LayouUtil.getDimen("x16");
        topRightLayout.setLayoutParams(topRightLayoutParam);

        TextView tvName = new TextView(GlobalContext.get());
        LinearLayout.LayoutParams tvNameLayoutParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tvName.setLayoutParams(tvNameLayoutParam);
        tvName.setGravity(Gravity.CENTER);
        tvName.setTextSize(LayouUtil.getDimen("m18"));
        tvName.setTextColor(Color.WHITE);
        tvName.setText(constellationFortuneData.name + constellationFortuneData.fortuneType);
        topRightLayout.addView(tvName);


        LinearLayout scoreLayout = new LinearLayout(GlobalContext.get());
        LinearLayout.LayoutParams scoreLayoutParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        scoreLayout.setLayoutParams(scoreLayoutParam);
        for (int i = 0; i < constellationFortuneData.level; i++) {
            ImageView imageView = new ImageView(GlobalContext.get());
            imageView.setImageDrawable(LayouUtil.getDrawable("star_enable"));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams((int) LayouUtil.getDimen("m24"), (int) LayouUtil.getDimen("m24")));
            layoutParams.rightMargin = (int) LayouUtil.getDimen("x8");
            imageView.setLayoutParams(layoutParams);
            scoreLayout.addView(imageView);
        }
        for (int i = 0; i < 5 - constellationFortuneData.level; i++) {
            ImageView imageView = new ImageView(GlobalContext.get());
            imageView.setImageDrawable(LayouUtil.getDrawable("star_disable"));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams((int) LayouUtil.getDimen("m24"), (int) LayouUtil.getDimen("m24")));
            layoutParams.rightMargin = (int) LayouUtil.getDimen("x8");
            imageView.setLayoutParams(layoutParams);
            scoreLayout.addView(imageView);
        }
        topRightLayout.addView(scoreLayout);

        topLayout.addView(topRightLayout);

        AutoSplitTextView tvDesc = new AutoSplitTextView(GlobalContext.get());
        LinearLayout.LayoutParams tvDescLayoutParam = new LinearLayout.LayoutParams(mTvDescWidth, mTvDescHeight);
        tvDescLayoutParam.topMargin = (int) LayouUtil.getDimen("y16");
        tvDesc.setLayoutParams(tvDescLayoutParam);
        tvDesc.setTextSize(LayouUtil.getDimen("m18"));
        tvDesc.setTextColor(Color.WHITE);
        tvDesc.setText(constellationFortuneData.desc);
        contentView.addView(topLayout);
        contentView.addView(tvDesc);

        tvDesc.setEllipsize(TextUtils.TruncateAt.END);
        tvDesc.setMaxLines(mTvDescHeight / tvDesc.getLineHeight());

        root.addView(titleView);
        root.addView(contentView);
        return root;
    }

    @Override
    public void init() {
        super.init();

    }

    //切换模式修改布局参数
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

    private int unit;

    private void initHalf() {
        if (WinLayout.isVertScreen) {
            unit = (int) LayouUtil.getDimen("vertical_unit");
            mTvDescWidth = (int)(45.3 * unit);
            mTvDescHeight = (int) (14.1 * unit);
            mTvDescLeftMargin = (int) (3.3 * unit);
        } else {
            unit = (int) LayouUtil.getDimen("unit");
            mTvDescWidth = (int)(54.4 * unit);
            mTvDescHeight = 17 * unit;
            mTvDescLeftMargin = (int) (4 * unit);
        }
    }

    private void initFull() {
        if (WinLayout.isVertScreen) {
            unit = (int) LayouUtil.getDimen("vertical_unit");
            mTvDescWidth = (int)(50 * unit);
            mTvDescHeight = (int) (14.1 * unit);

        } else {
            unit = (int) LayouUtil.getDimen("unit");
            mTvDescWidth = (int)(60 * unit);
            mTvDescHeight = 17 * unit;
        }
        
    }

}
