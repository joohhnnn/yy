package com.txznet.comm.ui.theme.test.view;

import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.ImageUtils;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IFloatView;

public class FloatView extends IFloatView {

	private static FloatView sInstance = new FloatView();
	private ImageView igView;

	private FloatView() {
	}

	public static FloatView getInstance() {
		return sInstance;
	}

	@Override
	public void release() {
		super.release();
	}
	
	@Override
	public ViewAdapter getView(ViewData data) {
		int talkHeight;
		if (WinLayout.isVertScreen){
			talkHeight = (int) LayouUtil.getDimen("x103");
		}else {
			int unit = (int) LayouUtil.getDimen(WinLayout.isVertScreen?"vertical_unit":"unit");
			talkHeight = 10 * unit;    //对话框高度
		}
		igView = new ImageView(GlobalContext.get());
		igView.setLayoutParams(new ViewGroup.LayoutParams(talkHeight, talkHeight));
		igView.setScaleType(ImageView.ScaleType.FIT_CENTER);
		igView.setImageDrawable(LayouUtil.getDrawable("person_float"));
		ViewAdapter viewAdapter = new ViewAdapter();
		viewAdapter.view = igView;
		viewAdapter.object = FloatView.getInstance();
		return viewAdapter;
	}
	
	@Override
	public void init() {
//		igView = new ImageView(GlobalContext.get());
//		igView.setImageDrawable(LayouUtil.getDrawable("mic"));
	}

	@Override
	public void updateState(int state) {
		//LogUtil.logd("updateState " + state);
//		if (textView != null) {
//			textView.setText("state:" + state);
//		}
	}

	@Override
	public void updateVolume(int volume) {
	}



}
