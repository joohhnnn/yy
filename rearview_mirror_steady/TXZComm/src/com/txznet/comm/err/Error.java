package com.txznet.comm.err;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by brainBear on 2017/9/20.
 * <p>
 * 该类用于将错误码统一转一遍，例如底层框架和后台返回的错误码数值上是重复的，处理错误的时候需要判断来源和错误码，
 * 逻辑上比较麻烦。转换算法为errorCode = source * ERROR_CODE_MASK + Math.abs(srcErrorCode), 程序统一用
 * errorCode来判断。客户端的错误码就直接定义errorCode，无需定义source和srcErrorCode。
 */
public class Error extends Exception implements Parcelable {
    public static final int ERROR_CODE_MASK = 10000;

    /**
     * 错误来源
     */
    public final int source;
    /**
     * 原始错误码
     */
    public final int srcErrorCode;
    /**
     * 错误码
     */
    public final int errorCode;
    /**
     * 错误描述
     */
    public final String desc;
    /**
     * 错误提示
     */
    public final String hint;

    public Error(int source, int srcErrorCode) {
        this(source, srcErrorCode, null, null);
    }

    public Error(int source, int srcErrorCode, String desc, String hint) {
        this.source = source;
        this.srcErrorCode = srcErrorCode;
        this.desc = desc;
        this.hint = hint;
        errorCode = source * ERROR_CODE_MASK + Math.abs(srcErrorCode);
    }

    public Error(int errorCode, Throwable cause) {
        super(cause);
        this.errorCode = errorCode;
        this.desc = null;
        this.hint = null;
        source = errorCode / ERROR_CODE_MASK;
        srcErrorCode = errorCode % ERROR_CODE_MASK;
    }

    public Error(int errorCode) {
        this(errorCode, null, null);
    }

    public Error(int errorCode, String desc, String hint) {
        this.errorCode = errorCode;
        this.desc = desc;
        this.hint = hint;
        source = errorCode / ERROR_CODE_MASK;
        srcErrorCode = errorCode % ERROR_CODE_MASK;
    }

//    private void decode() {
//        source = errorCode / ERROR_CODE_MASK;
//        srcErrorCode = errorCode % ERROR_CODE_MASK;
//    }
//
//    private void encode() {
//        errorCode = source * ERROR_CODE_MASK + Math.abs(srcErrorCode);
//    }

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

    public static int calcCode(int source, int srcErrorCode) {
        return source * ERROR_CODE_MASK + Math.abs(srcErrorCode);
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

    @Override
    public String toString() {
        return "Error{" +
                "errorCode=" + errorCode +
                ", desc='" + desc + '\'' +
                ", hint='" + hint + '\'' +
                ", throwable=" + getCause() +
                '}';
    }

    @Override
    public String getMessage() {
        return toString();
    }
}
