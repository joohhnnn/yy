package com.txznet.comm.ui.theme.test.view;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IFloatView;

import java.io.File;

public class FloatView extends IFloatView {

	private static FloatView sInstance = new FloatView();
	private ImageView igView;

	private FloatView() {
	}

	public static FloatView getInstance() {
		return sInstance;
	}

	@Override
	public void release() {
		super.release();
	}
	
	@Override
	public ViewAdapter getView(ViewData data) {
		igView = new ImageView(GlobalContext.get());
		igView.setImageDrawable(LayouUtil.getDrawable("person_float"));
		ViewAdapter viewAdapter = new ViewAdapter();
		viewAdapter.view = igView;
		viewAdapter.object = FloatView.getInstance();
		return viewAdapter;
	}
	
	@Override
	public void init() {
//		igView = new ImageView(GlobalContext.get());
//		igView.setImageDrawable(LayouUtil.getDrawable("mic"));
	}

	@Override
	public void updateState(int state) {
		//LogUtil.logd("updateState " + state);
//		if (textView != null) {
//			textView.setText("state:" + state);
//		}
	}

	@Override
	public void updateVolume(int volume) {
	}

	@Override
	public void setImageBitmap(final String normal, final String pressed) {
		if(igView != null){
			igView.getViewTreeObserver().addOnPreDrawListener(
					new ViewTreeObserver.OnPreDrawListener() {
						@Override
						public boolean onPreDraw() {
							igView.getViewTreeObserver().removeOnPreDrawListener(this);
							try {
								if (normal == null || pressed == null) {
									igView.setImageDrawable(LayouUtil.getDrawable("person_float"));
									return false;
								}
								File fNormal = new File(normal);
								File fPressed = new File(pressed);
								if (!fNormal.exists() || fNormal.length() == 0) {
									igView.setImageDrawable(LayouUtil.getDrawable("person_float"));
									LogUtil
											.loge("[UI3.0]SDKFloatView setCustomIcon failed, fNormal not found");
									return false;
								}
								BitmapFactory.Options options = new BitmapFactory.Options();
								options.outWidth = 100;
								options.outHeight = 100;
								if (fPressed != null && fPressed.length() != 0) {
									StateListDrawable drawable = new StateListDrawable();
									int sPressed = android.R.attr.state_pressed;
									Bitmap bPressed = decodeSampledBitmapFromResource(
													pressed,
													igView.getWidth(),
													igView.getHeight());
									Drawable dPressed = new BitmapDrawable(bPressed);
									drawable.addState(new int[] { sPressed },
											dPressed);
									Bitmap bNormal = decodeSampledBitmapFromResource(
													normal,
													igView.getWidth(),
													igView.getHeight());
									Drawable dNormal = new BitmapDrawable(bNormal);
									drawable.addState(new int[] {}, dNormal);
									igView.setImageDrawable(drawable);
								} else {
									Bitmap bNormal = decodeSampledBitmapFromResource(
													normal,
													igView.getWidth(),
													igView.getHeight());
									Drawable dNormal = new BitmapDrawable(bNormal);
									igView.setImageDrawable(dNormal);
									LogUtil
											.loge("[UI3.0]SDKFloatView setCustomIcon fPressed not found");
								}
							} catch (Exception e) {
								LogUtil
										.loge("[UI3.0]SDKFloatView setCustomIcon failed, cause "
												+ e.getClass()
												+ "::"
												+ e.getMessage());
							}
							return false;
						}
					});
			igView.invalidate();
		}
	}



	public static Bitmap decodeSampledBitmapFromResource(String path, int reqWidth, int reqHeight) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(path, options);
	}

	private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		if (height > reqHeight || width > reqWidth) {
			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}
		return inSampleSize;
	}

}
