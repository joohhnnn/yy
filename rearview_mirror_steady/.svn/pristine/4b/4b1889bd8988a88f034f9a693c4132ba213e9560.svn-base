package com.txznet.music.util;

import android.util.ArrayMap;

import org.jaudiotagger.audio.mp3.XingFrame;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class StringCodec {
    private static final byte[] UTF16BESignature = new byte[]{(byte) -2, (byte) -1};
    private static final byte[] UTF16LESignature = new byte[]{(byte) -1, (byte) -2};
    private static final byte[] UTF8Signature = new byte[]{(byte) -17, (byte) -69, (byte) -65};
    protected static Map encMap = new ArrayMap(4);
    private boolean addSignature = false;
    private String detectedEncoding;

    static {
        encMap.put("UTF-8", UTF8Signature);
        encMap.put("UTF8", UTF8Signature);
        encMap.put("UTF-16BE", UTF16BESignature);
        encMap.put(StandardCharsets.UTF_16LE, UTF16LESignature);
    }

    public static boolean isEncodable(String str, String str2) throws UnsupportedEncodingException {
        StringCodec stringCodec = new StringCodec();
        return stringCodec.decode(stringCodec.encode(str, str2), str2).equals(str);
    }

    public static boolean isUTF8(byte[] bArr) {
        return isUTF8(bArr, 0, bArr.length);
    }

    public static boolean isUTF8(byte[] bArr, int i, int i2) {
        if (startsWith(bArr, i, UTF8Signature)) {
            return true;
        }
        int i3 = i;
        boolean z = false;
        while (i3 < i + i2) {
            if ((bArr[i3] & XingFrame.MAX_BUFFER_SIZE_NEEDED_TO_READ_XING) == XingFrame.MAX_BUFFER_SIZE_NEEDED_TO_READ_XING) {
                int i4 = 2;
                while (i4 < 8 && ((1 << (7 - i4)) & bArr[i3]) != 0) {
                    i4++;
                }
                int i5 = 1;
                while (i5 < i4) {
                    if (i3 + i5 >= i2 || (bArr[i3 + i5] & XingFrame.MAX_BUFFER_SIZE_NEEDED_TO_READ_XING) != 128) {
                        return false;
                    }
                    i5++;
                }
                z = true;
            }
            i3++;
        }
        return z;
    }

    private static boolean startsWith(byte[] bArr, int i, byte[] bArr2) {
        int i2 = 0;
        while (i2 < bArr2.length) {
            if (i2 + i == bArr.length || bArr[i2 + i] != bArr2[i2]) {
                return false;
            }
            i2++;
        }
        return true;
    }

    public String decode(byte[] bArr) throws UnsupportedEncodingException {
        return decode(bArr, 0, bArr.length, null);
    }

    public String decode(byte[] bArr, int i, int i2) throws UnsupportedEncodingException {
        return decode(bArr, i, i2, null);
    }

    public String decode(byte[] bArr, int i, int i2, String str) throws UnsupportedEncodingException {
        if (str == null) {
            str = System.getProperty("file.encoding");
        }
        this.detectedEncoding = null;
        for (Object item : encMap.entrySet()) {
            Map.Entry entry = (Map.Entry) item;
            byte[] bArr2 = (byte[]) entry.getValue();
            if (startsWith(bArr, i, bArr2)) {
                this.detectedEncoding = (String) entry.getKey();
                return new String(bArr, bArr2.length + i, i2 - bArr2.length, this.detectedEncoding);
            }
        }
        if (!isUTF8(bArr, i, i2)) {
            return new String(bArr, 0, i2, str);
        }
        this.detectedEncoding = "UTF-8";
        return new String(bArr, i, i2, this.detectedEncoding);
    }

    public String decode(byte[] bArr, String str) throws UnsupportedEncodingException {
        return decode(bArr, 0, bArr.length, str);
    }

    public byte[] encode(String str) throws UnsupportedEncodingException {
        return encode(str, null);
    }

    public byte[] encode(String str, String str2) throws UnsupportedEncodingException {
        if (str2 == null) {
            str2 = System.getProperty("file.encoding");
        }
        byte[] bytes = str.getBytes(str2);
        this.detectedEncoding = str2;
        if (!this.addSignature) {
            return bytes;
        }
        byte[] bArr = (byte[]) encMap.get(str2);
        if (bArr == null) {
            return bytes;
        }
        byte[] bArr2 = new byte[0];
        try {
            bArr2 = new byte[(bArr.length + bytes.length)];
            System.arraycopy(bArr, 0, bArr2, 0, bArr.length);
            System.arraycopy(bytes, 0, bArr2, bArr.length, bytes.length);
            return bArr2;
        } catch (OutOfMemoryError e) {
            bArr = bArr2;
            e.printStackTrace();
            return bArr;
        }
    }

    public String getDetectedEncoding() {
        return this.detectedEncoding;
    }

    public boolean isAddSignature() {
        return this.addSignature;
    }

    public void setAddSignature(boolean z) {
        this.addSignature = z;
    }
}
