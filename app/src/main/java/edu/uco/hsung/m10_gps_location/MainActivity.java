package edu.uco.hsung.m10_gps_location;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final long POLLING_FREQ = 5000; // msec
    private static final float MIN_DISTANCE = 10.0f;

    public static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 1;

    // Views for display location information
    private TextView viewAccuracy;
    private TextView viewTime;
    private TextView viewLat;
    private TextView viewLng;

    // Reference to the LocationManager and LocationListener
    private LocationManager locationManager;
    private LocationListener locationListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_main);

        viewAccuracy = (TextView) findViewById(R.id.accuracy_view);
        viewTime = (TextView) findViewById(R.id.time_view);
        viewLat = (TextView) findViewById(R.id.lat_view);
        viewLng = (TextView) findViewById(R.id.lng_view);

        viewAccuracy.setText(R.string.no_intial_reading);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updateDisplay(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };

        // API 23 or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getApplicationContext().checkSelfPermission(
                    Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {

                this.requestPermissions(
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSION_ACCESS_FINE_LOCATION);
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (locationManager == null)
            return;

        if (locationManager.getProvider(LocationManager.GPS_PROVIDER) != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // API 23 or higher
                if (getApplicationContext().checkSelfPermission(
                        Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
                    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    if (locationManager == null) {
                        viewAccuracy.setText("No GPS is available");
                        // finish(); <-- to close the app (the app simply disappears)
                        return;
                    }
                    try {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER, POLLING_FREQ,
                                MIN_DISTANCE, locationListener);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                }
            } else { // API 22 or lower
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (locationManager == null) {
                    viewAccuracy.setText("No GPS is available");
                    // finish(); <-- to close the app (the app simply disappears)
                    return;
                }
                if (locationManager.getProvider(LocationManager.GPS_PROVIDER) != null) {
                    try {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER, POLLING_FREQ,
                                MIN_DISTANCE, locationListener);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        // Unregister location listeners
        if (locationManager != null && locationListener != null)
            locationManager.removeUpdates(locationListener);
    }

    @Override
    @TargetApi(23)
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        if (grantResults.length != 0 && requestCode == MY_PERMISSION_ACCESS_FINE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            try {
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, POLLING_FREQ,
                        MIN_DISTANCE, locationListener);
            } catch (SecurityException e) {
                e.printStackTrace();
            }

        } else {
            Toast.makeText(MainActivity.this, "GPS unavailable", Toast.LENGTH_SHORT).show();
        }
    }

    // Update display
    private void updateDisplay(Location location) {
        viewAccuracy.setText("Accuracy: " + location.getAccuracy());
        viewTime.setText("Time: " + new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale
                .getDefault()).format(new Date(location.getTime())));
        viewLat.setText("Longitude: " + location.getLongitude());
        viewLng.setText("Latitude: " + location.getLatitude());
    }

}