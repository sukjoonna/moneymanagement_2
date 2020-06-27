package com.example.moneymanagement3;

import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import java.time.LocalDate;

public class CycleUpdaterFragment extends Fragment {



    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true); // Must be set to true



    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void cycle_updater() {

        String cycle_input;
        Cursor res3;
        LocalDate currentDate; LocalDate startdate; LocalDate enddate;

        //creates database
        DataBaseHelper myDb = new DataBaseHelper(getActivity());
        //Cursor res = gets all data in the database table2 and table3
        res3 = myDb.get_setting();

        cycle_input = "01"; //sets the default cycle input as the first of the month
        currentDate = LocalDate.now();

        if (res3!=null && res3.moveToFirst()){  //makes sure table3 is not null
            cycle_input = res3.getString(2);;
        }
        String currentDate_string = String.valueOf(currentDate);
        String currentMonth_string = ""+ currentDate_string.substring(5,7); //"MM" -- [start ind,end ind)

        String var_string = ""+currentDate_string.substring(0,5) + currentMonth_string + "-" + cycle_input; //variable to compare current date with
        LocalDate var = LocalDate.parse(var_string);    //convert var into a localdate

        //determine and sets the start and end dates of the cycle
        if (currentDate.isBefore(var)){
            LocalDate var_new = var.plusMonths(-1);
            startdate = var_new;
            enddate = var;
            //update database table3
            myDb.replace_setting(String.valueOf(startdate) , String.valueOf(enddate) , cycle_input );
        }
        else {
            LocalDate var_new = var.plusMonths(1);
            startdate = var;
            enddate = var_new;
            //update database table3
            myDb.replace_setting(String.valueOf(startdate) , String.valueOf(enddate) , cycle_input );
        }


    }

//    @Override
//    public View onCreateView(...) {
//        return null; // You need to return null
//    }

}
