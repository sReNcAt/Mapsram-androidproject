package site.sren.mapsram;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TimePicker;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class selectAreaActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private LatLng select_LatLng;
    private Button current;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //타이틀바 없애기
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.select_area);

        //데이터 가져오기
        Intent intent = getIntent();
        String data = intent.getStringExtra("data");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.select_map);
        mapFragment.getMapAsync(this);

        Button Ok_btn = findViewById(R.id.areaOK);
        Button Can_btn = findViewById(R.id.areaCan);
        current = findViewById(R.id.current_location);

        Ok_btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Log.d("data",select_LatLng.latitude+" "+select_LatLng.longitude);
                Intent intent = new Intent();
                intent.putExtra("position", select_LatLng);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        Can_btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("result", "cancle");
                setResult(RESULT_CANCELED,intent);
                finish();
            }
        });

        current.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("map","move current location");
                if(GPSTracker.last_location!=null) {
                    MainActivity.mMap.clear();
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(GPSTracker.last_location, 15));
                }
            }
        });
    }

    public void onStop () {
        //do your stuff here
        super.onStop();
        Log.d("data",select_LatLng.latitude+" "+select_LatLng.longitude);
        Intent intent = new Intent();
        intent.putExtra("position", select_LatLng);
        setResult(RESULT_OK, intent);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.moveCamera(CameraUpdateFactory
                .newLatLngZoom(GPSTracker.last_location, 15));
        // Use a custom info window adapter to handle multiple lines of text in the
        // info window contents.
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            // Return null here, so that getInfoContents() is called next.
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                return null;
            }
        });

        // 맵 터치 이벤트 구현 //
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener(){
            @Override
            public void onMapClick(LatLng point) {
                mMap.clear();
                MarkerOptions mOptions = new MarkerOptions();
                // 마커 타이틀
                mOptions.title("마커 좌표");
                select_LatLng = point;
                Double latitude = point.latitude; // 위도
                Double longitude = point.longitude; // 경도
                // 마커의 스니펫(간단한 텍스트) 설정
                mOptions.snippet(latitude.toString() + ", " + longitude.toString());
                // LatLng: 위도 경도 쌍을 나타냄
                mOptions.position(new LatLng(latitude, longitude));
                // 마커(핀) 추가
                mMap.addMarker(mOptions);
            }
        });
        ////////////////////
    }
}
