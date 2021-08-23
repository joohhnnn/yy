package com.txznet.txz.module.dns;

import org.json.JSONArray;
import org.json.JSONObject;

import com.txz.ui.domain.UiDomain;
import com.txz.ui.event.UiEvent;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.IModule;

public class DnsManager extends IModule{
	private final static String[] DEFAULT_DOMAINS = {
			"scv2.hivoice.cn",
			"scs.openspeech.cn",
			"restapi.amap.com",
			"api.dianping.com",
			"api.map.baidu.com",
			"api.tongxingzhe.leting.io",
			"kaolafm.net",
			"kaolafm.cn",
			"kaolafm.com",
			"kaolating.com",
			"audiobuy.cc",
			"itings.com",
			"ws.stream.fm.qq.com",
			"base.music.qq.com",
			"antiserver.kuwo.cn",
			"apilocate.amap.com",
			"m5.amap.com",
			"cgicol.amap.com",
			"offline.aps.amap.com",
			"socol.amap.com",
			"restapi.amap.com",
			"cmg.amap.com",
			"sns.amap.com",
			"ts.amap.com",
			"mps.amap.com",
			"oss.amap.com",
			"page.amap.com",
			"offlinedata.alicdn.com",
			"asrv3.hivoice.cn"
	};
	
	private static DnsManager sIntance = new DnsManager();
	
	public static DnsManager getInstance(){
		return sIntance;
	}
	
	private DnsManager(){
		
	}
	
	@Override
	public int initialize_AfterStartJni() {
		AppLogic.runOnBackGround(new Runnable() {
			@Override
			public void run() {
				LogUtil.logd("report default domains");
				parseDomains(DEFAULT_DOMAINS);
			}
		}, 5*60*1000);//初始化五分钟后上报默认域名信息
		return super.initialize_AfterStartJni();
	}
	
	/*
	 * {"domains":["www.baidu.com", "www.google.com"]}
	 * {"domains":[  {"domain":"www.baidu.com", "ip": ["111", "222"]}, 
	 *                    {"domain":"www.baidu.com", "ip": ["333", "444"]}
	 *                  ]
	 * }
	 */
	private void parseDomains(String[] domains){
		if (domains == null || domains.length == 0){
			return;
		}
		
		UiDomain.Req_ParseDomains pbParseDomains = new UiDomain.Req_ParseDomains();
		pbParseDomains.rptDomains = new com.txz.ui.domain.UiDomain.Domain[domains.length];
		for (int i = 0; i < domains.length; ++i){
			pbParseDomains.rptDomains[i] = new UiDomain.Domain();
			pbParseDomains.rptDomains[i].strDomain = domains[i].getBytes();
		}
		JNIHelper.sendEvent(UiEvent.EVENT_DOMAIN, UiDomain.SUBEVENT_DOMAIN_PARSE, pbParseDomains);
	}
	
	public byte[] invokeTXZDns(final String packageName, String command, byte[] data){
		if ("parse".equals(command)){
			JSONBuilder builder = new JSONBuilder(data);
			String[] domains = builder.getVal("domains", String[].class);
			if (domains == null || domains.length == 0){
				return null;
			}
			
			UiDomain.Req_ParseDomains pbParseDomains = new UiDomain.Req_ParseDomains();
			pbParseDomains.rptDomains = new com.txz.ui.domain.UiDomain.Domain[domains.length];
			for (int i = 0; i < domains.length; ++i){
				pbParseDomains.rptDomains[i] = new UiDomain.Domain();
				pbParseDomains.rptDomains[i].strDomain = domains[i].getBytes();
			}
			
			JNIHelper.sendEvent(UiEvent.EVENT_DOMAIN, UiDomain.SUBEVENT_DOMAIN_PARSE, pbParseDomains);
		}else if ("report".equals(command)){
			try{
				JSONObject json = new JSONObject(new String(data));
				JSONArray jsonArray = json.getJSONArray("domains");
				UiDomain.Req_ParseDomains pbParseDomains = new UiDomain.Req_ParseDomains();
				pbParseDomains.rptDomains = new com.txz.ui.domain.UiDomain.Domain[ jsonArray.length()];
				for (int i = 0; i < jsonArray.length(); ++i){
					pbParseDomains.rptDomains[i] = new UiDomain.Domain();
					JSONObject o = jsonArray.getJSONObject(i);
					pbParseDomains.rptDomains[i].strDomain = o.getString("domain").getBytes();
					JSONArray ip = o.getJSONArray("ip");
					pbParseDomains.rptDomains[i].rptStrIp = new byte[ip.length()][];
					for (int j = 0; j < ip.length(); ++j){
						pbParseDomains.rptDomains[i].rptStrIp[j] = ((String)ip.get(j)).getBytes();
					}
				}
				
				JNIHelper.sendEvent(UiEvent.EVENT_DOMAIN, UiDomain.SUBEVENT_DOMAIN_REPORT, pbParseDomains);
			}catch(Exception e){
				LogUtil.loge("invokeDns exception : " + e.toString());
			}
			
		}
		return null;
	}
	
}
