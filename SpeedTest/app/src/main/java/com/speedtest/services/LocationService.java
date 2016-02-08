package com.speedtest.services;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;


public class LocationService extends Service implements LocationListener {

    public static final String LOCATION_CHANGE_BROADCAST_RECEIVER = "com.speedtest.services.location_change";

    private static final long MIN_UPDATE_TIME_MS = 0;
    private static final float MIN_UPDATE_DISTANCE = 0;
    private LocationManager locationManager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        try {
            if (locationManager != null)
                locationManager.removeUpdates(this);
        }catch (SecurityException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            try {
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location != null) {
                    this.onLocationChanged(location);
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,MIN_UPDATE_TIME_MS,MIN_UPDATE_DISTANCE,this);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onLocationChanged(Location location) {
        Intent intent = new Intent(LOCATION_CHANGE_BROADCAST_RECEIVER);
        intent.putExtra("latitude",(float)location.getLatitude());
        intent.putExtra("longitude",(float)location.getLongitude());
        sendBroadcast(intent);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {}
}
