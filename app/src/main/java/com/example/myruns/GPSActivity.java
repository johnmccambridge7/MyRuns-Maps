package com.example.myruns;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.w3c.dom.Text;

import java.util.ArrayList;

// todo:
// 1. add background support. - DONE
// 2. write information to database.
// 3. model to draw speed and other metrics etc. (make sure to use preferences) - DONE
// est time 6 hours

/*
System Design for Service:

1. Notify user when service begins (on tap open up activity). - DONE
2. Service in the background periodically pings GPSActivity and updates location. - DONE
3. Broadcast new location to GPSActivity. - DONE
4. Click on Notification in user bar to open activity. - DONE

 */

public class GPSActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    public Marker currentPin;
    public Marker startingPin;
    public LatLng currentCoord;
    public LatLng startingCoord;

    String units = "metric";
    String activityTypeData = "None";

    private ArrayList<LatLng> polygon;
    private ArrayList<LatLng> heights;
    private ArrayList<Timestamps> timestamps;

    private Double startingHeight;
    private Float currentSpeedValue;
    private double avgSpeedValue;

    private LocationReceiver locationReceiver;

    TextView avgSpeed;
    TextView currentSpeed;
    TextView distance;
    TextView activityType;
    TextView climb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps);

        avgSpeed = (TextView) findViewById(R.id.avgSpeed);
        currentSpeed = (TextView) findViewById(R.id.currentSpeed);
        distance = (TextView) findViewById(R.id.distance);
        activityType = (TextView) findViewById(R.id.activityType);
        climb = (TextView) findViewById(R.id.climb);

        Bundle intentData = getIntent().getExtras();
        this.units = intentData.getString("units");
        this.activityTypeData = intentData.getString("activityType");

        // start the service for getting location
        Intent intent = new Intent(this, LocationService.class);
        startService(intent);

        //locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        polygon = new ArrayList<LatLng>();
        heights = new ArrayList<LatLng>();
        timestamps = new ArrayList<Timestamps>();

        locationReceiver = new LocationReceiver();

        currentSpeedValue = 0.0f;
        currentCoord = new LatLng(0,0);
        startingCoord = new LatLng(0, 0);

        // SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        IntentFilter filter = new IntentFilter(LocationService.ACTION_NEW_LOCATION);
        LocalBroadcastManager.getInstance(this).registerReceiver(locationReceiver, filter);

        if(savedInstanceState != null) {
            polygon = savedInstanceState.getParcelableArrayList("points");
            heights = savedInstanceState.getParcelableArrayList("heights");

            double lat = savedInstanceState.getDouble("lat");
            double lng = savedInstanceState.getDouble("long");
            currentCoord = new LatLng(lat, lng);

            double savedLat = savedInstanceState.getDouble("startingLat");
            double savedLong = savedInstanceState.getDouble("startingLong");
            startingCoord = new LatLng(savedLat, savedLong);

            double distanceTravelled = getDistanceTravelled();
            String distString = "Distance: " + String.valueOf(distanceTravelled) + " m";
            distance.setText(distString);

            startingHeight = savedInstanceState.getDouble("startingHeight");
            String altString = "Climb: " + String.valueOf(getTotalClimb(startingHeight)) + " m";
            climb.setText(altString);

            String activity = "Type: " + savedInstanceState.getString("activity");
            activityType.setText(activity);

            avgSpeedValue = savedInstanceState.getDouble("averageSpeed");
            String avgSpeedData = "Avg Speed: " + String.valueOf(avgSpeedValue) + " m/h";
            avgSpeed.setText(avgSpeedData);

            currentSpeedValue = savedInstanceState.getFloat("currentSpeed");
            String currentSpeedData = "Curr. Speed: " + String.valueOf(currentSpeedValue) + " m/h";
            currentSpeed.setText(currentSpeedData);
        }
    }

    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putParcelableArrayList("points", this.polygon);
        bundle.putParcelableArrayList("heights", this.heights);
        bundle.putParcelableArrayList("times", this.timestamps);
        bundle.putDouble("lat", currentCoord.latitude);
        bundle.putDouble("long", currentCoord.longitude);
        bundle.putDouble("startingLat", startingCoord.latitude);
        bundle.putDouble("startingLong", startingCoord.longitude);
        bundle.putDouble("startingHeight", startingHeight);
        bundle.putString("activity", activityTypeData);
        bundle.putString("units", units);
        bundle.putFloat("currentSpeed", currentSpeedValue);
        bundle.putDouble("averageSpeed", avgSpeedValue);
    }

    public void save(View view) {
        // stop the background service
        // use thread to write to the database
        // convert latlong to byte array and vice versa



        Intent i = new Intent();
        i.setAction(LocationService.STOP_SERVICE_ACTION);
        sendBroadcast(i);
    }

    public void cancel(View view) {
        Intent i = new Intent();
        i.setAction(LocationService.STOP_SERVICE_ACTION);
        sendBroadcast(i);

        finish();
    }

    public Marker addMarker(LatLng point, boolean isStartingPoint) {
        MarkerOptions options = new MarkerOptions();
        options.position(point);

        if(isStartingPoint) {
            options.icon(
                    BitmapDescriptorFactory.defaultMarker(
                    BitmapDescriptorFactory.HUE_GREEN)
            );
        }

        return mMap.addMarker(options);
    }

    public double getTotalClimb(double startingHeight) {
        double netHeight = 0.0;

        for(LatLng data : heights) {
            double height = data.latitude;
            double delta = (height - startingHeight);

            if(delta > 0) {
                netHeight += delta;
            }
        }

        return netHeight;
    }

    public double getDistanceTravelled() {
        if(this.polygon.size() > 1) {
            double distance = 0.0;

            for(int i = 0; i < this.polygon.size() - 1; i++) {
                LatLng current = this.polygon.get(i);
                LatLng next = this.polygon.get(i + 1);

                float[] results = new float[3];
                Location.distanceBetween(current.latitude, current.longitude, next.latitude, next.longitude, results);

                distance += results[0];
            }

            return distance;
        }

        return 0.0;
    }

    /*
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if(!checkPermission()) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            refreshMap();
        }
    }

    //******** Check run time permission for locationManager. This is for v23+  ********
    private boolean checkPermission(){
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED)
            return true;
        else
            return false;
    }
    //****** Check run time permission ************

    public void refreshMap() {
        if (currentPin != null)
            currentPin.remove();

        PolylineOptions polylineCoords = new PolylineOptions();

        for(LatLng point : polygon) {
            polylineCoords.add(point);
        }

        mMap.addPolyline(polylineCoords);

        if(currentCoord.latitude == 0 && currentCoord.longitude == 0) {
            return;
        }

        startingPin = addMarker(startingCoord, true);
        currentPin = addMarker(currentCoord, false);
    }

    public class LocationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            double lng = intent.getExtras().getDouble("long", -10.0);
            double lat = intent.getExtras().getDouble("lat", -10.0);

            currentSpeedValue = intent.getExtras().getFloat("speed");
            double altitude = intent.getExtras().getDouble("altitude");

            currentCoord = new LatLng(lat, lng);

            if(startingHeight == null) {
                startingHeight = altitude;
            }

            heights.add(new LatLng(altitude, 0));

            Timestamps stamp = new Timestamps(System.currentTimeMillis());

            timestamps.add(stamp);
            polygon.add(currentCoord);

            String altString = "Climb: " + String.valueOf(getTotalClimb(startingHeight)) + " m";
            climb.setText(altString);

            double distanceTravelled = getDistanceTravelled();

            String distString = "Distance: " + String.valueOf(distanceTravelled) + " m";
            distance.setText(distString);

            avgSpeedValue = getAvgSpeed(distanceTravelled);
            String avgSpeedData = "Avg Speed: " + String.valueOf(avgSpeedValue) + " m/h";
            avgSpeed.setText(avgSpeedData);

            String currentSpeedData = "Curr. Speed: " + String.valueOf(currentSpeedValue) + " m/h";
            currentSpeed.setText(currentSpeedData);

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentCoord, 100));

            if(startingCoord.latitude == 0 && startingCoord.longitude == 0) {
                startingCoord = new LatLng(lat, lng);
            }

            refreshMap();
        }
    }

    // returns avg speed in meters per second
    public double getAvgSpeed(double distance) {
        if(this.timestamps.size() > 1) {
            double startTime = this.timestamps.get(0).getStamp();
            double endTime = this.timestamps.get(this.timestamps.size() - 1).getStamp();

            double deltaSeconds = (endTime - startTime) / 1000f;

            if(deltaSeconds == 0) {
                return 0.0;
            }

            return distance / deltaSeconds;
        }

        return 0.0;
    }

    // TEST ON DEVICE W.O PERMISSIONS
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            refreshMap();
        } else {
            finish();
        }
    }

    public void message(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
