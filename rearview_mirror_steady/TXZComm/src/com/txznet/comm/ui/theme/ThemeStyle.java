package com.txznet.comm.ui.theme;

import android.support.annotation.IntDef;

import com.txznet.comm.ui.IKeepClass;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

/**
 * Created by ASUS User on 2018/9/5.
 */

public class ThemeStyle implements IKeepClass {
    public final static int STYLE_MODEL_0 = 0;
    public final static int STYLE_MODEL_1 = 1;
    private final ArrayList<Style> mStyles = new ArrayList<Style>();

    @IntDef({STYLE_MODEL_0, STYLE_MODEL_1})
    @Retention(RetentionPolicy.SOURCE)
    public @interface StyleMode {
    }

    /**
     * 主题
     */
    public static class Theme implements IKeepClass, Serializable {
        private static final long serialVersionUID = 4084046630080383478L;
        private String mName;

        public Theme() {
        }

        /**
         * @param mName 主题名字
         */
        public Theme(String mName) {
            this.mName = mName;
        }

        /**
         * @return 获取主题的名称
         */
        public String getName() {
            return mName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Theme theme = (Theme) o;

            return mName != null ? mName.equals(theme.mName) : theme.mName == null;
        }

        @Override
        public int hashCode() {
            return mName != null ? mName.hashCode() : 0;
        }
    }

    /**
     * 皮肤包模式<br>
     * 默认模式名称：<br>
     * {@link #STYLE_MODEL_0} 新手模式<br>
     * {@link #STYLE_MODEL_1} 熟手模式<br>
     */
    public static class Model implements IKeepClass, Serializable {
        private static final long serialVersionUID = 528613280093988586L;
        private int mModel;
        private String mName;

        /**
         * @param mModel 模式定义<br>
         *               {@link #STYLE_MODEL_0} 新手模式<br>
         *               {@link #STYLE_MODEL_1} 熟手模式<br>
         * @param mName  模式名称
         */
        public Model(@StyleMode int mModel, String mName) {
            this.mModel = mModel;
            this.mName = mName;
        }

        /**
         * @return 获取模式
         */
        public int getModel() {
            return mModel;
        }

        /**
         * @return 获取模式名称
         */
        public String getName() {
            return mName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Model model = (Model) o;

            if (mModel != model.mModel) return false;
            return mName.equals(model.mName);
        }

        @Override
        public int hashCode() {
            int result = mModel;
            result = 31 * result + mName.hashCode();
            return result;
        }
    }

    /**
     * 主题样式
     * 声明样式名称，所属主题，所属类型，是否设为默认主题
     */
    public static class Style implements IKeepClass, Serializable {
        private static final long serialVersionUID = 2460889096660006752L;
        private String mName;
        private Model mModel;
        private Theme mTheme;
        private String mImgUrl;
        private boolean isDefault;

        /**
         * @param mName  样式名称
         * @param mModel 模式名称
         * @param mTheme 主题名称
         */
        public Style(String mName, Model mModel, Theme mTheme) {
            this.mName = mName;
            this.mModel = mModel;
            this.mTheme = mTheme;
        }

        /**
         * @param mName   样式名称
         * @param mModel  模式名称
         * @param mTheme  主题名称
         * @param mImgUrl 样式展示图片路径
         */
        public Style(String mName, Model mModel, Theme mTheme, String mImgUrl) {
            this.mName = mName;
            this.mModel = mModel;
            this.mTheme = mTheme;
            this.mImgUrl = mImgUrl;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Style style = (Style) o;

            if (!mName.equals(style.mName)) return false;
            if (!mModel.equals(style.mModel)) return false;
            return mTheme.equals(style.mTheme);
        }

        @Override
        public int hashCode() {
            int result = mName.hashCode();
            result = 31 * result + mModel.hashCode();
            result = 31 * result + mTheme.hashCode();
            return result;
        }

        /**
         * @return 获取所属模式
         */
        public Model getModel() {
            return mModel;
        }

        /**
         * @return 获取样式名称
         */
        public String getName() {
            return mName;
        }

        /**
         * @return 获取所属主题
         */
        public Theme getTheme() {
            return mTheme;
        }

        /**
         * @param aDefault 设置为默认样式
         */
        public void setDefault(boolean aDefault) {
            isDefault = aDefault;
        }

        /**
         * @return 是否是默认样式
         */
        public boolean isDefault() {
            return isDefault;
        }

        /**
         * @return 设置样式展示图片
         */
        public String getImgUrl() {
            return mImgUrl;
        }

        /**
         * @param mImgUrl 获取样式展示图片
         */
        public void setImgUrl(String mImgUrl) {
            this.mImgUrl = mImgUrl;
        }
    }

    /**
     * @param style 添加到样式列表
     */
    public synchronized void addStyle(Style style) {
        mStyles.add(style);
    }

    /**
     * @return 获取样式列表
     */
    public ArrayList<Style> getStyles() {
        return mStyles;
    }
}
