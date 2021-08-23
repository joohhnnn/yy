package com.txznet.music.widget;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.SeekBar;

import com.txznet.audio.bean.LocalBuffer;
import com.txznet.music.R;
import com.txznet.music.utils.CollectionUtils;

public class CustomSeekBar extends SeekBar {
	private Bitmap baseBitmap;
	private Bitmap bmp;
	private Canvas pCanvas;
	private List<LocalBuffer> buffers;

	public final static int PAINT_HEIGHT = 6;
	public static int PAINT_LEFT_PADDING = 10;
	public static int BITMAP_LEFT_PADDING = 10;
	public static int PAINT_RIGHT_PADDING = PAINT_LEFT_PADDING;

	public final static int COLOR_BACKGROUND = 0xFF393939;
	public final static int COLOR_PROGRESS = 0xFF1cc859;
	public final static int COLOR_BUFFER = 0xFF5d5a5a;

	public CustomSeekBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public CustomSeekBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CustomSeekBar(Context context) {
		this(context, null);
	}

	public void setPadding(int padding) {
		PAINT_LEFT_PADDING = PAINT_RIGHT_PADDING = padding;
	}

	/**
	 * 初始化
	 */
	private void init(Context context) {
		buffers = new ArrayList<LocalBuffer>();
		baseBitmap = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.fm_player_drag);

		bmp = Bitmap.createBitmap(baseBitmap.getWidth(),
				baseBitmap.getHeight(), baseBitmap.getConfig());
		BITMAP_LEFT_PADDING=PAINT_LEFT_PADDING = PAINT_RIGHT_PADDING = (bmp.getWidth() + 1) / 2;

		initbm();
	}

	private void initbm() {
		Paint paint = new Paint();
		pCanvas = new Canvas(bmp);
		pCanvas.drawBitmap(baseBitmap, new Matrix(), paint);
	}

	public synchronized void setBufferRange(List<LocalBuffer> buffers) {
		this.buffers.clear();
		if (buffers != null) {
			this.buffers.addAll(buffers);
		}
		postInvalidate();
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		int PAINT_WIDTH = getWidth()
				- (PAINT_LEFT_PADDING + PAINT_RIGHT_PADDING);
		// 底色
		Paint paint = new Paint();
		paint.setColor(COLOR_BACKGROUND);
		Rect r = new Rect(PAINT_LEFT_PADDING, getHeight() / 2 - PAINT_HEIGHT
				/ 2, getWidth() - PAINT_RIGHT_PADDING, getHeight() / 2
				+ PAINT_HEIGHT / 2);
		canvas.drawRect(r, paint);
		// ///////////////////////////////////////////////////////////////
		// 缓冲条
		if (CollectionUtils.isNotEmpty(buffers)) {
			Paint paint3 = new Paint();
			for (LocalBuffer buffer : buffers) {
				if (buffer == null) {
					break;
				}
				Rect r3 = new Rect(PAINT_LEFT_PADDING
						+ (int) (buffer.getFromP() * PAINT_WIDTH), getHeight()
						/ 2 - PAINT_HEIGHT / 2, PAINT_LEFT_PADDING
						+ (int) (buffer.getToP() * PAINT_WIDTH), getHeight()
						/ 2 + PAINT_HEIGHT / 2);
				paint3.setColor(COLOR_BUFFER);
				canvas.drawRect(r3, paint3);
			}
		}
		// 进度条
		Paint paint2 = new Paint();
		Rect r2 = new Rect(PAINT_LEFT_PADDING, getHeight() / 2 - PAINT_HEIGHT
				/ 2, PAINT_LEFT_PADDING
				+ (getProgress() * PAINT_WIDTH / getMax()), getHeight() / 2
				+ PAINT_HEIGHT / 2);
		paint2.setColor(COLOR_PROGRESS);
		canvas.drawRect(r2, paint2);
		//
		canvas.drawBitmap(bmp, BITMAP_LEFT_PADDING + getProgress() * PAINT_WIDTH
				/ getMax() - bmp.getWidth() / 2,
				getHeight() / 2 - bmp.getHeight() / 2, paint);
	}
}