package com.txznet.txz.plugin;

import com.txz.ui.innernet.UiInnerNet;

/**
 * Created by ASUS User on 2018/10/29.
 */

public interface IPluginDownloadListener {
    void onDownload(UiInnerNet.PluginFile pluginFile,String path);
}
