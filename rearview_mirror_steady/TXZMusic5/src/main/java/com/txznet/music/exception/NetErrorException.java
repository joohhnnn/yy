package com.txznet.music.exception;

import com.txznet.comm.err.Error;

/**
 * Created by brainBear on 2018/2/28.
 */

public class NetErrorException extends Exception {

    public final String cmd;
    public final Error error;

    public NetErrorException(String cmd, Error error) {
        this.cmd = cmd;
        this.error = error;
    }

    @Override
    public String toString() {
        return getClass().getName() + ": " + cmd + " " + error.toString();
    }
}