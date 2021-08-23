package com.txznet.txz.component.offlinepromote.dialog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.recordwin.Win2Dialog;
import com.txznet.loader.AppLogic;
import com.txznet.txz.R;
import com.txznet.txz.module.offlinepromote.OfflinePromoteManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.util.DeviceInfo;
import com.txznet.txz.util.QRUtil;

import java.lang.reflect.Field;
import java.util.List;

public class OfflinePromoteContentDialog extends Win2Dialog {
    private static final String TAG = "OfflinePromoteContent";

    private Builder mBuilder;

    private static final int[] DEFAULT_IMAGE = {R.drawable.offline_promote_1, R.drawable.offline_promote_2, R.drawable.offline_promote_3, R.drawable.offline_promote_4};
    private ViewPager mImgViewPager;
    private MyPagerAdapter mPagerAdapter;
    private LinearLayout indicatorLy;
    private static final int PAGE_COUNT = 4;

    private static final String TEXT_NEXT_LOOK = "下次再看";
    private static final String TEXT_UNINTERESTED = "不感兴趣";
    private static final String TEXT_ALREADY_SCAN = "已经扫码";
    public static final String TEXT_TITLE = "扫码使用新功能";
    private LinearLayout mRootLayout;

    private static final String TASK_OFFLINE_PROMOTE = "TASK_OFFLINE_PROMOTE";

    private OfflinePromoteContentDialog(Builder builder) {
        super(true, true, builder);
    }

    @Override
    protected View createView(Object... objects) {
        mBuilder = (Builder) objects[0];
        mRootLayout = new LinearLayout(GlobalContext.get());
        mRootLayout.setGravity(Gravity.CENTER);
        mRootLayout.addView(createLayout());
        mRootLayout.setScaleX(getScale());
        mRootLayout.setScaleY(getScale());
        return mRootLayout;
    }

    private View createLayout() {
        final LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundResource(R.drawable.shape_feedback);
        layout.setLayoutParams(new ViewGroup.LayoutParams(520, 422));
        //标题
        TextView tvTitle = new TextView(getContext());
        int textSize = 24;
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        tvTitle.setText(mBuilder.title != null ? mBuilder.title : TEXT_TITLE);
        tvTitle.setTextColor(Color.parseColor("#FFFFFF"));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_HORIZONTAL;
        tvTitle.setLayoutParams(params);
        int padding = 13;
        tvTitle.setPadding(0, padding, 0, padding);

        layout.addView(tvTitle);
        // 创建ViewPager界面
        layout.addView(createViewPager(getPageCount()));
        //指示器（。。.。）
        indicatorLy = new LinearLayout(getContext());
        indicatorLy.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams indParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        indParam.gravity = Gravity.CENTER_HORIZONTAL;
        indParam.topMargin = indParam.bottomMargin = 12;
        indicatorLy.setLayoutParams(indParam);
        layout.addView(indicatorLy);
        //初始化
        showIndicator(0);

        //白线
        View lineView = new View(getContext());
        lineView.setBackgroundColor(Color.parseColor("#26FFFFFF"));
        lineView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
        layout.addView(lineView);

        //btn
        LinearLayout btnLayout = new LinearLayout(GlobalContext.get());
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        btnLayout.setOrientation(LinearLayout.HORIZONTAL);
        btnLayout.setLayoutParams(params);
        layout.addView(btnLayout);

        //下次再看
        TextView tvNext = new TextView(GlobalContext.get());
        tvNext.setText(TEXT_NEXT_LOOK);
        tvNext.setGravity(Gravity.CENTER);
        tvNext.setTextColor(Color.parseColor("#44A9FF"));
        tvNext.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
        tvNext.setLayoutParams(params);
        tvNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OfflinePromoteManager.getInstance().onClickFeedback(OfflinePromoteManager.FEEDBACK_TYPE_CLOSE);
            }
        });
        btnLayout.addView(tvNext);

        //白线
        lineView = new View(getContext());
        lineView.setBackgroundColor(Color.parseColor("#26FFFFFF"));
        lineView.setLayoutParams(new ViewGroup.LayoutParams(1, ViewGroup.LayoutParams.MATCH_PARENT));
        btnLayout.addView(lineView);
        //不感兴趣
        TextView tvUnInterested = new TextView(GlobalContext.get());
        tvUnInterested.setText(TEXT_UNINTERESTED);
        tvUnInterested.setGravity(Gravity.CENTER);
        tvUnInterested.setTextColor(Color.parseColor("#B244A9FF"));
        tvUnInterested.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
        tvUnInterested.setLayoutParams(params);
        tvUnInterested.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OfflinePromoteManager.getInstance().onClickFeedback(OfflinePromoteManager.FEEDBACK_TYPE_UNINTERESTED);
            }
        });
        btnLayout.addView(tvUnInterested);
        //白线
        lineView = new View(getContext());
        lineView.setBackgroundColor(Color.parseColor("#26FFFFFF"));
        lineView.setLayoutParams(new ViewGroup.LayoutParams(1, ViewGroup.LayoutParams.MATCH_PARENT));
        btnLayout.addView(lineView);
        //已经扫码
        TextView tvAlreadyScan = new TextView(GlobalContext.get());
        tvAlreadyScan.setText(TEXT_ALREADY_SCAN);
        tvAlreadyScan.setGravity(Gravity.CENTER);
        tvAlreadyScan.setTextColor(Color.parseColor("#B244A9FF"));
        tvAlreadyScan.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
        tvAlreadyScan.setLayoutParams(params);
        tvAlreadyScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OfflinePromoteManager.getInstance().onClickFeedback(OfflinePromoteManager.FEEDBACK_TYPE_ALREADY_SCAN);
            }
        });
        btnLayout.addView(tvAlreadyScan);

        return layout;
    }


    @SuppressWarnings("WeakerAccess")
    private class MyPagerAdapter extends PagerAdapter {
        private final int size;
        private final boolean isBoundless;

        MyPagerAdapter(int size, boolean isBoundless) {
            this.size = size;
            this.isBoundless = isBoundless;
        }

        @Override
        public int getCount() {
            return isBoundless && size > 1 ? Integer.MAX_VALUE : size;
        }

        public int getVisibleCount() {
            return size;
        }

        public int getStartIndex() {
            int halfCount = getCount() / 2;
            return halfCount - halfCount % size;
        }

        private int getSmartSize(int size) {
            if (!isBoundless) return size;
            int minimumSize = 4;
            int result = size;
            while (result < minimumSize) {
                result += size;
            }
            return result;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            int realPosition = position % size;
            View itemView = createView(realPosition);
            container.addView(itemView);
            return itemView;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

        private View createView(final int pos) {
            FrameLayout layout = new FrameLayout(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 222);
            layout.setLayoutParams(params);
            ImageView imageView = new ImageView(GlobalContext.get());
            FrameLayout.LayoutParams fParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            fParams.gravity = Gravity.CENTER_HORIZONTAL;
            if (mBuilder.getIvUrlList() == null) {
                imageView.setImageResource(DEFAULT_IMAGE[pos]);
            } else {
                Bitmap bitmap = BitmapFactory.decodeFile(mBuilder.getIvUrlList().get(pos));
                imageView.setImageBitmap(bitmap);
            }
//            imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setLayoutParams(fParams);
            layout.addView(imageView);

            if (pos == size - 1) {
                int width = 132;
                int logoWidth = 22;
                Bitmap logoBitmap = BitmapFactory.decodeResource(GlobalContext.get().getResources(), R.drawable.logo);
                Bitmap bitmap = QRUtil.createQRCodeWithLogo(mBuilder.getQrCodeUrl(), width, 4, logoBitmap, logoWidth);
                ImageView ivQrCode = new ImageView(GlobalContext.get());
                ivQrCode.setBackgroundColor(Color.WHITE);
                fParams = new FrameLayout.LayoutParams(142,142);
                fParams.gravity = Gravity.CENTER_HORIZONTAL;
                ivQrCode.setLayoutParams(fParams);
                int padding = 10;
                ivQrCode.setPadding(padding, padding, padding, padding);
                ivQrCode.setImageBitmap(bitmap);
                layout.addView(ivQrCode);
            }
            return layout;
        }
    }

    private ViewPager createViewPager(final int size) {
        // 创建ViewPager
        final boolean isBoundless = Boolean.parseBoolean("true");
        mPagerAdapter = new MyPagerAdapter(size, isBoundless);
        mImgViewPager = new ViewPager(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 222);
        params.topMargin = 28;
        mImgViewPager.setLayoutParams(params);
        mImgViewPager.setAdapter(mPagerAdapter);
        mImgViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                showIndicator(position % getPageCount());
                long time = SystemClock.elapsedRealtime();
                if(time - mLastTime < mDelayTime){//小于3s表示是手动滑动，大于等于3s则表示是自动轮播
                    OfflinePromoteManager.getInstance().onClickFeedback(OfflinePromoteManager.FEEDBACK_TYPE_SLIP);
                }
                carousel();
            }
        });
        mImgViewPager.setCurrentItem(mPagerAdapter.getStartIndex());
        return mImgViewPager;
    }

    public static int getPageCount() {
        return PAGE_COUNT;
    }

    private void showIndicator(int currIdx) {
        if (indicatorLy == null) {
            return;
        }
        int size = getPageCount();
        if (size > 1) {
            indicatorLy.removeAllViews();
            for (int i = 0; i < size; i++) {
                View view = null;
                if (i == currIdx) {
                    view = createSolidPoint();
                } else {
                    view = createStrokePoint();
                }
                if (view != null) {
                    indicatorLy.addView(view);
                }
            }
        }
    }

    private View createSolidPoint() {
        Context context = getContext();
        int wh = 8;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(wh, wh);
        View view = new View(context);
        int margin = 5;
        params.setMargins(margin, 0, margin, 0);
        String color = "#1C7DFD";
        view.setBackgroundDrawable(shapeSolidDrawable(color));
        view.setLayoutParams(params);
        return view;
    }

    private View createStrokePoint() {
        Context context = getContext();
        int wh = 8;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(wh, wh);
        View view = new View(context);
        int margin = 5;
        params.setMargins(margin, 0, margin, 0);
        String color = "#33FFFFFF";
        view.setBackgroundDrawable(shapeSolidDrawable(color));
        view.setLayoutParams(params);
        return view;
    }

    private Drawable shapeSolidDrawable(String color) {
        ShapeDrawable drawable = new ShapeDrawable();
        drawable.setShape(new OvalShape());
        drawable.getPaint().setColor(Color.parseColor(color));
        return drawable;
    }

    public float getScale() {
        return Math.min((float) DeviceInfo.getScreenWidth() / 1024, (float) DeviceInfo.getScreenHeight() / 600);
    }

    private boolean isTouch = false;

    @Override
    public void show() {
        resetFirstPos();
        carousel();
        super.show();

        AsrUtil.AsrComplexSelectCallback callback = new AsrUtil.AsrComplexSelectCallback() {
            @Override
            public boolean needAsrState() {
                return false;
            }


            @Override
            public String getTaskId() {
                return TASK_OFFLINE_PROMOTE;
            }

            @Override
            public void onCommandSelected(String type, String command) {
                if ("NEXT_LOOK".equals(type)) {
                    OfflinePromoteManager.getInstance().onClickFeedback(OfflinePromoteManager.FEEDBACK_TYPE_CLOSE);
                } else if ("UNINTERESTED".equals(type)) {
                    OfflinePromoteManager.getInstance().onClickFeedback(OfflinePromoteManager.FEEDBACK_TYPE_UNINTERESTED);
                } else if ("ALREADY_SCAN".equals(command)) {
                    OfflinePromoteManager.getInstance().onClickFeedback(OfflinePromoteManager.FEEDBACK_TYPE_ALREADY_SCAN);
                }
            }
        }.addCommand("NEXT_LOOK",TEXT_NEXT_LOOK)
                .addCommand("UNINTERESTED",TEXT_UNINTERESTED)
                .addCommand("ALREADY_SCAN",TEXT_ALREADY_SCAN);
        WakeupManager.getInstance().useWakeupAsAsr(callback);

        GlobalContext.get().registerReceiver(mHomeReceiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
    }


    private long mLastTime = 0;
    private int mDelayTime = 3000;
    /**
     * 执行轮播任务
     */
    private void carousel(){
        mLastTime = SystemClock.elapsedRealtime();
        removeCarousel();
        AppLogic.runOnUiGround(mCarouselTask, mDelayTime);
    }

    private void removeCarousel(){
        AppLogic.removeUiGroundCallback(mCarouselTask);
    }

    private Runnable mCarouselTask = new Runnable() {
        @Override
        public void run() {
            int pos = mImgViewPager.getCurrentItem();
            mImgViewPager.setCurrentItem(pos + 1);
        }
    };

    /**
     * setCurrentItem>1会造成卡顿，ANR
     */
    private void resetFirstPos(){
        //每次show都需要从第一张图片开始展示
        int currentPos = mImgViewPager.getCurrentItem();
        int pos = currentPos % getPageCount();
        if(pos == 0){
            return;
        }
        for (int i = 1; i <= getPageCount() - pos; i++) {
            mImgViewPager.setCurrentItem( currentPos + i);
        }
        showIndicator(0);
    }

    public static class Builder {
        private String title;
        private String qrCodeUrl;
        private List<String> ivUrlList;//图片本地路径

        public String getTitle() {
            if(TextUtils.isEmpty(title)){
                return TEXT_TITLE;
            }
            return title;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public String getQrCodeUrl() {
            return qrCodeUrl;
        }

        public Builder setQrCodeUrl(String qrCodeUrl) {
            this.qrCodeUrl = qrCodeUrl;
            return this;
        }

        public List<String> getIvUrlList() {
            return ivUrlList;
        }

        public Builder setIvUrlList(List<String> ivUrlList) {
            this.ivUrlList = ivUrlList;
            return this;
        }

        public OfflinePromoteContentDialog build() {
            return new OfflinePromoteContentDialog(this);
        }

    }

    @Override
    public void dismiss() {
        super.dismiss();
        WakeupManager.getInstance().recoverWakeupFromAsr(TASK_OFFLINE_PROMOTE);
        GlobalContext.get().unregisterReceiver(mHomeReceiver);
        removeCarousel();
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == keyCode) {
            dismiss();
            OfflinePromoteManager.getInstance().showFloatWindow();
        }
        return super.onKeyDown(keyCode, event);
    }

    private BroadcastReceiver mHomeReceiver = new BroadcastReceiver() {
        private static final String LOG_TAG = "HomeReceiver";
        private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
        private static final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
        private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
        private static final String SYSTEM_DIALOG_REASON_LOCK = "lock";
        private static final String SYSTEM_DIALOG_REASON_ASSIST = "assist";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtil.logd("onReceive: action: " + action);
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                // android.intent.action.CLOSE_SYSTEM_DIALOGS
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if (SYSTEM_DIALOG_REASON_HOME_KEY.equals(reason)) {
                    // 短按Home键
                    dismiss();
                    OfflinePromoteManager.getInstance().showFloatWindow();
                } else if (SYSTEM_DIALOG_REASON_RECENT_APPS.equals(reason)) {
                    // 长按Home键 或者 activity切换键
                } else if (SYSTEM_DIALOG_REASON_LOCK.equals(reason)) {
                    // 锁屏
                } else if (SYSTEM_DIALOG_REASON_ASSIST.equals(reason)) {
                    // samsung 长按Home键
                }
            }
        }
    };

}
