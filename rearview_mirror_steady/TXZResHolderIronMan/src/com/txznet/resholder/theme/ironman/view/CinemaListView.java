package com.txznet.resholder.theme.ironman.view;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils.TruncateAt;
import android.util.LruCache;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.UI2Manager;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ViewUtils;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.CinemaListViewData;
import com.txznet.comm.ui.viewfactory.data.CinemaListViewData.CinemaBean;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.ICinemaListView;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.comm.util.imageloader.ImageLoaderInitialize.ImageLoaderImpl;
import com.txznet.txz.util.LanguageConvertor;
import com.txznet.txz.util.runnables.Runnable1;

@SuppressLint("NewApi")
public class CinemaListView extends ICinemaListView {

	private static CinemaListView sInstance = new CinemaListView();
	
	private List<View> mItemViews;
	
	public static final int DEFAULT_CINEMA_CACHEPOST = 4;
	private LruCache<String, Bitmap> mCachePost = new LruCache<String, Bitmap>(DEFAULT_CINEMA_CACHEPOST);	
	//字体等参数配置
	
	private Drawable cinemaBg;
	
	private CinemaListView() {
	}

	public static CinemaListView getInstance(){
		return sInstance;
	}
	
	@Override
	public void updateProgress(int progress, int selection) {
		LogUtil.logd("updateProgress " + progress + "," + selection);
	}

	@Override
	public void release() {
		super.release();
		if (mItemViews != null) {
			mItemViews.clear();
		}
	}
	
	@Override
	public ViewAdapter getView(ViewData data) {
		CinemaListViewData cinemaData = (CinemaListViewData)data;
		ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(cinemaData);
		
		LinearLayout llLayout = new LinearLayout(GlobalContext.get());
		llLayout.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,ListTitleView.getInstance().getTitleHeight());
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		llLayout.addView(titleViewAdapter.view,layoutParams);
		
		LinearLayout llContent = new LinearLayout(GlobalContext.get());
		llContent.setOrientation(LinearLayout.HORIZONTAL);
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
		llLayout.addView(llContent,layoutParams);
		llContent.setBackground(cinemaBg);
		llContent.setPadding((int)LayouUtil.getDimen("x5"), (int)LayouUtil.getDimen("y30"), (int)LayouUtil.getDimen("x5"), (int)LayouUtil.getDimen("y30"));
		mItemViews = new ArrayList<View>();
		for (int i = 0; i < cinemaData.count; i++) {
			View itemView = createItemView(i, cinemaData.getData().get(i));
			addChildView(llContent, itemView, i);
			mItemViews.add(itemView);
		}
		int emptyCount = DEFAULT_CINEMA_CACHEPOST - cinemaData.count;
		if (emptyCount > 0) {
			for (int i = 0; i < emptyCount; i++) {
				addChildView(llContent, createEmptyView(), cinemaData.count + i);
			}
		}
		
		ViewAdapter viewAdapter = new ViewAdapter();
		viewAdapter.type = data.getType();
		viewAdapter.view = llLayout;
		viewAdapter.isListView = true;
		viewAdapter.object = CinemaListView.getInstance();
		return viewAdapter;
	}
	
	private void addChildView(LinearLayout parent,View view,int pos){
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
		if (pos == 0) {
			layoutParams.setMargins(0, 0, (int)LayouUtil.getDimen("y6"), 0);
		} else if (pos == DEFAULT_CINEMA_CACHEPOST - 1) {
			layoutParams.setMargins((int)LayouUtil.getDimen("y6"), 0, 0, 0);
		} else {
			layoutParams.setMargins((int)LayouUtil.getDimen("y6"), 0, (int)LayouUtil.getDimen("y6"), 0);
		}
		parent.addView(view,layoutParams);
	}

	@Override
	public void init() {
		// 初始化配置，例如字体颜色等
		cinemaBg = LayouUtil.getDrawable("movie_bg");
		
	}
	
	@Override
	public List<View> getFocusViews() {
		return mItemViews;
	}

	@Override
	public void snapPage(boolean next) {
		LogUtil.logd("update snap "+next);
	}
	
	private View createItemView(int position,CinemaBean cinemaBean){
		LinearLayout llItem = new LinearLayout(GlobalContext.get());
		llItem.setOrientation(LinearLayout.VERTICAL);
		llItem.setBackground(LayouUtil.getDrawable("movie_item_bg"));
		llItem.setPadding((int)LayouUtil.getDimen("x2"), llItem.getPaddingTop()+(int)LayouUtil.getDimen("y2"), (int)LayouUtil.getDimen("x2"), 0);
		
		ImageView ivBill = new ImageView(GlobalContext.get());
		ivBill.setScaleType(ScaleType.CENTER_CROP);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1);
		llItem.addView(ivBill,layoutParams);
		
		TextView tvTitle = new TextView(GlobalContext.get());
		tvTitle.setEllipsize(TruncateAt.END);
		tvTitle.setGravity(Gravity.CENTER);
		tvTitle.setPadding((int)LayouUtil.getDimen("y10"), (int)LayouUtil.getDimen("y10"),(int)LayouUtil.getDimen("y10"), (int)LayouUtil.getDimen("y5"));
		tvTitle.setSingleLine();
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		llItem.addView(tvTitle,layoutParams);
		
		LinearLayout llScore = new LinearLayout(GlobalContext.get());
		llScore.setGravity(Gravity.CENTER);
		llScore.setOrientation(LinearLayout.HORIZONTAL);
		llScore.setPadding((int)LayouUtil.getDimen("x2"), (int)LayouUtil.getDimen("y10"), (int)LayouUtil.getDimen("x2"), (int)LayouUtil.getDimen("y10"));
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		llItem.addView(llScore,layoutParams);
		
		ImageView ivScore = new ImageView(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(0,(int)LayouUtil.getDimen("y20"),0.7f);
		layoutParams.rightMargin = (int) LayouUtil.getDimen("x2");
		ivScore.setScaleType(ScaleType.FIT_END);
		llScore.addView(ivScore,layoutParams);
		
		RelativeLayout rlScore = new RelativeLayout(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,0.3f);
		llScore.addView(rlScore,layoutParams);
		
		TextView tvScorePre = new TextView(GlobalContext.get());
		tvScorePre.setIncludeFontPadding(false);
		tvScorePre.setSingleLine();
		tvScorePre.setId(ViewUtils.generateViewId());
		RelativeLayout.LayoutParams mRLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		rlScore.addView(tvScorePre,mRLayoutParams);
		
		TextView tvScoreAft = new TextView(GlobalContext.get());
		tvScoreAft.setSingleLine();
		mRLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		mRLayoutParams.addRule(RelativeLayout.RIGHT_OF,tvScorePre.getId());
		rlScore.addView(tvScoreAft,mRLayoutParams);
		
		TextViewUtil.setTextSize(tvTitle, LayouUtil.getDimen("y24"));
		TextViewUtil.setTextColor(tvTitle, Color.WHITE);
		TextViewUtil.setTextSize(tvScorePre, LayouUtil.getDimen("y30"));
		TextViewUtil.setTextColor(tvScorePre, Color.parseColor("#FA952F"));
		TextViewUtil.setTextSize(tvScoreAft, LayouUtil.getDimen("y18"));
		TextViewUtil.setTextColor(tvScoreAft, Color.parseColor("#FA952F"));
		
		
		tvTitle.setText(LanguageConvertor.toLocale(cinemaBean.title));
		loadDrawableByUrl(ivBill, cinemaBean.post);
		
		String sc = String.format("%.1f", cinemaBean.score);
		if (sc.contains(".")) {
			String[] scoreArray = sc.split("\\.");
			if (scoreArray != null && scoreArray.length > 1) {
				tvScorePre.setText(LanguageConvertor.toLocale(scoreArray[0]));
				tvScoreAft.setText(LanguageConvertor.toLocale("." + scoreArray[1]));
			}
		} else {
			tvScorePre.setText(LanguageConvertor.toLocale(sc));
			tvScoreAft.setText("");
		}

		ivScore.setImageDrawable(getSoreMark(cinemaBean.score));
		if (ivScore.getVisibility() != View.VISIBLE) {
			ivScore.setVisibility(View.VISIBLE);
		}
		
		return llItem;
	}
	
	protected View createEmptyView() {
		return new View(GlobalContext.get());
	}
	
	private void loadDrawableByUrl(final ImageView ivHead, String uri) {
		Bitmap bitmap = null;
		synchronized (mCachePost) {
			bitmap = mCachePost.get(uri);
		}

		if (bitmap != null) {
			UI2Manager.runOnUIThread(new Runnable1<Bitmap>(bitmap) {

				@Override
				public void run() {
					ivHead.setImageBitmap(mP1);
					ivHead.setVisibility(View.VISIBLE);
				}
			}, 0);
			return;
		}

		ImageLoaderImpl.getInstance().displayImage(uri,ivHead, new SimpleImageLoadingListener() {
			@Override
			public void onLoadingStarted(String imageUri, View view) {
				super.onLoadingStarted(imageUri, view);
				((ImageView) view).setImageDrawable(LayouUtil.getDrawable("def_moive"));
			}

			@Override
			public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
				super.onLoadingFailed(imageUri, view, failReason);
			}

			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				super.onLoadingComplete(imageUri, view, loadedImage);
				if (loadedImage != null) {
					((ImageView) view).setImageBitmap(loadedImage);
					view.setVisibility(View.VISIBLE);
					synchronized (mCachePost) {
						mCachePost.put(imageUri, loadedImage);
					}
				}
			}
		});
	}
	
	private Drawable getSoreMark(double score) {
		if (score < 1.0f) {
			return LayouUtil.getDrawable("dz_icon_star0");
		} else if (score < 2.0f) {
			return LayouUtil.getDrawable("dz_icon_star1");
		} else if (score < 3.0f) {
			return LayouUtil.getDrawable("dz_icon_star2");
		} else if (score < 4.0f) {
			return LayouUtil.getDrawable("dz_icon_star3");
		} else if (score < 5.0f) {
			return LayouUtil.getDrawable("dz_icon_star4");
		} else if (score < 6.0f) {
			return LayouUtil.getDrawable("dz_icon_star5");
		} else if (score < 7.0f) {
			return LayouUtil.getDrawable("dz_icon_star6");
		} else if (score < 8.0f) {
			return LayouUtil.getDrawable("dz_icon_star7");
		} else if (score < 9.0f) {
			return LayouUtil.getDrawable("dz_icon_star8");
		} else if (score < 10.0f) {
			return LayouUtil.getDrawable("dz_icon_star9");
		} else {
			return LayouUtil.getDrawable("dz_icon_star10");
		}
	}
	
}
