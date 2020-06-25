package com.example.moneymanagement3;

import android.database.Cursor;

import androidx.fragment.app.FragmentActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MyDate extends FragmentActivity {

    public MyDate(){}

    public static String getMonth() {
        String currentDate;
        String current_month;
        String[] months;

        months = new String[]{"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

        currentDate = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(new Date());
        current_month = "" + currentDate.charAt(0) + currentDate.charAt(0);

        return current_month;
    }

    public static String getYear() {
        String currentDate;
        String current_year;

        currentDate = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(new Date());
        current_year = "" + currentDate.charAt(6) + currentDate.charAt(7) + currentDate.charAt(8) + currentDate.charAt(9);

        return current_year;

    }



}

