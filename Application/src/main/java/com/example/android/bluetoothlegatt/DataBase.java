package com.example.android.bluetoothlegatt;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mlram on 3/01/2018.
 */

public class DataBase extends SQLiteOpenHelper {
    private Filters myFilter = new Filters();
    public static final String DATABASE_NAME = "Accelerometer.db";
    public static final String TABLE_NAME = "RawAccelerometer_Table_RAW";//table that will hold the data from raw value
    public static final String TABLE_NAME_H = "RawAccelerometer_Table_H";//table that will hold the data from Histogram
    public static final String TABLE_NAME_HM = "RawAccelerometer_Table_HM";//table that will hold the data from Histogram Moving Average
    public static final String TABLE_NAME_HI = "RawAccelerometer_Table_HI";//table that will hold the data from Histogram IOT
    public static final String TABLE_NAME_HB = "RawAccelerometer_Table_HB";//table that will hold the data from Histogram BlackMan
    public static final String TABLE_NAME_HB1 = "RawAccelerometer_Table_HB";//table that will hold the data from Histogram BlackMan negative
    public static final String TABLE_NAME_Z = "RawAccelerometer_Table_Z";//table that will hold the data from z axis
    public static final String TABLE_NAME_ALL = "RawAccelerometer_Table_ALL";//table that will hold the data from x axis
    public static final String TABLE_NAME1 = "MAFAccelerometer_Table";
    public static final String TABLE_NAME2 = "MAFAccelerometer1_Table";
    public static final String TABLE_NAME3 = "LOWER_FILTER_Table";
    public static final String COL0 = "TIME";
    public static final String COL1 = "ID";
    public static final String COL2 = "RAW_DATA";
    public static final String COL_X = "X_DATA";
    public static final String COL_Y = "Y_DATA";
    public static final String COL_H = "H_DATA";
    public static final String COL_HM = "HM_DATA";
    public static final String COL_HI = "HI_DATA";
    public static final String COL_HB = "HB_DATA";
    public static final String COL_HB1 = "HB_DATA1";
    public static final String COL_Z = "Z_DATA";
    public static final String COL3 = "MAF_DATA";
    public static final String COL4 = "BMF_DATA";
    public static final String COL5 = "IOT1_DATA";
    GraphView graph;
    LineGraphSeries<DataPoint> series;
    List<String> savedList = new ArrayList<String>();//array list to save the raw data before inserting  it into the database
    List<Long> savedListTime = new ArrayList<Long>();//array list to save the raw data before inserting  it into the database
    List<String> savedList_X = new ArrayList<String>();//array list to save the raw_X data before inserting  it into the database
    List<String> savedList_Y = new ArrayList<String>();//array list to save the raw_Y data before inserting  it into the database
    List<String> savedList_Z = new ArrayList<String>();//array list to save the raw_Z data before inserting  it into the database
    public static volatile int CounterBleData = 0;//counts the data inside my saveList function
    public static volatile int CounterBleData1 = 0;//counts the data inside my saveList function
    private final static String TAG = DataBase.class.getSimpleName();
    public volatile static long tm = 0, timeActual = 0;


    public DataBase(Context context) {
        super(context, DATABASE_NAME, null, 17);
        SQLiteDatabase db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {//creates the databases
        db.execSQL("create table if not exists "+TABLE_NAME+"(ID INTEGER PRIMARY KEY AUTOINCREMENT, RAW_DATA TEXT, TIME REAL)");
        db.execSQL("create table if not exists "+TABLE_NAME_ALL+"(ID INTEGER PRIMARY KEY AUTOINCREMENT, X_DATA TEXT,Y_DATA TEXT,Z_DATA TEXT )");
        db.execSQL("create table if not exists "+TABLE_NAME_H+"(ID INTEGER PRIMARY KEY AUTOINCREMENT, H_DATA REAL)");
        db.execSQL("create table if not exists "+TABLE_NAME_HM+"(ID INTEGER PRIMARY KEY AUTOINCREMENT, HM_DATA REAL)");
        db.execSQL("create table if not exists "+TABLE_NAME_HI+"(ID INTEGER PRIMARY KEY AUTOINCREMENT, HI_DATA REAL)");
        db.execSQL("create table if not exists "+TABLE_NAME_HB+"(ID INTEGER PRIMARY KEY AUTOINCREMENT, HB_DATA REAL)");
        db.execSQL("create table if not exists "+TABLE_NAME_HB1+"(ID INTEGER PRIMARY KEY AUTOINCREMENT, HB_DATA1 REAL)");
        db.execSQL("create table if not exists "+TABLE_NAME_Z+"(ID INTEGER PRIMARY KEY AUTOINCREMENT, Z_DATA TEXT)");
        db.execSQL("create table if not exists "+TABLE_NAME1+"(ID INTEGER PRIMARY KEY AUTOINCREMENT, MAF_DATA REAL)");
        db.execSQL("create table if not exists "+TABLE_NAME2+"( ID INTEGER PRIMARY KEY AUTOINCREMENT, BMF_DATA REAL )");
        db.execSQL("create table if not exists "+TABLE_NAME3+"(ID INTEGER PRIMARY KEY AUTOINCREMENT, IOT1_DATA REAL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_NAME1);
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_NAME2);
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_NAME3);
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_NAME_ALL);
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_NAME_H);
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_NAME_HM);
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_NAME_HI);
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_NAME_HB);
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_NAME_HB1);
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_NAME_Z);
        onCreate(db);
    }

    public boolean insertData( String raw_data, long time){//inserts the raw data
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL2, raw_data);
        contentValues.put(COL0, time);
        long result = db.insert(TABLE_NAME, null, contentValues);
        if (result == -1){
            return false;
        }
        else
            return true;
    }


    public boolean insertData1( Double MAF_DATA){//inserts the data passed trhough the MAF
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL3, MAF_DATA);
        long result = db.insert(TABLE_NAME1, null, contentValues);
        if (result == -1){
            return false;
        }
        else
            return true;
    }

    public boolean insertData2( Double BMF_DATA){//inserts the data passed trhough the BMF
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL4, BMF_DATA);
        long result = db.insert(TABLE_NAME2, null, contentValues);
        if (result == -1){
            return false;
        }
        else
            return true;
    }

    public boolean insertData3( Double IOT1_DATA){//inserts the data passed trhough the IOT
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL5, IOT1_DATA);
        long result = db.insert(TABLE_NAME3, null, contentValues);
        if (result == -1){
            return false;
        }
        else
            return true;
    }

    public boolean insertData4( String X_DATA, String Y_DATA, String Z_DATA ){//inserts the from ALL AXIS
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_X, X_DATA);
        contentValues.put(COL_Y, Y_DATA);
        contentValues.put(COL_Z, Z_DATA);
        long result = db.insert(TABLE_NAME_ALL, null, contentValues);
        if (result == -1){
            return false;
        }
        else
            return true;
    }

    public boolean insertData_HISTOGRAM( Integer H_DATA){//inserts the from histogram
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_H, H_DATA);
        long result = db.insert(TABLE_NAME_H, null, contentValues);
        if (result == -1){
            return false;
        }
        else
            return true;
    }

    public boolean insertData_HISTOGRAM_MOVING( Integer HM_DATA){//inserts the from histogram MAF
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_HM, HM_DATA);
        long result = db.insert(TABLE_NAME_HM, null, contentValues);
        if (result == -1){
            return false;
        }
        else
            return true;
    }

    public boolean insertData_HISTOGRAM_IOT( Integer HI_DATA){//inserts the from histogram IOT
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_HI, HI_DATA);
        long result = db.insert(TABLE_NAME_HI, null, contentValues);
        if (result == -1){
            return false;
        }
        else
            return true;
    }

    public boolean insertData_HISTOGRAM_BLACKMAN( Integer HB_DATA){//inserts the from histogram BMF
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_HB, HB_DATA);
        long result = db.insert(TABLE_NAME_HB, null, contentValues);
        if (result == -1){
            return false;
        }
        else
            return true;
    }
    public boolean insertData_HISTOGRAM_BLACKMAN1( Integer HB_DATA1){//inserts the from histogram BMF
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_HB1, HB_DATA1);
        long result = db.insert(TABLE_NAME_HB1, null, contentValues);
        if (result == -1){
            return false;
        }
        else
            return true;
    }

    public boolean insertData6( String Z_DATA){//inserts the from ACC_Z AXIS
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_Z, Z_DATA);
        long result = db.insert(TABLE_NAME3, null, contentValues);
        if (result == -1){
            return false;
        }
        else
            return true;
    }


    public Cursor getAllData(){//function to call data from my table
        SQLiteDatabase db = this.getWritableDatabase();//make instance to the database
        Cursor res = db.rawQuery("select * from "+ TABLE_NAME, null);//get all the data from the table and puts it into a Cursor
        return res;//returns my values
    }


    public Cursor getAllData1(){//function to call data from my table
        SQLiteDatabase db = this.getWritableDatabase();//make instance to the database
        Cursor res = db.rawQuery("select * from "+ TABLE_NAME1, null);//get all the data from the table and puts it into a Cursor
        return res;//returns my values
    }

    public Cursor getAllData2(){//function to call data from my table
        SQLiteDatabase db = this.getWritableDatabase();//make instance to the database
        Cursor res = db.rawQuery("select * from "+ TABLE_NAME2, null);//get all the data from the table and puts it into a Cursor
        return res;//returns my values
    }

    public Cursor getAllData3(){//function to call data from my table
        SQLiteDatabase db = this.getWritableDatabase();//make instance to the database
        Cursor res = db.rawQuery("select * from "+ TABLE_NAME3, null);//get all the data from the table and puts it into a Cursor
        return res;//returns my values
    }

    public Cursor getAllData4(){//function to get data from my table_x
        SQLiteDatabase db = this.getWritableDatabase();//make instance to the database
        Cursor res = db.rawQuery("select * from "+ TABLE_NAME_ALL, null);//get all the data from the table and puts it into a Cursor
        return res;//returns my values
    }

    public Cursor getAllData_HISTOGRAM(){//function to get data from my HISTOGRAM
        SQLiteDatabase db = this.getWritableDatabase();//make instance to the database
        Cursor res = db.rawQuery("select * from "+ TABLE_NAME_H, null);//get all the data from the table and puts it into a Cursor
        return res;//returns my values
    }

    public Cursor getAllData_HISTOGRAM_MOVING(){//function to get data from my HISTOGRAM_MOVING
        SQLiteDatabase db = this.getWritableDatabase();//make instance to the database
        Cursor res = db.rawQuery("select * from "+ TABLE_NAME_HM, null);//get all the data from the table and puts it into a Cursor
        return res;//returns my values
    }

    public Cursor getAllData_HISTOGRAM_IOT(){//function to get data from my HISTOGRAM_IOT
        SQLiteDatabase db = this.getWritableDatabase();//make instance to the database
        Cursor res = db.rawQuery("select * from "+ TABLE_NAME_HI, null);//get all the data from the table and puts it into a Cursor
        return res;//returns my values
    }

    public Cursor getAllData_HISTOGRAM_BLACKMAN(){//function to get data from my HISTOGRAM_BMF
        SQLiteDatabase db = this.getWritableDatabase();//make instance to the database
        Cursor res = db.rawQuery("select * from "+ TABLE_NAME_HB, null);//get all the data from the table and puts it into a Cursor
        return res;//returns my values
    }
    public Cursor getAllData_HISTOGRAM_BLACKMAN1(){//function to get data from my HISTOGRAM_BMF
        SQLiteDatabase db = this.getWritableDatabase();//make instance to the database
        Cursor res = db.rawQuery("select * from "+ TABLE_NAME_HB1, null);//get all the data from the table and puts it into a Cursor
        return res;//returns my values
    }


    public Cursor getAllData6(){//function to get data from my table_z
        SQLiteDatabase db = this.getWritableDatabase();//make instance to the database
        Cursor res = db.rawQuery("select * from "+ TABLE_NAME_Z, null);//get all the data from the table and puts it into a Cursor
        return res;//returns my values
    }

    public boolean deleteAll() {
        SQLiteDatabase db = this.getWritableDatabase();

        //db.execSQL("DELETE FROM sqlite_sequence WHERE name = " + TABLE_NAME);
        //db.rawQuery("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '"+ TABLE_NAME+"'",null);
        db.delete(TABLE_NAME, null,null );
        db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '"+ TABLE_NAME+"'");
        db.delete(TABLE_NAME1, null,null);
        db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '"+ TABLE_NAME1+"'");
        db.delete(TABLE_NAME2, null,null);
        db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '"+ TABLE_NAME2+"'");
        db.delete(TABLE_NAME3, null,null);
        db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '"+ TABLE_NAME3+"'");
        db.delete(TABLE_NAME_ALL, null,null);
        db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '"+ TABLE_NAME_ALL+"'");
        db.delete(TABLE_NAME_H, null,null);
        db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '"+ TABLE_NAME_H+"'");
        db.delete(TABLE_NAME_HM, null,null);
        db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '"+ TABLE_NAME_HM+"'");
        db.delete(TABLE_NAME_HI, null,null);
        db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '"+ TABLE_NAME_HI+"'");
        db.delete(TABLE_NAME_HB, null,null);
        db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '"+ TABLE_NAME_HB+"'");
        db.delete(TABLE_NAME_HB1, null,null);
        db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '"+ TABLE_NAME_HB1+"'");
        db.delete(TABLE_NAME_Z,null,null);
        db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '"+ TABLE_NAME_Z+"'");
        db.execSQL("VACUUM");
        return true;
    }


    public DataPoint[] getAllDataGraph(){//function to update the graph

        DataPoint[] dp = new DataPoint[getAllData().getCount()];//counts the number of objects into the graph

        for (int i = 1; i < getAllData().getCount();i++){//reads the data from sqlite
            getAllData().moveToNext();
            dp[i] = new DataPoint(getAllData().getInt(0),Double.parseDouble(getAllData().getString(1)));
        }
        return dp;//returns my values
    }
    public Cursor getMAX(){
        SQLiteDatabase db = this.getWritableDatabase();//make instance to the database
        Cursor res = db.rawQuery("select * FROM "+TABLE_NAME1+" WHERE "+COL3+" = (SELECT MIN("+COL3+") FROM "+TABLE_NAME1+") OR "+COL3+" = (SELECT MAX("+COL3+") FROM "+TABLE_NAME1+") ORDER BY "+COL3 , null);//get all the data from the table and puts it into a Cursor


        return res;//returns my values
    }


    ////////////////////////////////////////////////// new form
    public List<String> saveListNew(String data, String data1, String data2, List<String> dataACC) {

        int i = 0;//counter
        //Float datatoFloat = Float.parseFloat(String.valueOf(data));
        boolean question = false;//item to get, out of the for() the boolean element

            savedList.clear();//clear list
            //savedListTime.clear();
            if (data != null) {

                //savedList.add(dataACC.get(0));
                //savedList.add(dataACC.get(1));
                //savedList.add(dataACC.get(2));

                String value1 = String.valueOf(myFilter.Acceleration_Vector(dataACC.get(0), dataACC.get(3), dataACC.get(6)));//get the total vector acceleration
                String value2 = String.valueOf(myFilter.Acceleration_Vector(dataACC.get(1), dataACC.get(4), dataACC.get(7)));//get the total vector acceleration
                String value3 = String.valueOf(myFilter.Acceleration_Vector(dataACC.get(2), dataACC.get(5), dataACC.get(8)));//get the total vector acceleration
                savedList.add(value1);
                savedList.add(value2);
                savedList.add(value3);

                Long getTime = System.currentTimeMillis();
                if (tm == 0) {
                    tm = getTime;
                }
                timeActual = getTime - tm;
                savedListTime.add(timeActual);
                savedListTime.add(timeActual);
                savedListTime.add(timeActual);
                //savedList.set(CounterBleData+1, data);
                CounterBleData++;//increases the counter till reach the desire count
                return savedList;
            }




            //savedList.clear();//clear list
            //savedListTime.clear();

        return savedList;
    }
    //////////////////////////////////////////////////end new form
    public boolean saveList(String data, String data1, String data2, List<String> dataACC) {

        int i = 0;//counter
        //Float datatoFloat = Float.parseFloat(String.valueOf(data));
        boolean question = false;//item to get, out of the for() the boolean element


        if (CounterBleData < 5) {//if the list is not full then add data to the list
            if (data != null) {

                //savedList.add(dataACC.get(0));
                //savedList.add(dataACC.get(1));
                //savedList.add(dataACC.get(2));

                String value1 = String.valueOf(myFilter.Acceleration_Vector(dataACC.get(0), dataACC.get(3), dataACC.get(6)));//get the total vector acceleration
                String value2 = String.valueOf(myFilter.Acceleration_Vector(dataACC.get(1), dataACC.get(4), dataACC.get(7)));//get the total vector acceleration
                String value3 = String.valueOf(myFilter.Acceleration_Vector(dataACC.get(2), dataACC.get(5), dataACC.get(8)));//get the total vector acceleration
                savedList.add(value1);
                savedList.add(value2);
                savedList.add(value3);

                Long getTime = System.currentTimeMillis();
                if (tm == 0) {
                    tm = getTime;
                }
                timeActual = getTime - tm;
                savedListTime.add(timeActual);
                savedListTime.add(timeActual);
                savedListTime.add(timeActual);
                //savedList.set(CounterBleData+1, data);
                CounterBleData++;//increases the counter till reach the desire count
            }

        }
        else if (CounterBleData == 5){//if the list is full then do
            for (i = 0; i <= savedList.size() - 1; i++) {// go trhoughout all the elements of the list
                CounterBleData = 0;

                boolean insert = insertData(savedList.get(i), savedListTime.get(i));//save all the elements into the database
                if (insert = true) {
                    question = true;
                }


            }

            savedList.clear();//clear list
            savedListTime.clear();

            //savedList.clear();//clear list
            //savedListTime.clear();
            return true;
        }
    return true;
    }
//////////////////////////
public boolean saveList_X_new(String data, String data1, String data2, List<String> dataACC){

    //Float datatoFloat = Float.parseFloat(String.valueOf(data));
        if (data != null) {
            savedList_X.add(dataACC.get(0));
            savedList_X.add(dataACC.get(1));
            savedList_X.add(dataACC.get(2));
            savedList_Y.add(dataACC.get(3));
            savedList_Y.add(dataACC.get(4));
            savedList_Y.add(dataACC.get(5));
            savedList_Z.add(dataACC.get(6));
            savedList_Z.add(dataACC.get(7));
            savedList_Z.add(dataACC.get(8));

        }
            //savedList.set(CounterBleData+1, data);

//            boolean insert = insertData4(savedList_X.get(i), savedList_Y.get(i), savedList_Z.get(i));//save all the elements into the database

    return true;



}
    ///////////////////////////////////////
    public boolean saveList_X(String data, String data1, String data2, List<String> dataACC){

        int i = 0;//counter
        //Float datatoFloat = Float.parseFloat(String.valueOf(data));
        boolean question = false;//item to get, out of the for() the boolean element
        if(CounterBleData1 < 5) {//if the list is not full then add data to the list
            if (data != null){
                savedList_X.add(dataACC.get(0));
                savedList_X.add(dataACC.get(1));
                savedList_X.add(dataACC.get(2));
                savedList_Y.add(dataACC.get(3));
                savedList_Y.add(dataACC.get(4));
                savedList_Y.add(dataACC.get(5));
                savedList_Z.add(dataACC.get(6));
                savedList_Z.add(dataACC.get(7));
                savedList_Z.add(dataACC.get(8));


                //savedList.set(CounterBleData+1, data);
                CounterBleData1++;//increases the counter till reach the desire count
                return false;}

        }
        else if (CounterBleData1 == 5)//if the list is full then do
            for (i = 0; i<= savedList_X.size()-1; i++ ) {// go trhoughout all the elements of the list
                CounterBleData1 = 0;

                boolean insert = insertData4(savedList_X.get(i), savedList_Y.get(i), savedList_Z.get(i));//save all the elements into the database
                if (insert = true) {
                    question = true;
                }


            }

        savedList_X.clear();//clear list
        savedList_Y.clear();
        savedList_Z.clear();
        return true;



    }

    public void setZero(){
        tm = 0;
        timeActual = 0;

    }




}
