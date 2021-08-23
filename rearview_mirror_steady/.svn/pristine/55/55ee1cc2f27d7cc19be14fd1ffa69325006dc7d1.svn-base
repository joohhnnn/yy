package com.txznet.audio.player.audio;

import java.io.Serializable;

import android.media.AudioManager;

public class PlayerAudio implements Serializable {
	protected Integer mDurnation = null;
	protected float mLastPlayTime = 0F;
	protected static final String TAG = "music:PlayerAudio: ";

	public int getStreamType() {
		return AudioManager.STREAM_MUSIC;
	}

	public Integer getDuration() {
		return mDurnation;
	}

	public boolean needCodecPlayer() {
		return false;
	}

	public float getmLastPlayTime() {
		return mLastPlayTime;
	}

	public void setmLastPlayTime(float mLastPlayTime) {
		this.mLastPlayTime = mLastPlayTime;
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
	// public int describeContents() {
	// return 0;
	// }
	//
	// public void writeToParcel(Parcel out, int flags) {
	// if(mDurnation != null){
	// out.writeInt(mDurnation);
	// }
	// }
	//
	// protected PlayerAudio(Parcel in) {
	// mDurnation = in.readInt();
	// }
	//
	// protected PlayerAudio() {
	// }
	//
	// public static final Parcelable.Creator<PlayerAudio> CREATOR = new
	// Parcelable.Creator<PlayerAudio>() {
	// public PlayerAudio createFromParcel(Parcel in) {
	// return new PlayerAudio(in);
	// }
	//
	// public PlayerAudio[] newArray(int size) {
	// return new PlayerAudio[size];
	// }
	// };

}