package com.txznet.music.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.flyco.tablayout.CommonTabLayout;
import com.flyco.tablayout.listener.CustomTabEntity;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.comm.util.NetworkUtil;
import com.txznet.comm.util.StringUtils;
import com.txznet.loader.AppLogic;
import com.txznet.music.Constant;
import com.txznet.music.GlideApp;
import com.txznet.music.R;
import com.txznet.music.action.LocalActionCreator;
import com.txznet.music.action.PageActionCreator;
import com.txznet.music.action.PlayerActionCreator;
import com.txznet.music.data.entity.Album;
import com.txznet.music.data.entity.AudioV5;
import com.txznet.music.data.entity.PlayScene;
import com.txznet.music.data.sp.SharedPreferencesUtils;
import com.txznet.music.helper.DrawablePool;
import com.txznet.music.helper.GlideHelper;
import com.txznet.music.helper.LottieHelper;
import com.txznet.music.helper.PlayHelper;
import com.txznet.music.helper.PlayerConfigHelper;
import com.txznet.music.helper.PushLogicHelper;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.report.entity.SysExitEvent;
import com.txznet.music.report.entity.SysOpenEvent;
import com.txznet.music.report.entity.SysPageSlideEvent;
import com.txznet.music.service.impl.PushCommand;
import com.txznet.music.service.push.AppPushInvoker;
import com.txznet.music.store.AlbumStore;
import com.txznet.music.store.HomePageStore;
import com.txznet.music.store.LocalAudioStore;
import com.txznet.music.store.PlayInfoStore;
import com.txznet.music.ui.base.BaseActivity;
import com.txznet.music.ui.base.DialogFragmentStack;
import com.txznet.music.ui.player.PlayerFragment;
import com.txznet.music.ui.splash.SplashFragment;
import com.txznet.music.ui.tab.BaseTab;
import com.txznet.music.ui.tab.MusicCategoryTab;
import com.txznet.music.ui.tab.MusicChoiceTab;
import com.txznet.music.ui.tab.RadioCategoryTab;
import com.txznet.music.ui.tab.RadioChoiceTab;
import com.txznet.music.ui.tab.RecFirstTab;
import com.txznet.music.ui.tab.RecSecondTab;
import com.txznet.music.util.AlbumUtils;
import com.txznet.music.util.DimenUtils;
import com.txznet.music.util.NetWorkObservable;
import com.txznet.music.util.PlaySceneUtils;
import com.txznet.music.util.SnappedLinearLayoutManager;
import com.txznet.music.util.ToastUtils;
import com.txznet.music.util.Utils;
import com.txznet.music.widget.GravityPagerSnapHelper;
import com.txznet.music.widget.dialog.ExitAppDialog;
import com.txznet.rxflux.Operation;
import com.txznet.rxflux.extensions.aac.ViewModelProviders;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.OnClick;

import static com.txznet.music.report.ReportEventProtocol.SYS_PAGE_MUSIC_CLICK;
import static com.txznet.music.report.ReportEventProtocol.SYS_PAGE_RADIO_CLICK;
import static com.txznet.music.report.ReportEventProtocol.SYS_PAGE_RECOMMAND_CLICK;

/**
 * 主界面
 *
 * @author zackzhou
 * @date 2018/12/3,11:33
 */
public class HomeActivity extends BaseActivity implements IDelayLaunch {

    @Bind(R.id.ll_content)
    ViewGroup mContent;

    @Bind(R.id.tl_tab)
    CommonTabLayout tlTab;

    @Bind(R.id.rv_data)
    RecyclerView rvData;

    @Bind(R.id.img_logo)
    ImageView imgLogo;
    @Bind(R.id.tv_name)
    TextView tvName;

    @Bind(R.id.img_logo_loading)
    LottieAnimationView imgLogoLoading;
    @Bind(R.id.img_logo_play)
    ImageView imgLogoPlay;
    @Bind(R.id.iv_close)
    ImageView ivClose;

    private RecFirstTab mRecFirstTab;
    private RecSecondTab mRecSecondTab;
    private MusicChoiceTab mMusicChoiceTab;
    private MusicCategoryTab mMusicCategoryTab;
    private RadioChoiceTab mRadioChoiceTab;
    private RadioCategoryTab mRadioCategoryTab;

    private boolean isRecTabLoaded, isMusicTabLoaded, isRadioTabLoaded;
    private ExitAppDialog mExitAppDialog;

    @SysPageSlideEvent.PageId
    private int mLastPageId = SysPageSlideEvent.PAGE_ID_RECOMMEND_FIRST;

    private NetWorkObservable.NetWorkObserver mNetWorkObserver;

    private boolean isInit;

    @Override
    protected String getActivityTag() {
        return "HomeActivity";
    }

    @Override
    public int getLayout() {
        return R.layout.home_activity;
    }

    @Override
    public void initView() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        if (TXZFileConfigUtil.getBooleanSingleConfig(Configuration.Key.HARDWARE_ACCELERATED, true)) {
//            getWindow().setFlags(
//                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
//                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
//        }
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

        if (!this.isTaskRoot() && getIntent() != null) {
            String action = getIntent().getAction();
            if (getIntent().hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(action)) {
                finish();
                return;
            }
        }

        SplashFragment splashFrag = new SplashFragment();
        splashFrag.setCancelable(false);
        splashFrag.showNow(getSupportFragmentManager(), "Splash");

        AppLogic.runOnUiGround(() -> {

            //需求：需要在打开界面之后就不弹界面了
            PushCommand.getInstance().stopShowTask();
            AppPushInvoker.getInstance().release();
            PushLogicHelper.getInstance().setAppOpened(true);
            SharedPreferencesUtils.setShowWindowView(true);

            mNetWorkObserver = connected -> {
                if (connected) {
                    isRecTabLoaded = false;
                    isMusicTabLoaded = false;
                    isRadioTabLoaded = false;
                }
                if (tlTab != null) {
                    int currTab = tlTab.getCurrentTab();
                    switch (currTab) {
                        case 0:
                            if (!isRecTabLoaded) {
                                notifyRefreshPage(PAGE_ID_REC, false);
                            }
                            break;
                        case 1:
                            if (!isMusicTabLoaded) {
                                notifyRefreshPage(PAGE_ID_MUSIC, false);
                            }
                            break;
                        case 2:
                            if (!isRadioTabLoaded) {
                                notifyRefreshPage(PAGE_ID_RADIO, false);
                            }
                            break;
                    }
                }
            };

            try {
                NetWorkObservable.get().registerObserver(mNetWorkObserver);
            } catch (Exception e) {
            }
        });
    }

    private void init() {
        isInit = true;

        GlideApp.with(GlobalContext.get()).pauseAllRequests();
        Glide.get(GlobalContext.get()).clearMemory();

        initViewInner();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initViewInner() {
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("pkgName", "com.txznet.music");

        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "comm.applets.set", jsonBuilder.toBytes(), null);

        imgLogoLoading.setImageAssetDelegate(asset -> {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.outWidth = asset.getWidth();
            options.outHeight = asset.getHeight();
            return BitmapFactory.decodeResource(getResources(), R.drawable.img_0, options);
        });
        initTabTitle();

        mRecFirstTab = new RecFirstTab(this, getSupportFragmentManager());
        mRecSecondTab = new RecSecondTab(this, getSupportFragmentManager());
        mMusicChoiceTab = new MusicChoiceTab(this);
        mMusicCategoryTab = new MusicCategoryTab(this);
        mRadioChoiceTab = new RadioChoiceTab(this, getSupportFragmentManager());
        mRadioCategoryTab = new RadioCategoryTab(this, getSupportFragmentManager());

        SnappedLinearLayoutManager layoutManager = new SnappedLinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvData.setLayoutManager(layoutManager);

        RecyclerView.Adapter adapter = new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                switch (viewType) {
                    case 0:
                        return new RecyclerView.ViewHolder(LayoutInflater.from(HomeActivity.this).inflate(R.layout.home_include_recommend_page_1, parent, false)) {
                        };
                    case 1:
                        return new RecyclerView.ViewHolder(LayoutInflater.from(HomeActivity.this).inflate(R.layout.home_include_recommend_page_2, parent, false)) {
                        };
                    case 2:
                        return new RecyclerView.ViewHolder(LayoutInflater.from(HomeActivity.this).inflate(R.layout.home_include_music_page_1, parent, false)) {
                        };
                    case 3:
                        return new RecyclerView.ViewHolder(LayoutInflater.from(HomeActivity.this).inflate(R.layout.home_include_music_page_2, parent, false)) {
                        };
                    case 4:
                        return new RecyclerView.ViewHolder(LayoutInflater.from(HomeActivity.this).inflate(R.layout.home_include_radio_page_1, parent, false)) {
                        };
                    case 5:
                        return new RecyclerView.ViewHolder(LayoutInflater.from(HomeActivity.this).inflate(R.layout.home_include_radio_page_2, parent, false)) {
                        };
                    default:
                        break;
                }
                return null;
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                switch (position) {
                    case 0:
                        mRecFirstTab.onBindViewHolder(holder);
                        break;
                    case 1:
                        mRecSecondTab.onBindViewHolder(holder);
                        break;
                    case 2:
                        mMusicChoiceTab.onBindViewHolder(holder);
                        break;
                    case 3:
                        mMusicCategoryTab.onBindViewHolder(holder);
                        break;
                    case 4:
                        mRadioChoiceTab.onBindViewHolder(holder);
                        break;
                    case 5:
                        mRadioCategoryTab.onBindViewHolder(holder);
                        break;
                }
            }

            @Override
            public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
            }

            @Override
            public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
                int position = holder.getAdapterPosition();
                switch (position) {
                    case 0:
                        mRecFirstTab.onViewAttachedToWindow(true);
                        break;
                    case 1:
                        mRecSecondTab.onViewAttachedToWindow(true);
                        break;
                    case 2:
                        mMusicChoiceTab.onViewAttachedToWindow(true);
                        break;
                    case 3:
                        mMusicCategoryTab.onViewAttachedToWindow(true);
                        break;
                    case 4:
                        mRadioChoiceTab.onViewAttachedToWindow(true);
                        break;
                    case 5:
                        mRadioCategoryTab.onViewAttachedToWindow(true);
                        break;
                }
            }

            @Override
            public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
                int position = holder.getAdapterPosition();
                switch (position) {
                    case 0:
                        mRecFirstTab.onViewDetachedFromWindow(true, false);
                        break;
                    case 1:
                        mRecSecondTab.onViewDetachedFromWindow(true, false);
                        break;
                    case 2:
                        mMusicChoiceTab.onViewDetachedFromWindow(true, false);
                        break;
                    case 3:
                        mMusicCategoryTab.onViewDetachedFromWindow(true, false);
                        break;
                    case 4:
                        mRadioChoiceTab.onViewDetachedFromWindow(true, false);
                        break;
                    case 5:
                        mRadioCategoryTab.onViewDetachedFromWindow(true, false);
                        break;
                }
            }

            @Override
            public int getItemViewType(int position) {
                return position;
            }

            @Override
            public int getItemCount() {
                return 6;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }
        };
        adapter.setHasStableIds(true);
        rvData.setHasFixedSize(true);
        rvData.setAdapter(adapter);
        rvData.setItemAnimator(null);

        // 滑动监听
        GravityPagerSnapHelper pageSnapHelper = new GravityPagerSnapHelper(Gravity.START, false, null);
        pageSnapHelper.attachToRecyclerView(rvData);

        rvData.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (RecyclerView.SCROLL_STATE_IDLE == newState) {
                    GlideApp.with(HomeActivity.this).resumeRequestsRecursive();
                    mRefreshTask.run();
                } else if (RecyclerView.SCROLL_STATE_DRAGGING == newState) {
                    // FIXME: 2019/4/12 从电台快速滑动到推荐，收到的状态会异常，追加一个延迟刷新任务
                    AppLogic.removeUiGroundCallback(mRefreshTask);
                    AppLogic.runOnUiGround(mRefreshTask, 500);
                } else {
                    GlideApp.with(HomeActivity.this).pauseRequestsRecursive();
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });


        mOnStackChangeListener = this::onDialogFragmentStackChanged;

        // 界面监听
        DialogFragmentStack.get().setOnStackChangeListener(mOnStackChangeListener);

        initData();
    }

    private DialogFragmentStack.OnStackChangeListener mOnStackChangeListener;

    // 当界面发生改变时
    private void onDialogFragmentStackChanged() {
        if (DialogFragmentStack.get().isEmpty()) {
            resumeCurrentTab(false);
            tvName.requestFocus();
        } else {
            AppLogic.removeUiGroundCallback(mRecycleTask);
            AppLogic.runOnUiGround(mRecycleTask, 1000);
        }
    }

    private Runnable mRecycleTask = () -> {
        if (!DialogFragmentStack.get().isEmpty()) {
            recycleTab(false, false);
            if (tvName != null) {
                tvName.clearFocus();
            }
        }
    };

    private Runnable mRefreshTask = () -> {
        int position = ((LinearLayoutManager) rvData.getLayoutManager()).findFirstVisibleItemPosition();
        tlTab.setCurrentTab(position / 2);
        int newPageId = SysPageSlideEvent.PAGE_ID_RECOMMEND_FIRST;
        switch (position) {
            case 0:
                newPageId = SysPageSlideEvent.PAGE_ID_RECOMMEND_FIRST;
                if (!isRecTabLoaded) {
                    notifyRefreshPage(PAGE_ID_REC, false);
                }
                break;
            case 1:
                newPageId = SysPageSlideEvent.PAGE_ID_RECOMMEND_SECOND;
                if (!isRecTabLoaded) {
                    notifyRefreshPage(PAGE_ID_REC, false);
                }
                break;
            case 2:
                newPageId = SysPageSlideEvent.PAGE_ID_MUSIC_CHOICE;
                if (!isMusicTabLoaded) {
                    notifyRefreshPage(PAGE_ID_MUSIC, false);
                }
                break;
            case 3:
                newPageId = SysPageSlideEvent.PAGE_ID_MUSIC_CATEGORY;
                if (!isMusicTabLoaded) {
                    notifyRefreshPage(PAGE_ID_MUSIC, false);
                }
                break;
            case 4:
                newPageId = SysPageSlideEvent.PAGE_ID_RADIO_CHOICE;
                if (!isRadioTabLoaded) {
                    notifyRefreshPage(PAGE_ID_RADIO, false);
                }
                break;
            case 5:
                newPageId = SysPageSlideEvent.PAGE_ID_RADIO_CATEGORY;
                if (!isRadioTabLoaded) {
                    notifyRefreshPage(PAGE_ID_RADIO, false);
                }
                break;
        }
        if (mLastPageId != newPageId) {
            ReportEvent.reportPageSlide(newPageId, mLastPageId);
            mLastPageId = newPageId;
        }
        GlideApp.with(HomeActivity.this).resumeRequestsRecursive();
    };


    // 播放信息为空
    private void onPlayInfoEmpty() {
        imgLogoLoading.setVisibility(View.GONE);
        imgLogoLoading.cancelAnimation();
        imgLogoPlay.setVisibility(View.VISIBLE);
        imgLogoPlay.setImageResource(R.drawable.home_player_play_btn_icon);
        tvName.setText("暂无内容");
        tvName.setTextColor(getResources().getColor(R.color.white_40));
        if (mPlayInfoStore.getAlbum().getValue() == null) {
            imgLogo.setImageResource(R.drawable.home_default_cover_icon);
        }
    }

    // 播放中
    private void onPlaying() {
        imgLogoLoading.setVisibility(View.GONE);
        imgLogoLoading.cancelAnimation();
        imgLogoPlay.setVisibility(View.VISIBLE);
        imgLogoPlay.setImageResource(R.drawable.home_player_pause_btn_icon);
    }

    // 播放缓冲中
    private void onPlayBuffering() {
        imgLogoLoading.setVisibility(View.VISIBLE);
        if (!imgLogoLoading.isAnimating()) {
            imgLogoLoading.playAnimation();
        }
        imgLogoPlay.setVisibility(View.GONE);
        if (mPlayInfoStore.getCurrPlaying().getValue() == null) {
            tvName.setText("加载中");
        }
    }

    // 播放暂停
    private void onPlayPause() {
        if ("加载中".equals(tvName.getText().toString())) {
            imgLogoLoading.setVisibility(View.VISIBLE);
            if (!imgLogoLoading.isAnimating()) {
                imgLogoLoading.playAnimation();
            }
            imgLogoPlay.setVisibility(View.GONE);
        } else {
            imgLogoLoading.setVisibility(View.GONE);
            imgLogoLoading.cancelAnimation();
            imgLogoPlay.setVisibility(View.VISIBLE);
            imgLogoPlay.setImageResource(R.drawable.home_player_play_btn_icon);
        }
    }

    private void initTabTitle() {
        final String[] TITLES = {"推荐", "音乐", "电台"};
        ArrayList<CustomTabEntity> customTabEntityList = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            final int index = i;
            CustomTabEntity tabEntity = new CustomTabEntity() {
                @Override
                public String getTabTitle() {
                    return TITLES[index];
                }

                @Override
                public int getTabSelectedIcon() {
                    return 0;
                }

                @Override
                public int getTabUnselectedIcon() {
                    return 0;
                }
            };
            customTabEntityList.add(tabEntity);
        }
        tlTab.setTextSelectColor(getResources().getColor(R.color.red));
        tlTab.setTextUnselectColor(getResources().getColor(R.color.white_50));
        tlTab.setUnderlineColor(getResources().getColor(R.color.red));
        tlTab.setIndicatorColor(getResources().getColor(R.color.red));
        tlTab.setIndicatorWidth(DimenUtils.px2dip(this, getResources().getDimension(R.dimen.m22)));
        tlTab.setIndicatorHeight(DimenUtils.px2dip(this, getResources().getDimension(R.dimen.m3)));
        tlTab.setTextsize(DimenUtils.px2sp(this, getResources().getDimension(R.dimen.base_tv_h1)));
        tlTab.setIndicatorCornerRadius(getResources().getDimension(R.dimen.m1));
        tlTab.setTabData(customTabEntityList);

        // tap点击事件
        tlTab.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
//                rvData.getLayoutManager().smoothScrollToPosition(rvData, null, position * 2);

                ((LinearLayoutManager) rvData.getLayoutManager()).scrollToPositionWithOffset(position * 2, 0);
                switch (position) {
                    case 0:
                        ReportEvent.reportPageClick(SYS_PAGE_RECOMMAND_CLICK);
                        if (!isRecTabLoaded) {
                            PageActionCreator.get().getRecommendPage();
                        }
                        break;
                    case 1:
                        ReportEvent.reportPageClick(SYS_PAGE_MUSIC_CLICK);
                        if (!isMusicTabLoaded) {
                            PageActionCreator.get().getMusicPage();
                        }
                        break;
                    case 2:
                        ReportEvent.reportPageClick(SYS_PAGE_RADIO_CLICK);
                        if (!isRadioTabLoaded) {
                            PageActionCreator.get().getRadioPage();
                        }
                        break;
                }
            }

            @Override
            public void onTabReselect(int position) {
                switch (position) {
                    case 0:
                        ReportEvent.reportPageClick(SYS_PAGE_RECOMMAND_CLICK);
                        break;
                    case 1:
                        ReportEvent.reportPageClick(SYS_PAGE_MUSIC_CLICK);
                        break;
                    case 2:
                        ReportEvent.reportPageClick(SYS_PAGE_RADIO_CLICK);
                        break;
                }
            }
        });
    }

    private PlayInfoStore mPlayInfoStore;
    private boolean hasTipLoadFailed;
    private int mLastAudioSid = -1;
    private long mLastAudioId = -1;

    private void refreshPlayerBarTitle(AudioV5 audio) {
        if (audio == null || audio.name == null) {
            return;
        }
        tvName.setTextColor(Color.WHITE);
        String subTitle;
        if (PlaySceneUtils.isMusicScene()) {
            subTitle = StringUtils.toString(audio.artist);
        } else {
            if (PlayHelper.get().getCurrAlbum() == null) {
                subTitle = "Unknown";
            } else {
                subTitle = PlayHelper.get().getCurrAlbum().name;
            }
        }
        if ("Unknown".equals(subTitle)) {
            tvName.setText(audio.name);
        } else {
            if (TextUtils.isEmpty(subTitle) && AlbumUtils.isAiRadio(PlayHelper.get().getCurrAlbum())) {
                subTitle = "AI电台";
            }
            tvName.setText(com.txznet.music.util.StringUtils.getTitleForAudioNameWithArtists(audio.name, subTitle));
        }

        if (DialogFragmentStack.get().isEmpty()) {
            tvName.requestFocus();
        }
    }

    private void initData() {
        // 播放条
        mPlayInfoStore = ViewModelProviders.of(this).get(PlayInfoStore.class);
        mPlayInfoStore.getCurrPlaying().observe(this, audio -> {
            if (audio == null || audio.name == null) {
                onPlayInfoEmpty();
            } else {
                if (audio.sid != mLastAudioSid && audio.id != mLastAudioId) {
                    mLastAudioSid = audio.sid;
                    mLastAudioId = audio.id;
                    onPlayBuffering();
                }
                refreshPlayerBarTitle(audio);
            }
        });

        mPlayInfoStore.getAlbum().observe(this, album -> {
            if (album != null) {
                if (!TextUtils.isEmpty(album.logo)) {
                    GlideHelper.loadWithCorners(HomeActivity.this, album.logo, R.drawable.home_default_cover_icon, imgLogo);
                }
                if (PlayHelper.get().getCurrAudio() == null) {
                    tvName.setText("加载中");
                    onPlayBuffering();
                }
            } else {
                GlideHelper.clear(HomeActivity.this, imgLogo);
                imgLogo.setImageResource(R.drawable.home_default_cover_icon);
            }
        });

        mPlayInfoStore.isPlayingStrict().observe(this, isPlaying -> {
            if (isPlaying == null || !isPlaying) {
                onPlayPause();
            } else {
                onPlaying();
            }
        });
        mPlayInfoStore.isPlaying().observe(this, isPlaying -> {
            if (PlayScene.LOCAL_MUSIC == PlayHelper.get().getCurrPlayScene()) {
                if (isPlaying == null || !isPlaying) {
                    mRecFirstTab.notifyLocalPaused();
                } else {
                    mRecFirstTab.notifyLocalPlaying();
                }
            } else {
                mRecFirstTab.notifyLocalPaused();
            }
        });

        mPlayInfoStore.isBuffering().observe(this, isBuffering -> {
            if (mPlayInfoStore.getCurrPlaying().getValue() != null && isBuffering != null && isBuffering) {
                onPlayBuffering();
            }
        });

        HomePageStore pageStore = ViewModelProviders.of(this).get(HomePageStore.class);
        pageStore.getRecPageData().observe(this, recommendPageData -> {
            if (recommendPageData != null) {
                if (recommendPageData.userRadioAlbum != null
                        && recommendPageData.userMusicAlbum != null
                        && recommendPageData.dailyRecAlbum != null
                        && recommendPageData.aiAlbum != null
                        && recommendPageData.commRecAlbums != null
                        && recommendPageData.billboard != null) {
                    isRecTabLoaded = true;
                }

                mRecFirstTab.refreshData(recommendPageData.commRecAlbums);
                mRecFirstTab.refreshAiData(recommendPageData.aiAlbum);
                // AI电台，级联刷新一下当前播放栏图标
                if (recommendPageData.aiAlbum != null && recommendPageData.aiAlbum.logo != null) {
                    Album album = mPlayInfoStore.getAlbum().getValue();
                    if (AlbumUtils.isAiRadio(album)) {
                        album.logo = recommendPageData.aiAlbum.logo;
                        if (recommendPageData.aiAlbum.name != null) {
                            album.name = recommendPageData.aiAlbum.name;
                        }
                        // 刷新状态栏
                        GlideHelper.loadWithCorners(HomeActivity.this, recommendPageData.aiAlbum.logo, R.drawable.home_default_cover_icon, imgLogo);
                        refreshPlayerBarTitle(mPlayInfoStore.getCurrPlaying().getValue());
                    }
                }
                mRecFirstTab.refreshDailyRec(recommendPageData.dailyRecAlbum);
                // 每日推荐，级联刷新一下当前播放栏图标
                if (recommendPageData.dailyRecAlbum != null && recommendPageData.dailyRecAlbum.logo != null) {
                    Album album = mPlayInfoStore.getAlbum().getValue();
                    if (AlbumUtils.isRecommend(album)) {
                        album.logo = recommendPageData.dailyRecAlbum.logo;
                        GlideHelper.loadWithCorners(HomeActivity.this, recommendPageData.dailyRecAlbum.logo, R.drawable.home_default_cover_icon, imgLogo);
                    }
                }

                mRecSecondTab.refreshData(recommendPageData.commRecAlbums);
                mRecSecondTab.refreshBillboard(recommendPageData.billboard);
                mRecSecondTab.refreshUserRadio(recommendPageData.userRadioAlbum);
                mRecSecondTab.refreshUserMusic(recommendPageData.userMusicAlbum);
            }
        });
        pageStore.getMusicPageData().observe(this, musicPageData -> {
            if (musicPageData != null) {
                if (musicPageData.choiceData != null
                        && musicPageData.categoryData != null) {
                    isMusicTabLoaded = true;
                }

                mMusicChoiceTab.refreshData(musicPageData.choiceData);
                mMusicCategoryTab.refreshData(musicPageData.categoryData);
            }
        });
        pageStore.getRadioPageData().observe(this, radioPageData -> {
            if (radioPageData != null) {
                if (radioPageData.billboard != null
                        && radioPageData.categoryList != null
                        && radioPageData.choiceList != null) {
                    isRadioTabLoaded = true;
                }

                mRadioChoiceTab.refreshBillboard(radioPageData.billboard);
                mRadioChoiceTab.refreshData(radioPageData.choiceList);
                mRadioCategoryTab.refreshData(radioPageData.categoryList);
            }
        });
        pageStore.getStatus().observe(this, status -> {
            notifyRefreshCurrPageDelay();
            if (!hasTipLoadFailed) {
                hasTipLoadFailed = true;
                if (NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
                    ToastUtils.showShortOnUI(Constant.RS_VOICE_SPEAK_ASR_NET_POOR);
                } else {
                    ToastUtils.showShortOnUI(Constant.RS_VOICE_SPEAK_ASR_NET_OFFLINE);
                }
            }
        });

        AlbumStore albumStore = ViewModelProviders.of(this).get(AlbumStore.class);
        albumStore.getPageItemDataGroup().observe(this, pageItemDataGroup -> {
            mMusicChoiceTab.dispatchRefreshData(pageItemDataGroup);
            mMusicCategoryTab.dispatchRefreshData(pageItemDataGroup);
        });
        albumStore.getStatusLiveEvent().observe(this, status -> {
            if (DialogFragmentStack.get().isEmpty()) {
                if (AlbumStore.Status.NO_NET == status && !hasTipLoadFailed) {
                    hasTipLoadFailed = true;
                    ToastUtils.showShortOnUI(Constant.RS_VOICE_SPEAK_ASR_NET_OFFLINE);
                }
            }
        });

        PageActionCreator.get().getRecommendPage();
        PlayerActionCreator.get().getPlayInfo(Operation.AUTO);

        ViewModelProviders.of(this).get(LocalAudioStore.class);
        LocalActionCreator.get().getLocalAudio(Operation.AUTO);
    }

    @OnClick({R.id.img_logo, R.id.tv_name, R.id.iv_close})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_logo:
            case R.id.tv_name:
                new PlayerFragment().show(getSupportFragmentManager(), "Player");
                PlayerActionCreator.get().play(Operation.AUTO);
                break;
            case R.id.iv_close:
                //退出界面
                if (mExitAppDialog == null) {
                    mExitAppDialog = new ExitAppDialog(this);
                    mExitAppDialog.setClickCallback(new ExitAppDialog.ExitAppCallback(this, mExitAppDialog));
                }
                mExitAppDialog.show();
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed");
        Utils.onBackRun(this);
        ReportEvent.reportExit(SysExitEvent.EXIT_TYPE_MANUAL);
    }


    @Override
    public void initContentView() {
        super.initContentView();
        if (mContent != null) {
            mContent.setBackgroundResource(R.drawable.base_bg);
        }
        if (ivClose != null) {
            ivClose.setImageResource(R.drawable.base_close_btn);
        }
    }

    @Override
    public void doLaunch() {
        init();
        int resumePlayType = getIntent() == null ? 0 : getIntent().getIntExtra("type", 0);
        switch (resumePlayType) {
            case 0:
                PlayerActionCreator.get().play(Operation.AUTO);
                break;
            case 1:
                PlayerActionCreator.get().playMusic(Operation.SOUND);
                break;
            case 2:
                PlayerActionCreator.get().playRadio(Operation.SOUND);
                break;
        }
    }

    private boolean bBackHomeAfterResume;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.hasExtra("type")) {
            int type = intent.getIntExtra("type", -1);
            switch (type) {
                case 0:
                    PlayerActionCreator.get().play(Operation.AUTO);
                    break;
                case 1:
                    PlayerActionCreator.get().playMusic(Operation.SOUND);
                    break;
                case 2:
                    PlayerActionCreator.get().playRadio(Operation.SOUND);
                    break;
            }
            if (type != -1) {
                bBackHomeAfterResume = true;
                intent.removeExtra("type");
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        AppLogic.runOnUiGround(() -> {
            ReportEvent.reportEnter(SysOpenEvent.ENTRY_TYPE_MANUAL);
            if (isInit) {
                if (DialogFragmentStack.get().isEmpty()) {
                    resumeCurrentTab(true);
                } else {
                    resumeCurrentTab(true);
                }
            }
            if (mContent != null) {
                mContent.setBackgroundResource(R.drawable.base_bg);
            }
            if (ivClose != null) {
                ivClose.setImageResource(R.drawable.base_close_btn);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppLogic.runOnUiGround(() -> {
            PlayerConfigHelper.get().resumeMediaPlayback();
            if (bBackHomeAfterResume) {
                DialogFragmentStack.get().exit();
                try {
                    if (tlTab != null && rvData != null) {
                        tlTab.setCurrentTab(0);
                        ((LinearLayoutManager) rvData.getLayoutManager()).scrollToPositionWithOffset(0, 0);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            bBackHomeAfterResume = false;
        });
    }

    @Override
    protected void onStop() {
        AppLogic.removeUiGroundCallback(mRefreshTask);
        recycleTab(true, true);

        GlideApp.with(this).pauseRequestsRecursive();
        GlideApp.with(this).pauseAllRequests();
        Glide.get(GlobalContext.get()).clearMemory();
        DrawablePool.clear();

        if (mContent != null) {
            mContent.setBackground(null);
        }
        if (ivClose != null) {
            ivClose.setImageDrawable(null);
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        AppLogic.removeUiGroundCallback(mRefreshCurrPageTask);
        try {
            if (mNetWorkObserver != null) {
                NetWorkObservable.get().unregisterObserver(mNetWorkObserver);
            }
        } catch (Exception e) {
        }
        if (mOnStackChangeListener != null) {
            DialogFragmentStack.get().setOnStackChangeListener(null);
        }
        LottieHelper.release();
        super.onDestroy();
    }

    private void recycleTab(boolean includeGlide, boolean cancelReq) {
        BaseTab[] tabs = new BaseTab[]{mRecFirstTab, mRecSecondTab, mMusicChoiceTab, mMusicCategoryTab, mRadioChoiceTab, mRadioCategoryTab};
        for (BaseTab tab : tabs) {
            if (tab != null) {
                tab.onViewDetachedFromWindow(includeGlide, cancelReq);
            }
        }
    }

    private void resumeCurrentTab(boolean includeGlide) {
        if (tlTab != null) {
            int position = tlTab.getCurrentTab();
            switch (position) {
                case 0:
                    if (mRecFirstTab != null) {
                        mRecFirstTab.onViewAttachedToWindow(includeGlide);
                    }
                    if (mRecSecondTab != null) {
                        mRecSecondTab.onViewAttachedToWindow(includeGlide);
                    }
                    break;
                case 1:
                    if (mMusicChoiceTab != null) {
                        mMusicChoiceTab.onViewAttachedToWindow(includeGlide);
                    }
                    if (mMusicCategoryTab != null) {
                        mMusicCategoryTab.onViewAttachedToWindow(includeGlide);
                    }
                    break;
                case 2:
                    if (mRadioChoiceTab != null) {
                        mRadioChoiceTab.onViewAttachedToWindow(includeGlide);
                    }
                    if (mRadioCategoryTab != null) {
                        mRadioCategoryTab.onViewAttachedToWindow(includeGlide);
                    }
                    break;
            }
        }
    }

    private int mLastReqPage = -1;
    private long mLastReqTime;

    private void notifyRefreshPage(int page, boolean fromError) {
        if (!fromError) {
            AppLogic.removeUiGroundCallback(mRefreshCurrPageTask);
            mRetryDelay = 0;
        }
        if (page == mLastReqPage && SystemClock.elapsedRealtime() - mLastReqTime < 500) {
            return;
        }
        mLastReqPage = page;
        mLastReqTime = SystemClock.elapsedRealtime();
        switch (page) {
            case PAGE_ID_REC:
                PageActionCreator.get().getRecommendPage();
                break;
            case PAGE_ID_MUSIC:
                PageActionCreator.get().getMusicPage();
                break;
            case PAGE_ID_RADIO:
                PageActionCreator.get().getRadioPage();
                break;
        }
    }

    private void notifyRefreshCurrPageDelay() {
        if (!NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
            return;
        }
        if (tlTab != null) {
            int position = tlTab.getCurrentTab();
            if (SystemClock.elapsedRealtime() - mLimitTime < 0) {
                return;
            }
            boolean bPass = false;
            switch (position) {
                case 0:
                    if (!isRecTabLoaded) {
                        bPass = true;
                    }
                    break;
                case 1:
                    if (!isMusicTabLoaded) {
                        bPass = true;
                    }
                    break;
                case 2:
                    if (!isRadioTabLoaded) {
                        bPass = true;
                    }
                    break;
            }
            if (bPass) {
                mRetryDelay += 5000;
                if (mRetryDelay > 20000) {
                    mRetryDelay = 20000;
                }
                Log.d(Constant.LOG_TAG_UI_DEBUG, "request page data failed, retry after " + mRetryDelay + "ms");
                mLimitTime = SystemClock.elapsedRealtime() + mRetryDelay;
                AppLogic.runOnUiGround(mRefreshCurrPageTask, mRetryDelay);
            }
        }
    }

    private static final int PAGE_ID_REC = 0;
    private static final int PAGE_ID_MUSIC = 1;
    private static final int PAGE_ID_RADIO = 2;
    private long mRetryDelay;
    private long mLimitTime;
    private Runnable mRefreshCurrPageTask = () -> {
        if (tlTab != null) {
            int position = tlTab.getCurrentTab();
            switch (position) {
                case 0:
                    if (!isRecTabLoaded) {
                        notifyRefreshPage(PAGE_ID_REC, true);
                    }
                    break;
                case 1:
                    if (!isMusicTabLoaded) {
                        notifyRefreshPage(PAGE_ID_MUSIC, true);
                    }
                    break;
                case 2:
                    if (!isRadioTabLoaded) {
                        notifyRefreshPage(PAGE_ID_RADIO, true);
                    }
                    break;
            }
        }
    };
}
