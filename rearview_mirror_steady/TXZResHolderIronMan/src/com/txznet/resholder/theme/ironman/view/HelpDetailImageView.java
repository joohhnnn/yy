package com.txznet.resholder.theme.ironman.view;

import android.graphics.Color;
import android.text.TextUtils;
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
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.ui.theme.ThemeConfigManager;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ScreenUtil;
import com.txznet.comm.ui.viewfactory.ViewFactory;
import com.txznet.comm.ui.viewfactory.data.HelpDetailImageViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IHelpDetailImageView;
import com.txznet.comm.util.StringUtils;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.txz.util.QRUtil;


public class HelpDetailImageView extends IHelpDetailImageView {
	private static HelpDetailImageView instance = new HelpDetailImageView();

	public static HelpDetailImageView getInstance() {
		return instance;
	}
	private int llContentPaddingLeft;
	private int llContentPaddingTop;
	private int llContentPaddingRight;
	private int llContentPaddingBottom;
	private int listItemMarginTop;

	@Override
	public void init() {
		super.init();
		listItemMarginTop = (int) ThemeConfigManager.getY(ThemeConfigManager.LIST_CONTENT_ITEM_MARGINTOP);
		llContentPaddingLeft = (int) ThemeConfigManager.getX(ThemeConfigManager.LIST_CONTENT_PADDINGLEFT);
		llContentPaddingTop = (int) ThemeConfigManager.getY(ThemeConfigManager.LIST_CONTENT_PADDINGTOP);
		llContentPaddingRight = (int) ThemeConfigManager.getX(ThemeConfigManager.LIST_CONTENT_PADDINGRIGHT);
		llContentPaddingBottom = (int) ThemeConfigManager.getY(ThemeConfigManager.LIST_CONTENT_PADDINGBOTTOM);
	}

	@Override
	public ViewFactory.ViewAdapter getView(ViewData data) {
		HelpDetailImageViewData helpDetailImageViewData = (HelpDetailImageViewData) data;
		ViewFactory.ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(helpDetailImageViewData);

		LinearLayout llLayout = new LinearLayout(GlobalContext.get());
		llLayout.setGravity(Gravity.CENTER_VERTICAL);
		llLayout.setOrientation(LinearLayout.VERTICAL);

		int contentHeight = ScreenUtil.getDisplayLvItemH(false) * ScreenUtil.getVisbileCount() + llContentPaddingTop+llContentPaddingBottom + listItemMarginTop *4;
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		llLayout.setLayoutParams(layoutParams);
		llLayout.setGravity(Gravity.CENTER_VERTICAL);
		llLayout.setOrientation(LinearLayout.VERTICAL);

		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ListTitleView.getInstance().getTitleHeight());
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		llLayout.addView(titleViewAdapter.view,layoutParams);

		LinearLayout llContent = new LinearLayout(GlobalContext.get());
		llContent.setOrientation(LinearLayout.VERTICAL);
		llContent.setBackground(LayouUtil.getDrawable("list_bg"));
		llContent.setPadding(llContentPaddingLeft,llContentPaddingTop,llContentPaddingRight,llContentPaddingBottom);
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,contentHeight);
		llLayout.addView(llContent,layoutParams);
		int itemHeight = (int)(contentHeight/(ScreenUtil.getVisbileCount()*1.25f));
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,itemHeight);

		View itemView = createItemView(0, helpDetailImageViewData.getHelpTitle(), true);
		llContent.addView(itemView,layoutParams);
		LinearLayout llDetail = new LinearLayout(GlobalContext.get());
		llDetail.setOrientation(LinearLayout.HORIZONTAL);
		llDetail.setGravity(Gravity.CENTER_VERTICAL);
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
		llContent.addView(llDetail,layoutParams);
		View view = new View(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,2);
		llDetail.addView(view,layoutParams);

		HelpDetailImageViewData.HelpDetailBean helpDetailImg = helpDetailImageViewData.getData().get(0);

		if (!TextUtils.isEmpty(helpDetailImg.img)) {
			ImageView iv = new ImageView(GlobalContext.get());
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
			layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 5);
			llDetail.addView(iv, layoutParams);
		}

		if (!TextUtils.isEmpty(helpDetailImg.text) && !TextUtils.isEmpty(helpDetailImg.img)) {
			view = new View(GlobalContext.get());
			layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
			llDetail.addView(view, layoutParams);
		}

		if (!TextUtils.isEmpty(helpDetailImg.text)) {
			TextView tv = new TextView(GlobalContext.get());
			tv.setGravity(Gravity.CENTER_VERTICAL);
			TextViewUtil.setTextSize(tv, (Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_HELP_ITEM_SIZE1));
			TextViewUtil.setTextColor(tv, (Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_HELP_ITEM_COLOR1));
			tv.setText(helpDetailImg.text);
			layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 10);
			llDetail.addView(tv, layoutParams);
		}

		view = new View(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,2);
		llDetail.addView(view,layoutParams);

		ViewFactory.ViewAdapter adapter = new ViewFactory.ViewAdapter();
		adapter.type = data.getType();
		adapter.view = llLayout;
		adapter.object = HelpDetailImageView.getInstance();
		return adapter;
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
		divider.setBackgroundColor(Color.parseColor("#4c4c4c"));
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

		tvContent.setText(StringUtils.isEmpty(name) ?"" : name);

		divider.setVisibility(showDivider?View.VISIBLE:View.INVISIBLE);

		return itemView;
	}

	@Override
	public void updateProgress(int progress, int selection) {

	}

	@Override
	public void snapPage(boolean next) {

	}
}
