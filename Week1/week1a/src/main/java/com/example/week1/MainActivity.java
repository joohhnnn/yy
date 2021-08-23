package com.example.week1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
 private static final int PERMISS_CONTACT = 1;
 Intent intent;
 Parcel parcel;
 ArrayList<Person> list = new ArrayList<Person>();
 Person person;
 Button button1;
 permissionTool permissionTool=new permissionTool();
 //TextView textView;

    //查询返回的列
    String[] SQL_COLUMN =new String[]{
            ContactsContract.CommonDataKinds.Identity.RAW_CONTACT_ID,//row_contact_id
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,//display_name
            ContactsContract.CommonDataKinds.Phone.NUMBER//data1
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化视图
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        intent=new Intent(MainActivity.this,AIDLService.class);
        stopService(intent);
        Log.e("服务端断开","Disconnect");


    }

    public void initView(){
       // textView=findViewById(R.id.textView2);
        permissionTool=new permissionTool();
        button1=findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //需要的权限
                String[] permissList = {Manifest.permission.READ_CONTACTS, Manifest.permission.READ_PHONE_STATE};
                //请求权限
                permissionTool.permissionTool(MainActivity.this, permissList, PERMISS_CONTACT);
                //添加获取通讯录事件

                printQueryResult();
                Log.e("12321312",list.get(1).getName());
                //打开B应用
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                ComponentName cn = new ComponentName("com.example.week1b","com.example.week1b.MainActivity");
                intent.setComponent(cn);
                startActivity(intent);

            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @org.jetbrains.annotations.NotNull String[] permissions, @NonNull @org.jetbrains.annotations.NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i]!= PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "无授权，无法调取通讯录", Toast.LENGTH_SHORT).show();
            }else{
                printQueryResult();
            }
        }
    }

    /**
     * 将通讯录的联系人信息封装到一个对象中，便于传递，并开启Service
     */
    void printQueryResult(){
        //content://com.android.contacts/data/phones
        //ContactsContract.CommonDataKinds常量类
        //使用游标获取通讯录
        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,SQL_COLUMN,null,null,null);
        if(cursor!=null){
            //textView.setText("");
            while (cursor.moveToNext()){
                String ID =cursor.getString(0);
                String contactName =cursor.getString(1);
                String photoNumber =cursor.getString(2);
               // textView.append("联系人ID："+ID+"\n联系人姓名："+contactName+"\n联系人电话："+photoNumber+"\n");
                // 为继承了序列化的person类赋值
                parcel=Parcel.obtain();
                person=Person.CREATOR.createFromParcel(parcel);
                person.setId(ID);
                person.setName(contactName);
                person.setPhone(photoNumber);

                parcel.recycle();
                list.add(person);


            }
            intent=new Intent(MainActivity.this,AIDLService.class);
            //启动service
            intent.putParcelableArrayListExtra("contact", list);
            startService(intent);

            cursor.close();
        }
    }


}