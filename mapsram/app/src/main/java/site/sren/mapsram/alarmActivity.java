package site.sren.mapsram;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class alarmActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.call_alarm);
        TextView alarm_work = findViewById(R.id.alarm_work);
        TextView alarm_time = findViewById(R.id.alarm_time);
        TextView alarm_memo = findViewById(R.id.alarm_memo);
        Button alarm_ok = findViewById(R.id.alarm_ok);

        Intent data = getIntent();

        //데이터 받기
        String work = data.getExtras().getString("work");
        String time = data.getExtras().getString("time");
        String memo = data.getExtras().getString("memo");

        alarm_work.setText(work);
        alarm_time.setText(time);
        alarm_memo.setText(memo);

        alarm_ok.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}