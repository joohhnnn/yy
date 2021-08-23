package com.txznet.comm.ui.theme.test.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.theme.BaseStyleConfig;
import com.txznet.comm.ui.theme.ThemeStyle;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.loader.AppLogicBase;
import com.txznet.sdk.TXZMovieManager;
import com.txznet.sdk.TXZRecordWinManager;
import com.txznet.txz.util.TXZFileConfigUtil;

/**
 * Created by ASUS User on 2018/9/5.
 */

public class StyleConfig extends BaseStyleConfig{
    private final static StyleConfig instance = new StyleConfig();

    public static StyleConfig getInstance() {
        return instance;
    }

    private StyleConfig() {
        mThemeStyle = new ThemeStyle();

        ThemeStyle.Theme theme;
        ThemeStyle.Model model;
        ThemeStyle.Style style;
        
		STYLE_PICTURE_PATH = Environment.getExternalStorageDirectory() +"/txz/stylePicture/";
		FULL_STYLE_PICTURE_PATH = STYLE_PICTURE_PATH + FULL_STYLE_PICTURE_NAME + STYLE_PICTURE_SUFF;
		HALF_STYLE_PICTURE_PATH = STYLE_PICTURE_PATH + HALF_STYLE_PICTURE_NAME + STYLE_PICTURE_SUFF;
		NONE_STYLE_PICTURE_PATH = STYLE_PICTURE_PATH + NONE_STYLE_PICTURE_NAME + STYLE_PICTURE_SUFF;

        theme = new ThemeStyle.Theme(THEME_ROBOT_NAME);

        //添加全屏样式
        //model name为空时，不会注册打开xx模式的指令
//        model = new ThemeStyle.Model(ThemeStyle.STYLE_MODEL_0, STYLE_MODE_NAME_1);
//        style = new ThemeStyle.Style(STYLE_FULL_SCREES_NAME, model, theme);
//        style.setImgUrl(FULL_STYLE_PICTURE_PATH);
//        style.setDefault(true);
//        mThemeStyle.addStyle(style);

        //添加半屏样式
//        if( TXZFileConfigUtil.getBooleanSingleConfig(TXZFileConfigUtil.KEY_UI3_BAND_HALF_MODEL,true)){
//            model = new ThemeStyle.Model(ThemeStyle.STYLE_MODEL_0, STYLE_MODE_NAME_1);
//            style = new ThemeStyle.Style(STYLE_HALF_SCREES_NAME, model, theme);
//            style.setImgUrl(HALF_STYLE_PICTURE_PATH);
//            mThemeStyle.addStyle(style);
//        }

        //添加无屏样式
//        if( TXZFileConfigUtil.getBooleanSingleConfig(TXZFileConfigUtil.KEY_UI3_BAND_NONE_MODEL,true)){
            model = new ThemeStyle.Model(ThemeStyle.STYLE_MODEL_1, STYLE_MODE_NAME_2);
            style = new ThemeStyle.Style(STYLE_NONE_SCREES_NAME, model, theme);
            style.setImgUrl(NONE_STYLE_PICTURE_PATH);
            mThemeStyle.addStyle(style);
//        }
        SkillfulReminding.getInstance().hasSkillful();
        
        decompPicture();
    }

    //将三种交互模式的图片存储到指定路径，给setting使用
    private void decompPicture() {
		LogUtil.logd(WinLayout.logTag+ "decompPicture begin");
		
        createPath(STYLE_PICTURE_PATH);
		Drawable draw;
		Bitmap bitmap;
		File file;
		file = new File(FULL_STYLE_PICTURE_PATH);
		if(!file.exists()) {
			draw = LayouUtil.getDrawable(FULL_STYLE_PICTURE_NAME);
			bitmap = drawableToBitmap(draw);
			saveBitmap(bitmap, file, CompressFormat.PNG);
		}
		
		file = new File(HALF_STYLE_PICTURE_PATH);
		if(!file.exists()) {
			draw = LayouUtil.getDrawable(HALF_STYLE_PICTURE_NAME);
			bitmap = drawableToBitmap(draw);
			saveBitmap(bitmap, file, CompressFormat.PNG);
		}
		
		file = new File(NONE_STYLE_PICTURE_PATH);
		if(!file.exists()) {
			draw = LayouUtil.getDrawable(NONE_STYLE_PICTURE_NAME);
			bitmap = drawableToBitmap(draw);
			saveBitmap(bitmap, file, CompressFormat.PNG);
		}
    }
    
    public void createPath(String path) {
        File file = new File(path);
        if(!file.exists()) {
        	file.mkdir();
        }
    }

    /**
     * 将Drawable转化为Bitmap
     * @param drawable
     * @return
     */
    public Bitmap drawableToBitmap(Drawable drawable) {
        if(drawable == null)
            return null;
        return ((BitmapDrawable)drawable).getBitmap();
    }

    /**
     * 将Bitmap以指定格式保存到指定路径
     * @param bitmap
     * @param path
     */
    public void saveBitmap(Bitmap bitmap, File file, Bitmap.CompressFormat format) {
        // 创建一个位于SD卡上的文件
        FileOutputStream out = null;
        try{
            // 打开指定文件输出流
            out = new FileOutputStream(file);
            // 将位图输出到指定文件
            bitmap.compress(format, 100 , out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private ThemeStyle mThemeStyle;
    private ThemeStyle.Style mSelectStyle;

    @Override
    public ThemeStyle getThemeStyle() {
        return mThemeStyle;
    }

    @Override
    public void setSelectStyle(ThemeStyle.Style style) {
        LogUtil.loge(WinLayout.logTag+ "setSelectStyle " + style.getName());
    	
        if(mSelectStyle != null) {
        	if(mSelectStyle.getName().equals(style.getName())) {
        		return;
        	}
        }
        mSelectStyle = style;     

        if (style.getTheme().getName().equals(THEME_ROBOT_NAME)) {
            if (style.getName().equals(STYLE_FULL_SCREES_NAME)) {
                mCurrentSelectStyleIndex = STYLE_ROBOT_FULL_SCREES;
            } else if (style.getName().equals(STYLE_HALF_SCREES_NAME)) {
                mCurrentSelectStyleIndex = STYLE_ROBOT_HALF_SCREES;
            } else if (style.getName().equals(STYLE_NONE_SCREES_NAME)) {
                mCurrentSelectStyleIndex = STYLE_ROBOT_NONE_SCREES;
            }
            AppLogicBase.runOnUiGround(new Runnable() {
				
				@Override
				public void run() {
		            WinLayout.getInstance().onStyleUpdate(mCurrentSelectStyleIndex);
                    TXZMovieManager.getInstance().sendCurrentThemeStyle(mCurrentSelectStyleIndex);
				}
			});
        }
    }

    public static final String THEME_ROBOT_NAME = "机器人风格";
    public static final String STYLE_MODE_NAME_1 = "新手模式";
    public static final String STYLE_MODE_NAME_2 = "熟手模式";
    public static final int STYLE_ROBOT_FULL_SCREES = 1;
    public static final String STYLE_FULL_SCREES_NAME = "全屏";
    public static final int STYLE_ROBOT_HALF_SCREES = 2;
    public static final String STYLE_HALF_SCREES_NAME = "半屏";
    public static final int STYLE_ROBOT_NONE_SCREES = 3;
    public static final String STYLE_NONE_SCREES_NAME = "无屏";
    
    public static String FULL_STYLE_PICTURE_PATH;
    public static String HALF_STYLE_PICTURE_PATH;
    public static String NONE_STYLE_PICTURE_PATH;
    
    public static String STYLE_PICTURE_PATH;
    public static final String FULL_STYLE_PICTURE_NAME = "style_full";
    public static final String HALF_STYLE_PICTURE_NAME = "style_half";
    public static final String NONE_STYLE_PICTURE_NAME = "style_none";
    public static final String STYLE_PICTURE_SUFF = ".png";
    

    private int mCurrentSelectStyleIndex = STYLE_ROBOT_FULL_SCREES;
    public int getSelectStyleIndex() {
        return mCurrentSelectStyleIndex;
    }

    public ThemeStyle.Style getSelectStyle() {
        return mSelectStyle;
    }
}
