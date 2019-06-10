package site.sren.mapsram;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class alarmViewActivity extends Activity {
    private static  SQLiteHelper helper;
    static String dbName = "alram.db";
    int dbVersion = MainActivity.dbVersion;
    static private SQLiteDatabase db;
    static String tag = "SQLite";
    int alarm_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.click_item);

        TextView alarm_work = findViewById(R.id.alarm_work_click);
        TextView alarm_time = findViewById(R.id.alarm_time_click);
        TextView alarm_memo = findViewById(R.id.alarm_memo_click);
        Button alarm_ok = findViewById(R.id.alarm_ok_click);
        Button alarm_delete = findViewById(R.id.alarm_delete_click);

        Intent data = getIntent();

        //데이터 받기
        String work = data.getExtras().getString("work");
        String time = data.getExtras().getString("time");
        String memo = data.getExtras().getString("memo");
        alarm_id = Integer.parseInt(data.getExtras().getString("id"));


        alarm_work.setText(work);
        alarm_time.setText(time);
        alarm_memo.setText(memo);

        alarm_ok.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        alarm_delete.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.d("알람",alarm_id+"번째");
                SQLite_delete(alarm_id);
                Toast.makeText(alarmViewActivity.this, "삭제 완료!", Toast.LENGTH_SHORT).show();
                MainActivity.refresh();
                finish();
            }
        });


    }
    void SQLite_delete(int id) {
        helper = new SQLiteHelper(this,dbName,null,dbVersion);
        try {
            db = helper.getWritableDatabase(); // 읽고 쓸수 있는 DB
            db.execSQL("delete from alram where id="+id+";");
            db.close();
            Log.d(tag, "delete 완료");
            //db = helper.getReadableDatabase(); // 읽기 전용 DB select문
        } catch (SQLiteException e) {

        }
    }
}