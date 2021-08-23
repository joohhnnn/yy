package com.txznet.music.service.impl;

/**
 * 内部处理逻辑
 *
 * @author telen
 * @date 2018/12/28,14:57
 */
public class InnerCommand extends BaseCommand {

    public InnerCommand() {
        //选中之后,默认的预加载操作,可能会被弃用吧.
        //做预加载用的
        addCmd("preload.index", (pkgName, cmd, data) -> {
            return new byte[0];
        });
    }
}
