package com.txznet.txz.util;

import com.pachira.sun.misc.BASE64Decoder;
import com.pachira.sun.misc.BASE64Encoder;
import com.txznet.txz.cfg.ProjectCfg;

import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AesCBC {
    /*已确认
     * 加密用的Key 可以用26个字母和数字组成
     * 此处使用AES-128-CBC加密模式，key需要为16位。
     */
    public static String sKey="txzing";
    public static String ivParameter="1234567812345678";
    private static AesCBC instance=null;
    //private static
    private AesCBC(){

    }
    public static AesCBC getInstance(){
        if (instance==null)
            instance= new AesCBC();
        return instance;
    }
    // 加密
    public static String encrypt(String sSrc, String encodingFormat, String sKey, String ivParameter) throws Exception {
        int base = 16;
        byte[] keyBytes = sKey.getBytes();
        if (keyBytes.length % base != 0) {
            int groups = keyBytes.length / base + (keyBytes.length % base != 0 ? 1 : 0);
            byte[] temp = new byte[groups * base];
            Arrays.fill(temp, (byte) 0);
            System.arraycopy(keyBytes, 0, temp, 0, keyBytes.length);
            keyBytes = temp;
        }
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec skeySpec = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes());//使用CBC模式，需要一个向量iv，可增加加密算法的强度
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
        byte[] encrypted = cipher.doFinal(sSrc.getBytes(encodingFormat));
        return bytesToHexString(encrypted);
//        return new BASE64Encoder().encode(encrypted);//此处使用BASE64做转码。
    }
    private static final char[] HEX_CHAR = {'0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    public static String bytesToHexString(byte[] bytes) {
        char[] buf = new char[bytes.length * 2];
        int index = 0;
        for(byte b : bytes) { // 利用位运算进行转换，可以看作方法一的变种
            buf[index++] = HEX_CHAR[b >>> 4 & 0xf];
            buf[index++] = HEX_CHAR[b & 0xf];
        }

        return new String(buf);
    }


    // 解密
//    public String decrypt(String sSrc, String encodingFormat, String sKey, String ivParameter) throws Exception {
//        try {
//            int base = 16;
//            byte[] keyBytes = sKey.getBytes();
//            if (keyBytes.length % base != 0) {
//                int groups = keyBytes.length / base + (keyBytes.length % base != 0 ? 1 : 0);
//                byte[] temp = new byte[groups * base];
//                Arrays.fill(temp, (byte) 0);
//                System.arraycopy(keyBytes, 0, temp, 0, keyBytes.length);
//                keyBytes = temp;
//            }
//            SecretKeySpec skeySpec = new SecretKeySpec(keyBytes, "AES");
//            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
//            IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes());
//            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
//            byte[] encrypted1 = new BASE64Decoder().decodeBuffer(sSrc);//先用base64解密
//            byte[] original = cipher.doFinal(encrypted1);
//            String originalString = new String(original,encodingFormat);
//            return originalString;
//        } catch (Exception ex) {
//            return null;
//        }
//    }


    public static void main(String[] args) throws Exception {
        // 需要加密的字串
        String cSrc = "123456";
        System.out.println("加密前的字串是："+cSrc);
        // 加密
        String enString = AesCBC.getInstance().encrypt(cSrc,"utf-8",sKey, ivParameter);
        System.out.println("加密后的字串是："+ enString);

        System.out.println("1jdzWuniG6UMtoa3T6uNLA==".equals(enString));

        // 解密
//        String DeString = AesCBC.getInstance().decrypt(enString,"utf-8",sKey,ivParameter);
//        System.out.println("解密后的字串是：" + DeString);
        String url = "http://thirdtest.txzing.com/nlp/fangde/push_token_ok?uid=" + AesCBC.encrypt(String.valueOf(1087), "utf-8", AesCBC.sKey, AesCBC.ivParameter);
        System.out.println(url);
    }
}
