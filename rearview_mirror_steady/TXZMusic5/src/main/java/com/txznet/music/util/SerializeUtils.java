package com.txznet.music.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author zackzhou
 * @date 2019/7/31,11:41
 */

public class SerializeUtils {
//    private static final ThreadLocal<FSTConfiguration> conf = new ThreadLocal<FSTConfiguration>() {
//        public FSTConfiguration initialValue() {
//            return FSTConfiguration.createDefaultConfiguration();
//        }
//    };

    public static <T> T toObject(byte[] data, Class<T> clazz) {
        return (T) toObject(data);
    }

//    public static byte[] toBytes(Object obj) {
//        return conf.get().asByteArray(obj);
//    }
//
//    public static Object toObject(byte[] data) {
//        return conf.get().asObject(data);
//    }

    public static byte[] toBytes(Object obj) {
        ByteArrayOutputStream byteArr = null;
        ObjectOutputStream out = null;
        try {
            byteArr = new ByteArrayOutputStream();
            out = new ObjectOutputStream(byteArr);
            out.writeObject(obj);
            out.flush();
            return byteArr.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (byteArr != null) {
                try {
                    byteArr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static Object toObject(byte[] data) {
        ObjectInputStream input = null;
        try {
            input = new ObjectInputStream(new ByteArrayInputStream(data));
            return input.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
