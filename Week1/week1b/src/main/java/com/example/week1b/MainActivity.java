package com.example.week1b;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.week1.IMyAidlInterface;
import com.example.week1.Person;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    List<Person> list = new ArrayList<Person>();
    IMyAidlInterface iMyAidlInterface;
    ListView listView;
    SQLiteDatabase db;

    List<Map<String,Object>> maplist;
    SimpleAdapter adapter;
    String[] SQL_COLUMN =new String[]{
            "id",//row_contact_id
            "name",//display_name
            "phone"//data1
    };
    /**
     *
     * 在连接service后实现方法
     *
     */
    private ServiceConnection conn=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
          iMyAidlInterface=IMyAidlInterface.Stub.asInterface(service);
            try {

                list=iMyAidlInterface.getlist();
               // list.get(1).toString();
                Log.e("客户端测试11111",list.get(1).getName());
                //初始化listview
               // listView();
                //初始化DB
                DB();

                listView();
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            iMyAidlInterface=null;
            Log.e("客户端断开","Disconnect");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {



        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        initView();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        //导入菜单布局
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        //创建菜单项的点击事件
        switch (item.getItemId()) {
            case R.id.mune_enter:
                Toast.makeText(this, "新建联系人成功", Toast.LENGTH_SHORT).show();
                InsertDialog();

                break;
            case R.id.mune_setting:
                listView();
                Toast.makeText(this, "重新渲染了ListView", Toast.LENGTH_SHORT).show();

                break;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    public void initView(){
        Log.e("启动顺序","Oncreate");

    }

    /**
     * 设置可输入信息的dialog
     */
    public void InsertDialog(){
        LayoutInflater factory = LayoutInflater.from(this);
        final View textEntryView = factory.inflate(R.layout.userpasslayout,null);
        final EditText editTextName = (EditText) textEntryView.findViewById(R.id.editTextName);
        final EditText editTextNumEditText = (EditText)textEntryView.findViewById(R.id.editTextNum);
        AlertDialog.Builder ad1 =new AlertDialog.Builder(this);
        ad1.setTitle("增加联系人:");
        ad1.setIcon(android.R.drawable.ic_dialog_info);
        ad1.setView(textEntryView);
        ad1.setPositiveButton("是",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int i) {
                ContentValues values = new ContentValues();

                values.put("name", editTextName.getText().toString());
                values.put("phone", editTextNumEditText.getText().toString());

                try {
                    db.insert("person", null, values);
                   // listView();
                }finally {
                    Log.e("无需插入","已存在");
                }


            }
        });
        ad1.setNegativeButton("否",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int i) {

            }
        });
        ad1.show();// 显示对话框


            }

    /**
     * 渲染listview
     */
    public void listView(){
            int i=1;
            listView=findViewById(R.id.list_view);
           //为 ListView 的所有 item 注册 ContextMenu
            this.registerForContextMenu(listView);
            maplist=new ArrayList<Map<String,Object>>();
            Cursor cursor = db.query("person",SQL_COLUMN,null,null, null, null, null);
            while(cursor.moveToNext()){
                String id = cursor.getString(cursor.getColumnIndex("id"));
                String name = cursor.getString(cursor.getColumnIndex("name"));
                String phone = cursor.getString(cursor.getColumnIndex("phone"));
                Map<String,Object>map=new HashMap<String,Object>();
                map.put("FakeId",i); i++;
                map.put("ID",id);
                map.put("name",name);
                map.put("phone",phone);
                maplist.add(map);
            }
                adapter=new SimpleAdapter(
                    this,
                    maplist,
                    R.layout.item,
                    new String[]{"FakeId","name","phone"},
                    new int[]{R.id.textView1,R.id.textView2,R.id.textView3});
            listView.setAdapter(adapter);


    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {
        menu.setHeaderTitle("选择操作");
        //menu.add(0,1,Menu.NONE,"修改");
        menu.add(0,1,Menu.NONE,"删除");

    }

    /**
     *
     * 长按删除实现方法，当前删除本地DB数据，可以删除本地通讯录数据，也可以通过AIDL传参数交给应用A来删除
     *
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
           // case 1:

              //  break;
            case 1:
                   //删除列表项...
                int pos=(int)listView.getAdapter().getItemId(menuInfo.position);
                   Map listitem =maplist.get(pos);
                   //String d_id=listitem.get("ID").toString();
                   String d_name=listitem.get("name").toString();
                   //Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
                   // ContentResolver resolver = this.getContentResolver();
                   //根据id删除data中的相应数据
                   // resolver.delete(uri, "display_name=?", new String[]{d_name});
                   // uri = Uri.parse("content://com.android.contacts/data");
                   //resolver.delete(uri, "raw_contact_id=?", new String[]{d_id+""});


                db.delete("person","name=?",new String[]{d_name});
                if(maplist.remove(pos)!=null){//这行代码必须有
                    System.out.println("success");
                }else {
                    System.out.println("failed");
                }
                adapter.notifyDataSetChanged();
                Toast.makeText(getBaseContext(), "删除此项", Toast.LENGTH_SHORT).show();

                break;
            default:
                return super.onContextItemSelected(item);
        }
        return true;
    }


    public void OnClick(View v) throws Exception {
        Intent intent=new Intent();
        intent.setComponent(new ComponentName("com.example.week1","com.example.week1.AIDLService"));
        bindService(intent,conn, Context.BIND_AUTO_CREATE);
        DB();
        listView();



    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unbindService(conn);
    }

    /**
     *
     * 创建数据库，并将从A应用传来的数据插入到数据库
     */
    public void  DB() throws Exception{
        //新建了一个test_db的数据库
        DBHelp dbHelp = new DBHelp(this,"person_db",null,1);
            db = dbHelp.getWritableDatabase();


        for(Person  person  :  list) {
            ContentValues values = new ContentValues();
            values.put("id", person.getId());
            values.put("name", person.getName());
            values.put("phone", person.getPhone());

           try {
               db.insert("person", null, values);
           }finally {
               Log.e("无需插入","已存在");
           }
        }

        //要查询的列名
        String[] SQL_COLUMN =new String[]{
                "name","phone"};
        Log.e("新建数据库","dad");


        //游标读取想要查询的行的集合,并遍历加到listview上
        /*Cursor cursor = db.query("person",SQL_COLUMN,null,null, null, null, null);
        while(cursor.moveToNext()){
            String name = cursor.getString(cursor.getColumnIndex("name"));
            textview_data = textview_data + "\n" + name;
        }*/



        //crud
       /* switch (view.getId()){
            case R.id.insert_button:
                ContentValues values = new ContentValues();
                values.put("name",insert_data);
                db.insert("user",null,values);
                break;
            case R.id.clear_insert_button:
                insert_text.setText("");
                break;
            case R.id.delete_button:
                db.delete("user","name=?",new String[]{delete_data});
                break;
            case R.id.clear_delete_button:
                delete_text.setText("");
                break;
            //更新数据按钮
            case R.id.update_button:
                ContentValues values2 = new ContentValues();
                values2.put("name", update_after_data);
                db.update("user", values2, "name = ?", new String[]{update_before_data});
                break;
            //更新数据按钮后面的清除按钮
            case R.id.clear_update_button:
                update_before_text.setText("");
                update_after_text.setText("");
                break;

            //查询全部按钮
            case R.id.query:
                //创建游标对象
                Cursor cursor = db.query("user", new String[]{"name"}, null, null, null, null, null);
                //利用游标遍历所有数据对象
                //为了显示全部，把所有对象连接起来，放到TextView中
                String textview_data = "";
                while(cursor.moveToNext()){
                    String name = cursor.getString(cursor.getColumnIndex("name"));
                    textview_data = textview_data + "\n" + name;
                }
                textView.setText(textview_data);
                break;
            //查询全部按钮下面的清除查询按钮
            case R.id.clear_query:
                textView.setText("");
                textView.setHint("查询内容为空");
                break;

            default:
                break;
        }*/
    }
}