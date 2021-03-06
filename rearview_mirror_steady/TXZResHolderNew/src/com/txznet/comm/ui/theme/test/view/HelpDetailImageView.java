package com.txznet.comm.ui.theme.test.view;

import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.zxing.WriterException;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.theme.ThemeConfigManager;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.config.StyleConfig;
import com.txznet.comm.ui.theme.test.config.VersionManager;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ScreenUtil;
import com.txznet.comm.ui.util.ViewUtils;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.HelpDetailImageViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IHelpDetailImageView;
import com.txznet.comm.ui.viewfactory.view.defaults.ListTitleView;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.comm.util.StringUtils;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.sdk.TXZRecordWinManager;
import com.txznet.txz.util.LanguageConvertor;
import com.txznet.txz.util.QRUtil;


public class HelpDetailImageView extends IHelpDetailImageView {
	private static HelpDetailImageView instance = new HelpDetailImageView();

	public static HelpDetailImageView getInstance() {
		return instance;
	}

	private int rlTopHeight;    //顶部工具栏高度
	private int rlTopLeftMargin;    //顶部工具栏左边距
	private int rlTopRightMargin;    //顶部工具栏右边距
	private int ivBackSize;    //返回键大小
	private int tvBackSize;    //“返回”字体大小
	private int tvBackColor;    //“返回”字体颜色
	private int tvHelpSize;    //“帮助”字体大小
	private int tvHelpColor;    //“帮助”字体颜色
	private int ivSettingSize;    //设置图标大小

	private int contentHorMargin;    //内容左右边距
	private int contentHeight;    //内容高度

	private int imageSize;    //二维码大小
	private int imageInterval;    //二维码与文字介绍间隔
	private int textWidth;    //文字介绍宽度

    private float tvContentSize;    //标题字体大小
    private int tvContentColor;    //标题字体颜色
    private int dividerrHeight;    //分隔线高度

	@Override
	public void init() {
		super.init();
        tvBackColor = Color.WHITE;
        tvHelpColor = Color.WHITE;

        //tvContentSize = (Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_HELP_LABEL_ITEM_SIZE1);
        //tvContentColor = (int) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_HELP_ITEM_COLOR1);
        //tvContentColor = Color.parseColor(LayouUtil.getString("color_main_title"));
		dividerrHeight = (int) LayouUtil.getDimen("y1");
		if (dividerrHeight ==0){
			dividerrHeight = 1;
		}
	}

	//切换模式修改布局参数
	public void onUpdateParams(int styleIndex){
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

	//全屏布局参数
	private void initFull(){
		// 初始化配置，例如字体颜色等
		if (WinLayout.isVertScreen){
			int unit = (int) LayouUtil.getDimen("vertical_unit");
            rlTopHeight = 10 * unit;
            rlTopLeftMargin = 3 * unit;
            rlTopRightMargin = 3 * unit;
            ivBackSize = 3 * unit;
            tvBackSize = (int) LayouUtil.getDimen("vertical_h3");
            tvHelpSize = (int) LayouUtil.getDimen("vertical_h1");
            ivSettingSize = 5 * unit;
            contentHorMargin = 2 * unit;
            contentHeight = SizeConfig.itemHelpHeight * SizeConfig.pageCount + SizeConfig.titleHeight;
            imageSize = 20 * unit;
            imageInterval = 3 * unit;
            textWidth = 36 * unit;
		}else {
			int unit = (int) LayouUtil.getDimen("unit");
            rlTopHeight = 10 * unit;
            rlTopLeftMargin = 3 * unit;
            rlTopRightMargin = 3 * unit;
            ivBackSize = 3 * unit;
            tvBackSize = (int) LayouUtil.getDimen("h3");
            tvHelpSize = (int) LayouUtil.getDimen("h1");
            ivSettingSize = 5 * unit;
            contentHorMargin = 5 * unit;
            contentHeight = SizeConfig.itemHelpHeight * SizeConfig.pageCount + SizeConfig.titleHeight;
            imageSize = 20 * unit;
            imageInterval = 3 * unit;
            textWidth = 36 * unit;
		}
	}

	//半屏布局参数
	private void initHalf(){
		// 初始化配置，例如字体颜色等
        if (WinLayout.isVertScreen){
            int unit = (int) LayouUtil.getDimen("vertical_unit");
            rlTopHeight = 10 * unit;
            rlTopLeftMargin = 3 * unit;
            rlTopRightMargin = 3 * unit;
            ivBackSize = 3 * unit;
            tvBackSize = (int) LayouUtil.getDimen("vertical_h3");
            tvHelpSize = (int) LayouUtil.getDimen("vertical_h1");
            ivSettingSize = 5 * unit;
            contentHorMargin = 2 * unit;
            contentHeight = SizeConfig.itemHelpHeight * SizeConfig.pageCount + SizeConfig.titleHeight;
            imageSize = 20 * unit;
            imageInterval = 3 * unit;
            textWidth = 36 * unit;
        }else {
            int unit = (int) LayouUtil.getDimen("unit");
            rlTopHeight = 10 * unit;
            rlTopLeftMargin = 3 * unit;
            rlTopRightMargin = 3 * unit;
            ivBackSize = 3 * unit;
            tvBackSize = (int) LayouUtil.getDimen("h3");
            tvHelpSize = (int) LayouUtil.getDimen("h1");
            ivSettingSize = 5 * unit;
            contentHorMargin = 5 * unit;
            contentHeight = SizeConfig.itemHelpHeight * SizeConfig.pageCount + SizeConfig.titleHeight;
            imageSize = 20 * unit;
            imageInterval = 3 * unit;
            textWidth = 36 * unit;
        }

	}

	//无屏布局参数
	private void initNone(){
        if (WinLayout.isVertScreen){
            rlTopHeight = (int) LayouUtil.getDimen("x61");
            rlTopLeftMargin = (int) LayouUtil.getDimen("x31");
            rlTopRightMargin = (int) LayouUtil.getDimen("x31");
            ivBackSize = (int) LayouUtil.getDimen("x31");
            tvBackSize = (int) LayouUtil.getDimen("x26");
            tvHelpSize = (int) LayouUtil.getDimen("x51");
            ivSettingSize = (int) LayouUtil.getDimen("x51");
            contentHorMargin = (int) LayouUtil.getDimen("x20");
            contentHeight = SizeConfig.itemHelpHeight * SizeConfig.pageCount + SizeConfig.titleHeight;
            imageSize = (int) LayouUtil.getDimen("x208");
            imageInterval = (int) LayouUtil.getDimen("x26");
            textWidth = (int) LayouUtil.getDimen("x375");
        }else {
            int unit = (int) LayouUtil.getDimen("unit");
            rlTopHeight = 10 * unit;
            rlTopLeftMargin = 3 * unit;
            rlTopRightMargin = 3 * unit;
            ivBackSize = 3 * unit;
            tvBackSize = (int) LayouUtil.getDimen("h3_none");
            tvHelpSize = (int) LayouUtil.getDimen("h1_none");
            ivSettingSize = 5 * unit;
            contentHorMargin = 5 * unit;
            contentHeight = SizeConfig.itemHelpHeight * SizeConfig.pageCount + SizeConfig.titleHeight;
            imageSize = 20 * unit;
            imageInterval = 3 * unit;
            textWidth = 36 * unit;
        }
	}

	@Override
	public ViewAdapter getView(ViewData data) {
		HelpDetailImageViewData helpDetailImageViewData = (HelpDetailImageViewData) data;
		WinLayout.getInstance().vTips = helpDetailImageViewData.vTips;
		LogUtil.logd(WinLayout.logTag+ "helpDetailImageViewData.vTips:" + helpDetailImageViewData.vTips + StyleConfig.getInstance().getSelectStyleIndex());

		View view = null;

		switch (StyleConfig.getInstance().getSelectStyleIndex()) {
			case StyleConfig.STYLE_ROBOT_FULL_SCREES:
			case StyleConfig.STYLE_ROBOT_HALF_SCREES:
			    if (WinLayout.isVertScreen){
                    view = createVerticalViewFull(helpDetailImageViewData);
                }else {
                    view = createViewFull(helpDetailImageViewData);
                }
				break;
			case StyleConfig.STYLE_ROBOT_NONE_SCREES:
			default:
				view = createViewNone(helpDetailImageViewData);
				break;
		}

		ViewAdapter adapter = new ViewAdapter();
		adapter.type = data.getType();
		adapter.view = view;
		adapter.view.setTag(data.getType());
		adapter.object = HelpDetailImageView.getInstance();
		return adapter;
	}

	private View createViewFull(final HelpDetailImageViewData helpDetailImageViewData){
		LinearLayout llLayout = new LinearLayout(GlobalContext.get());
		llLayout.setOrientation(LinearLayout.VERTICAL);
		//llLayout.setGravity(Gravity.CENTER_VERTICAL);

		llLayout.setOrientation(LinearLayout.VERTICAL);RelativeLayout rTop = new RelativeLayout(GlobalContext.get());
		rTop.setGravity(Gravity.CENTER_VERTICAL);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,rlTopHeight);
		layoutParams.setMargins(rlTopLeftMargin,0,rlTopRightMargin,0);
		llLayout.addView(rTop,layoutParams);

		/*ImageView ivSetting = new ImageView(GlobalContext.get());
		ivSetting.setImageDrawable(LayouUtil.getDrawable("setting_title"));
		RelativeLayout.LayoutParams rLayoutParams = new RelativeLayout.LayoutParams(ivSettingSize,ivSettingSize);
		rLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
		rLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		rTop.addView(ivSetting,rLayoutParams);
		ivSetting.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				RecordWin2Manager.getInstance().operateView(
						TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK,
						TXZRecordWinManager.RecordWin2.RecordWinController.VIEW_SETTING,0,0);
			}
		});*/

		TextView tvTittle = new TextView(GlobalContext.get());
		tvTittle.setGravity(Gravity.CENTER_VERTICAL);
		//tvTittle.setText("帮助");
		tvTittle.setText(LanguageConvertor.toLocale(helpDetailImageViewData.getHelpTitle()));
		RelativeLayout.LayoutParams rLayoutParams1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.MATCH_PARENT);
		rLayoutParams1.addRule(RelativeLayout.CENTER_IN_PARENT);
		rTop.addView(tvTittle,rLayoutParams1);

		LinearLayout lBackRoot = new LinearLayout(GlobalContext.get());
		lBackRoot.setOrientation(LinearLayout.HORIZONTAL);
		lBackRoot.setGravity(Gravity.CENTER_VERTICAL);
		RelativeLayout.LayoutParams rLayoutParams2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.MATCH_PARENT);
		rLayoutParams2.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		rTop.addView(lBackRoot,rLayoutParams2);
		lBackRoot.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				LogUtil.logd(WinLayout.logTag+ "onClick: back");
				if (VersionManager.getInstance().isUseHelpNewTag()) {
					com.txznet.comm.remote.util.ConfigUtil.setShowHelpNewTag(false);
				}
				RecordWin2Manager.getInstance().operateView(TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK,
						TXZRecordWinManager.RecordWin2.RecordWinController.VIEW_HELP_BACK, 0, 0);
			}
		});

		ImageView ivBack = new ImageView(GlobalContext.get());
		ivBack.setImageDrawable(LayouUtil.getDrawable("back"));
		layoutParams = new LinearLayout.LayoutParams(ivBackSize,ivBackSize);
		lBackRoot.addView(ivBack,layoutParams);

		TextView tvBack = new TextView(GlobalContext.get());
		tvBack.setGravity(Gravity.CENTER_VERTICAL);
		tvBack.setText(LanguageConvertor.toLocale("返回"));
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		lBackRoot.addView(tvBack,layoutParams);

		LinearLayout llContent = new LinearLayout(GlobalContext.get());
		llContent.setOrientation(LinearLayout.VERTICAL);
		llContent.setBackground(LayouUtil.getDrawable("white_range_layout"));
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,contentHeight);
		layoutParams.setMargins(contentHorMargin,0,contentHorMargin,0);
		llLayout.addView(llContent,layoutParams);

		/*int itemHeight = (int)(contentHeight/(ScreenUtil.getVisbileCount()*1.25f));
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,itemHeight);*/

		/*View itemView = createItemView(0, helpDetailImageViewData.getHelpTitle(), true);
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.itemHelpHeight * SizeConfig.pageCount);
		llContent.addView(itemView,layoutParams);*/

		FrameLayout flDetail = new FrameLayout(GlobalContext.get());
		//layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1);
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
		llContent.addView(flDetail,layoutParams);

		LinearLayout llDetail = new LinearLayout(GlobalContext.get());
		llDetail.setOrientation(WinLayout.isVertScreen?LinearLayout.VERTICAL:LinearLayout.HORIZONTAL);
		llDetail.setGravity(Gravity.CENTER_VERTICAL);
		//layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
		FrameLayout.LayoutParams flLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.WRAP_CONTENT);
		flLayoutParams.gravity = Gravity.CENTER;
		flDetail.addView(llDetail,flLayoutParams);

		/*View view = new View(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,2);
		llDetail.addView(view,layoutParams);*/

		final HelpDetailImageViewData.HelpDetailBean helpDetailImg = helpDetailImageViewData.getData().get(0);

		if (!TextUtils.isEmpty(helpDetailImg.img)) {
			ImageView iv = new ImageView(GlobalContext.get());
			iv.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					JSONBuilder data = new JSONBuilder();
					data.put("title", helpDetailImageViewData.getHelpTitle());
					data.put("url", helpDetailImg.img);
					data.put("desc", helpDetailImg.text);
					data.put("isFromFile", helpDetailImageViewData.isFromFile);
					data.put("from", "detail");
					ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.help.ui.detail.qrcode", data.toBytes(), null);
				}
			});
			iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			if (helpDetailImg.img.startsWith("qrcode:")){
				try {
					iv.setImageBitmap(QRUtil.createQRCodeBitmap(helpDetailImg.img.replace("qrcode:",""), (int) LayouUtil.getDimen("y200")));
				} catch (WriterException e) {
					e.printStackTrace();
				}
			}else if (helpDetailImageViewData.isFromFile) {
				ImageLoader.getInstance().displayImage("file://" + helpDetailImg.img, new ImageViewAware(iv));
			} else {
				iv.setImageDrawable(LayouUtil.getDrawable(helpDetailImg.img));
			}
			//layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 5);
			layoutParams = new LinearLayout.LayoutParams(imageSize, imageSize);
			if (WinLayout.isVertScreen){
				layoutParams.bottomMargin = imageInterval;
			}else {
				layoutParams.rightMargin = imageInterval;
			}
			layoutParams.gravity = Gravity.CENTER;
			llDetail.addView(iv, layoutParams);
		}

		/*if (!TextUtils.isEmpty(helpDetailImg.text) && !TextUtils.isEmpty(helpDetailImg.img)) {
			view = new View(GlobalContext.get());
			layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
			llDetail.addView(view, layoutParams);
		}*/

		if (!TextUtils.isEmpty(helpDetailImg.text)) {
			helpDetailImg.text = helpDetailImg.text.replace("\n\n","\n");
			TextView tv = new TextView(GlobalContext.get());
			tv.setGravity(Gravity.CENTER_VERTICAL);
			TextViewUtil.setTextSize(tv, (Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_HELP_ITEM_SIZE1));
			TextViewUtil.setTextColor(tv, (Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_HELP_ITEM_COLOR1));
			tv.setText(LanguageConvertor.toLocale(helpDetailImg.text));
			layoutParams = new LinearLayout.LayoutParams(textWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
			llDetail.addView(tv, layoutParams);
		}

		/*view = new View(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,2);
		llDetail.addView(view,layoutParams);*/


		TextViewUtil.setTextSize(tvTittle,tvHelpSize);
		TextViewUtil.setTextColor(tvTittle,tvHelpColor);
		TextViewUtil.setTextSize(tvBack,tvBackSize);
		TextViewUtil.setTextColor(tvBack,tvBackColor);
		return llLayout;
	}

    private View createVerticalViewFull(final HelpDetailImageViewData helpDetailImageViewData){
        LinearLayout llLayout = new LinearLayout(GlobalContext.get());
        llLayout.setOrientation(LinearLayout.VERTICAL);
        //llLayout.setGravity(Gravity.CENTER_VERTICAL);

        llLayout.setOrientation(LinearLayout.VERTICAL);RelativeLayout rTop = new RelativeLayout(GlobalContext.get());
        rTop.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,rlTopHeight);
        layoutParams.setMargins(rlTopLeftMargin,0,rlTopRightMargin,0);
        llLayout.addView(rTop,layoutParams);

        TextView tvTittle = new TextView(GlobalContext.get());
        tvTittle.setGravity(Gravity.CENTER_VERTICAL);
        //tvTittle.setText("帮助");
        tvTittle.setText(LanguageConvertor.toLocale(helpDetailImageViewData.getHelpTitle()));
        RelativeLayout.LayoutParams rLayoutParams1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.MATCH_PARENT);
        rLayoutParams1.addRule(RelativeLayout.CENTER_IN_PARENT);
        rTop.addView(tvTittle,rLayoutParams1);

        LinearLayout lBackRoot = new LinearLayout(GlobalContext.get());
        lBackRoot.setOrientation(LinearLayout.HORIZONTAL);
        lBackRoot.setGravity(Gravity.CENTER_VERTICAL);
        RelativeLayout.LayoutParams rLayoutParams2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.MATCH_PARENT);
        rLayoutParams2.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        rTop.addView(lBackRoot,rLayoutParams2);
        lBackRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogUtil.logd(WinLayout.logTag+ "onClick: back");
                if (VersionManager.getInstance().isUseHelpNewTag()) {
                    com.txznet.comm.remote.util.ConfigUtil.setShowHelpNewTag(false);
                }
                RecordWin2Manager.getInstance().operateView(TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK,
                        TXZRecordWinManager.RecordWin2.RecordWinController.VIEW_HELP_BACK, 0, 0);
            }
        });

        ImageView ivBack = new ImageView(GlobalContext.get());
        ivBack.setImageDrawable(LayouUtil.getDrawable("back"));
        layoutParams = new LinearLayout.LayoutParams(ivBackSize,ivBackSize);
        lBackRoot.addView(ivBack,layoutParams);

        TextView tvBack = new TextView(GlobalContext.get());
        tvBack.setGravity(Gravity.CENTER_VERTICAL);
        tvBack.setText(LanguageConvertor.toLocale("返回"));
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        lBackRoot.addView(tvBack,layoutParams);

        FrameLayout frameLayout = new FrameLayout(GlobalContext.get());
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(contentHorMargin,0,contentHorMargin,0);
        llLayout.addView(frameLayout,layoutParams);

        LinearLayout llContent = new LinearLayout(GlobalContext.get());
        llContent.setOrientation(LinearLayout.VERTICAL);
        llContent.setBackground(LayouUtil.getDrawable("white_range_layout"));
        /*layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,contentHeight);
        layoutParams.setMargins(contentHorMargin,0,contentHorMargin,0);
        llLayout.addView(llContent,layoutParams);*/
        FrameLayout.LayoutParams flLayoutParams = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,contentHeight);
        flLayoutParams.gravity = Gravity.CENTER;
        frameLayout.addView(llContent,flLayoutParams);

		/*int itemHeight = (int)(contentHeight/(ScreenUtil.getVisbileCount()*1.25f));
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,itemHeight);*/

		/*View itemView = createItemView(0, helpDetailImageViewData.getHelpTitle(), true);
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.itemHelpHeight * SizeConfig.pageCount);
		llContent.addView(itemView,layoutParams);*/

        FrameLayout flDetail = new FrameLayout(GlobalContext.get());
        //layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1);
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
        llContent.addView(flDetail,layoutParams);

        LinearLayout llDetail = new LinearLayout(GlobalContext.get());
        llDetail.setOrientation(WinLayout.isVertScreen?LinearLayout.VERTICAL:LinearLayout.HORIZONTAL);
        llDetail.setGravity(Gravity.CENTER_VERTICAL);
        //layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
        flLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.WRAP_CONTENT);
        flLayoutParams.gravity = Gravity.CENTER;
        flDetail.addView(llDetail,flLayoutParams);

		/*View view = new View(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,2);
		llDetail.addView(view,layoutParams);*/

        final HelpDetailImageViewData.HelpDetailBean helpDetailImg = helpDetailImageViewData.getData().get(0);

        if (!TextUtils.isEmpty(helpDetailImg.img)) {
            ImageView iv = new ImageView(GlobalContext.get());
			iv.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					JSONBuilder data = new JSONBuilder();
					data.put("title", helpDetailImageViewData.getHelpTitle());
					data.put("url", helpDetailImg.img);
					data.put("desc", helpDetailImg.text);
					data.put("isFromFile", helpDetailImageViewData.isFromFile);
					data.put("from", "detail");
					ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.help.ui.detail.qrcode", data.toBytes(), null);
				}
			});
            iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            if (helpDetailImg.img.startsWith("qrcode:")){
                try {
                    iv.setImageBitmap(QRUtil.createQRCodeBitmap(helpDetailImg.img.replace("qrcode:",""), (int) LayouUtil.getDimen("y200")));
                } catch (WriterException e) {
                    e.printStackTrace();
                }
            }else if (helpDetailImageViewData.isFromFile) {
                ImageLoader.getInstance().displayImage("file://" + helpDetailImg.img, new ImageViewAware(iv));
            } else {
                iv.setImageDrawable(LayouUtil.getDrawable(helpDetailImg.img));
            }
            //layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 5);
            layoutParams = new LinearLayout.LayoutParams(imageSize, imageSize);
            if (WinLayout.isVertScreen){
                layoutParams.bottomMargin = imageInterval;
            }else {
                layoutParams.rightMargin = imageInterval;
            }
            layoutParams.gravity = Gravity.CENTER;
            llDetail.addView(iv, layoutParams);
        }

		/*if (!TextUtils.isEmpty(helpDetailImg.text) && !TextUtils.isEmpty(helpDetailImg.img)) {
			view = new View(GlobalContext.get());
			layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
			llDetail.addView(view, layoutParams);
		}*/

        if (!TextUtils.isEmpty(helpDetailImg.text)) {
            TextView tv = new TextView(GlobalContext.get());
            tv.setGravity(Gravity.CENTER);
            TextViewUtil.setTextSize(tv, (Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_HELP_ITEM_SIZE1));
            TextViewUtil.setTextColor(tv, (Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_HELP_ITEM_COLOR1));
            tv.setText(LanguageConvertor.toLocale(helpDetailImg.text));
            layoutParams = new LinearLayout.LayoutParams(textWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
            llDetail.addView(tv, layoutParams);
        }

		/*view = new View(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,2);
		llDetail.addView(view,layoutParams);*/


        TextViewUtil.setTextSize(tvTittle,tvHelpSize);
        TextViewUtil.setTextColor(tvTittle,tvHelpColor);
        TextViewUtil.setTextSize(tvBack,tvBackSize);
        TextViewUtil.setTextColor(tvBack,tvBackColor);
        return llLayout;
    }

	private View createViewNone(final HelpDetailImageViewData helpDetailImageViewData){
	    tvContentSize = (float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_HELP_LABEL_ITEM_SIZE1);

		LinearLayout llLayout = new LinearLayout(GlobalContext.get());
		llLayout.setGravity(Gravity.CENTER_VERTICAL);
		llLayout.setOrientation(LinearLayout.VERTICAL);

		/*int contentHeight = ScreenUtil.getDisplayLvItemH(false) * ScreenUtil.getVisbileCount();*/
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		llLayout.setLayoutParams(layoutParams);
		/*llLayout.setGravity(Gravity.CENTER_VERTICAL);
		llLayout.setOrientation(LinearLayout.VERTICAL);*/

		LinearLayout llContent = new LinearLayout(GlobalContext.get());
		llContent.setOrientation(LinearLayout.VERTICAL);
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
		llLayout.addView(llContent,layoutParams);

		/*int itemHeight = (int)(contentHeight/(ScreenUtil.getVisbileCount()*1.8f));
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,itemHeight);*/

		/*View itemView = createItemViewNone(0, helpDetailImageViewData.getHelpTitle(), true);
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.titleHeight);
		llContent.addView(itemView,layoutParams);*/

        LinearLayout lTitle = new LinearLayout(GlobalContext.get());
        lTitle.setOrientation(LinearLayout.HORIZONTAL);
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.titleHeight);
        llContent.addView(lTitle,layoutParams);

        View divider = new View(GlobalContext.get());
        divider.setBackground(LayouUtil.getDrawable("line"));
        //layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,(int) LayouUtil.getDimen("y2"));
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,dividerrHeight);
        llContent.addView(divider, layoutParams);

		View.OnClickListener backListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (VersionManager.getInstance().isUseHelpNewTag()) {
					com.txznet.comm.remote.util.ConfigUtil.setShowHelpNewTag(false);
				}
				RecordWin2Manager.getInstance().operateView(TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK,
						TXZRecordWinManager.RecordWin2.RecordWinController.VIEW_HELP_BACK, 0, 0);
			}
		};

        ImageView ivBack = new ImageView(GlobalContext.get());
        ivBack.setImageDrawable(LayouUtil.getDrawable("back"));
        layoutParams = new LinearLayout.LayoutParams((int)(tvContentSize*1.2),(int)(tvContentSize*1.2));
        layoutParams.leftMargin = (int) LayouUtil.getDimen("x5");
        layoutParams.rightMargin = (int) LayouUtil.getDimen("x5");
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        lTitle.addView(ivBack,layoutParams);
        ivBack.setOnClickListener(backListener);

        TextView tvTitle = new TextView(GlobalContext.get());
        tvTitle.setGravity(Gravity.CENTER_VERTICAL);
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.MATCH_PARENT);
        lTitle.addView(tvTitle,layoutParams);
        tvTitle.setText(LanguageConvertor.toLocale(helpDetailImageViewData.getHelpTitle()));
        tvTitle.setOnClickListener(backListener);

        TextViewUtil.setTextSize(tvTitle, tvContentSize);
        TextViewUtil.setTextSize(tvTitle,tvContentSize);
        TextViewUtil.setTextSize(tvTitle,  Color.parseColor(LayouUtil.getString("color_main_title")));

		FrameLayout flDetail = new FrameLayout(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1);
		llContent.addView(flDetail,layoutParams);

		LinearLayout llDetail = new LinearLayout(GlobalContext.get());
		llDetail.setOrientation(LinearLayout.HORIZONTAL);
		llDetail.setGravity(Gravity.CENTER_VERTICAL);
		FrameLayout.LayoutParams flLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.WRAP_CONTENT);
		flLayoutParams.gravity = Gravity.CENTER;
		flDetail.addView(llDetail,flLayoutParams);

		/*View view = new View(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,2);
		llDetail.addView(view,layoutParams);*/

		final HelpDetailImageViewData.HelpDetailBean helpDetailImg = helpDetailImageViewData.getData().get(0);

		if (!TextUtils.isEmpty(helpDetailImg.img)) {
			ImageView iv = new ImageView(GlobalContext.get());
			iv.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					JSONBuilder data = new JSONBuilder();
					data.put("title", helpDetailImageViewData.getHelpTitle());
					data.put("url", helpDetailImg.img);
					data.put("desc", helpDetailImg.text);
					data.put("isFromFile", helpDetailImageViewData.isFromFile);
					data.put("from", "detail");
					ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.help.ui.detail.qrcode", data.toBytes(), null);
				}
			});
			iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			if (helpDetailImg.img.startsWith("qrcode:")){
				try {
					iv.setImageBitmap(QRUtil.createQRCodeBitmap(helpDetailImg.img.replace("qrcode:",""), (int) LayouUtil.getDimen("y200")));
				} catch (WriterException e) {
					e.printStackTrace();
				}
			}else if (helpDetailImageViewData.isFromFile) {
				ImageLoader.getInstance().displayImage("file://" + helpDetailImg.img, new ImageViewAware(iv));
			} else {
				iv.setImageDrawable(LayouUtil.getDrawable(helpDetailImg.img));
			}
			layoutParams = new LinearLayout.LayoutParams(imageSize, imageSize);
			layoutParams.rightMargin = imageInterval;
			llDetail.addView(iv, layoutParams);
		}

		/*if (!TextUtils.isEmpty(helpDetailImg.text) && !TextUtils.isEmpty(helpDetailImg.img)) {
			view = new View(GlobalContext.get());
			layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
			llDetail.addView(view, layoutParams);
		}*/

		if (!TextUtils.isEmpty(helpDetailImg.text)) {
			helpDetailImg.text = helpDetailImg.text.replace("\n\n","\n");
			TextView tv = new TextView(GlobalContext.get());
			tv.setGravity(Gravity.CENTER_VERTICAL);
			TextViewUtil.setTextSize(tv, (Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_HELP_ITEM_SIZE1));
			TextViewUtil.setTextColor(tv, (Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_HELP_ITEM_COLOR1));
			tv.setText(LanguageConvertor.toLocale(helpDetailImg.text));
			layoutParams = new LinearLayout.LayoutParams(textWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
			llDetail.addView(tv, layoutParams);
		}

		/*view = new View(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,2);
		llDetail.addView(view,layoutParams);*/

		return llLayout;
	}

	private View createItemView(int position, String name, boolean showDivider){
		RelativeLayout itemView = new RelativeLayout(GlobalContext.get());
		itemView.setTag(position);

		FrameLayout flContent = new FrameLayout(GlobalContext.get());
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
		layoutParams.topMargin = (int)LayouUtil.getDimen("y2");
		layoutParams.bottomMargin = (int)LayouUtil.getDimen("y2");
		itemView.addView(flContent,layoutParams);

		LinearLayout llContent = new LinearLayout(GlobalContext.get());
		llContent.setOrientation(LinearLayout.HORIZONTAL);
		llContent.setGravity(Gravity.CENTER_VERTICAL);
		FrameLayout.LayoutParams mFLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
		mFLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		flContent.addView(llContent, mFLayoutParams);

		LinearLayout llDetail = new LinearLayout(GlobalContext.get());
		llDetail.setGravity(Gravity.CENTER_VERTICAL);
		llDetail.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams mLLayoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
		mLLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		mLLayoutParams.leftMargin = (int) LayouUtil.getDimen("y15");
		llContent.addView(llDetail,mLLayoutParams);

		TextView tvContent = new TextView(GlobalContext.get());
		tvContent.setEllipsize(TextUtils.TruncateAt.END);
		tvContent.setSingleLine();
		tvContent.setGravity(Gravity.BOTTOM);
		mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		mLLayoutParams.gravity = Gravity.CENTER;
		mLLayoutParams.rightMargin = (int) LayouUtil.getDimen("y6");
		llDetail.addView(tvContent,mLLayoutParams);

		View divider = new View(GlobalContext.get());
		divider.setVisibility(View.GONE);
		//divider.setBackgroundColor(Color.parseColor("#4c4c4c"));
		divider.setBackground(LayouUtil.getDrawable("line"));
		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,(int) Math.ceil(LayouUtil.getDimen("y1")));
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		itemView.addView(divider, layoutParams);

		if (showDivider) {
			TextViewUtil.setTextSize(tvContent, (Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_HELP_LABEL_ITEM_SIZE1));
			TextViewUtil.setTextColor(tvContent, (Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_HELP_ITEM_COLOR1));
		}else {
			TextViewUtil.setTextSize(tvContent, (Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_HELP_ITEM_SIZE1));
			TextViewUtil.setTextColor(tvContent, (Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_HELP_ITEM_COLOR1));
		}

		tvContent.setText(LanguageConvertor.toLocale(StringUtils.isEmpty(name) ?"" : name));

		divider.setVisibility(showDivider?View.VISIBLE:View.INVISIBLE);

		return itemView;
	}

	private View createItemViewNone(int position, String name, boolean showDivider){
		RelativeLayout itemView = new RelativeLayout(GlobalContext.get());
		itemView.setGravity(Gravity.CENTER_VERTICAL);
		itemView.setTag(position);

		ImageView ivBack = new ImageView(GlobalContext.get());
		ivBack.setId(ViewUtils.generateViewId());
		ivBack.setImageDrawable(LayouUtil.getDrawable("back"));
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ivBackSize,ivBackSize);
		layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
		itemView.addView(ivBack,layoutParams);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogUtil.logd(WinLayout.logTag+ "onClick: back");
                if (VersionManager.getInstance().isUseHelpNewTag()) {
                    com.txznet.comm.remote.util.ConfigUtil.setShowHelpNewTag(false);
                }
                RecordWin2Manager.getInstance().operateView(TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK,
                        TXZRecordWinManager.RecordWin2.RecordWinController.VIEW_HELP_BACK, 0, 0);
            }
        });

		TextView tvBack = new TextView(GlobalContext.get());
		tvBack.setGravity(Gravity.CENTER_VERTICAL);
		//tvBack.setText("返回");
		tvBack.setText(LanguageConvertor.toLocale(name));
		TextViewUtil.setTextSize(tvBack,tvBackSize);
		TextViewUtil.setTextColor(tvBack,tvBackColor);
		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.RIGHT_OF,ivBack.getId());
		layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
		itemView.addView(tvBack,layoutParams);

		/*TextView tvContent = new TextView(GlobalContext.get());
		tvContent.setEllipsize(TextUtils.TruncateAt.END);
		tvContent.setSingleLine();
		tvContent.setGravity(Gravity.BOTTOM);
		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		itemView.addView(tvContent,layoutParams);*/

		View divider = new View(GlobalContext.get());
		divider.setVisibility(View.GONE);
		divider.setBackground(LayouUtil.getDrawable("line"));
		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,(int) Math.ceil(LayouUtil.getDimen("y1")));
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		itemView.addView(divider, layoutParams);

		/*if (showDivider) {
			TextViewUtil.setTextSize(tvContent, (Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_HELP_LABEL_ITEM_SIZE1));
			TextViewUtil.setTextColor(tvContent, (Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_HELP_ITEM_COLOR1));
		}else {
			TextViewUtil.setTextSize(tvContent, (Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_HELP_ITEM_SIZE1));
			TextViewUtil.setTextColor(tvContent, (Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_HELP_ITEM_COLOR1));
		}*/
		if (showDivider) {
			TextViewUtil.setTextSize(tvBack, (Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_HELP_LABEL_ITEM_SIZE1));
			TextViewUtil.setTextColor(tvBack, (Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_HELP_ITEM_COLOR1));
		}else {
			TextViewUtil.setTextSize(tvBack, (Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_HELP_ITEM_SIZE1));
			TextViewUtil.setTextColor(tvBack, (Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_HELP_ITEM_COLOR1));
		}

		//tvContent.setText(StringUtils.isEmpty(name) ?"" : name);
		tvBack.setText(LanguageConvertor.toLocale(StringUtils.isEmpty(name) ?"" : name));

		divider.setVisibility(showDivider?View.VISIBLE:View.INVISIBLE);

		return itemView;
	}

	@Override
	public void updateProgress(int progress, int selection) {

	}

	@Override
	public void snapPage(boolean next) {

	}

	@Override
	public void updateItemSelect(int arg0) {
		// TODO Auto-generated method stub
		
	}
}
