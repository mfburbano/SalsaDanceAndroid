/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.bluetoothlegatt;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.GridLayout;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity extends Activity {
    DataBase myDb;// call instance to my DataBase
    private Filters myFilter = new Filters();
    TextView data_value;//instance value of the text view
    Button Button3;// instance button view
    Button Button6;//instance to burron view MAF
    Button Button9;//instance to burron view MAF
    Button Button10;//instance to burron view HBMF
    Button Button11;//instance to burron view HMAF
    Button Button12;//instance to burron view HIOT
    Button Button14;//instance to burron view M-RAW
    Button Button15;//instance to burron view M-MAF
    Button Button16;//instance to burron view M-IOT
    Button Button17;//instance to burron view M-BMF
    GraphView graph;
    LineGraphSeries<DataPoint> series;
    LineGraphSeries<DataPoint> seriesMAF;
    LineGraphSeries<DataPoint> seriesIOT;
    LineGraphSeries<DataPoint> seriesBMF;
    LineGraphSeries<DataPoint> seriesWindow;
    LineGraphSeries<DataPoint> seriesWindowSD;
    SQLiteDatabase sqLiteDatabase;
    private final static String TAG = DeviceControlActivity.class.getSimpleName();


    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private TextView mConnectionState;
    private TextView mDataField;//data field fot the text view
    private String mDeviceName;//name of the device
    private String mDeviceAddress;// address of the device
    private ExpandableListView mGattServicesList;
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =//create an array for the characteristics
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    List<String> plain = new ArrayList<String>();// creates a list where all data from string column gets inserted from table rawdata
    List<Double> DataAccMAf = new ArrayList<Double>();// creates a list where all data from string column gets inserted from table MAF
    List<Double> DataAccIOT = new ArrayList<Double>();// creates a list where all data from string column gets inserted from table IOT
    List<Double> DataAccBMF = new ArrayList<Double>();// creates a list where all data from string column gets inserted from table BMF
    List<Integer> DataAccBINNED = new ArrayList<Integer>();// creates a list where the list from the binned data gets in
    List<Integer> DataAccHISTOGRAM = new ArrayList<Integer>();// creates a list where the list from the binned data gets in
    List<String> ListIntentHistogram = new ArrayList<String>();// creates a list which will be send to the histogram activity
    List<String> ListIntentHistogram1 = new ArrayList<String>();// creates a list which will be send to the histogram activity
    List<String> ListRealRead = new ArrayList<String>();// creates a list which will be send to the histogram activity

    public String[] CSV = new String[4];
    public String[] CSV_maf = new String[1];
    public String[] CSV_iot = new String[1];
    public String[] CSV_bmf = new String[1];

    private volatile Integer EventIntentHistogram = 0;//case for the intent

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    private static volatile int count1 = 0;//variable to check if the data in the count is different than before
    private static volatile int count0 = 10;//variable to check if the data in the count is different than before
    private static volatile int n = 1;
    private static volatile int CSV_count = 0;
    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
            Log.w(TAG, "IM OUT");
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {//receives the data from the characteristicis taken from the BroadcastUpdate
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                //displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {//action when there is data to receive
                //String plain = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                //plain.addAll(intent.getStringArrayListExtra(BluetoothLeService.EXTRA_DATA));//gets data from an array list
                if (intent.getStringArrayListExtra(BluetoothLeService.EXTRA_DATA) != null ) {
                    if (intent.getStringArrayListExtra(BluetoothLeService.EXTRA_DATA).size()>0) {
                        plain.clear();
                        plain.addAll(intent.getStringArrayListExtra(BluetoothLeService.EXTRA_DATA));
                        //String line1 = plain.substring(0, plain.indexOf("\n"));
                        //displ.ñayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));//DISPLAYS YOUR DATA INTO THE TEXTVIEW TAKEN AFTER BEING PARSED BY YOUR FUNCTION
                        displayData(plain.get(0));

                        Boolean isInserted_X = myDb.saveList_X_new(plain.get(0), plain.get(1), plain.get(2), plain);
                        //Boolean isInserted = myDb.saveList(plain.get(0), plain.get(1), plain.get(2), plain);//does similar actions to the FIFO, stores in a list and when its full, stores the DATABASE
                        ListRealRead.addAll(myDb.saveListNew(plain.get(0), plain.get(1), plain.get(2), plain));
                        // Boolean isInserted_X = myDb.saveList(plain.get(0), plain.get(1), plain.get(2), plain);
                        //Boolean isInserted_Y = myDb.saveList(plain.get(0), plain.get(1), plain.get(2), plain);
                        //Boolean isInserted_z = myDb.saveList(plain.get(0), plain.get(1), plain.get(2), plain);
                            //plain.clear();//clean the list so it wont repeat it self
                        //Boolean isInserted = myDb.insertData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));//INSERT YOUR DATA INTO THE DATABASE

                        //if (isInserted == true) {
                            //graph.removeAllSeries();
                            series = new LineGraphSeries<DataPoint>();//calls function from database and makes it ready to add to the graph
                            double y, x;

                            x = 0.0;
                            y = 10000;
                            Cursor res = myDb.getAllData();
                            if (ListRealRead.size() > 0 ) {

                                //count1 = ListRealRead.size();
                                //while (res.moveToNext()) {
                                for (int i = count1; i < ListRealRead.size(); i++) {
                                    x = i;
                                    y = Double.parseDouble(ListRealRead.get(i));
                                    //y = Math.sin(x);
                                    series.appendData(new DataPoint(x, y), true, ListRealRead.size());
                                }
                                count1 = ListRealRead.size()-1;
                                if (count1-n*1000 >= 0){//make sure it restarts the graph each time it goes out of the grph
                                    n++;
                                    graph.getViewport().setMaxX(n*1000);
                                    //graph.removeAllSeries();
                                }//}
                                //graph.addSeries(series);//shows the graph
                            }
                            //else return;

                            //}catch (NumberFormatException e)
                            //{
                            //e.printStackTrace();
                            //y = 10000;
                            //  Log.w(TAG, "PARSING IS NOT WORKING");
                            //}
                            //if (y != 10000) {

                            //}
                            //}


                            //myDb.get();
                            //Toast.makeText(DeviceControlActivity.this, "DATA INSERTED", Toast.LENGTH_LONG).show();
                        //}// else
                            //Toast.makeText(DeviceControlActivity.this, "List INSERTED", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    };



    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
    private final ExpandableListView.OnChildClickListener servicesListClickListner =
            new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                            int childPosition, long id) {
                    if (mGattCharacteristics != null) {
                        final BluetoothGattCharacteristic characteristic =
                                mGattCharacteristics.get(groupPosition).get(childPosition);
                        final int charaProp = characteristic.getProperties();
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                            // If there is an active notification on a characteristic, clear
                            // it first so it doesn't update the data field on the user interface.
                            if (mNotifyCharacteristic != null) {
                                mBluetoothLeService.setCharacteristicNotification(
                                        mNotifyCharacteristic, false);
                                mNotifyCharacteristic = null;
                            }
                            mBluetoothLeService.readCharacteristic(characteristic);
                        }
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            mNotifyCharacteristic = characteristic;
                            mBluetoothLeService.setCharacteristicNotification(
                                    characteristic, true);
                        }
                        return true;
                    }
                    return false;
                }
    };

    private void clearUI() {
        //mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        mDataField.setText(R.string.no_data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.button_control);
        myDb = new DataBase(this);//calling the constructor to create the data base  on this context
        data_value = (TextView) findViewById(R.id.data_value);//makes instance to the text view
        int v = 10;
        Button3 = (Button) findViewById(R.id.Button3);
        Button6 = (Button) findViewById(R.id.Button6);
        Button9 = (Button) findViewById(R.id.button9);//HRAW
        Button10 = (Button) findViewById(R.id.button10);//HBMF
        Button11 = (Button) findViewById(R.id.Button11);//HMAF
        Button12 = (Button) findViewById(R.id.button12);//HIOT
        Button14 = (Button) findViewById(R.id.button14);//M-raw
        Button15 = (Button) findViewById(R.id.button15);//M-maf
        Button16 = (Button) findViewById(R.id.button16);//M-iot
        Button17 = (Button) findViewById(R.id.button17);//M-bmf
        Button10.setVisibility(View.INVISIBLE);
        Button12.setVisibility(View.INVISIBLE);
        Button9.setVisibility(View.VISIBLE);
        Button11.setVisibility(View.INVISIBLE);

        //Button6 = (Button) findViewById(R.id.Button6);
        graph = (GraphView) findViewById(R.id.graph);//makes the instance to the graph
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(100);
        graph.getViewport().setMaxX(1000);
        //graph.getViewport().setScrollable(true);
        graph.getViewport().setScalable(true);
        GridLabelRenderer glr = graph.getGridLabelRenderer();
        glr.setPadding(32);// change the size of the table on graph view

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        sqLiteDatabase = myDb.getWritableDatabase();
        // Sets up UI references.
        //((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
       // mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
      //  mGattServicesList.setOnChildClickListener(servicesListClickListner);
      //  mConnectionState = (TextView) findViewById(R.id.connection_state);
        mDataField = (TextView) findViewById(R.id.data_value);

        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        OnClickView();// call the funcition to display the data stored in my DataBase
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null&&myDb.getAllData()==null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < ListRealRead.size(); i++){
                            Log.w(TAG, "VALOR: "+ ListRealRead.get(i));
                            myDb.insertData(ListRealRead.get(i),myDb.savedListTime.get(i));
                            myDb.insertData4(myDb.savedList_X.get(i), myDb.savedList_Y.get(i), myDb.savedList_Z.get(i));
                        }
                        handlerDataIn.sendEmptyMessage(0);

                    }
                };

                Thread RealThread = new Thread(r);
                RealThread.start();
                Log.w(TAG, "IM OUT");
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    Handler handlerDataIn = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            showMessage("","Data Inserted" );
            return;
        }
    };

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //mConnectionState.setText(resourceId);
            }
        });
    }

    private void displayData(String data) {
        if (data != null) {
            mDataField.setText(data);
        }
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 },
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 }
        );
        mGattServicesList.setAdapter(gattServiceAdapter);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
    public void OnClickWrite(View v){
        if(mBluetoothLeService != null) {
            //mBluetoothLeService.outBleNow();
            mBluetoothLeService.writeCustomCharacteristic(0xAA);

        }
    }

    public void OnClickRead(View v){
        //if(mBluetoothLeService != null) {
          //  mBluetoothLeService.readCustomCharacteristic();
            //Log.w(TAG,"Text View Changed" );
            //data_value.setText("hola");
        //}
        Button10.setVisibility(View.INVISIBLE);
        Button12.setVisibility(View.INVISIBLE);
        Button9.setVisibility(View.VISIBLE);
        Button11.setVisibility(View.INVISIBLE);
        Button14.setVisibility(View.VISIBLE);
        Button15.setVisibility(View.INVISIBLE);
        Button16.setVisibility(View.INVISIBLE);
        Button17.setVisibility(View.INVISIBLE);
        shoeGraphRaw();
    }

    public void OnClickView(){//method use when click button VIEW
        Button3.setOnClickListener(
                new View.OnClickListener() {//creates the logic in case of pressing the button
                    @Override
                    public void onClick(View view) {
                        Cursor res = myDb.getAllData();//calls the function getALlData
                        if (res.getCount()==0){
                            //we get here if there is nothing on the table or no result
                            showMessage("Error", "no data in the dataBase");
                            return;
                        }
                        StringBuffer data_BLE_DataBase_Raw = new StringBuffer();//if there is data creates a chain of strings
                        while (res.moveToNext()){//moves trhoughout the table
                            data_BLE_DataBase_Raw.append("Id: "+ res.getString(0)+"\n");//gets all of the ID's
                            data_BLE_DataBase_Raw.append("Data: "+ res.getString(1)+"\n");//gets all of the Data
                            data_BLE_DataBase_Raw.append("Time: "+ res.getString(2)+"\n\n");//gets all of the Data
                        }

                        //show all DATA

                        showMessage("Data", data_BLE_DataBase_Raw.toString());
                    }
                }
        );
    }

    //////////////Moving Average Filter
    public void OnclickMAF(View v){
        Button10.setVisibility(View.INVISIBLE);
        Button12.setVisibility(View.INVISIBLE);
        Button9.setVisibility(View.INVISIBLE);
        Button11.setVisibility(View.VISIBLE);
        Button14.setVisibility(View.INVISIBLE);
        Button15.setVisibility(View.VISIBLE);
        Button16.setVisibility(View.INVISIBLE);
        Button17.setVisibility(View.INVISIBLE);
        Cursor res = myDb.getAllData();//call all of the data base data of table raw data
        Cursor res2 = myDb.getAllData1();
        if (res != null && res.getCount()>0) {//verifies that there is data in the data base
            if (res2.getCount()==0) {

                Runnable r = new Runnable() {//creates a thread so the long operation wont stop my app
                    @Override
                    public void run() {
                        Cursor res = myDb.getAllData();//call all of the data base data of table raw data
                        DataAccMAf.addAll(myFilter.MAF1(res));//gets all data from filter Moving average filter MAF1
                        for (int i1 = 0; i1 < DataAccMAf.size(); i1++) {
                            myDb.insertData1(DataAccMAf.get(i1));
                        }
                        handlerMAF.sendEmptyMessage(0);
                    }
                };
                Thread MAFThread = new Thread(r);
                MAFThread.start();
            }
            else
                handlerMAF.sendEmptyMessage(0);//in case the data already exists deploy graph
                showMessage("FILTER", "Moving Average Filter");
            return;
        }
        else
            showMessage("Error", "no data in the dataBase");

        return;
    }

    Handler handlerMAF = new Handler(){//creates the handler that will happen when the thread is done
        @Override
        public void handleMessage(Message msg) {
            EventIntentHistogram = 3;
            //graph.removeAllSeries();//cleans any series
            Cursor res10 = myDb.getMAX();
            res10.moveToFirst();
            //res10.moveToLast();
            Log.w(TAG, "MINIMUM VALUE AT :"+res10.getString(0)+" VALUE OF: "+res10.getString(1));
            res10.moveToNext();
            Log.w(TAG, "NEXT VALUE AT :"+res10.getString(0)+" VALUE OF: "+res10.getString(1));
            res10.moveToLast();
            Log.w(TAG, "MAXIMUM VALUE AT :"+res10.getString(0)+" VALUE OF: "+res10.getString(1));
            graph.removeAllSeries();
            shoeGraphRaw();
            seriesMAF = new LineGraphSeries<DataPoint>();//calls function from database and makes it ready to add to the graph
            seriesMAF.setColor(Color.RED);
            double y, x;//initialize variables that will hold the math operations
            //DataAccMAf.addAll(myFilter.MAF1(res)};

            x = -5.0;
            y = 10000;
            StringBuffer data_BLE_DataBase_Raw = new StringBuffer();//if there is data creates a chain of strings
            //Log.w(TAG, "DATOS BASE: :" + DataAccMAf.get(0));
            Cursor res1 = myDb.getAllData1();//gets all data from database

            if (res1.getCount() != 0) {
                //count1 = myDb.getAllData1().getCount();
                //for (int i = 0; i<500; i++) {
                res1.moveToFirst();//goes back to the first data of the database
                while (res1.moveToNext()) {
                    x = res1.getInt(0);
                    //y = res.getDouble(1);
                    //x = x+0.1;
                    y = Double.parseDouble(res1.getString(1));
                    //y = Math.sin(x);
                    seriesMAF.appendData(new DataPoint(x, y), true, res1.getCount());
                }
                graph.addSeries(seriesMAF);//shows the graph
                seriesMAF.setTitle("MAF-ACC");
                graph.getLegendRenderer().setVisible(true);
                //myFilter.Binned(res1);
            } else
                showMessage("Error", "no data in the dataBase");
            return;
        }
    };
    /////////////////
    ///////////////FILTER CREATED FOR  IOT
    public void OnclickIOT(View v){
        Button9.setVisibility(View.INVISIBLE);
        Button10.setVisibility(View.INVISIBLE);
        Button11.setVisibility(View.INVISIBLE);
        Button12.setVisibility(View.VISIBLE);
        Button14.setVisibility(View.INVISIBLE);
        Button15.setVisibility(View.INVISIBLE);
        Button16.setVisibility(View.VISIBLE);
        Button17.setVisibility(View.INVISIBLE);
        Cursor res = myDb.getAllData();//call all of the data base data of table raw data
        Cursor res2 = myDb.getAllData3();
        if (res != null && res.getCount()>0 ) {//verifies that there is data in the data base
            if (res2.getCount()==0) {

                Runnable r = new Runnable() {//creates a thread so the long operation wont stop my app
                    @Override
                    public void run() {
                        Cursor res = myDb.getAllData();//call all of the data base data of table raw data
                        DataAccIOT.addAll(myFilter.IOT(res));//gets all data from filter Moving average filter MAF1
                        for (int i1 = 0; i1 < DataAccIOT.size(); i1++) {
                            myDb.insertData3(DataAccIOT.get(i1));
                        }
                        handlerIOT.sendEmptyMessage(0);
                    }
                };
                Thread IOTThread = new Thread(r);
                IOTThread.start();
            }
            else
                handlerIOT.sendEmptyMessage(0);
                showMessage("FILTER", "IOT");//in case the data already exists deploy graph
            return;
        }
        else
            showMessage("Error", "no data in the dataBase");
        return;
    }

    Handler handlerIOT = new Handler(){//creates the handler that will happen when the thread is done
        @Override
        public void handleMessage(Message msg) {
            EventIntentHistogram = 2;//second case for the intent
            //graph.removeAllSeries();//cleans any series
            graph.removeAllSeries();
            shoeGraphRaw();

            seriesIOT = new LineGraphSeries<DataPoint>();//calls function from database and makes it ready to add to the graph
            seriesIOT.setColor(Color.GRAY);

            double y, x;//initialize variables that will hold the math operations
            //DataAccMAf.addAll(myFilter.MAF1(res)};

            x = -5.0;
            y = 10000;
            StringBuffer data_BLE_DataBase_Raw = new StringBuffer();//if there is data creates a chain of strings
            //Log.w(TAG, "DATOS BASE: :" + DataAccIOT.get(0));
            Cursor res1 = myDb.getAllData3();//gets all data from database
            if (res1.getCount() != 0) {
                //count1 = myDb.getAllData1().getCount();
                //for (int i = 0; i<500; i++) {
                res1.moveToFirst();//goes back to the first data of the database
                while (res1.moveToNext()) {
                    x = res1.getInt(0);
                    //y = res.getDouble(1);
                    //x = x+0.1;
                    y = Double.parseDouble(res1.getString(1));
                    //y = Math.sin(x);
                    seriesIOT.appendData(new DataPoint(x, y), true, res1.getCount());
                }
                graph.addSeries(seriesIOT);//shows the graph
                seriesIOT.setTitle("IOT-ACC");
                graph.getLegendRenderer().setVisible(true);
            } else
                showMessage("Error", "no data in the dataBase");
            return;
        }
    };
    ///////////////

    public void OnclickBMF(View v){
        Button9.setVisibility(View.INVISIBLE);
        Button10.setVisibility(View.VISIBLE);
        Button11.setVisibility(View.INVISIBLE);
        Button12.setVisibility(View.INVISIBLE);
        Button14.setVisibility(View.INVISIBLE);
        Button15.setVisibility(View.INVISIBLE);
        Button16.setVisibility(View.INVISIBLE);
        Button17.setVisibility(View.VISIBLE);
        Cursor res = myDb.getAllData();//call all of the data base data of table raw data
        Cursor res2 = myDb.getAllData2();
        if (res != null && res.getCount()>0 ) {//verifies that there is data in the data base
            if (res2.getCount()==0) {

                Runnable r = new Runnable() {//creates a thread so the long operation wont stop my app
                    @Override
                    public void run() {
                        Cursor res = myDb.getAllData();//call all of the data base data of table raw data
                        DataAccBMF.addAll(myFilter.BlackManFilter(res));//gets all data from filter Moving average filter MAF1
                        for (int i1 = 0; i1 < DataAccBMF.size(); i1++) {
                        myDb.insertData2(DataAccBMF.get(i1));
                                }
                        handlerBMF.sendEmptyMessage(0);
                    }
                };
                Thread BMAThread = new Thread(r);
                BMAThread.start();
            }
            else
                handlerBMF.sendEmptyMessage(0);
                showMessage("FILTER", "BMF");//in case the data already exists deploy graph
                return;
        }
        else
            showMessage("Error", "no data in the dataBase");
            return;
    }

    Handler handlerBMF = new Handler(){//creates the handler that will happen when the thread is done
        @Override
        public void handleMessage(Message msg) {
            //graph.removeAllSeries();//cleans any series
            EventIntentHistogram = 1;//first case
            graph.removeAllSeries();
            shoeGraphRaw();
            seriesBMF = new LineGraphSeries<DataPoint>();//calls function from database and makes it ready to add to the graph
            seriesBMF.setColor(Color.GREEN);
            double y, x;//initialize variables that will hold the math operations
            //DataAccMAf.addAll(myFilter.MAF1(res)};

            x = -5.0;
            y = 10000;
            StringBuffer data_BLE_DataBase_Raw = new StringBuffer();//if there is data creates a chain of strings
            //Log.w(TAG, "DATOS BASE: :" + DataAccBMF.get(0));
            Cursor res1 = myDb.getAllData2();//gets all data from database
            if (res1.getCount() != 0) {
                //count1 = myDb.getAllData1().getCount();
                //for (int i = 0; i<500; i++) {
                res1.moveToFirst();//goes back to the first data of the database
                while (res1.moveToNext()) {
                    x = res1.getInt(0)+46;
                    //y = res.getDouble(1);
                    //x = x+0.1;
                    y = Double.parseDouble(res1.getString(1));
                    //y = Math.sin(x);
                    seriesBMF.appendData(new DataPoint(x, y), true, res1.getCount());
                }
                graph.addSeries(seriesBMF);//shows the graph
                seriesBMF.setTitle("BMF-ACC");
                graph.getLegendRenderer().setVisible(true);
            } else
                showMessage("Error", "no data in the dataBase");
                return;
        }
    };

    public void showMessage(String title, String Message){//method to show messages
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(Message);
        builder.show();
    }
////////////////////////////////////////////////////////////////////////////////////////
    public void OnclickHis(View v){
        Cursor res = myDb.getAllData();//call all of the data base data of table raw data
        ListIntentHistogram.clear();

        final Intent ReachHistogram = new Intent(this, HistogramActivity.class);
        final int result = 1;
        ReachHistogram.putStringArrayListExtra("callingActivity", (ArrayList<String>) ListIntentHistogram);
        startActivity(ReachHistogram);

    }
    /////////////////////////////////////////////////////////////////
    public void OnclickHisNOW(View v){
        showMessage("Histogram", "RAW DATA");
        Cursor res = myDb.getAllData();//call all of the data base data of table raw data

        Cursor res2 = myDb.getAllData_HISTOGRAM();
        if (res != null&&res.getCount()>0) {//verifies that there is data in the data base
            if (res2.getCount()==0) {

                Runnable r = new Runnable() {//creates a thread so the long operation wont stop my app
                    @Override
                    public void run() {
                        Log.w(TAG, "ENTRO EN EL HISTOGRAMA");
                        Cursor res = myDb.getAllData();//call all of the data base data of table raw data
                        DataAccBINNED.clear();
                        DataAccHISTOGRAM.clear();
                        DataAccBINNED.addAll(myFilter.Binned(res));//gets all data from filter Moving average filter MAF1
                        DataAccHISTOGRAM.addAll(myFilter.Histogram(DataAccBINNED));
                        for (int i = 0; i < DataAccHISTOGRAM.size(); i++) {
                            boolean insertHistogram = myDb.insertData_HISTOGRAM(DataAccHISTOGRAM.get(i));

                        }
                        //handlerHISTO.sendEmptyMessage(0);
                        reachHistogram();

                    }
                };
                Thread HISThread = new Thread(r);
                HISThread.start();
                //reachHistogram();
            }
            else
                reachHistogram();
            //}
            //else
            //handlerHISTO.sendEmptyMessage(0);
            //showMessage("FILTER", "HISTOGRAM");//in case the data already exists deploy graph
            //return;
        }
        else
            showMessage("Error", "no data in the dataBase");
        return;
    }
    //////////////////////////////////////////////////////////////////ON CLICK HISTOGRAM BUTTON BEGINS


//////////////////////////////////////////////////////////////ON CLICK HISTOGRAM BUTTON FINISH
    ////////////////////////////////////////////////////////FUNCTION DONE AFTER ONCLICK HISTOGRAM BUTTON


    public void reachHistogram(){//function use to reach the histogram
        Cursor resHistogram = myDb.getAllData_HISTOGRAM();//call all of the data base data of table histogram data
        //Cursor resHistogram = getHistogramDecision();//call all of the data base data of table histogram data base on the event intent
        ListIntentHistogram.clear();//clears all the data
        for (resHistogram.moveToFirst(); !resHistogram.isAfterLast(); resHistogram.moveToNext()) {//put the cursor into a list
            // The Cursor is now set to the right position
            ListIntentHistogram.add(resHistogram.getString(1));
        }
        final Intent ReachHistogram = new Intent(this, HistogramActivity.class);//declares the list and where is reaching
        final int result = 1;
        ReachHistogram.putStringArrayListExtra("callingActivity", (ArrayList<String>) ListIntentHistogram);
        startActivity(ReachHistogram);

    }
    /////////////////////////////////////////////////////////////////////////////////////
    public void OnclickHisNOW_BLACKMAN(View v){
//        showMessage("Histogram", "BMF DATA");
        Cursor res = myDb.getAllData2();//call all of the data base data of table raw data

        Cursor res2 = myDb.getAllData_HISTOGRAM_BLACKMAN();
        if (res != null&&res.getCount()>0) {//verifies that there is data in the data base
            if (res2.getCount()==0) {

                Runnable r = new Runnable() {//creates a thread so the long operation wont stop my app
                    @Override
                    public void run() {
                        Log.w(TAG, "ENTRO EN EL HISTOGRAMA");
                        Cursor res = myDb.getAllData2();//call all of the data base data of table raw data
                        DataAccBINNED.clear();
                        DataAccHISTOGRAM.clear();
                        DataAccBINNED.addAll(myFilter.Binned(res));//gets all data from filter Moving average filter MAF1
                        DataAccHISTOGRAM.addAll(myFilter.Histogram(DataAccBINNED));
                        for (int i = 0; i < DataAccHISTOGRAM.size(); i++) {
                            boolean insertHistogram = myDb.insertData_HISTOGRAM_BLACKMAN(DataAccHISTOGRAM.get(i));

                        }
                        for (int i1 = 0; i1 < myFilter.MovHistogramNegative.size(); i1++){
                            boolean insertHistogram1 = myDb.insertData_HISTOGRAM_BLACKMAN1(myFilter.MovHistogramNegative.get(i1));
                        }
                        //handlerHISTO.sendEmptyMessage(0);
                        reachHistogram_BLACKMAN();
                    }
                };
                Thread HISTBLACKhread = new Thread(r);
                HISTBLACKhread.start();
                //reachHistogram_BLACKMAN();
            }
            else
                reachHistogram_BLACKMAN();
            //}
            //else
            //handlerHISTO.sendEmptyMessage(0);
            //showMessage("FILTER", "HISTOGRAM");//in case the data already exists deploy graph
            //return;
        }

        else
            showMessage("Error", "no data in the dataBase");
            return;
    }
    //////////////////////////////////////////////////////////////////ON CLICK HISTOGRAM BUTTON BEGINS


//////////////////////////////////////////////////////////////ON CLICK HISTOGRAM BUTTON FINISH
    ////////////////////////////////////////////////////////FUNCTION DONE AFTER ONCLICK HISTOGRAM BUTTON


    public void reachHistogram_BLACKMAN(){//function use to reach the histogram
        Cursor resHistogram = myDb.getAllData_HISTOGRAM_BLACKMAN();//call all of the data base data of table histogram data
        Cursor resHistogram1 = myDb.getAllData_HISTOGRAM_BLACKMAN1();//call all of the data base data of table histogram data
        //Cursor resHistogram = getHistogramDecision();//call all of the data base data of table histogram data base on the event intent
        ListIntentHistogram.clear();//clears all the data
        for (resHistogram.moveToFirst(); !resHistogram.isAfterLast(); resHistogram.moveToNext()) {//put the cursor into a list
            // The Cursor is now set to the right position
            ListIntentHistogram.add(resHistogram.getString(1));
        }
        ListIntentHistogram1.clear();
        for (resHistogram1.moveToFirst(); !resHistogram1.isAfterLast(); resHistogram1.moveToNext()) {//put the cursor into a list
            // The Cursor is now set to the right position
            ListIntentHistogram1.add(resHistogram1.getString(1));
        }
        final Intent ReachHistogram = new Intent(this, HistogramActivity.class);//declares the list and where is reaching
        final int result = 1;
        ReachHistogram.putStringArrayListExtra("callingActivity", (ArrayList<String>) ListIntentHistogram);
        //final Intent ReachHistogram1 = new Intent(this, HistogramActivity.class);//declares the list and where is reaching
        //final int result1 = 1;
        //ReachHistogram1.putStringArrayListExtra("callingActivity1", (ArrayList<String>) ListIntentHistogram1);
        startActivity(ReachHistogram);

    }

    public void OnclickHisNOW_IOT(View v){
        showMessage("Histogram", "IOT DATA");
        Cursor res = myDb.getAllData3();//call all of the data base data of table raw data

        Cursor res2 = myDb.getAllData_HISTOGRAM_IOT();
        if (res != null&&res.getCount()>0) {//verifies that there is data in the data base
            if (res2.getCount()==0) {

                Runnable r = new Runnable() {//creates a thread so the long operation wont stop my app
                    @Override
                    public void run() {
                        Log.w(TAG, "ENTRO EN EL HISTOGRAMA");
                        Cursor res = myDb.getAllData3();//call all of the data base data of table raw data
                        DataAccBINNED.clear();
                        DataAccHISTOGRAM.clear();
                        DataAccBINNED.addAll(myFilter.Binned(res));//gets all data from filter Moving average filter MAF1
                        DataAccHISTOGRAM.addAll(myFilter.Histogram(DataAccBINNED));
                        for (int i = 0; i < DataAccHISTOGRAM.size(); i++) {
                            boolean insertHistogram = myDb.insertData_HISTOGRAM_IOT(DataAccHISTOGRAM.get(i));

                        }
                        //handlerHISTO.sendEmptyMessage(0);
                        reachHistogram_IOT();
                    }
                };
                Thread HISTIOThread = new Thread(r);
                HISTIOThread.start();
                //reachHistogram_IOT();
            }
            else
                reachHistogram_IOT();
            //}
            //else
            //handlerHISTO.sendEmptyMessage(0);
            //showMessage("FILTER", "HISTOGRAM");//in case the data already exists deploy graph
            //return;
        }
        else
            showMessage("Error", "no data in the dataBase");
        return;
    }
    //////////////////////////////////////////////////////////////////ON CLICK HISTOGRAM BUTTON BEGINS


//////////////////////////////////////////////////////////////ON CLICK HISTOGRAM BUTTON FINISH
    ////////////////////////////////////////////////////////FUNCTION DONE AFTER ONCLICK HISTOGRAM BUTTON


    public void reachHistogram_IOT(){//function use to reach the histogram
        Cursor resHistogram = myDb.getAllData_HISTOGRAM_IOT();//call all of the data base data of table histogram data
        //Cursor resHistogram = getHistogramDecision();//call all of the data base data of table histogram data base on the event intent
        ListIntentHistogram.clear();//clears all the data
        for (resHistogram.moveToFirst(); !resHistogram.isAfterLast(); resHistogram.moveToNext()) {//put the cursor into a list
            // The Cursor is now set to the right position
            ListIntentHistogram.add(resHistogram.getString(1));
        }
        final Intent ReachHistogram = new Intent(this, HistogramActivity.class);//declares the list and where is reaching
        final int result = 1;
        ReachHistogram.putStringArrayListExtra("callingActivity", (ArrayList<String>) ListIntentHistogram);
        startActivity(ReachHistogram);

    }

    public void OnclickHisNOW_MAF(View v){
        showMessage("Histogram", "MAF DATA");
        Cursor res = myDb.getAllData1();//call all of the data base data of table raw data

        Cursor res2 = myDb.getAllData_HISTOGRAM_MOVING();
        if (res != null&&res.getCount()>0) {//verifies that there is data in the data base
            if (res2.getCount()==0) {

                Runnable r = new Runnable() {//creates a thread so the long operation wont stop my app
                    @Override
                    public void run() {
                        Log.w(TAG, "ENTRO EN EL HISTOGRAMA");
                        Cursor res = myDb.getAllData1();//call all of the data base data of table raw data
                        DataAccBINNED.clear();
                        DataAccHISTOGRAM.clear();
                        DataAccBINNED.addAll(myFilter.Binned(res));//gets all data from filter Moving average filter MAF1
                        DataAccHISTOGRAM.addAll(myFilter.Histogram(DataAccBINNED));
                        for (int i = 0; i < DataAccHISTOGRAM.size(); i++) {
                            boolean insertHistogram = myDb.insertData_HISTOGRAM_MOVING(DataAccHISTOGRAM.get(i));

                        }
                        //handlerHISTO.sendEmptyMessage(0);
                        reachHistogram_MAF();
                    }
                };
                Thread HISMAFThread = new Thread(r);
                HISMAFThread.start();
                //reachHistogram_MAF();
            }
            else
                reachHistogram_MAF();
        }
        else
            showMessage("Error", "no data in the dataBase");
        return;
    }
    //////////////////////////////////////////////////////////////////ON CLICK HISTOGRAM BUTTON BEGINS


//////////////////////////////////////////////////////////////ON CLICK HISTOGRAM BUTTON FINISH
    ////////////////////////////////////////////////////////FUNCTION DONE AFTER ONCLICK HISTOGRAM BUTTON


    public void reachHistogram_MAF(){//function use to reach the histogram
        Cursor resHistogram = myDb.getAllData_HISTOGRAM_MOVING();//call all of the data base data of table histogram data
        //Cursor resHistogram = getHistogramDecision();//call all of the data base data of table histogram data base on the event intent
        ListIntentHistogram.clear();//clears all the data
        for (resHistogram.moveToFirst(); !resHistogram.isAfterLast(); resHistogram.moveToNext()) {//put the cursor into a list
            // The Cursor is now set to the right position
            ListIntentHistogram.add(resHistogram.getString(1));
        }
        final Intent ReachHistogram = new Intent(this, HistogramActivity.class);//declares the list and where is reaching
        final Intent ReachHistogram1 = new Intent(this, HistogramActivity.class);//declares the list and where is reaching
        final int result = 1;
        ReachHistogram.putStringArrayListExtra("callingActivity", (ArrayList<String>) ListIntentHistogram);
        ReachHistogram1.putStringArrayListExtra("callingActivity1", (ArrayList<String>) ListIntentHistogram);
        startActivity(ReachHistogram);

    }
    //////////////////////////////////////////////////////////////////////////////////

    public void shoeGraphRaw(){//method used to get the graph from the raw data

        graph.removeAllSeries();
        series = new LineGraphSeries<DataPoint>();//calls function from database and makes it ready to add to the graph
        double y, x;

        x = 0.0;
        y = 10000;


        //x = x+0.1;
        Cursor res = myDb.getAllData();

        //x = x+0.1;
        //try {
        if (myDb.getAllData().getCount() != 0 || myDb.getAllData().getCount() != count1) {

            count1 = myDb.getAllData().getCount();
            while (res.moveToNext()) {
                x = res.getInt(0);

                y = Double.parseDouble(res.getString(1));
                //y = Math.sin(x);
                series.appendData(new DataPoint(x, y), true, res.getCount());
            }
            graph.addSeries(series);//shows the graph
            series.setTitle("RAW-ACC");
            graph.getLegendRenderer().setVisible(true);
        } else return;
    }
    ///////////////////////////////////starts the mean calculation
    public void OnclickWindowRAWMean(View v){//mean calculation for the raw data
        Cursor res = myDb.getAllData();
        if (res != null && res.getCount() > 0 ) {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    Cursor res = myDb.getAllData();
                    myFilter.MeanWindow(res);
                    handlerWindow.sendEmptyMessage(0);
                }
            };
            Thread WindowThread = new Thread(r);
            WindowThread.start();
        }
    }
    public void OnclickWindowMAFMean(View v){//mean calculation for the MAF FILTER
        Cursor res = myDb.getAllData1();
        if (res != null && res.getCount() > 0 ) {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    Cursor res = myDb.getAllData1();
                    myFilter.MeanWindow(res);
                    handlerWindow.sendEmptyMessage(0);
                }
            };
            Thread WindowThread = new Thread(r);
            WindowThread.start();
        }
        else
            Toast.makeText(this, "NO DATA", Toast.LENGTH_SHORT  ).show();
            return;
    }
    public void OnclickWindowIOTMean(View v){//mean calculation for the IOT FILTER
        Cursor res = myDb.getAllData3();
        if (res != null && res.getCount() > 0 ) {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    Cursor res = myDb.getAllData3();
                    myFilter.MeanWindow(res);
                    handlerWindow.sendEmptyMessage(0);
                }
            };
            Thread WindowThread = new Thread(r);
            WindowThread.start();
        }
        else
            Toast.makeText(this, "NO DATA", Toast.LENGTH_SHORT  ).show();
        return;
    }
    public void OnclickWindowBMFMean(View v){//mean calculation for the BMF FILTER
        Cursor res = myDb.getAllData2();
        if (res != null && res.getCount() > 0 ) {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    Cursor res = myDb.getAllData2();
                    myFilter.MeanWindow(res);
                    handlerWindow.sendEmptyMessage(0);
                }
            };
            Thread WindowThread = new Thread(r);
            WindowThread.start();
        }
        else
            Toast.makeText(this, "NO DATA", Toast.LENGTH_SHORT  ).show();
        return;
    }
    Handler handlerWindow = new Handler(){//creates the handler that will happen when the thread is done
        @Override
        public void handleMessage(Message msg) {
            Log.w(TAG, "entrada autorizada");
            //graph.removeAllSeries();//cleans any series
            graph.removeAllSeries();

            seriesWindow = new LineGraphSeries<DataPoint>();//starts the series
            seriesWindow.setColor(Color.BLACK);

            seriesWindowSD = new LineGraphSeries<DataPoint>();//starts the series
            seriesWindow.setColor(Color.MAGENTA);
            double y, x, y1, x1;//initialize variables that will hold the math operations
            //DataAccMAf.addAll(myFilter.MAF1(res)};

            x = -5.0;
            x1 = 0.0;
            y = 10000;
            y1 = 1.0;
            //Log.w(TAG, "DATOS BASE: :" + DataAccIOT.get(0));
            if (myFilter.MeanWindowValue.size() != 0) {
                //count1 = myDb.getAllData1().getCount();
                for (int i = 0; i<myFilter.MeanWindowValue.size(); i++) {
                    x = i;
                    y = myFilter.MeanWindowValue.get(i);
                    Log.w(TAG, "valor esperado:"+ y);
                    //y = res.getDouble(1);
                    //x = x+0.1;
                    //y = Math.sin(x);
                    seriesWindow.appendData(new DataPoint(x, y), true, myFilter.MeanWindowValue.size());
                }
                for (int i = 0; i<myFilter.SDWindowValue.size(); i++) {
                    x = i;
                    y = myFilter.SDWindowValue.get(i);
                    Log.w(TAG, "valor esperado:"+ y);
                    //y = res.getDouble(1);
                    //x = x+0.1;
                    //y = Math.sin(x);
                    seriesWindowSD.appendData(new DataPoint(x, y), true, myFilter.SDWindowValue.size());
                }
                graph.addSeries(seriesWindow);//shows the graph
                seriesWindow.setTitle("Mean");
                graph.getLegendRenderer().setVisible(true);
                graph.addSeries(seriesWindowSD);//shows the graph
                seriesWindowSD.setTitle("SD");
                graph.getLegendRenderer().setVisible(true);
            } else
                showMessage("Error", "no data in the dataBase");
            return;
        }
    };
    ////////////////////finishes
    public void OnClickCSV(View v){//Button to create a simple csv file
        try {
            String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
            String fileName = "AnalysisData_raw_"+CSV_count+".csv";
            String fileName_MAF = "AnalysisData_MAF_"+CSV_count+".csv";
            String fileName_IOT = "AnalysisData_IOT_"+CSV_count+".csv";
            String fileName_BMF = "AnalysisData_BMF_"+CSV_count+".csv";
            CSV_count++;//
            String filePath = baseDir + File.separator + fileName;
            String filePath_MAF = baseDir + File.separator + fileName_MAF;
            String filePath_IOT = baseDir + File.separator + fileName_IOT;
            String filePath_BMF = baseDir + File.separator + fileName_BMF;
            String extStore = System.getenv("EXTERNAL_STORAGE");
            Cursor res = myDb.getAllData4();//gets the data from 3 axis
            Cursor res1 = myDb.getAllData();//gets the data from the magnitud acc
            Cursor res_MAF = myDb.getAllData1();
            Cursor res_IOT = myDb.getAllData3();
            Cursor res_BMF = myDb.getAllData2();
            File f = new File(extStore );
            CSVWriter writer;
            CSVWriter writer_MAF;
            CSVWriter writer_IOT;
            CSVWriter writer_BMF;
// File exist

                writer = new CSVWriter(new FileWriter(filePath));
                writer_MAF = new CSVWriter(new FileWriter(filePath_MAF));
                writer_IOT = new CSVWriter(new FileWriter(filePath_IOT));
                writer_BMF = new CSVWriter(new FileWriter(filePath_BMF));
                Toast.makeText(DeviceControlActivity.this, "already is", Toast.LENGTH_LONG).show();
            //}
            String[] data = {"AXX", "AYY","AZZ","ACC_TOTAL"};
            String[] data_MAF = {"ACC_MAF"};
            String[] data_IOT = {"ACC_IOT"};
            String[] data_BMF = {"ACC_BMF"};
            writer.writeNext(data);
            writer_MAF.writeNext(data_MAF);
            writer_IOT.writeNext(data_IOT);
            writer_BMF.writeNext(data_BMF);
            res.moveToFirst();//goes back to the first data of the database
            res1.moveToFirst();
            while (res.moveToNext()) {
                res1.moveToNext();
                CSV[0] = res.getString(1);
                CSV[1] = res.getString(2);
                CSV[2] = res.getString(3);
                CSV[3] = res1.getString(1);
                writer.writeNext(CSV);
            }

            if (res_MAF != null && res_MAF.getCount() > 0){
                while (res_MAF.moveToNext()) {
                    CSV_maf[0] = res_MAF.getString(1);
                    writer_MAF.writeNext(CSV_maf);
                }
            }
            if (res_IOT != null && res_IOT.getCount() > 0){
                while (res_IOT.moveToNext()) {
                    CSV_iot[0] = res_IOT.getString(1);
                    writer_IOT.writeNext(CSV_iot);
                }
            }
            if (res_BMF != null && res_BMF.getCount() > 0){
                while (res_BMF.moveToNext()) {
                    CSV_bmf[0] = res_BMF.getString(1);
                    writer_BMF.writeNext(CSV_bmf);
                }
            }

            writer.close();
            writer_MAF.close();
            writer_IOT.close();
            writer_BMF.close();
        }
        catch (IOException e){
            Toast.makeText(DeviceControlActivity.this, e.toString(), Toast.LENGTH_LONG).show();
        }

    }

    public void OnclickReset(View v){
        myDb.deleteAll();
        Toast.makeText(DeviceControlActivity.this, "All Data Errased", Toast.LENGTH_SHORT).show();
        ///myDb = new DataBase(this);
        graph.removeAllSeries();
        plain = new ArrayList<String>();// creates a list where all data from string column gets inserted from table rawdata
        DataAccMAf.clear();
        DataAccIOT.clear();
        DataAccBMF.clear();
        DataAccBINNED.clear();
        DataAccHISTOGRAM.clear();
        ListIntentHistogram.clear();
        ListIntentHistogram1.clear();
        myDb.savedList_Z.clear();
        myDb.savedList.clear();
        myDb.savedList_X.clear();
        myDb.savedList_Y.clear();
        myDb.savedListTime.clear();
        myDb.setZero();
        myFilter.ListaData.clear();
        myFilter.ListaDataIOT.clear();
        myFilter.ListMeanVar.clear();
        myFilter.MovAv.clear();
        myFilter.MovIOT.clear();
        myFilter.MovHistogram.clear();
        myFilter.MovHistogramNegative.clear();
        myFilter.MovAv.clear();
        myFilter.BlackMan1.clear();
        myFilter.MeanWindowValue.clear();
        myFilter.MeanWindow.clear();
        myFilter.SDWindowValue.clear();
        ListRealRead.clear();
        count1 = 0;
        n = 1;

    }

}
