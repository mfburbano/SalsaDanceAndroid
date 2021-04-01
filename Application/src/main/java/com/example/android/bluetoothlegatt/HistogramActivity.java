package com.example.android.bluetoothlegatt;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mlram on 20/01/2018.
 */

public class HistogramActivity extends Activity {
    GraphView graph;
    LineGraphSeries<DataPoint> series;
    LineGraphSeries<DataPoint> seriesHistogram;
    DataBase myDb;// call instance to my DataBase
    List<Integer> DataAccBINNED = new ArrayList<Integer>();// creates a list where the list from the binned data gets in
    List<Integer> DataAccHISTOGRAM = new ArrayList<Integer>();// creates a list where the list from the binned data gets in
    List<String> DataIntentHISTOGRAM = new ArrayList<String>();// creates a list where the list from the binned data gets in
    List<Double> DataMeanStandard = new ArrayList<Double>();// creates a list where the list from the binned data gets in
    private final static String TAG = BluetoothLeService.class.getSimpleName();
    private TextView TextMean;//text view used by the mean
    private TextView TextStandard;//text view used by the standard
    Filters myFilter = new Filters();
    LineGraphSeries<DataPoint> seriesHISTO;
    SQLiteDatabase sqLiteDatabase;
    @Override

    protected void onResume() {
        super.onResume();
        DataIntentHISTOGRAM.clear();
        Intent activityThatCalled = getIntent();
        DataIntentHISTOGRAM.addAll(activityThatCalled.getStringArrayListExtra("callingActivity"));

        graph.removeAllSeries();
        seriesHistogram = new LineGraphSeries<DataPoint>();//calls function from database and makes it ready to add to the graph
        seriesHistogram.setColor(Color.GREEN);
        double y, x;//initialize variables that will hold the math operations
        //DataAccMAf.addAll(myFilter.MAF1(res)};

        x = 0;
        y = 10000;
        StringBuffer data_BLE_DataBase_Raw = new StringBuffer();//if there is data creates a chain of strings
        if (DataIntentHISTOGRAM.size() != 0) {
            //count1 = myDb.getAllData1().getCount();
            for (int i = 0; i < DataIntentHISTOGRAM.size()-2; i++) {
                x = i;
                y = Double.parseDouble(DataIntentHISTOGRAM.get(i));
                //y = Math.sin(x);
                seriesHistogram.appendData(new DataPoint(x, y), true, DataIntentHISTOGRAM.size());
            }
            graph.addSeries(seriesHistogram);//shows the graph
            DataMeanStandard.addAll(myFilter.Mean(DataIntentHISTOGRAM));//applies the logic to get the mean and the standard deviation
            displayDataMean(DataMeanStandard.get(0).toString());//displays the mean
            displayDataStandard(DataMeanStandard.get(1).toString());//displays the standard deviation
        } else
            showMessage("Error", "no data in the dataBase");
        return;

    }

    /////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mav_graph);
        TextMean = (TextView) findViewById(R.id.ViewMean);
        TextStandard = (TextView) findViewById(R.id.ViewStandard);
        graph = (GraphView) findViewById(R.id.graphHistogram);//makes the instance to the graph
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(300);
        graph.getViewport().setMaxX(3000);
        graph.getViewport().setScrollable(true);
        GridLabelRenderer glr = graph.getGridLabelRenderer();
        glr.setPadding(32);// change the size of the table on graph view

    }



    public void OnclickGoBack(View v){
        Intent GoBack = new Intent(this, DeviceControlActivity.class);
        final int result = 1;
        startActivity(GoBack);
    }

    public void showMessage(String title, String Message){//method to show messages
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(Message);
        builder.show();
    }
    private void displayDataMean(String data) {
        if (data != null) {
            String SubData = data.substring(0,9);
            TextMean.setText(SubData);
        }
    }
    private void displayDataStandard(String data) {
        if (data != null) {
            String SubData = data.substring(0,9);
            TextStandard.setText(SubData);
        }
    }
}
