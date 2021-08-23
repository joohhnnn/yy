package com.txznet.debugtool.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.util.Log;

import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.debugtool.PlayRecordActivity;

public class AudioHelper {

	static AudioTrack track = null;
	static AudioRecord record = null;
	static boolean isPlaying = false;
	public static boolean isRecording = false;
	static int SAMPLE_RATE = 16 * 1000;
	static int CHANNEL_OUT = AudioFormat.CHANNEL_OUT_MONO;

	static String recordPath = PlayRecordActivity.AUDIO_SAVE_FILE_PREFIX
			+ "/test.pcm";

	static int frequency = 16 * 1000;
	static int channelIn = AudioFormat.CHANNEL_IN_MONO;
	static int channelConfig = AudioFormat.CHANNEL_OUT_MONO;
	static int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

	/**
	 * 
	 * @param filePath
	 */
	public static void playRow(String filePath) {
		if (isPlaying) {
			track.stop();
			track.flush();
			track.release();
			track = null;
		}
		File file = new File(filePath);
		isPlaying = true;
		int bufferSize = AudioTrack.getMinBufferSize(16000,
				CHANNEL_OUT, AudioFormat.ENCODING_PCM_16BIT);
		short[] buffer = new short[bufferSize / 4];
		try {

			InputStream is = new FileInputStream(file);
			DataInputStream dis=null;
			dis = new DataInputStream(new BufferedInputStream(is));
			track = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE,
					CHANNEL_OUT, AudioFormat.ENCODING_PCM_16BIT, bufferSize,
					AudioTrack.MODE_STREAM);
			track.play();

			while (isPlaying && dis.available() > 0) {
				int i = 0;
				while (dis.available() > 0 && i < buffer.length) {
					buffer[i] = dis.readShort();
					i++;
				}
				track.write(buffer, 0, buffer.length);
			}
			dis.close();

		} catch (Throwable t) {
			Log.e("AudioTrack", "Playback Failed");
		}
	}

	/**
	 * 按住录音
	 */
	public static void record() {
		isRecording = true;
		try {
			DataOutputStream dos = new DataOutputStream(
					new BufferedOutputStream(new FileOutputStream(recordPath)));
			int bufferSize = AudioRecord.getMinBufferSize(frequency, channelIn,
					audioEncoding);
			record = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency,
					channelIn, audioEncoding, bufferSize);
			short[] buffer = new short[bufferSize];
			record.startRecording();

			int r = 0;
			while (isRecording) {
				int bufferReadResult = record.read(buffer, 0, bufferSize);
				for (int i = 0; i < bufferReadResult; i++) {
					dos.writeShort(buffer[i]);
				}

				// publishProgress(new Integer(r));
				r++;
			}

			record.stop();
			dos.close();
			isRecording = false;
		} catch (Throwable t) {
			Log.e("AudioRecord", "Recording Failed");
		}
	}

	static MediaPlayer mPlayer = new MediaPlayer();

	/**
	 * MediaPlayer 播放
	 * 
	 * @param filePath
	 */
	public static void playMediaRow(String filePath) {
		try {
			mPlayer.reset();
			mPlayer.setAudioStreamType(TtsUtil.DEFAULT_TTS_STREAM);
			mPlayer.setDataSource(filePath);
			mPlayer.prepare();
			mPlayer.start();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
