package com.txznet.txz.module.news;

import android.app.Service;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.txz.R;
import com.txznet.txz.util.TXZFileConfigUtil;

public class NewsTipsView extends LinearLayout{
	
	public static class NewsMsg{
		public String tipTag;
		public String tipText;
	}
	
	public static class NewsViewHolder{
		public TextView tvTipTag;
		public TextView tvTipText1;
		public TextView tvseparator1;
		public TextView tvTipText2;
		public ImageView imLogo;
		public ImageView imClose;
	}
	public static interface INewsWinEvent{
				public void onShow();
				public void onDismiss();
	}
	
	private View mLayout;
	protected WindowManager.LayoutParams mLp;
	protected WindowManager mWinManager;
	protected int mWidth;
	protected int mHeight;
	private NewsViewHolder mViewHolder;
	private boolean mIsOpening = false;
	private INewsWinEvent mWinEvent;
	
	private static NewsTipsView sInstance;
	
	private class ConfigValue{
		public int width = -1;
		public int height = -1;
		public int textSize = -1;
		public int x = -1;
		public int y = -1;
	}
	
	private ConfigValue mConfigValue = null;
	private void initConfig(){
		mConfigValue = new ConfigValue();
		mConfigValue.width = TXZFileConfigUtil.getIntSingleConfig("news.view.width",  -1);
		mConfigValue.height = TXZFileConfigUtil.getIntSingleConfig("news.view.height",  -1);
		mConfigValue.textSize = TXZFileConfigUtil.getIntSingleConfig("news.view.text.size",  -1);
		mConfigValue.x = TXZFileConfigUtil.getIntSingleConfig("news.view.x",  -1);
		mConfigValue.y = TXZFileConfigUtil.getIntSingleConfig("news.view.y",  -1);
	}
	
	private NewsTipsView(Context context) {
		super(context);
		initConfig();
		mViewHolder = new NewsViewHolder();
		mWinManager = (WindowManager) getContext().getSystemService(Service.WINDOW_SERVICE);
		mLayout = getLayoutView();
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		//params.leftMargin = (int) getResources().getDimension(R.dimen.x150);
		//params.rightMargin = (int) getResources().getDimension(R.dimen.x150);
		addView(mLayout, params);
	}
    
	private static NewsMsg getNewsMsg(String strJson){
		JSONBuilder builder = new JSONBuilder(strJson);
		NewsMsg msg = new NewsMsg();
		msg.tipTag = builder.getVal("tipTag", String.class, "");
		msg.tipText = builder.getVal("tipText", String.class, "");
		return msg;
	}
	
	public static void showView(String strJson, INewsWinEvent eventHandler){
		if (sInstance == null) {
			synchronized (NewsTipsView.class) {
				if (sInstance == null) {
				sInstance = new NewsTipsView(GlobalContext.get());
				}
			}
		}
		NewsMsg msg = getNewsMsg(strJson);
		sInstance.open(eventHandler, msg);
	}
	
	public static void dismissView(){
		do{
			if (sInstance == null){
				break;
			}
			if (!sInstance.mIsOpening){
				break;
			}
			sInstance.close();
		}while(false);
	}

	private View getLayoutView() {
		NewsViewHolder v = mViewHolder;
		
		View convertView = LayoutInflater.from(getContext()).inflate(R.layout.news_tip_view, null);
		LinearLayout label = (LinearLayout) convertView.findViewById(R.id.ll_news_lable);
		v.tvTipTag = (TextView) convertView.findViewById(R.id.tv_news_tag);
		v.tvTipText1 = (TextView) convertView.findViewById(R.id.tv_news_text1);
		v.tvseparator1 = (TextView) convertView.findViewById(R.id.tv_news_separator1);
		v.tvTipText2 = (TextView) convertView.findViewById(R.id.tv_news_text2);
		
		int tagColor =  0xaadddddd;
		float tagSize = LayouUtil.getDimen("m20");
		int textColor =  Color.WHITE;
		float textSize = LayouUtil.getDimen("m20");
		
		if (mConfigValue.textSize > 0){
			textSize = tagSize = mConfigValue.textSize;
		}
		
		v.tvTipTag.setTextSize(tagSize);
		v.tvTipTag.setTextColor(tagColor);
		v.tvTipText1.setTextSize(textSize);
		v.tvTipText1.setTextColor(textColor);
		v.tvseparator1.setTextSize(tagSize);
		v.tvseparator1.setTextColor(tagColor);
		v.tvTipText2.setTextSize(textSize);
		v.tvTipText2.setTextColor(textColor);
		
		v.imClose = (ImageView) convertView.findViewById(R.id.iv_news_exit);
		v.imClose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		return convertView;
	}
	
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				getViewTreeObserver().removeOnPreDrawListener(this);
				mWidth = mLayout.getLayoutParams().width;
				mHeight = mLayout.getLayoutParams().height;
				
				if (mConfigValue.width > 0){
					mWidth = mConfigValue.width;
				}
				if (mConfigValue.height > 0){
					mHeight = mConfigValue.height;
				}
				
				mLp.width = mWidth;
				mLp.height = mHeight;
				mWinManager.updateViewLayout(NewsTipsView.this, mLp);
				return false;
			}
		});
	}
	
	
	public void open(INewsWinEvent eventHandler, NewsMsg msg){
		if(mIsOpening){
			return;
		}
		update(msg);
		
		mIsOpening = true;
		mLp = new WindowManager.LayoutParams();
		mLp.type = WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW + 10;
		mLp.width = WindowManager.LayoutParams.WRAP_CONTENT;
		mLp.height = WindowManager.LayoutParams.WRAP_CONTENT;
		mLp.flags = 40;
		mLp.format = PixelFormat.RGBA_8888;
		mLp.gravity = Gravity.TOP;
		
		if (mConfigValue.x >= 0){
			mLp.x = mConfigValue.x;
			mLp.gravity = mLp.gravity | Gravity.START;
		}else{
			mLp.gravity = mLp.gravity | Gravity.CENTER_HORIZONTAL;
		}
		
		if (mConfigValue.y >= 0){
			mLp.y = mConfigValue.y;
		}
		
		mWinManager.addView(this, mLp);
		mWinEvent = eventHandler;
	
		INewsWinEvent handler = mWinEvent;
		if (handler != null){
			handler.onShow();
		}
	}
	
	private void update(NewsMsg msg){
		NewsViewHolder v = mViewHolder;
		v.tvTipTag.setText(msg.tipTag);
		String[] tipWords = null;
		if (!TextUtils.isEmpty(msg.tipText)){
			tipWords = msg.tipText.split(";");
		}
		do{
			if (tipWords == null){
				break;
			}
			if (tipWords.length <= 0){
				break;
			}
			
			v.tvTipText1.setText(tipWords[0]);
			if (tipWords.length < 2){
				break;
			}
			v.tvseparator1.setText("或");
			v.tvTipText2.setText(tipWords[1]);
		
		}while(false);	
	}
	
	public boolean isShowing() {
		return mIsOpening;
	}

	public void close() {
		mWinEvent = null;//主动close不会有回调
		dismiss();
	}
	
	private void dismiss(){		
		post(new Runnable() {
			@Override
			public void run() {
				if (mIsOpening) {
					mWinManager.removeView(NewsTipsView.this);
					mIsOpening = false;
					
					INewsWinEvent handler = mWinEvent;
					if (handler != null){
						handler.onDismiss();
					}
					
				}
			}
		});
	}
	
}
