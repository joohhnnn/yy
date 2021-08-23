package com.txznet.music.message;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.percent.PercentRelativeLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.txznet.fm.bean.InfoMessage;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.loader.AppLogic;
import com.txznet.music.R;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.albumModule.logic.AlbumEngine;
import com.txznet.music.baseModule.bean.EnumState;
import com.txznet.music.baseModule.dao.DBManager;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.ui.BaseBarActivity;
import com.txznet.music.ui.layout.TXZLinearLayoutManager;
import com.txznet.music.widget.LoadingView;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by brainBear on 2017/12/23.
 */

public class MessageActivity extends BaseBarActivity implements Observer {

    @Bind(R.id.iv_left_back)
    ImageView ivLeftBack;
    @Bind(R.id.ll_left_back)
    LinearLayout llLeftBack;
    @Bind(R.id.tv_back)
    TextView tvBack;
    @Bind(R.id.tv_title)
    TextView tvTitle;
    @Bind(R.id.iv_delete)
    ImageView ivDelete;
    @Bind(R.id.tv_choice_all)
    TextView tvChoiceAll;
    @Bind(R.id.tv_delete)
    TextView tvDelete;
    @Bind(R.id.ll_delete_rage)
    PercentRelativeLayout llDeleteRage;
    @Bind(R.id.layout_loading)
    LoadingView mLoadingView;

    @Bind(R.id.rv_message)
    RecyclerView rvMessage;
    @Bind(R.id.choice_bg)
    ImageView choiceBg;
    private MessageAdapter mAdapter;

    private boolean isSelectStatus = false;
    private List<Message> mMessages = new ArrayList<>();
    private CompositeDisposable mCompositeDisposable;
    private PopupWindow mPopWindow;

    @Override
    public int getLayout() {
        return R.layout.activity_message;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCompositeDisposable = new CompositeDisposable();

        ObserverManage.getObserver().addObserver(this);

        initData();
    }

    private void initData() {
        updateMessages(true);
    }

    @Override
    public ImageView getBg() {
        return choiceBg;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.clear();

        ObserverManage.getObserver().deleteObserver(this);
    }

    @Override
    public void bindViews(Bundle savedInstanceState) {
        ButterKnife.bind(this);

        tvTitle.setText("消息");

        TXZLinearLayoutManager txzLinearLayoutManager = new TXZLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvMessage.setLayoutManager(txzLinearLayoutManager);

        mAdapter = new MessageAdapter();

        rvMessage.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new MessageAdapter.OnMessageClickListener() {
            @Override
            public void onItemClick(View view, Message message) {
                handleClick(message);
                ReportEvent.clickMessagePagePlay();
            }

            @Override
            public void onItemLongClick(View view, Message message) {
                showPopWindow(view, message);
            }
        });
    }


    private void handleClick(Message message) {
        if (message.getType() == Message.TYPE_ALBUM) {
            Album album = message.getAlbum();
            AlbumEngine.getInstance().playAlbum(EnumState.Operation.manual, album, album.getCategoryId(), null);
        } else if (message.getType() == Message.TYPE_AUDIO) {
            List<Audio> audios = new ArrayList<>();
            audios.addAll(message.getAudios());
            for (Message msg : mMessages) {
                if (msg.getType() == Message.TYPE_AUDIO) {
                    for (Audio audio : msg.getAudios()) {
                        if (!audios.contains(audio)) {
                            audios.add(audio);
                        }
                    }
                }
            }
            PlayEngineFactory.getEngine().setAudios(EnumState.Operation.manual, audios, null, 0, PlayInfoManager.DATA_MESSAGE);
            PlayEngineFactory.getEngine().play(EnumState.Operation.manual);
        }

    }


    private void showPopWindow(View parent, final Message message) {
        int[] contentLocation = new int[2];
        rvMessage.getLocationOnScreen(contentLocation);
        int contentHeight = rvMessage.getHeight();

        int width = (int) getResources().getDimension(R.dimen.x150);
        int height = (int) getResources().getDimension(R.dimen.y160);


        View view = LayoutInflater.from(this).inflate(R.layout.message_pop_window, null, false);

        view.findViewById(R.id.tv_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Disposable disposable = io.reactivex.Observable.just(message)
                        .doOnNext(new Consumer<Message>() {
                            @Override
                            public void accept(Message message) throws Exception {
                                DBManager.getInstance().deleteMessage(message);
                            }
                        })
                        .subscribeOn(Schedulers.io())
                        .map(new Function<Message, List<Message>>() {
                            @Override
                            public List<Message> apply(Message message) throws Exception {
                                return DBManager.getInstance().getMessages();
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<List<Message>>() {
                            @Override
                            public void accept(List<Message> messages) throws Exception {
                                mMessages.clear();
                                mMessages.addAll(messages);
                                mAdapter.notifyDataSetChanged();
                                mPopWindow.dismiss();
                                ReportEvent.clickMessagePageDeleteOne();
                            }
                        });

                mCompositeDisposable.add(disposable);
            }
        });

        view.findViewById(R.id.tv_listen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleClick(message);
                mPopWindow.dismiss();
                ReportEvent.clickMessagePagePlayPop();
            }
        });


        mPopWindow = new PopupWindow(view, width, height);
        mPopWindow.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2e303b")));
        mPopWindow.setFocusable(true);
        mPopWindow.setOutsideTouchable(true);
        mPopWindow.update();


        int[] parentLocation = new int[2];
        parent.getLocationOnScreen(parentLocation);
        int parentHeight = parent.getHeight();
        int parentWidth = parent.getWidth();

        int x = parentLocation[0] + parentWidth / 2 - width / 2;
        int y = parentLocation[1] + parentHeight / 2;

        if (contentLocation[1] + contentHeight < y + height) {
            y -= height;
        }

        mPopWindow.showAtLocation(parent, Gravity.TOP | Gravity.START, x, y);
    }


    private void updateMessages(boolean showLoading) {
        if (showLoading) {
            mLoadingView.showLoading(R.drawable.fm_local_scan, R.drawable.fm_local_scan_logo, "");
        }

        Disposable disposable = Flowable.just(DBManager.getInstance().getMessages())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Message>>() {
                    @Override
                    public void accept(List<Message> messages) throws Exception {
                        mMessages.clear();
                        mMessages.addAll(messages);
                        mAdapter.replaceData(mMessages);
                        mAdapter.notifyDataSetChanged();

                        checkMessagesEmpty();
                    }
                });
        mCompositeDisposable.add(disposable);
    }

    private void checkMessagesEmpty() {
        if (mMessages.isEmpty()) {
            mLoadingView.showEmpty("当前没有消息", R.drawable.ic_no_message, "");
            ivDelete.setVisibility(View.INVISIBLE);
        } else {
            mLoadingView.showContent();
            ivDelete.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        clearUnreadStatus();
    }


    private void clearUnreadStatus() {
        AppLogic.runOnBackGround(new Runnable() {
            @Override
            public void run() {
                DBManager.getInstance().clearMessageUnRead();

                ObserverManage.getObserver().send(InfoMessage.MESSAGE_CLEAR_UNREAD);
            }
        });
    }


    @Override
    protected String getActivityTag() {
        return "MessageActivity";
    }


    private void setTabSelectStatus(boolean select) {
        isSelectStatus = select;
        mAdapter.setSelectStatus(select);

        if (select) {
            llLeftBack.setVisibility(View.GONE);
            ivDelete.setVisibility(View.GONE);
            llDeleteRage.setVisibility(View.VISIBLE);
            tvBack.setVisibility(View.VISIBLE);
        } else {
            llLeftBack.setVisibility(View.VISIBLE);
            ivDelete.setVisibility(View.VISIBLE);
            llDeleteRage.setVisibility(View.GONE);
            tvBack.setVisibility(View.GONE);

            mAdapter.getSelectMessages().clear();
        }
    }


    @OnClick({R.id.ll_left_back, R.id.tv_back, R.id.iv_delete, R.id.tv_choice_all, R.id.tv_delete})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ll_left_back:
                finish();
                ReportEvent.clickMessagePageBack();
                break;
            case R.id.tv_back:
                setTabSelectStatus(false);
                break;
            case R.id.iv_delete:
                setTabSelectStatus(true);
                break;
            case R.id.tv_choice_all:
                mAdapter.getSelectMessages().clear();
                mAdapter.getSelectMessages().addAll(mMessages);
                mAdapter.notifyDataSetChanged();
                break;
            case R.id.tv_delete:
                Disposable disposable = Flowable.just(mAdapter.getSelectMessages())
                        .observeOn(Schedulers.io())
                        .map(new Function<List<Message>, Boolean>() {
                            @Override
                            public Boolean apply(List<Message> messages) throws Exception {
                                DBManager.getInstance().deleteMessages(messages);
//                                ReportEvent.clickMessagePageDelete();
                                return true;
                            }
                        })
                        .map(new Function<Boolean, List<Message>>() {
                            @Override
                            public List<Message> apply(Boolean aBoolean) throws Exception {
                                return DBManager.getInstance().getMessages();
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<List<Message>>() {
                            @Override
                            public void accept(List<Message> messages) throws Exception {
                                mMessages.clear();
                                mMessages.addAll(messages);

                                setTabSelectStatus(false);
                                checkMessagesEmpty();
                                ReportEvent.clickMessagePageDeleteAll();
                            }
                        });
                mCompositeDisposable.add(disposable);
                break;
        }
    }


    @Override
    public void onBackPressed() {
        if (isSelectStatus) {
            setTabSelectStatus(false);
        } else {
            super.onBackPressed();
            ReportEvent.clickMessagePageBack();
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof InfoMessage) {
            InfoMessage info = (InfoMessage) arg;
            switch (info.getType()) {

                case InfoMessage.MESSAGE_NEW_UNREAD:
                    updateMessages(false);
                    clearUnreadStatus();
                    break;
                case InfoMessage.MESSAGE_NEW_READ:
                    updateMessages(false);
                    break;
            }
        }
    }
}
