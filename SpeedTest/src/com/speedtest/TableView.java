package com.speedtest;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

public class TableView extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.table_view);
		
		SharedPreferences sp = getSharedPreferences("session", Context.MODE_PRIVATE);
        for (int i = 0; i < 5; i++) {
        	int id1 = getResources().getIdentifier("r"+(i+2)+"c1", "id", getPackageName());
        	TextView tv1 = (TextView)findViewById(id1);
            tv1.setText(sp.getFloat("size"+(i+1), 0)+"");

        	int id2 = getResources().getIdentifier("r"+(i+2)+"c2", "id", getPackageName());
        	TextView tv2 = (TextView)findViewById(id2);
            tv2.setText(sp.getFloat("upload"+(i+1), 0)+"");

        	int id3 = getResources().getIdentifier("r"+(i+2)+"c3", "id", getPackageName());
        	TextView tv3 = (TextView)findViewById(id3);
            tv3.setText(sp.getFloat("download"+(i+1), 0)+"");
        }
	}
}
