package com.txznet.txz.ui.widget.mov;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.LruCache;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.view.ScaleImageView;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.comm.util.imageloader.ImageLoaderInitialize.ImageLoaderImpl;
import com.txznet.loader.AppLogic;
import com.txznet.txz.R;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.util.runnables.Runnable1;

import java.util.ArrayList;
import java.util.List;

public class FilmLayout extends LinearLayout {
	public static final int DEFAULT_CINEMA_CACHEPOST = 8;

	private int mVisibleCount;
	private LruCache<String, Bitmap> mCachePost = null;
	private List<FilmItemView> mCacheBillViews = null;

	public static class FilmBean {
		public String answer;
		public String title;
		public String postUrl;
		public double score;
		public List<String> leadingRole = new ArrayList<String>();
		public List<String> types = new ArrayList<String>();
		public List<String> alias = new ArrayList<String>();
	}

	public FilmLayout(Context context) {
		this(context, null);
	}

	public FilmLayout(Context context, AttributeSet attr) {
		this(context, attr, 0);
	}

	public FilmLayout(Context context, AttributeSet attr, int defValue) {
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
	
	
	public void setCineList(List<FilmBean> cbs) {
		removeAllViewsInLayout();
		if (cbs != null) {
			final int totalCount = cbs.size();
			for (int i = 0; i < cbs.size(); i++) {
				final FilmBean bean = cbs.get(i);
				final FilmItemView child = createFilmLayout(i);
				child.setPadding(2, 2, 2, 2);

				if (child != null) {
					FilmItemView.ViewHolder viewHolder = (FilmItemView.ViewHolder) child.getTag(R.string.key_cinema_holder);
					if (viewHolder != null) {
						viewHolder.setTitle(bean.title);
						viewHolder.setScore(bean.score);
						loadDrawableByUrl(viewHolder, bean.postUrl);
					}
					child.setBackgroundDrawable(LayouUtil.getDrawable("movie_rang_bg"));
					mFocusViews.add(child);
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

	protected FilmItemView createFilmLayout(final int pos) {
		if (mCacheBillViews == null) {
			mCacheBillViews = new ArrayList<FilmItemView>();
		}
		if (mCacheBillViews.size() <= pos) {
			FilmItemView cbv = new FilmItemView(getContext());
			cbv.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					JSONBuilder jb = new JSONBuilder();
					jb.put("index", pos);
					ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.item.selected",
							jb.toBytes(), null);
				}
			});
			mCacheBillViews.add(cbv);
			return cbv;
		}

		FilmItemView cbv = mCacheBillViews.get(pos);
		FilmItemView.ViewHolder holder = cbv.mHolder;
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

	private void addChildView(View view, LayoutParams lp) {
		view.setLayoutParams(lp);
		addView(view);
	}

	private void loadDrawableByUrl(final FilmItemView.ViewHolder holder, String uri) {
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