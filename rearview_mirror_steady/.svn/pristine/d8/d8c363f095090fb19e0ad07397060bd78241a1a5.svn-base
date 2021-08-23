package com.txznet.sdkdemo.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.txznet.sdk.TXZMusicManager;
import com.txznet.sdk.TXZMusicManager.MusicModel;
import com.txznet.sdk.TXZMusicManager.MusicTool;
import com.txznet.sdk.TXZMusicManager.MusicToolStatusListener;
import com.txznet.sdk.TXZMusicManager.MusicToolType;
import com.txznet.sdk.TXZStatusManager;
import com.txznet.sdk.TXZStatusManager.StatusListener;
import com.txznet.sdk.music.TXZMusicTool;
import com.txznet.sdkdemo.bean.DebugUtil;
import com.txznet.sdkdemo.bean.DemoButton;

public class MusicActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addDemoButtons(new DemoButton(this, "快报推送", new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setAction("com.txznet.music.quick_report");
				sendBroadcast(intent);
				
				Intent intent1 = new Intent(Intent.ACTION_MAIN);
		        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);// 注意
		        intent1.addCategory(Intent.CATEGORY_HOME);
		        startActivity(intent1);
		        
				DebugUtil.showTips(((Button) v).getText());
			}
		}));
		
		addDemoButtons(new DemoButton(this, "设置音乐工具", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZMusicManager.getInstance().setMusicTool(mMusicTool);

				DebugUtil.showTips(((Button) v).getText());
			}
		}), new DemoButton(this, "取消音乐工具", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZMusicManager.getInstance().setMusicTool((MusicTool) null);

				DebugUtil.showTips(((Button) v).getText());
			}
		}), new DemoButton(this, "监听音乐状态", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZStatusManager.getInstance().addStatusListener(mMusicStatusListener);

				DebugUtil.showTips(((Button) v).getText());
			}
		}));

		addDemoButtons(new DemoButton(this, "同行者音乐", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZMusicManager.getInstance().setMusicTool(MusicToolType.MUSIC_TOOL_TXZ);
			}
		}), new DemoButton(this, "酷我音乐", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZMusicManager.getInstance().setMusicTool(MusicToolType.MUSIC_TOOL_KUWO);
			}
		}), new DemoButton(this, "考拉音乐", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZMusicManager.getInstance().setMusicTool(MusicToolType.MUSIC_TOOL_KAOLA);
			}
		}));

		addDemoButtons(new DemoButton(this, "播放", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZMusicManager.getInstance().play();

				DebugUtil.showTips(((Button) v).getText());
			}
		}), new DemoButton(this, "暂停", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZMusicManager.getInstance().pause();

				DebugUtil.showTips(((Button) v).getText());
			}
		}), new DemoButton(this, "上一首", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZMusicManager.getInstance().prev();

				DebugUtil.showTips(((Button) v).getText());
			}
		}), new DemoButton(this, "下一首", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZMusicManager.getInstance().next();

				DebugUtil.showTips(((Button) v).getText());
			}
		}));

		addDemoButtons(new DemoButton(this, "额外音乐列表", new OnClickListener() {
			@Override
			public void onClick(View v) {
				MusicModel model = new MusicModel();
				model.setTitle("测试歌曲");
				model.setArtist(new String[] { "测试歌手" });
				// 设置一个存在的路径测试
				model.setPath("/system/lib/libc.so");
				List<MusicModel> lst = new ArrayList<MusicModel>();
				lst.add(model);
				TXZMusicManager.getInstance().syncExMuicList(lst);
				DebugUtil.showTips(((Button) v).getText());
			}
		}), new DemoButton(this, "覆盖音乐列表", new OnClickListener() {
			@Override
			public void onClick(View v) {
				MusicModel model = new MusicModel();
				model.setTitle("测试节目");
				model.setArtist(new String[] { "测试艺术家" });
				// 设置一个存在的路径测试
				model.setPath("/system/lib/libz.so");
				List<MusicModel> lst = new ArrayList<MusicModel>();
				lst.add(model);
				TXZMusicManager.getInstance().syncMuicList(lst);
				DebugUtil.showTips(((Button) v).getText());
			}
		}));

		addDemoButtons(new DemoButton(this, "开启全屏", new OnClickListener() {

			@Override
			public void onClick(View v) {
				TXZMusicManager.getInstance().setFullScreen(true);
				DebugUtil.showTips(((Button) v).getText());
			}
		}), new DemoButton(this, "关闭全屏", new OnClickListener() {

			@Override
			public void onClick(View v) {
				TXZMusicManager.getInstance().setFullScreen(false);
				DebugUtil.showTips(((Button) v).getText());
			}
		}), new DemoButton(this, "打开应用", new OnClickListener() {

			@Override
			public void onClick(View v) {
				TXZMusicManager.getInstance().setNotOpenAppPName(new String[]{"com.txznet.music"});
				DebugUtil.showTips(((Button) v).getText());
			}
		}), new DemoButton(this, "取消打开应用", new OnClickListener() {

			@Override
			public void onClick(View v) {
				TXZMusicManager.getInstance().setNotOpenAppPName(new String[]{""});
				DebugUtil.showTips(((Button) v).getText());
			}
		}));
		addDemoButtons(new DemoButton(this, "开启悬浮播放器", new OnClickListener() {

			@Override
			public void onClick(View v) {
				TXZMusicManager.getInstance().setEnableFloatingPlayer(true);
				DebugUtil.showTips(((Button) v).getText());
			}
		}), new DemoButton(this, "关闭悬浮播放器", new OnClickListener() {

			@Override
			public void onClick(View v) {
				TXZMusicManager.getInstance().setEnableFloatingPlayer(false);
				DebugUtil.showTips(((Button) v).getText());
			}
		}), new DemoButton(this, "打开闪屏页", new OnClickListener() {

			@Override
			public void onClick(View v) {
				TXZMusicManager.getInstance().setEnableSplash(true);
				DebugUtil.showTips(((Button) v).getText());
			}
		}), new DemoButton(this, "关闭闪屏页", new OnClickListener() {

			@Override
			public void onClick(View v) {
				TXZMusicManager.getInstance().setEnableSplash(false);
				DebugUtil.showTips(((Button) v).getText());
			}
		}));
		addDemoButtons(new DemoButton(this, "打开快报推送", new OnClickListener() {

			@Override
			public void onClick(View v) {
				TXZMusicManager.getInstance().setShortPlayEnable(true);
				DebugUtil.showTips(((Button) v).getText());
			}
		}),new DemoButton(this, "关闭快报推送", new OnClickListener() {

			@Override
			public void onClick(View v) {
				TXZMusicManager.getInstance().setShortPlayEnable(false);
				DebugUtil.showTips(((Button) v).getText());
			}
		}));
		
		
	}

	private MusicTool mMusicTool = new MusicTool() {

		@Override
		public void unfavourMusic() {
			DebugUtil.showTips("取消收藏当前音乐");
		}

		@Override
		public void switchSong() {
			DebugUtil.showTips("切换音乐");
		}

		@Override
		public void switchModeRandom() {
			DebugUtil.showTips("随机播放模式");
		}

		@Override
		public void switchModeLoopOne() {
			DebugUtil.showTips("单曲循环模式");
		}

		@Override
		public void switchModeLoopAll() {
			DebugUtil.showTips("全部循环模式");
		}

		@Override
		public void setStatusListener(MusicToolStatusListener listener) {
			// TODO 状态监听器，如果实现自己的音乐工具，请将监听器记录下来，再对应状态变化时，使用该监听器来通知同行者
		}

		@Override
		public void prev() {
			DebugUtil.showTips("播放上一首");
		}

		@Override
		public void playRandom() {
			DebugUtil.showTips("随便听听");
		}

		@Override
		public void playMusic(MusicModel musicModel) {
			String title = musicModel.getTitle();
			String album = musicModel.getAlbum();
			String[] artist = musicModel.getArtist();
			String[] keywords = musicModel.getKeywords();
			TXZMusicTool.getInstance().playMusic(musicModel);
			DebugUtil.showTips("搜索标题是" + title + "专辑名称是" + album + "歌手是"
					+ DebugUtil.convertArrayToString(artist) + "关键字是"
					+ DebugUtil.convertArrayToString(keywords) + "的歌曲并播放");
		}

		@Override
		public void playFavourMusic() {
			DebugUtil.showTips("播放收藏歌曲");
		}

		@Override
		public void play() {
			DebugUtil.showTips("开始播放歌曲");
		}

		@Override
		public void pause() {
			DebugUtil.showTips("暂停播放歌曲");
		}

		@Override
		public void next() {
			DebugUtil.showTips("播放下一首");
		}

		@Override
		public boolean isPlaying() {
			// TODO 返回真实的播放器状态
			return false;
		}

		@Override
		public MusicModel getCurrentMusicModel() {
			// TODO 返回真实的播放的歌曲信息
			return null;
		}

		@Override
		public void favourMusic() {
			DebugUtil.showTips("收藏当前音乐");
		}

		@Override
		public void exit() {
			DebugUtil.showTips("退出音乐");
		}

		@Override
		public void continuePlay() {
			// TODO Auto-generated method stub
			
		}
	};

	StatusListener mMusicStatusListener = new StatusListener() {
		@Override
		public void onMusicPlay() {
			MusicModel model = TXZMusicManager.getInstance()
					.getCurrentMusicModel();
			if (model != null) {
				DebugUtil.showTips("开始播放：" + model.getTitle() + "-"
						+ DebugUtil.convertArrayToString(model.getArtist()));
			} else {
				DebugUtil.showTips("开始播放未知音乐");
			}
		}

		@Override
		public void onMusicPause() {
			DebugUtil.showTips("停止播放音乐");
		}

		@Override
		public void onEndTts() {
		}

		@Override
		public void onEndCall() {
		}

		@Override
		public void onEndAsr() {
		}

		@Override
		public void onBeginTts() {
		}

		@Override
		public void onBeginCall() {
		}

		@Override
		public void onBeginAsr() {
		}

		@Override
		public void onBeepEnd() {
		}
	};
}
