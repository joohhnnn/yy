package com.txznet.music.bean;

import java.util.List;

public interface IFinishCallBack<T> {

	void onComplete(List<T> result);// 完成

	void onError(String error);// 异常
}
