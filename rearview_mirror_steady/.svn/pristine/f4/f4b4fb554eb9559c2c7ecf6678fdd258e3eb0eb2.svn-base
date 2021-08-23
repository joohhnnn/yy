package com.txznet.music.service.impl;

import com.txznet.music.action.PlayerActionCreator;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.report.entity.SysOpenEvent;
import com.txznet.music.util.Utils;
import com.txznet.rxflux.Operation;

/**
 * @author zackzhou
 * @date 2019/3/19,16:41
 */

public class RadioCommand extends BaseCommand {

    public RadioCommand() {
        addCmd("open.play", (pkgName, cmd, data) -> {
            Utils.back2HomeWithRadio();
            ReportEvent.reportEnter(SysOpenEvent.ENTRY_TYPE_VOICE);
            return new byte[0];
        });

        addCmd("play", (pkgName, cmd, data) -> {
            PlayerActionCreator.get().play(Operation.SOUND);
            ReportEvent.reportEnter(SysOpenEvent.ENTRY_TYPE_VOICE);
            return new byte[0];
        });
    }
}
