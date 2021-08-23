package com.txznet.txz.component.text.txz;

import com.txznet.txz.plugin.IExecPlugin;
import com.txznet.txz.plugin.PluginManager;

public class TextTestAddImpl implements IExecPlugin{

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public Object execute(ClassLoader loader, String path, byte[] data) {
		TextTxzTestImpl testImpl = new TextTxzTestImpl();
		testImpl.initialize(null);
		boolean ret = (Boolean)PluginManager.invoke("txz.nlp.changeImpl", testImpl,2);
		PluginManager.invoke("comm.log.logd", "liTest:ret="+ret);
		return null;
	}

}
