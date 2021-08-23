package com.txznet.launcher;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.txznet.comm.base.BaseActivity;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.version.TXZVersion;
import com.txznet.launcher.domain.LaunchManager;
import com.txznet.launcher.event.BroadcastingCentre;
import com.txznet.launcher.event.EventTypes;
import com.txznet.launcher.widget.container.DialogRecordWin;
import com.txznet.launcher.widget.container.PageContainer;
import com.txznet.launcher.widget.image.SQImageLittle;
import com.txznet.sdk.TXZConfigManager;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // FIXME: 2018/8/18 这里发送广播，会导致发送了两次
        launchMain();
    }

    private void launchMain() {
        setContentView(R.layout.activity_main);
        LogUtil.logd(TXZVersion.PACKTIME + ",svn:" + TXZVersion.SVNVERSION);

        LaunchManager.getInstance().initContainer();
        LaunchManager.getInstance().assignViewParent((ViewGroup) findViewById(R.id.main_layout));


        // startActivity(new Intent(this, TestActivity.class));
    }

    private void testPageContainer() {
        PageContainer container = new PageContainer(MainActivity.this);
        setContentView(container);
    }

    private void testRecordWin() {
//        SmallRecordWin win = new SmallRecordWin(MainActivity.this);
        DialogRecordWin win = new DialogRecordWin(MainActivity.this);
        win.setImageView(new SQImageLittle(MainActivity.this));
        win.setContentView(View.inflate(MainActivity.this, R.layout.module_nav_hud_half, null));
//        win.setContentView(getContentView());
//        setContentView(win);
        win.open("您吃饭了吗");
    }

    private View getContentView() {
        TextView tv = new TextView(MainActivity.this);
        tv.setBackgroundColor(Color.RED);
        tv.setText("您吃饭了吗");
        return tv;
    }

    @Override
    protected void onResume() {
        LogUtil.logd("MainActivity onResume");
        super.onResume();
        BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_LAUNCH_ONRESUME);
    }

    @Override
    protected void onPause() {
        LogUtil.logd("MainActivity onPause");
        BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_LAUNCH_ONSTOP);
        super.onPause();
    }

    @Override
    protected void onStop() {
        LogUtil.logd("MainActivity onStop");
        super.onStop();
        BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_LAUNCH_ONSTOP);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.logd("MainActivity onDestroy");
    }

    @Override
    public void onBackPressed() {
        BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_LAUNCH_ONBACKPRESSED);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        LogUtil.logd("onKeyDown: keyCode=" + keyCode + ", event=" + event);
        /*
            远峰协议变更，红蓝键对应
            电源键 KEYCODE_POWER
            红键 KEYCODE_VOLUME_DOWN
            蓝键 KEYCODE_VOLUME_UP
          */
//        switch (keyCode) {
//            case KeyEvent.KEYCODE_VOLUME_UP: // 蓝键
//                BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_DEVICE_BLUE_BUTTON_PRESSED);
//                break;
//            case KeyEvent.KEYCODE_VOLUME_DOWN: // 红键
//                BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_DEVICE_RED_BUTTON_PRESSED);
//                break;
//        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 设置成不允许显示左上角的版本号。
     * @return 是否允许显示
     */
    @Override
    public boolean enableMaskFromChild() {
        return false;
    }
}