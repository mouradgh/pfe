package com.speedtest;

/**
 * Created by Admin on 10/02/2016.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.renderscript.Float4;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.speedtest.FileUtils.FileUtils;
import com.speedtest.model.DataModel;

import java.util.List;

public class MapView extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_view);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        float Color;
        List<DataModel> dataModelList = FileUtils.ParseDataFile(this, FileUtils.GetRootPath(this) + FileUtils.CHECK_SPEED_RESULT_FILE);
        LatLng loc;
        // Adding marker for every location in the file
        if (dataModelList != null && dataModelList.size() > 0) {
            for (DataModel dataModel : dataModelList) {
                loc = dataModel.getLocation();
                Color = getColorSpeed(dataModel.getDownloadSpeed());
                mMap.addMarker(new MarkerOptions().position(loc).icon(BitmapDescriptorFactory.defaultMarker(Color)));
            }
        }
        // setting the geolocation to true
        mMap.setMyLocationEnabled(true);

    }

    // getting the color according to the download speed
    private float getColorSpeed(float downloadSpeed) {
        float Color;
        if(downloadSpeed < 300.0)
            Color = BitmapDescriptorFactory.HUE_RED;
        else if(downloadSpeed < 800.0)
            Color = BitmapDescriptorFactory.HUE_YELLOW;
        else if(downloadSpeed < 1200.0)
            Color = BitmapDescriptorFactory.HUE_BLUE;
        else
            Color = BitmapDescriptorFactory.HUE_GREEN;
        return Color;
    }
}