package com.txznet.music.receiver;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.service.MediaPlaybackService;

/**
 * 耳机线控管理助手类 单例
 * 
 * @author 蛋蛋
 *
 */
public class HeadSetHelper {

	private static HeadSetHelper headSetHelper;

	private OnHeadSetListener headSetListener = null;

	private HangUpReceiver headsetPlugReceiver;
	Intent intent;

	public static HeadSetHelper getInstance() {
		if (headSetHelper == null) {
			headSetHelper = new HeadSetHelper();
		}
		return headSetHelper;
	}

	/**
	 * 设置耳机单击双击监听接口 必须在open前设置此接口，否则设置无效
	 * 
	 * @param headSetListener
	 */
	public void setOnHeadSetListener(OnHeadSetListener headSetListener) {
		this.headSetListener = headSetListener;
	}

	/**
	 * 开启耳机线控监听, 请务必在设置接口监听之后再调用此方法，否则接口无效
	 * 
	 * @param context
	 */
	@SuppressLint("NewApi")
	public void open(final Context context) {
		LogUtil.logd("registerMediaButton");
		intent = new Intent(context, MediaPlaybackService.class);
		context.startService(intent);
	}

	/**
	 * 关闭耳机线控监听
	 * 
	 * @param context
	 */
	public void close(Context context) {
		LogUtil.logd("unregisterMediaButton");
		if (intent != null) {
			context.stopService(intent);
			intent = null;
		}
	}

	/**
	 * 删除耳机单机双击监听接口
	 */
	public void delHeadSetListener() {
		this.headSetListener = null;
	}

	/**
	 * 获取耳机单击双击接口
	 * 
	 * @return
	 */
	protected OnHeadSetListener getOnHeadSetListener() {
		return headSetListener;
	}

	/**
	 * 耳机按钮单双击监听
	 * 
	 * @author
	 *
	 */
	public interface OnHeadSetListener {
		/**
		 * 单击触发,主线程。 此接口真正触发是在单击操作1秒后 因为需要判断1秒内是否仍监听到点击，有的话那就是双击了。
		 * <p>
		 * 如果您有更好的解决办法，请联系我： qq495389040
		 */
		public void onClick();

		/**
		 * 双击触发，此接口在主线程，可以放心使用
		 */
		public void onDoubleClick();
	}
}
