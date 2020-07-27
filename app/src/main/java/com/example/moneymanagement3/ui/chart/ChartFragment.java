package com.example.moneymanagement3.ui.chart;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;


import com.example.moneymanagement3.DataBaseHelper;
import com.example.moneymanagement3.R;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.datepicker.MaterialDatePicker;

import android.graphics.Color;

import ru.slybeaver.slycalendarview.SlyCalendarDialog;


public class ChartFragment extends Fragment {


    Button btn_to_piechart;
    Button btn_to_linechart;
    Button btn_to_barchart;
    Button btn_to_infochart; Button btn;
    /////
    //Class - Variable creations
    DataBaseHelper myDb;
    LocalDate currentDate;
    Cursor res2;
    Cursor res3;
    Cursor res4;
    LocalDate startdate;
    LocalDate enddate;
    String cycle_input;


    @RequiresApi(api = Build.VERSION_CODES.O)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_chart, container, false);

        myDb = new DataBaseHelper(getActivity());

        cycle_updater();

        btn_to_linechart = view.findViewById(R.id.to_linechart_btn);
        onClick_toLineChartBtn();

        btn_to_piechart = view.findViewById(R.id.to_piechart_btn);
        onClick_toPieChartBtn();

        btn_to_barchart = view.findViewById(R.id.to_barchart_btn);
        onClick_toBarChartBtn();

        btn_to_infochart = view.findViewById(R.id.to_infochart_btn);
        onClick_toInfoChartBtn();




        return view;
    }

//////////////////////////////////////////////////////////////////////////////////////////////////////


    public void onClick_toPieChartBtn () {
        //Button to go back to settings
        btn_to_piechart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                PieChartFragment frag= new PieChartFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, frag, "chartFrag")
                        .commit();
            }
        });
    }

    public void onClick_toLineChartBtn () {
        //Button to go back to settings
        btn_to_linechart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                LineChartFragment frag= new LineChartFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, frag, "chartFrag")
                        .commit();
            }
        });
    }

    public void onClick_toBarChartBtn () {
        //Button to go back to settings
        btn_to_barchart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                BarChartFragment frag= new BarChartFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, frag, "chartFrag")
                        .commit();
            }
        });
    }

    public void onClick_toInfoChartBtn () {
        //Button to go back to settings
        btn_to_infochart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                InfoFragment frag= new InfoFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, frag, "chartFrag")
                        .commit();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    //updates the start and end date of the cycle
    public void cycle_updater() {

        res3 = myDb.get_setting();
        cycle_input = "01"; //sets the default cycle input as the first of the month
        currentDate = LocalDate.now(); //get current date

        if (res3!=null && res3.moveToFirst()){  //makes sure table3 is not null
            cycle_input = res3.getString(2);
        }
        else{
            myDb.create_filler_setting_onStartup(cycle_input);
        }

        String currentDate_string = String.valueOf(currentDate);
        String currentMonth_string = ""+ currentDate_string.substring(5,7); //"MM" -- [start ind,end ind)

        String var_string = ""+currentDate_string.substring(0,5) + currentMonth_string + "-" + cycle_input; //variable to compare current date with
        LocalDate var = LocalDate.parse(var_string);    //convert var into a localdate

        //determine and sets the new start and end dates of the cycle
        res3 = myDb.get_setting();
        res3.moveToFirst();
        if (currentDate.isBefore(var)){
            LocalDate var_new = var.plusMonths(-1);
            startdate = var_new;
            enddate = var.minusDays(1);
            //update database table3
            myDb.update_cycle_setting(String.valueOf(startdate) , String.valueOf(enddate) , res3.getString(2) );
        }
        else {
            LocalDate var_new = var.plusMonths(1);
            startdate = var;
            enddate = var_new.minusDays(1);
            //update database table3
            myDb.update_cycle_setting(String.valueOf(startdate) , String.valueOf(enddate) , res3.getString(2) );
        }




        //******************************************************************************new added


        //dealing with table4 (cycle table) ---- for cycle spinner
        res4 = myDb.get_cycles();
        if (res4!=null && res4.moveToLast()) { //if table4 is not null on startup (run basically every time this fragment is selected)
            String past_startdate = res4.getString(0);
            String past_enddate = res4.getString(1);

//            if (!past_startdate.equals(String.valueOf(startdate)) && !past_enddate.equals(String.valueOf(enddate))) { //if a new cycle started (new month)
//                long difference_month = 0;
//                difference_month = ChronoUnit.MONTHS.between(LocalDate.parse(past_startdate),startdate);
//
//                res4.moveToLast();
//                String cycle_budget = res4.getString(2);
//                String categories_list_as_string = res4.getString(3);
//                String categories_budget_list_as_string = res4.getString(4);
//                //inserts the start and end date of the cycle only if the dates changed
//                myDb.insert_new_cycle(String.valueOf(startdate), String.valueOf(enddate), cycle_budget,
//                        categories_list_as_string, categories_budget_list_as_string);
//            }
            if (!past_startdate.equals(String.valueOf(startdate)) && !past_enddate.equals(String.valueOf(enddate))) { //if a new cycle started (new month)
                long difference_month = 0;
                difference_month = ChronoUnit.MONTHS.between(LocalDate.parse(past_startdate),startdate);

                res4.moveToLast();
                LocalDate startdate_temp = LocalDate.parse(res4.getString(0));
                LocalDate enddate_temp = LocalDate.parse(res4.getString(1));
                String cycle_budget = res4.getString(2);
                String categories_list_as_string = res4.getString(3);
                String categories_budget_list_as_string = res4.getString(4);

                for(int i = 0; i < difference_month; i++){
                    startdate_temp = startdate_temp.plusMonths(1);
                    enddate_temp = startdate_temp.plusMonths(1).minusDays(1);
                    //inserts the start and end date of the cycle only if the dates changed
                    myDb.insert_new_cycle(String.valueOf(startdate_temp), String.valueOf(enddate_temp), cycle_budget,
                            categories_list_as_string, categories_budget_list_as_string);
                }

            }



        }

        else { //if table4 null (only when first run)

            StringBuilder categories_budget_list_as_string = new StringBuilder();
            StringBuilder categories_list_as_string = new StringBuilder();
            res2 = myDb.getAllData_categories();
            if (res2 != null) { // if categories table3 is not empty
                while (res2.moveToNext()) {
                    String category = res2.getString(1);
                    categories_list_as_string.append(category).append(";");
                    categories_budget_list_as_string.append("0.00").append(";");
                }
            }
            //inserts the start and end date of the cycle only if the dates changed
            myDb.insert_new_cycle(String.valueOf(startdate), String.valueOf(enddate), "0.00",
                    categories_list_as_string.toString(), categories_budget_list_as_string.toString());


        }

        //******************************************************************************new added

    }



}
