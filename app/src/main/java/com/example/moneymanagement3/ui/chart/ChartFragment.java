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

    @RequiresApi(api = Build.VERSION_CODES.O)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_chart, container, false);

        btn_to_linechart = view.findViewById(R.id.to_linechart_btn);
        onClick_toLineChartBtn();

        btn_to_piechart = view.findViewById(R.id.to_piechart_btn);
        onClick_toPieChartBtn();

        btn_to_barchart = view.findViewById(R.id.to_barchart_btn);
        onClick_toBarChartBtn();

        btn_to_infochart = view.findViewById(R.id.to_infochart_btn);
        onClick_toInfoChartBtn();



        btn = view.findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {


            }
        });



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
                PieChartFragment frag= new PieChartFragment();
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




}
