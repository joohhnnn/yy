package com.txznet.comm.ui.theme.test.skin;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.text.TextUtils;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.test.utils.UnZipUtil;
import com.txznet.comm.ui.theme.test.view.FloatView;
import com.txznet.comm.ui.theme.test.view.RecordView;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogicBase;
import com.txznet.resholder.R;
import com.txznet.txz.util.FileUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 说明：LOGO皮肤更换全局资称定义与管理
 * <p>
 * 四种帧动画资源：听、思考、播报、等待。由json文件定义名称，因为不确定帧数
 * <p>
 * SK: skin
 *
 * @author xiaolin
 * create at 2020-11-18 20:17
 */
public class SK {

    private static final String TAG = "SK";

    public static class AnimInfo {
        public String name;// 动画名
        public int duration;// 每一帧时间
        public String[] pictureNames;// 资源名称
    }

    /**
     * 图片，枚举名称=资源名称
     */
    public enum DRAWABLE {
        // 悬浮按钮
        person_float,
        person_float_press,

        person_float_time,// 悬浮按钮倒计时底图
        smart_handy_logo,// 智能捷径机器人LOGO
        skin_logo,// 皮肤图标

        // TODO 测试用
        person,
        test
    }

    /**
     * 动画
     */
    public enum ANIM {
        bobao("bobao"),
        sikao("sikao"),
        ting("ting"),
        wait("wait");

        private String key;

        ANIM(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

    private static final String PATH_SMART_HANDY =
            Environment.getExternalStorageDirectory().getPath() + "/txz/smarthandy/";
    private static final String PATH_SMART_HANDY_SKIN = PATH_SMART_HANDY + "skin/";
    private static final String PATH_RES = PATH_SMART_HANDY_SKIN + "res/";
    private static final String PATH_SKIN_CFG = PATH_SMART_HANDY_SKIN + "assets/skin_cfg.json";

    // 找不到资源使用的默认图片
    private static final int DEFAULT_DRAWABLE = R.drawable.star_enable;

    /**
     * 使用资源优先级，后续会根据dpi计算
     */
    private static List<String> drawableDir = new ArrayList<String>() {{
        add("drawable-nodpi");
        add("drawable-nodpi-v4");
        add("drawable-hdpi");
        add("drawable-xhdpi");
        add("drawable-xxhdpi");
        add("drawable-xxxhdpi");
        add("drawable-mdpi");
        add("drawable-ldpi");
    }};

    private static Map<String, AnimInfo> animMap = new HashMap<>();
    private static Map<String, AnimInfo> animMapDefault = new HashMap<>();
    private static boolean inited = false;


    /**
     * 应用主题包
     *
     * @param skinFile
     */
    public static void applySkin(String skinFile) {
        if(!inited){
            loadConfigDefault();
        }
        inited = true;

        LogUtil.d(WinLayout.logTag, "apply skin:" + skinFile);
        if (!new File(skinFile).exists()) {
            return;
        }

        // 移除旧文件
        FileUtil.removeDirectory(PATH_SMART_HANDY_SKIN);
        // 隐藏媒体文件
        try {
            new File(PATH_SMART_HANDY).mkdirs();
            new File(PATH_SMART_HANDY, ".nomedia").createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 解压文件
        UnZipUtil.getInstance().UnZip(skinFile, PATH_SMART_HANDY_SKIN);
        loadConfig();

        /*刷新资源引用*/
        // TODO 刷新皮肤资源引用
        AppLogicBase.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                RecordView.getInstance().onUpdateAnim();
                FloatView.getInstance().onUpdateResource();
            }
        });
    }

    /**
     * 读取默认的/assets/skin_cfg.json醒置文件
     */
    private static void loadConfigDefault() {
        animMapDefault.clear();
        Context context = UIResLoader.getInstance().getModifyContext();
        try {
            InputStream ins = context.getAssets().open("skin_cfg.json");
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = ins.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            JSONBuilder jb = new JSONBuilder(result.toByteArray());
            loadConfig(jb, animMapDefault);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取解压后的皮肤资源
     */
    private static void loadConfig() {
        // 读取skin_cfg.json醒置文件
        File file = new File(PATH_SKIN_CFG);
        JSONBuilder jb = new JSONBuilder(file);
        loadConfig(jb, animMap);
    }

    private static void loadConfig(JSONBuilder jb, Map<String, AnimInfo> map) {
        map.clear();
        int ver = jb.getVal("ver", int.class, 0);// 版本，只有一个版本1
        String skinName = jb.getVal("skinName", String.class);// 主题名称
        String author = jb.getVal("author", String.class);// 作者
        String time = jb.getVal("time", String.class);// 创作时间

        LogUtil.d(TAG, String.format(Locale.getDefault(),
                "ver:%d, skinName:%s, author:%s, time:%s",
                ver, skinName, author, time));
        JSONArray animList = jb.getVal("animList", JSONArray.class);
        for (int i = 0; i < animList.length(); i++) {
            try {
                JSONObject obj = animList.getJSONObject(i);
                String name = obj.getString("name");
                int duration = obj.getInt("duration");
                JSONArray pictureJsonAry = obj.getJSONArray("pictures");
                String[] pictureNames = new String[pictureJsonAry.length()];
                for (int j = 0; j < pictureJsonAry.length(); j++) {
                    pictureNames[j] = pictureJsonAry.getString(j);
                }

                AnimInfo animInfo = new AnimInfo();
                animInfo.name = name;
                animInfo.duration = duration;
                animInfo.pictureNames = pictureNames;
                map.put(name, animInfo);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private static String getResPath(String resName) {
        if (TextUtils.isEmpty(resName)) {
            LogUtil.d(TAG, "resource file not found, resName:" + resName);
            return null;
        }
        for (String row : drawableDir) {
            String path = PATH_RES + row + "/" + resName + ".png";
            File file = new File(path);
            if (file.exists()) {
                return path;
            }
        }
        LogUtil.d(TAG, "resource file not found, resName:" + resName);
        return null;
    }

    public static Drawable getDrawable(DRAWABLE d) {
        return getDrawable(d.name());
    }

    /**
     * @param name 没有后缀
     * @return
     */
    public static Drawable getDrawable(String name) {
        LogUtil.d("getDrawable():" + name);
        // 外部资源
        Bitmap bitmap = BitmapFactory.decodeFile(getResPath(name));
        if (bitmap != null) {
            Resources res = UIResLoader.getInstance().getModifyContext().getResources();
            return new BitmapDrawable(res, bitmap);
        }

        // 内置资源
        Drawable drawable = UIResLoader.getInstance().getDrawable(name);
        if (drawable != null) {
            LogUtil.e("使用内置资源:" + name);
            return drawable;
        }

        LogUtil.e("使用默认资源:" + name);

        // 默认资源
        drawable = UIResLoader.getInstance().getModifyContext().getResources().getDrawable(DEFAULT_DRAWABLE);
        return drawable;
    }

    public static AnimInfo getAnim(ANIM anim) {
        AnimInfo info = animMap.get(anim.getKey());
        if (info == null) {
            info = animMapDefault.get(anim.getKey());
        }
        return info;
    }
}
