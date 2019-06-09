package site.sren.mapsram;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Calendar;


public class BackgroundService extends Service {
    public MainActivity activity;
    private IBinder mIBinder = new MyBinder();
    public static GPSTracker gps = null;
    public static Handler mHandler;
    public static int count = 0;
    Intent my_intent;
    private CountThread mCountThread = null;
    private double before_lat = 0.00;
    private double before_lon = 0.00;
    private int current_minute = 0;
    DistanceCalculator calc = new DistanceCalculator();

    public int var = 777; //서비스바인딩의 예시로 출력할 값
    private BackgroundService context;
    private AlarmManager alarm_manager;
    private PendingIntent pendingIntent;
    private static  SQLiteHelper helper;
    static String dbName = "alram.db";
    static int dbVersion = 4;
    static private SQLiteDatabase db;
    static String tag = "SQLite";

    class MyBinder extends Binder{
        BackgroundService getService(){
            return BackgroundService.this;
        }
    }

    @Nullable
    public void onCreate(Context context) {
        Log.e("Service", "onCreate()");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //SQLite 객체 생성
        helper = new SQLiteHelper(this,dbName,null,dbVersion);
        try {
            db = helper.getWritableDatabase(); // 읽고 쓸수 있는 DB
            //db = helper.getReadableDatabase(); // 읽기 전용 DB select문
        } catch (SQLiteException e) {

        }
        Log.e("Service", "onStartCommand()");
        mCountThread = new CountThread();
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                //super.handleMessage(msg);
                if(msg.what==MainActivity.RENEW_GPS){
                    if(gps == null) {

                    }else{
                        gps.Update();
                        Log.d("GPS_STATUS", "gps update");
                    }
                }
                if(msg.what==MainActivity.SEND_PRINT){
                    count++;
                    Calendar currentTime = Calendar.getInstance();
                    int hour = currentTime.get(Calendar.HOUR_OF_DAY);
                    int minute = currentTime.get(Calendar.MINUTE);
                    int second = currentTime.get(Calendar.SECOND);
                    int distance = 0;
                    double current_lat = Double.parseDouble((msg.obj+"\n").split("/")[0]);
                    double current_lon = Double.parseDouble((msg.obj+"\n").split("/")[1]);
                    if(current_minute!=minute) {
                        current_minute=minute;
                        SQLite_select(hour,current_lat,current_lon);
                    }
                    //Log.d("GPS_VALUE", msg.obj + "\n");
                    if(before_lat!=0.00 || before_lon !=0.00){
                        distance = (int)(calc.distance(before_lat, before_lon, current_lat, current_lon, "M"));
                        //여기서확인
                        Log.d("time",hour+"시 "+minute+"분");
                        Log.d("distance",distance+"미터");
                    }else {
                        before_lat = current_lat;
                        before_lon = current_lon;
                    }
                    MainActivity.test_text.setText(current_lat+"/"+current_lon+"\n"+distance+"m\n"+hour+"시 "+minute+"분 "+second+"초");
                }
                if(msg.what==MainActivity.STOP_GPS){
                    mCountThread.stopThread();
                    Log.d("GPS_STATUS", "Thread 정지");
                }
            }
        };
        mCountThread.start();
        BackgroundService.gps = new GPSTracker(BackgroundService.this, BackgroundService.mHandler);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e("Service", "onBind()");
        return mIBinder;
    }

    @Override
    public void onDestroy() {
        Log.e("Service", "onDestroy()");
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e("Service", "onUnbind()");
        return super.onUnbind(intent);
    }

    // Thread 클래스
    class CountThread extends Thread implements Runnable {

        private boolean isPlay = false;

        public CountThread() {
            isPlay = true;
        }

        public void isThreadState(boolean isPlay) {
            this.isPlay = isPlay;
        }

        public void stopThread() {
            isPlay = !isPlay;
        }

        public void startThread(){
            isPlay = true;
        }
        @Override
        public void run() {
            super.run();

            int i = 0;

            while (isPlay) {
                i++;
                // 메시지 얻어오기
                Message msg = mHandler.obtainMessage();

                // 메시지 ID 설정
                msg.what = MainActivity.RENEW_GPS;

                // 메시지 정보 설정3 (Object 형식)
                String hi = new String("Count Thared 가 동작하고 있습니다.");
                //msg.obj = hi;

                mHandler.sendMessage(msg);

                // 1초 딜레이
                try {
                    Thread.sleep(1000);
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    void SQLite_select(int hours,double lat,double lon) {
        helper = new SQLiteHelper(this,dbName,null,dbVersion);
        try {
            db = helper.getWritableDatabase(); // 읽고 쓸수 있는 DB
            //db = helper.getReadableDatabase(); // 읽기 전용 DB select문
        } catch (SQLiteException e) {

        }
        Calendar currentTime = Calendar.getInstance();
        int c_year = currentTime.get(Calendar.YEAR);
        int c_month = currentTime.get(Calendar.MONTH);
        int c_day = currentTime.get(Calendar.DAY_OF_MONTH);
        int c_hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int c_minute = currentTime.get(Calendar.MINUTE);
        int c_second = currentTime.get(Calendar.SECOND);
        Cursor c = db.rawQuery("select * from alram where status = '0' and hour = "+hours+";", null);
        while(c.moveToNext()) {
            int id = c.getInt(0);
            String work = c.getString(1);
            String memo = c.getString(2);
            int type = c.getInt(3);
            int hour = c.getInt(4);
            int minutes = c.getInt(5);
            String lati = c.getString(6);
            String longi = c.getString(7);
            int year = c.getInt(8);
            int month = c.getInt(9);
            int day = c.getInt(10);

            if(c_minute<=minutes){
                int distance = (int)(calc.distance(Double.parseDouble(lati), Double.parseDouble(longi), lat, lon, "M"));
                Log.d("알람",lati+" : "+longi);
                Log.d("알람","거리 : "+distance+"\n");
                if(minutes-c_minute==1 && (distance<=500 || (Double.parseDouble(lati)==0.0 && Double.parseDouble(longi) ==0.0) )){
                    alarmSetting(hours,minutes,work,memo,year,month,day,id);
                }
            }
        }
    }

    public void alarmSetting(int hour,int minute,String work,String memo,int year,int month,int day,int id){

        // calendar에 시간 셋팅
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);

        my_intent = new Intent(BackgroundService.this, alarmReciever.class);
        alarm_manager = (AlarmManager) getSystemService(ALARM_SERVICE);

        Log.d("알람",hour+"시"+minute+"분에 알람설정 완료");
        // reveiver에 string 값 넘겨주기
        my_intent.putExtra("state","alarm on");
        my_intent.putExtra("id",id+"");
        my_intent.putExtra("work",work);
        my_intent.putExtra("time",year+"년 "+month+"월 "+day+"일 "+hour+"시 "+minute+"분");
        my_intent.putExtra("memo",memo);

        pendingIntent = PendingIntent.getBroadcast(BackgroundService.this, 0, my_intent,PendingIntent.FLAG_UPDATE_CURRENT);

        // 알람셋팅
        alarm_manager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),pendingIntent);
    }

    public void alarmClear(){
        // 알람매니저 취소
        alarm_manager.cancel(pendingIntent);
        my_intent.putExtra("state","alarm off");
        // 알람취소
        sendBroadcast(my_intent);
    }
}
