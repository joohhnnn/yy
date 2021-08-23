package com.txznet.music.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.txz.ui.map.UiMap.LocationInfo;
import com.txznet.comm.base.CrashCommonHandler;
import com.txznet.comm.remote.ServiceHandler;
import com.txznet.sdk.TXZLocationManager;
import com.txznet.sdk.bean.LocationData;
import com.txznet.txz.service.IService;

/**
 * 电台之家专用 同行者考拉3.0
 *
 * @author telenewbie
 */
public abstract class CoreService extends Service {
    private static final String TAG = "music:service:";
    private static IEngineCallBack callback;
    private static final int NORMAL = 1;

    private byte[] _sendInvoke(final String packageName, final String command, final byte[] data) {
        byte[] ret = ServiceHandler.preInvoke(packageName, command, data);

        if (ret != null && ret.length > 0) {
            return ret;
        }

//		LogUtil.logd(TAG + "receive " + packageName + " command " + command + ",callback=" + callback);
        if (callback == null) {
            return ret;
        }
        if (command.equals("music.client.sleep")) {
            return callback.deviceSleep();
        }
        if (command.equals("music.client.wakeup")) {
            return callback.deviceWakeUp();
        }
        if (command.equals("music.client.exit")) {
            return callback.clientExit();
        }
        if (command.equals("client.enter_reverse")) {
            return callback.clientBackCarOn();
        }
        if (command.equals("client.quit_reverse")) {
            return callback.clientBackCarOff();
        }
        if (command.equals("music.client.enter_reverse")) {
            return callback.clientBackCarOn();
        }
        if (command.equals("music.client.quit_reverse")) {
            return callback.clientBackCarOff();
        }
        if (command.startsWith("music.remote.callback.")) {

        } else if (command.startsWith("music.")) {
            return callback.invokeMusic(packageName, command.substring("music.".length()), data);
        } else if (command.equals("sdk.init.success")) {
            return callback.soundInitSuccess();
        } else if (command.startsWith("audio.")) {
            return callback.invokeAudio(packageName, command.substring("audio.".length()), data);
        } else if (command.startsWith("tool.loc.updateLoc")) {
            if (data == null) {
                callback.onLocationUpdate(null);
            } else {
                try {
                    LocationInfo locationInfo = LocationInfo.parseFrom(data);
                    LocationData locationData = TXZLocationManager.getInstance().convertLocationData(locationInfo);
                    callback.onLocationUpdate(locationData);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            return callback.onOtherCmd(packageName, command, data);
        }
        return ret;
    }

    public class SampleBinder extends IService.Stub {

        @Override
        public byte[] sendInvoke(final String packageName, final String command, final byte[] data)
                throws RemoteException {
            try {
                return _sendInvoke(packageName, command, data);
            } catch (Exception e) {
                CrashCommonHandler.getInstance().uncaughtException(Thread.currentThread(), e);
            }
            return null;
        }
    }

    @Override
    public final IBinder onBind(Intent intent) {
        callback = getCallback();
        return new SampleBinder();
    }

    public abstract IEngineCallBack getCallback();

}
