package com.speedtest;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}
	
	public void drawPlot(View v) {
		final Activity activity = this;

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
						dialog.setMessage("Download...20%");
					}
				});
				float down2 = downloadFile("hydrangeas.jpg");
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						dialog.setMessage("Download...40%");
					}
				});
				float down3 = downloadFile("lighthouse.jpg");
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						dialog.setMessage("Download...60%");
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
						dialog.setMessage("Upload...0%");
						Toast.makeText(activity, "Download terminé", Toast.LENGTH_SHORT).show();
					}
				});
				
				float up1 = uploadFile("tulips.jpg");
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						dialog.setMessage("Upload...20%");
					}
				});
				float up2 = uploadFile("hydrangeas.jpg");
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						dialog.setMessage("Upload...40%");
					}
				});
				float up3 = uploadFile("lighthouse.jpg");
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						dialog.setMessage("Upload...60%");
					}
				});
				float up4 = uploadFile("jellyfish.jpg");
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						dialog.setMessage("Upload...80%");
					}
				});
				float up5 = uploadFile("koala.jpg");
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						dialog.dismiss();
						Toast.makeText(activity, "Upload terminé", Toast.LENGTH_SHORT).show();
						Toast.makeText(activity, "Accès aux résultats", Toast.LENGTH_SHORT).show();
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
		}).start();
	}
	
	public void viewPlot(View v) {
		Intent i = new Intent(this, ScatterPlot.class);
		startActivity(i);
	}
	
	public void viewTable(View v) {
		Intent i = new Intent(this, TableView.class);
		startActivity(i);
	}

	public float downloadFile(String fileName) {
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
			conn.setDoInput(true); 
			conn.setDoOutput(true); 
			conn.setUseCaches(false);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("ENCTYPE", "multipart/form-data");
			conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
			conn.setRequestProperty("uploaded_file", fileName);

			dos = new DataOutputStream(conn.getOutputStream());
			
			dos.writeBytes(twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + fileName + "\"" + lineEnd);

			dos.writeBytes(lineEnd);


			bytesAvailable = fileInputStream.available();

			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			buffer = new byte[bufferSize];

			bytesRead = fileInputStream.read(buffer, 0, bufferSize);

			long startTime = System.currentTimeMillis();
			while (bytesRead > 0) {
				dos.write(buffer, 0, bufferSize);
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			}

			dos.writeBytes(lineEnd);
			dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

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
	}

}