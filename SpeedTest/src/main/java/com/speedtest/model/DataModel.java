package com.speedtest.model;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by Admin on 1/7/16.
 */
public class DataModel {
    private String fileName = null;
    private long fileSize = 0;
    private float downloadSpeed = 0;
    private float uploadSpeed = 0;
    private String date = "";
    private String time ="";
    private String internetType = "";
    private LatLng latLng = new LatLng(0,0);
    private String phoneName = "";
    private String version = "";

    public static String tableHeader = "File name, File size, Download speed, Upload speed, Date, Time, Connection type, Latitude, Longitude, Phone name, Android version";

    public DataModel() {}

    // Setter
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
    public void setDownloadSpeed(float speed) {
        this.downloadSpeed = speed;
    }
    public void setUploadSpeed(float speed) {
        this.uploadSpeed = speed;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public void setTime(String time) {
        this.time = time;
    }
    public void setConnectionType(String internetType) {
        this.internetType = internetType;
    }
    public void setLocation(LatLng latLng) {
        this.latLng = latLng;
    }
    public void setPhoneName(String phoneName) {
        this.phoneName = phoneName;
    }
    public void setVersion(String version) {
        this.version = version;
    }

    // Getter
    public String getFileName() { return this.fileName; }
    public long getFileSize() { return this.fileSize; }
    public float getDownloadSpeed() { return this.downloadSpeed; }
    public float getUploadSpeed() { return this.uploadSpeed; }
    public String getPhoneName() { return this.phoneName; }
    public String getVersion() { return this.version; }

    /*@Override
    public String toString() {
        return "File name|" + fileName + "," + " Size|" + fileSize + "," + " Download speed|" + downloadSpeed + "," + " Upload speed|" + uploadSpeed + ","
                + " Date Time|" + date + "," + " Time|" + time + "," + " Internet type|" + internetType;
    }*/

    @Override
    public String toString() {
        return fileName + "," + fileSize + "," + downloadSpeed + "," + uploadSpeed + "," + date + "," + time + "," + internetType + "," + latLng.latitude + ","
                + latLng.longitude + "," + phoneName + "," + version;
    }

    public static DataModel CalculateSpeedForParticularFile(String fileName, List<DataModel>dataModels) {
        DataModel retDataModel = null;
        int count = 0;
        float download = 0;
        float upload = 0;
        for (DataModel dataModel : dataModels) {
            if(dataModel.getFileName().equals(fileName)) {
                if(retDataModel == null) {
                    retDataModel = new DataModel();
                    retDataModel.setFileName(fileName);
                    retDataModel.setFileSize(dataModel.getFileSize());
                }
                download += dataModel.getDownloadSpeed();
                upload += dataModel.getUploadSpeed();
                count++;
            }
            if(retDataModel != null) {
                retDataModel.setDownloadSpeed(download / count);
                retDataModel.setUploadSpeed(upload / count);
            }
        }
        return retDataModel;
    }
}
