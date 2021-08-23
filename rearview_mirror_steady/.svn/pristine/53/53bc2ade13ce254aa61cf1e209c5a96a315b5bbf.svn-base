package com.txznet.txz.service; //换成每个应用自己的包名

interface IService{   
		/**
		* 调用接口
		*     packageName  调用方自己的名字
		*     eventId  主事件
		*     subEventId 子事件
		*     data 传递的数据
		* 返回码
		*/
        byte[] sendInvoke(in String packageName, in String command, in byte[] data);
        
        /**
        通知远程自己方的接口
        */
        //void setSelfInterface(in String packageName, in IService service);
}

