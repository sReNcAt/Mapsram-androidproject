package site.sren.mapsram;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

import static site.sren.mapsram.MainActivity.SQLite_select_all;

public class alarmService extends Service {

    private alarmService context;
    Intent my_intent;
    PendingIntent pendingIntent;
    AlarmManager alarm_manager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        startForegroundService();
    }

    void startForegroundService() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.activity_main);

        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "mapsram_service_channel";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "MapsRam Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                    .createNotificationChannel(channel);

            builder = new Builder(this, CHANNEL_ID);
        } else {
            builder = new Builder(this);
        }
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContent(remoteViews)
                .setContentIntent(pendingIntent);

        startForeground(1, builder.build());
        Thread bt = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        Map<Integer, String[]> get_alarm = SQLite_select_all(false);
                        if(get_alarm.size()>0){
                            Log.d("service",get_alarm.size()+"");
                            for (int i=0; i<get_alarm.size(); i++){
                                String[] item = get_alarm.get(i);
                                int hour = Integer.parseInt(item[4]);
                                int minutes =Integer.parseInt(item[5]);

                                Date date = new Date();
                                Calendar calendar = GregorianCalendar.getInstance();
                                calendar.setTime(date);
                                calendar.get(Calendar.HOUR_OF_DAY); // gets hour in 24h format
                                //calendar.get(Calendar.HOUR);        // gets hour in 12h format
                                calendar.get(Calendar.MONTH);

                                int current_hour = calendar.get(Calendar.HOUR_OF_DAY);
                                int current_minutes = calendar.get(Calendar.MONTH);
                                
                                if(hour == current_hour && minutes == current_minutes){
                                    alarmSetting(hour,minutes);
                                    Thread.sleep(1000);
                                    alarmClear();
                                }

                                if(GPSTracker.last_location!=null){

                                }
                            }
                        }else{
                            Log.d("service","아이템이 없습니다.");
                        }
                        Thread.sleep(1000);
                    }catch(InterruptedException e){
                        e.printStackTrace();
                    }
                    if(1==2){
                        Log.i("background-counter","---백스레드 중지---");
                        break;
                    }
                }
            }
        });
        bt.setName("백그라운드스레드");
        bt.start();
    }

    public void alarmSetting(int hour,int minute){

        // 시간 가져옴
        hour = 9;
        minute = 10;

        // calendar에 시간 셋팅
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        Log.d("alarm",hour+"시"+minute+"분");
        this.context = this;
        my_intent = new Intent(this.context, alarmReciever.class);
        alarm_manager = (AlarmManager) getSystemService(ALARM_SERVICE);

        // reveiver에 string 값 넘겨주기
        my_intent.putExtra("state","alarm on");

        pendingIntent = PendingIntent.getBroadcast(alarmService.this, 0, my_intent,PendingIntent.FLAG_UPDATE_CURRENT);

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