package com.txznet.audio.player;

/**
 * Created by brainBear on 2017/8/16.
 */
public interface OnCodecListener {
    public void onTrackWrite(long total);

    public void onDecode(long total);

    public void onError(int errCode, String errDesc);

    public void onCodecEnd();

    public void onState(ICodecTrack.State state);
}
