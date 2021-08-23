package com.txznet.music.ui;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.comm.base.ActivityStack;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.Logger;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.music.R;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.albumModule.bean.InterestTag;
import com.txznet.music.albumModule.logic.AlbumEngine;
import com.txznet.music.albumModule.ui.MusicCategoryFragment;
import com.txznet.music.albumModule.ui.RadioCategoryFragment;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.IFinishCallBack;
import com.txznet.music.baseModule.bean.EnumState;
import com.txznet.music.baseModule.dao.DBManager;
import com.txznet.music.historyModule.ui.MineFragmentV42;
import com.txznet.music.localModule.ui.LocalMusicFragment;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.playerModule.logic.focus.MyFocusListener;
import com.txznet.music.push.PushLogicHelper;
import com.txznet.music.push.PushManager;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.report.bean.ClickOpenApp;
import com.txznet.music.report.bean.ExitEvent;
import com.txznet.music.utils.AttrUtils;
import com.txznet.music.utils.JsonHelper;
import com.txznet.music.utils.ScreenUtils;
import com.txznet.music.utils.SharedPreferencesUtils;
import com.txznet.music.utils.UIHelper;
import com.txznet.music.utils.Utils;
import com.txznet.music.widget.TipsDialog;
import com.txznet.reserve.activity.ReserveNoHistoryStandardActivity0;

import java.util.List;
import java.util.Observer;
import java.util.concurrent.Callable;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static com.txznet.loader.AppLogicBase.getApp;

/**
 * Created by brainBear on 2017/12/7.
 */

public class HomeActivity extends BaseBarActivity implements View.OnClickListener, Observer {


    public static final int LOCAL_i = 0;
    public static final int MUSIC_i = 1;
    public static final int RADIO_i = 2;
    public static final int HISTORY_i = 3;
    private static final String TAG = "HomeActivity:";
    private final String KEY_FRAGMENT_POSITION = "position";
    public static int mTabIndex = -1;
    public static final int DEFAULT_TYPE = HomeActivity.MUSIC_i;
    public int jumpToType = DEFAULT_TYPE;


    Fragment mLocalMusicFragment;
    Fragment mSingleMusicFragment;
    Fragment mSingleRadioFragmentV42;
    Fragment mineFragmentV42;


    // End Of Content View Elements
    //左侧各个空间
    private RelativeLayout mRlLocal;
    private ImageView mIvLocal;
    private TextView mTvLocal;
    private RelativeLayout mRlMusic;
    private ImageView mIvMusic;
    private TextView mTvMusic;
    private RelativeLayout mRlRadio;
    private ImageView mIvRadio;
    private TextView mTvRadio;
    private RelativeLayout mRlMine;
    private ImageView mIvMine;
    private TextView mTvMine;
    private ImageView choiceBg;
    private View redDot;
    private CompositeDisposable mCompositeDisposable;


    @Override
    public void bindViews(Bundle savedInstanceState) {

        mRlLocal = (RelativeLayout) findViewById(R.id.ll_local);
        mIvLocal = (ImageView) findViewById(R.id.iv_local);
        mTvLocal = (TextView) findViewById(R.id.tv_local);
        mRlMusic = (RelativeLayout) findViewById(R.id.ll_music);
        mIvMusic = (ImageView) findViewById(R.id.iv_music);
        mTvMusic = (TextView) findViewById(R.id.tv_music);
        mRlRadio = (RelativeLayout) findViewById(R.id.ll_radio);
        mIvRadio = (ImageView) findViewById(R.id.iv_radio);
        mTvRadio = (TextView) findViewById(R.id.tv_radio);
        mRlMine = (RelativeLayout) findViewById(R.id.ll_user);
        mIvMine = (ImageView) findViewById(R.id.iv_user);
        mTvMine = (TextView) findViewById(R.id.tv_user);
        choiceBg = (ImageView) findViewById(R.id.choice_bg);
        redDot = findViewById(R.id.red_dot);


        Resources resources = getResources();
        mIvLocal.setBackground(resources.getDrawable(R.drawable.d_local_icon));
        mTvLocal.setText(resources.getString(R.string.menu_text_local));
        mTvLocal.setTextColor(resources.getColorStateList(R.color.item_play_list_click_1));
        mTvLocal.setTextSize(TypedValue.COMPLEX_UNIT_PX, AttrUtils.getAttrDimension(this, R.attr.menu_text_size, 20));

        mIvMusic.setBackground(resources.getDrawable(R.drawable.d_music_icon));
        mTvMusic.setText(resources.getString(R.string.menu_text_music));
        mTvMusic.setTextColor(resources.getColorStateList(R.color.item_play_list_click_1));
        mTvMusic.setTextSize(TypedValue.COMPLEX_UNIT_PX, AttrUtils.getAttrDimension(this, R.attr.menu_text_size, 20));

        mIvRadio.setBackground(resources.getDrawable(R.drawable.d_radio_icon));
        mTvRadio.setText(resources.getString(R.string.menu_text_radio));
        mTvRadio.setTextColor(resources.getColorStateList(R.color.item_play_list_click_1));
        mTvRadio.setTextSize(TypedValue.COMPLEX_UNIT_PX, AttrUtils.getAttrDimension(this, R.attr.menu_text_size, 20));

        mIvMine.setBackground(resources.getDrawable(R.drawable.d_history_icon));
        mTvMine.setText(resources.getString(R.string.menu_text_user));
        mTvMine.setTextColor(resources.getColorStateList(R.color.item_play_list_click_1));
        mTvMine.setTextSize(TypedValue.COMPLEX_UNIT_PX, AttrUtils.getAttrDimension(this, R.attr.menu_text_size, 20));

        //需求：需要在打开界面之后就不弹界面了
        PushLogicHelper.getInstance().setExecuteShowView(true);
        PushLogicHelper.getInstance().setAppOpened(true);
        SharedPreferencesUtils.setCanNotShowPushWin(true);
        PushManager.getInstance().clickCancel();
    }


    public ImageView getChoiceBg() {
        return choiceBg;
    }

    @Override
    public int getLayout() {
        if (ScreenUtils.getScreenType() == ScreenUtils.TYPE_VERTICAL) {
            return R.layout.act_single_layout_cheji;
        } else if (ScreenUtils.isPhonePortrait()) {
            return R.layout.act_single_layout_phone_portrait;
        } else {
            return R.layout.act_single_layout_houshijing;
        }
    }

    protected void initData(Bundle savedInstanceState) {

        // 主界面
        // 四个子界面
        if (savedInstanceState != null) {
            mLocalMusicFragment = getFragmentManager().findFragmentByTag(LocalMusicFragment.class.getSimpleName());
            mSingleMusicFragment = getFragmentManager().findFragmentByTag(MusicCategoryFragment.class.getSimpleName());
            mSingleRadioFragmentV42 = getFragmentManager().findFragmentByTag(RadioCategoryFragment.class.getSimpleName());
            mineFragmentV42 = getFragmentManager().findFragmentByTag(MineFragmentV42.class.getSimpleName());
        }

        changeToFragment(jumpToType);
    }


    protected void initListener() {
        findViewById(R.id.ll_local).setOnClickListener(this);
        findViewById(R.id.ll_user).setOnClickListener(this);
        findViewById(R.id.ll_music).setOnClickListener(this);
        findViewById(R.id.ll_radio).setOnClickListener(this);


    }

    @Override
    protected String getActivityTag() {
        return "HomeActivity#" + this.hashCode() + "/我的首页";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ReportEvent.clickOpenActivity(ClickOpenApp.OPE_MANUAL, ClickOpenApp.MAIN_MUSIC);
        super.onCreate(savedInstanceState);
        if (getIntent() != null && getIntent().getExtras() != null) {
            jumpToType = getIntent().getExtras().getInt(Utils.KEY_TYPE, HomeActivity.MUSIC_i);
        }
        initListener();

        initData(savedInstanceState);
        MyFocusListener.getInstance().requestAudioFocus(AudioManager.AUDIOFOCUS_GAIN);
        mCompositeDisposable = new CompositeDisposable();
        ObserverManage.getObserver().addObserver(this);

        //开始播放

        if (SharedPreferencesUtils.isResumeAutoPlay()
                && !PlayEngineFactory.getEngine().isPlaying()
                && PlayInfoManager.getInstance().getCurrentAudio() != null) {
            PlayEngineFactory.getEngine().play(EnumState.Operation.auto);
        }

        PlayEngineFactory.getEngine().play(EnumState.Operation.manual);
    }

    @Override
    protected void onStart() {

        // 初始化播放器组件
        // PlayEngineFactory.getEngine().setListener(this);
        PlayEngineFactory.getEngine().init();
//        if (lastAlbum != null) {
//            ImageFactory.getInstance().setStyle(IImageLoader.BLUR_FILTER);
//            ImageFactory.getInstance().display(this, lastAlbum.getLogo(), getChoiceBg(), 0);
//        }

        super.onStart();
    }

    private void setSelected(TextView view, ImageView iv) {
        mTvMusic.setSelected(false);
        mTvMine.setSelected(false);
        mTvLocal.setSelected(false);
        mTvRadio.setSelected(false);

        mIvMine.setSelected(false);
        mIvRadio.setSelected(false);
        mIvMusic.setSelected(false);
        mIvLocal.setSelected(false);

        iv.setSelected(true);
        view.setSelected(true);
    }

    // 跳转页面
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_local:
                changeToFragment(LOCAL_i);
                break;
            case R.id.ll_music:
                clickMusicTag();
                break;
            case R.id.ll_radio:
                clickRadioTag();
                break;
            case R.id.ll_user:
                changeToFragment(HISTORY_i);
                break;
        }
    }

    public void clickMusicTag() {
        changeToFragment(MUSIC_i);
        reqInterestTag(Constant.GET_INTEREST_TAG);
    }

    public void clickRadioTag() {
        changeToFragment(RADIO_i);
        reqInterestTag(Constant.GET_FM_INTEREST_TAG);
    }

    private void reqInterestTag(final String url) {
       /* List<InterestTag> result = new ArrayList<>();
        String[] conStr = new String[]{
                "5","·12 汶川地震",
                "发","生于北京时间（UTC + 8）",
                "2","008 年5月12日（星期一）14 时28分04秒",
                "根","据中华人民共和国地震局的数据",
                "此","次",
                "地","震的",
                "面","波震级",
                "里","氏震级达8 .0 Ms",
                "矩","震级达8 .3 Mw",
                "（","根据美国地质调查局的数据，矩震级为7 .9 Mw）",
                "地","震烈度达到11度",
                "地","震波及大半个中国及亚洲多个国家和地区",
                "北","至辽宁",
                "东","至上海"
        };
        for (int i = 0; i < 100; i++) {
            result.add(new InterestTag(i, conStr[i % conStr.length]));
        }


        Intent intent = new Intent(HomeActivity.this, ReserveNoHistoryStandardActivity0.class);
        intent.putExtra("url", url);
        intent.putExtra("data", JsonHelper.toJson(result));
        startActivity(intent);*/

        AlbumEngine.getInstance().queryInterestTag(url, new IFinishCallBack<InterestTag>() {
            @Override
            public void onComplete(List<InterestTag> result) {
                final long endTime = SystemClock.elapsedRealtime();
                Intent intent = new Intent(HomeActivity.this, ReserveNoHistoryStandardActivity0.class);
                intent.putExtra("url", url);
                intent.putExtra("data", JsonHelper.toJson(result));
                if (!ActivityStack.getInstance().currentActivity().getClass().equals(ReserveNoHistoryStandardActivity0.class)) {
                    startActivity(intent);
                } else {
                    if (url.equals(Constant.GET_INTEREST_TAG)) {
                        SharedPreferencesUtils.setReqInsterestTagCount(SharedPreferencesUtils.getReqInsterestTagCount() - 1);
                    } else {
                        SharedPreferencesUtils.setReqFMInterestTagCount(SharedPreferencesUtils.getReqFMInterestTagCount() - 1);
                    }
                }
            }

            @Override
            public void onError(String error) {
                Logger.d(TAG, "queryInterestTag:" + error);
            }
        });
    }

    public void changeToFragment(int mType) {
        LogUtil.logd(TAG + "onClick:" + mType);
        if (mType == mTabIndex) {
            return;
        }
        mTabIndex = mType;
        // 管理fragment
        // Fragment事务
        FragmentTransaction transaction = getFragmentManager()
                .beginTransaction();
        hide(transaction);
        switch (mType) {
            case 0:
                setSelected(mTvLocal, mIvLocal);
                mLocalMusicFragment = getFragmentManager().findFragmentByTag(LocalMusicFragment.class.getSimpleName());
                if (null != mLocalMusicFragment) {
                    transaction.show(mLocalMusicFragment);
                } else {
                    mLocalMusicFragment = new LocalMusicFragment();
                    transaction.add(R.id.fm_content, mLocalMusicFragment, LocalMusicFragment.class.getSimpleName());
                }
                break;
            case 1:
                setSelected(mTvMusic, mIvMusic);
                mSingleMusicFragment = getFragmentManager().findFragmentByTag(MusicCategoryFragment.class.getSimpleName());
                if (null != mSingleMusicFragment) {
                    transaction.show(mSingleMusicFragment);
                } else {
                    mSingleMusicFragment = new MusicCategoryFragment();
                    transaction.add(R.id.fm_content, mSingleMusicFragment, MusicCategoryFragment.class.getSimpleName());
                }
                break;
            case 2:
                setSelected(mTvRadio, mIvRadio);
                mSingleRadioFragmentV42 = getFragmentManager().findFragmentByTag(RadioCategoryFragment.class.getSimpleName());
                if (null != mSingleRadioFragmentV42) {
                    transaction.show(mSingleRadioFragmentV42);
                } else {
                    mSingleRadioFragmentV42 = new RadioCategoryFragment();
                    transaction.add(R.id.fm_content, mSingleRadioFragmentV42, RadioCategoryFragment.class.getSimpleName());
                }
                break;
            case 3:
                setSelected(mTvMine, mIvMine);
                mineFragmentV42 = getFragmentManager().findFragmentByTag(MineFragmentV42.class.getSimpleName());
                if (null != mineFragmentV42) {
                    transaction.show(mineFragmentV42);
                } else {
                    mineFragmentV42 = new MineFragmentV42();
                    transaction.add(R.id.fm_content, mineFragmentV42, MineFragmentV42.class.getSimpleName());
                }
                break;
            default:
                break;
        }
        ReportEvent.clickMainMenu(mType);
        transaction.commitAllowingStateLoss();

    }

    TipsDialog tipsDialog;

    @Override
    public void onBackPressed() {
        LogUtil.logd(TAG + "]onClick:back:" + "返回按键");
        if (SharedPreferencesUtils.getShowExitDialog()) {
            if (tipsDialog == null) {
                TipsDialog.TipsDialogBuildData tipsDialogBuildData = new TipsDialog.TipsDialogBuildData();
                tipsDialogBuildData.setTitle("确定要退出" + getApp().getResources().getString(R.string.app_name) + "吗?");
                tipsDialogBuildData.setContext(this);
                tipsDialogBuildData.setSureText("退出同听");
                tipsDialogBuildData.setCancelText("后台播放");
                tipsDialogBuildData.setWindowType(WindowManager.LayoutParams.TYPE_APPLICATION);
                tipsDialog = new TipsDialog(tipsDialogBuildData);
                tipsDialog.setSureListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ReportEvent.reportExitEvent(ExitEvent.ACTION_EXIT_MANUAL);
                        HomeActivity.this.moveTaskToBack(true);// 解决26694问题
                        SharedPreferencesUtils.setExitWithPlay(false);
//                    sendBroadcast(new Intent("com.txznet.music.main_finish"));
                        //如果不发这个com.txznet.music.main_finish广播会导致知豆的双屏出现异常.(声控退出同听出现问题,故此改正)
                        UIHelper.exit();
                    }
                });
                tipsDialog.setCancelListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPreferencesUtils.setExitWithPlay(true);
                        HomeActivity.this.moveTaskToBack(true);
                        sendBroadcast(new Intent("com.txznet.music.main_finish"));
                    }
                });
                tipsDialog.showImediately();
            } else {
                tipsDialog.showImediately();
            }
        } else {
            moveTaskToBack(true);
        }
    }


    /**
     * hide 所有的fragment
     *
     * @param ft
     */
    private void hide(FragmentTransaction ft) {
        if (null != mLocalMusicFragment) {
            ft.hide(mLocalMusicFragment);
        }
        if (null != mSingleMusicFragment) {
            ft.hide(mSingleMusicFragment);
        }
        if (null != mSingleRadioFragmentV42) {
            ft.hide(mSingleRadioFragmentV42);
        }
        if (null != mineFragmentV42) {
            ft.hide(mineFragmentV42);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkRedDot();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onStop() {
//        getChoiceBg().setImageDrawable(null);
        super.onStop();
    }

    @Override
    protected void onPause() {
        // finish();// 品旭的生命走起不走该方法。
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        sendBroadcast(new Intent("com.txznet.music.main_finish"));

        mSingleMusicFragment = null;
        mSingleRadioFragmentV42 = null;
        mLocalMusicFragment = null;
        mineFragmentV42 = null;

        mCompositeDisposable.clear();
        ObserverManage.getObserver().deleteObserver(this);

        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
        outState.putInt(KEY_FRAGMENT_POSITION, mTabIndex);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        int position = savedInstanceState.getInt(KEY_FRAGMENT_POSITION, 2);
        changeToFragment(position);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onPlayInfoUpdated(Audio audio, Album album) {
        super.onPlayInfoUpdated(audio, album);
    }

    @Override
    public ImageView getBg() {
        return getChoiceBg();
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        LogUtil.logd(getActivityTag() + " invoke " + "onNewIntent");
        if (intent != null) {
            int intExtra = intent.getIntExtra(Utils.KEY_TYPE, DEFAULT_TYPE);
            changeToFragment(intExtra);
        }
    }


    private void checkRedDot() {
        Disposable disposable = io.reactivex.Observable.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return DBManager.getInstance().checkUnreadMessage();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        if (aBoolean) {
                            redDot.setVisibility(View.VISIBLE);
                        } else {
                            redDot.setVisibility(View.INVISIBLE);
                        }
                    }
                });

        mCompositeDisposable.add(disposable);
    }

    @Override
    public void update(java.util.Observable o, Object arg) {
        if (arg instanceof InfoMessage) {
            InfoMessage info = (InfoMessage) arg;
            switch (info.getType()) {

                case InfoMessage.MESSAGE_CLEAR_UNREAD:
                    redDot.setVisibility(View.INVISIBLE);
                    break;
                case InfoMessage.MESSAGE_NEW_UNREAD:
                    redDot.setVisibility(View.VISIBLE);
                    break;

            }
        }
    }
}
