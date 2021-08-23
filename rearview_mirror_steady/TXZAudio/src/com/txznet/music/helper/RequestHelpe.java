package com.txznet.music.helper;

import java.util.HashMap;
import java.util.Map;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.music.Constant;
import com.txznet.music.bean.req.ReqAlbumAudio;
import com.txznet.music.bean.req.ReqCategory;
import com.txznet.music.bean.req.ReqCheck;
import com.txznet.music.bean.req.ReqSearchAlbum;
import com.txznet.music.bean.response.Album;
import com.txznet.music.utils.NetHelp;
import com.txznet.music.utils.ToastUtils;

public class RequestHelpe {

	private static final String TAG = "[MUSIC][REQUEST] ";
	public static String currentAlbum = "";
	
	public static Map<Integer,TempAlbum> reqLine =new HashMap<Integer, RequestHelpe.TempAlbum>();
	
	// 请求的路径
//		public final static String GET_CATEGORY = "/category/get";// 首页获取分类数据
//		public final static String GET_SEARCH_LIST = "/album/list";// 从分类进入歌单
//		public final static String GET_ALBUM_AUDIO = "/album/audio";// 根据歌单获取歌曲数据
//		public final static String GET_SEARCH = "/text/search";// 搜索歌曲
//		public final static String GET_TAG = "/conf/check";// 获取版本号
//		public final static String GET_REPORT = "/report/report";// 上报数据给服务器
//		public final static String GET_PROCESSING = "/text/preprocessing";// 上报数据给服务器
//		public final static String GET_STATS = "/report/statistic";// 上报统计数据给服务器
//		public final static String GET_FAKE_SEARCH = "/text/fake_request";// 假请求
//		public final static String GET_REPORT_ERROR = "/report/abnormal";// 上报错误数据
	

	/**
	 * 发送请求所有分类
	 */
	public static void reqCategory() {
		ReqCategory category = new ReqCategory();
		category.setbAll(1);
		NetHelp.sendRequest(Constant.GET_CATEGORY, category);
	}

	/**
	 * 请求专辑
	 */
	public static void reqAlbum(int categoryId, int pageOff) {
		ReqSearchAlbum album = new ReqSearchAlbum();
		album.setPageId(pageOff);
		album.setCategoryId(categoryId);// 500000
		NetHelp.sendRequest(Constant.GET_SEARCH_LIST, album);
	}

	/**
	 * 请求音频
	 */
	public static void reqAudio(Album album, long categoryID) {
		reqAudio(album.getId(), album.getSid(),1, album.getName(), categoryID);
	}
	/**
	 * 
	 * @param id 专辑ID
	 * @param sid 专辑源id
	 * @param pageID 页码
	 * @param albumName 专辑名称
	 * @param categoryID 如果是集合arrCategorys 则取第一个
	 */
	public static void reqAudio(long id,int sid,int pageID,String albumName, long categoryID) {
		LogUtil.logd(TAG+"[request] "+Constant.GET_ALBUM_AUDIO+",albumName "+albumName+",categoryID "+categoryID);
		currentAlbum = albumName;
		ReqAlbumAudio reqData = new ReqAlbumAudio();
		reqData.setSid(sid);
		reqData.setId(id);
		reqData.setPageId(pageID);
		reqData.setCategoryId(categoryID);
		reqData.setOffset(Constant.PAGECOUNT);
		int requestID=Constant.ManualSessionID = NetHelp.sendRequest(Constant.GET_ALBUM_AUDIO, reqData);
		if (0==requestID) {//当前无网络
			AppLogic.runOnUiGround(new Runnable() {
				
				@Override
				public void run() {
				ToastUtils.showShort(Constant.RS_VOICE_SPEAK_NONE_NET);	
				}
			}, 0);
		}else{
			reqLine.put(requestID,new TempAlbum(id, sid, albumName, categoryID));
		}
	}
	
	public  static class TempAlbum{
		long id;
		int sid;
		String albumName;
		long categoryID;
		public TempAlbum(long id, int sid, String albumName, long categoryID) {
			super();
			this.id = id;
			this.sid = sid;
			this.albumName = albumName;
			this.categoryID = categoryID;
		}
		@Override
		public String toString() {
			return "TempAlbum [id=" + id + ", sid=" + sid + ", albumName="
					+ albumName + ", categoryID=" + categoryID + "]";
		}
		
	}

	public static void reqTag() {
		ReqCheck check = new ReqCheck();
		check.setLogoTag(0);
		NetHelp.sendRequest(Constant.GET_TAG, check);
	}
}
