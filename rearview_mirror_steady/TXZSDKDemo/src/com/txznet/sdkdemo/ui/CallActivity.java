package com.txznet.sdkdemo.ui;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.txznet.sdk.TXZCallManager;
import com.txznet.sdk.TXZCallManager.CallTool;
import com.txznet.sdk.TXZCallManager.CallToolStatusListener;
import com.txznet.sdk.TXZCallManager.Contact;
import com.txznet.sdk.TXZConfigManager;
import com.txznet.sdkdemo.bean.DebugUtil;
import com.txznet.sdkdemo.bean.DemoButton;

public class CallActivity extends BaseActivity {

	private CallToolStatusListener mCallToolStatusListener;
	private CallTool mCallTool = new CallTool() {
		@Override
		public void setStatusListener(CallToolStatusListener listener) {
			// 记录下listener，适当的时机通知sdk状态变
			mCallToolStatusListener = listener;
			if (listener != null) {
				// TODO 通知最后的电话状态
				listener.onEnabled();
				listener.onIdle();
			}
		}

		@Override
		public boolean rejectIncoming() {
			DebugUtil.showTips("模拟拒接来电");
			return true;
		}

		@Override
		public boolean makeCall(Contact contact) {
			DebugUtil.showTips("模拟打电话给" + contact.getName()
					+ contact.getNumber());
			return true;
		}

		@Override
		public boolean hangupCall() {
			DebugUtil.showTips("模拟挂断电话");
			return true;
		}

		@Override
		public CallStatus getStatus() {
			return CallStatus.CALL_STATUS_IDLE;
		}

		@Override
		public boolean acceptIncoming() {
			DebugUtil.showTips("模拟接受来电");
			return true;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addDemoButtons(new DemoButton(this, "模拟电话工具", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZCallManager.getInstance().setCallTool(mCallTool);
				
				DebugUtil.showTips("已启用模拟电话工具");
			}
		}), new DemoButton(this, "取消电话工具", new OnClickListener() {
			@Override
			public void onClick(View v) {
				mCallToolStatusListener = null;
				TXZCallManager.getInstance().setCallTool(null);
				
				DebugUtil.showTips("已清除模拟电话工具");
			}
		}));

		addDemoButtons(new DemoButton(this, "去电状态", new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mCallToolStatusListener != null) {
					Contact con = new Contact();
					con.setName("张三");
					con.setNumber("10086");
					mCallToolStatusListener.onMakeCall(con);
				}
				
				DebugUtil.showTips("已模拟去电状态");
			}
		}), new DemoButton(this, "来电状态", new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mCallToolStatusListener != null) {
					Contact con = new Contact();
					con.setName("张三");
					con.setNumber("10086");
					mCallToolStatusListener.onIncoming(con,
							true/* 是否tts播报来电信息 */, true/* 是否启动声控识别接听拒接 */);
				}
				
				DebugUtil.showTips("已模拟来电状态");
			}
		}), new DemoButton(this, "空闲状态", new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mCallToolStatusListener != null) {
					mCallToolStatusListener.onIdle();
				}
				
				DebugUtil.showTips("已模拟空闲状态");
			}
		}), new DemoButton(this, "接通状态", new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mCallToolStatusListener != null) {
					mCallToolStatusListener.onOffhook();
				}
				
				DebugUtil.showTips("已模拟接通状态");
			}
		}));

		addDemoButtons(new DemoButton(this, "同步联系人", new OnClickListener() {
			@Override
			public void onClick(View v) {
				syncContacts();
			}
		}), new DemoButton(this, "蓝牙连上状态", new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mCallToolStatusListener != null) {
					mCallToolStatusListener.onEnabled();
				}
				
				DebugUtil.showTips("已模拟连接状态");
			}
		}), new DemoButton(this, "蓝牙断开状态", new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mCallToolStatusListener != null) {
					mCallToolStatusListener
							.onDisabled("很抱歉，蓝牙断开了，电话不可用了"/* 这里的提示会直接交互展示给用户 */);
				}
				
				DebugUtil.showTips("已模拟断开状态");
			}
		}));
	}

	/**
	 * 调用时机：SDK初始化完成、蓝牙连接上、联系人数据发生变更
	 */
	public static void syncContacts() {
		// TODO 同步前先判断是否初始化成功
		if (!TXZConfigManager.getInstance().isInitedSuccess())
			return;

		List<Contact> lst = new ArrayList<Contact>();
		Contact con;
		con = new Contact();
		con.setName("张三");
		con.setNumber("30001");
		lst.add(con);
		con = new Contact();
		con.setName("张三");
		con.setNumber("30002");
		lst.add(con);
		con = new Contact();
		con.setName("张三");
		con.setNumber("30003");
		lst.add(con);
		con = new Contact();
		con.setName("章三");
		con.setNumber("30100");
		lst.add(con);
		con = new Contact();
		con.setName("張三");
		con.setNumber("30200");
		lst.add(con);
		con = new Contact();
		con.setName("李四");
		con.setNumber("40000");
		lst.add(con);
		con = new Contact();
		con.setName("杨总-腾讯");
		con.setNumber("40001");
		lst.add(con);
		con = new Contact();
		con.setName("杨总-华为");
		con.setNumber("40002");
		lst.add(con);
		con = new Contact();
		con.setName("杨总-百度");
		con.setNumber("40003");
		lst.add(con);
		con = new Contact();
		con.setName("曾茜");
		con.setNumber("40001");
		lst.add(con);
		con = new Contact();
		con.setName("层倩");
		con.setNumber("40002");
		lst.add(con);
		con = new Contact();
		con.setName("层希");
		con.setNumber("40003");
		lst.add(con);
		con = new Contact();
		con.setName("增倩");
		con.setNumber("40004");
		lst.add(con);
		con = new Contact();
		con.setName("增希");
		con.setNumber("40005");
		lst.add(con);
		con = new Contact();
		con.setName("号码测试");
		con.setNumber("15361088570");
		lst.add(con);
		con = new Contact();
		con.setName("号码测试");
		con.setNumber("5678");
		lst.add(con);
		con = new Contact();
		con.setName("号码测试");
		con.setNumber("075588888888");
		lst.add(con);
		con = new Contact();
		con.setName("归属地运营商");
		con.setNumber("15361088570");
		lst.add(con);
		con = new Contact();
		con.setName("归属地运营商");
		con.setNumber("15334565678");
		lst.add(con);
		con = new Contact();
		con.setName("归属地运营商");
		con.setNumber("18612997778");
		lst.add(con);
		TXZCallManager.getInstance().syncContacts(lst);
		
		DebugUtil.showTips("已同步联系人：张三、李四、曾茜、杨总、号码测试、归属地运营商");
	}

}
