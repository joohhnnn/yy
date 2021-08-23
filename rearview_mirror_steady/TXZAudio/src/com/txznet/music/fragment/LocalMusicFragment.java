package com.txznet.music.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.fm.dao.LocalAudioDBHelper;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.music.Constant;
import com.txznet.music.R;
import com.txznet.music.adpter.LocalAudioAdapter;
import com.txznet.music.bean.IFinishCallBack;
import com.txznet.music.bean.response.Audio;
import com.txznet.music.engine.MediaPlayerActivityEngine;
import com.txznet.music.fragment.logic.LocalLogic;
import com.txznet.music.fragment.manager.LocalManager;
import com.txznet.music.utils.AnimationUtils;
import com.txznet.music.utils.FileUtils;
import com.txznet.music.utils.SharedPreferencesUtils;
import com.txznet.music.widget.LinearLayoutManagerWrapper;
import com.txznet.music.widget.NavListener;
import com.txznet.music.widget.NavRecyclerView;
import com.txznet.txz.util.NavBtnSupporter.NavBtnSupporter;

import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * @author telenewbie
 * @version 创建时间：2016年5月7日 下午6:22:51
 * 
 */
public class LocalMusicFragment extends BaseDataFragment<Audio> implements OnClickListener, Observer {

	// 使用单例
	public static class MyInstance {
		public static LocalMusicFragment instance = new LocalMusicFragment();
	}

	protected static final String Audio = null;

	private NavRecyclerView mLvLocal;
	private LocalAudioAdapter mAdapter;
	private TextView mTvNoData;
	private View mRefreshView;
	private LinearLayout mLLnoDataView;
	private View mLLSearch;
	private ImageView mIvSearch;
	private TextView mTvSearch;
	private TextView tvNoDataRefresh;

	Animation animation = AnimationUtils.getRotateAnimation();

	// //////////////
	private List<Audio> mAudios = new ArrayList<Audio>();
	private View mRlLocalSongs;
	private TextView mType;

	@Override
	public void reqData() {
	}

	@Override
	public void onStart() {
		// 加入
		ObserverManage.getObserver().addObserver(this);
		super.onStart();
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		LogUtil.logd(TAG + "local onHiddenChanged:" + hidden);
		if (hidden == false) {
			notifyData();
		}
		super.onHiddenChanged(hidden);
	}

	@Override
	public void bindViews() {
		mLvLocal = (NavRecyclerView) findViewById(R.id.lv_song_list);
		mTvNoData = (TextView) findViewById(R.id.nolist);
		mLLnoDataView = (LinearLayout) findViewById(R.id.ll_nodata);
		mRefreshView = View.inflate(getActivity(), R.layout.merge_refresh_btn, mLLnoDataView);
		tvNoDataRefresh = (TextView) mRefreshView.findViewById(R.id.refreshTv);

		mLLSearch = findViewById(R.id.search_ll);
		mIvSearch = (ImageView) findViewById(R.id.search_iv);
		mTvSearch = (TextView) findViewById(R.id.search_local_tv);

		mRlLocalSongs = findViewById(R.id.rl_local_songs);

		mType = (TextView) findViewById(R.id.type);
		mType.setText(R.string.localMusic);

	}

	View[] mViewList = new View[] {};

	private void setNavViewList(View[] views) {
		if(!isHidden()){
			LogUtil.logd("NAVBtn:local fragment set views " + (views == null ? 0 : views.length));
			ObserverManage.getObserver().send(InfoMessage.ADD_VIEW_LIST, views);
		}
	}
	
	private void setNavCurrentView(View view){
		if(!isHidden()){
			ObserverManage.getObserver().send(InfoMessage.SET_CURRENT_VIEW, view);
		}
	}

	@Override
	public void onResume() {
//		ObserverManage.getObserver().send(InfoMessage.ADD_VIEW_LIST, mViewList);
		setNavViewList(mViewList);
		super.onResume();
	}

	@Override
	public void onPause() {
		ObserverManage.getObserver().send(InfoMessage.DELETE_VIEW_LIST, mViewList);
		super.onPause();
	}

	private void setDataChange() {
		mAdapter.notifyDataSetChanged();
		if (mAdapter.getItemCount() != 0) {
			mViewList = new View[] { mLvLocal };
		} else {
			mViewList = new View[] { tvNoDataRefresh };
		}
		setNavViewList(mViewList);
	}

	@Override
	public void initListener() {
		mRefreshView.findViewById(R.id.refreshTv).setOnClickListener(this);
	}

	@Override
	public void initData() {
		mAdapter = new LocalAudioAdapter(mAudios, getActivity().getApplicationContext());
		mAdapter.showHeadView(true);
		mTvNoData.setText("本地没有音乐");
		// mLvLocal.addHeaderView(refreshHeader);
		mLvLocal.setLayoutManager(new LinearLayoutManagerWrapper(getActivity(), "LocalMusicFragment"));
		mLvLocal.setEmptyView(mLLnoDataView);
		mLvLocal.setAdapter(mAdapter);
		mLvLocal.setNavListener(mAdapter);

		// mAdapter.setOffset(1);
		mAdapter.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (position >= 0) {
					Audio tempAudio = null;
					try {
						tempAudio = mAudios.get(position);
					} catch (Exception e) {
						setDataChange();
						LogUtil.loge(TAG + "[Exception] click", e);
						return;
					}
					if (LocalLogic.isValid(tempAudio)) {
						MediaPlayerActivityEngine.getInstance().setAudios(mAudios, position);
						// MediaPlayerActivityEngine.getInstance()
						// .setCurrentAlbum(Constant.LOCAL_MUSIC_TYPE);
						SharedPreferencesUtils.setAudioSource(Constant.LOCAL_MUSIC_TYPE);
						MediaPlayerActivityEngine.getInstance().playOrPause();
					} else {
						TtsUtil.speakResource("RS_VOICE_SPEAKNOTEXIST_TIPS", Constant.RS_VOICE_SPEAKNOTEXIST_TIPS);
						// 复制值
						List<Audio> audios = new ArrayList<Audio>();
						audios.addAll(mAudios);
						LocalManager.getInstance().deleteNotExistFile(audios, new IFinishCallBack<Audio>() {

							@Override
							public void onComplete(List<Audio> result) {
								LocalMusicFragment.this.notify(result);
								// SongListFragment.getInstance()
								// .hiddenRefreshView();
							}

							@Override
							public void onError(String error) {

							}
						});

					}
				}
			}
		});

		mAdapter.setOnItemDeleteListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Audio tempAudio;
				try {
					tempAudio = mAudios.get(position);
				} catch (Exception e) {
					setDataChange();
					LogUtil.loge(TAG + "[Exception] delete", e);
					return;
				}
				FileUtils.delFile(tempAudio.getStrDownloadUrl());
				// 从本地中删除数据
				LocalAudioDBHelper.getInstance().remove(tempAudio.getId());
				// 更新界面
				mAudios.remove(position);
				setDataChange();
			}
		});
		
		mAdapter.setOnScanListener(new NavListener.OnRefreshListener() {
			
			@Override
			public void onRefresh(int position) {
				mAudios.clear();
				mAdapter.notifyDataSetChanged();
				mLvLocal.onNavOperation(NavBtnSupporter.NAV_BTN_BACK);
				new LocalLogic().scanMusic();
			}
		});
	}

	@Override
	public int getLayout() {
		return R.layout.fragment_local;
	}

	@Override
	public int getFragmentId() {
		return 0;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.refreshTv:
			setNavViewList(new View[] { mLvLocal });
			setNavCurrentView(mLvLocal);
			new LocalLogic().scanMusic();
			break;

		default:
			break;
		}
	}

	private void beginScan() {
		mLLSearch.setVisibility(View.VISIBLE);
		mIvSearch.startAnimation(animation);
		mRlLocalSongs.setVisibility(View.GONE);
	}

	private void endScan() {
		// 从数据库中获取并显示
		// IO操作，需要在线程中
		mLLSearch.setVisibility(View.GONE);
		mRlLocalSongs.setVisibility(View.VISIBLE);
		// 停止动画
		mIvSearch.clearAnimation();
		notify(LocalManager.getInstance().getLocalAudios());
	}

	@Override
	public void notify(List<Audio> audios) {
		this.mAudios.clear();
		if (audios != null) {
			this.mAudios.addAll(audios);
		}

		setDataChange();

	}

	@Override
	public void update(Observable observable, Object data) {
		if (data instanceof InfoMessage) {
			InfoMessage info = (InfoMessage) data;
			LogUtil.logd(TAG + "receiver  type =" + info.getType());

			switch (info.getType()) {
			case InfoMessage.SCAN_STATED:
				// 扫描开始
				beginScan();
				break;
			case InfoMessage.SCAN_FINISHED:
				// 扫描完成
				endScan();
				break;
			case InfoMessage.NOTIFY_LOCAL_AUDIO:
				Audio audio = (Audio) info.getObj();
				if (audio != null && mAudios != null) {
					setDataChange();
				}
				break;
			case InfoMessage.REFRESH_LOCAL_POSITION:
				// refreshPosition();
				// TODO:刷新位置
				break;

			default:
				break;
			}
		}
	}

	@Override
	public List<Audio> getDataFromLocal() {
		return LocalAudioDBHelper.getInstance().findAll(Audio.class);
	}

}
