package com.txznet.txzsetting.util;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.txznet.txzsetting.TXZApplication;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * 资源、配置文件存放路径常量及获取
 * 给方案公司使用的位置: /system/txz/ /system/app/ /etc/txz/ /custom/etc/
 *
 * @author Terry
 */
public class FilePathConstants {
    private static final String TAG = FilePathConstants.class.getSimpleName();

    private FilePathConstants() {
    }

    /**
     * 文件存放路径1(给方案商使用)
     */
    public static final String BASE_PATH_USER1 = "/etc/txz/";
    /**
     * 文件存放路径2(给方案商使用)
     */
    public static final String BASE_PATH_USER2 = "/system/txz/";
    /**
     * 文件存放路径3(给方案商使用)(方易通嫌新建目录拷贝文件麻烦，希望放在/system/app/下跟apk一起拷贝过去)
     */
    public static final String BASE_PATH_USER3 = "/system/app/";
    /**
     * 文件存放路径3(给方案商使用)(达讯嫌放在/system/路径下需要修改系统分区，/custom/etc/是mtk推荐的配置文件存放位置)
     */
    public static final String BASE_PATH_USER4 = "/custom/etc/";

    /**
     * 文件存放优先路径(预留给我们用于下发或者运营)
     */
    public static final String BASE_PATH_PRIOR = Environment.getExternalStorageDirectory() + "/txz/";

    // ////////////////////// TXZSetting资源//////////////////////////////
    public static final String SKIN_FILE_PRIOR_RESOURCE = BASE_PATH_PRIOR + "com.txznet.txzsetting.cfg"; // 优先读取配置文件路径
    public static final String SKIN_FILE_RESOURCE_USER = BASE_PATH_USER1 + "com.txznet.txzsetting.cfg"; // 用户配置文件路径1
    public static final String SKIN_FILE_RESOURCE_USER_2 = BASE_PATH_USER2 + "com.txznet.txzsetting.cfg"; // 用户配置文件路径2
    public static final String SKIN_FILE_RESOURCE_USER_3 = BASE_PATH_USER3 + "com.txznet.txzsetting.cfg"; // 用户配置文件路径2
    public static final String SKIN_FILE_RESOURCE_USER_4 = BASE_PATH_USER4 + "com.txznet.txzsetting.cfg"; // 用户配置文件路径2


    public static String SKIN_FILE_RESOURCE_DEFAULT; // 默认资源文件路径/data/ResHolder.apk

    // ////////////////////////////配置文件///////////////////////////
    // 给方案公司使用的位置: /system/txz/ /system/app/ /etc/txz/ /custom/etc/
    public static final String CONFIG_PATH_USER1 = BASE_PATH_USER2;
    public static final String CONFIG_PATH_USER2 = BASE_PATH_USER3;
    public static final String CONFIG_PATH_USER3 = BASE_PATH_USER1;
    public static final String CONFIG_PATH_USER4 = BASE_PATH_USER4;
    public static final String CONFIG_PATH_PRIOR = BASE_PATH_PRIOR; // 用户后续下发运营
    public static final String CONFIG_PATH_UPGRADE = BASE_PATH_PRIOR; // 用于升级的文件路径
    public static String CONFIG_PATH_DEFAULT; // 默认资源文件路径 data/data/com.txznet.xxx/data/cfg
    public static String mConfigFileName = null; // 包名+.cfg com.txznet.txz.cfg
    public static final String FILE_NAME_COMM_CONFIG = "comm.txz.cfg"; // comm 配置文件，多个app共同用到的配置项
    public static String mConfigFileNameUpgrade = null; // 包名+.cfg.upgrade   com.txznet.txz.cfg.upgrade

    public static String getTXZSettingCfgFile() {
        File priorFile = new File(SKIN_FILE_PRIOR_RESOURCE);
        if (priorFile.exists()) {
            return SKIN_FILE_PRIOR_RESOURCE;
        }
        File userFile1 = new File(SKIN_FILE_RESOURCE_USER);
        if (userFile1.exists()) {
            return SKIN_FILE_RESOURCE_USER;
        }
        File userFile2 = new File(SKIN_FILE_RESOURCE_USER_2);
        if (userFile2.exists()) {
            return SKIN_FILE_RESOURCE_USER_2;
        }
        File userFile3 = new File(SKIN_FILE_RESOURCE_USER_3);
        if (userFile3.exists()) {
            return SKIN_FILE_RESOURCE_USER_3;
        }
        File userFile4 = new File(SKIN_FILE_RESOURCE_USER_4);
        if (userFile4.exists()) {
            return SKIN_FILE_RESOURCE_USER_4;
        }
        SKIN_FILE_RESOURCE_DEFAULT = TXZApplication.getApp().getApplicationInfo().dataDir + "/com.txznet.txzsetting.cfg";
        return SKIN_FILE_RESOURCE_DEFAULT;
    }

    /**
     * 加载配置项 <br>
     *
     * @param configKeys 想要获取配置的key
     * @return 获取到的配置项
     */
    public static HashMap<String, String> getConfig(String... configKeys) {
        List<String> list = Arrays.asList(configKeys);
        return getConfig(list);
    }

    /**
     * 加载配置项 <br>
     *
     * @param configKeys 想要获取配置的key
     * @return 获取到的配置项
     */
    public static HashMap<String, String> getConfig(List<String> configKeys) {
        if (configKeys == null || configKeys.size() == 0) {
            return null;
        }
        HashMap<String, String> cfgs = new HashMap<String, String>();
        loadConfigs();
        if (sConfigs == null || sConfigs.size() == 0) {
            return cfgs;
        }
        for (String key : configKeys) {
            if (TextUtils.isEmpty(key)) {
                continue;
            }
            String value = sConfigs.get(key);
            if (TextUtils.isEmpty(value)) {
                continue;
            }
            cfgs.put(key, value);
        }
        return cfgs;
    }

    private static boolean sIsConfigLoaded = false;
    private static HashMap<String, String> sConfigs;


    private static void loadConfigs() {
        if (!sIsConfigLoaded) {
            if (FilePathConstants.mConfigFileName == null || FilePathConstants.CONFIG_PATH_DEFAULT == null) {
                FilePathConstants.mConfigFileName = TXZApplication.getApp().getPackageName() + ".cfg";
                FilePathConstants.CONFIG_PATH_DEFAULT = TXZApplication.getApp().getApplicationInfo().dataDir + "/cfg/";
            }
            synchronized (FilePathConstants.class) {
                if (!sIsConfigLoaded) {
                    String fileDefault = getTXZSettingCfgFile();
                    File defaultCommFile = new File(fileDefault);
                    if (defaultCommFile.exists()) {
                        loadConfigFromFile(defaultCommFile);
                    }
                    sIsConfigLoaded = true;
                }
            }
        }
    }

    private static void loadConfigFromFile(File file) {
        FileInputStream fis = null;
        InputStreamReader reader = null;
        BufferedReader bufferedReader = null;
        try {
            fis = new FileInputStream(file);
            reader = new InputStreamReader(fis);
            bufferedReader = new BufferedReader(reader);
            String lineTxt = null;
            while ((lineTxt = bufferedReader.readLine()) != null) {
                if (TextUtils.isEmpty(lineTxt))
                    continue;
                String[] subStrings = lineTxt.split("=");
                if (subStrings.length != 2 || TextUtils.isEmpty(subStrings[0])
                        || TextUtils.isEmpty(subStrings[1])) {
                    continue;
                }
                if (sConfigs == null) {
                    sConfigs = new HashMap<String, String>();
                }
                sConfigs.put(subStrings[0].trim(), subStrings[1].trim());
            }
        } catch (FileNotFoundException e) {
            Log.d(TAG, e.toString());
        } catch (IOException e) {
            Log.d(TAG, e.toString());
        } finally {
            try {
                if (fis != null)
                    fis.close();
                if (reader != null)
                    reader.close();
                if (bufferedReader != null)
                    bufferedReader.close();
            } catch (IOException e) {
                Log.d(TAG, e.toString());
            }
        }
    }


}
