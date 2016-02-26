package com.speedtest;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;


//import com.github.mikephil.charting.*;


import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.charts.ScatterChart.ScatterShape;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendPosition;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.renderer.LineChartRenderer;


import com.speedtest.FileUtils.FileUtils;
import com.speedtest.model.DataModel;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.regression.SimpleRegression;


public class ScatterPlot extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scatter_plot);
		
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.scatter_plot);

        List<DataModel> dataModelList = FileUtils.ParseDataFile(this,FileUtils.GetRootPath(this) + FileUtils.CHECK_SPEED_RESULT_FILE);
        DataModel[] dataModels;

        if(dataModelList != null && dataModelList.size() > 0) {
            DataModel dataModelFile1 = DataModel.CalculateSpeedForParticularFile(MainActivity.files[0],dataModelList);
            DataModel dataModelFile2 = DataModel.CalculateSpeedForParticularFile(MainActivity.files[1],dataModelList);
            DataModel dataModelFile3 = DataModel.CalculateSpeedForParticularFile(MainActivity.files[2],dataModelList);
            DataModel dataModelFile4 = DataModel.CalculateSpeedForParticularFile(MainActivity.files[3],dataModelList);
            DataModel dataModelFile5 = DataModel.CalculateSpeedForParticularFile(MainActivity.files[4],dataModelList);

            dataModels = new DataModel[] { dataModelFile1, dataModelFile2, dataModelFile3, dataModelFile4, dataModelFile5 };

            CombinedChart ch = (CombinedChart)findViewById(R.id.chart1);
            ch.setDescription("");

            ch.setDrawGridBackground(false);

            ch.setTouchEnabled(true);

            // enable scaling and dragging
            ch.setDragEnabled(true);
            ch.setScaleEnabled(true);

            ch.setMaxVisibleValueCount(100);
            ch.setPinchZoom(true);

            Legend l = ch.getLegend();
            l.setPosition(LegendPosition.BELOW_CHART_RIGHT);

            ch.getAxisRight().setEnabled(false);

            XAxis xl = ch.getXAxis();
            xl.setDrawGridLines(true);

            //SharedPreferences sp = getSharedPreferences("session", Context.MODE_PRIVATE);
            ArrayList<String> xVals = new ArrayList<String>();
            for (int i = 0; i < 5; i++) {
                //xVals.add((sp.getFloat("size"+(i+1), 0)) + "");
                xVals.add((dataModels[i].getFileSize() / 1024) + " kb");
            }

            ArrayList<Entry> yVals1 = new ArrayList<Entry>();
            ArrayList<Entry> yVals2 = new ArrayList<Entry>();

            ArrayList<Entry> line1 = new ArrayList<Entry>();
            ArrayList<Entry> line2 = new ArrayList<Entry>();

            /**********Check for regression**************/
            double[][] dataDownload = new double[dataModelList.size()][2];
            double[][] dataUpload = new double[dataModelList.size()][2];

            for (int i = 0; i < dataModelList.size() - 1; i++) {
                Log.i("info", " model = " + dataModelList.get(i).toString());
                dataDownload[i][0] = (double) dataModelList.get(i).getFileSize();
                dataDownload[i][1] = (double) dataModelList.get(i).getDownloadSpeed();

                dataUpload[i][0] = (double) dataModelList.get(i).getFileSize();
                dataUpload[i][1] = (double) dataModelList.get(i).getUploadSpeed();
            }

            /*****Simple Regression******/
            SimpleRegression downloadDataSimpleRegression = new SimpleRegression();
            SimpleRegression uploadDataSimpleRegression = new SimpleRegression();
            downloadDataSimpleRegression.addData(dataDownload);
            uploadDataSimpleRegression.addData(dataUpload);

            /*****Polynomial Regression****/
            RealMatrix rmDown = new Array2DRowRealMatrix(dataDownload);
            RealMatrix rmUp = new Array2DRowRealMatrix(dataUpload);

            PolynomialRegression downloadDataPolynomialRegression = new PolynomialRegression(rmDown.getColumn(0), rmDown.getColumn(1), 2, "Download speed");
            PolynomialRegression uploadDataPolynomialRegression = new PolynomialRegression(rmUp.getColumn(0), rmUp.getColumn(1), 2, "Upload speed");

            for (int i = 0; i < 5; i++) {
                float point;
                if(downloadDataSimpleRegression.getR() > downloadDataPolynomialRegression.R2()) {
                    point = (float)downloadDataSimpleRegression.predict(dataModels[i].getFileSize());
                }
                else {
                    point = (float)downloadDataPolynomialRegression.predict(dataModels[i].getFileSize());
                }
                line1.add(new Entry(point, i));

                yVals1.add(new Entry(dataModels[i].getDownloadSpeed(), i));
            }

            for (int i = 0; i < 5; i++) {
                float point;
                if(downloadDataSimpleRegression.getR() > downloadDataPolynomialRegression.R2()) {
                    point = (float)uploadDataSimpleRegression.predict(dataModels[i].getFileSize());
                }
                else {
                    point = (float)uploadDataPolynomialRegression.predict(dataModels[i].getFileSize());
                }
                line2.add(new Entry(point, i));

                yVals2.add(new Entry(dataModels[i].getUploadSpeed(), i));
            }

            // create a dataset and give it a type
            ScatterDataSet set1 = new ScatterDataSet(yVals1, "Download");
            set1.setScatterShape(ScatterShape.SQUARE);
            set1.setColor(ColorTemplate.COLORFUL_COLORS[0]);
            ScatterDataSet set2 = new ScatterDataSet(yVals2, "Upload");
            set2.setScatterShape(ScatterShape.TRIANGLE);
            set2.setColor(ColorTemplate.COLORFUL_COLORS[1]);
            set1.setScatterShapeSize(8f);
            set2.setScatterShapeSize(8f);


            LineDataSet lineSet1 = new LineDataSet(line1, "Download");
            lineSet1.setColor(ColorTemplate.COLORFUL_COLORS[0]);
            LineDataSet lineSet2 = new LineDataSet(line2, "Upload");
            lineSet2.setColor(ColorTemplate.COLORFUL_COLORS[1]);


            ArrayList<ScatterDataSet> dataSets = new ArrayList<ScatterDataSet>();
            dataSets.add(set1); // add the datasets
            dataSets.add(set2);

            ArrayList<LineDataSet> lineSets = new ArrayList<LineDataSet>();
            lineSets.add(lineSet1); // add the datasets
            lineSets.add(lineSet2);

            // create a data object with the datasets
            ScatterData data1 = new ScatterData(xVals, dataSets);
            LineData data2 = new LineData(xVals, lineSets);
            CombinedData cd = new CombinedData(xVals);
            cd.setData(data1);
            cd.setData(data2);

            ch.setData(cd);
            ch.invalidate();
        }
	}
}
