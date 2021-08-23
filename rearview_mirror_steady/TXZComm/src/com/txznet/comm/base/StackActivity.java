package com.txznet.comm.base;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import java.lang.reflect.Field;

class StackActivity extends FragmentActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActivityStack.getInstance().push(this);

        setTheme();
    }

    /**
     * 设置Activity主题
     * 默认采用AppTransparentTheme, 如需设置其他主题可以选择重写此方法.
     * <p>
     * <Strong>注意:</Strong>重写此方法时建议参考方法原实现, 通过反射方式进行设置, 规避旧版本
     * 升级时资源文件替换不彻底导致的资源找不到的问题.
     */
    protected void setTheme() {
        try {
            Class<?> clsRstyle = Class.forName("com.txznet.txz.comm.R$style");
			Field f = clsRstyle.getDeclaredField("AppTransparentTheme");
			this.setTheme(f.getInt(null));
		} catch (Exception e) {
		}
	}

	@Override
	protected void onDestroy() {
		ActivityStack.getInstance().pop(this);
		super.onDestroy();
		if (isFinishing()) {
			if (!ActivityStack.getInstance().has()) {
				// 界面全部释放了
				BaseApplication.callAppLogicMethod("destroy");
			}
		}
	}

    @Override
    protected void onStart() {
        super.onStart();
        ActivityStack.getInstance().pushForeground();
    }

    @Override
    protected void onStop() {
        super.onStop();
        ActivityStack.getInstance().popForeground();
    }
}
