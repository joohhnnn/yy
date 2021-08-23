package com.txznet.txz.util;

import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

import com.txznet.comm.remote.util.LogUtil;

/**
 * 加密解密工具包
 */
public class CipherUtil {

    public static final String DES           = "DES";
    public static final String ALGORITHM_DES = "DES/CBC/PKCS5Padding";

    /**
     * DES算法，加密
     * @param data 待加密字符串
     * @param key 加密私钥，长度不能够小于8位
     * @return 加密后的字节数组，一般结合Base64编码使用
     * @throws InvalidAlgorithmParameterException
     * @throws Exception
     */
    public static String enCrypt(String key, String data) {
        if (data == null) {
            return null;
        }
        try {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
            //key的长度不能够小于8位字节
            DESKeySpec dks = new DESKeySpec(key.getBytes());
            Key secretKey = keyFactory.generateSecret(dks);
            Cipher cipher = Cipher.getInstance(ALGORITHM_DES);
            IvParameterSpec iv = new IvParameterSpec("12345678".getBytes());
            AlgorithmParameterSpec paramSpec = iv;
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, paramSpec);
            byte[] bytes = cipher.doFinal(data.getBytes());
            return byte2hex(bytes);
        } catch (Exception e) {
            e.printStackTrace();
            return data;
        }
    }

    /**
     * DES算法，解密
     * @param data 待解密字符串
     * @param key 解密私钥，长度不能够小于8位
     * @return 解密后的字节数组
     * @throws Exception 异常
     */
    public static String deCrypt(String key, String data) {
        if (data == null) {
            return null;
        }
        try {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
            //key的长度不能够小于8位字节
            DESKeySpec dks = new DESKeySpec(key.getBytes());
            Key secretKey = keyFactory.generateSecret(dks);
            Cipher cipher = Cipher.getInstance(ALGORITHM_DES);
            IvParameterSpec iv = new IvParameterSpec("12345678".getBytes());
            AlgorithmParameterSpec paramSpec = iv;
            cipher.init(Cipher.DECRYPT_MODE, secretKey, paramSpec);
            return new String(cipher.doFinal(hex2byte(data.getBytes())));
        } catch (Exception e) {
            e.printStackTrace();
            return data;
        }
    }
    
    /**
     * DES加密 
     * @param key
     * @param data
     * @return
     */
    public static byte[] enCrypt(String key, byte[] data) {
        if (data == null) {
            return null;
        }
        try {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
            //key的长度不能够小于8位字节
            DESKeySpec dks = new DESKeySpec(key.getBytes());
            Key secretKey = keyFactory.generateSecret(dks);
            Cipher cipher = Cipher.getInstance(ALGORITHM_DES);
            IvParameterSpec iv = new IvParameterSpec("12345678".getBytes());
            AlgorithmParameterSpec paramSpec = iv;
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, paramSpec);
            return cipher.doFinal(data);
        } catch (Exception e) {
        	LogUtil.logw("DES enCrypt = "+e.getMessage());
            return data;
        }
    }
    
    /**
     * DES解密
     * @param key
     * @param data
     * @return
     */
    public static byte[] deCrypt(String key, byte[] data) {
        if (data == null) {
            return null;
        }
        try {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
            //key的长度不能够小于8位字节
            DESKeySpec dks = new DESKeySpec(key.getBytes());
            Key secretKey = keyFactory.generateSecret(dks);
            Cipher cipher = Cipher.getInstance(ALGORITHM_DES);
            IvParameterSpec iv = new IvParameterSpec("12345678".getBytes());
            AlgorithmParameterSpec paramSpec = iv;
            cipher.init(Cipher.DECRYPT_MODE, secretKey, paramSpec);
            return cipher.doFinal(data);
        } catch (Exception e) {
        	LogUtil.logw("DES deCrypt = "+e.getMessage());
        	return data;
        }
    }
    
    /**
     * 二行制转字符串
     * @param b
     * @return
     */
    private static String byte2hex(byte[] b) {
        StringBuilder hs = new StringBuilder();
        String stmp;
        for (int n = 0; b != null && n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0XFF);
            if (stmp.length() == 1) {
                hs.append('0');
            }
            hs.append(stmp);
        }
        return hs.toString().toUpperCase();
    }
    
    private static byte[] hex2byte(byte[] b) {
        if ((b.length % 2) != 0) {
            throw new IllegalArgumentException();
        }
        byte[] b2 = new byte[b.length / 2];
        for (int n = 0; n < b.length; n += 2) {
            String item = new String(b, n, 2);
            b2[n / 2] = (byte) Integer.parseInt(item, 16);
        }
        return b2;
    }
    
/*    private final static String KEY = "ACSDFLKlasdkfkjllasdf";

	public static void main(String[] args) {
		File infoFile = new File("./data");
		BufferedReader br = null;
		String string = "";
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(infoFile)));
			string = br.readLine();
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(string);
		
		String enCrypt = CipherUtil.enCrypt(KEY, string);
		System.out.println(enCrypt);
		infoFile = new File("./enCryptData");
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(infoFile)));
			bw.write(enCrypt);
			bw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(infoFile)));
			string = br.readLine();
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String deCrypt = CipherUtil.deCrypt(KEY, string);
		System.out.println(deCrypt);
	}*/
    
}
