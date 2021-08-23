package com.txznet.txz.util.player;

import java.util.List;

/**
 * Created by brainBear on 2017/8/16.
 */
public interface OnStateListener {
    void onPreparedListener();

    void onCompletionListener();

    void onSeekCompleteListener(long seekTime);

    void onPlayProgressListener(long position, long duration);

    void onBufferingUpdateListener(List<LocalBuffer> buffers);

    void onErrorListener(Error error);

    void onPlayStateListener();

    void onPauseStateListener();

    void onBufferingStateListener();

    void onBufferingEndStateListener();

    void onIdelListener();
}
