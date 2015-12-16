package com.speedtest;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.WindowManager;

import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.charts.ScatterChart.ScatterShape;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendPosition;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

public class ScatterPlot extends Activity {
	
	private ScatterChart mChart;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scatter_plot);
		
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.scatter_plot);

        mChart = (ScatterChart) findViewById(R.id.chart1);
        mChart.setDescription("");

        mChart.setDrawGridBackground(false);

        mChart.setTouchEnabled(true);

        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);

        mChart.setMaxVisibleValueCount(100);
        mChart.setPinchZoom(true);

        Legend l = mChart.getLegend();
        l.setPosition(LegendPosition.BELOW_CHART_RIGHT);
        
        mChart.getAxisRight().setEnabled(false);

        XAxis xl = mChart.getXAxis();
        xl.setDrawGridLines(true);
        
        SharedPreferences sp = getSharedPreferences("session", Context.MODE_PRIVATE);
        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = 0; i < 5; i++) {
            xVals.add((sp.getFloat("size"+(i+1), 0)) + "");
        }

        ArrayList<Entry> yVals1 = new ArrayList<Entry>();
        ArrayList<Entry> yVals2 = new ArrayList<Entry>();

        for (int i = 0; i < 5; i++) {
            yVals1.add(new Entry(sp.getFloat("download"+(i+1), 0), i));
        }

        for (int i = 0; i < 5; i++) {
            yVals2.add(new Entry(sp.getFloat("upload"+(i+1), 0), i));
        }

        ScatterDataSet set1 = new ScatterDataSet(yVals1, "Download");
        set1.setScatterShape(ScatterShape.SQUARE);
        set1.setColor(ColorTemplate.COLORFUL_COLORS[0]);
        ScatterDataSet set2 = new ScatterDataSet(yVals2, "Upload");
        set2.setScatterShape(ScatterShape.TRIANGLE);
        set2.setColor(ColorTemplate.COLORFUL_COLORS[1]);

        set1.setScatterShapeSize(8f);
        set2.setScatterShapeSize(8f);

        ArrayList<ScatterDataSet> dataSets = new ArrayList<ScatterDataSet>();
        dataSets.add(set1); 
        dataSets.add(set2);

        ScatterData data = new ScatterData(xVals, dataSets);

        mChart.setData(data);
        mChart.invalidate();
	}
}
