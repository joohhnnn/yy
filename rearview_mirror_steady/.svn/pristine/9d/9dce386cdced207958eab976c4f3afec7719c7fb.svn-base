package com.txznet.webchat.ui.car.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.txznet.webchat.R;

/**
 * 简单按钮
 * 支持设置不同状态下的drawable
 * Created by J on 16/10/15.
 */

public class ResourceButton extends LinearLayout {

    // attrs
    private int mIconSize;
    private int mTextSize;
    private int mPaddingVertical;
    private int mPaddingHorizontal;
    private int mPaddingIcon;

    private Drawable mIconNormal;
    private Drawable mIconPressed;
    private boolean bIconVisible;

    private int mColorTextNormal;
    private int mColorTextPressed;

    private String mStrText;

    // views
    private ImageView mIvIcon;
    private TextView mTvText;

    private boolean bPressed;

    private OnBtnClickListener mOnClickListener;

    public void setOnBtnClickListener(OnBtnClickListener listener) {
        mOnClickListener = listener;
    }


    public ResourceButton(Context context) {
        this(context, null);
    }

    public ResourceButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ResourceButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        initAttrs(attrs);
        initViews();
        initListeners();
    }

    public void setText(String text) {
        mStrText = text;
        mTvText.setText(text);
    }

    public void setIconNormal(Drawable icon) {
        mIconNormal = icon;

        if (!bPressed && bIconVisible) {
            mIvIcon.setImageDrawable(icon);
        }
    }

    public void setIconPressed(Drawable icon) {
        mIconPressed = icon;

        if (bPressed && bIconVisible) {
            mIvIcon.setImageDrawable(icon);
        }
    }

    private void initAttrs(AttributeSet attrs) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.ResourceButton);

        mIconSize = ta.getDimensionPixelSize(R.styleable.ResourceButton_resbtn_size_icon, getResources().getDimensionPixelSize(R.dimen.y32));
        mTextSize = ta.getDimensionPixelSize(R.styleable.ResourceButton_resbtn_size_text, getResources().getDimensionPixelSize(R.dimen.y24));
        mPaddingVertical = ta.getDimensionPixelSize(R.styleable.ResourceButton_resbtn_padding_vertical, 0);
        mPaddingHorizontal = ta.getDimensionPixelSize(R.styleable.ResourceButton_resbtn_padding_horizontal, 0);
        mPaddingIcon = ta.getDimensionPixelSize(R.styleable.ResourceButton_resbtn_padding_icon, getResources().getDimensionPixelSize(R.dimen.x16));
        mIconNormal = getResources().getDrawable(ta.getResourceId(R.styleable.ResourceButton_resbtn_src_icon_normal, R.drawable.transparent_background));
        mIconPressed = getResources().getDrawable(ta.getResourceId(R.styleable.ResourceButton_resbtn_src_icon_pressed, R.drawable.transparent_background));
        mColorTextNormal = getResources().getColor(ta.getResourceId(R.styleable.ResourceButton_resbtn_color_text_normal, R.color.color_text_primary));
        mColorTextPressed = getResources().getColor(ta.getResourceId(R.styleable.ResourceButton_resbtn_color_text_pressed, R.color.color_text_accent));
        mStrText = ta.getString(R.styleable.ResourceButton_resbtn_str_text);
        bIconVisible = ta.getBoolean(R.styleable.ResourceButton_resbtn_boolean_icon_visible, true);

        /*if (null == mIconNormal) {
            mIconNormal = getResources().getDrawable(R.drawable.src_car_main_setting);
        }

        if (null == mIconPressed) {
            mIconPressed = getResources().getDrawable(R.drawable.src_car_main_setting_pressed);
        }*/

        // 两种状态下的icon都未设置的情况下, 不显示icon
        if (null == mIconNormal && null == mIconPressed) {
            bIconVisible = false;
        }

        ta.recycle();
    }

    private void initViews() {
        // Icon
        if (bIconVisible) {
            mIvIcon = new ImageView(getContext());
            LayoutParams lpIcon = new LayoutParams(mIconSize, mIconSize);
            lpIcon.setMargins(mPaddingHorizontal, 0, mPaddingIcon, 0);
            lpIcon.gravity = Gravity.CENTER_VERTICAL;
            mIvIcon.setImageDrawable(mIconNormal);
            mIvIcon.setLayoutParams(lpIcon);
            this.addView(mIvIcon);
        }


        mTvText = new TextView(getContext());
        LayoutParams lpText = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        if (!TextUtils.isEmpty(mStrText)) {
            lpText.setMargins(mPaddingHorizontal, 0, mPaddingHorizontal, 0);
        }
        lpText.gravity = Gravity.CENTER_VERTICAL;
        mTvText.setText(mStrText);
        mTvText.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
        mTvText.setTextColor(mColorTextNormal);
        mTvText.setLayoutParams(lpText);

        this.addView(mTvText);
    }

    private void initListeners() {
        this.setClickable(true);
        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        setPressedState(true);
                        break;

                    case MotionEvent.ACTION_UP:
                        setPressedState(false);
                        //triggerClick(event);
                        break;
                }

                return false;
            }
        });
    }

    private void setPressedState(boolean pressed) {
        if (bPressed == pressed) {
            return;
        }

        bPressed = pressed;

        if (bPressed) {
            if (bIconVisible) {
                mIvIcon.setImageDrawable(mIconPressed);
            }

            mTvText.setTextColor(mColorTextPressed);
        } else {
            if (bIconVisible) {
                mIvIcon.setImageDrawable(mIconNormal);
            }

            mTvText.setTextColor(mColorTextNormal);
        }
    }

    /*private void triggerClick(MotionEvent e) {
        float eX = e.getX();
        float eY = e.getY();

        if (eX >= 0 && eX <= this.getWidth() && eY >= 0 && eY <= this.getHeight()) {
            if (mOnClickListener != null) {
                L.e("&&&", "resBtn triggerClick");
                mOnClickListener.onClick(ResourceButton.this);
            }
        }
    }*/

    public interface OnBtnClickListener {
        void onClick(View v);
    }

    @Override
    public boolean performClick() {
        if (null != mOnClickListener) {
            mOnClickListener.onClick(ResourceButton.this);
        }

        return super.performClick();
    }
}
