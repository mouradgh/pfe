package com.speedtest.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.speedtest.FileUtils.FileUtils;
import com.speedtest.common.InternetConnectionType;
import com.speedtest.model.DataModel;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Admin on 1/7/16.
 */
public class CheckSpeedService extends Service {

    public static final String BROADCAST_COMPLETE_CHECK_SPEED = "com.speedtest.services.CheckSpeedService";
    public static final String BROADCAST_UPLOAD_TASK = "com.speedtest.services.CheckSpeedService.upload_task";
    public static final String BROADCAST_DOWNLOAD_TASK = "com.speedtest.services.CheckSpeedService.download_task";


    public static final String FILES = "files";
    public static final String REPEAT = "repeat";
    public static final String COMPLETE_SIMPLE_TASK = "complete_simple_task";

    public static String wifiStrength="";
    public static String gsmStrength="";

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("HH:mm:ss");
    private LatLng latLng;
    private ExecutorService executorService = null;

    private JSONObject geoJSON;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.i("info"," CheckSpeedService : onCreate");
        super.onCreate();
        executorService = Executors.newFixedThreadPool(1);
    }

    @Override
    public void onDestroy() {
        Log.i("info"," CheckSpeedService : onDestroy");
        Intent intent = new Intent(BROADCAST_COMPLETE_CHECK_SPEED);
        sendBroadcast(intent);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flag, int startId) {
        Log.i("info"," CheckSpeedService : onStartCommand : startId = " + startId);
        String[] files = intent.getStringArrayExtra(FILES);
        int repeat = intent.getIntExtra(CheckSpeedService.REPEAT, 0);
        latLng = new LatLng((double)intent.getFloatExtra("latitude",0), (double) intent.getFloatExtra("longitude",0));

        MyPhoneStateListener MyListener = new MyPhoneStateListener();
        TelephonyManager Tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        Tel.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);


        //wifi

        registerReceiver(new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    final WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                    int state = wifi.getWifiState();
                    if (state == WifiManager.WIFI_STATE_ENABLED) {
                        List<ScanResult> results = wifi.getScanResults();

                        for (ScanResult result : results) {
                            if (result.BSSID.equals(wifi.getConnectionInfo().getBSSID())) {
                                int level = WifiManager.calculateSignalLevel(wifi.getConnectionInfo().getRssi(),
                                        result.level);
                                int difference = level * 100 / result.level;
                                int signalStrangth = 0;
                                if (difference >= 100)
                                    signalStrangth = 4;
                                else if (difference >= 75)
                                    signalStrangth = 3;
                                else if (difference >= 50)
                                    signalStrangth = 2;
                                else if (difference >= 25)
                                    signalStrangth = 1;
                                wifiStrength= String.valueOf(signalStrangth);

                            }

                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        }, new IntentFilter(WifiManager.RSSI_CHANGED_ACTION));

        try {

            JSONArray arrayPoints = new JSONArray();
            arrayPoints.put(latLng.longitude);
            arrayPoints.put(latLng.latitude);

            JSONObject typePoint = new JSONObject();
            typePoint.put("type","Point");
            typePoint.put("coordinates",(Object)arrayPoints);

            JSONObject properties = new JSONObject();
            properties.put("name","Location");

            JSONObject typeFeature = new JSONObject();
            typeFeature.put("type","Feature");
            typeFeature.put("geometry",(Object)typePoint);
            typeFeature.put("properties",(Object)properties);

            this.geoJSON = typeFeature;

            Log.i("info","  " + typeFeature.toString());
            Log.i("info","  " + typePoint.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        executorService.execute(new CheckSpeedTask(files, repeat, startId));

        return START_NOT_STICKY;
    }

    /**********************************************************************************************/
    // Service task
    /**********************************************************************************************/
    private class CheckSpeedTask implements Runnable {
        private String[] files = null;
        private int repeat = 0;
        private int startId = -1;

        public CheckSpeedTask(String[] files, int repeat, int startId) {
            this.files = files;
            this.repeat = repeat;
            this.startId = startId;
        }

        @Override
        public void run() {
            List<DataModel>dataModelsList = new ArrayList<>();
            for (int i = 0; i < repeat * files.length; i++) {
                dataModelsList.add(new DataModel());
            }

            String operator = " ";
            TelephonyManager  tm=(TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if(!tm.getNetworkOperatorName().isEmpty()){
                operator = tm.getNetworkOperatorName();
            }

            if (!path.exists())
                path.mkdirs();

            // Download
            int repeatDownload = 0;
            for (int i = 0; i < files.length; i++) {
                String currentFile = files[i];
                File file = new File(path, currentFile);
                do {
                    float downloadSpeed = downloadFile(currentFile);
                    dataModelsList.get(i * repeat + repeatDownload).setFileName(currentFile);
                    dataModelsList.get(i * repeat + repeatDownload).setFileSize(file.length());
                    dataModelsList.get(i * repeat + repeatDownload).setDownloadSpeed(downloadSpeed);
                    dataModelsList.get(i * repeat + repeatDownload).setDate(simpleDateFormat.format(new Date()));
                    dataModelsList.get(i * repeat + repeatDownload).setTime(simpleTimeFormat.format(new Date()));
                    dataModelsList.get(i * repeat + repeatDownload).setConnectionType(InternetConnectionType.getNetworkClass(getApplicationContext()));
                    dataModelsList.get(i * repeat + repeatDownload).setLocation(latLng);
                    dataModelsList.get(i * repeat + repeatDownload).setPhoneName(getDeviceName());
                    dataModelsList.get(i * repeat + repeatDownload).setSignalStrengthWifi(wifiStrength);
                    dataModelsList.get(i * repeat + repeatDownload).setSignalStrengthGSM(gsmStrength);
                    dataModelsList.get(i * repeat + repeatDownload).setOperator(operator);

                    if (geoJSON != null) {
                        dataModelsList.get(i * repeat + repeatDownload).setGeoJSON(geoJSON.toString());
                    }

                    repeatDownload += 1;
                }while (repeatDownload < repeat);
                repeatDownload = 0;

                Intent intent = new Intent(CheckSpeedService.BROADCAST_DOWNLOAD_TASK);
                intent.putExtra(COMPLETE_SIMPLE_TASK, i + 1);
                sendBroadcast(intent);
            }


            SystemClock.sleep(3000);
            // Upload
            Intent intent = new Intent(CheckSpeedService.BROADCAST_UPLOAD_TASK);
            intent.putExtra(COMPLETE_SIMPLE_TASK, 0);
            sendBroadcast(intent);
            int repeatUpload = 0;
            for (int i = 0; i < files.length; i++) {
                String currentFile = files[i];
                do{
                    float uploadSpeed = uploadFile(currentFile);
                    dataModelsList.get(i * repeat + repeatUpload).setUploadSpeed(uploadSpeed);
                    repeatUpload += 1;
                } while (repeatUpload < repeat);
                repeatUpload = 0;

                intent = new Intent(CheckSpeedService.BROADCAST_UPLOAD_TASK);
                intent.putExtra(COMPLETE_SIMPLE_TASK, i + 1);
                sendBroadcast(intent);
            }

            SystemClock.sleep(3000);
            FileUtils.AddDataToFile(dataModelsList, FileUtils.GetRootPath(CheckSpeedService.this) + FileUtils.CHECK_SPEED_RESULT_FILE);
            stopSelf(startId);
        }
    }

    private String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }
    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    /**********************************************************************************************/
    // Http request download file
    /**********************************************************************************************/
    public float downloadFile(String fileName) {
        final String upLoadServerUri = "http://rockstar-onquantum.rhcloud.com/images/" + fileName;//"https://test.amcysoft.com/speedtest/uploads/" + fileName;
        float dataRate = 0;
        HttpURLConnection conn = null;
        try {
            URL url = new URL(upLoadServerUri);
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!path.exists()) path.mkdirs();
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(50000);
            conn.setReadTimeout(50000);
            conn.setDoInput(true);
            conn.connect();

            OutputStream os = new FileOutputStream(new File(path, fileName));
            InputStream is = conn.getInputStream();

            long startTime = System.currentTimeMillis();
            long download = 0;
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
        }finally {
            conn.disconnect();
        }
        return dataRate;
    }
    /**********************************************************************************************/
    // Http request upload file
    /**********************************************************************************************/
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
    }
    private class MyPhoneStateListener extends PhoneStateListener
    {
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength)
        {
            String level;
            super.onSignalStrengthsChanged(signalStrength);
            int asu=signalStrength.getGsmSignalStrength();
            if (asu <= 2 || asu == 99)
                level = "0";
            else if (asu >= 12)
                level = "4";
            else if (asu >= 8)
                level = "3";
            else if (asu >= 5)
                level = "2";
            else
                level = "1";
            gsmStrength=level;
        }

    };
}
