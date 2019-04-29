package site.sren.mapsram;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {


    public static int RENEW_GPS = 1;
    public static int SEND_PRINT = 2;
    public static int STOP_GPS = 3;
    public static TextView test_text;
    final static ArrayList<String> items = new ArrayList<String>() ;
    private SQLiteHelper helper;
    String dbName = "alram.db";
    int dbVersion = 1;
    private SQLiteDatabase db;
    String tag = "SQLite";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_FINE_LOCATION  },
                    0 );
        }
        test_text = (TextView)findViewById(R.id.test_text);
        //Service 객체 생성
        GPSTracker.isGPSEnabled = true;
        Intent serviceIntent = new Intent(getApplicationContext(),BackgroundService.class);
        serviceIntent.putExtra("count",1);
        startService(serviceIntent);
        //SQLite 객체 생성
        helper = new SQLiteHelper(this,dbName,null,dbVersion);
        try {
            db = helper.getWritableDatabase(); // 읽고 쓸수 있는 DB
            //db = helper.getReadableDatabase(); // 읽기 전용 DB select문
        } catch (SQLiteException e) {
            e.printStackTrace();
            Log.e(tag, e.getMessage());
            Log.e(tag, "데이터베이스를 얻어올 수 없음");
            finish(); // 액티비티 종료
        }

        // ArrayAdapter 생성. 아이템 View를 선택(single choice)가능하도록 만듦.
        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_single_choice, items) ;

        // listview 생성 및 adapter 지정.
        final ListView listview = (ListView) findViewById(R.id.listview1) ;
        listview.setAdapter(adapter) ;

        /*
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */
    }

    //1번 버튼
    public void mOnPopupClick(View v){
        //팝업(액티비티) 호출
        Intent intent = new Intent(this, addPopupActivity.class);
        intent.putExtra("data", "Test Popup");
        startActivityForResult(intent, 1);
    }

    //2번 버튼
    public void mInsertClick(View v){
        Log.d(tag, "insert 클릭");
        SQLite_insert();
        BackgroundService.gps = new GPSTracker(MainActivity.this,BackgroundService.mHandler);
    }

    //3번 버튼
    public void mSelectClick(View v){
        Log.d(tag, "select 클릭");
        SQLite_select();
        Message msg = BackgroundService.mHandler.obtainMessage();
        // 메시지 ID 설정
        msg.what = MainActivity.STOP_GPS;
        // 메시지 정보 설정3 (Object 형식)
        //String hi = new String("Count Thared 가 동작하고 있습니다.");
        //msg.obj = hi;
        BackgroundService.mHandler.sendMessage(msg);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==1){
            if(resultCode==RESULT_OK){
                //데이터 받기
                String result = data.getStringExtra("result");
                Log.d("result",result);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    // SQLite 제어를 위한 CRUD
    void SQLite_delete() {
        db.execSQL("delete from alram where id=1;");
        Log.d(tag, "delete 완료");
    }

    void SQLite_update() {
        db.execSQL("update alram set id='0' where id=1;");
        Log.d(tag, "update 완료");
    }

    void SQLite_select() {
        Cursor c = db.rawQuery("select * from alram;", null);
        while(c.moveToNext()) {
            int id = c.getInt(0);
            String name = c.getString(1);
            Log.d(tag,"id:"+id+",name:"+name);
        }
    }

    void SQLite_insert () {
        db.execSQL("insert into alram (name) values('test');");
        Log.d(tag, "insert 성공");
    }

}
