package com.txznet.txz.plugin;

public interface IExecPluginV1 {
	/**
	 * 
	 * @return 插件版本号
	 */
	public String getVersion();

	/**
	 * 
	 * 后台可以下发数据接口，插件可以带参数，这样同一个插件编译好可以处理多个任务
	 *
	 * @param loader
	 *            插件的类装载器
	 * @param path
	 *            插件文件的路径
	 * @param data
	 *            插件执行的数据参数
	 * @return
	 */
	public Object execute(ClassLoader loader, String path, byte[] data);
	
	/**
	 * comm版本见 PluginManager.PLUGIN_MGR_VERSION
	 * 返回需要支持该插件的最小comm版本
	 * @return
	 */
	public int getMinSupportCommVersion();
}
