package com.speedtest;

/**
 * Created by Admin on 10/02/2016.
 */

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;


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
    int BLUE = 0x500000FF; //transparency fixed to 50%
    int RED=0x50FF0000;
    int YELLOW=0x50FFFF00;
    int GREEN=0x5000FF00;

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
        int color;
        List<DataModel> dataModelList = FileUtils.ParseDataFile(this, FileUtils.GetRootPath(this) + FileUtils.CHECK_SPEED_RESULT_FILE);
        LatLng loc=null;
        // Adding marker for every location in the file
        if (dataModelList != null && dataModelList.size() > 0) {
            for (DataModel dataModel : dataModelList) {
                loc = dataModel.getLocation();
                color = getColorSpeed(dataModel.getDownloadSpeed());
               // mMap.addMarker(new MarkerOptions().position(loc).icon(BitmapDescriptorFactory.defaultMarker(color)));
                mMap.addCircle(new CircleOptions().center(loc).fillColor(color).radius(30.0).strokeColor(Color.TRANSPARENT));
            }
        }
        // setting the geolocation to true
        mMap.setMyLocationEnabled(true);

    }

    // getting the color according to the download speed
    private int getColorSpeed(float downloadSpeed) {
        int Color;
        if(downloadSpeed < 300.0)
            Color = RED;
        else if(downloadSpeed < 800.0)
            Color =YELLOW;
        else if(downloadSpeed < 1200.0)
            Color = BLUE;
        else
            Color = GREEN;
        return Color;
    }
}