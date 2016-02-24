package com.speedtest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.math3.stat.descriptive.*;

//import com.github.mikephil.charting.*;

//import com.github.PhilJay.MPAndroidChart.MPChartLib;

/*
import com.github.PhilJay.MPAndroidChart.MPChartLib.src.com.github.mikephil.charting.charts.CandleStickChart;
import com.github.PhilJay.MPAndroidChart.MPChartLib.src.com.github.mikephil.charting.components.Legend;
import com.github.PhilJay.MPAndroidChart.MPChartLib.src.com.github.mikephil.charting.components.XAxis;
import com.github.PhilJay.MPAndroidChart.MPChartLib.src.com.github.mikephil.charting.components.YAxis;
import com.github.PhilJay.MPAndroidChart.MPChartLib.src.com.github.mikephil.charting.data.CandleData;
import com.github.PhilJay.MPAndroidChart.MPChartLib.src.com.github.mikephil.charting.data.CandleDataSet;
import com.github.PhilJay.MPAndroidChart.MPChartLib.src.com.github.mikephil.charting.data.CandleEntry;
*/

import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;


import com.speedtest.FileUtils.FileUtils;
import com.speedtest.model.DataModel;


import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Admin on 1/7/16.
 */
public class CandleStickChartActivity extends Activity {

    String data_str_upload,data_str_download;

    List<DataModel> dataModelList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.candlechart_layout);


        dataModelList = FileUtils.ParseDataFile(this, FileUtils.GetRootPath(this) + FileUtils.CHECK_SPEED_RESULT_FILE);
        DataModel[] dataModels;

        if (dataModelList != null && dataModelList.size() > 0) {
            DataModel dataModelFile1 = DataModel.CalculateSpeedForParticularFile(MainActivity.files[0], dataModelList);
            DataModel dataModelFile2 = DataModel.CalculateSpeedForParticularFile(MainActivity.files[1], dataModelList);
            DataModel dataModelFile3 = DataModel.CalculateSpeedForParticularFile(MainActivity.files[2], dataModelList);
            DataModel dataModelFile4 = DataModel.CalculateSpeedForParticularFile(MainActivity.files[3], dataModelList);
            DataModel dataModelFile5 = DataModel.CalculateSpeedForParticularFile(MainActivity.files[4], dataModelList);

            dataModels = new DataModel[]{dataModelFile1, dataModelFile2, dataModelFile3, dataModelFile4, dataModelFile5};
        }

        DescriptiveStatistics stat = new DescriptiveStatistics();

        Double max, min, average, Q1, Q3, IQR, LowerWhisker, UpperWhisker;
        DecimalFormat formatter=new DecimalFormat();
        formatter.setMaximumFractionDigits(2);
        try {

            //-----Download Calculation
            for (int i = 0; i < dataModelList.size(); i++)
                stat.addValue(Double.parseDouble(dataModelList.get(i).toString().split(",")[2].toString()));

            max = stat.getMax();
            min = stat.getMin();
            average = stat.getMean();
            Q1 = stat.getPercentile(25.0);
            Q3 = stat.getPercentile(75.0);
            IQR = Q3 - Q1;
            LowerWhisker = Q1 - (1.5 * IQR);
            UpperWhisker = Q3 + (1.5 * IQR);



            data_str_download = "Maximum : " + formatter.format(max) + "\nMinimum : " + formatter.format(min) + "\nMean : " + formatter.format(average) + "\nQ1 : " + formatter.format(Q1) + "\nQ3 : " + formatter.format(Q3) + "\nIQR : " + formatter.format(IQR) + "\nLowerWhisker : " + formatter.format(LowerWhisker) + "\nUpperWhisker : " + formatter.format(UpperWhisker);

            //--------Upload Calculation
            stat.clear();
            for (int i = 0; i < dataModelList.size(); i++)
                stat.addValue(Double.parseDouble(dataModelList.get(i).toString().split(",")[3].toString()));

            max = stat.getMax();
            min = stat.getMin();
            average = stat.getMean();
            Q1 = stat.getPercentile(25.0);
            Q3 = stat.getPercentile(75.0);
            IQR = Q3 - Q1;
            LowerWhisker = Q1 - (1.5 * IQR);
            UpperWhisker = Q3 + (1.5 * IQR);

            data_str_upload = "Maximum : " + formatter.format(max) + "\nMinimum : " + formatter.format(min) + "\nMean : " + formatter.format(average) + "\nQ1 : " + formatter.format(Q1) + "\nQ3 : " + formatter.format(Q3) + "\nIQR : " + formatter.format(IQR) + "\nLowerWhisker : " + formatter.format(LowerWhisker) + "\nUpperWhisker : " + formatter.format(UpperWhisker);

            final TextView data = (TextView) findViewById(R.id.textView4);
            final RadioButton r=(RadioButton)findViewById(R.id.radioButton);
            final RadioButton r1=(RadioButton)findViewById(R.id.radioButton2);
            Button b=(Button)findViewById(R.id.button5);

            r.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(b) {
                        r1.setChecked(false);
                        data.setText(data_str_download);
                    }
                }
            });

            r1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        r.setChecked(false);
                        data.setText(data_str_upload);
                    }
                }
            });

            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    clipboard.setText(data.getText());
                }
            });
            data.setText(data_str_download);
        } catch (Exception ex) {

        }
    }

    public void displayDownloadStat(View v) {

    }
}
