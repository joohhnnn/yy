package com.txznet.txz.component.selector;

import java.util.ArrayList;
import java.util.List;

import com.txznet.loader.AppLogic;
import com.txznet.txz.component.audio.AudioSelector.AudioSelectorListener;
import com.txznet.txz.component.audio.AudioSelector.Music;
import com.txznet.txz.component.selector.ISelectControl.OnItemSelectListener;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.call.CallSelectControl;
import com.txznet.txz.module.music.bean.AudioShowData;
import com.txznet.txz.ui.win.nav.SearchEditDialog;

/**
 * 
 * 选择器操作
 *
 */
public class Selector {

	public static void entryAudioSelector(final List<Music> musics, String keyWord,
			final AudioSelectorListener listener) {
		if (musics == null) {
			JNIHelper.logd("entryAudioSelector musics is null");
			return;
		}

		List<AudioShowData> asds = new ArrayList<AudioShowData>();
		for (Music m : musics) {
			AudioShowData asd = new AudioShowData();
			asd.setId(0);
			asd.setName(m.authorName);
			asd.setTitle(m.audioName);
			asd.setAlbumId(m.audioId);
			asd.setAlbumName(m.sourceName);
			asd.setAlbumIntro(m.albumIntro);
			asd.setAlbumTrackCount(m.includeTrackCount);
			asds.add(asd);
		}

		SelectorHelper.entryMusicSelector(asds, new OnItemSelectListener() {

			@Override
			public void onItemSelect(List srcList, int index, Object obj) {
				if (listener != null) {
					listener.onAudioSelected(musics.get(index), index);
				}
			}
		});
	}

	public static void clearSelectorWakeup() {
		SelectorHelper.clearIsSelecting();
	}

	public static void onUiEventCancel() {
		if (CallSelectControl.isSelecting()) {
			CallSelectControl.selectCancel(false);
		}

		SelectorHelper.selectCancel();
	}

	public static void onUiEventOK() {
		if (CallSelectControl.isSelecting()) {
			CallSelectControl.selectSure(false);
		}

		SelectorHelper.selectSure();
	}

	public static void onUiEventBack() {
		/*
		 * 关闭窗口停止录音和识别，电话
		 */
		if (CallSelectControl.isSelecting()) {
			CallSelectControl.clearIsSelecting();
		}

		SelectorHelper.clearIsSelecting();
	}

	public static void onPauseStopTtsAndAsr() {
		/*
		 * 关闭窗口停止录音和识别，电话
		 */
		if (CallSelectControl.isSelecting()) {
			CallSelectControl.clearProgress();
			CallSelectControl.stopTtsAndAsr();
		}

		// 暂不处理列表的暂停，点击后重新计数
		SelectorHelper.onResumeDelayTask();
	}

	public static void onDialogDismiss() {
		/*
		 * 关闭窗口停止录音
		 */
		if (CallSelectControl.isSelecting()) {
			CallSelectControl.clearIsSelecting();
		}

		closeAllWin();
		clearSelectorWakeup();
	}

	public static void closeAllWin() {
		AppLogic.removeUiGroundCallback(mDismissRunnable);
		AppLogic.runOnUiGround(mDismissRunnable, 20);
	}

	static Runnable mDismissRunnable = new Runnable() {

		@Override
		public void run() {
			// WinMapDialog.getInstance().dismiss();
			SearchEditDialog.getInstance().setNeedCloseDialog(true);
			SearchEditDialog.getInstance().dismiss();
		}
	};

	public static byte[] procInvoke(String packageName, String command, byte[] data) {
		return SelectorHelper.procInvoke(packageName, command, data);
	}
}