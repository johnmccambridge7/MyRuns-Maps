package com.example.myruns;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GPSActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    public Marker currentPin;
    private LocationManager locationManager;
    private ArrayList<LatLng> polygon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        polygon = new ArrayList<LatLng>();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if(savedInstanceState != null) {
            polygon = savedInstanceState.getParcelableArrayList("points");
        }
    }

    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putParcelableArrayList("points", this.polygon);
    }

    public void cancel(View view) {
        finish();
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
        if(!checkPermission())
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        else
            refreshLocationOnMap();
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

    // converts the location into the lat long coord
    private LatLng fromLocationToLatLng(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    private void updateWithNewLocation(Location location) {
        if (location != null) {
            LatLng coordinate = fromLocationToLatLng(location);

            this.polygon.add(coordinate);

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinate, 17));

            if (currentPin != null)
                currentPin.remove();

            PolylineOptions polylineCoords = new PolylineOptions();

            for(LatLng point : this.polygon) {
                polylineCoords.add(point);
            }

            mMap.addPolyline(polylineCoords);

            //this.polygon.add(new MarkerOptions().position(coordinate).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            currentPin = mMap.addMarker(
                    new MarkerOptions().position(coordinate).icon(BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_GREEN)).title("Current Location.")
            );

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

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            refreshLocationOnMap();
        } else {
            finish();
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

            currentPin = mMap.addMarker(
                    new MarkerOptions().position(latlng).icon(BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_GREEN)));
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 17)); //17: the desired zoom level, in the range of 2.0 to 21.0
            updateWithNewLocation(l);
        }

        locationManager.requestLocationUpdates(provider, 1000, 0, locationListener);
    }

    public void message(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
