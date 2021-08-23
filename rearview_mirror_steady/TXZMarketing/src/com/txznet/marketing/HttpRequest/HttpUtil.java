package com.txznet.marketing.HttpRequest;

import android.util.Log;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Create by JackPan on 2019/01/17.
 */
public class HttpUtil {

    private static final String TAG = "HttpUtil";

    private static HttpUtil mInstance = new HttpUtil();
    private HttpUtil(){}
    public static HttpUtil getmInstance(){
        return mInstance;
    }

    //请求的url
    private String postUrl = "https://wx.txzing.com/module/device/service/SelfMarketing";

    private String uid;
    private String appName = "selfmarketing";
    private String name = "tongxingzhe";

    /**
     *获得HttpURLConnection对象
     * @return HttpURLConnection
     */
    private HttpURLConnection getConnnect(String url,String method){
        try {
            URL mUrl = new URL(url);
            try {
                HttpURLConnection connection = (HttpURLConnection) mUrl.openConnection();
                //设置请求类型
                connection.setRequestMethod(method);
                //设置连接超时时间
                connection.setConnectTimeout(3000);
                //设置读取超时时间
                connection.setReadTimeout(3000);
                // 设置是否向 httpUrlConnection 输出，
                // 对于post请求，参数要放在 http 正文内，因此需要设为true。
                // 默认情况下是false;
                connection.setDoOutput(true);
                // 设置是否从 httpUrlConnection 读入，默认情况下是true;
                connection.setDoInput(true);

                return connection;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    //网络请求的入口
    public void sendRequset(final HttpCallbackListener listener){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = getConnnect(postUrl,"POST");
                BufferedWriter writer = null;
                BufferedReader reader = null;
                try {
                    connection.connect();
                    //利用 getOutputStream() 传输 POST 消息
                    writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
                    writer.write(getRequestJSON());
                    writer.flush();
                    writer.close();

                    //利用 getInputStream() 获得返回的数据
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null){
                        response.append(line);
                    }
                    if (listener != null){
                        //回调onFinish方法
                        listener.onFinish(parseResponseJSON(response.toString()));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    if (listener != null){
                        //回调onError方法
                        listener.onError(e);
                    }
                } finally {
                    //断开连接
                    if (connection != null){
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

    //请求发送的数据
    private String getRequestJSON(){
        String jsonResult = "";    //返回的字符串
        try {
            JSONObject jObject = new JSONObject();
            jObject.put("name",appName);
            jObject.put("sign",md5());
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(jObject);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("uid",uid);
            jsonObject.put("service",jsonArray);
            jsonResult = jsonObject.toString();
            Log.d(TAG, "uid: "+uid);
            Log.d(TAG, "getRequestJSON: "+jsonResult);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonResult;
    }

    //处理获得的数据
    private boolean parseResponseJSON(String response){
        Log.d(TAG, "parseResponseJSON: "+response);
        boolean isCanUse = false;
        try {
            JSONObject jsonObject = new JSONObject(response);
            int err = jsonObject.getInt("err");
            JSONObject content = jsonObject.getJSONObject("content");
            String uID = content.getString("uid");
            JSONArray jsonArray = content.getJSONArray("service");
            JSONObject jObject = jsonArray.getJSONObject(0);
            String name = jObject.getString("name");
            int use = jObject.getInt("use");

            isCanUse = use == 1;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return isCanUse;
    }

    //http请求回调
    public interface HttpCallbackListener{
        void onFinish(Boolean isCanUse);
        void onError(Exception e);
    }

    //获取当前的设备号
    private String getUid(){
        String filePath = "txz/uid.dat";
        uid =  FileUtil.getFileContent(filePath);
        Log.d(TAG, "getUid: "+uid);
        return uid;
    }

    /**
     * uid + appName生成md5_1
     * md5_1 + tongxingzhe生成md5_2
     * 输出md5_2
     * @return
     */
    //根据uid生成MD5码
    private String md5(){
        getUid();
        String str = "";
        try {
            MessageDigest md5_1 = MessageDigest.getInstance("MD5");
            byte[] bytes1 = md5_1.digest((uid+appName).getBytes());
            String result1 = "";
            for (byte b : bytes1) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result1 += temp;
            }
            Log.d(TAG, "md5_1: "+result1);

            MessageDigest md5_2 = MessageDigest.getInstance("MD5");
            byte[] bytes2 = md5_2.digest((result1+name).getBytes());
            String result2 = "";
            for (byte b : bytes2) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result2 += temp;
            }
            str = result2;
            Log.d(TAG, "md5_2: "+result2);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return str;
    }


}
