package com.example.moneymanagement3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;

import com.example.moneymanagement3.ui.home.HomeFragment;

import java.time.LocalDate;

public class DataBaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Tracker.db";
    public static final String TABLE_NAME = "names_table";
    public static final String TABLE_NAME2 = "categories_table";
    public static final String TABLE_NAME3 = "setting_table";
    public static final String COL_0 = "ID";
    public static final String COL_1 = "NAME";
    public static final String COL_2 = "AMOUNT";
    public static final String COL_3 = "CATEGORY";
    public static final String COL_4 = "DATE";
    public static final String COL_5 = "START_DATE";
    public static final String COL_6 = "END_DATE";
    public static final String COL_7 = "DATE_TIMESTAMP1";
    public static final String COL_8 = "CYCLE_INPUT";

//    public static final String COL_2 = "NAME";

    public DataBaseHelper(@Nullable FragmentActivity context) {
        super(context, DATABASE_NAME, null , 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //creating table "names_table" with 2 cols
        db.execSQL("create table " + TABLE_NAME +" (ID INTEGER PRIMARY KEY AUTOINCREMENT, NAME TEXT, AMOUNT TEXT, CATEGORY TEXT, DATE TEXT,DATE_TIMESTAMP1 TIMESTAMP )");
        db.execSQL("create table " + TABLE_NAME2 +" (ID INTEGER PRIMARY KEY AUTOINCREMENT, CATEGORY TEXT)");
        db.execSQL("create table " + TABLE_NAME3 +" (START_DATE TEXT, END_DATE TEXT, CYCLE_INPUT TEXT)");

//        db.execSQL("create table " + TABLE_NAME +" (ID INTEGER PRIMARY KEY AUTOINCREMENT,NAME TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME2);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME3);
        onCreate(db);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean insertData(String name, String amount, String category, String date, LocalDate DATE_TIMESTAMP1) {
        //Table1
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1,name);
        contentValues.put(COL_2,amount);
        contentValues.put(COL_3,category);
        contentValues.put(COL_4,date);
        contentValues.put(COL_7, String.valueOf(DATE_TIMESTAMP1));
        long result = db.insert(TABLE_NAME,null ,contentValues);
        if (result == -1)
            return false;
        else
            return true;
    }

    public boolean updateData( String id, String name, String amount, String category, String date,LocalDate DATE_TIMESTAMP1) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1,name);
        contentValues.put(COL_2,amount);
        contentValues.put(COL_3,category);
        contentValues.put(COL_4,date);
        contentValues.put(COL_7, String.valueOf(DATE_TIMESTAMP1));
        db.update(TABLE_NAME, contentValues, "ID = ?",new String[] { id });
        return true;
    }

    public Integer deleteData (String id) {
        //Table 1
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, "ID = ?",new String[] {id});
    }

    public Integer deleteAll() {
        //table1
        SQLiteDatabase db = this.getWritableDatabase();
        int deletedrows = db.delete(TABLE_NAME,"1",null);
        db.close();
        return deletedrows;
    }


    public Cursor getAllData() {
        //Table 1
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+TABLE_NAME,null);
        return res;
    }

    public Cursor getDataDateRange(LocalDate StartDate, LocalDate EndDate) {
        //Table 1
        // this would mainly be for our charts, we insert two timestamp and do some comparisons ez pz probably
        SQLiteDatabase db = this.getWritableDatabase();
        //Cursor res = db.rawQuery("select * from "+TABLE_NAME +" WHERE DATE_TIMESTAMP1 > datetime(" + StartDate +")"+ "& DATE_TIMESTAMP1 < datetime(" + EndDate+")" ,null);
        Cursor res = db.rawQuery("select * from "+TABLE_NAME +" WHERE DATE_TIMESTAMP1 BETWEEN "+ "datetime(" + "\"" +StartDate + "\"" + ")" + "AND " + "datetime("+ "\"" +EndDate+"\""+ ")",null);
        return res;
    }

    @RequiresApi(api = Build.VERSION_CODES.O) //okay so this requires android oreo to run
    public Cursor getDataDefault() {
        //Table 1
        // this would be a sort of default time where we just grab data from the same month
        LocalDate currentDate = LocalDate.now();
        String currentMonth= String.valueOf(currentDate.getMonthValue());
        String currentYear= String.valueOf(currentDate.getYear());

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_NAME + " WHERE ltrim(substr(DATE_TIMESTAMP1,6,2),0) = "+ "\"" + currentMonth  + "\"" + " AND substr(DATE_TIMESTAMP1,1,4) = " + "\"" + currentYear+ "\"",null);
        return res;
    }



    //////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void insertData_categories(String category) {
        //Table2
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_3,category);
        long result = db.insert(TABLE_NAME2,null ,contentValues);
        if (result == -1) {
        }
    }


    public Integer delete_categories(String id) {
        //table2
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME2, "ID = ?",new String[] {id});
    }


    public Integer deleteAll_categories() {
        //table2
        SQLiteDatabase db = this.getWritableDatabase();
        int deletedrows = db.delete(TABLE_NAME2,"1",null);
        db.close();
        return deletedrows;
    }

    public Cursor getAllData_categories() {
        //Table 2
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+TABLE_NAME2,null);
        return res;
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////

    public void replace_setting ( String startdate, String enddate, String cycle_input) {
        //Table 3
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME3,"1",null);

        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_5,startdate);
        contentValues.put(COL_6,enddate);
        contentValues.put(COL_8,cycle_input);
        long result = db.insert(TABLE_NAME3,null ,contentValues);
    };

    public boolean update_cycle_input (String old_cycle_input, String new_cycle_input) {
        //Table 3
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_8,new_cycle_input);
        long result = db.update(TABLE_NAME3, contentValues, "CYCLE_INPUT = ?",new String[]{old_cycle_input});
        if (result > 0)
            return true;
        else
            return false;
    };


    public void deleteAll_setting () {
        //Table 3
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME3,"1",null);
    };

    public void insert_setting (String startdate, String enddate, String cycle_input) {
        //Table 3
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_5,startdate);
        contentValues.put(COL_6,enddate);
        contentValues.put(COL_8,cycle_input);
        long result = db.insert(TABLE_NAME3,null ,contentValues);
    };

    public Cursor get_setting() {
        //Table 3
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+TABLE_NAME3,null);
        return res;
    }


}
