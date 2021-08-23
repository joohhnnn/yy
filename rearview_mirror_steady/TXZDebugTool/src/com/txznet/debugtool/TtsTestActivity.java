package com.txznet.debugtool;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.text.TextUtils;
import android.view.View;

import com.txznet.debugtool.util.TtsUtils;
import com.txznet.sdk.TXZTtsManager;
import com.txznet.widget.DebugButton;


public class TtsTestActivity extends BaseDebugActivity {
	ArrayList<String> speakTexts = new ArrayList<String>();
	@Override
	protected void onInitButtons() {
		initTtsText();
		TtsUtils.getInstance().setTtsTexts(speakTexts);
		addDemoButtons(new DebugButton(this, "测试播放TTS", new View.OnClickListener() {	
			@Override
			public void onClick(View v) {
				TtsUtils.getInstance().startPlayTts();
			}
		}),new DebugButton(this, "停止播报", new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				TtsUtils.getInstance().stopPlayTts();
			}
		}));
	}
	
	private void initTtsText(){
		speakTexts.clear();
		File file = new File("/sdcard/txz/tts_test.txt");
		try {
			if (file.exists()) {
				readTexts(new FileInputStream(file));
			}else {
				readTexts(getAssets().open("tts_test.txt"));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void readTexts(InputStream inputStream) {
		try {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			String text = null;
			while ((text = bufferedReader.readLine()) != null) {
				if (!TextUtils.isEmpty(text)) {
					speakTexts.add(text);	
				}	
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
//	
//	@Override
//	protected void onPause() {
//		super.onPause();
//		isCancel = true;
//		TXZTtsManager.getInstance().cancelSpeak(mTaskId);
//	}
//	
	
}
