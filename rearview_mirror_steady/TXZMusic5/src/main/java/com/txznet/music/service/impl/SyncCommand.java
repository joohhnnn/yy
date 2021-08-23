package com.txznet.music.service.impl;

/**
 * Core所需要的一些信息
 *
 * @author telen
 * @date 2018/12/28,10:52
 */
public class SyncCommand extends BaseCommand {
    public SyncCommand() {


        //获取当前版本,因为可能涉及热升级,AndroidManifest中的版本号是不会变的.
        addCmd("get.version", (pkgName, cmd, data) -> {
            // FIXME: 2018/12/28 这里感觉怪怪的,理论上应该返回具体的版本号不是吗?需要看下Core的相关处理的位置.
            return String.valueOf(true).getBytes();
        });
        //是否需要支持"历史"相关的一些声控指令
        addCmd("history.support", (pkgName, cmd, data) -> ("" + false).getBytes());
    }
}
