package com.txznet.txz.module.remoteservice;

import com.txz.ui.event.UiEvent;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.txz.module.IModule;
import com.txznet.txz.util.PackageInstaller;

public class RemoteServiceManager extends IModule {
    static RemoteServiceManager sModuleInstance = new RemoteServiceManager();

    private RemoteServiceManager() {
        // 尝试在启动时绑定所有远程服务
        PackageInstaller.rebindService();
    }

    public static RemoteServiceManager getInstance() {
        return sModuleInstance;
    }

    // /////////////////////////////////////////////////////////////////////////

    @Override
    public int initialize_BeforeStartJni() {
        // 注册需要处理的事件
        regEvent(UiEvent.EVENT_SYSTEM_TXZ_REMOTE_SERVICE_INVOKE);
        return super.initialize_BeforeStartJni();
    }

    @Override
    public int initialize_AfterStartJni() {
        // 发送初始化需要触发的事件
        return super.initialize_AfterStartJni();
    }

    @Override
    public int onEvent(int eventId, int subEventId, byte[] data) {
        // 处理事件
        switch (eventId) {
            case UiEvent.EVENT_SYSTEM_TXZ_REMOTE_SERVICE_INVOKE: {
                try {
                    final UiEvent.RemoteInvokeInfo info = UiEvent.RemoteInvokeInfo
                            .parseFrom(data);

                    if (null == info.uint32Timeout) {
                        ServiceManager.getInstance().sendInvoke(info.strPackageName,
                                info.strCommand, info.strData, null);
                    } else {
                        ServiceManager.getInstance().sendInvoke(info.strPackageName,
                                info.strCommand, info.strData, null, info.uint32Timeout);
                    }
                } catch (Exception e) {
                }
                break;
            }
            default:
                break;
        }
        return super.onEvent(eventId, subEventId, data);
    }

}
