package com.txznet.record.view;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ScreenUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.record.lib.R;
import com.txznet.txz.util.LanguageConvertor;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TitleView extends FrameLayout {

	public static class Info {
		public static final int MUSIC = 1;
		public static final int POI = 2;
		public static final int WX = 3;

		public String prefix;
		public String titlefix;
		public String aftfix;
		public String midfix;
		public String cityfix;
		public int curPage;
		public int maxPage;
		public boolean hideDrawable;
	}

	private TextView mPreTv;
	private TextView mAftTv;
	private TextView mCityTv;
	private TextView mMidTv;
	private TextView mTitleTv;
	private ImageView mCityImg ;
	private LinearLayout mPagerLy;
	private TextView mPrePagerTv;
	private TextView mCurPagerTv;
	private TextView mNextPagerTv;
	
	int mCurPage;
	int mMaxPage;

	public TitleView(Context context) {
		this(context, null);
	}

	public TitleView(Context context, AttributeSet attr) {
		this(context, attr, 0);
	}

	public TitleView(Context context, AttributeSet attr, int defValue) {
		super(context, attr, defValue);
		createView();
		update((Info) null);
	}

	public View createView() {
		View conView = View.inflate(getContext(), R.layout.lv_title_ly, this);
		mPreTv = (TextView) conView.findViewById(R.id.prefix_tv);
		mAftTv = (TextView) conView.findViewById(R.id.aftfix_tv);
		mMidTv = (TextView) conView.findViewById(R.id.midfix_tv);
		mCityTv = (TextView) conView.findViewById(R.id.title_city);
		mCityTv.setFocusable(false);
		mCityTv.setFocusableInTouchMode(false);
		mCityImg = (ImageView) conView.findViewById(R.id.img_city);
		mTitleTv = (TextView) conView.findViewById(R.id.title_tv);
		mTitleTv.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

			@Override
			public void onGlobalLayout() {
				mTitleTv.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				Rect bounds = new Rect();
				mTitleTv.getHitRect(bounds);
				bounds.top -= 10;
				bounds.left -= 100;
				bounds.bottom += 10;
				bounds.right += 100;
				TouchDelegate td = new TouchDelegate(bounds, mTitleTv);
				if (View.class.isInstance(mTitleTv.getParent())) {
					((View) mTitleTv.getParent()).setTouchDelegate(td);
				}
			}
		});
		mTitleTv.setFocusable(false);
		mTitleTv.setFocusableInTouchMode(false);
		mPagerLy = (LinearLayout) conView.findViewById(R.id.pager_ly);
		mPagerLy.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				mPagerLy.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				LinearLayout.LayoutParams layout = (LinearLayout.LayoutParams)mPagerLy.getLayoutParams();
				layout.leftMargin = layout.height;
				mPagerLy.setLayoutParams(layout);
			}
		});
		mPrePagerTv = (TextView) conView.findViewById(R.id.pre_pager);
		mCurPagerTv = (TextView) conView.findViewById(R.id.curPager);
		mNextPagerTv = (TextView) conView.findViewById(R.id.next_pager);
		
		mPrePagerTv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				JSONBuilder jb = new JSONBuilder();
				jb.put("type", 1);
				jb.put("clicktype", 1);
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.display.page",
						jb.toBytes(), null);
			}
		});
		mPrePagerTv.setFocusable(false);
		mPrePagerTv.setFocusableInTouchMode(false);
		mNextPagerTv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				JSONBuilder jb = new JSONBuilder();
				jb.put("type", 1);
				jb.put("clicktype", 2);
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.display.page",
						jb.toBytes(), null);
			}
		});
		mNextPagerTv.setFocusable(false);
		mNextPagerTv.setFocusableInTouchMode(false);
		return this;
	}

	public void setOnClickListener(OnClickListener listener) {
		if (mTitleTv != null) {
			mTitleTv.setOnClickListener(listener);
		}
	}
	public void setCityOnClickListener(OnClickListener listener) {
		if (mCityTv != null) {
			mCityTv.setOnClickListener(listener);
		}
	}

	public void update(Info updateInfo) {
		clearView();
		if (updateInfo == null) {
			return;
		}

		TextViewUtil.setTextSize(mPreTv,ViewConfiger.SIZE_POI_INTRO_SIZE1);
		TextViewUtil.setTextColor(mPreTv,ViewConfiger.COLOR_POI_INTRO_COLOR1);
		TextViewUtil.setTextSize(mAftTv,ViewConfiger.SIZE_POI_INTRO_SIZE1);
		TextViewUtil.setTextColor(mAftTv,ViewConfiger.COLOR_POI_INTRO_COLOR1);
		TextViewUtil.setTextSize(mMidTv,ViewConfiger.SIZE_POI_INTRO_SIZE1);
		TextViewUtil.setTextColor(mMidTv,ViewConfiger.COLOR_POI_INTRO_COLOR1);	
		TextViewUtil.setTextSize(mCityTv,ViewConfiger.SIZE_POI_INTRO_SIZE2);
		TextViewUtil.setTextColor(mCityTv,ViewConfiger.COLOR_POI_INTRO_CLOR2);	
		TextViewUtil.setTextSize(mTitleTv,ViewConfiger.SIZE_POI_INTRO_SIZE2);
		TextViewUtil.setTextColor(mTitleTv,ViewConfiger.COLOR_POI_INTRO_CLOR2);
		TextViewUtil.setTextSize(mPrePagerTv,ViewConfiger.SIZE_POI_PAGE_SIZE1);
		TextViewUtil.setTextColor(mPrePagerTv,ViewConfiger.COLOR_POI_PAGE_COLOR1);
		TextViewUtil.setTextSize(mCurPagerTv,ViewConfiger.SIZE_POI_PAGE_SIZE1);
		TextViewUtil.setTextColor(mCurPagerTv,ViewConfiger.COLOR_POI_PAGE_COLOR2);
		TextViewUtil.setTextSize(mNextPagerTv,ViewConfiger.SIZE_POI_PAGE_SIZE1);
		TextViewUtil.setTextColor(mNextPagerTv,ViewConfiger.COLOR_POI_PAGE_COLOR2);
		
		mCurPage = updateInfo.curPage;
		mMaxPage = updateInfo.maxPage;
		refreshPageInfo(mCurPage, mMaxPage);

		String prefix = updateInfo.prefix;
		if (!TextUtils.isEmpty(prefix)) {
			mPreTv.setText(LanguageConvertor.toLocale(prefix));
			mPreTv.setVisibility(View.VISIBLE);
		}
		boolean hideDrawable = updateInfo.hideDrawable;
		String titlefix = updateInfo.titlefix;
		if (!TextUtils.isEmpty(titlefix)) {
			mTitleTv.setText(LanguageConvertor.toLocale(titlefix));
			mTitleTv.setVisibility(View.VISIBLE);
			Drawable tvTitleDrawableLeft = LayouUtil.getDrawable("icon_edit_new");
			if (tvTitleDrawableLeft != null && !hideDrawable) {
				int height = tvTitleDrawableLeft.getIntrinsicHeight();
				float scale = (float)mTitleTv.getLineHeight()/(float)height;
				tvTitleDrawableLeft.setBounds(0, 0, (int)(tvTitleDrawableLeft.getIntrinsicWidth()*scale), (int)(height*scale));
				mTitleTv.setCompoundDrawables(null, null, tvTitleDrawableLeft, null);
			}
		}

		String cityfix = updateInfo.cityfix;
		if (!TextUtils.isEmpty(cityfix)) {
			if(cityfix.equals("LOADING") && !hideDrawable){
				mCityImg.setImageResource(R.drawable.poimap_loading_anim);
		        AnimationDrawable animationDrawable1 = (AnimationDrawable) mCityImg.getDrawable();
		        mCityImg.setVisibility(View.VISIBLE);		
		        animationDrawable1.start();
			}else{
			mCityTv.setText(LanguageConvertor.toLocale(cityfix));
			mCityTv.setVisibility(View.VISIBLE);
				Drawable tvCityDrawableLeft = LayouUtil.getDrawable("icon_arrow");
				if (tvCityDrawableLeft != null && !hideDrawable) {
					int height = tvCityDrawableLeft.getIntrinsicHeight();
					float scale = (float)mCityTv.getLineHeight()/(float)height;
					tvCityDrawableLeft.setBounds(0, 0, (int)(tvCityDrawableLeft.getIntrinsicWidth()*scale), (int)(height*scale));
					mCityTv.setCompoundDrawables(null, null, tvCityDrawableLeft, null);
				}
			}
		}

		String aftfix = updateInfo.aftfix;
		if (!TextUtils.isEmpty(aftfix)) {
			mAftTv.setText(LanguageConvertor.toLocale(aftfix));
			mAftTv.setVisibility(View.VISIBLE);
		}
		String midfix = updateInfo.midfix;
		if (!TextUtils.isEmpty(midfix)) {
			mMidTv.setText(LanguageConvertor.toLocale(midfix));
			mMidTv.setVisibility(View.VISIBLE);
		}
		if( !TextUtils.isEmpty(updateInfo.cityfix) ){
			mTitleTv.setMaxEms(1000);//取消最大长度的限制
			mTitleTv.setEllipsize(null);
		}else{
			if (ScreenUtil.getScreenWidth() <= 800) {
				mTitleTv.setMaxEms(8);
			} else {
				mTitleTv.setMaxEms(10);
			}			
		}
	}
	
	public void refreshCurPage(int curPage) {
		if (curPage > mMaxPage - 1) {
			curPage = mMaxPage - 1;
		}
		if (curPage < 0) {
			curPage = 0;
		}
		
		mCurPage = curPage;
		refreshPageInfo(mCurPage, mMaxPage);
	}

	private void refreshPageInfo(int curPage, int maxPage) {
		if (maxPage != 0 && maxPage != -1/* && maxPage > 1*/) {
			mPagerLy.setVisibility(VISIBLE);
			if (curPage == 0) {
				TextViewUtil.setTextColor(mPrePagerTv,ViewConfiger.COLOR_POI_PAGE_COLOR1);
			} else {
				TextViewUtil.setTextColor(mPrePagerTv,ViewConfiger.COLOR_POI_PAGE_COLOR2);
			}
			if (curPage == maxPage - 1) {
				TextViewUtil.setTextColor(mNextPagerTv,ViewConfiger.COLOR_POI_PAGE_COLOR1);
			} else {
				TextViewUtil.setTextColor(mNextPagerTv,ViewConfiger.COLOR_POI_PAGE_COLOR2);
			}
			if(maxPage == 1){
				TextViewUtil.setTextColor(mPrePagerTv,ViewConfiger.COLOR_POI_PAGE_COLOR1);
				TextViewUtil.setTextColor(mNextPagerTv,ViewConfiger.COLOR_POI_PAGE_COLOR1);
			}
			mCurPagerTv.setText((curPage + 1) + "/" + maxPage);
		} else {
			mPagerLy.setVisibility(GONE);
		}
	}

	private void clearView() {
		mPreTv.setText("");
		mAftTv.setText("");
		mMidTv.setText("");
		mTitleTv.setText("");
		mTitleTv.setVisibility(GONE);
		mCityTv.setText("");
		mCityTv.setVisibility(GONE);
		mCityImg.setVisibility(View.GONE);
	}
}