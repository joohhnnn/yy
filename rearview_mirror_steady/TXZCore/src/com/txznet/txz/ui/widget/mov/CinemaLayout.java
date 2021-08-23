package com.txznet.txz.ui.widget.mov;

import java.util.ArrayList;
import java.util.List;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.view.ScaleImageView;
import com.txznet.comm.util.imageloader.ImageLoaderInitialize.ImageLoaderImpl;
import com.txznet.loader.AppLogic;
import com.txznet.txz.R;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.ui.widget.mov.CinemaBillView.ViewHolder;
import com.txznet.txz.util.runnables.Runnable1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.LruCache;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class CinemaLayout extends LinearLayout {
	public static final int DEFAULT_CINEMA_CACHEPOST = 8;

	private int mVisibleCount;
	private LruCache<String, Bitmap> mCachePost = null;
	private List<CinemaBillView> mCacheBillViews = null;

	public static class CinemaBean {
		public String title;
		public String post;
		public double score;
	}

	public CinemaLayout(Context context) {
		this(context, null);
	}

	public CinemaLayout(Context context, AttributeSet attr) {
		this(context, attr, 0);
	}

	public CinemaLayout(Context context, AttributeSet attr, int defValue) {
		super(context, attr, defValue);

		init();
	}

	private void init() {
		// 横向布局
		setOrientation(HORIZONTAL);
	}

	public void release() {
		mFocusViews.clear();
	}
	
	public void setVisibleCount(int visibleCount) {
		JNIHelper.logd("setVisibleCount:" + visibleCount);
		this.mVisibleCount = visibleCount;
	}

	private List<View> mFocusViews = new ArrayList<View>();
	
	public List<View> getFocusViews() {
		return mFocusViews;
	}
	
	
	public void setCineList(List<CinemaBean> cbs) {
		removeAllViewsInLayout();
		if (cbs != null) {
			final int totalCount = cbs.size();
			for (int i = 0; i < cbs.size(); i++) {
				final CinemaBean bean = cbs.get(i);
				final CinemaBillView child = createCinemaLayout(i);
				child.setPadding(4, 4, 4, 4);

				if (child != null) {
					ViewHolder viewHolder = (ViewHolder) child.getTag(R.string.key_cinema_holder);
					if (viewHolder != null) {
						viewHolder.setTitle(bean.title);
						viewHolder.setScore(bean.score);
						loadDrawableByUrl(viewHolder, bean.post);
					}
					child.setBackgroundDrawable(LayouUtil.getDrawable("white_range_layout"));
					mFocusViews.add(child);
					// child.setOnFocusChangeListener(new
					// OnFocusChangeListener() {
					// @Override
					// public void onFocusChange(View v, boolean hasFocus) {
					// LinearLayout itemView = (LinearLayout) v;
					// if (hasFocus) {
					// itemView.setBackgroundColor(Color.parseColor("#4AA5FA"));
					// } else {
					// itemView.setBackgroundDrawable(LayouUtil.getDrawable("white_range_layout"));
					// }
					// }
					// });
				}
				addChildView(child, i);
			}

			int emptyCount = mVisibleCount - totalCount;
			if (emptyCount > 0) {
				for (int i = 0; i < emptyCount; i++) {
					addChildView(createEmptyView(), totalCount + i);
				}
			}
		}

		requestLayout();
	}

	protected CinemaBillView createCinemaLayout(int pos) {
		if (mCacheBillViews == null) {
			mCacheBillViews = new ArrayList<CinemaBillView>();
		}
		if (mCacheBillViews.size() <= pos) {
			CinemaBillView cbv = new CinemaBillView(getContext());
			mCacheBillViews.add(cbv);
			return cbv;
		}

		CinemaBillView cbv = mCacheBillViews.get(pos);
		ViewHolder holder = cbv.mHolder;
		if (holder != null) {
			holder.clear();
		}
		return cbv;
	}
	
	public void clear() {
		mCachePost = null;
		mCacheBillViews = null;
	}

	protected View createEmptyView() {
		ScaleImageView view = new ScaleImageView(getContext());
		view.setMode(ScaleImageView.MODE_AUTO_HEIGHT);
		view.setScale(1.5f);
		return view;
	}

	private void addChildView(View view, int index) {
		LayoutParams lp = new LayoutParams(0, LayoutParams.MATCH_PARENT, 1);
		int dimen6 = (int) getContext().getResources().getDimension(R.dimen.y6);
		if (index == 0) {
			lp.setMargins(0, 0, dimen6, 0);
		} else if (index == mVisibleCount - 1) {
			lp.setMargins(dimen6, 0, 0, 0);
		} else {
			lp.setMargins(dimen6, 0, dimen6, 0);
		}
		view.setLayoutParams(lp);
		addView(view);
	}

	private void loadDrawableByUrl(final ViewHolder holder, String uri) {
		Bitmap bitmap = null;
		if (mCachePost != null) {
			synchronized (mCachePost) {
				bitmap = mCachePost.get(uri);
			}
		}

		if (bitmap != null) {
			AppLogic.runOnUiGround(new Runnable1<Bitmap>(bitmap) {

				@Override
				public void run() {
					holder.mCineBillIv.setImageBitmap(mP1);
					holder.mCineBillIv.setVisibility(View.VISIBLE);
				}
			}, 0);
			return;
		}

		ImageLoaderImpl.getInstance().displayImage(uri, holder.mCineBillIv, new SimpleImageLoadingListener() {
			@Override
			public void onLoadingStarted(String imageUri, View view) {
				super.onLoadingStarted(imageUri, view);
				JNIHelper.logw("ImageLoader onLoadingStarted:" + imageUri);
			}

			@Override
			public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
				super.onLoadingFailed(imageUri, view, failReason);
				JNIHelper.logw("ImageLoader onLoadingFailed:" + imageUri);
			}

			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				super.onLoadingComplete(imageUri, view, loadedImage);
				JNIHelper.logw("ImageLoader onLoadingComplete:" + imageUri);
				if (loadedImage != null) {
					((ImageView) view).setImageBitmap(loadedImage);
					view.setVisibility(View.VISIBLE);
					if (mCachePost == null) {
						mCachePost = new LruCache<String, Bitmap>(DEFAULT_CINEMA_CACHEPOST);
					}
					synchronized (mCachePost) {
						mCachePost.put(imageUri, loadedImage);
					}
				}
			}
		});
	}
}