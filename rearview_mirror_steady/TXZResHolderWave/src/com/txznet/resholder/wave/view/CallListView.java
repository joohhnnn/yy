package com.txznet.resholder.wave.view;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.ui.theme.ThemeConfigManager;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ListViewItemAnim;
import com.txznet.comm.ui.view.GradientProgressBar;
import com.txznet.comm.ui.view.RippleView;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.CallListViewData;
import com.txznet.comm.ui.viewfactory.data.CallListViewData.CallBean;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.ICallListView;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.txz.util.LanguageConvertor;

@SuppressLint("NewApi")
public class CallListView extends ICallListView {

	private static CallListView sInstance = new CallListView();
	
	private List<View> mItemViews;	
	
	//字体等参数配置
	
	private int flContentMarginTop;
	private int flContentMarginBottom;
	private Drawable tvNumBg;
	private int tvNumWidth;
	private int tvNumHeight;
	private int tvNumMarginLeft;
	private int llDetailMarginLeft;
	private int tvContentMarginLeft;
	private int tvContentMarginRight;
	
	private int dividerHeight;
	
	private float tvNumSize;
	private int tvNumColor;
	private float tvContentSize;
	private int tvContentColor;
	private int tvProvinceWidth;
	private int tvCityWidth;
	private int tvCityMarginLeft;
	private int tvIspWidth;
	private int tvIspMarginLeft;


	private float tvProvinceSize;
	private int tvProvinceColor;
	private float tvCitySize;
	private int tvCityColor;
	private float tvIspSize;
	private int tvIspColor;
	private ArrayList<GradientProgressBar> progressBars = new ArrayList<GradientProgressBar>(4);
	
	private CallListView() {
	}

	public static CallListView getInstance(){
		return sInstance;
	}
	
	@Override
	public void release() {
		super.release();
		if (mItemViews != null) {
			mItemViews.clear();
		}
		if (progressBars != null) {
			progressBars.clear();
		}
	}
	
	@Override
	public void updateProgress(int progress, int selection) {
		LogUtil.logd("updateProgress " + progress + "," + selection);
		if (progressBars.size() > selection) {
			GradientProgressBar progressBar = progressBars.get(selection);
			if (progress < 0) {
				if (progressBar.getVisibility() == View.VISIBLE) {
					progressBar.setVisibility(View.GONE);
				}
			}else {
				if (progressBar.getVisibility() == View.GONE) {
					progressBar.setVisibility(View.VISIBLE);
				}
				progressBar.setProgress(progress);
			}
		}
	}

	@Override
	public ViewAdapter getView(ViewData data) {
		CallListViewData callListViewData = (CallListViewData) data;
		ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(callListViewData);
		
		LinearLayout llLayout = new LinearLayout(GlobalContext.get());
		llLayout.setGravity(Gravity.CENTER_VERTICAL);
		llLayout.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,ListTitleView.getInstance().getTitleHeight());
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		llLayout.addView(titleViewAdapter.view,layoutParams);
		
		LinearLayout llContent = new LinearLayout(GlobalContext.get());
		llContent.setOrientation(LinearLayout.VERTICAL);
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,ConfigUtil.getDisplayLvItemH(false) * ConfigUtil.getVisbileCount());
		llLayout.addView(llContent,layoutParams);
		llContent.setLayoutAnimation(ListViewItemAnim.getAnimationController());
		progressBars.clear();
		mItemViews = new ArrayList<View>();
		for (int i = 0; i < callListViewData.count; i++) {
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
					ConfigUtil.getDisplayLvItemH(false));
			View itemView = createItemView(i, callListViewData.isMultName, callListViewData.getData().get(i));
			llContent.addView(itemView, layoutParams);
			mItemViews.add(itemView);
		}
		
		ViewAdapter viewAdapter = new ViewAdapter();
		viewAdapter.type = data.getType();
		viewAdapter.view = llLayout;
		viewAdapter.isListView = true;
		viewAdapter.object = CallListView.getInstance();
		return viewAdapter;
	}

	@Override
	public void init() {
		// 初始化配置，例如字体颜色等
		flContentMarginTop = (int) ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_CONTENT_MARGINTOP);
		flContentMarginBottom = (int) ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_CONTENT_MARGINBOTTOM);
		tvNumBg = LayouUtil.getDrawable("poi_item_circle_bg");
		tvNumWidth = (int)ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_TXTNUM_WIDTH);
		tvNumHeight = (int)ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_TXTNUM_HEIGHT);
		tvNumMarginLeft = (int) ThemeConfigManager.getX(ThemeConfigManager.LIST_ITEM_TXTNUM_MARGINLEFT);
		llDetailMarginLeft = (int) ThemeConfigManager.getX(ThemeConfigManager.LIST_ITEM_LAYOUT_CONTENT_MARGINLEFT);
		tvContentMarginLeft = (int) ThemeConfigManager.getX(ThemeConfigManager.LIST_ITEM_TXTCONTENT_MARGINLEFT);
		tvContentMarginRight = (int) ThemeConfigManager.getX(ThemeConfigManager.LIST_ITEM_TXTCONTENT_MARGINLEFT);
		
		dividerHeight = (int) Math.ceil(ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_DIVIDER_HEIGHT));
		
		tvNumSize=(Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_CALL_INDEX_SIZE1);
		tvNumColor=(Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_CALL_INDEX_COLOR1);
		tvContentSize=(Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_CALL_ITEM_SIZE1);
		tvContentColor=(Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_CALL_ITEM_COLOR1);
		tvProvinceSize=(Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_CALL_ITEM_SIZE2);
		tvProvinceColor=(Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_CALL_ITEM_COLOR2);
		tvCitySize=(Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_CALL_ITEM_SIZE2);
		tvCityColor=(Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_CALL_ITEM_COLOR2);
		tvIspSize=(Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_CALL_ITEM_SIZE2);
		tvIspColor=(Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_CALL_ITEM_COLOR2);
		
		tvProvinceWidth = (int) LayouUtil.getDimen("x70");
		tvCityWidth = (int) LayouUtil.getDimen("x70");
		tvCityMarginLeft = (int) LayouUtil.getDimen("x4");
		tvIspWidth = (int) LayouUtil.getDimen("x46");
		tvIspMarginLeft = (int) LayouUtil.getDimen("x4");
		
	}
	

	@Override
	public void snapPage(boolean next) {
		LogUtil.logd("update snap "+next);
	}
	
	@Override
	public List<View> getFocusViews() {
		return mItemViews;
	}
	
	private View createItemView(int position, boolean isMultiName,CallBean callBean){
		RippleView itemView = new RippleView(GlobalContext.get());
		itemView.setTag(position);
		itemView.setOnClickListener(ListTitleView.getInstance().getOnItemClickListener());
		itemView.setOnTouchListener(ListTitleView.getInstance().getOnTouchListener());
		FrameLayout flContent = new FrameLayout(GlobalContext.get());
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
		layoutParams.topMargin = flContentMarginTop;
		layoutParams.bottomMargin = flContentMarginBottom;
		itemView.addView(flContent,layoutParams);
		
		GradientProgressBar mProgressBar = new GradientProgressBar(GlobalContext.get());
		mProgressBar.setVisibility(View.GONE);
		FrameLayout.LayoutParams mFLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
		flContent.addView(mProgressBar, mFLayoutParams);
		progressBars.add(mProgressBar);
		
		LinearLayout llContent = new LinearLayout(GlobalContext.get());
		llContent.setOrientation(LinearLayout.HORIZONTAL);
		llContent.setGravity(Gravity.CENTER_VERTICAL);
		mFLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
		mFLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		flContent.addView(llContent, mFLayoutParams);
		
		TextView tvNum = new TextView(GlobalContext.get());
		tvNum.setBackground(tvNumBg);
		tvNum.setGravity(Gravity.CENTER);
		tvNum.setIncludeFontPadding(false);
		tvNum.setPadding(0, 0, 0, 0);
		LinearLayout.LayoutParams mLLayoutParams = new LinearLayout.LayoutParams(tvNumWidth,tvNumHeight);
		mLLayoutParams.leftMargin = tvNumMarginLeft;
		mLLayoutParams.gravity = Gravity.CENTER;
		llContent.addView(tvNum,mLLayoutParams);
		
		LinearLayout llDetail = new LinearLayout(GlobalContext.get());
		llDetail.setGravity(Gravity.CENTER_VERTICAL);
		llDetail.setOrientation(LinearLayout.VERTICAL);
		mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
		mLLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		mLLayoutParams.leftMargin = llDetailMarginLeft;
		llContent.addView(llDetail,mLLayoutParams);
		
		LinearLayout tvLayout = new LinearLayout(GlobalContext.get());
		tvLayout.setGravity(Gravity.CENTER_VERTICAL);
		tvLayout.setOrientation(LinearLayout.HORIZONTAL);
		mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		mLLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		llDetail.addView(tvLayout,mLLayoutParams);
		
		TextView tvContent = new TextView(GlobalContext.get());
		tvContent.setEllipsize(TruncateAt.END);
		tvContent.setSingleLine();
		tvContent.setGravity(Gravity.BOTTOM);
		mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		mLLayoutParams.gravity = Gravity.CENTER_VERTICAL;
//		mLLayoutParams.leftMargin = tvContentMarginLeft;
		// llDetail.addView(tvContent,mLLayoutParams);
		tvLayout.addView(tvContent, mLLayoutParams);
		
		TextView tvPhone = new TextView(GlobalContext.get());
		tvPhone.setEllipsize(TruncateAt.END);
		tvPhone.setSingleLine();
		tvPhone.setGravity(Gravity.BOTTOM);
		mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		mLLayoutParams.leftMargin = tvContentMarginLeft;
		mLLayoutParams.rightMargin = tvContentMarginRight;
		tvLayout.addView(tvPhone, mLLayoutParams);
		
		LinearLayout llDesc = new LinearLayout(GlobalContext.get());
		llDesc.setOrientation(LinearLayout.HORIZONTAL);
		llDesc.setGravity(Gravity.CENTER_VERTICAL);
		mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		llDetail.addView(llDesc,mLLayoutParams);
		
		TextView tvProvince = new TextView(GlobalContext.get());
		tvProvince.setSingleLine();
		tvProvince.setEllipsize(TruncateAt.END);
		mLLayoutParams = new LinearLayout.LayoutParams(tvProvinceWidth,LinearLayout.LayoutParams.WRAP_CONTENT);
		mLLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		llDesc.addView(tvProvince,mLLayoutParams);
		
		TextView tvCity = new TextView(GlobalContext.get());
		tvCity.setSingleLine();
		tvCity.setEllipsize(TruncateAt.END);
		mLLayoutParams = new LinearLayout.LayoutParams(tvCityWidth,LinearLayout.LayoutParams.WRAP_CONTENT);
		mLLayoutParams.leftMargin = tvCityMarginLeft;
		mLLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		llDesc.addView(tvCity,mLLayoutParams);
		
		TextView tvIsp = new TextView(GlobalContext.get());
		tvIsp.setSingleLine();
		tvIsp.setEllipsize(TruncateAt.END);
		mLLayoutParams = new LinearLayout.LayoutParams(tvIspWidth,LinearLayout.LayoutParams.WRAP_CONTENT);
		mLLayoutParams.leftMargin = tvIspMarginLeft;
		mLLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		llDesc.addView(tvIsp,mLLayoutParams);
		
		View divider = new View(GlobalContext.get());
		divider.setVisibility(View.GONE);
		divider.setBackgroundColor(Color.parseColor("#4c4c4c"));
		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,dividerHeight);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		itemView.addView(divider, layoutParams);
		
		TextViewUtil.setTextSize(tvNum, tvNumSize);
		TextViewUtil.setTextColor(tvNum, tvNumColor);
		TextViewUtil.setTextSize(tvContent, tvContentSize);
		TextViewUtil.setTextColor(tvContent, tvContentColor);
		TextViewUtil.setTextSize(tvPhone, tvContentSize);
		TextViewUtil.setTextColor(tvPhone, tvContentColor);
		TextViewUtil.setTextSize(tvProvince, tvProvinceSize);
		TextViewUtil.setTextColor(tvProvince, tvProvinceColor);
		TextViewUtil.setTextSize(tvCity, tvCitySize);
		TextViewUtil.setTextColor(tvCity, tvCityColor);
		TextViewUtil.setTextSize(tvIsp, tvIspSize);
		TextViewUtil.setTextColor(tvIsp, tvIspColor);

		
		
		tvNum.setText(String.valueOf(position + 1));
		String name = callBean.name;
		String number = callBean.number;
		if (TextUtils.isEmpty(name) && !TextUtils.isEmpty(number)) {
			tvContent.setText(number);
			tvPhone.setText("");
		} else {
			tvContent.setText(name == null ? "" : LanguageConvertor.toLocale(name));
			tvPhone.setText(number == null ? "" : number);
		}
		tvProvince.setText(callBean.province == null ? "" : LanguageConvertor.toLocale(callBean.province));
		tvCity.setText(callBean.city == null ? "" : LanguageConvertor.toLocale(callBean.city));
		tvIsp.setText(callBean.isp == null ? "" : LanguageConvertor.toLocale(callBean.isp));

        if (callBean.province == null && callBean.city == null && callBean.isp == null) {
            tvProvince.setVisibility(View.GONE);
            tvCity.setVisibility(View.GONE);
        } else {
            tvProvince.setVisibility(View.VISIBLE);
            tvCity.setVisibility(View.VISIBLE);
        }
        tvIsp.setVisibility(callBean.isp == null ? View.GONE : View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);

		divider.setVisibility(View.VISIBLE);
		
		
		return itemView;
	}
	
	
}
