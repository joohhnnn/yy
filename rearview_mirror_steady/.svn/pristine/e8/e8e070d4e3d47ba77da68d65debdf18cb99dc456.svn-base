package com.txznet.txz.plugin;

import android.util.Log;

public class TestPlugin implements IExecPluginV1, IExecPluginVersion  {
	
	
	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public Object execute(ClassLoader loader, String path, byte[] data) {
		Log.d("TestPlugin", "pbh loader=" + loader);
		Log.d("TestPlugin", "pbh path=" + path);
		Log.d("TestPlugin", "pbh data=" + data);
		
		TestPluginLogic.test();
		return null;
	}

	@Override
	public int getMinSupportCommVersion() {
		return 1;
	}

	@Override
	public int getPluginInterfaceVersion() {
		return 1;
	}
	
}
