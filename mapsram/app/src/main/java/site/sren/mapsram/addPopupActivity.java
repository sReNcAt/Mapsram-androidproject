package site.sren.mapsram;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;

public class addPopupActivity extends Activity {
    TextView txtText;
    TimePicker mTimePicker;
    CheckBox checkBox1;
    CheckBox checkBox2;
    EditText areaEdit;
    EditText workEdit;

    LatLng select_position;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_add);

        //필드 변수 선언
        mTimePicker = (TimePicker) findViewById(R.id.timePicker);
        checkBox1 = (CheckBox) findViewById(R.id.check1);
        checkBox2 = (CheckBox) findViewById(R.id.check2);
        areaEdit = findViewById(R.id.areaEdit);
        workEdit = findViewById(R.id.workEdit);

        //데이터 가져오기
        Intent intent = getIntent();
        String data = intent.getStringExtra("data");
        //txtText.setText(data);

        //체크박스 클릭시 이벤트
        checkBox2.setOnClickListener(new CheckBox.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), selectAreaActivity.class);
                startActivityForResult(intent, 1);
            }
        }) ;
    }
    //저장 버튼 클릭
    public void mOnSave(View v){
        //데이터 전달하기
        Intent intent = new Intent();
        intent.putExtra("result", "Save Popup");
        setResult(RESULT_OK, intent);


        //타임피커에서 시,분 가져오기
        Calendar b = Calendar.getInstance();
        int hour, min;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M){
            hour = mTimePicker.getHour();
            min = mTimePicker.getMinute();
        } else {
            hour = mTimePicker.getCurrentHour();
            min = mTimePicker.getCurrentMinute();
        }

        Log.d("field","장소 : "+areaEdit.getText()+"\n"+
                "시 : "+hour+"\n"+
                "분 : "+min+"\n"+
                "할일 : "+workEdit.getText()+"\n"+
                "체크1 : "+checkBox1.isChecked()+"\n"+
                "체크2 : "+checkBox2.isChecked()+"\n");
        if(select_position!=null){
            Log.d("field",select_position.latitude+" "+select_position.longitude);
        }else{
            Log.d("field","null");
        }

        if (checkBox1.isChecked()) {
            // TODO : CheckBox is checked.
        } else {
            // TODO : CheckBox is unchecked.
        }
        //Log.d
        //액티비티(팝업) 닫기
        finish();
    }
    //취소 버튼 클릭
    public void mOnClose(View v){
        //데이터 전달하기
        Intent intent = new Intent();
        intent.putExtra("result", "Close Popup");
        setResult(RESULT_OK, intent);

        //액티비티(팝업) 닫기
        finish();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case 90:
                if (resultCode == RESULT_OK) {
                    select_position = data.getExtras().getParcelable("position");
                    //Log.d("field",select_position.latitude+" "+select_position.latitude);
                }
                break;
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥레이어 클릭시 안닫히게
        if(event.getAction()==MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        //안드로이드 백버튼 막기
        return;
    }

}
