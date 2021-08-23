package com.txznet.music.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.fm.dao.AudioDBHelper;
import com.txznet.fm.dao.HistoryAudioDBHelper;
import com.txznet.fm.dao.LocalAudioDBHelper;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.loader.AppLogic;
import com.txznet.music.Constant;
import com.txznet.music.R;
import com.txznet.music.adpter.SongListAdapter;
import com.txznet.music.adpter.SongListHistoryAdapter;
import com.txznet.music.bean.IFinishCallBack;
import com.txznet.music.bean.response.Audio;
import com.txznet.music.engine.MediaPlayerActivityEngine;
import com.txznet.music.engine.SongListFragmentEngine;
import com.txznet.music.fragment.base.BaseFragment;
import com.txznet.music.fragment.logic.LocalLogic;
import com.txznet.music.fragment.manager.LocalManager;
import com.txznet.music.helper.RequestHelpe;
import com.txznet.music.ui.MediaPlayerActivity;
import com.txznet.music.utils.AnimationUtils;
import com.txznet.music.utils.CollectionUtils;
import com.txznet.music.utils.DimenUtils;
import com.txznet.music.utils.FileUtils;
import com.txznet.music.utils.MyAsyncTask;
import com.txznet.music.utils.SharedPreferencesUtils;
import com.txznet.music.utils.ToastUtils;
import com.txznet.music.utils.Utils;
import com.txznet.music.widget.CustomDialog;
import com.txznet.txz.util.runnables.Runnable1;

public class SongListFragment extends BaseFragment implements Observer,
		OnClickListener {

	private ListView lvSongList;
	private View deleteLayout;
	private BaseAdapter adapter;
	private List<Audio> data = new ArrayList<Audio>();
	private static List<Audio> localData = new ArrayList<Audio>();
	private static List<Audio> historyData = new ArrayList<Audio>();
	private TextView ivNoData;
	CustomDialog dialog;

	private ViewGroup llTitle;

	boolean isDetach = false;
	private View btn_refresh;

	private SongListFragmentEngine engine;

	public final static String FRAGMENT_TYPE = "fragment_type";

	private int type;// 0.表示本地音乐，（需要从本地音乐库中获取数据）1.表示最近播放（需要从文件中读取播放顺序以及记录）

	private static class Instance {
		public static final SongListFragment fragment = new SongListFragment();
	}

	public static SongListFragment getInstance() {
		return Instance.fragment;
	}

	private View search_ll;
	private View refreshHeader;
	private LinearLayout llNodata;

	// View header;

	MyAsyncTask<Integer, List<Audio>> execute = null;

	private SongListFragment() {
		if (Constant.ISTESTDATA) {
			maxScanTime = 5;
		} else {
			maxScanTime = 30;
		}
	}

	int finalPosition = 0;

	Runnable1<Integer> toOtherAct = new Runnable1<Integer>(finalPosition) {

		@Override
		public void run() {
			final Audio audio = (Audio) adapter.getItem(finalPosition);
			// 先判断该歌曲是否存在
			// 歌曲不存在，则删除
			if(!LocalLogic.isValid(audio)){
				TtsUtil.speakResource("RS_VOICE_SPEAKNOTEXIST_TIPS",
						Constant.RS_VOICE_SPEAKNOTEXIST_TIPS);
					// 复制值
					List<Audio> audios = new ArrayList<Audio>();
					audios.addAll(data);
					LocalManager.getInstance().deleteNotExistFile(audios, new IFinishCallBack<Audio>() {

						@Override
						public void onComplete(List<Audio> result) {
							if (type == Constant.LOCAL_MUSIC_TYPE) {
								notifyAdapter(result);
								SongListFragment.getInstance().hiddenRefreshView();
							}else{
								data.clear();
								data.addAll(result);
								adapter.notifyDataSetChanged();
							}
						}

						@Override
						public void onError(String error) {

						}
					});
				return;
			}
			// MediaPlayerActivityEngine.getInstance().stop();
			if (Utils.isSong(audio.getSid())) {
				// SharedPreferencesUtils.setCurrentAlbumID(0);
				if (type == Constant.LOCAL_MUSIC_TYPE) {
					MediaPlayerActivityEngine.getInstance().setAudios(data,
							finalPosition);
				} else {

					List<Integer> sids = Utils.getSongSid();
					StringBuffer sb = new StringBuffer();
					for (int i = 0; i < sids.size(); i++) {
						if (i > 0) {
							sb.append(" or ");
						}
						sb.append("sid").append(" = ").append(sids.get(i));
					}

					List<Audio> historyMusics = HistoryAudioDBHelper
							.getInstance().findAll(Audio.class, sb.toString(),
									null, null);
					MediaPlayerActivityEngine.getInstance().setAudios(
							historyMusics, historyMusics.indexOf(audio));
				}
				// MediaPlayerActivityEngine.getInstance().setCurrentAlbum(type);
				MediaPlayerActivityEngine.getInstance().playOrPause();
			} else {
				List<Audio> queryData = AudioDBHelper.getInstance().find(
						Audio.class, "albumId == ?",
						new String[] { audio.getAlbumId() });
				// DBSQLHelper1.queryData(Audio.class,
				// new TypeToken<List<String>>() {
				// }.getType(), " where albumId == " + audio.getAlbumId());
				if (queryData == null || queryData.size() <= 0) {
					ToastUtils.showShort("数据已被清空，即将从网络上重新缓存");
					// 从网络上请求
					LogUtil.logd(TAG + "this history album"
							+ audio.getAlbumName() + "[" + audio.getAlbumId()
							+ "]" + " not exist on the db,get data from net");
					try {
						RequestHelpe.reqAudio(
								Long.parseLong(audio.getAlbumId()),
								audio.getSid(), 1, audio.getAlbumName(),
								Long.parseLong(audio.getStrCategoryId()));
					} catch (Exception e) {
						LogUtil.loge(TAG + "request error :id ="
								+ audio.getAlbumId() + ", categoryID="
								+ audio.getStrCategoryId() + e.toString());
					}
					return;

				}

				MediaPlayerActivityEngine.getInstance().setAudios(queryData,
						queryData.indexOf(audio));
				// MediaPlayerActivityEngine.getInstance().setCurrentAlbumName(
				// audio.getAlbumName());
				// MediaPlayerActivityEngine.getInstance().setCurrentAlbum(
				// Utils.toLong(audio.getAlbumId()));
				// SharedPreferencesUtils
				// .setCurrentAlbumID(MediaPlayerActivityEngine
				// .getInstance().getCurrentAlbum());
				MediaPlayerActivityEngine.getInstance().playOrPause();
			}
			AppLogic.runOnUiGround(new Runnable() {

				@Override
				public void run() {
					Intent intent = new Intent(getActivity(),
							MediaPlayerActivity.class);
					SharedPreferencesUtils.setAudioSource(type);
					startActivity(intent);// 跳转到播放器页面
				}
			}, 0);
		}
	};

	@Override
	public void bindViews() {
		lvSongList = (ListView) findViewById(R.id.lv_song_list);
		llNodata = (LinearLayout) findViewById(R.id.ll_nodata);
		ivNoData = (TextView) findViewById(R.id.nolist);
		deleteLayout = View.inflate(getActivity(),
				R.layout.layout_song_list_delete, null);
		refreshHeader = View.inflate(getActivity(),
				R.layout.ll_song_local_scan, null);
		search_ll = findViewById(R.id.search_ll);
		ivSearch = (ImageView) findViewById(R.id.search_iv);
		tvSearch = (TextView) findViewById(R.id.search_local_tv);
		tvRefreshTotal = (TextView) refreshHeader
				.findViewById(R.id.refresh_total_tv);
		llTitle = (ViewGroup) findViewById(R.id.ll_local_title);
		// header = findViewById(R.id.header);

	}

	@Override
	public void initListener() {
		deleteLayout.setOnClickListener(this);
		lvSongList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (type == Constant.LOCAL_MUSIC_TYPE) {
					position -= 1;
					if (position < 0) {
						return;
					}
				}
				finalPosition = position;
				LogUtil.logd(TAG + "item click pos:" + position + " audio:"
						+ data.get(position));
				AppLogic.removeBackGroundCallback(toOtherAct);
				AppLogic.runOnBackGround(toOtherAct, 0);
			}
		});
		refreshHeader.findViewById(R.id.refreshTv).setOnClickListener(this);
		llTitle.findViewById(R.id.refreshTv).setOnClickListener(this);
	}

	@Override
	public void initData() {
		engine = new SongListFragmentEngine(getActivity());
		type = getArguments().getInt(FRAGMENT_TYPE);// 默认是0
		data.clear();
		// 初始化弹框
		dialog = new CustomDialog(getActivity()).setMessage("确定删除所有记录吗？")
				.setSureListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						engine.removeAllMusicFromHistory();
						notifyAdapter(null);
						dialog.dismiss();
					}
				}).setCancleListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});

		// 初始化资源
		if (type == Constant.HISTORY_TYPE) {// 历史记录，从文件中读取数据
			data.addAll(historyData);
			if (null == deleteLayout.getLayoutParams()) {
				AbsListView.LayoutParams params = new AbsListView.LayoutParams(
						AbsListView.LayoutParams.MATCH_PARENT,
						DimenUtils.dip2Pixel(getActivity()
								.getApplicationContext(), 90));
				deleteLayout.setLayoutParams(params);
			}

			lvSongList.addFooterView(deleteLayout);
			ivNoData.setText("当前没有收听记录");
			adapter = new SongListHistoryAdapter(data, getActivity()
					.getApplicationContext(), type);
			llTitle.setVisibility(View.GONE);
		} else {// 从本地音乐库中读取数据
			data.addAll(localData);
			adapter = new SongListAdapter(data, getActivity()
					.getApplicationContext());
			ivNoData.setText("本地没有音乐");
			lvSongList.addHeaderView(refreshHeader);
			if (CollectionUtils.isEmpty(data)) {
				llTitle.setVisibility(View.VISIBLE);
			}
			refreshHeader();
		}

		lvSongList.setEmptyView(llNodata);
		lvSongList.setAdapter(adapter);

	}

	@Override
	public int getLayout() {
		return R.layout.fragment_song_list_layout;
	}

	public List<Audio> getDataFromLocalDB() {
		if (type != Constant.LOCAL_MUSIC_TYPE) {
			LogUtil.logd("type is error ,now is type=" + type);
			return null;
		}
		List<Audio> findAll = null;
		try {
			long startTime = SystemClock.elapsedRealtime();
			findAll = LocalAudioDBHelper.getInstance().findAll(Audio.class);
			LogUtil.logi("Collections.sort(data);time::"
					+ (SystemClock.elapsedRealtime() - startTime) + ",size::"
					+ findAll.size());
		} catch (Exception e) {
			LogUtil.loge("getDataFromLocal occurr error " + e.getMessage());
		}
		return findAll;
	}

	/**
	 * 必须在主线程中刷新组件
	 */
	public void refreshHeader() {
		if (type == Constant.HISTORY_TYPE) {
			return;
		}

		try {
			search_ll.setVisibility(View.GONE);
			if (CollectionUtils.isEmpty(data)) {
				ivSearch.clearAnimation();
				llNodata.setVisibility(View.VISIBLE);
				llTitle.setVisibility(View.VISIBLE);
				refreshHeader.setVisibility(View.GONE);
			} else {
				refreshHeader.setVisibility(View.VISIBLE);
				llTitle.setVisibility(View.GONE);
			}
			tvRefreshTotal.setText(GlobalContext.get().getString(
					R.string.total_song, data.size()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<Audio> getDataFromHistory() {
		return HistoryAudioDBHelper.getInstance().findAll(Audio.class);
	}

	public void refreshPosition() {
		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				if (null != adapter) {
					adapter.notifyDataSetChanged();
				}
			}
		}, 0);
	}

	@Override
	public int getFragmentId() {
		return 3;
	}

	public int geType() {
		return type;
	}

	public List<Audio> getAdapterData() {
		return data;
	}

	Animation animation = AnimationUtils.getRotateAnimation();
	private int size;
	private TextView tvSearch;
	private ImageView ivSearch;
	public TextView tvRefreshTotal;
	int progressCount = 0;
	private boolean pressScan;
	public boolean shouldCheckSDCard = true;

	public boolean getPressScan() {
		return pressScan;
	}

	Toast toast = null;

	public void scanMusic() {
		// AniUtil.startAnimation(aniLoading);
		if (pressScan) {
			AppLogic.runOnUiGround(new Runnable() {

				@Override
				public void run() {
					if (toast != null) {
						toast.cancel();
					}
					toast = Toast.makeText(getActivity(), R.string.str_loding,
							Toast.LENGTH_SHORT);
					toast.show();
					showRefreshView();
				}
			}, 100);
			return;
		}
		pressScan = true;
		tvSearch.setText(getString(R.string.search_local_song));

		execute = new MyAsyncTask<Integer, List<Audio>>() {
			protected void onPreExecute() {
				SongListFragment.getInstance().showRefreshView();
			};

			@Override
			protected List<Audio> doInBackground(Integer... params) {
				List<Audio> noOrder = LocalLogic.getDataFromSDCard();
				// 排序
				Collections.sort(noOrder, new Comparator<Audio>() {

					@Override
					public int compare(Audio lhs, Audio rhs) {
						return lhs.getPinyin().compareTo(rhs.getPinyin());
					}
				});

				LogUtil.logd(TAG + "scan size =" + noOrder.size());
				return noOrder;
			}

			@Override
			protected void onPostExecute(List<Audio> result) {
				if (type == Constant.LOCAL_MUSIC_TYPE) {
					notifyAdapter(result);
					SongListFragment.getInstance().hiddenRefreshView();
				}
				pressScan = false;
			}

			// @Override
			// protected void onCancelled() {
			// LogUtil.logd("is cancled");
			// pressScan = false;
			// super.onCancelled();
			// }

			protected void onCancelled(java.util.List<Audio> result) {
				pressScan = false;
				LogUtil.logd("is cancled with value");
			};

		}.execute();
	}

	/**
	 * 从主线程中刷新数据
	 * 
	 * @param nofifyData
	 */
	public void notifyAdapter(List<Audio> nofifyData) {
		LogUtil.logd(TAG + "[notify] notify adapter = "
				+ (null != nofifyData ? nofifyData.size() : 0));
		if (null != adapter) {
			data.clear();
			if (CollectionUtils.isNotEmpty(nofifyData)) {
				data.addAll(nofifyData);
			}
			if (nofifyData != null) {
				for (Audio audio : nofifyData) {
					LogUtil.logd(TAG + "[" + type + "]:" + audio);
				}
			}
			adapter.notifyDataSetChanged();
		}
		if (type == Constant.LOCAL_MUSIC_TYPE) {
			// 刷新头部
			refreshHeader();
		}
	}

	@Override
	public void reqData() {

	}

	// public Timer timer = new Timer();
	public int min = 0;
	public int maxScanTime = 30;
	private boolean isCancleTask;

	@Override
	public void onAttach(Activity context) {
		isDetach = false;
		super.onAttach(context);
	}

	@Override
	public void onDetach() {
		isDetach = true;
		super.onDetach();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onDestroy() {
		ObserverManage.getObserver().deleteObserver(this);
		super.onDestroy();
	}

	@Override
	public void onPause() {
		// if (!AsyncTaskManager.isInvalid(execute)) {
		// execute.cancel(true);
		// }
		super.onPause();
	}

	@Override
	public void onStart() {
		ObserverManage.getObserver().addObserver(this);
		LogUtil.logd("current Type is " + type);
		execute = new MyAsyncTask<Integer, List<Audio>>() {

			@Override
			protected List<Audio> doInBackground(Integer... params) {
				if (params.length > 0) {
					int type = params[0];
					if (type == Constant.HISTORY_TYPE) {// 历史记录，从文件中读取数据
						return getDataFromHistory();
					} else {
						return getDataFromLocalDB();
					}
				}
				return null;
			}

			protected void onPostExecute(java.util.List<Audio> result) {
				notifyAdapter(result);
				hiddenRefreshView();
			};

		}.execute(type);

		super.onStart();
	}

	@Override
	public void onStop() {
		if (type == Constant.HISTORY_TYPE) {// 历史记录，从文件中读取数据
			historyData.clear();
			historyData.addAll(data);
		} else {
			localData.clear();
			localData.addAll(data);
		}
		execute.cancel(true);
		isCancleTask = true;
		// timer.cancel();
		super.onStop();
	}

	/**
	 * 隐藏刷新界面
	 */
	public void hiddenRefreshView() {
		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				if (type == Constant.LOCAL_MUSIC_TYPE && lvSongList != null) {
					isCancleTask = true;
					lvSongList.setVisibility(View.VISIBLE);
					search_ll.setVisibility(View.GONE);
					ivSearch.clearAnimation();
				}
			}
		}, 0);
	}

	/**
	 * 显示刷新界面
	 */
	public void showRefreshView() {
		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				if (search_ll.getVisibility()==View.VISIBLE) {
					return;//显示加载中就不显示。
				}
				lvSongList.setVisibility(View.GONE);
				search_ll.setVisibility(View.VISIBLE);
				ivSearch.startAnimation(animation);
				llNodata.setVisibility(View.GONE);
				llTitle.setVisibility(View.GONE);
			}
		}, 0);
	}


	@Override
	public void update(Observable observable, Object data) {

		if (data instanceof InfoMessage) {
			InfoMessage info = (InfoMessage) data;
			switch (info.getType()) {
			case InfoMessage.DELETE_LOCAL_MUSIC:
				refreshHeader();
				break;
			case InfoMessage.REFRESH_LOCAL_POSITION:
				refreshPosition();
				break;

			default:
				break;
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.refreshTv:
			scanMusic();
			break;
		case R.id.tv_clear_history:
			dialog.show();
			break;

		default:
			break;
		}
	}

}
