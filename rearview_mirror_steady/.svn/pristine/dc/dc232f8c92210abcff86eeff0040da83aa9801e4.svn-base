package com.txznet.txz.component.command;

import android.os.Environment;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.txz.util.FileUtil;
import com.txznet.txz.util.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;

public class CommandManager {
    private static Set<String> sAdapterCommandKeys;
    private static JSONObject sAdapterCommandJsonObject;
    /**
     * 是否已经加载了适配指令表文件
     */
    public static Boolean sHadLoadAdapterCommandFile = false;

    public static void updateCoreLocalCommandFile() {
        LogUtil.logd("[UPDATE]--updateCoreLocalCommandFile");
        File localCommandUpdatePath = new File(CORE_LOCAL_COMMAND_UPDATE_PATH);
        File localCommandBackupPath = new File(CORE_LOCAL_COMMAND_BACKUP_PATH);
        File localCommandLoadPath = new File(CORE_LOCAL_COMMAND_LOAD_PATH);
        updateCommandFile(localCommandUpdatePath, localCommandBackupPath, localCommandLoadPath);
    }

    public static String[] getCommands(String key) {
        if (sAdapterCommandKeys == null || sAdapterCommandKeys.size() <= 0 || key == null) {
            return null;
        }
        if (sAdapterCommandKeys.contains(key)) {
            JSONArray jsonArray = sAdapterCommandJsonObject.getJSONArray(key);
            String[] result = new String[jsonArray.size()];
            for (int i = 0; i < jsonArray.size(); i++) {
                result[i] = jsonArray.getString(i);
            }
            return result;
        }
        return null;
    }

    public static void loadAdapterCommandFile() {
        try {
            LogUtil.d("loadAdapterCommandFile "  + mAdapterLocalCommandLoadPath);
            sAdapterCommandJsonObject = JSONObject.parseObject(parseJsonFile(mAdapterLocalCommandLoadPath));
            if (sAdapterCommandJsonObject != null) {
                sAdapterCommandKeys = sAdapterCommandJsonObject.keySet();
            }
            sHadLoadAdapterCommandFile = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean checkCommandFile(String path) {
        try {
            JSONObject.parseObject(parseJsonFile(path));
            return true;
        } catch (Exception e) {
            LogUtil.d("bad format file " + path);
            e.printStackTrace();
            return false;
        }
    }

    private static String parseJsonFile(String jsonPath) {
        if (TextUtils.isEmpty(jsonPath)) {
            LogUtil.e("command file doesn't exist" + jsonPath);
            return "";
        }
        File jsonFile = new File(jsonPath);
        if (!jsonFile.exists()) {
            LogUtil.d("command file doesn't exist" + jsonPath);
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader srcReader = null;
        try {
            srcReader = new BufferedReader(new FileReader(jsonFile));
            String line = null;
            while ((line = srcReader.readLine()) != null) {
                line = line.trim();
                if ((line.length() >= 1 && line.charAt(0) == '#') || (line.length() >= 2 && line.startsWith("//"))){
                    stringBuilder.append("\n"); //增加空行，报错可以定位
                } else{
                    stringBuilder.append(line).append("\n");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (srcReader != null)
                    srcReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return  stringBuilder.toString();
    }

    public static void updateAdapterLocalCommandFile() {
        LogUtil.logd("[UPDATE]--updateAdapterLocalCommandFile");
        if (mAdapterLocalCommandBackupPath == null || mAdapterLocalCommandLoadPath == null) {
            LogUtil.d("null exception" + mAdapterLocalCommandLoadPath + mAdapterLocalCommandBackupPath);
            return;
        }
        File adapterLocalCommandUpdatePath = new File(ADAPTER_LOCAL_COMMAND_UPDATE_PATH);
        File adapterLocalCommandBackupPath = new File(mAdapterLocalCommandBackupPath);
        File adapterLocalCommandLoadPath = new File(mAdapterLocalCommandLoadPath);
        updateCommandFile(adapterLocalCommandUpdatePath, adapterLocalCommandBackupPath, adapterLocalCommandLoadPath);
    }

    private static void updateCommandFile(File updateFile, File backupFile, File loadFile) {
        int compareUpdate = compareCommandFileVersion(updateFile, loadFile);
        if (compareUpdate > 0) {
            LogUtil.logd("[UPDATE]--update command file: compareUpdate=" + compareUpdate);
            if (checkCommandFile(updateFile.getAbsolutePath())) {
                FileUtils.copyFile(updateFile, loadFile);
            }
        }
        int compareBackup = compareCommandFileVersion(backupFile, loadFile);
        if (compareBackup > 0) {
            LogUtil.logd("[UPDATE]--update command file: compareBackup=" + compareBackup);
            if (checkCommandFile(backupFile.getAbsolutePath())) {
                FileUtils.copyFile(backupFile, loadFile);
            } else {
                FileUtils.delete(backupFile);
            }
        } else if (compareBackup < 0) {
            LogUtil.logd("[UPDATE]--backup command file: compareBackup=" + compareBackup);
            FileUtils.copyFile(loadFile, backupFile);
            if(compareUpdate > 0) {
                ReportUtil.doReport(new ReportUtil.Report.Builder().setType("update_command_file")
                        .putExtra("time", System.currentTimeMillis())
                        .putExtra("file", CORE_LOCAL_COMMAND_LOAD_PATH.equals(loadFile.getAbsolutePath()) ? "core": "adapter").setSessionId().buildCommReport());
            }
        }
    }


    public static final String CORE_LOCAL_COMMAND_UPDATE_PATH = GlobalContext.get().getApplicationInfo().dataDir + "/res_txz_cmd_keywords.json";
    public static final String CORE_LOCAL_COMMAND_BACKUP_PATH = GlobalContext.get().getApplicationInfo().dataDir + "/cmd/backup/res_txz_cmd_keywords.json";
    public static final String CORE_LOCAL_COMMAND_LOAD_PATH = GlobalContext.get().getApplicationInfo().dataDir + "/data/res_txz_cmd_keywords.json";

    public static final String ADAPTER_LOCAL_COMMAND_UPDATE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/txz/res_txz_adapter_cmd_keywords.json";
    private static String mAdapterLocalCommandBackupPath;
    private static String mAdapterLocalCommandLoadPath;

    public static String getAdapterLocalCommandBackupPath() {
        return mAdapterLocalCommandBackupPath;
    }

    public static void setAdapterLocalCommandBackupPath(String adapterLocalCommandBackupPath) {
        if (TextUtils.isEmpty(adapterLocalCommandBackupPath)) {
            mAdapterLocalCommandBackupPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/txz/cmd/backup/res_txz_adapter_cmd_keywords.json";
        } else {
            mAdapterLocalCommandBackupPath = adapterLocalCommandBackupPath + "/res_txz_adapter_cmd_keywords.json";
        }
        LogUtil.d("adapterLocalCommandBackupPath =" + adapterLocalCommandBackupPath);
    }

    public static String getAdapterLocalCommandLoadPath() {
        LogUtil.d("adapterLocalCommandLoadPath =" + mAdapterLocalCommandLoadPath);
        return mAdapterLocalCommandLoadPath;
    }

    public static void setAdapterLocalCommandLoadPath(String adapterLocalCommandLoadPath) {
        if (TextUtils.isEmpty(adapterLocalCommandLoadPath)) {
            mAdapterLocalCommandLoadPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/txz/cmd/res_txz_adapter_cmd_keywords.json";
        } else {
            mAdapterLocalCommandLoadPath = adapterLocalCommandLoadPath +  "/res_txz_adapter_cmd_keywords.json";
        }
        LogUtil.d("adapterLocalCommandLoadPath =" + mAdapterLocalCommandLoadPath);
    }

    /**
     * scrFile版本号大返回1
     * destFile版本号大返回-1 相等返回0
     * srcFile存在但destFile不存在，返回2
     * srcFile不存在但destFile存在，返回-2
     * @param srcFile
     * @param destFile
     * @return
     */
    private static int compareCommandFileVersion(File srcFile, File destFile) {
        if (srcFile == null || destFile == null) {
            throw new IllegalArgumentException("Argument could not be null");
        }
        int resultCode = 0;
        if (srcFile.exists() && destFile.exists()) {
            try {
                BufferedReader srcReader = new BufferedReader(new FileReader(srcFile));
                BufferedReader destReader = new BufferedReader(new FileReader(destFile));
                String srcVersionLine = srcReader.readLine();
                String destVersionLine = destReader.readLine();
                LogUtil.d("srcVersionLine: " + srcVersionLine + " destVersionLine: " + destVersionLine);
                if (isVersionInfo(srcVersionLine) && isVersionInfo(destVersionLine)) {
                    String[] srcVersionInfo = srcVersionLine.split(":");
                    String[] destVersionInfo = destVersionLine.split(":");

                    if (srcVersionInfo.length > 1 && destVersionInfo.length > 1) {
                        String[] v1 = srcVersionInfo[1].split("\\.");
                        String[] v2 = destVersionInfo[1].split("\\.");
                        int compare;
                        for (int i=0;i<v1.length;i++) {
                            compare = (Integer.valueOf(v1[i]) < Integer.valueOf(v2[i])) ? -1 : ((Integer.valueOf(v1[i]) == Integer.valueOf(v2[i])) ? 0 : 1);
                            if (compare != 0) {
                                resultCode = compare;
                                return resultCode;
                            }
                        }
                    }
                    return resultCode;
                }
                if (isVersionInfo(srcVersionLine)) {
                    resultCode = 1;
                }
                if (isVersionInfo(destVersionLine)) {
                    resultCode = -1;
                }
                return resultCode;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return resultCode;
        }
        if (srcFile.exists()) {
            resultCode = 2;
        }
        if (destFile.exists()) {
            resultCode = -2;
        }
        return resultCode;
    }

    private static boolean isVersionInfo(String version) {
        return version != null && version.startsWith("#version:");
    }
}
