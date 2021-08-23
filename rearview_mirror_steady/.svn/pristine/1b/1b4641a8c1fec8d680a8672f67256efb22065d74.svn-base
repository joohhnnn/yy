package com.txznet.audio.player.audio;

import java.io.File;
import java.util.Locale;

import android.os.Parcel;

public class FileAudio extends PlayerAudio {
	private String mPath;

	public FileAudio(String path) {
		mPath = path;
	}

	public String getPath() {
		return mPath;
	}

	@Override
	public boolean needCodecPlayer() {
		return mPath.toLowerCase(Locale.CHINESE).endsWith(".opus");
	}

	@Override
	public String getAudioName() {
		return new File(mPath).getName();
	}

//	@Override
//	public int describeContents() {
//		return 0;
//	}
//
//	@Override
//	public void writeToParcel(Parcel dest, int flags) {
//		super.writeToParcel(dest, flags);
//		dest.writeString(mPath);
//	}
//
//	public FileAudio(Parcel in) {
//		super(in);
//		mPath = in.readString();
//	}
}
