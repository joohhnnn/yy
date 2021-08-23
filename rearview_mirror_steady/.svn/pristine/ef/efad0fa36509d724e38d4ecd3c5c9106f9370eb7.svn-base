package com.txznet.comm.ui.recordwin;

import android.graphics.drawable.Drawable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.txznet.comm.ui.WinRecordObserver.WinRecordCycleObserver;
import com.txznet.comm.ui.layout.IWinLayout;
import com.txznet.comm.ui.layout.layout1.TXZWinLayout2;

public class RecordWin2Impl3 implements IRecordWin2{

	private static RecordWin2Impl3 sInstance = new RecordWin2Impl3();
	
	public Float mWinBgAlpha;
	public WinRecordCycleObserver mWinRecordCycleObserver;
	
	private RecordWin2Impl3(){
	}
	
	public static RecordWin2Impl3 getInstance() {
		return sInstance;
	}
	
	@Override
	public void init() {
		TXZWinLayout2.getInstance().init();
	}

	@Override
	public boolean isShowing() {
		return isShow;
	}

	@Override
	public void setIsFullSreenDialog(boolean isFullScreen) {
	}

	@Override
	public void setWinBgAlpha(Float winBgAlpha) {
		this.mWinBgAlpha = winBgAlpha;
	}

	@Override
	public void updateWinLayout(IWinLayout winLayout) {
		
	}

	boolean isShow = false;
	private Class clazzActivity;
	private Method methodShow;
	private Method methodDismiss;
	
	
	@Override
	public void show() {
		try {
			isShow = true;
			if(clazzActivity == null){
				clazzActivity = Class.forName("com.txznet.reserve.activity.ReserveSingleInstanceActivity0");
			}
			if(methodShow == null){
				methodShow = clazzActivity.getDeclaredMethod("show");	
			}
			methodShow.invoke(clazzActivity);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void dismiss() {
		try {
			isShow = false;
			if(clazzActivity == null){
				clazzActivity = Class.forName("com.txznet.reserve.activity.ReserveSingleInstanceActivity0");
			}
			if(methodDismiss == null){
				methodDismiss = clazzActivity.getDeclaredMethod("dismiss");	
			}
			methodDismiss.invoke(clazzActivity);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setWinRecordObserver(WinRecordCycleObserver observer) {
		this.mWinRecordCycleObserver = observer;
	}

	@Override
	public void setWinType(int type) {
		
	}

	@Override
	public void setWinFlags(int flags) {
		
	}

	@Override
	public void newInstance() {
		// TODO 用到这个类的方案公司暂时没这个需求，懒得加了
	}

	@Override
	public void setContentWidth(int width) {
		//TODO 用到这个类的方案公司暂时没这个需求，懒得加了
	}

	@Override
	public void setIfSetWinBg(boolean ifSet) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateDisplayArea(int x, int y, int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDialogCancel(boolean flag) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void setSystemUiVisibility(int type) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDialogCanceledOnTouchOutside(boolean cancel) {
		
	}

	@Override
	public void setAllowOutSideClickSentToBehind(boolean allow) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateBackground(Drawable drawable) {

	}

	@Override
	public void setWinSoft(int soft) {
		// TODO Auto-generated method stub
		
	}
}
