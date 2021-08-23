package com.txznet.record.setting;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.txznet.comm.base.BaseActivity;
import com.txznet.comm.remote.util.ConfigUtil;
import com.txznet.sdk.TXZConfigManager;
import com.txznet.txz.comm.R;

public class ChangeCommandActivity extends BaseActivity {

	private static boolean mWkwordsEditable = true;

	public static void enableWkWordsEditable(boolean editable) {
		mWkwordsEditable = editable;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.layout_asrcommand);
		initView();
		initData();
		mCommandAdapter = new MyCommandAdapter(ChangeCommandActivity.this);
		lv_command.setAdapter(mCommandAdapter);
	}

	@Override
	protected void onResume() {
		initEditable(mWkwordsEditable);
		super.onResume();
	}

	private void initEditable(boolean editable) {
		layout_addcommand.setVisibility(editable ? View.VISIBLE : View.GONE);
		if (mCommands.size() >= 4) {
			layout_addcommand.setVisibility(View.GONE);
		}
		mCommandAdapter.setEditable(editable);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		if (mCommands.isEmpty()) {
			initData();
		}
		if (mCommandAdapter == null) {
			mCommandAdapter = new MyCommandAdapter(ChangeCommandActivity.this);
		}
		mCommandAdapter.notifyDataSetChanged();
		super.onNewIntent(intent);
	}

	@Override
	protected void onDestroy() {
		mCommandAdapter = null;
		super.onDestroy();
	}

	private ListViewForScrollView lv_command;
	private RelativeLayout layout_addcommand;
	private ImageButton imgbnt_add;
	private TextView imgbnt_backToSetting;
	static MyCommandAdapter mCommandAdapter;
	static ArrayList<String> mCommands = new ArrayList<String>();

	private OnClickListener backToSettingClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			finish();
		}
	};

	private OnClickListener addClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			synchronized (v) {
				if (mCommands.size() >= 4) {
					Toast.makeText(ChangeCommandActivity.this, "亲，唤醒词太多，体验效果更不好哦", Toast.LENGTH_LONG).show();
					return;
				} else {
					changeCommandDialog("", ChangeCommandActivity.this, "添加唤醒词", -1);
				}
			}
		}
	};

	private void initView() {
		lv_command = (ListViewForScrollView) findViewById(R.id.lv_command);
		imgbnt_add = (ImageButton) findViewById(R.id.imgbnt_add);
		imgbnt_add.setOnClickListener(addClickListener);
		layout_addcommand = (RelativeLayout) findViewById(R.id.layout_addcommand);
		layout_addcommand.setOnClickListener(addClickListener);
		imgbnt_backToSetting = (TextView) findViewById(R.id.imgbnt_backToSetting);
		imgbnt_backToSetting.setOnClickListener(backToSettingClickListener);
	}

	private void initData() {
		String[] keywords = ConfigUtil.getWakeupKeywords();
		if (keywords != null) {
			mCommands.clear();
			for (int i = 0; i < keywords.length; i++) {
				if (i < 4) {
					mCommands.add(keywords[i]);
				}
			}
		}
	}

	/**
	 * 修改唤醒词
	 * 
	 * @param command
	 * @param context
	 * @param title
	 * @param position
	 *            -1 表示添加唤醒词
	 */
	public static void changeCommandDialog(final String command, final Context context, String title, final int position) {
		final TXZEditDialog dialog = new TXZEditDialog(context);
		dialog.setCanceledOnTouchOutside(false);
		final EditText editText = (EditText) dialog.getEditText();
		editText.setText(command);
		TextView titleTxt = (TextView) dialog.getTitleTextView();
		titleTxt.setText(title);
		dialog.setOnPositiveListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (editText == null || editText.getText().toString().trim().isEmpty()) {
					synchronized (v) {
						Toast.makeText(context, "唤醒词不能为空", Toast.LENGTH_LONG).show();
					}
				} else if (editText != null && editText.getText().toString().trim().length() < 4) {
					synchronized (v) {
						Toast.makeText(context, "亲，唤醒词字数太少，识别效果不好哦", Toast.LENGTH_LONG).show();
					}
				} else {
					if (mCommands == null) {
						mCommands = new ArrayList<String>();
					}
					if (command.isEmpty() && position == -1) {
						if (mCommands.contains(editText.getText().toString().trim())) {
							synchronized (v) {
								Toast.makeText(context, "您添加的唤醒词已存在", Toast.LENGTH_LONG).show();
							}
						} else {
							mCommands.add(editText.getText().toString().trim());
							mCommandAdapter.notifyDataSetChanged();
						}
					} else if (!command.isEmpty() && position != -1) {
						mCommands.set(position, editText.getText().toString().trim());
						mCommandAdapter.notifyDataSetChanged();
					}
					TXZConfigManager.getInstance()
							.setWakeupKeywordsNew(mCommands.toArray(new String[mCommands.size()]));
					dialog.dismiss();
				}
			}
		});
		dialog.setOnNegativeListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}

}
