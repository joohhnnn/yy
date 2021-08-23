package com.txznet.txz.module.ui;

import java.util.ArrayList;
import java.util.List;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.plugin.WinPlugin;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.sdk.TXZRecordWinManager.RecordWin2.RecordWinController;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.choice.ChoiceManager;
import com.txznet.txz.module.ui.WinManager.RecordInvokeAdapter;
import com.txznet.txz.module.ui.parse.CinemaQueryParse;
import com.txznet.txz.module.ui.parse.DefaultParse;
import com.txznet.txz.module.ui.parse.TTSNoResultParse;
import com.txznet.txz.plugin.PluginManager;
import com.txznet.txz.plugin.PluginManager.CommandProcessor;
import com.txznet.txz.plugin.interfaces.AbsTextJsonParse;

import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager.LayoutParams;

public class ViewPluginUtil {
	public static final String PLUGIN_PREFIX = "txz.plugin.win.";

	private WinManager mWinManager;
	private List<AbsTextJsonParse> mParses = new ArrayList<AbsTextJsonParse>();

	public ViewPluginUtil(WinManager wm) {
		mWinManager = wm;

		installParse(new DefaultParse());
		installParse(new TTSNoResultParse());
		installParse(new CinemaQueryParse());
	}

	private CommandProcessor mCommandProcessor = new CommandProcessor() {

		@Override
		public Object invoke(String command, Object[] args) {
			invokePluginCommand(command, args);
			return null;
		}
	};

	public void invokePluginCommand(String command, Object... args) {
		JNIHelper.logd("recv command:" + command);
		try {
			if (command.startsWith("win2")) {
				invokeWin2PluginCommand(command.substring("win2.".length()), args);
				return;
			}
			if (command.startsWith("winPlugin")) {
				invokeWinPlguinCommand(command, args);
			}
			if (command.equals("setParse")) {
				AbsTextJsonParse jsonParse = (AbsTextJsonParse) args[0];
				installParse(jsonParse);
			} else if (command.equals("addPlugin")) {
				/**
				 * args[0]:String // 插件View的ID args[1]:View // 显示的View
				 * args[2]:boolean // 是否替换上次相同的类型的View args[3]:boolean //
				 * 是否不是嵌入到聊天界面
				 */
				String typeId = (String) args[0];
				View view = (View) args[1];
				boolean replace = (Boolean) args[2];
				boolean isInDep = (Boolean) args[3];
				WinManager.getInstance().addPluginView(typeId, view, replace, isInDep);
			} else if (command.equals("forceLocalAdapter")) {
				mWinManager.mForceLocalAdapter = (Boolean) args[0];
			} else if (command.equals("showJson")) {
				showText((String) args[0]);
			} else if (command.equals("setAdapter")) {
				mWinManager.mPluginInvokeAdapter = (RecordInvokeAdapter) args[0];
			} else {
				invokeRecordAdapter(command, args);
			}
		} catch (Exception e) {
			JNIHelper.loge(e.toString());
		}
		return;
	}

	
	/**
	 * 需要在win2上显示的相关View 
	 */
	private void invokeWin2PluginCommand(String command, Object... args) {
		JNIHelper.logd("recv win2 cmd:" + command);
		try {
			if (command.equals("addFullContentView")) {
				RecordWin2Manager.getInstance().addView(RecordWinController.TARGET_CONTENT_FULL, (View) args[0]);
			} else if (command.equals("addMsgView")) {
				RecordWin2Manager.getInstance().addView(RecordWinController.TARGET_CONTENT_CHAT, (View) args[0]);
			} else if (command.equals("show")) {
				RecordWin2Manager.getInstance().show();
			} else if (command.equals("dismiss")) {
				RecordWin2Manager.getInstance().dismiss();
			} else if (command.equals("showData")) {
				RecordWin2Manager.getInstance().showData((String) args[0]);
			}
		} catch (Exception e) {
			LogUtil.loge("error invoke win2 plugin cmd!");
		}
	}

	private void invokeWinPlguinCommand(String command, Object... objects) {
		JNIHelper.logd("recv WinPlugin cmd:" + command);
		try {
			if ("showView".equals(command)) {
				// 更新winPlugin上面显示的view并展示出来
				// arg1:type arg2:view arg3:layoutParmas
				WinPlugin.showPluginView((View) objects[1], (LayoutParams) objects[2], (Integer) objects[0], objects);
			} else if ("show".equals(command)) {
				WinPlugin.showWin((Integer) objects[0]);
			} else if ("dismiss".equals(command)) {
				WinPlugin.dismissWin((Integer) objects[0]);
			} else if ("updateViewData".equals(command)) {
				WinPlugin.updateViewData((Integer) objects[0], objects);
			}
		} catch (Exception e) {
			LogUtil.loge("error invoke win plugin cmd!");
		}
	}
	
	private void invokeRecordAdapter(String command, Object[] args) {
		if ("show".equals(command)) {
			mWinManager.getAdapter().show();
		} else if ("dismiss".equals(command)) {
			mWinManager.getAdapter().dismiss();
		} else if ("refreshState".equals(command)) {
			mWinManager.getAdapter().refreshState("record", (Integer) args[0]);
		} else if ("refreshVolume".equals(command)) {
			mWinManager.getAdapter().refreshVolume((Integer) args[0]);
		} else if ("refreshProgress".equals(command)) {
			mWinManager.getAdapter().refreshProgress((Integer) args[0], (Integer) args[1]);
		} else if ("refreshItemSelect".equals(command)) {
			mWinManager.getAdapter().refreshItemSelect((Integer) args[0]);
		} else if ("addMsg".equals(command)) {
			mWinManager.getAdapter().addMsg((Integer) args[0], (String) args[1]);
		} else if ("addListMsg".equals(command)) {
			mWinManager.getAdapter().addListMsg((String) args[0]);
		} else if ("showStock".equals(command)) {
			mWinManager.getAdapter().showStock((byte[]) args[0]);
		} else if ("showWeather".equals(command)) {
			mWinManager.getAdapter().showWeather((byte[]) args[0]);
		} else if ("snapPager".equals(command)) {
			mWinManager.getAdapter().snapPager((Boolean) args[0]);
		}
	}

	public void addPluginProcessor() {
		PluginManager.addCommandProcessor(PLUGIN_PREFIX, mCommandProcessor);
	}

	// 添加一个语义解析器
	public void installParse(AbsTextJsonParse parse) {
		boolean hasAdd = false;
		for (AbsTextJsonParse tjp : mParses) {
			if (tjp.getName().equals(parse.getName())) {
				JNIHelper.loge("Replace Parse");
				mParses.remove(tjp);
				hasAdd = true;
				mParses.add(parse);
				break;
			}
		}

		if (!hasAdd) {
			mParses.add(parse);
			JNIHelper.loge("add Parse");
		}
	}

	public void showText(String textJson) {
		if (TextUtils.isEmpty(textJson)) {
			JNIHelper.logw("showText textJson is empty！");
			return;
		}

		JNIHelper.logd("ViewPluginUtil showText:" + textJson);
		ChoiceManager.getInstance().clearIsSelecting();
		for (int i = mParses.size() - 1; i >= 0; i--) {
			AbsTextJsonParse parse = mParses.get(i);
			if (parse != null && parse.acceptText(WinManager.getInstance().hasThirdImpl(), textJson)) {
				parse.parseStrData(textJson);
				return;
			}
		}

		JNIHelper.logd("no parse process textJson:" + textJson);
	}
	
	/**
	 * 上下页
	 * @param isNext
	 */
	public void snapPage(boolean isNext) {
		for (AbsTextJsonParse parse : mParses) {
			if (parse instanceof CinemaQueryParse) {
				((CinemaQueryParse) parse).snapPage(isNext);
				break;
			}
		}
	}
}