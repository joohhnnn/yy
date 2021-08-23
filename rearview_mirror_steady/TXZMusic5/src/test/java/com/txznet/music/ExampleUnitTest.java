package com.txznet.music;

import com.txznet.audio.player.entity.Audio;
import com.txznet.audio.player.queue.PlayQueue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    List<Audio> mList = new ArrayList<>();
    PlayQueue mQueue = new PlayQueue();

//    {
//        Audio audio = new Audio();
//        audio.setName("a.mp3");
//        mList.add(audio);
//
//        Audio audio1 = new Audio();
//        audio1.setName("b.mp3");
//        mList.add(audio1);
//
//        mQueue.setQueue(mList);
//    }

    @Test
    public void testQueue() {

    }
}