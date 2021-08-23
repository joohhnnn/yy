package com.txznet.feedback.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.res.AssetManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.feedback.AppLogic;
import com.txznet.feedback.R;
import com.txznet.feedback.data.QBean;
import com.txznet.feedback.dtool.JsonParser;
import com.txznet.feedback.dtool.JsonParser.ParserListener;
import com.txznet.feedback.util.FileUtil;
import com.txznet.feedback.util.IOUtil;
import com.txznet.txz.util.runnables.Runnable1;

public class QuestionService {
	private static final String FILE_DIR = FileUtil.getSdCardDir()
			+ "/txz/feedback/question.txt";
	private static final String FILE_NAME = "question.txt";

	private List<String> mTitleList = new ArrayList<String>();
	private Map<Integer, View> posToViewMap = new HashMap<Integer, View>();

	private List<QBean> mQBeanList = new ArrayList<QBean>();
	private ParserListener mParserListener;

	private static QuestionService instance = new QuestionService();

	private QuestionService() {
	}

	public void init() {
		mTitleList.clear();
		posToViewMap.clear();

		if (!new File(FILE_DIR).exists()) {
			AssetManager am = AppLogic.getApp().getAssets();
			InputStream is = null;
			if (am != null) {
				try {
					is = am.open(FILE_NAME);
				} catch (IOException e) {
					LogUtil.loge(e.toString());
				}
			}

			FileUtil.copyFile(is, FILE_DIR);
		}

		File file = new File(FILE_DIR);
		if (!file.exists()) {
			invokeNoFile();
			return;
		}

		mParserListener = new ParserListener() {

			@Override
			public void onParserEnd(List<QBean> result) {
				mQBeanList = result;
				for (QBean bean : mQBeanList) {
					mTitleList.add(bean.description);
				}

				invokeAnalysisListener();
			}
		};

		AppLogic.runOnBackGround(new Runnable1<File>(file) {

			@Override
			public void run() {
				String result = IOUtil.parseFile(mP1);
				JsonParser.getInstance().setParserListener(mParserListener);
				JsonParser.getInstance().parseJson(result);
			}
		}, 0);
	}

	public static QuestionService getInstance() {
		return instance;
	}

	/**
	 * 获取问题的列表展示数据
	 * 
	 * @return
	 */
	public List<String> getTitleList() {
		return mTitleList;
	}

	public View getViewByPos(int position) {
		View view = posToViewMap.get(position);
		// Question question = mQuestions.get(position);
		QBean bean = mQBeanList.get(position);
		// if(question == null){
		// return view;
		// }
		if (bean == null) {
			return view;
		}

		if (view == null && bean.type == QBean.TYPE_MULTI) {
			view = LayoutInflater.from(AppLogic.getApp()).inflate(
					R.layout.tts_help_layout, null);
			posToViewMap.put(position, view);
		}

		if (view == null) {
			view = LayoutInflater.from(AppLogic.getApp()).inflate(
					R.layout.help_tv, null);
			posToViewMap.put(position, view);
		} else {
			return view;
		}

		switch (bean.type) {
		case QBean.TYPE_MULTI:
			TextView tv = (TextView) view.findViewById(R.id.wakeup_txt);
			List<QBean> beanList = bean.mQBeans;
			for (QBean b : beanList) {
				if (b.description.contains("语音唤醒")) {
					tv.setText(b.answer);
					break;
				}
			}
			break;

		case QBean.TYPE_ONLY:
			((TextView) view).setText(bean.answer);
			break;
		}

		// for(Question q:qList){
		// ((TextView)view).setText(q.getDescription() + "\n");
		// }

		return view;
	}

	private OnAnalysisListener mListener;

	public void setOnAnalysisListener(OnAnalysisListener listener) {
		mListener = listener;
	}

	private void invokeAnalysisListener() {
		if (mListener != null) {
			mListener.onSuccess();
		}
	}

	private void invokeNoFile() {
		if (mListener != null) {
			mListener.noFile();
		}
	}

	public interface OnAnalysisListener {
		public void onSuccess();

		public void noFile();
	}
}