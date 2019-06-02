package site.sren.mapsram;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import site.sren.mapsram.constants;

/**
 * Created by Ch on 2016-05-13.
 */
public class alarmBroadcastReciever extends BroadcastReceiver {
    public static boolean isLaunched = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        isLaunched = true;

        // 현재 시간을 화면에 보낸다.
        saveTime(context);
    }

    private void saveTime(Context context) {
        long currentTime = System.currentTimeMillis();
        Intent intent = new Intent(constants.INTENTFILTER_BROADCAST_TIMER);
        intent.putExtra(constants.KEY_DEFAULT, currentTime);
        context.sendBroadcast(intent);
    }
}