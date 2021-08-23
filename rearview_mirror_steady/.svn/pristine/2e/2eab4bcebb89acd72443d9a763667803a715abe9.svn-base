package com.txznet.launcher.module.wechat;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.percent.PercentRelativeLayout;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.TextureMapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.launcher.R;
import com.txznet.launcher.domain.wechat.WechatManager;
import com.txznet.launcher.event.EventTypes;
import com.txznet.launcher.img.ImgLoader;
import com.txznet.launcher.module.BaseModule;
import com.txznet.launcher.module.wechat.bean.WechatMsgData;
import com.txznet.launcher.utils.PreferenceUtil;
import com.txznet.launcher.widget.CornerMaskImageView;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZLocationManager;
import com.txznet.sdk.TXZWechatManagerV2;
import com.txznet.sdk.bean.LocationData;
import com.txznet.sdk.bean.WechatMessageV2;
import com.txznet.txz.util.runnables.Runnable1;

import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by TXZ-METEORLUO on 2018/2/24.
 * 微信登录后展示的界面，有个人的头像、昵称等。
 */

public class WechatModule extends BaseModule {

    private int status;
    private FullViewHolder fullViewHolder;
    private WechatViewHolder wechatViewHolder;
    private WechatManager.WechatStateListener listener;

    @Override
    public View onCreateView(Context context, ViewGroup parent, int status) {
        this.status = status;
        View rootView = null;
        switch (status) {
            case STATUS_FULL:
                rootView = View.inflate(context, R.layout.module_wechat_full, null);
                fullViewHolder = new FullViewHolder(rootView);
                break;
            case STATUS_HALF:
                rootView = View.inflate(context, R.layout.module_wechat_half, null);
                wechatViewHolder = new WechatViewHolder(rootView);
                break;
            case STATUS_THIRD:
                rootView = View.inflate(context, R.layout.module_wechat_third, null);
                wechatViewHolder = new WechatViewHolder(rootView);
                break;
        }
        showNormalView();
        return rootView;
    }

    private void showNormalView() {
        switch (status) {
            case STATUS_FULL:
                fullViewHolder.mapWechatLocation.setVisibility(View.GONE);
                fullViewHolder.mapWechatLocation.onDestroy();
                fullViewHolder.lyAddr.setVisibility(View.GONE);
                fullViewHolder.llWechatUser.setVisibility(View.VISIBLE);
                fullViewHolder.llWechatContacts.setVisibility(View.VISIBLE);
                updateContactsHead();
                fullViewHolder.flWechatMsg.setVisibility(View.GONE);
                loadUserIcon(fullViewHolder.ivWechatUserIcon);

                fullViewHolder.tvWechatUserNick.setText(PreferenceUtil.getInstance().getString(PreferenceUtil.KEY_WECHAT_USER_NICK, ""));
                //加载最近的聊天用户
                break;
            case STATUS_HALF:
            case STATUS_THIRD:
                wechatViewHolder.rlWechatUser.setVisibility(View.VISIBLE);
                wechatViewHolder.flMsgLayout.setVisibility(View.GONE);
                loadUserIcon(wechatViewHolder.ivWechatUserIcon);
                wechatViewHolder.tvWechatUserNick.setText(PreferenceUtil.getInstance().getString(PreferenceUtil.KEY_WECHAT_USER_NICK, ""));
                break;
        }
    }

    private void loadUserIcon(final ImageView imageView){
        String userId = PreferenceUtil.getInstance().getString(PreferenceUtil.KEY_WECHAT_USER_ID,"");
        if (TextUtils.isEmpty(userId)) {
            ImgLoader.loadCircleImage(WechatManager.WECHAT_USER_ICON_FILE, imageView, true);
        } else {
            WechatManager.getInstance().getUsericon(userId, new TXZWechatManagerV2.ImageListener() {
                @Override
                public void onImageReady(String id, String imgPath) {
                    ImgLoader.loadCircleImage(imgPath, imageView, true);
                }
            });
        }
    }

    @Override
    public void refreshView(String data) {
        super.refreshView(data);
    }

    private void showMessageView(WechatMsgData wechatMsgData) {
        switch (status) {
            case STATUS_FULL:
                switch (wechatMsgData.mMsgType) {
                    case WechatMessageV2.MSG_TYPE_LOCATION:
                        fullViewHolder.mapWechatLocation.setVisibility(View.VISIBLE);
                        fullViewHolder.lyAddr.setVisibility(View.VISIBLE);
                        fullViewHolder.llWechatUser.setVisibility(View.GONE);
                        fullViewHolder.tvWechatMsgNav.setVisibility(View.VISIBLE);
                        LocationData locationData = TXZLocationManager.getInstance().getCurrentLocationInfo();
                        if (locationData != null) {
                            float distance = AMapUtils.calculateLineDistance(
                                    new LatLng(locationData.dbl_lat, locationData.dbl_lng),
                                    new LatLng(wechatMsgData.mLatitude, wechatMsgData.mLongitude));
                            String distanceStr;
                            if (distance < 1000) {
                                distanceStr = Math.round(distance) + "M";
                            } else {
                                distanceStr = new DecimalFormat("#.#").format(distance / 1000) + "KM";
                            }
                            fullViewHolder.tvDistance.setText(distanceStr);
                        }
                        fullViewHolder.tvAddress.setText(wechatMsgData.mAddress);
                        initMap(wechatMsgData.mAddress, wechatMsgData.mLatitude, wechatMsgData.mLongitude);
                        break;
                    default:
                        fullViewHolder.mapWechatLocation.setVisibility(View.GONE);
                        fullViewHolder.lyAddr.setVisibility(View.GONE);
                        fullViewHolder.llWechatUser.setVisibility(View.VISIBLE);

                        ImgLoader.loadCircleImage(WechatManager.WECHAT_USER_ICON_FILE, fullViewHolder.ivWechatUserIcon, true);
                        fullViewHolder.tvWechatUserNick.setText(PreferenceUtil.getInstance().getString(PreferenceUtil.KEY_WECHAT_USER_NICK, ""));

                        fullViewHolder.tvWechatMsgNav.setVisibility(View.GONE);
                        break;
                }
                fullViewHolder.llWechatContacts.setVisibility(View.GONE);
                fullViewHolder.flWechatMsg.setVisibility(View.VISIBLE);

                fullViewHolder.tvWechatSenderNick.setText(wechatMsgData.mSessionNick);
                WechatManager.getInstance().getUsericon(wechatMsgData.mSenderId, new TXZWechatManagerV2.ImageListener() {
                    @Override
                    public void onImageReady(String id, String imgPath) {
                        ImgLoader.loadCircleImage(imgPath, fullViewHolder.ivWechatSenderIcon);
                    }
                });
                break;
            case STATUS_HALF:
            case STATUS_THIRD:
                switch (wechatMsgData.mMsgType) {
                    case WechatMessageV2.MSG_TYPE_LOCATION:
                        wechatViewHolder.tvWechatMsgNav.setVisibility(View.VISIBLE);
                        break;
                    default:
                        wechatViewHolder.tvWechatMsgNav.setVisibility(View.GONE);
                        break;
                }
                wechatViewHolder.rlWechatUser.setVisibility(View.GONE);
                wechatViewHolder.flMsgLayout.setVisibility(View.VISIBLE);
                wechatViewHolder.tvWechatSenderNick.setText(wechatMsgData.mSessionNick);
                WechatManager.getInstance().getUsericon(wechatMsgData.mSenderId, new TXZWechatManagerV2.ImageListener() {
                    @Override
                    public void onImageReady(String id, String imgPath) {
                        ImgLoader.loadCircleImage(imgPath, wechatViewHolder.ivWechatSenderIcon);
                    }
                });
                break;
        }
    }

    public void initMap(String title, double lat, double lng) {
        fullViewHolder.mapWechatLocation.onCreate(null);
        fullViewHolder.mapWechatLocation.getMap().getUiSettings().setZoomControlsEnabled(false);
        fullViewHolder.mapWechatLocation.getMap().getUiSettings().setScaleControlsEnabled(true);
        fullViewHolder.mapWechatLocation.getMap().setInfoWindowAdapter(new AMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return createInfoWindow(marker.getTitle());
            }

            @Override
            public View getInfoContents(Marker marker) {
                return null;
            }
        });
        Marker m = fullViewHolder.mapWechatLocation.getMap().addMarker(
                new MarkerOptions()
                        .position(new LatLng(lat, lng))
                        .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(
                                GlobalContext.get().getResources(), R.drawable.icon_map_marker))));
        m.setTitle(title);
        m.showInfoWindow();
        fullViewHolder.mapWechatLocation.getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 16));
        if (hasEnoughWidth(title, 16,280)) {
            fullViewHolder.mapWechatLocation.getMap().animateCamera(CameraUpdateFactory.scrollBy(0, 0));
        } else {
            fullViewHolder.mapWechatLocation.getMap().animateCamera(CameraUpdateFactory.scrollBy(0, -22));
        }
    }

    private View createInfoWindow(String poiName) {
        if (poiName == null) {
            poiName = "";
        }
        FrameLayout frameLayout = new FrameLayout(GlobalContext.get());
        TextView tv = new TextView(GlobalContext.get());
//        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(280, ViewGroup.LayoutParams.WRAP_CONTENT);
//        tv.setLayoutParams(params);
        if (hasEnoughWidth(poiName, 16, 280)) {
            tv.setGravity(Gravity.CENTER);
        } else {
            tv.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
        }
        tv.setTextSize(16);
        tv.setTextColor(Color.WHITE);
        tv.setMaxLines(2);
        tv.setText(poiName);
        frameLayout.setPadding(0, 10, 0, 10);
        frameLayout.setBackgroundResource(R.drawable.poi_issued_name_bg);
        frameLayout.addView(tv, new FrameLayout.LayoutParams(280, ViewGroup.LayoutParams.WRAP_CONTENT));
        return frameLayout;
    }

    private void updateContactsHead() {
        CopyOnWriteArrayList<String> contactsId = WechatManager.getInstance().getContactsId();
        for (int i = 0; i < WechatManager.SHOW_WECHAT_CONTRACTS_COUNT; i++) {
            if (i < contactsId.size()) {
                final CornerMaskImageView ivHead = fullViewHolder.ivContactsHeads.get(i);
                String contractId = contactsId.get(i);
                ivHead.setVisibility(View.VISIBLE);
                WechatManager.getInstance().getUsericon(contractId, new TXZWechatManagerV2.ImageListener() {
                    @Override
                    public void onImageReady(String id, String imgPath) {
                        ImgLoader.loadCircleImage(imgPath, ivHead);
                    }
                });
                if (WechatManager.getInstance().isContactNotify(contractId)) {
                    ivHead.clearCornerMask();
                } else {
                    ivHead.showCornerMaskAtRightBottom(R.drawable.ic_artboard, 9);
                }
            } else {
                fullViewHolder.ivContactsHeads.get(i).setVisibility(View.GONE);
            }
        }
    }

    private boolean hasEnoughWidth(String text, int textSize, int maxWidth) {
        Paint paint = new Paint();
        paint.setTextSize(textSize);
        return paint.measureText(text) < maxWidth;
    }

    @Override
    public void onCreate(String data) {
        super.onCreate(data);
        listener = new WechatManager.WechatStateListener() {
            @Override
            public void dismissNotify() {
                AppLogic.runOnUiGround(new Runnable() {
                    @Override
                    public void run() {
                        showNormalView();
                    }
                });
            }

            @Override
            public void updateNotify(WechatMsgData msgData) {
                AppLogic.runOnUiGround(new Runnable1<WechatMsgData>(msgData) {
                    @Override
                    public void run() {
                        showMessageView(mP1);
                    }
                });

            }

            @Override
            public void updateContacts() {
                AppLogic.runOnUiGround(new Runnable() {
                    @Override
                    public void run() {
                        showNormalView();
                    }
                });
            }
        };
        WechatManager.getInstance().addWechatStateListener(listener);
    }

    @Override
    public void onResume() {
        super.onResume();
        WechatManager.getInstance().addWechatStateListener(listener);
    }

    @Override
    public void onPreRemove() {
        super.onPreRemove();
        WechatManager.getInstance().removeWechatStateListener(listener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected void onEvent(String eventType) {
        super.onEvent(eventType);
        if (TextUtils.equals(eventType,EventTypes.EVENT_WX_LOGIN)) {
            AppLogic.runOnUiGround(new Runnable() {
                @Override
                public void run() {
                    showNormalView();
                }
            });
        }
    }

    @Override
    public String[] getObserverEventTypes() {
        return new String[] {EventTypes.EVENT_WX_LOGIN};
    }

    static class FullViewHolder {
        @Bind(R.id.tv_wechat_title)
        TextView tvWechatTitle;
        @Bind(R.id.iv_wechat_user_icon)
        ImageView ivWechatUserIcon;
        @Bind(R.id.tv_wechat_user_nick)
        TextView tvWechatUserNick;
        @Bind(R.id.ll_wechat_user)
        LinearLayout llWechatUser;
        @Bind(R.id.map_wechat_location)
        TextureMapView mapWechatLocation;
        @Bind(R.id.addr_ly)
        ViewGroup lyAddr;
        @Bind(R.id.distance_tv)
        TextView tvDistance;
        @Bind(R.id.address_tv)
        TextView tvAddress;
        @Bind({R.id.iv_contacts_head_0, R.id.iv_contacts_head_1, R.id.iv_contacts_head_2, R.id.iv_contacts_head_3, R.id.iv_contacts_head_4, R.id.iv_contacts_head_5, R.id.iv_contacts_head_6})
        List<CornerMaskImageView> ivContactsHeads;
        @Bind(R.id.ll_wechat_contacts)
        LinearLayout llWechatContacts;
        @Bind(R.id.iv_wechat_sender_icon)
        ImageView ivWechatSenderIcon;
        @Bind(R.id.tv_wechat_sender_nick)
        TextView tvWechatSenderNick;
        @Bind(R.id.tv_wechat_msg_answer)
        TextView tvWechatMsgAnswer;
        @Bind(R.id.tv_wechat_msg_nav)
        TextView tvWechatMsgNav;
        @Bind(R.id.fl_wechat_msg)
        FrameLayout flWechatMsg;

        FullViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    static class WechatViewHolder {
        @Bind(R.id.iv_wechat_user_icon)
        ImageView ivWechatUserIcon;
        @Bind(R.id.tv_wechat_user_nick)
        TextView tvWechatUserNick;
        @Bind(R.id.rl_wechat_user)
        PercentRelativeLayout rlWechatUser;
        @Bind(R.id.iv_wechat_icon)
        ImageView ivWechatIcon;
        @Bind(R.id.iv_wechat_sender_icon)
        ImageView ivWechatSenderIcon;
        @Bind(R.id.tv_wechat_sender_nick)
        TextView tvWechatSenderNick;
        @Bind(R.id.tv_wechat_msg_answer)
        TextView tvWechatMsgAnswer;
        @Bind(R.id.tv_wechat_msg_nav)
        TextView tvWechatMsgNav;
        @Bind(R.id.fl_msg_layout)
        FrameLayout flMsgLayout;

        WechatViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

}
