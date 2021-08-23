package com.txznet.comm.ui.theme.test.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.test.anim.FrameAnimation;
import com.txznet.comm.ui.theme.test.config.Constants;
import com.txznet.comm.ui.theme.test.skin.SK;
import com.txznet.comm.ui.theme.test.utils.ExtViewAdapter;
import com.txznet.comm.ui.theme.test.widget.FloatViewRelativeLayout;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IFloatView;
import com.txznet.comm.ui.viewfactory.view.IRecordView;
import com.txznet.loader.AppLogicBase;
import com.txznet.resholder.R;

import static android.util.TypedValue.COMPLEX_UNIT_PX;

/**
 * 悬浮按钮
 * <p>
 * 2020-09-09 18:00
 *
 * @author xiaolin
 */
public class FloatView extends IFloatView {

    private static FloatView sInstance = new FloatView();
    private FloatViewRelativeLayout floatViewRelativeLayout;
    private ImageView igView;
    private TextView tvTime;
    private FrameAnimation animationWait;

    private int floatButtonState = 0;// 0:默认图标，1:倒计时图标

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
    public ExtViewAdapter getView(ViewData data) {
        Context context = UIResLoader.getInstance().getModifyContext();
        View view = LayoutInflater.from(context).inflate(R.layout.float_view, (ViewGroup) null);
        floatViewRelativeLayout = (FloatViewRelativeLayout) view;
        igView = view.findViewById(R.id.img);
        tvTime = view.findViewById(R.id.tvTime);

        floatButtonState = 0;
        igView.setImageDrawable(getPersonDrawable());

        ExtViewAdapter viewAdapter = new ExtViewAdapter();
        viewAdapter.view = view;
        viewAdapter.view.setTag(viewAdapter);
        viewAdapter.object = FloatView.getInstance();
        return viewAdapter;
    }

    @Override
    public void init() {
        LogUtil.logd(WinLayout.logTag + "FloatView.init()");

        animationWait = new FrameAnimation();
        for (int i = 1; i <= 48; i++) {
            Drawable drawable = LayouUtil.getDrawable("logo_frame_wait" + i);
            animationWait.addFrame(drawable, 40);
        }

    }

    private long mAutoStopAstTimeout = 0;
    private long mStartTime = 0;
    private Runnable updateTimeRunnable = new Runnable() {
        @Override
        public void run() {
            long rest = mAutoStopAstTimeout - (System.currentTimeMillis() - mStartTime);
            rest = Math.round(rest / 1000F);
            if (rest <= 0) {
                AppLogicBase.removeUiGroundCallback(updateTimeRunnable);
                floatButtonState = 0;
                igView.setImageDrawable(getPersonDrawable());
                tvTime.setText("");
            } else {
                float textSize = floatViewRelativeLayout.getSize() * 0.19F;
                if (tvTime.getTextSize() != textSize) {
                    tvTime.setTextSize(COMPLEX_UNIT_PX, textSize);
                    tvTime.setPadding(0, 0, 0, (int) (floatViewRelativeLayout.getSize() * 0.02F));
                }
                tvTime.setText(String.valueOf(rest));
                AppLogicBase.removeUiGroundCallback(updateTimeRunnable);
                AppLogicBase.runOnUiGround(updateTimeRunnable, 334);
            }
        }
    };

    @Override
    public void updateState(int state) {
        LogUtil.logd(WinLayout.logTag + "FloatView.updateState() state:" + state);

        if (state == IRecordView.STATE_START_ASR_WHEN_DISMISS) {// 开始
//			if(igView != null){
//				igView.setImageDrawable(animationWait);
//				animationWait.setOneShot(false);
//				animationWait.stop();
//				animationWait.start();
//			}
            if (igView != null && Constants.enableStartAsrWhenDismiss) {
                floatButtonState = 1;
                // 倒计时背景
                igView.setImageDrawable(SK.getDrawable(SK.DRAWABLE.person_float_time));
                mAutoStopAstTimeout = Constants.autoStopAstTimeout;
                mStartTime = System.currentTimeMillis();
                // 移除之前的任务
                AppLogicBase.removeUiGroundCallback(updateTimeRunnable);
                AppLogicBase.runOnUiGround(updateTimeRunnable);
            }
        } else if (state == IRecordView.STATE_WIN_CLOSE) {// 结束
            if (!Constants.enableStartAsrWhenDismiss) {
                // 移除之前的任务
                AppLogicBase.removeUiGroundCallback(updateTimeRunnable);
            }
            //animationWait.stop();
            floatButtonState = 0;
            igView.setImageDrawable(getPersonDrawable());
            tvTime.setText("");
        }
    }

    @Override
    public void updateVolume(int volume) {
        LogUtil.logd(WinLayout.logTag + "FloatView.updateVolume() volume:" + volume);
    }

    private Drawable getPersonDrawable(){
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, SK.getDrawable(SK.DRAWABLE.person_float_press));
        stateListDrawable.addState(new int[]{-android.R.attr.state_pressed}, SK.getDrawable(SK.DRAWABLE.person_float));
        return stateListDrawable;
    }


    public void onUpdateResource(){
        if(igView == null){
            return;
        }
        if(floatButtonState == 0){
            igView.setImageDrawable(getPersonDrawable());
        } else if(floatButtonState == 1){
            igView.setImageDrawable(SK.getDrawable(SK.DRAWABLE.person_float_time));
        }
    }


}
