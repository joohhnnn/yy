package com.txznet.txz.util.player;

import java.io.Serializable;

import android.media.AudioManager;

public class PlayerAudio implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected Integer mDurnation = null;

	public int getStreamType() {
		return AudioManager.STREAM_MUSIC;
	}

	public Integer getDuration() {
		return mDurnation;
	}

	public boolean needCodecPlayer() {
		return false;
	}

	/**
	 * 重写这个方法调试线程名字
	 * 
	 * @return
	 */
	public String getAudioName() {
		return "Audio";
	}
//
//	public int describeContents() {
//		return 0;
//	}
//
//	public void writeToParcel(Parcel out, int flags) {
//		if(mDurnation != null){
//			out.writeInt(mDurnation);
//		}
//	}
//
//	protected PlayerAudio(Parcel in) {
//		mDurnation = in.readInt();
//	}
//
//	protected PlayerAudio() {
//	}
//
//	public static final Parcelable.Creator<PlayerAudio> CREATOR = new Parcelable.Creator<PlayerAudio>() {
//		public PlayerAudio createFromParcel(Parcel in) {
//			return new PlayerAudio(in);
//		}
//
//		public PlayerAudio[] newArray(int size) {
//			return new PlayerAudio[size];
//		}
//	};

}