package com.example.app3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.app3.util.JsonUtil;
import com.txznet.adapter.aidl.TXZAIDLManager;

public class MainActivity extends AppCompatActivity {
    private EditText et;
    private Button bt;
    private TextView tv;
    private EditText etNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {
        et = findViewById(R.id.et_rg_command);
        bt = findViewById(R.id.bt_rg_command);
        tv = findViewById(R.id.tv_result);
        etNav = findViewById(R.id.et_nav);

//        bt.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String regCommand = et.getText().toString();
////                System.out.println(regCommand);
//                if (!"".equals(regCommand)){
//                    TXZAIDLManager.getInstance().sendCommand(2400, "cmd.reg", JsonUtil.transParamToJson("cmd", regCommand).getBytes(), false);
//                }
//            }
//        });


//        TXZAIDLManager.getInstance().initService(this, new TXZAIDLManager.TXZCommandListener() {
//            @Override
//            public void onBindSuccess() {
//
//            }
//
//            @Override
//            public byte[] onCommandReceive(int keyType, String s, byte[] bytes) {
//                switch (keyType){
//                    case 1400:
//                        if ("cmd.match".equals(s)){
//                            String cmd = JsonUtil.getStringFromJson("cmd", bytes, "");
//                            if (!"".equals(cmd)){
//                                setResult(cmd);
//                            }
//                        }
//                }
////                return new byte[0];
//                return null;
//            }
//        });
    }

    private void setResult(String s){
        tv.setText("语音命中了《" + s + "》命令");
    }

    public void navTo(View view) {
        String addr = etNav.getText().toString();
        if (addr.equals("")){
            addr = "深圳宝安国际机场";
        }
        TXZAIDLManager.getInstance().sendCommand(2100, "nav.to",
                JsonUtil.transParamToBytes("addr", addr, "lat", 22.63249969482422, "lng", 113.81513977050781), false);
    }

    public void sendBC(View view) {
        Intent intent = new Intent();
        intent.setAction("autochips.intent.action.PREQB_POWEROFF");
        sendBroadcast(intent);
    }

    public void TXZWakeup(View view) {
        Intent intent = new Intent();
        intent.setAction("com.txznet.txz.wakeup");
        sendBroadcast(intent);
    }

    public void throwNullPointer(View view) {
        throw new NullPointerException();
    }
}
