package com.txznet.debugtool;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.txznet.audio.codec.TXZAudioEchoCancel;
import com.txznet.audio.codec.TXZAudioResampler;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.RecorderUtil;
import com.txznet.comm.remote.util.RecorderUtil.RecordCallback;
import com.txznet.comm.remote.util.RecorderUtil.RecordOption;
import com.txznet.comm.ui.dialog.CommonDialog;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZConfigManager;
import com.txznet.txz.util.recordcenter.AesDte;
import com.txznet.widget.DebugButton;
import com.txznet.widget.DebugUtil;

public class RecordTestActivity extends BaseDebugActivity {
	public final String TAG = "RecordTestActivity";
	String currentName = "";
	
	int mFormat = AudioFormat.ENCODING_PCM_16BIT;
	AudioRecord mAudioRecord;
	AudioTrack mAudioTrack;
	byte[] mBuffer = new byte[50 * 1024 * 1024];
	int mBufferCount = 0;
	long mRecordTime = 0;
	MediaPlayer mMediaPlayer = new MediaPlayer();
	private int FILE_SELECT_CODE = 101;


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode ==RESULT_OK&&requestCode==FILE_SELECT_CODE){
			try{
				//?????????????????????
				Uri uri = data.getData();
				String[] proj = {MediaStore.Images.Media.DATA};
				Cursor actualimagecursor = getContentResolver().query(uri, proj, null, null, null);
				int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				if (actualimagecursor.moveToFirst()){
					path = actualimagecursor.getString(actual_image_column_index);
				}
				if (path == null){
					if (uri.getPath().split(":").length>=2){
						path = uri.getPath().split(":")[1];
						path = "/sdcard/"+path;
					}
				}
				Log.d(TAG, "onActivityResult: path = "+path+"----uri == "+uri.getPath());
				showFilePathEdit();
			}catch (Exception e){
				e.printStackTrace();
				Toast.makeText(RecordTestActivity.this,"??????????????????",Toast.LENGTH_SHORT).show();
			}

		}
	}

	private void showFilePathEdit() {
		final Dialog dialog = new Dialog(this);
		dialog.setTitle("????????????");
		LinearLayout rootLayout = new LinearLayout(this);
		rootLayout.setOrientation(LinearLayout.VERTICAL);
		//?????????
		final EditText editText = new EditText(this);
		editText.setText(path==null?"/sdcard/txz/":path);

		//?????????
		Button submit = new Button(this);
		submit.setText("??????");
		submit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				path = editText.getText().toString();
				mRecordTime = System.currentTimeMillis();
				try {
					readPCMData(path);
					dialog.dismiss();
				} catch (IOException e) {
					e.printStackTrace();
					Toast.makeText(RecordTestActivity.this,"??????????????????",Toast.LENGTH_SHORT).show();
				}
			}
		});
		rootLayout.addView(editText);
		rootLayout.addView(submit);
		ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(-1,-1);
		dialog.addContentView(rootLayout,layoutParams);
		dialog.show();
	}

	public void startRecordAudioMono(int audioSource) {
		startRecordAudioInner(16000, audioSource, AudioFormat.CHANNEL_IN_MONO);
	}

	public void startRecordAudioStereo(int audioSource) {
		startRecordAudioInner(48000, audioSource, AudioFormat.CHANNEL_IN_STEREO);
	}

	public void startRecordAudioResample(int audioSource) {
		startRecordAudioInner(44100, audioSource, AudioFormat.CHANNEL_IN_MONO);
	}

	public void startRecordAudioInner(final int rate, final int audioSource,
			final int channel) {
		clearAudio(true);
		TXZConfigManager.getInstance().enableWakeup(false);
		DebugUtil.showTips("??????????????????");
		AppLogic.runOnUiGround(new Runnable() {
			@Override
			public void run() {
				mRecordTime = System.currentTimeMillis();
				mAudioRecord = new AudioRecord(audioSource, rate, channel,
						mFormat, AudioRecord.getMinBufferSize(rate, channel,
								mFormat));
				final TXZAudioResampler.Resampler resampler = rate != 16000 ? new TXZAudioResampler.Resampler(
						channel == AudioFormat.CHANNEL_IN_MONO ? 1 : 2, rate,
						16000) : null;
				
//				final TXZAudioResampler.Resampler resampler = null;
				new Thread(new Runnable() {
					@Override
					public void run() {
						
						try {
							mAudioRecord.startRecording();	
							DebugUtil.showTips("???????????????");
						} catch (Exception e) {
							e.printStackTrace();
							DebugUtil.showTips("?????????????????????????????????!");
							clearAudio(true);
							return;
						}
						
						long count = 0;
						long start = System.currentTimeMillis();
						int n = 0;
						while (true) {
							if (mAudioRecord != null) {
								if ((n = mAudioRecord.read(mBuffer,
										mBufferCount, 12000)) > 0) {
									Log.d(TAG, "record data: " + n + "/"
											+ mBufferCount);
									if (resampler != null) {
										int r = resampler.resample(mBuffer,
												mBufferCount, n, mBuffer,
												mBufferCount);
										mBufferCount += r;
									} else {
										mBufferCount += n;
									}
									long cc = Math.round((System
											.currentTimeMillis() - start) / 1000.0);
									if (cc != count) {
										count = cc;
										DebugUtil.showTips("?????????" + count + "???");
									}
								} else {
									if (n < 0) {
										try {
											DebugUtil.showTips("??????????????????" + n
													+ "?????????"
													+ mAudioRecord.getState());
										} catch (Exception e) {
										}
									}
									break;
								}
							}
						}
						Log.d(TAG, "record data end");
						if (resampler != null) {
							resampler.release();
						}
					}
				}).start();
			}
		}, 2000);
	}

	public void clearAudio(boolean clearData) {
		Log.d(TAG, "clearAudio begin");
		RecorderUtil.stop();
		AudioManager mAm = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mAm.setStreamMute(AudioManager.STREAM_MUSIC, false);
		mAm.abandonAudioFocus(mOnAudioFocusChangeListener);
		TXZConfigManager.getInstance().enableWakeup(true);
		Log.d(TAG, "clearAudio release record");
		if (mAudioRecord != null) {
			AudioRecord ar = mAudioRecord;
			mAudioRecord = null;
			// ar.stop();
			try {
				ar.release();				
			} catch (Exception e) {

			}
			ar = null;
		}
		Log.d(TAG, "clearAudio release track");
		if (mAudioTrack != null) {
			AudioTrack at = mAudioTrack;
			mAudioTrack = null;
			// at.stop();
			try {
				at.release();
			} catch (Exception e) {

			}
		}
		if (clearData) {
			Log.d(TAG, "clearAudio clear data");
			mBufferCount = 0;
		}
	}

	private void beforePlay() {
		AudioManager mAm = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mAm.setStreamMute(AudioManager.STREAM_MUSIC, false);
		mAm.requestAudioFocus(mOnAudioFocusChangeListener,
				AudioManager.STREAM_MUSIC,
				AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
	}

	OnAudioFocusChangeListener mOnAudioFocusChangeListener = new OnAudioFocusChangeListener() {
		@Override
		public void onAudioFocusChange(int focusChange) {
		}
	};

	private void playAssetsWav(String file) {
		try {
			AssetFileDescriptor fileDescriptor = GlobalContext.get()
					.getAssets().openFd(file);
			mMediaPlayer
					.setDataSource(fileDescriptor.getFileDescriptor(),
							fileDescriptor.getStartOffset(),
							fileDescriptor.getLength());
			mMediaPlayer.prepare();
			mMediaPlayer.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onInitButtons() {
		addDemoButtons(new DebugButton(this, "????????????", new OnClickListener() {
			@Override
			public void onClick(View v) {
				File f = new File("/sdcard/txz/recordTest");
				if(f.exists()&&f.isDirectory()){
					for (File pcm : f.listFiles()) {
						pcm.delete();
					}
				}
				DebugUtil.showTips("???????????????");
			}
		}), new DebugButton(this, "????????????", new OnClickListener() {
			@Override
			public void onClick(View v) {
				startRecordAudioMono(MediaRecorder.AudioSource.DEFAULT);
			}
		}), new DebugButton(this, "????????????", new OnClickListener() {
			@Override
			public void onClick(View v) {
				startRecordAudioMono(MediaRecorder.AudioSource.VOICE_CALL);
			}
		}), new DebugButton(this, "????????????", new OnClickListener() {
			@Override
			public void onClick(View v) {
				startRecordAudioMono(MediaRecorder.AudioSource.VOICE_RECOGNITION);
			}
		}));

		addDemoButtons(new DebugButton(this, "???????????????", new OnClickListener() {
			@Override
			public void onClick(View v) {
				startRecordAudioResample(MediaRecorder.AudioSource.DEFAULT);
			}
		}), new DebugButton(this, "???????????????", new OnClickListener() {
			@Override
			public void onClick(View v) {
				startRecordAudioStereo(MediaRecorder.AudioSource.DEFAULT);
			}
		}), new DebugButton(this, "???????????????", new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					FileInputStream in = new FileInputStream(
							"/sdcard/44100.snd");
					TXZAudioResampler.Resampler resampler = new TXZAudioResampler.Resampler(
							44100, 16000);
					mBufferCount = 0;
					while (true) {
						int n = 0;
						n = in.read(mBuffer, mBufferCount, 1600);
						if (n < 0)
							break;
						if (n > 0) {
							Log.d(TAG, "record data: " + n + "/" + mBufferCount);
							int r = resampler.resample(mBuffer, mBufferCount,
									n, mBuffer, mBufferCount);
							Log.d(TAG, "record data resample: " + r);
							if (r > 0) {
								mBufferCount += r;
							}
						}
					}
					resampler.release();
					in.close();
				} catch (Exception e) {
				}
			}
		}),new DebugButton(this, "????????????", new OnClickListener() {
			@Override
			public void onClick(View v) {
				importData();
			}
		}));

		addDemoButtons(new DebugButton(this, "????????????", new OnClickListener() {
			@Override
			public void onClick(View v) {
				playAssetsWav("300hz.wav");
			}
		}), new DebugButton(this, "????????????", new OnClickListener() {
			@Override
			public void onClick(View v) {
				playAssetsWav("600hz.wav");
			}
		}), new DebugButton(this, "????????????", new OnClickListener() {
			@Override
			public void onClick(View v) {
				playAssetsWav("1000hz.wav");
			}
		}), new DebugButton(this, "??????TTS(MP)", new OnClickListener() {
			@Override
			public void onClick(View v) {
				playAssetsWav("tts.wav");
			}
		}),new DebugButton(this, "??????TTS(AT)", new OnClickListener() {
			@Override
			public void onClick(View v) {
				int bufsize = AudioTrack.getMinBufferSize(22050,
						AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);

				InputStream is = getResources().openRawResource(R.raw.select);
				DataInputStream dis = new DataInputStream(new BufferedInputStream(is));
				byte[] buffer = new byte[1024];

				AudioTrack trackplayer = new AudioTrack(AudioManager.STREAM_MUSIC,
						22050, AudioFormat.CHANNEL_OUT_MONO,
						AudioFormat.ENCODING_PCM_16BIT, bufsize, AudioTrack.MODE_STREAM);

				trackplayer.play();

				try {

					while (dis.available() > 0) {
						dis.read(buffer, 0, buffer.length);
						trackplayer.write(buffer, 0, buffer.length);
					}

					if (trackplayer != null) {
						if (trackplayer.getState() == AudioTrack.STATE_INITIALIZED) {
							trackplayer.stop();
						}
						trackplayer.release();
						trackplayer = null;
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}),new DebugButton(this, "????????????", new OnClickListener() {
			@Override
			public void onClick(View view) {
				playAssetsWav("saoping0-20khz.wav");
			}
		}), new DebugButton(this, "????????????", new OnClickListener() {
			@Override
			public void onClick(View v) {
				mMediaPlayer.reset();
			}
		}));

		addDemoButtons(new DebugButton(this, "????????????", new OnClickListener() {
			@Override
			public void onClick(View v) {
				DebugUtil.showTips("????????????");
				clearAudio(false);
				mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 16000,
						AudioFormat.CHANNEL_OUT_MONO, mFormat,
						10 * 1024 * 1024, AudioTrack.MODE_STATIC);
				beforePlay();
				new Thread(new Runnable() {
					@Override
					public void run() {
						savePCM(mBuffer, mBufferCount, "momo.pcm");
						mAudioTrack.write(mBuffer, 0, mBufferCount);
						mAudioTrack.play();
					}
				}).start();
			}
		}), new DebugButton(this, "???????????????", new OnClickListener() {
			@Override
			public void onClick(View v) {
				DebugUtil.showTips("????????????");
				clearAudio(false);
				mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 16000,
						AudioFormat.CHANNEL_OUT_STEREO, mFormat,
						10 * 1024 * 1024, AudioTrack.MODE_STATIC);
				beforePlay();
				new Thread(new Runnable() {
					@Override
					public void run() {
						savePCM(mBuffer, mBufferCount, "stereo.pcm");
						mAudioTrack.write(mBuffer, 0, mBufferCount);
						mAudioTrack.play();
					}
				}).start();
			}
		}), new DebugButton(this, "???????????????", new OnClickListener() {
			@Override
			public void onClick(View v) {
				DebugUtil.showTips("????????????");
				clearAudio(false);
				mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 16000,
						AudioFormat.CHANNEL_OUT_MONO, mFormat,
						10 * 1024 * 1024, AudioTrack.MODE_STATIC);
				beforePlay();
				new Thread(new Runnable() {
					@Override
					public void run() {
						byte[] bs = new byte[mBufferCount / 2];
						for (int i = 0; i < mBufferCount - 1
								&& (i / 2 + 1 < mBufferCount / 2); i += 4) {
							bs[i / 2] = mBuffer[i];
							bs[i / 2 + 1] = mBuffer[i + 1];
						}
						savePCM(bs, bs.length, "left.pcm");
						mAudioTrack.write(bs, 0, bs.length);
						mAudioTrack.play();
					}
				}).start();
			}
		}), new DebugButton(this, "???????????????", new OnClickListener() {
			@Override
			public void onClick(View v) {
				DebugUtil.showTips("????????????");
				clearAudio(false);
				mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 16000,
						AudioFormat.CHANNEL_OUT_MONO, mFormat,
						10 * 1024 * 1024, AudioTrack.MODE_STATIC);
				beforePlay();
				new Thread(new Runnable() {
					@Override
					public void run() {
						byte[] bs = new byte[mBufferCount / 2];
						for (int i = 2; i < mBufferCount - 1
								&& (i / 2 + 1 < mBufferCount / 2); i += 4) {
							bs[i / 2 - 1] = mBuffer[i];
							bs[i / 2] = mBuffer[i + 1];
						}
						savePCM(bs, bs.length, "right.pcm");
						mAudioTrack.write(bs, 0, bs.length);
						mAudioTrack.play();
					}
				}).start();
			}
		}), new DebugButton(this, "????????????", new OnClickListener() {
			@Override
			public void onClick(View v) {
				DebugUtil.showTips("????????????");
				clearAudio(false);
				mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 16000,
						AudioFormat.CHANNEL_OUT_MONO, mFormat,
						10 * 1024 * 1024, AudioTrack.MODE_STATIC);
				beforePlay();
				new Thread(new Runnable() {
					@Override
					public void run() {
						byte[] l = new byte[mBufferCount / 2];
						for (int i = 0; i < mBufferCount - 1
								&& (i / 2 + 1 < mBufferCount / 2); i += 4) {
							l[i / 2] = mBuffer[i];
							l[i / 2 + 1] = mBuffer[i + 1];
						}
						byte[] r = new byte[mBufferCount / 2];
						for (int i = 2; i < mBufferCount - 1
								&& (i / 2 + 1 < mBufferCount / 2); i += 4) {
							r[i / 2 - 1] = mBuffer[i];
							r[i / 2] = mBuffer[i + 1];
						}
						byte[] aec = new byte[mBufferCount / 2];
						final int FRAME_COUNT = 320;
						final int FILTER_LENGTH = 5 * 16000 / 1000;
						TXZAudioEchoCancel.EchoCancel echoCancel = new TXZAudioEchoCancel.EchoCancel(
								FRAME_COUNT, FILTER_LENGTH);
						int offset = 0;
						while (true) {
							int n = FRAME_COUNT*2;
							if (n >= l.length - offset) {
								n = l.length - offset;
							}
							echoCancel.process(l, offset, n, r, offset, aec,
									offset);
							offset += n;
							if (n >= l.length - offset) {
								break;
							}
						}
						echoCancel.release();
						savePCM(aec, aec.length, "aec.pcm");
						mAudioTrack.write(aec, 0, aec.length);
						mAudioTrack.play();
					}
				}).start();
			}
		}), new DebugButton(this, "?????????????????????", new OnClickListener() {
			@Override
			public void onClick(View v) {
				DebugUtil.showTips("????????????");
				clearAudio(false);
				mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 16000,
						AudioFormat.CHANNEL_OUT_MONO, mFormat,
						10 * 1024 * 1024, AudioTrack.MODE_STATIC);
				beforePlay();
				new Thread(new Runnable() {
					@Override
					public void run() {
						byte[] l = new byte[mBufferCount / 2];
						for (int i = 0; i < mBufferCount - 1
								&& (i / 2 + 1 < mBufferCount / 2); i += 4) {
							l[i / 2] = mBuffer[i];
							l[i / 2 + 1] = mBuffer[i + 1];
						}
						byte[] r = new byte[mBufferCount / 2];
						for (int i = 2; i < mBufferCount - 1
								&& (i / 2 + 1 < mBufferCount / 2); i += 4) {
							r[i / 2 - 1] = mBuffer[i];
							r[i / 2] = mBuffer[i + 1];
						}
						byte[] aec = new byte[mBufferCount / 2];
						final int FRAME_COUNT = 320;
						final int FILTER_LENGTH = 5 * 16000 / 1000;
						TXZAudioEchoCancel.EchoCancel echoCancel = new TXZAudioEchoCancel.EchoCancel(
								FRAME_COUNT, FILTER_LENGTH);
						int offset = 0;
						while (true) {
							int n = FRAME_COUNT*2;
							if (n >= r.length - offset) {
								n = r.length - offset;
							}
							echoCancel.process(r, offset, n, l, offset, aec,
									offset);
							offset += n;
							if (n >= r.length - offset) {
								break;
							}
						}
						echoCancel.release();
						savePCM(aec, aec.length, "aec.pcm");
						mAudioTrack.write(aec, 0, aec.length);
						mAudioTrack.play();
					}
				}).start();
			}
		}));
		
		addDemoButtons(new DebugButton(this, "??????????????????????????????", new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				DebugUtil.showTips("??????????????????????????????");
				handleAesDte(1);
			}
		}),new DebugButton(this, "??????????????????????????????", new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                DebugUtil.showTips("??????????????????????????????");
                handleAesDte(2);
            }
        }),new DebugButton(this, "??????????????????????????????", new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				DebugUtil.showTips("??????????????????????????????");
				handleTargetAesDte(1);
			}
		}),new DebugButton(this, "??????????????????????????????", new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                DebugUtil.showTips("??????????????????????????????");
                handleTargetAesDte(2);
            }
        }));

		addDemoButtons(new DebugButton(this, "????????????", new OnClickListener() {
			@Override
			public void onClick(View v) {
				clearAudio(false);
				DebugUtil.showTips("?????????");
			}
		}), new DebugButton(this, "????????????", new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					String name = "rec_" + System.currentTimeMillis() + ".pcm";
					FileOutputStream out = new FileOutputStream(new File(
							Environment.getExternalStorageDirectory(),
							"txz/voice/" + name));
					out.write(mBuffer, 0, mBufferCount);
					out.close();
					DebugUtil.showTips("?????????????????????" + name);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}), new DebugButton(this, "????????????", new OnClickListener() {
			@Override
			public void onClick(final View v) {
				RecorderUtil.start(new RecordCallback() {
					@Override
					public void onVolume(int arg0) {
					}

					@Override
					public void onSpeechTimeout() {
					}

					@Override
					public void onPCMBuffer(short[] arg0, int arg1) {
					}

					@Override
					public void onMuteTimeout() {
					}

					@Override
					public void onMute(final int vol) {
						Log.d("MuteTest", "Mute time: " + vol);
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								((Button) v).setText("????????????" + vol);
							}
						});
					}

					@Override
					public void onMP3Buffer(byte[] arg0) {
					}

					@Override
					public void onError(int arg0) {
					}

					@Override
					public void onCancel() {
					}

					@Override
					public void onBegin() {
					}

					@Override
					public void onEnd(int speechLength) {
					}

					@Override
					public void onParseResult(int voiceLength,
							String voiceText, String voiceUrl) {
					}
				}, new RecordOption().setEncodeMp3(false).setMaxMute(5000)
						.setMaxSpeech(1000000).setSkipMute(false));
			}
		}));
	}

	private String path;

	/**
	 * ??????????????????
	 */
	private void importData() {
		chooseFile();
	}



	/**
	 * ????????????
	 */
	private void chooseFile() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("*/*");
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		try {
			startActivityForResult(Intent.createChooser(intent, "????????????"), FILE_SELECT_CODE);
		} catch (android.content.ActivityNotFoundException ex) {
			Toast.makeText(this, "??????????????????????????????-_-!!", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * ??????pcm????????????
	 * @param path
	 */
	private void readPCMData(String path) throws IOException {
		FileInputStream fileInputStream = new FileInputStream(path);
		mBufferCount = fileInputStream.read(mBuffer);
		fileInputStream.close();
		if(mBufferCount<=0){
			throw new IOException("file length is zero");
		}
	}

	protected void handleTargetAesDte(int filter) {
		File dir = new File(Environment.getExternalStorageDirectory()
				.getPath(), "txz/aectest");
		if(!dir.exists()){
			DebugUtil.showTips("aectest dir is not exist");
			return;
		}
		File file = new File(dir, "test.pcm");
		if(!file.exists()){
			DebugUtil.showTips(".../test.cpm is not exist");
			return;
		}
		CommonDialog dia = new CommonDialog();
		dia.setTitle("????????????"+file.getName());
		dia.setCancelable(false);
		dia.show();
		int delay = 0;
		AesDte aesDte = new AesDte(16000, 1);
		aesDte.setOptionInt(3, filter);
		byte[] buf = new byte[320];
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			while(fis.read(buf) > 0){
				delay = aesDte.process(buf, null);
				if(delay != 0){
					break;
				}
			}
		} catch (FileNotFoundException e) {
			LogUtil.loge(file.getName() + " FileNotFoundException");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			LogUtil.loge(file.getName() + " IOException");
		} finally {
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		LogUtil.logd("dtetest: ddelay = "+delay);
		DebugUtil.showTips("???????????????"+delay/16+"ms");
		dia.cancel();
	}

	/**
	 * ?????????????????????????????????
	 */
	protected void handleAesDte(int filter) {
		if(TextUtils.isEmpty(currentName)){
			DebugUtil.showTips("?????????????????????????????????");
			return;
		}
		CommonDialog dia = new CommonDialog();
		dia.setTitle("????????????"+currentName);
		dia.setCancelable(false);
		dia.show();
		int delay = 0;
		AesDte aesDte = new AesDte(16000, 1);
		aesDte.setOptionInt(3, filter);
		File file = new File(currentName);
		byte[] buf = new byte[640];
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			while(fis.read(buf) > 0){
				delay = aesDte.process(buf, null);
				if(delay != 0){
					break;
				}
			}
		} catch (FileNotFoundException e) {
			LogUtil.loge(currentName + " FileNotFoundException");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			LogUtil.loge(currentName + " IOException");
		} finally {
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		LogUtil.logd("dtetest: ddelay = "+delay);
		DebugUtil.showTips("???????????????"+delay/16+"ms");
		dia.cancel();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TXZConfigManager.getInstance().setEnableRecording(false);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mMediaPlayer.reset();
		mMediaPlayer.release();
		clearAudio(true);
		TXZConfigManager.getInstance().setEnableRecording(true);
	}

	private void savePCM(byte[] data, int count, String name) {
		if (count <= 0)
			return;
		try {
			File d = new File(Environment.getExternalStorageDirectory()
					.getPath(), "txz/recordTest");
			d.mkdirs();
			String fileName = "" + mRecordTime + "_" + name;
			File fout = new File(d, fileName);
			currentName = fout.getPath();
			LogUtil.logd("currentName: "+currentName);
			OutputStream out = new FileOutputStream(fout);
			out.write(data, 0, count);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
