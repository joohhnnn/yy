package com.txznet.txz.module.account;

import java.io.UnsupportedEncodingException;

import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.txz.equipment_manager.EquipmentManager;
import com.txz.equipment_manager.EquipmentManager.Req_Account_Register;
import com.txz.equipment_manager.EquipmentManager.Req_Verification_Code;
import com.txz.equipment_manager.EquipmentManager.Resp_Account_Register;
import com.txz.equipment_manager.EquipmentManager.Resp_Verification_Code;
import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.equipment.UiEquipment.AccountInfo;
import com.txz.ui.event.UiEvent;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.app.PackageManager;
import com.txznet.txz.module.login.LoginManagerImpl.OnGetVerifyCodeListener;
import com.txznet.txz.ui.win.login.LoginView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.text.TextUtils;

public class AccountManager extends IModule {
	public static final String JUMP_PHONE = "13088889999";
	private static final String TAG = "Account:";
    private static final String SP_NAME = "account";
    
    private static final String SP_KEY_PHONE = "phone";
    private static final String SP_KEY_REGISTER = "register";
    private static final String SP_KEY_GUIDE_FINISH = "finish";
    private static final String SP_KEY_GUIDE_PACKAGE = "package";

    private static final String ACTION_GUIDE_FINISH = "com.txznet.guide.finish";

	private String mCurrUserPhone;
	
	// 输入错误的时候是否允许跳过
	private boolean mCanSkipLogin = false;
	private OnGetVerifyCodeListener mGetListener;
	
	private static final AccountManager sInstance = new AccountManager();
    private final SharedPreferences mSp;

    public static AccountManager getInstance() {
		return sInstance;
	}

	private AccountManager() {
        mSp = GlobalContext.get().getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    }

	@Override
	public int initialize_AfterInitSuccess() {
        boolean isRegister = mSp.getBoolean(SP_KEY_REGISTER, false);

        if (isRegister && !isGuideFinish()) {
            openGuideApp();
        }

        IntentFilter intentFilter = new IntentFilter(ACTION_GUIDE_FINISH);

        GlobalContext.get().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                LogUtil.d(TAG + "guide finish");
                mSp.edit().putBoolean(SP_KEY_GUIDE_FINISH, true).apply();
            }
        }, intentFilter);

        return super.initialize_AfterInitSuccess();
    }

	@Override
	public int initialize_BeforeStartJni() {
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_RESP_VERIFICATION_CODE);
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_RESP_ACCOUNT_REGISTER);

		return super.initialize_BeforeStartJni();
	}

	/**
	 * 会话ID
	 */
	private static int mSessionId = 0;

	
	@Override
	public int onCommand(String cmd) {
		return super.onCommand(cmd);
	}


    /**
     * 判断新手引导是否完成
     *
     * @return 新手引导完成返回true
     */
    private boolean isGuideFinish() {
        return mSp.getBoolean(SP_KEY_GUIDE_FINISH, false);
    }

	@Override
	public int onEvent(int eventId, int subEventId, byte[] data) {
		switch (eventId) {
		case UiEvent.EVENT_ACTION_EQUIPMENT:
			switch (subEventId) {
			case UiEquipment.SUBEVENT_RESP_VERIFICATION_CODE:
				//请求验证码响应
				Resp_Verification_Code resp_Verification_Code = null;
				try {
					resp_Verification_Code = EquipmentManager.Resp_Verification_Code.parseFrom(data);
				} catch (InvalidProtocolBufferNanoException e) {
					e.printStackTrace();
				}
				
				if (mGetListener != null && resp_Verification_Code != null) {
					mGetListener.onGetVerifyCode(resp_Verification_Code);

					int retCode = 0;
					if (resp_Verification_Code.retCode != null) {
						retCode = resp_Verification_Code.retCode;
					}
					mGetListener.onVerifyCode(retCode, getVerifyReson(retCode));
				}
				
				LogUtil.logd(TAG + "verification code:"
						+ (resp_Verification_Code == null ? "null" : resp_Verification_Code.toString())
						+ ",listener reqSession:" + (mGetListener != null ? mGetListener.req_session : "null"));
				break;
			case UiEquipment.SUBEVENT_RESP_ACCOUNT_REGISTER:
				if (data == null) {
					LogUtil.logd(TAG + "onResp data is null！");
					break;
				}
				//注册响应
				Resp_Account_Register resp_Account_Register = null;
				try {
					resp_Account_Register = EquipmentManager.Resp_Account_Register.parseFrom(data);
				} catch (InvalidProtocolBufferNanoException e) {
					e.printStackTrace();
				}
				LogUtil.logd(TAG + "register " + (resp_Account_Register == null ? "null" : resp_Account_Register.toString()));

                if(null == resp_Account_Register){
                    break;
                }
                
				int retCode = 0;
				if (resp_Account_Register.retCode != null) {
					retCode = resp_Account_Register.retCode;
				}
                
				if (mGetListener != null) {
					mGetListener.onVerifyCode(retCode, getVerifyReson(retCode));
				}

				if (retCode == EquipmentManager.EC_CODE_OK) {
					mSp.edit().putBoolean(SP_KEY_REGISTER, true).apply();
					openGuideApp();
				} else {
                    LogUtil.loge(TAG + "register error " + resp_Account_Register.retCode);
                }
                break;

			default:
				break;
			}
			break;

		default:
			break;
		}
		return super.onEvent(eventId, subEventId, data);
	}
	
	/**
	 * 打开新手引导APP
	 */
	public void openGuideApp() {
		LoginView.getInstance(GlobalContext.get()).dismiss();
		// 启动新手引导
		String packageName = mSp.getString(SP_KEY_GUIDE_PACKAGE, null);
		if (!TextUtils.isEmpty(packageName)) {
			PackageManager.getInstance().openApp(packageName);
		}
	}

	/**
	 * 验证码登录错误码
	 * @param errorCode
	 * @return
	 */
	private String getVerifyReson(int errorCode) {
		String reson = "";
		switch (errorCode) {
		case EquipmentManager.EC_CODE_SEND:
			reson = "短信发送失败";
			break;
		case EquipmentManager.EC_CODE_ERROR:
			reson = "验证码错误";
			break;
		case EquipmentManager.EC_CODE_PHONE:
			reson = "查找不到对话或手机号";
			break;
		case EquipmentManager.EC_CODE_SAVE:
			reson = "后台保存验证码失败";
			break;
		case EquipmentManager.EC_CODE_TIME:
			reson = "验证码过期";
			break;
		default:
			break;
		}
		return reson;
	}
	
	/**
	 * 请求短信验证码
	 * @param number 手机号
	 */
	public void requestVerificationCode(String number, OnGetVerifyCodeListener listener) {
		if (TextUtils.isEmpty(number)) {
			return;
		}
		LogUtil.logd(TAG + "request code " + number);
		mSessionId++;
		mGetListener = listener;
		if (mGetListener != null) {
			mGetListener.req_session = mSessionId;
		}
		Req_Verification_Code req_Verification_Code = new EquipmentManager.Req_Verification_Code();
		req_Verification_Code.phoneNumber = number.getBytes();
		req_Verification_Code.sessionId = mSessionId;
		JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_VERIFICATION_CODE,
				req_Verification_Code.toByteArray(req_Verification_Code));
		
		saveUserPhone(number);
	}
	
	/**
	 * 注册
	 * @param number 手机号
	 * @param code 短信验证码
	 */
	public void requestRegister(String number, String code, OnGetVerifyCodeListener listener) {
		mGetListener = listener;
		
		LogUtil.logd(TAG + "requestRegister " + number + " " + code);
		Req_Account_Register req_Account_Register = new EquipmentManager.Req_Account_Register();
		req_Account_Register.sessionId = mSessionId;
		req_Account_Register.phoneNumber = number.getBytes();
		req_Account_Register.smCode = code.getBytes();
		JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_ACCOUNT_REGISTER,
				req_Account_Register.toByteArray(req_Account_Register));
	}
	
	public void saveUserPhone(String number) {
		try {
			// 保留当前手机号
			mSp.edit().putString(SP_KEY_PHONE, number).apply();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取用户的手机号
	 * @return
	 */
	public String getUserPhone() {
		String phone = mSp.getString(SP_KEY_PHONE, "");
		LogUtil.logd("cache phone:" + phone);
		return phone;
	}
	
	/**
	 * 输入验证码错误的时候能否跳过
	 * 
	 * @return
	 */
	public boolean canSkipLogin() {
		return mCanSkipLogin;
	}
	
	/**
	 * 配置项回调
	 * @param accountInfo 账户系统配置项
	 */
	public void onConfig(AccountInfo accountInfo) {
		if (accountInfo == null) {
			LogUtil.loge(TAG + "config is null");
			return;
		}
		LogUtil.logd(TAG + "config " + accountInfo.toString());

		int uint32AccountFlag = 0;
		if (accountInfo.uint32AccountFlag != null) {
			uint32AccountFlag = accountInfo.uint32AccountFlag;
		}

		mCurrUserPhone = new String(accountInfo.strPhoneNumber);
		mCanSkipLogin = (uint32AccountFlag & UiEquipment.FLAG_CAN_SKIN) == UiEquipment.FLAG_CAN_SKIN;
		LogUtil.logd("userPhone:" + mCurrUserPhone + ",canSkipLogin:" + mCanSkipLogin);

		boolean registered = mSp.getBoolean(SP_KEY_REGISTER, false);
		boolean isShow = (uint32AccountFlag & UiEquipment.FLAG_IS_SHOW) == UiEquipment.FLAG_IS_SHOW;
		LogUtil.logd(TAG + "registered:" + registered + " show:" + isShow);
		if (!registered && isShow) {
			LoginView.getInstance(GlobalContext.get()).show();
		}

		if (accountInfo.strPackagesName != null) {
			try {
				String packageName = new String(accountInfo.strPackagesName, "utf-8");
				mSp.edit().putString(SP_KEY_GUIDE_PACKAGE, packageName).apply();

				if (!isShow && !isGuideFinish()) {
					openGuideApp();
				}
			} catch (UnsupportedEncodingException e) {
				LogUtil.loge(TAG, e);
				e.printStackTrace();
			}
		}
    }
	
	/**
	 * 验证手机格式
	 */
	public static boolean isMobile(String number) {
		/*
		 * 移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188
		 * 联通：130、131、132、152、155、156、185、186 电信：133、153、180、189、（1349卫通）
		 * 总结起来就是第一位必定为1，第二位必定为3或5或8，其他位置的可以为0-9
		 */
		String num = "[1]\\d{10}";// "[1]"代表第1位为数字1，"[358]"代表第二位可以为3、5、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
		if (TextUtils.isEmpty(number)) {
			return false;
		} else {
			// matches():字符串是否在给定的正则表达式匹配
			return number.matches(num);
		}
	}
}
