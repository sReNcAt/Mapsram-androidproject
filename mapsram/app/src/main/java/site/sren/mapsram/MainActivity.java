package site.sren.mapsram;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
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
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    AlarmManager alarm_manager;
    public static GoogleMap mMap;
    public static boolean isbtn2 = false;
    public static int RENEW_GPS = 1;
    public static int SEND_PRINT = 2;
    public static int STOP_GPS = 3;
    public static TextView test_text;
    final static ArrayList<String> items = new ArrayList<String>() ;

    private static  SQLiteHelper helper;
    static String dbName = "alram.db";
    static int dbVersion = 4;
    static private SQLiteDatabase db;
    static String tag = "SQLite";

    private ArrayList<String> arrayList;
    private static ListViewAdaptar adapter;
    private static ListView listview;
    // 알람리시버 intent 생성
    Intent my_intent;
    final Calendar calendar = Calendar.getInstance();
    PendingIntent pendingIntent;
    private Context context = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        /*
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        */
        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( this, new String[] {  Manifest.permission.ACCESS_FINE_LOCATION  },
                    0 );
        }
        // Build the map.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        getSupportFragmentManager().beginTransaction().hide(mapFragment).commit();
        arrayList = new ArrayList<String>();
        adapter = new ListViewAdaptar();
        //adapter = new ListViewAdaptar();

        this.context = this;

        // listview 생성 및 adapter 지정.
        listview = (ListView) findViewById(R.id.itemlist) ;
        listview.setAdapter(adapter) ;
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListViewItem aa = (ListViewItem) adapter.getItem(position);
                Intent intent = new Intent(getApplicationContext(),alarmActivity.class);
                intent.putExtra("work",aa.getWork());
                String time = aa.getYear()+"년 "+aa.getMonth()+"월 "+aa.getDay()+"일 "+aa.getHour()+"시 "+aa.getMinutes()+"분";
                intent.putExtra("time",time);
                intent.putExtra("memo",aa.getMemo());
                startActivity(intent);
                //SQLite_update(Integer.parseInt(aa.getId()));
            }
        });
        alarm_manager = (AlarmManager) getSystemService(ALARM_SERVICE);

        my_intent = new Intent(this.context, alarmReciever.class);


        Display mDisplay = this.getWindowManager().getDefaultDisplay();
        int height = mDisplay.getHeight();
        ViewGroup.LayoutParams params = mapFragment.getView().getLayoutParams();

        params.height = (int)(height/2.5);

        mapFragment.getView().setLayoutParams(params);
        //listview.setLayoutParams(params);
        mapFragment.getMapAsync(this);
        test_text = (TextView)findViewById(R.id.test_text);

        //Service 객체 생성
        GPSTracker.isGPSEnabled = true;

        //서비스 객체 실행 확인
        if(!isServiceRunningCheck()) {
            Intent serviceIntent = new Intent(getApplicationContext(), BackgroundService.class);
            serviceIntent.putExtra("count", 1);
            startService(serviceIntent);
        }

        //SQLite 객체 생성
        helper = new SQLiteHelper(this,dbName,null,dbVersion);
        try {
            db = helper.getWritableDatabase(); // 읽고 쓸수 있는 DB
            SQLite_select_all(true);

            //db = helper.getReadableDatabase(); // 읽기 전용 DB select문
        } catch (SQLiteException e) {
            e.printStackTrace();
            Log.e(tag, e.getMessage());
            Log.e(tag, "데이터베이스를 얻어올 수 없음");
            finish(); // 액티비티 종료
        }

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
        //gps 서비스 시작
        Button current = findViewById(R.id.current_location_main);
        current.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("map","move current location");
                if(GPSTracker.last_location!=null) {
                    MainActivity.mMap.clear();
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(GPSTracker.last_location, 15));
                }
            }
        });

        //포그라운드 서비스 시작
        /*
        Intent intent = new Intent(getApplicationContext(), alarmService.class);
        if (Build.VERSION.SDK_INT >= 26) {
            getApplicationContext().startForegroundService(intent);
        }
        else {
            getApplicationContext().startService(intent);
        }
        */
    }

    //서비스 중복실행 방지
    public boolean isServiceRunningCheck() {
        ActivityManager manager = (ActivityManager) this.getSystemService(Activity.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("site.sren.mapsram.BackgroundService".equals(service.service.getClassName())) {
                Log.e("service","true");
                return true;
            }
        }
        Log.e("service","false");
        return false;
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
        //SQLite_insert();
    }

    //3번 버튼
    public void mSelectClick(View v){
        Log.d(tag, "select 클릭");
        SQLite_select();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==1){
            if(resultCode==RESULT_OK){
                //데이터 받기
                String result = data.getStringExtra("result");
                Log.d("result",result);
                if(result.length()>0) { // 한글자 이상 입력 된다면 추가
                    String inputStr = result;
                    String[] array = inputStr.split("&");
                    Log.d("db",inputStr);
                    Log.d("db",array[0]);
                    Log.d("db",array[1]);
                    Log.d("db",array[2]);
                    Log.d("db",array[3]);
                    Log.d("db",array[4]);
                    Log.d("db",array[5]);
                    Log.d("db",array[6]);

                    SQLite_insert(array[0],array[1],Integer.parseInt(array[2]),Integer.parseInt(array[3]),Integer.parseInt(array[4]),
                            (array[5]),(array[6]),Integer.parseInt(array[7]),Integer.parseInt(array[8]),Integer.parseInt(array[8]));
                    //adapter.clearItem();
                    Log.d("결과",result);
                    //adapter.addItem(array[0],array[1],Integer.parseInt(array[2]),Integer.parseInt(array[3]),Integer.parseInt(array[4]),.parseDouble(array[5]),Double.parseDouble(array[6]),Integer.parseInt(array[7]),Integer.parseInt(array[8]),Integer.parseInt(array[8]));
                    //adapter.notifyDataSetChanged();
                    final Handler handler = new Handler()
                    {
                        public void handleMessage(Message msg) {
                            adapter = new ListViewAdaptar();
                            listview.setAdapter(adapter) ;
                            SQLite_select_all(true);
                        }
                    };
                    new Thread()
                    {
                        public void run()
                        {
                            Message msg = handler.obtainMessage();
                            handler.sendMessage(msg);
                        }
                    }.start();
                    //arrayList.add(inputStr);
                }
            }
        }
    }

    public void alarmSetting(){

        // 시간 가져옴
        int hour = 9;
        int minute = 10;

        // calendar에 시간 셋팅
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);


        Toast.makeText(MainActivity.this,"Alarm 예정 " + hour + "시 " + minute + "분",Toast.LENGTH_SHORT).show();

        // reveiver에 string 값 넘겨주기
        my_intent.putExtra("state","alarm on");

        pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, my_intent,PendingIntent.FLAG_UPDATE_CURRENT);

        // 알람셋팅
        alarm_manager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),pendingIntent);
    }

    public void alarmClear(){
        Toast.makeText(MainActivity.this,"Alarm 종료",Toast.LENGTH_SHORT).show();
        // 알람매니저 취소
        alarm_manager.cancel(pendingIntent);
        my_intent.putExtra("state","alarm off");
        // 알람취소
        sendBroadcast(my_intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
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

    void SQLite_update(int id) {
        db.execSQL("update alram set status='0' where id="+id+";");
        Log.d(tag, "update 완료");
    }

    static void SQLite_select() {
        Cursor c = db.rawQuery("select * from alram where status = '0';", null);
        while(c.moveToNext()) {
            int id = c.getInt(0);
            String name = c.getString(1);
            Log.d(tag,"id:"+id+",name:"+name);
        }
    }

    static Map<Integer, String[]> SQLite_select_all(boolean isMain) {
        Cursor c = db.rawQuery("select * from alram where status = '0' order by id;", null);
        Map<Integer, String[]> item_array = new HashMap<>();

        int i=1;
        while(c.moveToNext()) {
            int id = c.getInt(0);
            String name = c.getString(1);
            String memo = c.getString(2);
            int type = c.getInt(3);
            int hour = c.getInt(4);
            int minutes = c.getInt(5);
            String lati = c.getString(6);
            String longi = c.getString(7);
            int year = c.getInt(8);
            int month = c.getInt(9);
            int day = c.getInt(10);
            String valueArr[];
            item_array.put(i,new String[]{String.valueOf(id),name,memo, String.valueOf(type), String.valueOf(hour), String.valueOf(minutes),lati,longi});
            if(isMain) {
                //adapter.notifyDataSetChanged();
                adapter.addItem(name, memo, type, hour, minutes, Double.parseDouble(lati), Double.parseDouble(longi),year,month,day);
            }
            i++;
        }
        return item_array;
    }
    public void SQLite_insert (String work,String memo,int type,int hour,int minutes,String lati,String longi,int year,int month,int day) {
        db.execSQL("insert into alram (status,work,memo,type,hour,minutes,lati,longi,year,month,day) values('0',\""+
                work+"\",\""+memo+"\","+type+","+hour+","+minutes+",'"+lati+"','"+longi+"',"+year+","+month+","+day+");");
        Log.d(tag, "insert 성공");

    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.moveCamera(CameraUpdateFactory
                .newLatLngZoom(GPSTracker.last_location, 15));
        // Use a custom info window adapter to handle multiple lines of text in the
        // info window contents.
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            // Return null here, so that getInfoContents() is called next.
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                return null;
            }
        });

        // 맵 터치 이벤트 구현 //
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener(){
            @Override
            public void onMapClick(LatLng point) {
                mMap.clear();
                MarkerOptions mOptions = new MarkerOptions();
                // 마커 타이틀
                mOptions.title("마커 좌표");
                Double latitude = point.latitude; // 위도
                Double longitude = point.longitude; // 경도
                // 마커의 스니펫(간단한 텍스트) 설정
                mOptions.snippet(latitude.toString() + ", " + longitude.toString());
                // LatLng: 위도 경도 쌍을 나타냄
                mOptions.position(new LatLng(latitude, longitude));
                // 마커(핀) 추가
                mMap.addMarker(mOptions);
            }
        });
        ////////////////////
    }


}
