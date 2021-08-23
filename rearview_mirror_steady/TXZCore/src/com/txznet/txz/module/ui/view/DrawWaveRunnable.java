package com.txznet.txz.module.ui.view;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Environment;
import android.os.SystemClock;

public class DrawWaveRunnable implements Runnable {

	public int mRecordDataCount;
	public byte[] mRecordBuff;
	private int recDataSize = 2 * 16000 * 60 * 3; // 3分钟的录音

	public int rateY = 1; // Y轴缩小的比例 默认为1
	public int rateX = 20;// 控制多少帧取一帧

	private int marginRight = 300;// 波形图绘制距离右边的距离
	private float divider = 0.1f;// 为了节约绘画时间，每0.1个像素画一个数据
	
	public DrawWaveRunnable() {
		mRecordDataCount = 0;
		mRecordBuff = new byte[recDataSize];	
		// 如果要兼顾左右声道的跳取帧 ，那么跳跃数就要能被4整除 ， 否则有可能取着右声道的插入了左声道的数据
		while ((rateX % 4) != 0) {
			++rateX;
		}
	}

	@Override
	public void run() {

	}

	class DrawWaveRunn implements Runnable {

		public boolean ifDraw;
		private int offset;

		public WaveSurfaceView mWSfv;
		public ArrayList<Short> mDrawBuff;
		private float drawMaxLength;
		private float[] pts = new float[4 * 1024 * 1024];
		long t1 , t2;
		long refreshTime = 120; // ms
		int lastPoi;

		public DrawWaveRunn(WaveSurfaceView wSfv , int offset) {
			this.mWSfv = wSfv;
			this.offset = offset;
			ifDraw = true;
			mDrawBuff = new ArrayList<Short>();
			drawMaxLength = (mWSfv.getWidth() - marginRight) / divider;
			t1 = t2 = lastPoi = 0;
		}

		@Override
		public void run() {
			while (ifDraw) {
				
				if (refreshTime > (t2 - t1)) {

					try {
						Thread.sleep(refreshTime - (t2 - t1));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				t1 = SystemClock.currentThreadTimeMillis();
				
				if(mRecordDataCount <= 0)
					continue;
				
				int z = (mRecordDataCount - lastPoi) / rateX;
				z = lastPoi + z * rateX;

				mDrawBuff.clear();
				for (int j = 0 , k = z - offset; k >= 0 && j < drawMaxLength; k -= rateX, j++) {
					mDrawBuff.add((short) ((0x0000 | mRecordBuff[k + 1]) << 8 | mRecordBuff[k]));
				}
				
				lastPoi = z;

				Canvas canvas = mWSfv.getHolder().lockCanvas(
						new Rect(0, 0, mWSfv.getWidth(), +mWSfv.getHeight())); // 关键:获取画布
																				// ，锁定画布
				canvas.drawARGB(255, 239, 239, 239);

				int start = (int) ((mDrawBuff.size()) * divider);
				if (mWSfv.getWidth() - start <= marginRight) { // 不让引导线超出右边范围
					start = mWSfv.getWidth() - marginRight;
				}

				float line_off4 = WaveSurfaceView.line_off / 4.00f;
				canvas.drawCircle(start, line_off4, line_off4,
						WaveSurfaceView.circlePaint);// 上圆
				canvas.drawCircle(start, mWSfv.getHeight() - line_off4,
						line_off4, WaveSurfaceView.circlePaint);// 下圆
				canvas.drawLine(start, 0, start, mWSfv.getHeight(),
						WaveSurfaceView.circlePaint);// 垂直的线

				int height = mWSfv.getHeight() - WaveSurfaceView.line_off;
				canvas.drawLine(0, WaveSurfaceView.line_off / 2,
						mWSfv.getWidth(), WaveSurfaceView.line_off / 2,
						WaveSurfaceView.paintLine);// 最上面的那根线
				canvas.drawLine(0,
						height * 0.5f + WaveSurfaceView.line_off / 2,
						mWSfv.getWidth(), height * 0.5f
								+ WaveSurfaceView.line_off / 2,
						WaveSurfaceView.center);// 中心线
				canvas.drawLine(0, mWSfv.getHeight() - WaveSurfaceView.line_off
						/ 2 - 1, mWSfv.getWidth(), mWSfv.getHeight()
						- WaveSurfaceView.line_off / 2 - 1,
						WaveSurfaceView.paintLine);// 最下面的那根线

				rateY = (65535 / mWSfv.getHeight());
				float x, y;
				
//				Log.e("Prisoner", "mDrawBuff.size = " + mDrawBuff.size());
				int j = 0;
				for (int i = 0; i < mDrawBuff.size(); i++ , j+=4) {
					y = mDrawBuff.get(mDrawBuff.size() - 1 - i) / rateY
							+ mWSfv.getHeight() / 2; // 调节缩小比例，调节基准线
					x = (i * divider);
					
					/*
					if (mWSfv.getWidth() - (i - 1) * divider <= marginRight) { // 不让波形线超出右边范围
						x = mWSfv.getWidth() - marginRight;
					}
					*/
					
					pts[j] = x;
					pts[j+1] = y;
					pts[j+2] = x;
					pts[j+3] = mWSfv.getHeight() - y;

					/*
					// 画线的方式很多，你可以根据自己要求去画。这里只是为了简单
					canvas.drawLine(x, y, x, mWSfv.getHeight() - y,
							WaveSurfaceView.mPaint);// 中间出波形
							*/
				}
				
				canvas.drawLines(pts, 0, j, WaveSurfaceView.mPaint);

				mWSfv.getHolder().unlockCanvasAndPost(canvas); // 解锁画布，提交画好的图像
				t2 = SystemClock.currentThreadTimeMillis();
			}

		}
	};

	public boolean ifRecording() {
		return false;
	}

	public void stop() {

	}

	public void savePCM(final String name) {

		new Thread(new Runnable() {

			@Override
			public void run() {

				if (mRecordDataCount <= 0)
					return;
				try {
					File d = new File(Environment.getExternalStorageDirectory()
							.getPath(), "txz/recordTest");
					d.mkdirs();
					String fileName = System.currentTimeMillis() + "_" + name;
					File fout = new File(d, fileName);
					OutputStream out = new FileOutputStream(fout);
					out.write(mRecordBuff, 0, mRecordDataCount);
					out.close();
					
					// 放大倍数 ， 
					/*
					short power = 2;
					Thread.sleep(300);
					short k;
					for (int i = 0; i < mRecordDataCount; i+=2) {
		        		 k = (short)(((0x0000 | mRecordBuff[i]) << 8) | mRecordBuff[i+1]); // 这里直接用大端的方式来组装数据 ，原因暂时未知
		        		 if(i == 0)
		        			 Log.e("Prisoner", "before " + Integer.toHexString(mRecordBuff[i] & 0xFF) + " " + Integer.toHexString(mRecordBuff[i+1] & 0xFF) + " " + k);
		        		 k = (short) (k * power);
		        		 mRecordBuff[i+1] = (byte) (k & 0x00ff);
		        		 mRecordBuff[i] = (byte) (k >> 8);
		        		 if(i == 0)
		        			 Log.e("Prisoner", "after " + Integer.toHexString(mRecordBuff[i] & 0xFF) + " " + Integer.toHexString(mRecordBuff[i+1] & 0xFF) + " " + k);
					}
					
					File fout1 = new File(d, power + "_"  + fileName);
					OutputStream out1 = new FileOutputStream(fout1);
					out1.write(mRecordBuff, 0, mRecordDataCount);
					out1.close();
					*/
					
					
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}).start();

	}

}
