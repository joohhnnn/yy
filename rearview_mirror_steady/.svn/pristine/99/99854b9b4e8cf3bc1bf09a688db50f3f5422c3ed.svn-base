package com.txznet.nav.manager;

import java.io.File;
import java.util.List;

import android.os.Environment;
import android.text.TextUtils;

import com.amap.api.maps.MapsInitializer;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.remote.util.TtsUtil.ITtsCallback;
import com.txznet.comm.sp.CommonSp;
import com.txznet.comm.ui.dialog.WinConfirmAsr;
import com.txznet.loader.AppLogic;
import com.txznet.nav.offline.DownloadMapManager;
import com.txznet.txz.util.StorageUtil;

/**
 * 配置城市数据读取路径: 1、读取所有的sd卡路径 2、倒置判断sd是否存在地图目录
 * 
 * @author ASUS User
 *
 */
public class DataSourceManager {

	private static final String AMAP_SUFFIX = "/data/vmap";

	private static final String NET_SUFFIX = "/amap" + AMAP_SUFFIX;

	private static final String SOURCE_FOLDER = "/txzNav";

	private static final String SOURCE_PATH = "/txzNav" + AMAP_SUFFIX;

	private static final String NET_SOURCE_PATH = Environment
			.getExternalStorageDirectory() + "/amap" + AMAP_SUFFIX;

	public static String mCurrentSourcePath;

	private boolean mProcSelectHasResult;

	private static DataSourceManager mManager = new DataSourceManager();

	public static DataSourceManager getInstance() {
		return mManager;
	}

	private DataSourceManager() {
	}

	public void resetSourcePath(boolean needNotice) {
		// 扫描SD卡
//		AppLogic.removeUiGroundCallback(mCheckSDPathRunnable);
//		AppLogic.runOnUiGround(mCheckSDPathRunnable, 200);
	}

	Runnable mCheckSDPathRunnable = new Runnable() {

		@Override
		public void run() {
			procSelectSDCard();

			if (mProcSelectHasResult) {
				String sdDir = MapsInitializer.sdcardDir;
				LogUtil.logd("DataSourceManager checkSDPathRunnable MapsInitializer.sdcardDir:"
						+ sdDir);
				if (TextUtils.isEmpty(mCurrentSourcePath)) {
					if (!TextUtils.isEmpty(sdDir)) {
						// 不相等
						prepareForSelectSDCardPath();
					}
				} else {
					// 不包含才处理
					if (!sdDir.equals(mCurrentSourcePath)) {
						if (!sdDir.contains(mCurrentSourcePath)) {
							prepareForSelectSDCardPath();
						}
					}
				}

			}
		}
	};

	public void procSelectSDCard() {
		try {
			// 恢复sdcardDir的路径
			String dir = SavePrefs.getInstance().getDir();
			MapsInitializer.sdcardDir = dir;
		} catch (Exception e) {
			e.printStackTrace();
		}

		mProcSelectHasResult = false;
		mCurrentSourcePath = "";
		// 获取内置SD卡路径
		String mInnerSdPath = StorageUtil.getInnerSDCardPath();
		// 获取所有外置SD卡路径
		List<String> mAllExters = StorageUtil.getAllExterSdcardPath();
		// 如果外置SD卡全部为空
		if (mAllExters == null || mAllExters.size() < 1) {
			if (TextUtils.isEmpty(mInnerSdPath)) {
				// 处理没有可用的SD卡，一般不会走到这一步
				return;
			}

			// 如果内置SD卡存在可用的路径，设置该路径
			if (isExistFilePath(new File(mInnerSdPath), NET_SUFFIX)) {
				// mCurrentSourcePath = mInnerSdPath; 默认路径
				mCurrentSourcePath = "";
				mProcSelectHasResult = true;
			}

			return;
		}

		// 遍历所有SD卡路径，从最后插入的SD卡路径开始
		for (int i = mAllExters.size() - 1; i >= 0; i--) {
			String extPath = mAllExters.get(i);
			File root = new File(extPath);
			if (root.exists()) {
				// 处理存在的情况下
				if (isExistFilePath(root, SOURCE_PATH)) {
					// 找到SD卡数据源
					mCurrentSourcePath = extPath;
					mProcSelectHasResult = true;
					return;
				}
			}
		}

		if (procExternSDNoExistDatas()) {
			return;
		}

		if (!TextUtils.isEmpty(mInnerSdPath)) {
			if (isExistFilePath(new File(mInnerSdPath), NET_SUFFIX)) {
				// mCurrentSourcePath = mInnerSdPath; 默认路径
				mCurrentSourcePath = "";
				mProcSelectHasResult = true;
			}
		}

		if (isExistFilePath(new File(NET_SOURCE_PATH), "")) {
			mCurrentSourcePath = "";
			mProcSelectHasResult = true;
		}

		LogUtil.logd("SD Path:" + NET_SOURCE_PATH);

		return;
	}

	// SD卡不存在数据，提示用户是否选择下载数据到此卡上
	private boolean procExternSDNoExistDatas() {
		return false;
	}

	private void prepareForSelectSDCardPath() {
		final boolean isNaviNow = NavManager.getInstance().isNavi();
		String hint = "检测到SD卡发生变化，是否切换导航数据源路径？";
		new WinConfirmAsr() {
			@Override
			public void onClickOk() {
				// 设置数据源路径
				if (isNaviNow) {
					procNaviSence();
					return;
				}

				selectSourcePath();
			}

			@Override
			public void onSpeakOk() {
				this.dismiss();
				if (isNaviNow) {
					procNaviSence();
					return;
				}

				TtsUtil.speakText("好的，将为您切换数据源路径", new ITtsCallback() {
					public void onEnd() {
						onClickOk();
					};
				});
			};

			@Override
			public void onClickCancel() {
				super.onClickCancel();
			};

			@Override
			public void onSpeakCancel() {
				super.onSpeakCancel();
				onClickCancel();
			};
		}.setMessage(hint).setHintTts(hint)
				.setSureText("切换", new String[] { "确定", "是", "切换" })
				.setCancelText("取消", new String[] { "取消", "放弃", "返回" }).show();
	}

	private void procNaviSence() {
		String hint = "切换数据源路径将重启导航，确定切换？";
		new WinConfirmAsr() {
			@Override
			public void onClickOk() {
				// 设置数据源路径
				selectSourcePath();
			}

			@Override
			public void onSpeakOk() {
				this.dismiss();
				TtsUtil.speakText("好的，将为您切换数据源路径", new ITtsCallback() {
					public void onEnd() {
						onClickOk();
					};
				});
			};

			@Override
			public void onClickCancel() {
				super.onClickCancel();
			};

			@Override
			public void onSpeakCancel() {
				super.onSpeakCancel();
				onClickCancel();
			};
		}.setMessage(hint).setHintTts(hint)
				.setSureText("确定", new String[] { "确定", "是" })
				.setCancelText("取消", new String[] { "取消", "放弃", "返回" }).show();
	}

	public boolean selectSourcePath() {
		if (mProcSelectHasResult) {
			String sdcardDir = MapsInitializer.sdcardDir;
			if (!TextUtils.isEmpty(mCurrentSourcePath)
					&& !mCurrentSourcePath.contains(SOURCE_FOLDER)) {
				mCurrentSourcePath += SOURCE_FOLDER;
			}

			if (TextUtils.isEmpty(sdcardDir) && mCurrentSourcePath.equals("")) {
				mCurrentSourcePath = "";
				onMapsSDCardDirChange(mCurrentSourcePath);
			} else {
				if (!sdcardDir.equals(mCurrentSourcePath)) {
					onMapsSDCardDirChange(mCurrentSourcePath);
				}
			}
			return true;
		}

		return false;
	}

	/**
	 * 如果发生数据源路径发生改变，则应该处理变更操作
	 * 
	 * @param current
	 */
	public void onMapsSDCardDirChange(String current) {
		LogUtil.logd("DataSourceManager onMapsSDCardDirChange MapsInitializer.sdcardDir:"
				+ current);
		MapsInitializer.sdcardDir = current;
		// 重新初始化
		DownloadMapManager.getInstance().initialize();
		SavePrefs.getInstance().setDir(current);
		// 重启导航应用
		AppLogic.restartProcess();
	}

	private boolean isExistFilePath(File root, String path) {
		File file = new File(root.getAbsolutePath() + File.separator + path);
		if (!file.exists()) {
			return false;
		}
		return true;
	}

	private static class SavePrefs extends CommonSp {
		private static final String KEY = "sdcardDir";
		private static final String spName = "maps_initializer_path";

		private static SavePrefs mPrefs = new SavePrefs();

		public static SavePrefs getInstance() {
			return mPrefs;
		}

		protected SavePrefs() {
			super(AppLogic.getApp(), spName);
		}

		public void setDir(String sdDir) {
			setValue(KEY, sdDir);
		}

		public String getDir() {
			return getValue(KEY, "");
		}
	}
}