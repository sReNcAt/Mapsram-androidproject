package site.sren.mapsram;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;
import java.util.Calendar;


public class BackgroundService extends Service {
    private IBinder mIBinder = new MyBinder();
    public static GPSTracker gps = null;
    public static Handler mHandler;
    public static int count = 0;

    private CountThread mCountThread = null;
    private double before_lat = 0.00;
    private double before_lon = 0.00;

    DistanceCalculator calc = new DistanceCalculator();

    public int var = 777; //서비스바인딩의 예시로 출력할 값

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

                    Log.d("GPS_VALUE", msg.obj + "\n");
                    if(before_lat!=0.00 || before_lon !=0.00){
                        distance = (int)(calc.distance(before_lat, before_lon, current_lat, current_lon, "M"));
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
}
