package com.txznet.txz.component.media.loader;

import com.txznet.txz.component.media.util.FileReader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * 媒体工具适配装载信息
 *
 * 包含媒体工具适配dex包相关信息及校验参数, 由dex包同路径下同名的chk文件解析
 *
 * chk文件内容示例:
 * {"entry":"com.txznet.module.txz_moudle_audio_xmly.AudioXmly",
 * "chk":"8866963efd1bced0d1c32e4177c3c80a","priority":10,"type":"AUDIO","version":1,
 * "target":"com.ximalaya.ting.android.car","targetVersion":"2.0.2"}
 *
 * Created by J on 2019/3/12.
 */
public class MediaToolLoadInfo {
    public int version;
    public String dexPath;
    public String chkPath;

    public String targetPackageName;
    public String targetVersionName;
    public String targetVersionMin;
    public String targetVersionMax;
    public String targetJarName;
    public String type;
    public int priority;
    public String entryClass;
    public String checkMd5;

    public MediaToolLoadInfo(File dexFile, File chkFile) throws JSONException {
        dexPath = dexFile.getAbsolutePath();
        chkPath = chkFile.getAbsolutePath();
        // 读入chk文件内容
        String chkContent = FileReader.readFile(chkFile.getPath());
        resolveJson(chkContent);
    }

    public MediaToolLoadInfo(String json, String targetJarName) throws JSONException {
        resolveJson(json);
        this.targetJarName = targetJarName;
    }

    private void resolveJson(String json) throws JSONException {
        JSONObject jObj = new JSONObject(json);
        version = jObj.getInt("version");
        targetPackageName = jObj.getString("target");
        targetVersionName = jObj.getString("targetVersion");
        targetVersionMin = jObj.getString("targetVersionMin");
        targetVersionMax = jObj.getString("targetVersionMax");
        type = jObj.getString("type");
        priority = jObj.getInt("priority");
        entryClass = jObj.getString("entry");
        checkMd5 = jObj.getString("chk");
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
