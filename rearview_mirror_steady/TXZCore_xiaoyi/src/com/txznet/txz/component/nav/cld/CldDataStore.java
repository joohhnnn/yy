package com.txznet.txz.component.nav.cld;

public class CldDataStore {
	/**
	 * 0:直行 1:右前方 2:向右 3:右后方 4:调头 5:向左 6:左前方
	 */
	public Long lDirection; // 转弯方向
	
	public Long lDistance; // 距离诱导点的距离，单位m(有路径)
	public Long lRemainDistance; // 距离目的地的距离，单位m
	public Long lTotalDistance; // 出发地与目的地之间的总距离，单位m
	public Long lRemainTime; // 距离目的地的剩余时间
	public Long lTotalTime; // 出发地与目的地之间的总时间
	public String szCurrentRoadName; // 当前道路名字
	public String szNextRoadName; // 下一道路名字
	public Long lCurrentRoadType; // 当前道路类型
	public Long lCurrentSpeed; // 当前车速，单位km/h
	public Long lCurrentLimitedSpeed;// 当前限制车速，单位km/h
	public Long lCurrentGPSAngle; // 当前GPS角度
	public Long lExitIndexRoads; // 环岛出口序号
	public Long lNumOfOutRoads; // 环岛出口数
	public Long lReserve; // 保留 // 值为-2时，说明导航退出

	private static CldDataStore sStore = new CldDataStore();

	private CldDataStore() {

	}

	public static CldDataStore getInstance() {
		return sStore;
	}

	public void reset() {
		lDirection = null;
		lDistance = null;
		lRemainDistance = null;
		lTotalDistance = null;
		lRemainTime = null;
		lTotalTime = null;
		szCurrentRoadName = null;
		szNextRoadName = null;
		lCurrentRoadType = null;
		lCurrentSpeed = null;
		lCurrentLimitedSpeed = null;
		lCurrentGPSAngle = null;
		lExitIndexRoads = null;
		lNumOfOutRoads = null;
		lReserve = null;
	}
}
