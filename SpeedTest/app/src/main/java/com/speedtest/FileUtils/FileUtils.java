package com.speedtest.FileUtils;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.speedtest.model.DataModel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Admin on 1/7/16.
 */
public final class FileUtils {

    public static final String CHECK_SPEED_RESULT_FILE = "/check_speed_result.csv";

    private FileUtils(){}

    public static String GetRootPath(Context context) {
        File rootPath = new File(context.getExternalFilesDir(null).toString());
        if(rootPath.exists() == false) {
            if(rootPath.mkdirs() == false) {
                return null;
            }
        }
        return rootPath.toString();
    }

    public static void AddDataToFile(List<DataModel>dataModels, String path) {
        if(dataModels == null || dataModels.size() == 0)
            return;
        boolean addTableHeader = false;

        File file = new File(path);
        FileWriter fileWriter = null;
        try {
            if(!file.exists()) {
                file.createNewFile();
                addTableHeader = true;
            }
            fileWriter = new FileWriter(file,true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            if(addTableHeader) {
                bufferedWriter.write(DataModel.tableHeader);
                bufferedWriter.newLine();
            }

            for (DataModel dataModel : dataModels) {
                bufferedWriter.write(dataModel.toString());
                bufferedWriter.newLine();
            }
            bufferedWriter.flush();
            bufferedWriter.close();
            fileWriter.close();

        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<DataModel> ParseDataFile(Context context, String path) {
        File file = new File(path);
        if(!file.exists())
            return null;

        List<DataModel>dataModelList = new ArrayList<>();

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String str = reader.readLine();
            str = reader.readLine();
            while (str != null) {
                String[] parseData = str.split(",");
                DataModel dataModel = new DataModel();
                dataModel.setFileName(parseData[0]);
                dataModel.setFileSize(Long.parseLong(parseData[1]));
                dataModel.setDownloadSpeed(Float.parseFloat(parseData[2]));
                dataModel.setUploadSpeed(Float.parseFloat(parseData[3]));
                dataModel.setDate(parseData[4]);
                dataModel.setTime(parseData[5]);
                dataModel.setConnectionType(parseData[6]);
                double lat = Double.parseDouble(parseData[7]);
                double lng = Double.parseDouble(parseData[8]);
                dataModel.setPhoneName(parseData[9]);
                dataModel.setVersion(parseData[10]);

                dataModel.setLocation(new LatLng(lat, lng));
                dataModelList.add(dataModel);
                Log.i("info"," PARSE : " + dataModel.toString());
                str = reader.readLine();
            }
        }catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return dataModelList;
    }

    /*public static List<DataModel> ParseDataFile(Context context, String path) {
        File file = new File(path);
        if(!file.exists())
            return null;

        List<DataModel>dataModelList = new ArrayList<>();

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String str = reader.readLine();
            while (str != null) {
                String[] parseData = str.split(",");
                String[] fileName  = parseData[0].split("|");
                String[] fileSize = parseData[1].split("|");
                String[] downloadSpeed = parseData[2].split("|");
                String[] uploadSpeed = parseData[3].split("|");
                String[] dateTime = parseData[4].split(":");
                String[] connectionType = parseData[5].split("|");

                DataModel dataModel = new DataModel();
                dataModel.setFileName(fileName[1].replace(" ",""));
                dataModel.setFileSize(Long.parseLong(fileSize[1].replace(" ","")));
                dataModel.setDownloadSpeed(Float.parseFloat(downloadSpeed[1].replace(" ","")));
                dataModel.setUploadSpeed(Float.parseFloat(uploadSpeed[1].replace(" ","")));
                dataModel.setDate(dateTime[1].replace(" ",""));
                dataModel.setConnectionType(connectionType[1].replace(" ",""));

                dataModelList.add(dataModel);
                Log.i("info"," PARSE : " + dataModel.toString());
                str = reader.readLine();
            }
        }catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return dataModelList;
    }*/
}
