package com.txznet.launcher.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.txznet.comm.remote.ServiceHandlerBase;
import com.txznet.launcher.event.BroadcastingCentre;
import com.txznet.launcher.event.EventTypes;
import com.txznet.sdkinner.TXZServiceCommandDispatcher;
import com.txznet.txz.service.IService;

import java.util.Map;

public class TXZService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return new TXZServiceBinder();
    }


    public static class TXZServiceBinder extends IService.Stub {
        @Override
        public byte[] sendInvoke(final String packageName, final String command, final byte[] data) throws RemoteException {
            if (command.equals("sdk.init.success")) {
                BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_TXZ_INIT_SUCCESS);
            }
            byte[] ret = ServiceHandlerBase.preInvoke(packageName, command,
                    data);
            for (Map.Entry<String, TXZServiceCommandDispatcher.CommandProcessor> entry : TXZServiceCommandDispatcher.mProcessors
                    .entrySet()) {
                if (command.startsWith(entry.getKey())) {
                    if (entry.getValue() == null)
                        break;
                    return entry.getValue().process(packageName,
                            command.substring(entry.getKey().length()), data);
                }
            }
            return ret;
        }
    }
}
