package com.speedtest.services;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("HH:mm:ss");
    private LatLng latLng;
    private ExecutorService executorService = null;

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

            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
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
                    dataModelsList.get(i * repeat + repeatDownload).setVersion(new String(Build.VERSION.RELEASE));
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
}
