package com.txznet.music.bean.req;

import java.util.ArrayList;
import java.util.List;

import android.R.integer;

/**
 * 做数据统计
 * 
 * @author telenewbie
 * @version 创建时间：2016年3月28日 下午8:34:04
 * 
 */
public class ReqDataStats {

	public enum Action {
		PREVIOUS(1), NEXT(2), PLAY(3), PAUSE(4), PREVIOUS_SOUND(5), NEXT_SOUND(6), PLAY_SOUND(7), PAUSE_SOUND(8), /*   */
		NEXT_AUTO(9), /* 自动切换 */
		COMPLETE_AUTO(10), /* 播放结束 */
		FOUND_SOUND(11), /* 声控搜索 */
		INDEX_SOUND(12), /* 声控选择 */
		SHOW_LIST(13), /* 点击列表 */
		ACT_LOCAL(14), /* 本地音乐 */
		ACT_HISTORY(15), /* 历史列表 */
		ACT_ALBUM(16), /* 专辑 */
		ACT_LOGIN(17); /* 登录 */
		
		
		private int id;
		Action(int id){
			this.id=id;
		}
		public int getId() {
			return id;
		}
		
	}

	private List<ReportInfo> infos;

	private ReportInfo info;
	private long time;
	private int actionName;// 上/下/播放/暂停/声控/自动切换/结束

	public static class ReportInfo {
		private long id;// 音频ID
		private int sid;// 来源ID
		private long duration;// 时长
		private float currentPercent;// 当前播放进度
		private String artists;// 艺术家
		private String title;// 音频名称

		// public ReportInfo(long id, int sid, String atrits, String title) {
		// super();
		// this.id = id;
		// this.sid = sid;
		// this.atrits = atrits;
		// this.title = title;
		// }

		public ReportInfo(long id, int sid, long duration, float currentPercent) {
			super();
			this.id = id;
			this.sid = sid;
			this.duration = duration;
			this.currentPercent = currentPercent;
		}

		public void setAtrits(String atrits) {
			this.artists = atrits;
		}

		public ReportInfo(long id, int sid, long duration, float currentPercent, String artists, String title) {
			super();
			this.id = id;
			this.sid = sid;
			this.duration = duration;
			this.currentPercent = currentPercent;
			this.artists = artists;
			this.title = title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

	}

	public ReqDataStats(List<ReportInfo> infos, long time, Action actionName) {
		super();
		this.infos = infos;
		this.time = time;
		this.actionName = actionName.ordinal();
	}

	/**
	 * 行为操作上报BEAN
	 * 
	 * @param info
	 * @param time
	 * @param actionName
	 */
	public ReqDataStats(ReportInfo info, long time, Action actionName) {
		super();
		this.info = info;
		this.time = time;
		this.actionName = actionName.getId();
	}

	// /**
	// *
	// * @param time
	// * @param actionName
	// * @param id
	// * @param sid
	// */
	// public ReqDataStats(long time, Action actionName, long id, int sid) {
	// super();
	// this.time = time;
	// this.actionName = actionName;
	// infos = new ArrayList<ReqDataStats.ReportInfo>();
	// infos.add(new ReportInfo(id, sid));
	// }

}
