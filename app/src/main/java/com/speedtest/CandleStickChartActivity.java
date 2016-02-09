package com.speedtest;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Admin on 1/7/16.
 */
public class CandleStickChartActivity extends Activity {
    private CandleStickChart mChart;
    private SeekBar mSeekBarX, mSeekBarY;
    private TextView tvX, tvY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.candlechart_layout);

        //tvX = (TextView) findViewById(R.id.tvXMax);
        //tvY = (TextView) findViewById(R.id.tvYMax);

        //mSeekBarX = (SeekBar) findViewById(R.id.seekBar1);
        //mSeekBarX.setOnSeekBarChangeListener(this);

        //mSeekBarY = (SeekBar) findViewById(R.id.seekBar2);
        //mSeekBarY.setOnSeekBarChangeListener(this);


        List<DataModel> dataModelList = FileUtils.ParseDataFile(this,FileUtils.GetRootPath(this) + FileUtils.CHECK_SPEED_RESULT_FILE);
        DataModel[] dataModels;

        if(dataModelList != null && dataModelList.size() > 0) {
            DataModel dataModelFile1 = DataModel.CalculateSpeedForParticularFile(MainActivity.files[0], dataModelList);
            DataModel dataModelFile2 = DataModel.CalculateSpeedForParticularFile(MainActivity.files[1], dataModelList);
            DataModel dataModelFile3 = DataModel.CalculateSpeedForParticularFile(MainActivity.files[2], dataModelList);
            DataModel dataModelFile4 = DataModel.CalculateSpeedForParticularFile(MainActivity.files[3], dataModelList);
            DataModel dataModelFile5 = DataModel.CalculateSpeedForParticularFile(MainActivity.files[4], dataModelList);

            dataModels = new DataModel[]{dataModelFile1, dataModelFile2, dataModelFile3, dataModelFile4, dataModelFile5};

            mChart = (CandleStickChart) findViewById(R.id.chart1);
            mChart.setDescription("");
            mChart.setMaxVisibleValueCount(100);
            mChart.setPinchZoom(false);
            mChart.setDrawGridBackground(false);



            XAxis xAxis = mChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setSpaceBetweenLabels(2);
            xAxis.setDrawGridLines(true);

            YAxis leftAxis = mChart.getAxisLeft();
//        leftAxis.setEnabled(false);
            //leftAxis.setLabelCount(7, false);
            leftAxis.setDrawGridLines(false);
            //leftAxis.setDrawAxisLine(false);
            //leftAxis.setStartAtZero(false);

            YAxis rightAxis = mChart.getAxisRight();
            rightAxis.setEnabled(false);
//        rightAxis.setStartAtZero(false);


            mChart.getLegend().setEnabled(false);

             Legend l = mChart.getLegend();
             l.setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
             l.setFormSize(8f);
             l.setFormToTextSpace(4f);
             l.setXEntrySpace(6f);

            //mChart.setDrawLegend(false);

            //Legend l = mChart.getLegend();
            //l.setPosition(Legend.LegendPosition.BELOW_CHART_RIGHT);

            mChart.getAxisRight().setEnabled(false);

            mChart.resetTracking();

            ArrayList<CandleEntry> yVals1 = new ArrayList<CandleEntry>();
            ArrayList<CandleEntry> yVals2 = new ArrayList<CandleEntry>();

            for (int i = 0; i < 5; i++) {
                yVals1.add(new CandleEntry(i,
                        dataModels[i].getDownloadSpeed() + 10,
                        dataModels[i].getDownloadSpeed() -20,
                        dataModels[i].getDownloadSpeed(),
                        dataModels[i].getDownloadSpeed() - 10)
                );
            }

            for (int i = 0; i < 5; i++) {
                yVals2.add(new CandleEntry(i,
                        dataModels[i].getUploadSpeed() + 10,
                        dataModels[i].getUploadSpeed() - 20,
                        dataModels[i].getUploadSpeed(),
                        dataModels[i].getUploadSpeed() - 10)
                );
            }

            /*ArrayList<String> xVals = new ArrayList<String>();
            for (int i = 0; i < 5; i++) {
                xVals.add("" + (1990 + i));
            }*/
            ArrayList<String> xVals = new ArrayList<String>();
            for (int i = 0; i < 5; i++) {
                xVals.add((dataModels[i].getFileSize() / 1024) + " kb");
            }

            CandleDataSet set1 = new CandleDataSet(yVals1, "Download");
            set1.setAxisDependency(YAxis.AxisDependency.LEFT);
//        set1.setColor(Color.rgb(80, 80, 80));
            set1.setShadowColor(Color.RED);
            set1.setShadowWidth(0.7f);
            set1.setDecreasingColor(Color.RED);
            set1.setDecreasingPaintStyle(Paint.Style.STROKE);
            set1.setIncreasingColor(Color.rgb(255, 0, 0));
            set1.setIncreasingPaintStyle(Paint.Style.STROKE);
            //set1.setHighlightLineWidth(1f);

            CandleDataSet set2 = new CandleDataSet(yVals2, "Upload");
            set2.setAxisDependency(YAxis.AxisDependency.LEFT);
//        set1.setColor(Color.rgb(80, 80, 80));
            set2.setShadowColor(Color.BLUE);
            set2.setShadowWidth(0.5f);
            set2.setDecreasingColor(Color.BLUE);
            set2.setDecreasingPaintStyle(Paint.Style.STROKE);
            set2.setIncreasingColor(Color.rgb(0, 0, 255));
            set2.setIncreasingPaintStyle(Paint.Style.STROKE);
            //set1.setHighlightLineWidth(1f);

            List<CandleDataSet> candleDataSets = new ArrayList<>();
            candleDataSets.add(set1);
            candleDataSets.add(set2);

            CandleData data = new CandleData(xVals, candleDataSets);

            mChart.setData(data);
            mChart.invalidate();
        }
    }
}
