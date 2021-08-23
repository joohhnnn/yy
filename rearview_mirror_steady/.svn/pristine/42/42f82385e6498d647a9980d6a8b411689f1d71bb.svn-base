package com.txznet.txz.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.txz.R;

public class TitleBar extends RelativeLayout {

	private ImageButton btLeft;
	private TextView tvRight;
	private TextView title;
	private ImageView imgRight;

	public TitleBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TitleBar(Context context) {
		this(context, null, 0);
	}

	public TitleBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}

	private void init(Context context, AttributeSet attrs, int defStyle) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.widget_title_bar, this, true);
		tvRight = (TextView) view.findViewById(R.id.rightText);
		imgRight = (ImageView) view.findViewById(R.id.rightImg);
		btLeft = (ImageButton) view.findViewById(R.id.leftIcon);
		this.title = (TextView) view.findViewById(R.id.titleText);
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.TitleBar);
		int rightIconId = a.getResourceId(R.styleable.TitleBar_rightImg, -1);
		int iconId = a.getResourceId(R.styleable.TitleBar_leftIcon, -1);
		String rightText = a.getString(R.styleable.TitleBar_rightText);
		String title = a.getString(R.styleable.TitleBar_title);
		if (iconId != -1)
			btLeft.setImageResource(iconId);
		else
			btLeft.setVisibility(View.GONE);
		if (rightIconId != -1)
			imgRight.setImageResource(rightIconId);
		else
			imgRight.setVisibility(View.GONE);
		if (!TextUtils.isEmpty(rightText)) {
			tvRight.setText(rightText);
		} else {
			tvRight.setText("");
		}
		if (!TextUtils.isEmpty(title)) {
			this.title.setText(title);
		}
		a.recycle();
	}

	public void setTitleText(String text) {
		title.setText(text);
	}

	public void setLeftIconClickListener(View.OnClickListener listener) {
		btLeft.setOnClickListener(listener);
	}

	public void setRightTextClickListener(View.OnClickListener listener) {
		tvRight.setOnClickListener(listener);
	}

	public void setRightText(String text) {
		tvRight.setText(text);
	}

	public void setRightTextColor(int color) {
		tvRight.setTextColor(color);
	}

	public void setRightImgOnClickListener(View.OnClickListener listener) {
		imgRight.setOnClickListener(listener);
	}

}
