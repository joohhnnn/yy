//package com.txznet.music.historyModule.ui;
//
//import android.content.res.Resources;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//import android.util.TypedValue;
//import android.view.View;
//import android.widget.AdapterView;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//import com.txznet.comm.remote.GlobalContext;
//import com.txznet.comm.remote.util.LogUtil;
//import com.txznet.fm.bean.InfoMessage;
//import com.txznet.music.R;
//import com.txznet.music.albumModule.bean.Audio;
//import com.txznet.music.baseModule.ui.BaseFragment;
//import com.txznet.music.historyModule.ui.adpter.HistoryAudioAdapter;
//import com.txznet.music.historyModule.ui.adpter.HistoryItemDecoration;
//import com.txznet.music.ui.layout.TXZGridLayoutManager;
//import com.txznet.music.ui.layout.TXZLinearLayoutManager;
//import com.txznet.music.utils.ScreenUtils;
//import com.txznet.music.widget.NavRecyclerView;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Observable;
//
///**
// * Created by ASUS User on 2016/11/9.
// */
//public abstract class BaseHistoryFragment extends BaseFragment {
//
//    private ImageView ivNoResult;
//    private Resources mRes;
//
//    private NavRecyclerView mListHistory;
//    private LinearLayout mLl_nodata;
//    private TextView mNolist;
//    protected HistoryAudioAdapter mAdapter;
//    protected List<Audio> mAudios = new ArrayList<Audio>();
//    private RecyclerView.LayoutManager mLayoutManager;
//
//    @Override
//    public void bindViews() {
//        mRes = getActivity().getResources();
//
//        mListHistory = (NavRecyclerView) findViewById(R.id.lv_list);
//        mLl_nodata = (LinearLayout) findViewById(R.id.ll_nodata);
//        mNolist = (TextView) findViewById(R.id.nolist);
//        ivNoResult = (ImageView) findViewById(R.id.iv_no_result);
//
//        ivNoResult.setImageDrawable(mRes.getDrawable(R.drawable.local_noresult));
//        mNolist.setText(mRes.getString(R.string.no_history_text));
//        mNolist.setTextSize(TypedValue.COMPLEX_UNIT_PX, mRes.getDimension(R.dimen.no_history_text_size));
//        mNolist.setTextColor(mRes.getColor(R.color.no_history_text_color));
//    }
//
//    protected void setDataChange() {
//        mAdapter.notifyDataSetChanged();
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//    }
//
//    public void notify(List<Audio> t) {
//        mAudios.clear();
//        if (t != null) {
//            mAudios.addAll(t);
//        }
//        setDataChange();
//    }
//
//    public abstract void onItemClickHandle(AdapterView<?> parent, View view, int position, long id);
//
//    public abstract List<Audio> getHistories();
//
//    public abstract void refreshPlayPosition(Audio currentAudio);
//
//    @Override
//    public void initListener() {
//    }
//
//    NavRecyclerView.EmptyStatusListener mListEmptyListener = new NavRecyclerView.EmptyStatusListener() {
//        @Override
//        public void onStatusChange(boolean isEmpty) {
//            mLl_nodata.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
//            mListHistory.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
//        }
//    };
//
//    @Override
//    public void initData() {
//        mAdapter = new HistoryAudioAdapter(mAudios, GlobalContext.get());
//        mListHistory.setEmptyStatusListener(mListEmptyListener);
////		mListHistory.setEmptyView(mLl_nodata);
////		if (ScreenUtils.getScreenType(getActivity(), false) == ScreenUtils.TYPE_SCREEN_SHORT) {
////			mLayoutManager = new TXZLinearLayoutManager(getActivity());
////			mListHistory.setLayoutManager(mLayoutManager);
////		} else {
//        if(ScreenUtils.getScreenType() == ScreenUtils.TYPE_VERTICAL){
//            mLayoutManager = new TXZLinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
//        }else{
//            mLayoutManager = new TXZGridLayoutManager(getActivity(), 2);
//            mListHistory.addItemDecoration(new HistoryItemDecoration());
//        }
//		mListHistory.setLayoutManager(mLayoutManager);
////		}
//        mListHistory.setAdapter(mAdapter);
//        mAdapter.setShowRescanBtn(false, null);
//        mAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view,
//                                    int position, long id) {
//                onItemClickHandle(parent, view, position, id);
//            }
//        });
//
////		mAdapter.setOnDeleteItemShowListener(new HistoryAudioAdapter.OnDeleteItemShowListener() {
////			@Override
////			public void onShow(boolean isShow, int position) {
////				if (isShow) {
////					if (mLayoutManager instanceof TXZGridLayoutManager) {
////						TXZGridLayoutManager layoutManager = (TXZGridLayoutManager) mLayoutManager;
////						layoutManager.setStackFromEnd(true);
////						layoutManager.scrollToPositionWithOffset(position, 0);
////						layoutManager.setStackFromEnd(false);
////					} else if(mLayoutManager instanceof  TXZLinearLayoutManager){
////						TXZLinearLayoutManager layoutManager = (TXZLinearLayoutManager) mLayoutManager;
////						layoutManager.setStackFromEnd(true);
////						layoutManager.scrollToPositionWithOffset(position, 0);
////						layoutManager.setStackFromEnd(false);
////					}
////				}
////			}
////		});
//    }
//
//    @Override
//    public int getLayout() {
//        return R.layout.list_with_nodata;
//    }
//
//
//    @Override
//    public void update(Observable observable, Object data) {
//        if (data instanceof InfoMessage) {
//            InfoMessage info = (InfoMessage) data;
//            LogUtil.logd(TAG + "[" + getFragmentId() + "]" + "reqData:info type:" + info.getType());
//            switch (info.getType()) {
//                case InfoMessage.REFRESH_HISTORY_MUSIC_LIST:
////					notify(HistoryEngine.getInstance().getMusicHistory());
//                    notify(getHistories());
//                    break;
//                case InfoMessage.REQUERY_HISTORY_MUSIC_LIST:
//                    reqData();
//                    break;
//                case InfoMessage.NOTIFY_LOCAL_AUDIO:
//                    Audio audio = (Audio) info.getObj();
//                    if (audio != null && mAudios != null) {
//                        setDataChange();
//                    }
//                    break;
//                case InfoMessage.PLAYER_CURRENT_AUDIO:
////					Audio current = (Audio) info.getObj();
////					refreshPlayPosition(current);
//                    break;
//
//                default:
//                    break;
//            }
//        }
//    }
//
//    @Override
//    public void onHiddenChanged(boolean hidden) {
//        super.onHiddenChanged(hidden);
//    }
//
//    @Override
//    public void onStop() {
//        notify(null);
//        super.onStop();
//    }
//
//    @Override
//    public void onStart() {
//        notify(getHistories());
//        super.onStart();
//    }
//}
