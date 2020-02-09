package com.example.myruns;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class LocationService extends Service {
    public static final int NOTIFY_ID = 11;
    public static final String CHANNEL_ID = "notification channel";
    public static final String STOP_SERVICE_ACTION = "stop service action";
    public static final String ACTION_NEW_LOCATION = "com.example.action.NEW_LOCATION";

    LocationBroadcastReceiver receiver;
    NotificationManager notificationManager;
    LocationManager locationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        sendTrackingNotification();
        refreshLocationOnMap();

        receiver = new LocationBroadcastReceiver();

        // called when cancel button is pressed
        IntentFilter filter = new IntentFilter();
        filter.addAction(STOP_SERVICE_ACTION);
        registerReceiver(receiver, filter);
    }

    // converts the location into the lat long coord
    private LatLng fromLocationToLatLng(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    private void updateWithNewLocation(Location location) {
        if (location != null) {
            LatLng coordinate = fromLocationToLatLng(location);

            //message(String.valueOf(coordinate.latitude));

            Intent intent = new Intent(ACTION_NEW_LOCATION);

            intent.putExtra("long", coordinate.longitude);
            intent.putExtra("lat", coordinate.latitude);

            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

            if (!Geocoder.isPresent()) {
                Toast.makeText(getApplicationContext(), "No geocoder available", Toast.LENGTH_LONG).show();
            } else {
                try {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    Geocoder gc = new Geocoder(this, Locale.getDefault());

                    List<Address> addresses = gc.getFromLocation(latitude, longitude, 1);
                    StringBuilder sb = new StringBuilder();
                    if (addresses.size() > 0) {
                        Address address = addresses.get(0);

                        for (int i = 0; i < address.getMaxAddressLineIndex(); i++)
                            sb.append(address.getAddressLine(i)).append("\n");

                        sb.append(address.getLocality()).append("\n");
                        sb.append(address.getPostalCode()).append("\n");
                        sb.append(address.getCountryName());
                    }
                } catch (IOException e) {
                    Log.d("johnmacdonald", "IO Exception", e);
                }
            }
        }
    }

    private final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            updateWithNewLocation(location);
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    private void refreshLocationOnMap() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(true);
        String provider = locationManager.getBestProvider(criteria, true);

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Location l = locationManager.getLastKnownLocation(provider);

        if(l != null) {
            LatLng latlng = fromLocationToLatLng(l);
            updateWithNewLocation(l);
        }

        locationManager.requestLocationUpdates(provider, 1000, 0, locationListener);
    }

    public void message(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent i, int flags, int startID) {
        return START_NOT_STICKY;
    }

    public void sendTrackingNotification() {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
        notificationBuilder.setContentTitle("title");
        notificationBuilder.setContentText("content");
        notificationBuilder.setSmallIcon(R.drawable.common_google_signin_btn_icon_dark);
        Notification notification = notificationBuilder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL; // or builder.setAutoCancel(true);

        if(Build.VERSION.SDK_INT > 26) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "channel name",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        notificationManager.notify(NOTIFY_ID, notification);
    }

    public class LocationBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("johnmacdonald", "# received");
        }
    }
}
