package com.txznet.comm.ui.viewfactory.view.defaults;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.view.AutoSplitTextView;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.ConstellationFortuneData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IConstellationFortuneView;
import com.txznet.comm.util.TextViewUtil;

public class ConstellationFortuneView extends IConstellationFortuneView {

    private static ConstellationFortuneView sInstance = new ConstellationFortuneView();

    private ConstellationFortuneView() {
    }

    public static ConstellationFortuneView getInstance() {
        return sInstance;
    }

    @Override
    public ViewAdapter getView(ViewData data) {
        ConstellationFortuneData constellationFortuneData = (ConstellationFortuneData) data;
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
        TextViewUtil.setTextSize(tvName, LayouUtil.getDimen("m22"));
        tvName.setTextColor(Color.WHITE);
        tvName.setText(constellationFortuneData.name + constellationFortuneData.fortuneType);
        contentView.addView(tvName);


        LinearLayout scoreLayout = new LinearLayout(GlobalContext.get());
        int width  = (int) (LayouUtil.getDimen("m32") * 5 + LayouUtil.getDimen("x24") * 4);
        LinearLayout.LayoutParams scoreLayoutParam = new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.WRAP_CONTENT);
        scoreLayoutParam.bottomMargin = (int) LayouUtil.getDimen("y24");
        scoreLayout.setLayoutParams(scoreLayoutParam);
        for (int i = 0; i < constellationFortuneData.level; i++) {
            ImageView imageView = new ImageView(GlobalContext.get());
            imageView.setImageDrawable(LayouUtil.getDrawable("star_enable"));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams((int)LayouUtil.getDimen("m32"), (int)LayouUtil.getDimen("m32")));
            layoutParams.rightMargin = (int) LayouUtil.getDimen("x24");
            imageView.setLayoutParams(layoutParams);
            scoreLayout.addView(imageView);
        }
        for (int i = 0; i < 5 - constellationFortuneData.level; i++) {
            ImageView imageView = new ImageView(GlobalContext.get());
            imageView.setImageDrawable(LayouUtil.getDrawable("star_disable"));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams((int)LayouUtil.getDimen("m32"), (int)LayouUtil.getDimen("m32")));
            layoutParams.rightMargin = (int) LayouUtil.getDimen("x24");
            imageView.setLayoutParams(layoutParams);
            scoreLayout.addView(imageView);
        }
        contentView.addView(scoreLayout);

        AutoSplitTextView tvDesc = new AutoSplitTextView(GlobalContext.get());
        int tvDescHeight = (int)LayouUtil.getDimen("y136");
        tvDesc.setLayoutParams(new LinearLayout.LayoutParams((int)LayouUtil.getDimen("x469"), tvDescHeight));
        TextViewUtil.setTextSize(tvDesc, LayouUtil.getDimen("m19"));
        tvDesc.setTextColor(Color.WHITE);
        tvDesc.setEllipsize(TextUtils.TruncateAt.END);
        tvDesc.setText(constellationFortuneData.desc);
        tvDesc.setMaxLines(tvDescHeight/tvDesc.getLineHeight());
        contentView.addView(tvDesc);

        root.addView(titleView);
        root.addView(contentView);
        ViewAdapter adapter = new ViewAdapter();
        adapter.type = data.getType();
        adapter.view = root;
        adapter.object = ConstellationFortuneView.getInstance();
        return adapter;
    }

    @Override
    public void init() {
        super.init();
    }

}
