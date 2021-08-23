package com.txznet.proxy.util;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author zackzhou
 * @date 2019/7/31,14:32
 */

public class IORecycler {
    private IORecycler() {
    }

    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
