package com.speedtest;

/**
 * Created by Admin on 10/02/2016.
 */

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.WeightedLatLng;
import com.speedtest.FileUtils.FileUtils;
import com.speedtest.model.DataModel;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import java.util.ArrayList;
import java.util.List;

public class MapView extends FragmentActivity implements OnMapReadyCallback {
    final String NET_3G = "3G";
    final String NET_4G = "4G";
    final String NET_WIFI = "WIFI";
    final String NET_ALL = "ALL";
    private GoogleMap mMap;
    private HeatmapTileProvider mProvider;
    private TileOverlay mOverlay;
    private ArrayList <WeightedLatLng> ListWeightedLoc= new ArrayList<>();
    List<DataModel> dataModelList;
    int intensity;
    // Creating the gradient.
    int[] colors = {
            Color.rgb(102, 225, 0), // green
            Color.rgb(255, 255, 0),    // yellow
            Color.rgb(225, 0, 0)    // red
    };
    float[] startPoints = { // starting point for each color, given as a percentage of the maximum intensity
            0.01f, 0.02f, 0.03f
    };
    Gradient gradient = new Gradient(colors, startPoints);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_view);
        dataModelList = FileUtils.ParseDataFile(this, FileUtils.GetRootPath(this) + FileUtils.CHECK_SPEED_RESULT_FILE);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Spinner element
        Spinner spinner = (Spinner) findViewById(R.id.spinner);

        // Spinner click listener
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // On selecting a spinner item
                String item = parent.getItemAtPosition(position).toString();
                if (item.equals(NET_3G))
                    AddHeatMAP(NET_3G);
                else if (item.equals(NET_4G))
                    AddHeatMAP(NET_4G);
                else if (item.equals(NET_WIFI))
                    AddHeatMAP(NET_WIFI);
                else AddHeatMAP(NET_ALL);
                // Showing selected spinner item
                Toast.makeText(parent.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Spinner Drop down elements
        List<String> categories = new ArrayList<>();
        categories.add(NET_3G);
        categories.add(NET_4G);
        categories.add(NET_WIFI);
        categories.add(NET_ALL);

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Adding the heatmap
        if (dataModelList != null && dataModelList.size() > 0) {
                AddHeatMAP(NET_ALL);
        }
        // setting the geolocation to true
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
    }

    public void AddHeatMAP(String type){
        if (type.equals(NET_ALL)){
            if(mOverlay!=null){
                mOverlay.remove();
                ListWeightedLoc.clear();
            }
            for (DataModel dataModel : dataModelList) {
                intensity = getColorSpeed(dataModel.getDownloadSpeed());
                ListWeightedLoc.add(new WeightedLatLng(dataModel.getLocation(), intensity));
            }
            if(!ListWeightedLoc.isEmpty()) {
                // Create a heat map tile provider
                mProvider = new HeatmapTileProvider.Builder()
                        .weightedData(ListWeightedLoc)
                        .gradient(gradient)
                        .build();
                // Add a tile overlay to the map, using the heat map tile provider.
                mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
            }
        }
        else{
            if(mOverlay!=null) {
                mOverlay.remove();
                ListWeightedLoc.clear();
            }
            for (DataModel dataModel : dataModelList) {
                if (dataModel.getConnectionType().contains(type)) {
                    intensity = getColorSpeed(dataModel.getDownloadSpeed());
                    ListWeightedLoc.add(new WeightedLatLng(dataModel.getLocation(), intensity));
                }
            }
            if(!ListWeightedLoc.isEmpty()) {
                // Create a heat map tile provider
                mProvider = new HeatmapTileProvider.Builder()
                        .weightedData(ListWeightedLoc)
                        .gradient(gradient)
                        .build();
                // Add a tile overlay to the map, using the heat map tile provider.
                mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
            }
        }

    }
    // getting the color according to the download speed
    private int getColorSpeed(float downloadSpeed) {
        int Color;
        if(downloadSpeed < 500.0)
            Color = 1;
        else if(downloadSpeed < 1200.0)
            Color =20;
        else
            Color = 1000;
        return Color;
    }
}