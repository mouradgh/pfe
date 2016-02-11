package com.speedtest;

/**
 * Created by Admin on 10/02/2016.
 */

    import android.content.Context;
    import android.content.SharedPreferences;
    import android.os.Bundle;
    import android.support.v4.app.FragmentActivity;

    import com.google.android.gms.maps.CameraUpdateFactory;
    import com.google.android.gms.maps.GoogleMap;
    import com.google.android.gms.maps.OnMapReadyCallback;
    import com.google.android.gms.maps.SupportMapFragment;
    import com.google.android.gms.maps.model.LatLng;
    import com.google.android.gms.maps.model.MarkerOptions;

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
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        /*LatLng speed = new LatLng(Double.parseDouble(sp.getString("mapX", "")), Double.parseDouble(sp.getString("mapY", "")));
        float down = sp.getFloat("avgDown", 0);
        float up = sp.getFloat("avgUp", 0);
        mMap.addMarker(new MarkerOptions().position(speed).title("Downloading: "+down+"\nUploading: "+up));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(speed));*/
    }
}
