package com.txznet.txz.component.asr.ifly;

public interface MsgConst {
	int MSG_REGISTER_CLIENT = 0;
	int MSG_READY = 3; //SDK初始化就绪
	int MSG_REG_KEYWORDS = 4; //注册词典
	int MSG_SUCCESS = 5; //词典注册成功
	int MSG_EXIT_SERVICE = 6; //退出
}
