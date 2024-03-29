package site.sren.mapsram;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by xxxx on 2017-02-04.
 */
public class GPSTracker extends Service implements LocationListener {

    public List<Marker> marker_temp_list = new ArrayList<Marker>();
    public static LatLng last_location = new LatLng(0, 0);
    private final Context mContext;

    // flag for GPS status
    public static boolean isGPSEnabled = false;

    // flag for network status
    public static boolean isNetworkEnabled = false;

    // flag for GPS status
    boolean canGetLocation = false;

    Location location; // location
    double latitude; // latitude
    double longitude; // longitude

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    // Declaring a Location Manager
    protected LocationManager locationManager;

    private Handler mHandler;

    public GPSTracker(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
        getLocation();
    }
    public void Update(){
        getLocation();
    }
    public Location getLocation() {

        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( mContext, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            //Log.d("gps","failed GPS Start");
            return null;
        }
        //Log.d("gps","Sucsess GPS Start");
        try {
            locationManager = (LocationManager) mContext
                    .getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                //Log.d("gps","no Network Provider is Enabled");
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    //Log.d("gps","GPS Using Network");
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    //Log.d("Network", "Network");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    //Log.d("gps","GPS Using GPS");
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        //Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                //Log.d("gps","latitude : " + latitude + "\n longitude : " + longitude);
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (location != null) {

            send_handler_location(location);
            //last_location = new LatLng(Math.round(location.getLatitude()*100000)/100000.0, Math.round(location.getLongitude()*100000)/100000.0);
            last_location = new LatLng(location.getLatitude(), location.getLongitude());
            if(marker_temp_list.size()>0) {
                for (Marker marker_temp: marker_temp_list) {
                    marker_temp.remove();
                }
            }else{
                MainActivity.mMap.moveCamera(CameraUpdateFactory
                        .newLatLngZoom(last_location, 15));
            }
            //MainActivity.mMap.clear();
            Marker marker_temp = MainActivity.mMap.addMarker(new MarkerOptions()
                    .title("현재위치")
                    .position(last_location)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                    //.snippet("위치확인 불가"));
            //Log.d("알람",last_location+"");
            marker_temp_list.add(marker_temp);
        }else{
            //Log.d("gps", "null Location");
        }
        return location;
    }

    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app
     *
     * @param location*/
    public void send_handler_location(Location location){
        Message msg = mHandler.obtainMessage();
        msg.what = MainActivity.SEND_PRINT;
        msg.obj = new String(location.getLatitude()+"/"+location.getLongitude());
        mHandler.sendMessage(msg);
    };

    public void stopUsingGPS(){
        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( mContext, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            return;
        }
        if(locationManager != null){
            locationManager.removeUpdates(GPSTracker.this);
        }
    }

    /**
     * Function to get latitude
     * */
    public double getLatitude(){
        if(location != null){
            latitude = location.getLatitude();
        }

        // return latitude
        return latitude;
    }

    /**
     * Function to get longitude
     * */
    public double getLongitude(){
        if(location != null){
            longitude = location.getLongitude();
        }

        // return longitude
        return longitude;
    }

    /**
     * Function to check GPS/wifi enabled
     * @return boolean
     * */
    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    /**
     * Function to show settings alert dialog
     * On pressing Settings button will lauch Settings Options
     * */
    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location != null){
            double latitude= location.getLatitude();
            double longitude = location.getLongitude();
            //Toast.makeText(mContext, "onLocationChanged is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_SHORT).show();
            //sendString("onLocationChanged is - \nLat: " + latitude + "\nLong: " + longitude + " provider:"+location.getProvider()+" mock:"+location.isFromMockProvider());
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        //Toast.makeText(mContext, "onProviderDisabled " + provider, Toast.LENGTH_SHORT).show();
        mHandler.sendEmptyMessage(MainActivity.RENEW_GPS);
        //sendString( "onProviderDisabled " + provider);
    }

    @Override
    public void onProviderEnabled(String provider) {
        //Toast.makeText(mContext, "onProviderEnabled " + provider, Toast.LENGTH_SHORT).show();
        mHandler.sendEmptyMessage(MainActivity.RENEW_GPS);
        //sendString( "onProviderEnabled " + provider);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        //Toast.makeText(mContext, "onStatusChanged " + provider + " : " + status, Toast.LENGTH_SHORT).show();
        mHandler.sendEmptyMessage(MainActivity.RENEW_GPS);
        //sendString("onStatusChanged " + provider + " : " + status + ":" + printBundle(extras));
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    private void sendString(String str){
        Message msg = mHandler.obtainMessage();
        msg.what = MainActivity.SEND_PRINT;
        msg.obj = new String(str+" / GPS Tracker");
        mHandler.sendMessage(msg);
    }

    public static String printBundle(Bundle extras) {
        StringBuilder sb = new StringBuilder();
        try {
            sb.append("extras = " + extras);
            sb.append("\n");
            if (extras != null) {
                Set keys = extras.keySet();
                sb.append("++ bundle key count = " + keys.size());
                sb.append("\n");

                for (String _key : extras.keySet()) {
                    sb.append("key=" + _key + " : " + extras.get(_key)+",");
                }
                sb.append("\n");
            }
        } catch (Exception e) {

        } finally {

        }
        return sb.toString();
    }

}