package com.txznet.debugtool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;

import com.txznet.loader.AppLogic;
import com.txznet.txz.util.runnables.Runnable1;
import com.txznet.widget.DebugButton;

public class PlayRecordActivity extends BaseDebugActivity {

	public static final String AUDIO_SAVE_FILE_PREFIX = Environment
			.getExternalStorageDirectory().getPath() + "/txz/voice";

	private static String getDateStr(long millis) {
		Date d = new Date(millis);
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss",
				Locale.CHINA);
		return df.format(d);
	}

	@Override
	protected void onInitButtons() {
		File d = new File(AUDIO_SAVE_FILE_PREFIX);
		String[] fs = d.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				if (filename.endsWith(".pcm"))
					return true;
				return false;
			}
		});
		for (int i = fs.length; i > 0; --i) {
			final File ff = new File(d, fs[i - 1]);
			long t = ff.lastModified();
			int c = DebugButton.COLOR1;
			String dur = (ff.length() / (16000 * 16 / 8)) + "秒";
			if (ff.length() % 10 == 4) {
				c = DebugButton.COLOR4;
				dur = "未唤醒";
			} else if (ff.length() % 10 == 2) {
				c = DebugButton.COLOR3;
				dur = "唤醒";
			} else if (ff.getName().startsWith("rec_")) {
				c = DebugButton.COLOR2;
				dur = "录音" + dur;
			}

			String label = fs[i - 1] + "   时间[" + getDateStr(t) + "]    时长["
					+ dur + "]";
			addDemoButtons(new DebugButton(this, label, c,
					new OnClickListener() {
						@Override
						public void onClick(View v) {
							clearAudio();
							AppLogic.runOnBackGround(new Runnable1<File>(ff) {
								@Override
								public void run() {
									try {
										FileInputStream in = new FileInputStream(
												ff);
										byte[] data = new byte[(int) ff
												.length()];
										in.read(data);
										in.close();
										mAudioTrack = new AudioTrack(
												AudioManager.STREAM_MUSIC,
												16000,
												AudioFormat.CHANNEL_OUT_MONO,
												AudioFormat.ENCODING_PCM_16BIT,
												10 * 1024 * 1024,
												AudioTrack.MODE_STATIC);
										mAudioTrack.write(data, 0, data.length);
										mAudioTrack.play();
									} catch (Exception e) {
									}
								}
							}, 0);
						}
					}));
		}
	}

	AudioTrack mAudioTrack;

	protected void clearAudio() {
		try {
			if (mAudioTrack != null) {
				mAudioTrack.flush();
				mAudioTrack.release();
				mAudioTrack = null;
			}
		} catch (Exception e) {
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		clearAudio();
	}
}
