package com.txznet.txz.module.team;

import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.event.UiEvent;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.txz.module.IModule;

public class TeamManager extends IModule {
	static TeamManager sModuleInstance = null;

	private TeamManager() {

	}

	public static TeamManager getInstance() {
		if (sModuleInstance == null) {
			synchronized (TeamManager.class) {
				if (sModuleInstance == null)
					sModuleInstance = new TeamManager();
			}
		}
		return sModuleInstance;
	}

	// /////////////////////////////////////////////////////////////////////////

	@Override
	public int initialize_BeforeStartJni() {
		// 注册需要处理的事件
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
				UiEquipment.SUBEVENT_RESP_GET_BIND_CAR_TEAM_URL);
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
				UiEquipment.SUBEVENT_NOTIFY_CAR_TEAM);
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
		case UiEvent.EVENT_ACTION_EQUIPMENT:
			switch (subEventId) {
			case UiEquipment.SUBEVENT_RESP_GET_BIND_CAR_TEAM_URL: {
				try {
					UiEquipment.Resp_GetBindCarTeamUrl res = UiEquipment.Resp_GetBindCarTeamUrl
							.parseFrom(data);
					JSONBuilder builder = new JSONBuilder();
					builder.put("issuccess", res.bOk);
					if (!res.bOk) {
					} else {
						builder.put("qrcode", res.strBindWxUrl);
						builder.put("isbind", res.bIsBind);
						if (res.bIsBind) {
							builder.put("carinfo", res.strCarInfo);
						}
					}
					ServiceManager.getInstance().sendInvoke(
							ServiceManager.TEAM, "team.info.qrcode",
							builder.toString().getBytes(), null);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
			case UiEquipment.SUBEVENT_NOTIFY_CAR_TEAM: {
				try {
					UiEquipment.Notify_CarTeam res = UiEquipment.Notify_CarTeam
							.parseFrom(data);
					JSONBuilder builder = new JSONBuilder();

					builder.put("strCarInfo", res.strCarInfo);
					builder.put("strUrl", res.strUrl);
					builder.put("uint32Type", res.uint32Type);
					ServiceManager.getInstance().sendInvoke(
							ServiceManager.TEAM, "team.info.bind",
							builder.toString().getBytes(), null);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			default:
				break;
			}
		}
		return super.onEvent(eventId, subEventId, data);
	}
}
