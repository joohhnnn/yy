package com.txznet.music;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.txznet.music.data.http.api.txz.entity.resp.TXZRespSearch;
import com.txznet.music.util.JsonHelper;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.txznet.music", appContext.getPackageName());

        String testStr = "{\"errCode\":0,\"arrAudio\":[],\"arrAlbum\":[],\"arrMix\":[{\"audio\":{\"sid\":2,\"id\":102065754,\"name\":\"\\u6b62\\u6218\\u4e4b\\u6b87\",\"albumId\":\"\",\"duration\":0,\"arrArtistName\":[\"\\u5468\\u6770\\u4f26\"],\"bShowSource\":true,\"bNoCache\":false,\"downloadType\":1,\"strDownloadUrl\":\"C4000042fRqf4fC8ZB\",\"strProcessingUrl\":\"http:\\/\\/c.y.qq.com\\/base\\/fcgi-bin\\/fcg_music_express_mobile3.fcg?format=json&inCharset=utf8&outCharset=utf-8&notice=0&platform=yqq&needNewCode=0&cid=205361747&songmid=0042fRqf4fC8ZB&filename=C4000042fRqf4fC8ZB.m4a&guid=37a560bd5446a27c0151e6e4de00a072\",\"report\":\"\\u6b62\\u6218\\u4e4b\\u6b87\",\"sourceFrom\":\"QQ\\u97f3\\u4e50\",\"score\":10000,\"urlType\":2,\"flag\":10,\"wakeUp\":[]},\"album\":null}],\"returnType\":3,\"playType\":1,\"delayTime\":0,\"playIndex\":0,\"arrMeasure\":[],\"errMeasure\":null}";

        JsonHelper.fromJson(testStr, TXZRespSearch.class);

    }
}
