package com.speedtest;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import com.speedtest.FileUtils.FileUtils;
import com.speedtest.model.DataModel;

import java.util.List;

public class TableView extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.table_view);

		List<DataModel> dataModelList = FileUtils.ParseDataFile(this,FileUtils.GetRootPath(this) + FileUtils.CHECK_SPEED_RESULT_FILE);
		DataModel[] dataModels;

		if(dataModelList != null && dataModelList.size() > 0) {
			DataModel dataModelFile1 = DataModel.CalculateSpeedForParticularFile(MainActivity.files[0],dataModelList);
			DataModel dataModelFile2 = DataModel.CalculateSpeedForParticularFile(MainActivity.files[1],dataModelList);
			DataModel dataModelFile3 = DataModel.CalculateSpeedForParticularFile(MainActivity.files[2],dataModelList);
			DataModel dataModelFile4 = DataModel.CalculateSpeedForParticularFile(MainActivity.files[3],dataModelList);
			DataModel dataModelFile5 = DataModel.CalculateSpeedForParticularFile(MainActivity.files[4],dataModelList);

			dataModels = new DataModel[] { dataModelFile1, dataModelFile2, dataModelFile3, dataModelFile4, dataModelFile5 };
			//SharedPreferences sp = getSharedPreferences("session", Context.MODE_PRIVATE);
			for (int i = 0; i < 5; i++) {
				int id1 = getResources().getIdentifier("r"+(i+2)+"c1", "id", getPackageName());
				TextView tv1 = (TextView)findViewById(id1);
				//tv1.setText(sp.getFloat("size"+(i+1), 0)+"");
				tv1.setText(Long.toString(dataModels[i].getFileSize()));

				int id2 = getResources().getIdentifier("r"+(i+2)+"c2", "id", getPackageName());
				TextView tv2 = (TextView)findViewById(id2);
				//tv2.setText(sp.getFloat("upload"+(i+1), 0)+"");
				tv2.setText(Float.toString(dataModels[i].getUploadSpeed()));

				int id3 = getResources().getIdentifier("r"+(i+2)+"c3", "id", getPackageName());
				TextView tv3 = (TextView)findViewById(id3);
				//tv3.setText(sp.getFloat("download"+(i+1), 0)+"");
				tv3.setText(Float.toString(dataModels[i].getDownloadSpeed()));
			}
		}
	}
}
