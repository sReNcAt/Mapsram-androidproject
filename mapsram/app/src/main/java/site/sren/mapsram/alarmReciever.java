package site.sren.mapsram;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class alarmReciever extends BroadcastReceiver {

    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        // intent로부터 전달받은 string
        String get_yout_string = intent.getExtras().getString("state");
        String work = intent.getExtras().getString("work");
        String time = intent.getExtras().getString("time");
        String memo = intent.getExtras().getString("memo");
        String id = intent.getExtras().getString("id");
        // RingtonePlayingService 서비스 intent 생성
        Intent service_intent = new Intent(context, RingtonePlayingService.class);

        // RingtonePlayinService로 extra string값 보내기
        service_intent.putExtra("state", get_yout_string);
        service_intent.putExtra("work", work);
        service_intent.putExtra("time", time);
        service_intent.putExtra("memo", memo);
        service_intent.putExtra("id", id);

        // start the ringtone service

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
            this.context.startForegroundService(service_intent);
        }else{
            this.context.startService(service_intent);
        }
    }
}