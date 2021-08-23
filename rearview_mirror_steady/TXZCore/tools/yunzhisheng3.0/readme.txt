一、开启回音消除功能
前提:设备硬件已支持双声道录音。
开启方法:使用TXZ_SDK初始化TXZCore时需设置initParam.setFilterNoiseType(1);不开启该功能，则不需要设置此参数。

二、切换TTS模型。暂时主要是在默认模型和林志林音色模型之间切换。
操作:
1、需手动将林志林音色模型文件拷贝到设备上。林志林音色模型文件是这个文件:TXZCore\tools\yunzhisheng3.0\backend_lzl。
2、初始化TXZCore成功后可调用TXZTtsManager.getInstance().setTtsModel(String modelPath)来切换TTS模型，modelPath为TTS模型文件在设备上的绝对路径。
如果modelPath等于null,则会切回默认的TTS模型。