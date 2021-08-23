package com.txznet.txz;

//尽量使用短文本，减少上报数据大小

public interface ReportEventConst {
	String Client = "client";
	
	String Call = "call";
	String CallOut = "out"; // 
	String CallIn = "in";
	
	String Nav = "nav";
	String NavSearch = "search"; // 发起导航，包括手动输入和语音输入，进入导航列表页面
	String NavView = "view";// 进入导航界面,导航来源：回家，去公司,历史记录，搜索；  触发方式：声控和手动
	String NavHome = "home";
	String NavCompany = "com";
	String NavHistory = "history";
	String NavParamFromVoice = "fromVoice";
	String NavParamNavSource = "src"; // "search","home","history","company"
}
