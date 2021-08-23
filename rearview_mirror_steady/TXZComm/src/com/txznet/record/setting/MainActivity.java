package com.txznet.record.setting;

import java.util.ArrayList;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.comm.base.BaseActivity;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.ConfigUtil;
import com.txznet.comm.remote.util.ConfigUtil.ConfigListener;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.dialog2.WinConfirm;
import com.txznet.comm.ui.view.CheckedImageView;
import com.txznet.loader.AppLogicBase;
import com.txznet.sdk.TXZConfigManager;
import com.txznet.sdk.TXZConfigManager.FloatToolType;
import com.txznet.sdk.TXZTtsManager;
import com.txznet.txz.comm.R;

public class MainActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		initView();
		initData();
		ConfigUtil.registerConfigListener(mConfigListener);
	}
	
	
	// 同行者语音识别配置文件地址
	public static String TXZ_COMMAND_CONFIG_FILE = Environment
			.getExternalStorageDirectory() + "/txz/commandConfig.properties";

	private CheckedImageView image_wakeSwitch,image_windowSwitch;
	private RelativeLayout layout_wake,layout_window,layout_wkwords,layout_recogdB,layout_ttsdB,layout_reset;
	private TextView imgbnt_backToRecord;
	private Button bntHint_command, bntHint_recogdB, bntHint_TtsdB,
			bntHint_reset;
	private TextView txt_recogValue, txt_ttsValue;

	private int recogValueId = 2;
	private int ttsValueId = 2;

	final String[] recogStr = new String[] { "极高（适合嘈杂环境，极易被唤醒）",
			"高（适合噪音环境，容易被唤醒）", "正常（适合普通环境，推荐）", "低（适合安静环境，较难被唤醒）",
			"极低（适合安静环境，极难被唤醒）" };
	final String[] recogStrPrefix = new String[] { "极高", "高", "正常", "低", "极低" };
	final String[] ttsStr = new String[] { "极快（适合急性子，极容易漏听内容）",
			"快（适合急性子，容易漏听内容）", "正常（推荐）", "慢（适合慢性子，容易不耐烦）", "极慢（适合慢性子，极容易不耐烦）" };
	final String[] ttsStrPrefix = new String[] { "极快", "快", "正常", "慢", "极慢" };
	
	private static int mHideOptions = 0;
	public static void hideOptions(int hideOptions){
		mHideOptions = hideOptions;
	}

	private OnClickListener backOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			finish();
		}
	};

	private OnClickListener addCommandOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent forwordToAddCommand = new Intent(MainActivity.this,
					ChangeCommandActivity.class);
			forwordToAddCommand.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			startActivity(forwordToAddCommand);
		}
	};

	private OnClickListener changeRecogValueClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			showRecogValueDialog();
		}
	};

	private OnClickListener changeTtsValueClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			showTtsValueDialog();
		}
	};

	private OnClickListener resetConfigClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			WinConfirm.WinConfirmBuildData buildData = new WinConfirm.WinConfirmBuildData();
			buildData.setMessageText("重置设置参数？");
			WinConfirm warmDialog = new WinConfirm(buildData) {
				@Override
				public void onClickOk() {
					ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.config.restore", null, null);
				}

				@Override
				public String getReportDialogId() {
					return "txz_config_restore";
				}

			};
			warmDialog.show();
		}
	};
	
	private OnClickListener wakeConfigListener  = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			LogUtil.logi(" wakeConfigListener");
			TXZConfigManager.getInstance().enableWakeup((!image_wakeSwitch.isChecked()));
		}
	};
	
	private OnClickListener windowConfigListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			LogUtil.logi(" windowConfigListener");
			TXZConfigManager.getInstance().showFloatTool(image_windowSwitch.isChecked()?FloatToolType.FLOAT_NONE:FloatToolType.FLOAT_TOP);
		}
	};

	private void initView() {
		layout_wake = (RelativeLayout) findViewById(R.id.layout_wakeSwitch);
		layout_window = (RelativeLayout) findViewById(R.id.layout_windowSwitch);
		layout_wkwords = (RelativeLayout) findViewById(R.id.layout_arsCommand);
		layout_recogdB = (RelativeLayout) findViewById(R.id.layout_recogdB);
		layout_ttsdB = (RelativeLayout) findViewById(R.id.layout_ttsdB);
		layout_reset = (RelativeLayout) findViewById(R.id.layout_reset);
		layout_wake.setOnClickListener(wakeConfigListener);
		layout_window.setOnClickListener(windowConfigListener);
		image_wakeSwitch = (CheckedImageView) findViewById(R.id.iv_wakeSwitch);
		image_windowSwitch = (CheckedImageView)findViewById(R.id.iv_windowSwitch);
		imgbnt_backToRecord = (TextView) findViewById(R.id.imgbnt_backToRecord);
		imgbnt_backToRecord.setOnClickListener(backOnClickListener);
		bntHint_command = (Button) findViewById(R.id.bntHint_command);
		bntHint_command.setOnClickListener(addCommandOnClickListener);
		bntHint_recogdB = (Button) findViewById(R.id.bntHint_recogdB);
		bntHint_recogdB.setOnClickListener(changeRecogValueClickListener);
		bntHint_TtsdB = (Button) findViewById(R.id.bntHint_TtsdB);
		bntHint_TtsdB.setOnClickListener(changeTtsValueClickListener);
		txt_recogValue = (TextView) findViewById(R.id.txt_recogValue);
		txt_ttsValue = (TextView) findViewById(R.id.txt_ttsValue);
		bntHint_reset = (Button) findViewById(R.id.bntHint_reset);
		bntHint_reset.setOnClickListener(resetConfigClickListener);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		if (txt_recogValue.getText().toString().isEmpty()
				|| txt_ttsValue.getText().toString().isEmpty()) {
			initData();
		}
		txt_recogValue.setText(recogDescripe);
		txt_ttsValue.setText(ttsDescripe);
		super.onNewIntent(intent);
	}

	String recogDescripe = ""; // TXZCore获取的参数描述
	String ttsDescripe = "";// TXZCore获取的参数描述

	private void getRecogValueId(float recogValue) {
		if (recogValue <= -3.4f) {
			recogValueId = 0;
		} else if (recogValue <= -3.2f) {
			recogValueId = 1;
		} else if (recogValue <= -3.0f) {
			recogValueId = 2;
		} else if (recogValue <= -2.8f) {
			recogValueId = 3;
		} else {
			recogValueId = 4;
		}
	}

	private Integer getTtsValueId(Integer ttsValue) {
		if (ttsValue <= 35) {
			ttsValueId = 4;
		} else if (ttsValue <= 60) {
			ttsValueId = 3;
		} else if (ttsValue <= 80) {
			ttsValueId = 2;
		} else if (ttsValue <= 95) {
			ttsValueId = 1;
		} else{
			ttsValueId = 0;
		}
		return ttsValue;
	}

	private void initData() {
		Float wakeupThreshold = ConfigUtil.getWakeupThreshold();
		if (wakeupThreshold != null) {
			float recogValue = wakeupThreshold;
			getRecogValueId(recogValue);
		}

		Integer voiceSpeed = ConfigUtil.getVoiceSpeed();
		if (voiceSpeed != null) {
			int ttsValue = voiceSpeed;
			getTtsValueId(ttsValue);
		}
		
		recogDescripe = recogStrPrefix[recogValueId];
		ttsDescripe = ttsStrPrefix[ttsValueId];
		txt_recogValue.setText(recogDescripe);
		txt_ttsValue.setText(ttsDescripe);
		if (ConfigUtil.isWakeUpSound() != null) {
			image_wakeSwitch.setChecked(ConfigUtil.isWakeUpSound());
		}

		if (ConfigUtil.getFloatTool() != null) {
			if (ConfigUtil.getFloatTool().equals("FLOAT_NONE")) {
				image_windowSwitch.setChecked(false);
			} else {
				image_windowSwitch.setChecked(true);
			}
		}
	}
	
	ConfigListener mConfigListener = new ConfigListener() {
		@Override
		public void onConfigChanged(final String data) {
			AppLogicBase.runOnUiGround(new Runnable() {
				@Override
				public void run() {
					try {
						JSONObject config = new JSONObject(data);
						Float wakeupThreshhold = ConfigUtil.getConfigWakeupThreshhold(config);
						if (wakeupThreshhold != null) {
							getRecogValueId(wakeupThreshhold);
							txt_recogValue.setText(recogStrPrefix[recogValueId]);
						}
						Integer speedVoice = ConfigUtil.getConfigSpeedVoice(config);
						if (speedVoice != null) {
							getTtsValueId(speedVoice);
							txt_ttsValue.setText(ttsStrPrefix[ttsValueId]);
						}
						String[] wakeupKeywords = ConfigUtil.getConfigWakeupKeywords(config);
						if (wakeupKeywords != null) {
							// 刷新唤醒词
							if(ChangeCommandActivity.mCommandAdapter != null){
								ArrayList<String> wakeupKeywordList = new ArrayList<String>();
								for(String keyword : wakeupKeywords){
									wakeupKeywordList.add(keyword);
								}
								ChangeCommandActivity.mCommands = wakeupKeywordList;
								ChangeCommandActivity.mCommandAdapter.notifyDataSetChanged();
							}
						}
						Boolean isEnableWakeUp = ConfigUtil.getConfigWakeupSound(config);
						image_wakeSwitch.setChecked(isEnableWakeUp);
						
						if (ConfigUtil.getFloatTool() != null) {
							if (ConfigUtil.getFloatTool().equals("FLOAT_NONE")) {
								image_windowSwitch.setChecked(false);
							} else {
								image_windowSwitch.setChecked(true);
							}
						}
					} catch (Exception e) {
					}

				}
			}, 0);
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
		updateOptionsVisible(mHideOptions);
		ConfigUtil.registerConfigListener(mConfigListener);
	};
	
	private void updateOptionsVisible(int hideOptions){
		layout_wake.setVisibility(hideOptions%2==1?View.GONE:View.VISIBLE);
		layout_window.setVisibility((hideOptions/2)%2==1?View.GONE:View.VISIBLE);
		layout_wkwords.setVisibility((hideOptions/4)%2==1?View.GONE:View.VISIBLE);
		layout_recogdB.setVisibility((hideOptions/8)%2==1?View.GONE:View.VISIBLE);
		layout_ttsdB.setVisibility((hideOptions/16)%2==1?View.GONE:View.VISIBLE);
		layout_reset.setVisibility((hideOptions/32)%2==1?View.GONE:View.VISIBLE);
	}
	
	@Override
	protected void onDestroy() {
		ConfigUtil.unregisterConfigListener(mConfigListener);
		super.onDestroy();
	}

	/**
	 * 播报速度
	 * 
	 * @param values
	 */
	private void showTtsValueDialog() {
		AlertDialog.Builder builder = new Builder(MainActivity.this,
				AlertDialog.THEME_DEVICE_DEFAULT_DARK);
		builder.setTitle("语音播报速度");
		final AlertDialog dialog = builder.create();
		Window window = dialog.getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();
		lp.alpha = 0.9f;
		window.setAttributes(lp);
		builder.setSingleChoiceItems(ttsStr, ttsValueId,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						ttsValueId = which;
						txt_ttsValue.setText(ttsStrPrefix[ttsValueId]);
						//
						// 同步数据
						//
						int ttsValue = 0;
						switch (ttsValueId) {
						case 0:
							ttsValue = 100;
							break;
						case 1:
							ttsValue = 90;
							break;
						case 2:
							ttsValue = 70;
							break;
						case 3:
							ttsValue = 50;
							break;
						case 4:
							ttsValue = 20;
							break;
						default:
							break;
						}
						TXZTtsManager.getInstance().setVoiceSpeed(ttsValue);
						dialog.dismiss();
					}
				});
		builder.create().show();
	}

	/**
	 * 识别灵敏度
	 */
	private void showRecogValueDialog() {
		AlertDialog.Builder builder = new Builder(MainActivity.this,
				AlertDialog.THEME_DEVICE_DEFAULT_DARK);
		builder.setTitle("语音唤醒灵敏度");
		final AlertDialog dialog = builder.create();
		Window window = dialog.getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();
		lp.alpha = 0.9f;
		lp.width = (int) 800;
		lp.height = (int) 500;
		window.setAttributes(lp);
		builder.setSingleChoiceItems(recogStr, recogValueId,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						recogValueId = which;
						txt_recogValue.setText(recogStrPrefix[recogValueId]);
						float recogValue = 0.0f;
						switch (recogValueId) {
						case 0:
							recogValue = -3.5f;
							break;
						case 1:
							recogValue = -3.3f;
							break;
						case 2:
							recogValue = -3.1f;
							break;
						case 3:
							recogValue = -2.9f;
							break;
						case 4:
							recogValue = -2.7f;
							break;
						default:
							break;
						}
						TXZConfigManager.getInstance().setWakeupThreshhold(
								recogValue);
						dialog.dismiss();
					}
				});
		builder.create().show();
	}
}
