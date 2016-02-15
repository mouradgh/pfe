package com.speedtest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.speedtest.FileUtils.FileUtils;
import com.speedtest.model.DataModel;
import com.speedtest.PolynomialRegression;

//import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.regression.SimpleRegression;
//import org.apache.commons.math3.linear.Array2DRowRealMatrix;


import java.util.List;

/**
 * Created by Admin on 2/3/16.
 */
public class SimpleRegressionActivity extends Activity {

    private EditText editText;
    private TextView predictText;
    private Button calculate;
    private TextView label;
    private LinearLayout layout;

    private SimpleRegression downloadDataSimpleRegression = new SimpleRegression();
    private SimpleRegression uploadDataSimpleRegression = new SimpleRegression();

    private PolynomialRegression downloadDataPolynomialRegression;
    private PolynomialRegression uploadDataPolynomialRegression;


    private double[][] dataDownload;
    private double[][] dataUpload;

    //Polynomial regression down/up speed/file size
    private double [] prdownspeed;
    private double [] prupspeed;

    private double [] prdownsize;
    private double [] prupsize;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_regression);

        layout = (LinearLayout) findViewById(R.id.linerLayout);
        editText = (EditText)findViewById(R.id.editText);
        predictText = (TextView)findViewById(R.id.editText2);
        calculate = (Button)findViewById(R.id.button3);
        label = (TextView)findViewById(R.id.textView2);

        List<DataModel> dataModelList = FileUtils.ParseDataFile(this, FileUtils.GetRootPath(this) + FileUtils.CHECK_SPEED_RESULT_FILE);

        if(dataModelList != null && dataModelList.size() > 0) {
            label.setVisibility(View.GONE);
            layout.setVisibility(View.VISIBLE);
            editText.setVisibility(View.VISIBLE);
            calculate.setVisibility(View.VISIBLE);
            predictText.setVisibility(View.VISIBLE);


            dataDownload = new double[dataModelList.size()][2];
            dataUpload = new double[dataModelList.size()][2];


            for (int i = 0; i < dataModelList.size() - 1; i++) {
                Log.i("info", " model = " + dataModelList.get(i).toString());
                dataDownload[i][0] = (double) dataModelList.get(i).getFileSize();
                dataDownload[i][1] = (double) dataModelList.get(i).getDownloadSpeed();

                prdownspeed[i] = (double) dataModelList.get(i).getDownloadSpeed();
                prdownsize[i] = (double) dataModelList.get(i).getFileSize();

                dataUpload[i][0] = (double) dataModelList.get(i).getFileSize();
                dataUpload[i][1] = (double) dataModelList.get(i).getUploadSpeed();

                prupspeed[i] = (double) dataModelList.get(i).getUploadSpeed();
                prupsize[i] = (double) dataModelList.get(i).getFileSize();
            }


            downloadDataSimpleRegression.addData(dataDownload);
            uploadDataSimpleRegression.addData(dataUpload);

            //RealMatrix rmDown = new Array2DRowRealMatrix(dataDownload);
            downloadDataPolynomialRegression = new PolynomialRegression(prdownsize, prdownspeed, 2, "Download speed");

            //RealMatrix rmUp = new Array2DRowRealMatrix(dataUpload);
            uploadDataPolynomialRegression = new PolynomialRegression(prupsize, prupspeed, 2, "Upload speed");

            calculate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CalculateRegression();
                }
            });
        }
    }


    private void CalculateRegression() {
        try {
            double predict = Double.parseDouble(predictText.getText().toString());

            editText.getText().append("Predict value = " + Double.toString(predict) + "\n");
            editText.getText().append("Download data SR:" + "\n");
            //editText.getText().append("Intercept = " + Double.toString(downloadDataSimpleRegression.getIntercept()) + "\n");
            //editText.getText().append("Slope = " + Double.toString(downloadDataSimpleRegression.getSlope()) + "\n");
            //editText.getText().append("Slope Std Err = " + Double.toString(downloadDataSimpleRegression.getSlopeStdErr()) + "\n");
            editText.getText().append("R squared = " + Double.toString(downloadDataSimpleRegression.getR()) + "\n");
            editText.getText().append("Predict = " + Double.toString(predict/(downloadDataSimpleRegression.predict(predict)*1000)));
            editText.getText().append("\n\n");

            editText.getText().append("Upload data SR:" + "\n");
            //editText.getText().append("Intercept = " + Double.toString(uploadDataSimpleRegression.getIntercept()) + "\n");
            //editText.getText().append("Slope = " + Double.toString(uploadDataSimpleRegression.getSlope()) + "\n");
            //editText.getText().append("Slope Std Err = " + Double.toString(uploadDataSimpleRegression.getSlopeStdErr()) + "\n");
            editText.getText().append("R squared = " + Double.toString(uploadDataSimpleRegression.getR()) + "\n");
            editText.getText().append("Predict = " + Double.toString(predict/(uploadDataSimpleRegression.predict(predict)*1000)));
            editText.getText().append("\n" + "--------------------------------------------------------------" + "\n");

            editText.getText().append("Download data PR:" + "\n");
            editText.getText().append("R squared = " + Double.toString(downloadDataPolynomialRegression.R2()) + "\n");
            editText.getText().append("Predict = " + Double.toString(predict/(downloadDataPolynomialRegression.predict(predict)*1000)));
            editText.getText().append("\n\n");

            editText.getText().append("Upload data PR:" + "\n");
            editText.getText().append("R squared = " + Double.toString(uploadDataPolynomialRegression.R2()) + "\n");
            editText.getText().append("Predict = " + Double.toString(predict/(uploadDataPolynomialRegression.predict(predict)*1000)));
            editText.getText().append("\n" + "--------------------------------------------------------------" + "\n");
        } catch (NumberFormatException e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Error");
            builder.setMessage("Incorrect number format, example 1.5");
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();

            e.printStackTrace();
        }
    }
}
