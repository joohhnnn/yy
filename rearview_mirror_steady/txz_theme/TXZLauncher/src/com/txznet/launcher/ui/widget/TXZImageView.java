package com.txznet.launcher.ui.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class TXZImageView extends ImageView {

	private boolean bPressed = false;

	public TXZImageView(Context context) {
		super(context);

	}

	public TXZImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public TXZImageView(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
	}
    
	public void setPressState(boolean state){
		bPressed = state;
		invalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Bitmap bitmap = this.getDrawable() == null ? null
				: drawableToBitmap(getDrawable());

		if (bitmap == null) {
			return;
		}
        
		if (bPressed) {
		    //被按下，则绘制半透明阴影图层
			drawLayer(canvas, bitmap);
		}
	}

	private void drawLayer(Canvas canvas, Bitmap bitmap) {
		float scaleX = 0;
		float scaleY = 0;
		
		scaleX = this.getWidth() * 1.0f / bitmap.getWidth();
		scaleY = this.getHeight() * 1.0f / bitmap.getHeight();
        
		float scale = 0;
		
		scale = scaleX < scaleY ? scaleY :scaleY;//该ImageView的ScaleType已经固定设置为Fit_Center
		                                                            //即表示ImageView中的图片居中缩放到最短的长度。
		Matrix matrix = new Matrix();
		matrix.setScale(scale, scale);
		Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
				bitmap.getHeight(), matrix, true);

		float x = 0;
		float y = 0;
		Paint paint = new Paint();
		BlurMaskFilter bf = new BlurMaskFilter(1, BlurMaskFilter.Blur.INNER);
		paint.setColor(0x77000000);
		paint.setMaskFilter(bf);
		
		x = this.getWidth() - newBitmap.getWidth();
		x = x / 2;

		y = this.getHeight() - newBitmap.getHeight();
		y = y / 2;
		
		canvas.drawBitmap(newBitmap.extractAlpha(paint, null),  x,  y,  paint);
	}
	
	public static Bitmap drawableToBitmap(Drawable drawable) {
		int width = drawable.getIntrinsicWidth();
		int height = drawable.getIntrinsicHeight();

		Bitmap bitmap = Bitmap.createBitmap(width, height, drawable
				.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
				: Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);

		drawable.setBounds(0, 0, width, height);
		drawable.draw(canvas);
		return bitmap;
	}

}
