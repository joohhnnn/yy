package com.txznet.webchat.ui.rearview_mirror.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.txznet.webchat.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 包含图标、文字、两种不同状态的按钮, 用于主界面方形按钮
 * 只包含了最简单的xml设置逻辑，待扩充
 * Created by J on 2016/3/18.
 */
public class IconTextStateBtn extends FrameLayout {
    @Bind(R.id.iv_itsb_icon)
    ImageView mIvIcon;
    @Bind(R.id.tv_itsb_text)
    TextView mTvText;

    // two states
    public static final String BTN_STATE_ENABLED = "icon_text_state_btn_enabled";
    public static final String BTN_STATE_DISABLED = "icon_text_state_btn_disabled";
    //view resources
    private int mIconNormal; // 默认状态图标
    private int mIconDisabled; // 第二状态图标
    private String mTextNormal; // 按钮文字
    private String mTextDisabled; // 第二状态文字
    private boolean bClickable;

    //enable status
    private boolean mIsEnabled = true;

    //event
    private OnClickListener mOnClickListener;


    public IconTextStateBtn(Context context) {
        super(context);
        init(context, null);
    }

    public IconTextStateBtn(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public IconTextStateBtn(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public void setEnabled(boolean enabled) {
        if (enabled != mIsEnabled) {
            changeState();
        }
    }

    public void setTextEnabled(String text) {
        mTextNormal = text;

        updateState();
    }

    public void setTextDisabled(String text) {
        mTextDisabled = text;

        updateState();
    }

    public void setIconEnabled(int iconRes) {
        mIconNormal = iconRes;

        updateState();
    }

    public void setIconDisabled(int iconRes) {
        mIconDisabled = iconRes;

        updateState();
    }

    public boolean isEnabled() {
        return mIsEnabled;
    }

    private void init(Context context, AttributeSet attrs) {
        //inflate layout
        View v = LayoutInflater.from(context).inflate(R.layout.layout_icon_text_state_btn, this, true);
        ButterKnife.bind(this, v);

        //init attrs
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.IconTextStateBtn);
            mIconNormal = ta.getResourceId(R.styleable.IconTextStateBtn_itsb_icon, 0);
            mIconDisabled = ta.getResourceId(R.styleable.IconTextStateBtn_itsb_icon_disabled, 0);
            mTextNormal = ta.getString(R.styleable.IconTextStateBtn_itsb_text);
            mTextDisabled = ta.getString(R.styleable.IconTextStateBtn_itsb_text_disabled);
            bClickable = ta.getBoolean(R.styleable.IconTextStateBtn_itsb_clickable, true);
            //set icon & text
            //mIvIcon.setImageResource(mIconNormal);
            mIvIcon.setImageDrawable(getResources().getDrawable(mIconNormal));
            mTvText.setText(mTextNormal);
            ta.recycle();
        }

        //click listener
        this.setClickable(bClickable);
        if (bClickable) {
            this.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    changeState();

                    if (mOnClickListener != null) {
                        mOnClickListener.onClick(v, mIsEnabled ? BTN_STATE_ENABLED : BTN_STATE_DISABLED);
                    }
                }
            });
        }
    }

    private void changeState() {
        mIsEnabled = !mIsEnabled;

        updateState();
    }

    private void updateState() {
        if (mIsEnabled) {
            mIvIcon.setImageDrawable(getResources().getDrawable(mIconNormal));
            mTvText.setText(mTextNormal);
        } else {
            if (null != mTextDisabled) {
                mTvText.setText(mTextDisabled);
            }

            if (0 != mIconDisabled) {
                mIvIcon.setImageDrawable(getResources().getDrawable(mIconDisabled));
            }
        }
    }

    public void setOnBtnClickListener(OnClickListener listener) {
        mOnClickListener = listener;
    }


    public interface OnClickListener {
        void onClick(View v, String state);
    }
}
