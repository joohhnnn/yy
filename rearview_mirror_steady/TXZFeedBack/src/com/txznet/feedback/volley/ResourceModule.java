package com.txznet.feedback.volley;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.text.TextUtils;

import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.feedback.AppLogic;
import com.txznet.feedback.data.Message;
import com.txznet.feedback.service.MsgService;
import com.txznet.feedback.ui.FeedBackWin;
import com.txznet.feedback.ui.NoticePagerActivity;
import com.txznet.feedback.util.DeviceInfo;

public class ResourceModule {
	
	public static final String SD_FILE_PATH = "/txz/feedback/cache/record/";
	
	public static final String VOICE_UPLOAD_URL = "http://feedback.txzing.com/service/feedback/feedback.php";
	
	public static final String VOICE_DOWNLOAD_URL = "http://feedback.txzing.com/service/feedback/get_file.php";
	
	public static final String GET_FEEDBACK_RESULT = "http://feedback.txzing.com/service/feedback/result.php";
	
    private static ResourceModule sInstance = new ResourceModule();

    protected ResourceModule() {

    }

    public static ResourceModule getInstance() {
        return sInstance;
    }

    public static abstract class DownloadCallback {
        protected Object mData;

        public void setCustomData(String d) {
            mData = d;
        }

        public abstract void onError();

        public abstract void onSuccess(String id, String filePath);

        // 已经有相同任务存在时
        public abstract void onDuplicate();
    }
    
    public static abstract class UploadCallback{
    	
    	public abstract void onError();
    	
    	public abstract void onSuccess();
    	
    }

    Set<String> mResourceTask = new HashSet<String>();

//    /**
//     * 下载指定地址数据，按id文件名保存，如果id的缓存已存在，则由force决定是否下载
//     * @param addr
//     * @param id
//     * @param force
//     * @param cb
//     * @return
//     */
//    public String downloadResource(final String addr, final String id, boolean force, final DownloadCallback cb) {
//        final String filePath = getFileCachePathById(id);
//        final File f = new File(filePath);
//        if (f.exists()) {
//            if (force) {
//                f.delete();
//            } else {
//                if (cb != null) {
//                    cb.onSuccess(id, filePath);
//                    return filePath;
//                }
//            }
//        }
//
//        synchronized (mResourceTask) {
//            if (mResourceTask.contains(addr)) {
//                MyApplication.getApp().runOnBackGround(new Runnable() {
//                    @Override
//                    public void run() {
//                    	cb.onDuplicate();
//                    }
//                }, 0);
//                return filePath;
//            }
//            mResourceTask.add(addr);
//        }
//
//        RawRequest rawRequest = new RawRequest(addr, new Response.Listener<byte[]>() {
//            @Override
//            public void onResponse(byte[] data) {
//                synchronized (mResourceTask) {
//                    mResourceTask.remove(addr);
//                }
//                try {
//                    f.createNewFile();
//                    FileOutputStream out = new FileOutputStream(f); 
 //                    out.write(data);
//                    out.close();
//                    if (cb != null)
//                        cb.onSuccess(id, filePath);
//                } catch (Exception e) {
//                    if (cb != null)
//                        cb.onError();
//                }
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError volleyError) {
//                LogUtil.loge("down load resource failed: " + addr);
//                synchronized (mResourceTask) {
//                    mResourceTask.remove(addr);
//                }
//                if (cb != null)
//                    cb.onError();
//            }
//        });
//        RequestManager.getInstance().addRequest(rawRequest);
//        return filePath;
//    }
    
    /**
     * 下载某个指定的音频
     * @param filePathName
     * @param voiceId
     * @param dc
     */
    public void downloadVoice(final String filePathName, final String voiceId, final DownloadCallback dc){
    	synchronized (mResourceTask) {
            if (mResourceTask.contains(filePathName)) {
                AppLogic.runOnBackGround(new Runnable() {
                	
                    @Override
                    public void run() {
                    	dc.onDuplicate();
                    }
                }, 0);
                
                return;
            }
            
            mResourceTask.add(filePathName);
        }
    	
    	StringBuilder sb = new StringBuilder();
    	sb.append(VOICE_DOWNLOAD_URL).append("?").append(getBaseRequest()).append("&voiceId=").append(voiceId);
    	
    	RawRequest rawRequest = new RawRequest(sb.toString(), new Response.Listener<byte[]>() {
            @Override
            public void onResponse(byte[] data) {
                synchronized (mResourceTask) {
                    mResourceTask.remove(filePathName);
                }
                
                if(data == null || data.length == 0){
                	dc.onError();
                	return;
                }
                
                try {
                	File f = new File(filePathName);
                	if(f.exists()){
                		f.delete();
                	}
                	
                	f.getParentFile().mkdirs();
                    f.createNewFile();
                    FileOutputStream out = new FileOutputStream(f);
                    out.write(data);
                    out.close();
                    if (dc != null)
                        dc.onSuccess(voiceId, filePathName);
                } catch (Exception e) {
                    if (dc != null)
                        dc.onError();
                    LogUtil.loge(e.toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                LogUtil.loge("down load resource failed: " + filePathName);
                synchronized (mResourceTask) {
                    mResourceTask.remove(filePathName);
                }
                if (dc != null)
                    dc.onError();
            }
        });
    	
        RequestManager.getInstance().addRequest(rawRequest);
    }
    
    /**
     * 获取所有的官方消息
     */
    public void executeGetNetMessage(){
    	try {
			StringBuilder sb = new StringBuilder();
			sb.append(GET_FEEDBACK_RESULT).append("?").append(getBaseRequest());
			
			JsonArrayRequest jar = new JsonArrayRequest(sb.toString(), new Listener<JSONArray>() {

				@Override
				public void onResponse(JSONArray arg0) {
					for(int i = 0;i<arg0.length();i++){
						try {
							JSONObject jo = (JSONObject) arg0.get(i);
							Message msg = Message.parseJsonObject(jo);
							MsgService.getInstance().addMessage(msg);
						} catch (JSONException e) {
							LogUtil.logi("onResponse JSONObject:"+arg0.toString());
						}
					}
					NoticePagerActivity.sendMsgBroadCast();
					FeedBackWin.getInstance().setShowRedPoint(MsgService.getInstance().isNewIn());
				}
			}, new ErrorListener() {

				@Override
				public void onErrorResponse(VolleyError arg0) {
					
				}
			});
			
			RequestManager.getInstance().addRequest(jar);
		} catch (Exception e) {
			 LogUtil.loge(e.toString());
		}
    }
    
    public int getRequestIndex(){
    	return 0;
    }
    
    /**
     * 上传音频到服务器
     * @param filePath
     */
    public void uploadVoice(String filePath,long id,UploadCallback ucb){
    	File file = new File(filePath);
    	if(!file.exists()){
    		return;
    	}
    	
    	try {
			MultipartEntity fileMultipartEntity = new MultipartEntity();
			fileMultipartEntity.addPart("imei", new StringBody(DeviceInfo.getIMEI(), Charset.forName("utf-8")));
			fileMultipartEntity.addPart("cpu_serial", new StringBody(DeviceInfo.getCPUSerialNumber(), Charset.forName("utf-8")));
			fileMultipartEntity.addPart("wifi_mac_addr", new StringBody(DeviceInfo.getWifiMacAddress(), Charset.forName("utf-8")));
			if(!TextUtils.isEmpty(DeviceInfo.getBluetoothMacAddress())){
				fileMultipartEntity.addPart("bluetooth_mac_addr", new StringBody(DeviceInfo.getBluetoothMacAddress(), Charset.forName("utf-8")));
			}
			
			fileMultipartEntity.addPart("build_serial", new StringBody(DeviceInfo.getBuildSerialNumber(), Charset.forName("utf-8")));
			fileMultipartEntity.addPart("android_id", new StringBody(DeviceInfo.getAndroidId(), Charset.forName("utf-8")));
			
			fileMultipartEntity.addPart("voiceId", new StringBody(String.valueOf(id), Charset.forName("utf-8")));
			fileMultipartEntity.addPart("voiceFile", new FileBody(file));
			
			boolean bsu = uploadFile(VOICE_UPLOAD_URL, fileMultipartEntity);
			if(ucb != null){
				if(bsu){
					ucb.onSuccess();
				}else {
					ucb.onError();
				}
			}
		} catch (UnsupportedEncodingException e) {
			LogUtil.loge(e.toString());
			if(ucb != null){
				ucb.onError();
			}
		}
    }
    
    public String getBaseRequest(){
    	StringBuilder sb = new StringBuilder();
    	sb.append("imei=").append(DeviceInfo.getIMEI()).append("&")
    	.append("cpu_serial=").append(DeviceInfo.getCPUSerialNumber()).append("&")
    	.append("wifi_mac_addr=").append(DeviceInfo.getWifiMacAddress()).append("&");
    	if(!TextUtils.isEmpty(DeviceInfo.getBluetoothMacAddress())){
    		sb.append("bluetooth_mac_addr=").append(DeviceInfo.getBluetoothMacAddress()).append("&");
		}
    	sb.append("build_serial=").append(DeviceInfo.getBuildSerialNumber()).append("&")
    	.append("android_id=").append(DeviceInfo.getAndroidId());
    	return sb.toString();
    }

    public String getFileCachePathById(String id) {
        File f = new File(Environment.getExternalStorageDirectory().getPath() + SD_FILE_PATH + id);
        f.getParentFile().mkdirs();
        return f.getAbsolutePath();
    }
	
	/**
	 * 上传文件
	 * @param url
	 * @param fileMultipartEntity
	 * @return
	 */
	public static boolean uploadFile(String url,MultipartEntity fileMultipartEntity){
        boolean success=false;
        HttpClient httpClient=newInstance();
        HttpPost postMethod=new HttpPost(url);
        try {
            postMethod.setEntity(fileMultipartEntity);
            HttpResponse response = httpClient.execute(postMethod);
            int statusCode = response.getStatusLine().getStatusCode();
            if(statusCode==HttpStatus.SC_OK||statusCode==HttpStatus.SC_NO_CONTENT){
               success=true;
            }else{
            	
            }
        } catch (UnsupportedEncodingException e) {
        	LogUtil.loge(e.toString());
        	postMethod.abort();
        } catch (IOException e) {
        	LogUtil.loge(e.toString());
        	postMethod.abort();
        } finally{
        	closeHttpClient(httpClient);
        }
        return success;
    }
	
	public static HttpClient newInstance(){
    	final HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, "UTF-8");

        HttpConnectionParams.setStaleCheckingEnabled(params, false);
        HttpConnectionParams.setConnectionTimeout(params, 30000);
        HttpConnectionParams.setSoTimeout(params, 30000);
        HttpConnectionParams.setSocketBufferSize(params, 8192);

        HttpClientParams.setRedirecting(params, true);

        HttpProtocolParams.setUserAgent(params, System.getProperty("http.agent"));
        
        setDefaultProxy(params);

        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));

        ClientConnectionManager manager = new ThreadSafeClientConnManager(params, schemeRegistry);
        HttpClient httpClient = new DefaultHttpClient(manager, params);
        return httpClient;
    }
	
	public static void closeHttpClient(HttpClient client){
    	if(client!=null){
    		client.getConnectionManager().shutdown();
    	}
    }
	
	private static void setDefaultProxy(HttpParams params){
        final Context context= AppLogic.getApp();
        if(context==null){
            return;
        }
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            Uri uri = Uri.parse("content://telephony/carriers/preferapn");
            Cursor c=null;
            try {
                c = context.getContentResolver().query(uri,null, null, null, null);
                if (c != null && c.moveToFirst()) {
                    String apnName=c.getString(c.getColumnIndex("apn"));
                    if("cmwap".equalsIgnoreCase(apnName)||"ctwap".equalsIgnoreCase(apnName)||"3gwap".equals(apnName)){
                        String proxyStr = c.getString(c.getColumnIndex("proxy"));
                        if (proxyStr != null && proxyStr.trim().length() > 0) {
                            HttpHost proxy = new HttpHost(proxyStr, 80);
                            params.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
                        }
                    }
                }
            } catch (Exception e) {
            	LogUtil.loge(e.toString());
			} finally {
                if(c!=null&&!c.isClosed()){
                    c.close();
                }
            }
        }
    }
}
