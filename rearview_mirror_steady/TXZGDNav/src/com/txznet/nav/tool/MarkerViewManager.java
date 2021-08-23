package com.txznet.nav.tool;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.nav.R;
import com.txznet.nav.ui.widget.CircleImageView;
import com.txznet.nav.ui.widget.MarkerView;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;

public class MarkerViewManager {

	private static MarkerViewManager instance = new MarkerViewManager();

	private MarkerViewManager() {

	}

	MarkerViewBuilderTool mBuilderTool = null;

	{
		mBuilderTool = new CircleMarkViewBuilder();
	}

	public static MarkerViewManager getInstance() {
		return instance;
	}

	public MarkViewItem createMarkerView(Bitmap bitmap, String uid, float degree) {
		if (mBuilderTool == null) {
			mBuilderTool = new CircleMarkViewBuilder();
		}

		Object[] params = new Object[3];
		params[0] = bitmap;
		params[1] = uid;
		params[2] = degree;
		return mBuilderTool.onBuildView(params);
	}

	public MarkViewItem decorateMarkViewItem(Bitmap bitmap, MarkViewItem mvi) {
		if (mvi == null || bitmap == null) {
			return mvi;
		}

		MarkerView mv = (MarkerView) mvi.getView();
		if (mv == null) {
			return mvi;
		}

		mv.setHeadImageBitmap(bitmap);
		return mvi;
	}

	public Bitmap convertBitmap(Bitmap bitmap, MarkViewItem mvi) {
		MarkViewItem mv = decorateMarkViewItem(bitmap, mvi);
		if (mv != null) {
			MarkerView v = (MarkerView) mv.getView();
			return genUserBitmap(mv, v.getRotate());
		}

		return null;
	}

	public Bitmap genUserBitmap(MarkViewItem mvi, float degree) {
		if (mvi == null) {
			return null;
		}

		if (mvi.mView == null) {
			return null;
		}

		MarkerView mkv = (MarkerView) mvi.mView;
		if (mkv == null) {
			return null;
		}

		if (mkv.getRotate() == degree) {
			return convertToBitmap(mvi);
		}

		mkv.setCarDirection(degree);
		return convertToBitmap(mvi);
	}

	public static interface MarkerViewBuilderTool {
		public MarkViewItem onBuildView(Object... params);
	}

	public static class MarkViewItem {
		private View mView;
		private Object mKey;
		private boolean mReBuild;

		public void setView(View view) {
			this.mView = view;
		}

		public View getView() {
			return this.mView;
		}

		public void setKey(Object key) {
			this.mKey = key;
		}

		public Object getKey() {
			return this.mKey;
		}

		public void setRebuild(boolean rebuild) {
			this.mReBuild = rebuild;
		}

		public boolean isRebuild() {
			return this.mReBuild;
		}
	}

	public static class CircleMarkViewBuilder implements MarkerViewBuilderTool {

		@Override
		public MarkViewItem onBuildView(Object... params) {
			Class clazz = null;
			if (params == null) {
				params = new Object[3];
				params[0] = R.drawable.default_headimage;
				params[1] = "";
				params[2] = 0.0f;
				clazz = Integer.class;
			} else {
				if (params[0] != null && params[0] instanceof Bitmap) {
					clazz = Bitmap.class;
				} else if (params[0] != null && params[0] instanceof Drawable) {
					clazz = Drawable.class;
				} else {
					clazz = Integer.class;
					params[0] = R.drawable.default_headimage;
				}
			}

			try {
				MarkerView mv = (MarkerView) LayoutInflater.from(
						AppLogic.getApp()).inflate(R.layout.marker_view,
						null);
				mv.setCarDirection((Float) params[2]);
				CircleImageView civ = (CircleImageView) LayoutInflater.from(
						AppLogic.getApp()).inflate(R.layout.civ_layout,
						null);
				if (clazz == Integer.class) {
					civ.setImageResource((Integer) params[0]);
				} else if (clazz == Bitmap.class) {
					civ.setImageBitmap((Bitmap) params[0]);
				} else if (clazz == Drawable.class) {
					civ.setImageDrawable((Drawable) params[0]);
				}

				Drawable drawable = getCacheDrawable(civ);
				int retry = 0;
				while (drawable == null && retry < 3) {
					drawable = getCacheDrawable(civ);
					retry++;
				}

				MarkViewItem mvi = new MarkViewItem();
				mvi.setKey(params[1]);
				if (clazz == Integer.class) { // 通过id取得图片为默认，需重新更换图片
					mvi.setRebuild(true);
				} else {
					mvi.setRebuild(false);
				}

				if (drawable == null) {
					mvi.setView(mv);
					return mvi;
				}

				mv.setTag(civ);
				mv.setHeadImageDrawable(drawable);
				mvi.setView(mv);
				return mvi;
			} catch (Exception e) {
				LogUtil.loge(e.toString());
			}
			return null;
		}
	}

	private static Drawable getCacheDrawable(View view) {
		view.setDrawingCacheEnabled(true);
		view.measure(MeasureSpec.makeMeasureSpec(80, MeasureSpec.EXACTLY),
				MeasureSpec.makeMeasureSpec(80, MeasureSpec.EXACTLY));
		view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
		view.buildDrawingCache();
		Bitmap bm = view.getDrawingCache();
		BitmapDrawable bd = new BitmapDrawable(AppLogic.getApp()
				.getResources(), bm);
		bd.setBounds(0, 0, bd.getIntrinsicWidth(), bd.getIntrinsicHeight());
		return bd;
	}

	private static Bitmap getCacheBitmap(View view) {
		view.setDrawingCacheEnabled(true);
		view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
		view.buildDrawingCache();
		Bitmap bm = view.getDrawingCache();
		return bm;
	}

	public static Bitmap convertToBitmap(MarkViewItem mvi) {
		if (mvi == null) {
			return null;
		}
		return getCacheBitmap(mvi.getView());
	}
}