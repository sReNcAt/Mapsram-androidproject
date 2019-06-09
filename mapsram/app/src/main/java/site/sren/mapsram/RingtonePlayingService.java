package site.sren.mapsram;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class RingtonePlayingService extends Service {

    MediaPlayer mediaPlayer;
    int startId;
    boolean isRunning;
    String CHANNEL_ID = "default";
    NotificationChannel channel;
    Notification notification;

    private static  SQLiteHelper helper;
    static String dbName = "alram.db";
    static int dbVersion = 3;
    static private SQLiteDatabase db;
    static String tag = "SQLite";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= 26) {
            channel = new NotificationChannel(CHANNEL_ID,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
            helper = new SQLiteHelper(this,dbName,null,dbVersion);
            try {
                db = helper.getWritableDatabase(); // 읽고 쓸수 있는 DB
                //db = helper.getReadableDatabase(); // 읽기 전용 DB select문
            } catch (SQLiteException e) {

            }
            notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("알람시작")
                    .setContentText("알람음이 재생됩니다.")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setAutoCancel(true) // 알림 터치시 반응 후 삭제
                    .build();

            //startForeground(1, notification);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String getState = intent.getExtras().getString("state");
        String work = intent.getExtras().getString("work");
        String time = intent.getExtras().getString("time");
        String memo = intent.getExtras().getString("memo");
        String id = intent.getExtras().getString("id");

        notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("알람!!")
                .setContentText("알람음이 재생됩니다.")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true) // 알림 터치시 반응 후 삭제
                .build();

        startForeground(1, notification);

        Intent intent1 = new Intent(this,alarmActivity.class);
        intent1.putExtra("work",work);
        intent1.putExtra("time",time);
        intent1.putExtra("memo",memo);
        SQLite_update(Integer.parseInt(id));
        startActivity(intent1.addFlags(FLAG_ACTIVITY_NEW_TASK));

        if(1==1){
            return START_NOT_STICKY;
        }

        assert getState != null;
        switch (getState) {
            case "alarm on":
                startId = 1;
                break;
            case "alarm off":
                startId = 0;
                break;
            default:
                startId = 0;
                break;
        }

        // 알람음 재생 X , 알람음 시작 클릭
        if(!this.isRunning && startId == 1) {

            mediaPlayer = MediaPlayer.create(this,R.raw.alarm);
            mediaPlayer.start();

            this.isRunning = true;
            this.startId = 0;
            notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("알람!!")
                    .setContentText("알람음이 재생됩니다.")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setAutoCancel(true) // 알림 터치시 반응 후 삭제
                    .build();

            startForeground(1, notification);

        }




        // 알람음 재생 O , 알람음 종료 버튼 클릭
        else if(this.isRunning && startId == 0) {

            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();

            stopForeground(true);
            this.isRunning = false;
            this.startId = 0;
        }

        return START_NOT_STICKY;
    }

    void SQLite_update(int id) {
        helper = new SQLiteHelper(this,dbName,null,dbVersion);
        try {
            db = helper.getWritableDatabase(); // 읽고 쓸수 있는 DB
            db.execSQL("update alram set status='1' where id="+id+";");
            Log.d(tag, "update 완료");
            //db = helper.getReadableDatabase(); // 읽기 전용 DB select문
        } catch (SQLiteException e) {

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d("onDestory() 실행", "서비스 파괴");

    }
}
