package com.txznet.marketing;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.txznet.marketing.ui.MediaPlayerSurfaceView;

public class TipDialog extends Dialog {

	private static TipDialog mInstance = null;

	//dialog显示状态
	private boolean state = false;

	public static TipDialog getInstance(Context ctx) {
		if (mInstance == null) {
			synchronized (TipDialog.class) {
				if (mInstance == null) {
					mInstance = new TipDialog(ctx);
				}
			}
		}
		return mInstance;
	}

	protected View mView;
	LinearLayout linearLayout;
	LinearLayout linearLayout2;
	ImageView imageSay;
	TextView textView;
	TextView rePlayView;
	ImageView imageReplay;
	ImageView imageView;

	@SuppressLint("NewApi")
	private TipDialog(Context ctx) {
		super(ctx, R.style.TXZ_Dialog_Style_Full);

		mView = LayoutInflater.from(ctx).inflate(R.layout.tip_dialog_layout,
				null);
		linearLayout = (LinearLayout)mView.findViewById(R.id.linearLayout);
		linearLayout2 = (LinearLayout) mView.findViewById(R.id.linearLayout2);
		imageSay = (ImageView) mView.findViewById(R.id.imageSay);
		textView = (TextView) mView.findViewById(R.id.textView);
		rePlayView = (TextView) mView.findViewById(R.id.replay);
		imageReplay = (ImageView) mView.findViewById(R.id.imageReplay);

		imageView = (ImageView) mView.findViewById(R.id.imageView);

		//添加重播的点击事件
		rePlayView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	dismiss();
            	MediaPlayerSurfaceView.getInstance().rePlay();
            }
        });
		imageReplay.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				dismiss();
				MediaPlayerSurfaceView.getInstance().rePlay();
			}
		});


		initWinRecord();
		setContentView(mView);

		mView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
				| View.SYSTEM_UI_FLAG_FULLSCREEN);
		getWindow().setLayout(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);

		getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg);

	}

	private void initWinRecord() {

		getWindow().setWindowAnimations(R.style.SlideDialogAnimation);
		getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

		getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING
						| WindowManager.LayoutParams.FLAG_LOCAL_FOCUS_MODE
						| WindowManager.LayoutParams.FLAG_FULLSCREEN
						| WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN
						| WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER
						| WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

	}

	public TipDialog setText(String text) {
		textView.setText(text);
		//textView.setTextColor(Color.parseColor("#BEBEBE"));
		textView.setTextColor(Color.parseColor("#FFFFFF"));
		imageSay.setImageResource(R.drawable.say1);

		linearLayout2.setVisibility(View.GONE);
		imageView.setVisibility(View.GONE);
		linearLayout.setVisibility(View.VISIBLE);
		return mInstance;
	}

	public void setColor(String colorString){
		imageSay.setImageResource(R.drawable.say2);
		textView.setTextColor(Color.parseColor(colorString));
	}

	public void showImageView(){
		linearLayout.setVisibility(View.GONE);
		linearLayout2.setVisibility(View.GONE);
		imageView.setVisibility(View.VISIBLE);

		AnimationDrawable ad =(AnimationDrawable) imageView.getDrawable();
		ad.start();
	}

	public TipDialog showButton(){
		linearLayout.setVisibility(View.GONE);
		imageView.setVisibility(View.GONE);
		linearLayout2.setVisibility(View.VISIBLE);

		return mInstance;
	}

	//获取TipDialog的显示状态
	public boolean getVisibility(){
		return state;
	}

	@Override
	public void show() {
		state = true;
		super.show();

	}

	@Override
	public void dismiss() {
		state = false;
		super.dismiss();

		if (MainActivity.getInstance().isRunningFont){
			MediaPlayerSurfaceView.getInstance().skipCommandPoint();
		}
	}

}