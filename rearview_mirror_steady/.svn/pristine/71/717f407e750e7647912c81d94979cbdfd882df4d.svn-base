package com.txznet.txz.util.recordcenter;

public class AesDte {
	  static {
	        System.loadLibrary("aesdte");
	    }
	    
	    public AesDte(int rate, int channel){
	        init(rate, channel);
	    }
	    
	    public static native int init(int rate, int channel);
	    
	    public static native int process(byte[] micBuf, byte[] echoBuf);
	    
	    /**
	     * 设置
	     * id为3时
	     *     param为1表示双麦右声道参考回音消除 。默认为双麦右声道参考回音消除（滤噪为1的情况）
	     *     param为2或其它值表示与1相反
	     * @param id
	     * @param param
	     */
	    public static native void setOptionInt(int id, int param);
}
