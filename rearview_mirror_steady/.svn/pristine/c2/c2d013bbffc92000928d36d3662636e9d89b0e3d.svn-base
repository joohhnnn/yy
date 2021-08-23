package com.txznet.launcher.module.nav;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.txznet.launcher.R;
import com.txznet.launcher.component.nav.NavAppComponent;
import com.txznet.launcher.domain.nav.NavManager;
import com.txznet.launcher.event.EventTypes;
import com.txznet.launcher.module.BaseModule;
import com.txznet.loader.AppLogic;
import com.txznet.txz.util.runnables.Runnable1;

/**
 * Created by TXZ-METEORLUO on 2018/2/7.
 * 导航应用有独立的应用界面，当用户返回桌面后，以卡片的形式在桌面显示。这个类就是该卡片的界面
 */

public class HUDModule extends BaseModule {
    private NavAppComponent.HUDUpdateListener mUpdateListener = new NavAppComponent.HUDUpdateListener() {

        @Override
        public void onHudUpdate(NavAppComponent.HUDInfo info) {
            if (Looper.getMainLooper() == Looper.myLooper()) {
                refreshView(info);
            } else {
                AppLogic.runOnUiGround(new Runnable1<NavAppComponent.HUDInfo>(info) {
                    @Override
                    public void run() {
                        mTmpInfo = mP1;
                        refreshView(mP1);
                    }
                });
            }
        }
    };

    private NavAppComponent.HUDInfo mInitInfo;

    @Override
    public void onCreate(String data) {
        super.onCreate(data);
        mInitInfo = NavAppComponent.HUDInfo.fromString(data);
        NavManager.getInstance().addHUDUpdateListener(mUpdateListener);
    }

    private int curStatus;
    private Context mContext;

    @Override
    public View onCreateView(Context context, ViewGroup parent, int status) {
        curStatus = status;
        mContext = context;
        View view = null;
        switch (status) {
            case STATUS_FULL:
                view = View.inflate(context, R.layout.module_nav_hud_full, null);
                initFullView(view);
                break;
            case STATUS_HALF:
                view = View.inflate(context, R.layout.module_nav_hud_half, null);
                initHalfView(view);
                break;
            case STATUS_THIRD:
                view = View.inflate(context, R.layout.module_nav_hud_third, null);
                initHalfView(view);
                break;
        }
        // 将最不可能为false的判断放在最后，减少判断次数。
        if (mTmpInfo != null && view != null && mContext != null) {
            refreshView(mTmpInfo);
        }
        return view;
    }

    private TextView sourceTv;
    private ImageView directIv;
    private TextView limitTv;
    private TextView distanceTv;
    private TextView nextRoadTv;

    private void initFullView(View view) {
        sourceTv = (TextView) view.findViewById(R.id.source_tv);
        directIv = (ImageView) view.findViewById(R.id.direct_iv);
        limitTv = (TextView) view.findViewById(R.id.limit_speed_tv);
        distanceTv = (TextView) view.findViewById(R.id.remain_distance_tv);
        nextRoadTv = (TextView) view.findViewById(R.id.next_road_tv);

        refreshView(mInitInfo);
    }

    private TextView mDestTv;
    private TextView mRemainTv;
    private LinearLayout mLayout;

    private void initHalfView(View view) {
        mDestTv = (TextView) view.findViewById(R.id.dest_tv);
        mRemainTv = (TextView) view.findViewById(R.id.remain_tv);
        mLayout = (LinearLayout) view.findViewById(R.id.traffic_bar);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mTmpInfo != null) {
            refreshView(mTmpInfo);
        }
    }

    private NavAppComponent.HUDInfo mTmpInfo;

    private void refreshView(NavAppComponent.HUDInfo info) {
        if (info == null) {
            return;
        }
        if (mContext == null) {
            mTmpInfo = info;
            return;
        }
        String navPkn = info.navPkn;
        String dirDes = info.dirDes; // 如左转右转
        int limitSpeed = info.limitSpeed;// 道路限速 km/h
        int dirDistance = info.dirDistance;// 多少米后
        String curRoad = info.curRoad;// 当前道路
        String nextRoad = info.nextRoad;// 下一条道路
        String destName = info.destName;// 目的地名称
        int remainTime = info.remainTime;// 剩余时间
        int remainDistance = info.remainDistance;//剩余距离
        String appName = getAppNameByPkn(mContext, navPkn);

        if (curStatus == STATUS_FULL) {
            // 根据需求，只有一个导航卡片时名字写死成高德导航，但考虑到可能还会换这个名字，所以保留了对应的view对象。
//            sourceTv.setText(appName);
            directIv.setImageResource(getDirectIconResId(navPkn, dirDes));
            if (limitSpeed > 0) {
                limitTv.setVisibility(View.VISIBLE);
                limitTv.setText(limitSpeed + "");
            } else {
                limitTv.setVisibility(View.INVISIBLE);
            }
            distanceTv.setText(dirDistance + "");
            nextRoadTv.setText(nextRoad);
        } else if (curStatus == STATUS_HALF || curStatus == STATUS_THIRD) {
            mDestTv.setText("目的地：" + destName);
            mRemainTv.setText("剩余" + NavManager.getInstance().getRemainDistance(remainDistance)
                    + "，大约" + NavManager.getInstance().getRemainTime(remainTime) + "后到达");
        }
    }

    /**
     * 通过描述获取对应图标的ID
     *
     * @param dirDesc
     * @return
     */
    private int getDirectIconResId(String navPkn, String dirDesc) {
        if (!TextUtils.isEmpty(navPkn)) {
            if (navPkn.startsWith("com.autonavi.amap")) {
                return AUTONAVI_ICON[findAmapDesIdx(dirDesc)];
            }
        }
        return AUTONAVI_ICON[findAmapDesIdx(dirDesc)];
    }

    private int findAmapDesIdx(String desc) {
        for (int i = 0; i < AUTONAVI_DES.length; i++) {
            String des = AUTONAVI_DES[i];
            if (des.equals(desc)) {
                return i;
            }
        }
        return 0;
    }

    // 高德auto引导数据
    public static final String[] AUTONAVI_DES = new String[]{"自车", "左转", "右转", "左前方", "右前方", "左后方", "右后方", "左转掉头",
            "直行", "到达途经点", "进入环岛", "驶出环岛", "到达服务区", "到达收费站", "到达目的地", "进入隧道"};
    // 高德地图对应图标
    public static final int[] AUTONAVI_ICON = new int[]{
            R.drawable.sou0, R.drawable.sou2, R.drawable.sou3, R.drawable.sou4, R.drawable.sou5,
            R.drawable.sou6, R.drawable.sou7, R.drawable.sou8, R.drawable.sou9, R.drawable.sou10,
            R.drawable.sou11, R.drawable.sou12, R.drawable.sou13, R.drawable.sou14, R.drawable.sou15, R.drawable.sou16,
    };

    private String getAppNameByPkn(Context context, String navPkn) {
        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo info = pm.getApplicationInfo(navPkn, 0);
            if (info != null) {
                return (String) info.loadLabel(pm);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        NavManager.getInstance().removeHUDUpdateListener(mUpdateListener);
    }

    @Override
    public String[] getObserverEventTypes() {
        return new String[]{
                EventTypes.EVENT_NAV_FOREGROUND,
                EventTypes.EVENT_NAV_BACKGROUND,
                EventTypes.EVENT_NAV_START_NAVI,
                EventTypes.EVENT_NAV_END_NAVI,
                EventTypes.EVENT_NAV_EXIT_NAVI
        };
    }

    @Override
    protected void onEvent(String eventType) {
        if (EventTypes.EVENT_NAV_FOREGROUND.equals(eventType)) {
            onForebackGround(true);
        } else if (EventTypes.EVENT_NAV_BACKGROUND.equals(eventType)) {
            onForebackGround(false);
        } else if (EventTypes.EVENT_NAV_START_NAVI.equals(eventType)) {
            onNavState(true);
        } else if (EventTypes.EVENT_NAV_END_NAVI.equals(eventType)) {
            onNavState(false);
        } else if (EventTypes.EVENT_NAV_EXIT_NAVI.equals(eventType)) {
            onNavExit();
        }
    }

    private void onForebackGround(boolean isFocus) {

    }

    private void onNavState(boolean isNaving) {

    }

    private void onNavExit() {

    }
}
