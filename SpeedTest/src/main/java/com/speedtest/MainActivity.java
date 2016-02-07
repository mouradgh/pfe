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
	public static String[] files = new String[] {"lighthouse.jpg", "hydrangeas.jpg", "tulips.jpg","jellyfish.jpg", "koala.jpg"};

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

		Log.i("info"," LOCATION : " + latLng.toString());
	}

	@Override
	public void onStop() {
		super.onStop();
		if(broadcastReceiver != null)
			unregisterReceiver(broadcastReceiver);

		// Stop Location service
		stopService(new Intent(getApplicationContext(),LocationService.class));
		unregisterReceiver(locationChange);
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

		/*final Activity activity = this;
		final ProgressDialog dialog = new ProgressDialog(this);
		dialog.setMessage("Checking download speed...0%");
		dialog.setMax(10);
		dialog.show();
		new Thread(new Runnable() {@Override
			public void run() {
				float down1 = downloadFile("tulips.jpg");
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						dialog.setMessage("Checking download speed...20%");
					}
				});
				float down2 = downloadFile("hydrangeas.jpg");
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						dialog.setMessage("Checking download speed...40%");
					}
				});
				float down3 = downloadFile("lighthouse.jpg");
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						dialog.setMessage("Checking download speed...60%");
					}
				});
				float down4 = downloadFile("jellyfish.jpg");
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						dialog.setMessage("Checking download speed...80%");
					}
				});
				float down5 = downloadFile("koala.jpg");
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						dialog.setMessage("Checking uploading speed...0%");
						Toast.makeText(activity, "Downloading Process has been completed.", Toast.LENGTH_SHORT).show();
					}
				});

				float up1 = uploadFile("tulips.jpg");
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						dialog.setMessage("Checking uploading speed...20%");
					}
				});
				float up2 = uploadFile("hydrangeas.jpg");
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						dialog.setMessage("Checking uploading speed...40%");
					}
				});
				float up3 = uploadFile("lighthouse.jpg");
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						dialog.setMessage("Checking uploading speed...60%");
					}
				});
				float up4 = uploadFile("jellyfish.jpg");
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						dialog.setMessage("Checking uploading speed...80%");
					}
				});
				float up5 = uploadFile("koala.jpg");
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						dialog.dismiss();
						Toast.makeText(activity, "Uploading Process has been completed.", Toast.LENGTH_SHORT).show();
						Toast.makeText(activity, "Now you can view the recent test results..", Toast.LENGTH_SHORT).show();
					}
				});
				
		
				SharedPreferences sp = getSharedPreferences("session", Context.MODE_PRIVATE);
				Editor editor = sp.edit();
				editor.putFloat("upload1", up1);
				editor.putFloat("upload2", up2);
				editor.putFloat("upload3", up3);
				editor.putFloat("upload4", up4);
				editor.putFloat("upload5", up5);
				
				editor.putFloat("download1", down1);
				editor.putFloat("download2", down2);
				editor.putFloat("download3", down3);
				editor.putFloat("download4", down4);
				editor.putFloat("download5", down5);
				
				File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
				if (!path.exists()) path.mkdirs();
				File source1 = new File(path, "tulips.jpg");
				float size1 = source1.length()/1024;
				if(source1.exists()) source1.delete();
				
				File source2 = new File(path, "hydrangeas.jpg");
				float size2 = source2.length()/1024;
				if(source2.exists()) source2.delete();
				
				File source3 = new File(path, "lighthouse.jpg");
				float size3 = source3.length()/1024;
				if(source3.exists()) source3.delete();
				
				File source4 = new File(path, "jellyfish.jpg");
				float size4 = source4.length()/1024;
				if(source4.exists()) source4.delete();
				
				File source5 = new File(path, "koala.jpg");
				float size5 = source5.length()/1024;
				if(source5.exists()) source5.delete();
				
				editor.putFloat("size1", size1);
				editor.putFloat("size2", size2);
				editor.putFloat("size3", size3);
				editor.putFloat("size4", size4);
				editor.putFloat("size5", size5);
				editor.commit();
			}
		}).start();*/
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

	/*public float downloadFile(String fileName) {
		final String upLoadServerUri = "http://topcity-1.com/test/testing/uploads/";
		float dataRate = 0;
		try {
			URL url = new URL(upLoadServerUri + fileName);
			File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
			if (!path.exists()) path.mkdirs();
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.connect();
			OutputStream os = new FileOutputStream(new File(path, fileName));
			InputStream is = conn.getInputStream();

			long startTime = System.currentTimeMillis();
			int download = 0;
			byte[] data = new byte[1024];
			int bufferLength = 0;
			while ((bufferLength = is.read(data)) > 0) {
				os.write(data, 0, bufferLength);
				download += bufferLength;
			}
			long endTime = System.currentTimeMillis();
			float rate = download / (float)(endTime - startTime);
			dataRate = rate * 1000 / 1024.0f;
			is.close();
			os.close();
		} catch (Exception e) {
			Log.i("test", "" + e);
		}
		return dataRate;
	}

	public float uploadFile(String fileName) {
		final String upLoadServerUri = "http://topcity-1.com/test/testing/upload.php";
		float dataRate = 0;

		HttpURLConnection conn = null;
		DataOutputStream dos = null;
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";
		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 2 * 1024 * 1024;

		try {
			File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
			if (!path.exists()) path.mkdirs();
			File sourceFile = new File(path, fileName);
			FileInputStream fileInputStream = new FileInputStream(sourceFile);
			URL url = new URL(upLoadServerUri);
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoInput(true); // Allow Inputs
			conn.setDoOutput(true); // Allow Outputs
			conn.setUseCaches(false); // Don't use a Cached Copy
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("ENCTYPE", "multipart/form-data");
			conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
			conn.setRequestProperty("uploaded_file", fileName);

			dos = new DataOutputStream(conn.getOutputStream());
			
			dos.writeBytes(twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + fileName + "\"" + lineEnd);

			dos.writeBytes(lineEnd);

			// create a buffer of  maximum size
			bytesAvailable = fileInputStream.available();

			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			buffer = new byte[bufferSize];

			// read file and write it into form...
			bytesRead = fileInputStream.read(buffer, 0, bufferSize);

			long startTime = System.currentTimeMillis();
			while (bytesRead > 0) {
				dos.write(buffer, 0, bufferSize);
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			}

			// send multipart form data necesssary after file data...
			dos.writeBytes(lineEnd);
			dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

			// Responses from the server (code and message)
			int serverResponseCode = conn.getResponseCode();
			String serverResponseMessage = conn.getResponseMessage();

			Log.i("uploadFile", "HTTP Response is : " + serverResponseMessage + ": " + serverResponseCode);

			if (serverResponseCode == 200) {
				final BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String s = br.readLine();
				while (s != null) {
					Log.i("test", s);
					s = br.readLine();
				};
			}
			//close the streams //
			fileInputStream.close();
			dos.flush();
			dos.close();
			long endTime = System.currentTimeMillis();
			float rate = (int) sourceFile.length() / (float)(endTime - startTime);
			dataRate = rate * 1000 / 1024.0f;
		} catch (Exception e) {
			Log.i("test", "Exception : " + e.getMessage());
		}
		return dataRate;
	}*/
}