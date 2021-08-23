package com.txznet.txz.util.player;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by brainBear on 2017/9/20.
 * <p>
 * 该类用于将错误码统一转一遍，例如底层框架和后台返回的错误码数值上是重复的，处理错误的时候需要判断来源和错误码，
 * 逻辑上比较麻烦。转换算法为errorCode = source * ERROR_CODE_BASE + Math.abs(srcErrorCode), 程序统一用
 * errorCode来判断。客户端的错误码就直接定义errorCode，无需定义source和srcErrorCode。
 */

public class Error implements Parcelable {


    /**
     * 来源于core，例如网络请求底层框架返回错误
     */
    public static final int SOURCE_CORE = 1;
    /**
     * 来源于服务器，例如请求参数出错
     */
    public static final int SOURCE_SERVER = 2;
    /**
     * 来源于解码器
     */
    public static final int SOURCE_DECODE = 3;
    /**
     * 来源于客户端
     */
    public static final int SOURCE_CLIENT = 4;
    private static final int ERROR_CODE_BASE = 10000;


    //客户端网络请求超时
    public static final int ERROR_CLIENT_NET_TIMEOUT = SOURCE_CLIENT * ERROR_CODE_BASE + 1;
    //客户端没有联网
    public static final int ERROR_CLIENT_NET_OFFLINE = SOURCE_CLIENT * ERROR_CODE_BASE + 2;
    //客户端网络请求返回数据为空
    public static final int ERROR_CLIENT_NET_EMPTY_DATA = SOURCE_CLIENT * ERROR_CODE_BASE + 3;


    //文件校验失败
    public static final int ERROR_CLIENT_MEDIA_FILE_CHECK_FAIL = SOURCE_CLIENT * ERROR_CODE_BASE + 102;
    //播放地址错误
    public static final int ERROR_CLIENT_MEDIA_WRONG_URL = SOURCE_CLIENT * ERROR_CODE_BASE + 103;

    public static final int ERROR_CLIENT_MEDIA_BAD_REQUEST = SOURCE_CLIENT * ERROR_CODE_BASE + 104;
    public static final int ERROR_CLIENT_MEDIA_FILE_FORBIDDEN = SOURCE_CLIENT * ERROR_CODE_BASE + 105;
    public static final int ERROR_CLIENT_MEDIA_NOT_FOUND = SOURCE_CLIENT * ERROR_CODE_BASE + 106;
    public static final int ERROR_CLIENT_MEDIA_GATE_WAY = SOURCE_CLIENT * ERROR_CODE_BASE + 107;
    public static final int ERROR_CLIENT_MEDIA_ERR_IO = SOURCE_CLIENT * ERROR_CODE_BASE + 108;
    public static final int ERROR_CLIENT_MEDIA_BAD_DATA = SOURCE_CLIENT * ERROR_CODE_BASE + 109;
    public static final int ERROR_CLIENT_MEDIA_GET_AUDIO = SOURCE_CLIENT * ERROR_CODE_BASE + 110;
    public static final int ERROR_CLIENT_MEDIA_REMOTE = SOURCE_CLIENT * ERROR_CODE_BASE + 111;
    public static final int ERROR_CLIENT_MEDIA_REQ_SERVER = SOURCE_CLIENT * ERROR_CODE_BASE + 112;
    public static final int ERROR_CLIENT_MEDIA_REQ_TIMEOUT = SOURCE_CLIENT * ERROR_CODE_BASE + 113;
    public static final int ERROR_CLIENT_MEDIA_SYS_PLAYER = SOURCE_CLIENT * ERROR_CODE_BASE + 114;
    public static final int ERROR_CLIENT_MEDIA_NULL_STATE = SOURCE_CLIENT * ERROR_CODE_BASE + 115;


    /**
     * 错误来源
     */
    private int source;

    /**
     * 原始错误码
     */
    private int srcErrorCode;
    /**
     * 错误码
     */
    private int errorCode;
    /**
     * 错误描述
     */
    private String desc;


    /**
     * 错误提示
     */
    private String hint;

    public Error(@SOURCE int source, int srcErrorCode) {
        this(source, srcErrorCode, null, null);
    }

    public Error(@SOURCE int source, int srcErrorCode, String desc, String hint) {
        this.source = source;
        this.srcErrorCode = srcErrorCode;
        this.desc = desc;

        encode();
    }

    public Error(int errorCode) {
        this(errorCode, null, null);
    }

    public Error(int errorCode, String desc, String hint) {
        this.errorCode = errorCode;
        this.desc = desc;
        this.hint = hint;

        decode();
    }

    private void decode() {
        source = errorCode / ERROR_CODE_BASE;
        srcErrorCode = errorCode % ERROR_CODE_BASE;
    }

    private void encode() {
        errorCode = source * ERROR_CODE_BASE + Math.abs(srcErrorCode);
    }

    @Override
    public String toString() {
        return "Error{" +
                "source=" + source +
                ", srcErrorCode=" + srcErrorCode +
                ", errorCode=" + errorCode +
                ", desc='" + desc + '\'' +
                '}';
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public int getSrcErrorCode() {
        return srcErrorCode;
    }

    public void setSrcErrorCode(int srcErrorCode) {
        this.srcErrorCode = srcErrorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    @IntDef({SOURCE_CORE, SOURCE_SERVER, SOURCE_DECODE, SOURCE_CLIENT})
    @Retention(RetentionPolicy.SOURCE)
    private @interface SOURCE {
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.source);
        dest.writeInt(this.srcErrorCode);
        dest.writeInt(this.errorCode);
        dest.writeString(this.desc);
        dest.writeString(this.hint);
    }

    protected Error(Parcel in) {
        this.source = in.readInt();
        this.srcErrorCode = in.readInt();
        this.errorCode = in.readInt();
        this.desc = in.readString();
        this.hint = in.readString();
    }

    public static final Creator<Error> CREATOR = new Creator<Error>() {
        @Override
        public Error createFromParcel(Parcel source) {
            return new Error(source);
        }

        @Override
        public Error[] newArray(int size) {
            return new Error[size];
        }
    };
}
