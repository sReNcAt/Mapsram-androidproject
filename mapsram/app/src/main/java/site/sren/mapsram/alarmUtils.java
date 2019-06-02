package site.sren.mapsram;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import site.sren.mapsram.AlarmOneMinuteBroadcastReceiver;

public class alarmUtils {
    private final static int FIVE_SECOND = 5 * 1000;
    private final static int ONE_MINUES = 60 * 1000;

    private static alarmUtils _instance;

    public static alarmUtils getInstance() {
        if (_instance == null) _instance = new alarmUtils();
        return _instance;
    }

    public void startOneMinuteAlram(Context context) {

        // AlarmOneMinuteBroadcastReceiver 초기화
        Intent alarmIntent = new Intent(context, AlarmOneMinuteBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);

        // 1분뒤에 AlarmOneMinuteBroadcastReceiver 호출 한다.
        startAlram(context, pendingIntent, ONE_MINUES);
    }

    private void startAlram(Context context, PendingIntent pendingIntent, int delay) {

        // AlarmManager 호출
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // 1분뒤에 AlarmOneMinuteBroadcastReceiver 호출 한다.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            manager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay, pendingIntent);
        } else {
            manager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay, pendingIntent);
        }
    }
}