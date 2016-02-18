package com.speedtest;

/**
 * Created by Admin on 10/02/2016.
 */

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;
import com.speedtest.FileUtils.FileUtils;
import com.speedtest.model.DataModel;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MapView extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private HeatmapTileProvider mProvider;
    private TileOverlay mOverlay;
    private ArrayList <WeightedLatLng> ListWeightedLoc= new ArrayList<>();
    List<DataModel> dataModelList;
    int BLUE = 0x500000FF; //transparency fixed to 50%
    int RED=0x50FF0000;
    int YELLOW=0x50FFFF00;
    int GREEN=0x5000FF00;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_view);
        dataModelList = FileUtils.ParseDataFile(this, FileUtils.GetRootPath(this) + FileUtils.CHECK_SPEED_RESULT_FILE);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        int i=10;
        // Adding circles for every location in the file
        if (dataModelList != null && dataModelList.size() > 0) {
            //AddCirclesToMAP(dataModelList, "");
            for (DataModel dataModel : dataModelList) {
                ListWeightedLoc.add(new WeightedLatLng(dataModel.getLocation(),i));
                i+=10;
            }
        }
        // Create a heat map tile provider
        mProvider = new HeatmapTileProvider.Builder()
                .weightedData(ListWeightedLoc)
                .build();
        // Add a tile overlay to the map, using the heat map tile provider.
        mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));

        // setting the geolocation to true
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
    }



    private void AddCirclesToMAP(List<DataModel> dataModelList, String type){
        int color;
        LatLng loc;
        if (type.isEmpty()){
            mMap.clear();
            for (DataModel dataModel : dataModelList) {
                loc = dataModel.getLocation();
                color = getColorSpeed(dataModel.getDownloadSpeed());
                mMap.addCircle(new CircleOptions().center(loc).fillColor(color).radius(5.0).strokeColor(Color.TRANSPARENT));
            }
        }
        else{
            mMap.clear();
            for (DataModel dataModel : dataModelList) {
                if (dataModel.getConnectionType().contains(type)) {
                    loc = dataModel.getLocation();
                    color = getColorSpeed(dataModel.getDownloadSpeed());
                    mMap.addCircle(new CircleOptions().center(loc).fillColor(color).radius(5.0).strokeColor(Color.TRANSPARENT));
                }
            }
        }

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

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radioButton:
                if (checked) {
                    mMap.clear();
                    AddCirclesToMAP(dataModelList, "3G");
                }
                    break;
            case R.id.radioButton2:
                if (checked) {
                    mMap.clear();
                    AddCirclesToMAP(dataModelList, "4G");
                }
                    break;
            case R.id.radioButton3:
                if (checked) {
                    mMap.clear();
                    AddCirclesToMAP(dataModelList, "WIFI");
                }
                break;
            case R.id.radioButton4:
                if (checked) {
                    mMap.clear();
                    AddCirclesToMAP(dataModelList, "");
                }
                break;
        }
    }
}