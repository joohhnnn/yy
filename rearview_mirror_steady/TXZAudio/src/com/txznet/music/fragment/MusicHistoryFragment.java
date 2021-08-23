package com.txznet.music.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.fm.dao.HistoryAudioDBHelper;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.music.Constant;
import com.txznet.music.R;
import com.txznet.music.adpter.HistoryAudioAdapter;
import com.txznet.music.adpter.LocalAudioAdapter;
import com.txznet.music.bean.response.Audio;
import com.txznet.music.bean.response.PlayConf;
import com.txznet.music.engine.MediaPlayerActivityEngine;
import com.txznet.music.fragment.logic.LocalLogic;
import com.txznet.music.utils.SharedPreferencesUtils;
import com.txznet.music.utils.Utils;
import com.txznet.music.widget.LinearLayoutManagerWrapper;
import com.txznet.music.widget.NavRecyclerView;

import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class MusicHistoryFragment extends BaseDataFragment<Audio> {
	// 使用单例
	public static class MyInstance {
		public static MusicHistoryFragment instance = new MusicHistoryFragment();
	}

	// Content View Elements

	private NavRecyclerView mLv_list;
	private LinearLayout mLl_nodata;
	private TextView mNolist;
	private LocalAudioAdapter mAdapter;
	private List<Audio> mAudios = new ArrayList<Audio>();

	@Override
	public void bindViews() {

		mLv_list = (NavRecyclerView) findViewById(R.id.lv_list);
		mLl_nodata = (LinearLayout) findViewById(R.id.ll_nodata);
		mNolist = (TextView) findViewById(R.id.nolist);
	}

	
	private void setNavViewList(){
		if(!isHidden()){
			View[] views = null;
			if (mAdapter.getItemCount() != 0) {
				views = new View[] { mLv_list };
			}
			LogUtil.logd("NAVBtn:music history fragment set view list");
			ObserverManage.getObserver().send(InfoMessage.ADD_HISTORY_VIEW_LIST, views);
			if(mLv_list.isIn()){
				ObserverManage.getObserver().send(InfoMessage.SET_CURRENT_VIEW, mLv_list);
			}
		}
	}
	
	private void setDataChange() {
		mAdapter.notifyDataSetChanged();
		setNavViewList();
	}
	
	@Override
	public void onResume() {
		setNavViewList();
		super.onResume();
	}
	
	@Override
	public void onPause() {
		View[] views = null;
		if(mLv_list.getAdapter().getItemCount() != 0){
			views = new View[]{mLv_list};
		}
		LogUtil.logd("NAVBtn:music history fragment delete view list");
		ObserverManage.getObserver().send(InfoMessage.DELETE_HISTORY_VIEW_LIST, views);
		super.onPause();
	}
	
	
	@Override
	public List<Audio> getDataFromLocal() {
		List<Integer> sidType = Utils.getSidType(PlayConf.MUSIC_TYPE);
		sidType.add(0);// 本地音乐
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < sidType.size(); i++) {
			sb.append(" sid = ");
			sb.append(sidType.get(i));
			if (sidType.size() - 1 != i) {
				sb.append(" or ");
			}
		}
		LogUtil.logd(TAG + "where =" + sb.toString());
		return HistoryAudioDBHelper.getInstance().findAll(Audio.class,
				sb.toString(), null, null);
	}

	@Override
	public void notify(List<Audio> t) {
		mAudios.clear();
		if (t != null) {
			mAudios.addAll(t);
		}
		setDataChange();
	}

	@Override
	public void reqData() {
		// TODO Auto-generated method stub

	}

	@Override
	public void initListener() {
	}

	@Override
	public void initData() {
		mAdapter = new LocalAudioAdapter(mAudios, getActivity()
				.getApplicationContext());

		mLv_list.setEmptyView(mLl_nodata);
		mLv_list.setLayoutManager(new LinearLayoutManagerWrapper(getActivity(), "MusicHistoryFragment"));
		mLv_list.setAdapter(mAdapter);
		mLv_list.setNavListener(mAdapter);
		mAdapter.showHeadView(false);
		mAdapter.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (mAudios == null || mAudios.size() <= position) {
					LogUtil.loge(TAG + "index out of bounds index:" + position + " size:"
							+ (mAudios == null ? -1 : mAudios.size()));
					return;
				}
				if (LocalLogic.isValid(mAudios.get(position))) {
					MediaPlayerActivityEngine.getInstance().setAudios(mAudios,
							position);
					SharedPreferencesUtils.setAudioSource(Constant.HISTORY_TYPE);
					MediaPlayerActivityEngine.getInstance().playOrPause();
				} else {
					HistoryAudioDBHelper.getInstance().remove((mAudios.get(position)).getId());
					mAudios.remove(position);
					setDataChange();
				}
			}
		});
	}

	@Override
	public int getLayout() {
		return R.layout.list_with_nodata;
	}

	@Override
	public int getFragmentId() {
		// TODO Auto-generated method stub
		return 4;
	}

	@Override
	public void update(Observable observable, Object data) {
		if (data instanceof InfoMessage) {
			InfoMessage info = (InfoMessage) data;
			LogUtil.logd(TAG + "receiver  type =" + info.getType());
			switch (info.getType()) {
			case InfoMessage.NOTIFY_LOCAL_AUDIO:
				Audio audio = (Audio) info.getObj();
				if (audio != null && mAudios != null) {
					setDataChange();
				}
				break;
			case InfoMessage.PLAYER_CURRENT_AUDIO:
				Audio current = (Audio) info.getObj();
				if (current != null && mAudios != null&&Utils.isSong(current.getSid())) {
					if (mAudios.contains(current)) {
						mAudios.remove(current);
					}
					mAudios.add(0,current);
					setDataChange();
				}
				break;

			default:
				break;
			}
		}
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		if (hidden == false) {
			notifyData();
			setNavViewList();
		} else {
			View[] views = null;
			if (mLv_list.getAdapter().getItemCount() != 0) {
				views = new View[] { mLv_list };
			}
			ObserverManage.getObserver().send(InfoMessage.DELETE_HISTORY_VIEW_LIST, views);
			super.onPause();
		}
		super.onHiddenChanged(hidden);
	}
}
