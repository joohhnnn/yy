package com.txznet.launcher.module.record;


import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.launcher.BuildConfig;
import com.txznet.launcher.R;
import com.txznet.launcher.domain.LaunchManager;
import com.txznet.launcher.domain.txz.RecordWinManager;
import com.txznet.launcher.domain.wechat.WechatManager;
import com.txznet.launcher.event.EventTypes;
import com.txznet.launcher.img.GlideApp;
import com.txznet.launcher.img.ImgLoader;
import com.txznet.launcher.module.BaseModule;
import com.txznet.launcher.module.record.bean.AudioListMsgData;
import com.txznet.launcher.module.record.bean.BaseListMsgData;
import com.txznet.launcher.module.record.bean.CallListMsgData;
import com.txznet.launcher.module.record.bean.CinemaListMsgData;
import com.txznet.launcher.module.record.bean.PoiListMsgData;
import com.txznet.launcher.module.record.bean.ReminderListMsgData;
import com.txznet.launcher.module.record.bean.SimListMsgData;
import com.txznet.launcher.module.record.bean.SimpleListMsgData;
import com.txznet.launcher.module.record.bean.TtsListMsgData;
import com.txznet.launcher.module.record.bean.WechatListMsgData;
import com.txznet.sdk.TXZWechatManagerV2;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 列表通用的Module
 * call_list
 * poi_list
 * wechat_list
 * audio_list
 * cinema_list
 * sim_list
 * tts_list
 * reminder_list
 */
public class ChatListModule extends BaseModule {
    //core中传过来标记列表类型的
    public static final int TYPE_CALL_LIST = 0;
    public static final int TYPE_WECHAT_LIST = 1;
    public static final int TYPE_POI_LIST = 2;
    public static final int TYPE_AUDIO_LIST = 4;
    public static final int TYPE_SIM_LIST = 5;
    public static final int TYPE_TTS_LIST = 6;
    public static final int TYPE_CINEMA_LIST = 7;
    public static final int TYPE_SIMPLE_LIST = 11;
    public static final int TYPE_REMINDER_LIST = 12;

    @Bind(R.id.ll_list_title)
    LinearLayout llListTitle;
    @Bind(R.id.tv_list_title)
    TextView tvListTitle;
    @Bind(R.id.tv_list_pre)
    TextView tvListPre;
    @Bind(R.id.tv_list_page)
    TextView tvListPage;
    @Bind(R.id.tv_list_next)
    TextView tvListNext;
    @Bind(R.id.fl_list_content)
    FrameLayout flListContent;
    private int mCurrentType = -1;
    private int mLastType = -1;
    private BaseListMsgData baseListMsgData;

    @OnClick(R.id.tv_list_pre)
    void onPrePageClick() {
        if (BuildConfig.ENABLE_TOUCH_EVENT) {
            ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.display.page", new JSONBuilder().put("type", 1).put("clicktype", 1).toBytes(), null);
        }
    }

    @OnClick(R.id.tv_list_next)
    void onNextPageClick() {
        if (BuildConfig.ENABLE_TOUCH_EVENT) {
            ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.display.page", new JSONBuilder().put("type", 1).put("clicktype", 2).toBytes(), null);
        }
    }

    @Override
    public void onCreate(String data) {
        super.onCreate(data);
        baseListMsgData = parseData(data);
    }

    @Override
    public void refreshView(String data) {
        super.refreshView(data);
        mLastType = mCurrentType;
        baseListMsgData = parseData(data);
        updateTitle();
        refreshContentView(flListContent.getContext());
    }

    @Override
    public View onCreateView(Context context, ViewGroup parent, int status) {
        View view = View.inflate(context, R.layout.module_record_list, null);

        ButterKnife.bind(this, view);

        updateTitle();

        refreshContentView(context);

        return view;
    }

    private void updateTitle() {
//        tvListTitle.setText(baseListMsgData.mTitleInfo.prefix);
        tvListPage.setText((baseListMsgData.mTitleInfo.curPage + 1) + "/" + baseListMsgData.mTitleInfo.maxPage);
    }

    /**
     * 注意事项，一个类型的List，添加到列表中的最外层View的个数需和列表的个数一致，如果有分割线的，将分割线加到各个itemView中
     *
     * @param context
     */
    private void refreshContentView(Context context) {
        LinearLayout view = null;
        boolean needAddView = mCurrentType != mLastType
                || flListContent.getChildAt(0) == null
                || ((ViewGroup) flListContent.getChildAt(0)).getChildCount() != baseListMsgData.mTitleInfo.count;
        if (needAddView) {
            flListContent.removeAllViews();
            view = new LinearLayout(context);
            view.setOrientation(LinearLayout.VERTICAL);

            LayoutAnimationController animationController = new LayoutAnimationController(AnimationUtils.loadAnimation(context, R.anim.anim_slide_up_in), 0.5f);
            view.setLayoutAnimation(animationController);

            flListContent.addView(view, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }else {
            view = (LinearLayout) flListContent.getChildAt(0);
        }
        switch (mCurrentType) {
            case TYPE_CALL_LIST:
                break;
            case TYPE_WECHAT_LIST:
                createWechatList(context, view, needAddView);
                break;
            case TYPE_POI_LIST:
                createPoiList(context, view, needAddView);
                break;
            case TYPE_AUDIO_LIST:
                createAudioList(context, view, needAddView);
                break;
            case TYPE_SIM_LIST:
                break;
            case TYPE_TTS_LIST:
                createTtsList(context, view, needAddView);
                break;
            case TYPE_CINEMA_LIST:
                break;
            case TYPE_REMINDER_LIST:
                break;
            case TYPE_SIMPLE_LIST:
                createSimpleList(context, view, needAddView);
                break;
        }
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                int index = (int) v.getTag();
                String data = new JSONBuilder().put("index", index)
                        .put("type", 1).toString();
                ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.item.selected",
                        data.getBytes(), null);
            }catch (Exception e) {

            }
        }
    };

    private void setOnItemClickListener(View item , int index){
        if (BuildConfig.ENABLE_TOUCH_EVENT) {
            item.setTag(index);
            item.setOnClickListener(onClickListener);
        }
    }

    private void createAudioList(Context context, LinearLayout parentView, boolean needAddView) {
        AudioListMsgData audioListMsgData = (AudioListMsgData) baseListMsgData;
        AudioListMsgData.AudioData audioData;
        for (int i = 0; i < audioListMsgData.mTitleInfo.count; i++) {
            audioData = audioListMsgData.mDatas.get(i);
            View item = createItemView(context, parentView, needAddView, R.layout.layout_record_audio_item, i);

            ((TextView) item.findViewById(R.id.tv_audio_num)).setText("" + (i + 1));
            ((TextView) item.findViewById(R.id.tv_audio_title)).setText(audioData.title);

            if (TextUtils.isEmpty(audioData.name)) {
                ((TextView) item.findViewById(R.id.tv_audio_singer)).setVisibility(View.GONE);
                ((TextView) item.findViewById(R.id.tv_audio_singer)).setText(null);
            } else {
                ((TextView) item.findViewById(R.id.tv_audio_singer)).setVisibility(View.VISIBLE);
                ((TextView) item.findViewById(R.id.tv_audio_singer)).setText(audioData.name);
            }


            setOnItemClickListener(item,i);
        }
    }

    private void createPoiList(Context context, LinearLayout parentView, boolean needAddView) {
        PoiListMsgData poiListMsgData = (PoiListMsgData) baseListMsgData;
        PoiListMsgData.PoiItem poiItem;
        for (int i = 0; i < poiListMsgData.mTitleInfo.count; i++) {
            poiItem = poiListMsgData.mDatas.get(i);
            View item = createItemView(context, parentView, needAddView, R.layout.layout_record_poi_item, i);

            ((TextView) item.findViewById(R.id.tv_poi_num)).setText("" + (i + 1));
            ((TextView) item.findViewById(R.id.tv_poi_name)).setText(poiItem.item.getName());
            TextView poiGeo = (TextView) item.findViewById(R.id.tv_poi_geo);
            poiGeo.setText(poiItem.item.getGeoinfo());
            poiGeo.setVisibility(TextUtils.isEmpty(poiItem.item.getGeoinfo()) ? View.GONE : View.VISIBLE);
            ((TextView) item.findViewById(R.id.tv_poi_distance)).setText(String.format("%.1f", poiItem.item.getDistance() / 1000.0) + "km");

            setOnItemClickListener(item,i);
        }
    }

    private void createWechatList(Context context, LinearLayout parentView, boolean needAddView) {
        WechatListMsgData wechatListMsgData = (WechatListMsgData) baseListMsgData;
        WechatListMsgData.WechatData wechatData;
        for (int i = 0; i < wechatListMsgData.mTitleInfo.count; i++) {
            wechatData = wechatListMsgData.mDatas.get(i);
            View item = createItemView(context, parentView, needAddView, R.layout.layout_record_wechat_item, i);

            ((TextView) item.findViewById(R.id.tv_wechat_num)).setText("" + (i + 1));
            ((TextView) item.findViewById(R.id.tv_wechat_name)).setText(wechatData.name);
            final ImageView ivWechatHead = (ImageView) item.findViewById(R.id.iv_wechat_head);
            GlideApp.with(GlobalContext.get()).load(R.drawable.ic_wechat_default_head).apply(RequestOptions.bitmapTransform(new CircleCrop())).into(ivWechatHead);
            WechatManager.getInstance().getUsericon(wechatData.id, new TXZWechatManagerV2.ImageListener() {
                @Override
                public void onImageReady(String id, String imgPath) {
                    if (ivWechatHead != null) {
                        File header = new File(imgPath);
                        if (header.exists()) {
                            ImgLoader.loadCircleImage("file://" + header.getAbsolutePath(), ivWechatHead);
                        }
                    }
                }
            });

            setOnItemClickListener(item,i);
        }
    }

    private void createSimpleList(Context context, LinearLayout parentView, boolean needAddView) {
        SimpleListMsgData simpleListMsgData = (SimpleListMsgData) baseListMsgData;
        SimpleListMsgData.SimpleData simpleData;
        for (int i = 0; i < simpleListMsgData.mTitleInfo.count; i++) {
            simpleData = simpleListMsgData.mDatas.get(i);
            View item = createItemView(context, parentView, needAddView, R.layout.layout_record_simple_item, i);

            ((TextView) item.findViewById(R.id.tv_simple_num)).setText("" + (i + 1));
            ((TextView) item.findViewById(R.id.tv_simple_title)).setText(simpleData.title);

            setOnItemClickListener(item,i);

        }
    }

    private void createTtsList(Context context, LinearLayout parentView, boolean needAddView) {
        TtsListMsgData ttsListMsgData = (TtsListMsgData) baseListMsgData;
        TtsListMsgData.TtsData ttsData;
        for (int i = 0; i < ttsListMsgData.mTitleInfo.count; i++) {
            ttsData = ttsListMsgData.mDatas.get(i);
            View item = createItemView(context, parentView, needAddView, R.layout.layout_record_simple_item, i);

            ((TextView) item.findViewById(R.id.tv_simple_num)).setText("" + (i + 1));
            ((TextView) item.findViewById(R.id.tv_simple_title)).setText(ttsData.name);

            setOnItemClickListener(item,i);

        }
    }

    private View createItemView(Context context, LinearLayout parentView, boolean needAddView, int layoutId, int position) {
        View item;
        if (needAddView) {
            item = View.inflate(context, layoutId, null);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 60);
            layoutParams.topMargin = 8;
            parentView.addView(item, layoutParams);
        } else {
            item = parentView.getChildAt(position);
        }
        return item;
    }


    private BaseListMsgData parseData(String data) {
        long start = System.currentTimeMillis();


        BaseListMsgData baseListData = null;
        JSONBuilder jsData = new JSONBuilder(data);
        mCurrentType = jsData.getVal("type", Integer.class);
        switch (mCurrentType) {
            case TYPE_CALL_LIST:
                baseListData = new CallListMsgData();
                baseListData.parseData(jsData);
                break;
            case TYPE_WECHAT_LIST:
                baseListData = new WechatListMsgData();
                baseListData.parseData(jsData);
                break;
            case TYPE_POI_LIST:
                baseListData = new PoiListMsgData();
                baseListData.parseData(jsData);
                break;
            case TYPE_AUDIO_LIST:
                baseListData = new AudioListMsgData();
                baseListData.parseData(jsData);
                break;


            case TYPE_SIM_LIST:
                baseListData = new SimListMsgData();
                baseListData.parseData(jsData);
                break;
            case TYPE_TTS_LIST:
                baseListData = new TtsListMsgData();
                baseListData.parseData(jsData);
                break;
            case TYPE_CINEMA_LIST:
                baseListData = new CinemaListMsgData();
                baseListData.parseData(jsData);
                break;
            case TYPE_REMINDER_LIST:
                baseListData = new ReminderListMsgData();
                baseListData.parseData(jsData);
                break;
            case TYPE_SIMPLE_LIST:
                baseListData = new SimpleListMsgData();
                baseListData.parseData(jsData);
                break;
        }

        return baseListData;
    }


    @Override
    public String[] getObserverEventTypes() {
        return new String[]{EventTypes.EVENT_WX_LOGOUT};
    }

    @Override
    protected void onEvent(String eventType) {
        super.onEvent(eventType);
        if (TextUtils.equals(eventType,EventTypes.EVENT_WX_LOGOUT)) {
            if (mCurrentType == TYPE_WECHAT_LIST) {
                RecordWinManager.getInstance().ctrlRecordWinDismiss();
            }
        }
    }
}
