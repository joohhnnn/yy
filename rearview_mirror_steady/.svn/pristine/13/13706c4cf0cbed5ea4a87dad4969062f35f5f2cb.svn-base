//package com.txznet.music.playerModule.logic;
//
//import com.txznet.audio.player.TXZAudioPlayer;
//import com.txznet.comm.remote.util.LogUtil;
//import com.txznet.loader.AppLogic;
//import com.txznet.music.albumModule.bean.Audio;
//import com.txznet.music.net.NetManager;
//import com.txznet.music.playerModule.logic.net.request.ReqThirdSearch;
//import com.txznet.music.utils.SyncCoreData;
//import com.txznet.music.utils.ToastUtils;
//import com.txznet.music.utils.Utils;
//import com.txznet.txz.util.runnables.Runnable1;
//
///**
// * 播放器的主要处理类
// *
// * @author ASUS User
// *
// */
//public abstract class IPluginPlayerClient {
//
//	private static final String TAG = "[Music][Plugin]";
//	private static TXZAudioPlayer audioPlayer;
//
//	public void preparing(Audio tempAudio) {
//
//		if (tempAudio == null) {
//			LogUtil.logw(TAG + "[play] audio is null");
//			return;
//		}
//		if (audioPlayer != null) {
//			audioPlayer.release();
//		}
//		audioPlayer = createPlayer(tempAudio);
//		if (null == audioPlayer) {
//			AppLogic.runOnUiGround(new Runnable1<Audio>(tempAudio) {
//
//				@Override
//				public void run() {
//					preparing(mP1);
//				}
//			}, 100);
//			return;
//		}
//		// audioPlayer.setOnBufferingUpdateListener();
//		// audioPlayer.setOnCompletionListener();
//		// audioPlayer.setOnErrorListener();
//		// audioPlayer.setOnPreparedListener();
//		// audioPlayer.setOnSeekCompleteListener();
//		// audioPlayer.setOnPlayProgressListener();
//
//		// 同时上报缓冲状态
//		SyncCoreData.syncCurPlayerBufferingStatus();
//		audioPlayer.prepareAsync();
//		// 假请求
//		if (null != tempAudio && !Utils.isSong(tempAudio.getSid())) {// 不是歌曲
//			ReqThirdSearch reqData = new ReqThirdSearch(tempAudio.getSid(),
//					tempAudio.getId(), 0);
//            NetManager.getInstance().fakeRequest(reqData);
//		}
//	}
//
//	public void play() {
//		if (audioPlayer != null) {
//			audioPlayer.start();
//		}
//	}
//
//	public void release() {
//		if (audioPlayer != null) {
//			audioPlayer.release();
//		}
//	}
//
//	public void pause() {
//		if (audioPlayer != null) {
//			audioPlayer.pause();
//		}else{
//			ToastUtils.showShortOnUI("xxxxxxxx");
//		}
//	}
//
//	public void seekTo( long position) {
//		if (audioPlayer != null) {
//			audioPlayer.seekTo(position);
//		}
//	}
//
//	public void setVolume(float volume) {
//		if (audioPlayer != null) {
//			audioPlayer.setVolume(volume);
//		}
//	}
//
//	public abstract TXZAudioPlayer createPlayer(Audio tempAudio);
//}
