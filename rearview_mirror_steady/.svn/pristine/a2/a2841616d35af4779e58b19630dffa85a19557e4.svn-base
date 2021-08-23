package com.txznet.audio.player.audio;

import java.io.File;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.bean.response.Audio;
import com.txznet.music.utils.CollectionUtils;
import com.txznet.music.utils.DataInterfaceBroadcastHelper;
import com.txznet.music.utils.JsonHelper;
import com.txznet.music.utils.Utils;
import com.txznet.sdk.TXZMusicManager;
import com.txznet.sdk.TXZMusicManager.MusicModel;

public class NetAudio extends PlayerAudio {
	protected Audio mAudio;
	public static final String UNDERLINE = "_";
	public static final String POSTFIX = ".tmd";
	

	public NetAudio(Audio audio) {
		mAudio = audio;
		if (getFinalFile() != null) {
			tempAudio = new Audio();
			tempAudio.setStrDownloadUrl(getFinalFile()==null?mAudio.getStrDownloadUrl():getFinalFile().getAbsolutePath());
			tempAudio.setSid(mAudio.getSid());
			tempAudio.setName(mAudio.getName());
			tempAudio.setArrArtistName(mAudio.getArrArtistName());
			tempAudio.setDuration(mAudio.getDuration());
			tempAudio.setId(mAudio.getId());
			
		}
		mDurnation=(int) audio.getDuration();
		try{
			LogUtil.logd(TAG+audio.getLastPlayTime());
			mLastPlayTime=Float.parseFloat(audio.getLastPlayTime());
		}catch(Exception e){
			
		}
	}

	@Override
	public String getAudioName() {
		return mAudio.getName();
	}

	public String getUrl() {
		return mAudio.getStrDownloadUrl();
	}

	@Override
	public boolean needCodecPlayer() {
		return mAudio.getStrDownloadUrl().toLowerCase(Locale.CHINESE).endsWith(".opus");
	}

	File mCacheFile = null;
	private Audio tempAudio;

	public File getCacheDir() {
		if (mCacheFile == null) {
			try {
				if (Utils.isSong(mAudio.getSid())) {
					mCacheFile = new File(Environment.getExternalStorageDirectory(), "txz/cache/song");
				} else {
					mCacheFile = new File(Environment.getExternalStorageDirectory(), "txz/cache/other");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return mCacheFile;
	}

	public File getFinalFile() {
		if (Utils.isSong(mAudio.getSid())) {
			return new File(Environment.getExternalStorageDirectory(), "txz/audio/song/" + mAudio.getId() + UNDERLINE + mAudio.getSid() + POSTFIX);
		}
		return null;
	}

	protected String calCacheId(String key) {
		try {
			MessageDigest mdInst = MessageDigest.getInstance("MD5");
			byte[] md5Bytes = mdInst.digest(key.getBytes());
			StringBuilder hexValue = new StringBuilder();
			for (int i = 0; i < md5Bytes.length; i++) {
				int val = ((int) md5Bytes[i]) & 0xff;
				if (val < 16)
					hexValue.append("0");
				hexValue.append(Integer.toHexString(val));
			}
			return hexValue.toString();
		} catch (Exception e) {
			return null;
		}
	}

	public String getCacheId() {
		return calCacheId(this.getUrl());
	}

	public byte[] getAudioInfo() {
		return JsonHelper.toJson(tempAudio).getBytes();
	}

	/**
	 * // 发送广播给客户端，提示下载完成的通知
	 */
	public void onDownloadComplete() {
		DataInterfaceBroadcastHelper.sendDownloadBroadcast(tempAudio);
		sendToCore();
	}

	/**
	 * TODO 同步给同行者,做离线识别，必须在主进程全量同步
	 */
	private void sendToCore() {
		MusicModel model = new MusicModel();
		model.setAlbum(mAudio.getAlbumName());
		model.setArtist(CollectionUtils.toStrings(mAudio.getArrArtistName()));
		model.setTitle(mAudio.getName());
		model.setPath(mAudio.getStrDownloadUrl());
		List<MusicModel> musics = new ArrayList<TXZMusicManager.MusicModel>();
		musics.add(model);
		TXZMusicManager.getInstance().syncExMuicList(musics);
	}

//	@Override
//	public void writeToParcel(Parcel dest, int flags) {
//		super.writeToParcel(dest, flags);
//		dest.writeSerializable(mAudio);
//	}
//
//	public static final Parcelable.Creator<NetAudio> CREATOR = new Parcelable.Creator<NetAudio>() {
//		public NetAudio createFromParcel(Parcel in) {
//			return new NetAudio(in);
//		}
//
//		public NetAudio[] newArray(int size) {
//			return new NetAudio[size];
//		}
//	};
//
//	protected NetAudio(Parcel in) {
//		super(in);
//		mAudio=(Audio) in.readSerializable();
//	}

}
