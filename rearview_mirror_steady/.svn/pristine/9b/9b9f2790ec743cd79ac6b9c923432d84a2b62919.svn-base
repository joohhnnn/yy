package com.txznet.simdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        sendBroadcast(new Intent("com.txznet.sim.service.open"));
        Log.i("test","send broadcast com.txznet.sim.service.open");
        finish();
    }
}
