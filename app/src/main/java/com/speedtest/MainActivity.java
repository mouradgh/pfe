package com.speedtest;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.google.android.gms.maps.model.LatLng;
import com.speedtest.services.CheckSpeedService;
import com.speedtest.services.LocationService;

import org.apache.commons.math3.stat.regression.SimpleRegression;

public class MainActivity extends Activity {

	private SimpleRegression simpleRegression = new SimpleRegression();

	private BroadcastReceiver broadcastReceiver;
	private BroadcastReceiver locationChange;
	private LatLng latLng = new LatLng(0,0);

	private ProgressDialog dialog;
	public static String[] files = new String[] {"img5.jpg", "img4.jpg", "img3.jpg","img2.jpg", "img1.jpg"};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		broadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if(intent.getAction().equals(CheckSpeedService.BROADCAST_COMPLETE_CHECK_SPEED)) {
					dialog.dismiss();
				} else if(intent.getAction().equals(CheckSpeedService.BROADCAST_DOWNLOAD_TASK)) {
					int task = intent.getIntExtra(CheckSpeedService.COMPLETE_SIMPLE_TASK, 0);
					if(task > 0)
						dialog.setMessage("Checking download speed..." + (100 / files.length * task) + "%");
				} else if(intent.getAction().equals(CheckSpeedService.BROADCAST_UPLOAD_TASK)) {
					int task = intent.getIntExtra(CheckSpeedService.COMPLETE_SIMPLE_TASK, 0);
					if(task > 0)
						dialog.setMessage("Checking upload speed..." + (100 / files.length * task) + "%");
					else
						dialog.setMessage("Checking upload speed...  0%");
				}
			}
		};

		locationChange = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				latLng = new LatLng(intent.getFloatExtra("latitude",0),intent.getFloatExtra("longitude",0));
			}
		};


		((Button)findViewById(R.id.button2)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, SimpleRegressionActivity.class));
			}
		});
	}

	@Override
	public void onStart() {
		super.onStart();
		IntentFilter Complete = new IntentFilter(CheckSpeedService.BROADCAST_COMPLETE_CHECK_SPEED);
		IntentFilter Download = new IntentFilter(CheckSpeedService.BROADCAST_DOWNLOAD_TASK);
		IntentFilter Upload = new IntentFilter(CheckSpeedService.BROADCAST_UPLOAD_TASK);

		registerReceiver(broadcastReceiver, Complete);
		registerReceiver(broadcastReceiver, Download);
		registerReceiver(broadcastReceiver, Upload);

		// Start Location service
		startService(new Intent(getApplicationContext(), LocationService.class));
		IntentFilter intentFilter = new IntentFilter(LocationService.LOCATION_CHANGE_BROADCAST_RECEIVER);
		registerReceiver(locationChange, intentFilter);

		Log.i("info", " LOCATION : " + latLng.toString());
	}

	@Override
	public void onStop() {
		super.onStop();
		if(broadcastReceiver != null)
			unregisterReceiver(broadcastReceiver);

		// Stop Location service
		stopService(new Intent(getApplicationContext(), LocationService.class));
		unregisterReceiver(locationChange);
	}



	public void onReceive(WifiManager wifiManager) {
		int numberOfLevels=5;
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int level=WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels);
	}
	
	public void drawPlot(View v) {
		Intent intent = new Intent(this, CheckSpeedService.class);
		intent.putExtra(CheckSpeedService.REPEAT, 5);
		intent.putExtra(CheckSpeedService.FILES,files);
		intent.putExtra("latitude",(float)latLng.latitude);
		intent.putExtra("longitude",(float)latLng.longitude);

		startService(intent);

		dialog = new ProgressDialog(this);
		dialog.setMessage("Checking download speed...0%");
		dialog.show();
	}

	public void viewPlotCandle(View v) {
		Intent i = new Intent(this, CandleStickChartActivity.class);
		startActivity(i);
	}
	public void viewPlot(View v) {
		Intent i = new Intent(this, ScatterPlot.class);
		startActivity(i);
	}
	
	public void viewTable(View v) {
		Intent i = new Intent(this, TableView.class);
		startActivity(i);
	}
}