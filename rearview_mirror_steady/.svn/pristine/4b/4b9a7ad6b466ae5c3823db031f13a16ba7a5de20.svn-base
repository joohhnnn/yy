package com.txznet.music.ui;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.txznet.audio.player.MediaError;
import com.txznet.audio.player.RemoteAudioPlayer;
import com.txznet.audio.player.SysAudioPlayer;
import com.txznet.audio.player.TXZAudioPlayer;
import com.txznet.audio.player.SessionManager.SessionInfo;
import com.txznet.audio.player.TXZAudioPlayer.OnErrorListener;
import com.txznet.audio.player.TXZAudioPlayer.OnPreparedListener;
import com.txznet.audio.player.TXZAudioPlayer.OnSeekCompleteListener;
import com.txznet.audio.player.audio.FileAudio;
import com.txznet.audio.player.audio.NetAudio;
import com.txznet.audio.player.audio.PlayerAudio;
import com.txznet.audio.server.LocalMediaServer;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.music.Constant;
import com.txznet.music.R;
import com.txznet.music.bean.req.ReqCategory;
import com.txznet.music.bean.req.ReqSearchAlbum;
import com.txznet.music.bean.response.Audio;
import com.txznet.music.bean.response.ResponseCache;
import com.txznet.music.engine.factory.TxzAudioPlayerFactory;
import com.txznet.music.utils.ImageUtils;
import com.txznet.music.utils.NetHelp;

public class TestActivity extends Activity {
	SeekBar mSeekBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTheme(android.R.style.Theme_Holo_Light_DarkActionBar);
		setContentView(R.layout.activity_test);
		mSeekBar = (SeekBar) findViewById(R.id.seekBar);
		mSeekBar.setMax(100);
		mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				if (mPlayer != null) {
					mPlayer.seekTo((int) (seekBar.getProgress() / 100f));
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
			}
		});

		// queryAll = Cache.queryAll();
		SpannableString spannableString = new SpannableString("我们都是喜欢孩子");
		spannableString.setSpan(new AbsoluteSizeSpan(10), 0, 4,
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		spannableString.setSpan(new AbsoluteSizeSpan(20), 4,
				spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		TextView tView = new TextView(this);
		// tView.setText(Html.fromHtml(getString(R.string.title_artist)));
		tView.setText(spannableString);
		LinearLayout llView = (LinearLayout) findViewById(R.id.ll_view);
		llView.addView(tView);
		WebView view = new WebView(this);
		view.loadData(getString(R.string.title_artist), "text/html", "utf-8");
		llView.addView(view);
		ImageView ivImageView = new ImageView(this);
		LayoutParams layoutParams = ivImageView.getLayoutParams();
		if (layoutParams == null) {
			layoutParams = new LayoutParams(100, 100);
		}
		ivImageView.setLayoutParams(layoutParams);
		llView.addView(ivImageView);
		ImageLoader
				.getInstance()
				.displayImage(
						"http://p.qpic.cn/music_cover/3y6bAxXh7FnIic7XGrg707CicLicqpa66SpXU5dup45FJdgupQJ1tasXA/300?n=1",
						ivImageView,
						ImageUtils.initDefault(R.drawable.fm_item_default, 0));
	}

	TXZAudioPlayer mPlayer;
	Timer mTimer;
	private List<ResponseCache> queryAll;
	private int size = 0;
	/**
	 * 重新进行播放
	 */
	Runnable replayRunnable = new Runnable() {

		@Override
		public void run() {
			play(null);
		}
	};
	/**
	 * @param view
	 */
	public void play(View view) {
		Audio currentAudio = new Audio();
		currentAudio
				.setStrDownloadUrl("http://127.0.0.1:"+LocalMediaServer.getInstance().getPort()+"/a.mp3");
//		.setStrDownloadUrl("http://cc.stream.qqmusic.qq.com/C200001jdCld0h4ssk.m4a?vkey=853EA7DF3B34103DE85CC93D2379EFF7D0F684D6D4CA142DAEEDBB2FFBFDC92619351B563C69F17790194E4A01C5C4C80A3E13D7DD192902&guid=4603ed0d9cdb2980b111a5e0f5a3a802&fromtag=0");
		currentAudio.setName("xx");
		currentAudio.setSid(1);
		mPlayer = RemoteAudioPlayer.createAudioPlayer(currentAudio);
//		mPlayer = TxzAudioPlayerFactory.createPlayer(currentAudio);
		if (mPlayer == null) {
			AppLogic.runOnBackGround(replayRunnable, 100);
			return;
		}
		try {
			mPlayer.prepareAsync();
		} catch (Exception e) {
			e.printStackTrace();
		}
		mPlayer.setOnPreparedListener(new TXZAudioPlayer.OnPreparedListener() {
			@Override
			public void onPrepared(TXZAudioPlayer ap) {
				mPlayer.start();
			}
		});
		mPlayer.setOnErrorListener(new OnErrorListener() {

			@Override
			public boolean onError(TXZAudioPlayer ap, MediaError err) {
				LogUtil.loge("error:" + err.getErrCode() + ","
						+ err.getErrHint());
				return true;
			}
		});
	}

	public void start(View view) {
		if (mPlayer != null) {
			mPlayer.start();
		}
	}

	public void pause(View view) {
		if (mPlayer != null) {
			mPlayer.pause();
		}
	}

	public void stop(View view) {
		if (mPlayer != null) {
			mPlayer.release();
			mPlayer = null;
			// mTimer.cancel();
			// mTimer = null;
		}
	}

	public void testNet(View view) {
		ReqCategory category = new ReqCategory();
		category.setbAll(1);
		category.setCategoryId(100000);
		NetHelp.sendRequest(Constant.GET_CATEGORY, category);
	}

	public void testGET_SEARCH_LIST(View view) {
		ReqSearchAlbum album = new ReqSearchAlbum();
		album.setCategoryId(200000);
		album.setOrderType(1);
		album.setPageId(1);
		NetHelp.sendRequest(Constant.GET_SEARCH_LIST, album);
	}
}
