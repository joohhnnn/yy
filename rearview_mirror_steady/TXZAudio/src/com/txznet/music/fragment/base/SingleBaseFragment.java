package com.txznet.music.fragment.base;

import java.util.List;
import java.util.Observable;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.music.Constant;
import com.txznet.music.bean.response.Category;
import com.txznet.music.bean.response.ResponseSearchAlbum;
import com.txznet.music.fragment.BaseDataFragment;

public abstract class SingleBaseFragment<T> extends BaseDataFragment<T> {/*
	protected Category mCategory;

	@Override
	public void update(Observable observable, Object data) {

		if (data instanceof InfoMessage) {
			InfoMessage info = (InfoMessage) data;
			switch (info.getType()) {
			case InfoMessage.REQ_CATEGORY_ALL:
				LogUtil.logd(TAG + "reqData:REQ_CATEGORY_ALL");
				List<T> arrCategory = (List<T>) info.getObj();
				notify(arrCategory);
				break;
			case InfoMessage.RESP_ALBUM:
				ResponseSearchAlbum responseAlbum = (ResponseSearchAlbum) info
						.getObj();
				if (null != mCategory
						&& String.valueOf(mCategory.getCategoryId()).equals(
								responseAlbum.getCategoryId())) {
					pageOff = responseAlbum.getPageId();
					if (pageOff == 1) {// 如果是第一页
						notifyAlbum(responseAlbum.getArrAlbum(), false);
					} else {
						notifyAlbum(responseAlbum.getArrAlbum(), true);
					}

				}
				break;
			case InfoMessage.NET_ERROR:
				showNetTimeOutView(Constant.SPEAK_NONE_NET);
				break;

			case InfoMessage.NET_TIMEOUT_ERROR:
				showNetTimeOutView(Constant.SPEAK_TIPS_TIMEOUT);
				break;

			default:
				break;
			}
		}
	}

	@Override
	public void reqData() {
		// TODO Auto-generated method stub

	}

	@Override
	public int getLayout() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getFragmentId() {
		// TODO Auto-generated method stub
		return 0;
	}

*/}
