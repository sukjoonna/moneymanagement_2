package com.example.moneymanagement3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.example.moneymanagement3.ui.home.HomeFragment;

public class DataBaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Tracker.db";
    public static final String TABLE_NAME = "names_table";
    public static final String TABLE_NAME2 = "categories_table";
    public static final String TABLE_NAME3 = "month_year_table";
    public static final String COL_0 = "ID";
    public static final String COL_1 = "NAME";
    public static final String COL_2 = "AMOUNT";
    public static final String COL_3 = "CATEGORY";
    public static final String COL_4 = "DATE";
    public static final String COL_5 = "MONTH";
    public static final String COL_6 = "YEAR";


//    public static final String COL_2 = "NAME";

    public DataBaseHelper(@Nullable FragmentActivity context) {
        super(context, DATABASE_NAME, null , 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //creating table "names_table" with 2 cols
        db.execSQL("create table " + TABLE_NAME +" (ID INTEGER PRIMARY KEY AUTOINCREMENT, NAME TEXT, AMOUNT TEXT, CATEGORY TEXT, DATE TEXT)");
        db.execSQL("create table " + TABLE_NAME2 +" (ID INTEGER PRIMARY KEY AUTOINCREMENT, CATEGORY TEXT)");
        db.execSQL("create table " + TABLE_NAME3 +" (YEAR STRING, MONTH STRING)");

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

    public boolean insertData(String name, String amount, String category, String date) {
        //Table1
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1,name);
        contentValues.put(COL_2,amount);
        contentValues.put(COL_3,category);
        contentValues.put(COL_4,date);
        long result = db.insert(TABLE_NAME,null ,contentValues);
        if (result == -1)
            return false;
        else
            return true;
    }

    public boolean updateData( String id, String name, String amount, String category, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1,name);
        contentValues.put(COL_2,amount);
        contentValues.put(COL_3,category);
        contentValues.put(COL_4,date);
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

    public void replace_MonthAndYear (String month, String year) {
        //Table 3
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME3,"1",null);

        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_5,month);
        contentValues.put(COL_6,year);
        long result = db.insert(TABLE_NAME3,null ,contentValues);
    };

    public void insert_MonthAndYear (String month, String year) {
        //Table 3
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_5,month);
        contentValues.put(COL_6,year);
        long result = db.insert(TABLE_NAME3,null ,contentValues);
    };

    public Cursor get_MonthAndYear() {
        //Table 3
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+TABLE_NAME3,null);
        return res;
    }


}
