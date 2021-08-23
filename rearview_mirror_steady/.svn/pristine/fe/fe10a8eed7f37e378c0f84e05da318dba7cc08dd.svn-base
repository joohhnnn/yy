package com.txznet.music.ui;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.music.R;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.albumModule.ui.SingleMusicFragment;
import com.txznet.music.albumModule.ui.SingleRadioFragmentV42;
import com.txznet.music.baseModule.bean.EnumState;
import com.txznet.music.baseModule.dao.DBManager;
import com.txznet.music.data.kaola.KaolaPlayHelper;
import com.txznet.music.data.netease.NeteaseSDK;
import com.txznet.music.data.netease.net.bean.RespSearch;
import com.txznet.music.data.utils.OnGetData;
import com.txznet.music.historyModule.ui.MineFragmentV42;
import com.txznet.music.image.glide.GlideImageLoader;
import com.txznet.music.localModule.ui.LocalMusicFragment;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.playerModule.logic.focus.MyFocusListener;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.report.bean.ClickOpenApp;
import com.txznet.music.report.bean.ExitEvent;
import com.txznet.music.service.MyService;
import com.txznet.music.service.ThirdHelper;
import com.txznet.music.utils.ScreenUtils;
import com.txznet.music.utils.SharedPreferencesUtils;
import com.txznet.music.utils.UIHelper;
import com.txznet.music.widget.TipsDialog;

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
    public static final String KEY_TAB = "TAB:";
    private static final String TAG = "HomeActivity:";
    private final String KEY_FRAGMENT_POSITION = "position";
    int mTabIndex;


    LocalMusicFragment mLocalMusicFragment;
    SingleMusicFragment mSingleMusicFragment;
    SingleRadioFragmentV42 mSingleRadioFragmentV42;
    //    MineFragment mMineFragment;
    MineFragmentV42 mineFragmentV42;
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
        mTvLocal.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.menu_text_size));

        mIvMusic.setBackground(resources.getDrawable(R.drawable.d_music_icon));
        mTvMusic.setText(resources.getString(R.string.menu_text_music));
        mTvMusic.setTextColor(resources.getColorStateList(R.color.item_play_list_click_1));
        mTvMusic.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.menu_text_size));

        mIvRadio.setBackground(resources.getDrawable(R.drawable.d_radio_icon));
        mTvRadio.setText(resources.getString(R.string.menu_text_radio));
        mTvRadio.setTextColor(resources.getColorStateList(R.color.item_play_list_click_1));
        mTvRadio.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.menu_text_size));

        mIvMine.setBackground(resources.getDrawable(R.drawable.d_history_icon));
        mTvMine.setText(resources.getString(R.string.menu_text_user));
        mTvMine.setTextColor(resources.getColorStateList(R.color.item_play_list_click_1));
        mTvMine.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.menu_text_size));

    }


    public ImageView getChoiceBg() {
        return choiceBg;
    }

    @Override
    public int getLayout() {
        if (ScreenUtils.getScreenType() == ScreenUtils.TYPE_CHEJI || ScreenUtils.getScreenType() == ScreenUtils.TYPE_VERTICAL) {
            return R.layout.act_single_layout_cheji;
        } else {
            return R.layout.act_single_layout_houshijing;
        }
    }

    protected void initData(Bundle savedInstanceState) {

        // 主界面
        // 四个子界面
        if (savedInstanceState != null) {
            mLocalMusicFragment = (LocalMusicFragment) getFragmentManager()
                    .findFragmentByTag(LocalMusicFragment.class.getSimpleName());
            mSingleMusicFragment = (SingleMusicFragment) getFragmentManager()
                    .findFragmentByTag(
                            SingleMusicFragment.class.getSimpleName());
            mSingleRadioFragmentV42 = (SingleRadioFragmentV42) getFragmentManager()
                    .findFragmentByTag(
                            SingleRadioFragmentV42.class.getSimpleName());

        }
        changeToFragment(1);
        setSelected(mTvMusic, mIvMusic);
    }


    protected void initListener() {
        findViewById(R.id.ll_local).setOnClickListener(this);
        findViewById(R.id.ll_user).setOnClickListener(this);
        findViewById(R.id.ll_music).setOnClickListener(this);
        findViewById(R.id.ll_radio).setOnClickListener(this);
        findViewById(R.id.ll_music).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                JSONBuilder jsonBuilder = new JSONBuilder();
                jsonBuilder.put("sessionId", 1L);
                jsonBuilder.put("field", 1);
                jsonBuilder.put("keywords", new String[]{"明天你好"});

                ThirdHelper.getInstance().invokeMusic("", "search", jsonBuilder.toBytes());


            }
        });
        findViewById(R.id.ll_radio).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONBuilder jsonBuilder = new JSONBuilder();
                jsonBuilder.put("sessionId", 1L);
                jsonBuilder.put("field", 2);
                jsonBuilder.put("keywords", new String[]{"二货一箩筐"});

                ThirdHelper.getInstance().invokeMusic("", "search", jsonBuilder.toBytes());

            }
        });

    }

    @Override
    protected String getActivityTag() {
        return "HomeActivity#" + this.hashCode() + "/我的首页";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ReportEvent.clickOpenActivity(ClickOpenApp.OPE_MANUAL, ClickOpenApp.MAIN_MUSIC);
        super.onCreate(savedInstanceState);
        initListener();

        initData(savedInstanceState);

        if (SharedPreferencesUtils.isResumeAutoPlay()
                && !PlayEngineFactory.getEngine().isPlaying()
                && PlayInfoManager.getInstance().getCurrentAudio() != null) {
            PlayEngineFactory.getEngine().play(EnumState.Operation.auto);
        }
        MyFocusListener.getInstance().requestAudioFocus(AudioManager.AUDIOFOCUS_GAIN);
        mCompositeDisposable = new CompositeDisposable();

        ObserverManage.getObserver().addObserver(this);
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
                changeToFragment(MUSIC_i);
                break;
            case R.id.ll_radio:
                changeToFragment(RADIO_i);
                break;
            case R.id.ll_user:
                changeToFragment(HISTORY_i);
                break;
        }
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
                mLocalMusicFragment = (LocalMusicFragment) getFragmentManager().findFragmentByTag("LocalMusicFragment");
                if (null != mLocalMusicFragment) {
                    transaction.show(mLocalMusicFragment);
                } else {
                    mLocalMusicFragment = new LocalMusicFragment();
                    transaction.add(R.id.fm_content, mLocalMusicFragment, "LocalMusicFragment");
                }
                break;
            case 1:
                setSelected(mTvMusic, mIvMusic);
                mSingleMusicFragment = (SingleMusicFragment) getFragmentManager().findFragmentByTag("SingleMusicFragment");
                if (null != mSingleMusicFragment) {
                    transaction.show(mSingleMusicFragment);
                } else {
                    mSingleMusicFragment = new SingleMusicFragment();
                    transaction.add(R.id.fm_content, mSingleMusicFragment, "SingleMusicFragment");
                }
                break;
            case 2:
                setSelected(mTvRadio, mIvRadio);
                mSingleRadioFragmentV42 = (SingleRadioFragmentV42) getFragmentManager().findFragmentByTag("SingleRadioFragmentV42");
                if (null != mSingleRadioFragmentV42) {
                    transaction.show(mSingleRadioFragmentV42);
                } else {
                    mSingleRadioFragmentV42 = new SingleRadioFragmentV42();
                    transaction.add(R.id.fm_content, mSingleRadioFragmentV42, "SingleRadioFragmentV42");
                }
                break;
            case 3:
                setSelected(mTvMine, mIvMine);
                mineFragmentV42 = (MineFragmentV42) getFragmentManager().findFragmentByTag("MineFragment");
                if (null != mineFragmentV42) {
                    transaction.show(mineFragmentV42);
                } else {
                    mineFragmentV42 = new MineFragmentV42();
                    transaction.add(R.id.fm_content, mineFragmentV42, "MineFragment");
                }
                break;
            default:
                break;
        }
        ReportEvent.clickMainMenu(mType);
        transaction.commitAllowingStateLoss();

    }

    @Override
    public void onBackPressed() {
        LogUtil.logd(TAG + "]onClick:back:" + "返回按键");
        if (SharedPreferencesUtils.getShowExitDialog()) {

            TipsDialog.TipsDialogBuildData tipsDialogBuildData = new TipsDialog.TipsDialogBuildData();
            tipsDialogBuildData.setTitle("确定要退出" + getApp().getResources().getString(R.string.app_name) + "吗?");
            tipsDialogBuildData.setContext(this);
            tipsDialogBuildData.setSureText("退出同听");
            tipsDialogBuildData.setCancelText("后台播放");
            final TipsDialog tipsDialog = new TipsDialog(tipsDialogBuildData);
            tipsDialog.setSureListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ReportEvent.reportExitEvent(ExitEvent.ACTION_EXIT_MANUAL);
                    HomeActivity.this.moveTaskToBack(true);// 解决26694问题
                    SharedPreferencesUtils.setExitWithPlay(false);
                    UIHelper.exit();
                    sendBroadcast(new Intent("com.txznet.music.main_finish"));
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
            tipsDialog.show();


//
//            final ConfirmDialog dialog = new ConfirmDialog(this);
//            dialog.setContentText("确定要退出" + getApp().getResources().getString(R.string.app_name)+"吗?", null);
//            dialog.setSureText("退出同听", new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    dialog.dismiss();
//                    SingleActivity.this.moveTaskToBack(true);// 解决26694问题
//                    SharedPreferencesUtils.setExitWithPlay(false);
//                    UIHelper.exit();
//                    sendBroadcast(new Intent("com.txznet.music.main_finish"));
//                }
//            });
//            dialog.setCancleText("后台播放", new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    SharedPreferencesUtils.setExitWithPlay(true);
//                    SingleActivity.this.moveTaskToBack(true);
//                    dialog.dismiss();
//                    sendBroadcast(new Intent("com.txznet.music.main_finish"));
//                }
//            });
//
//            dialog.show();
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
        outState.putInt(KEY_FRAGMENT_POSITION, mTabIndex);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        int position = savedInstanceState.getInt(KEY_FRAGMENT_POSITION, 1);
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
            int intExtra = intent.getIntExtra(KEY_TAB, -1);
            if (intExtra != -1) {
                changeToFragment(intExtra);
            }
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
