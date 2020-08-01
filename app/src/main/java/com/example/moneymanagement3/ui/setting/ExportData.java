package com.example.moneymanagement3.ui.setting;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Space;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.moneymanagement3.DataBaseHelper;
import com.example.moneymanagement3.R;
import com.example.moneymanagement3.ui.budget.SetBudgetCatFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MONTHS;

public class ExportData extends Fragment {
    View view;
    DataBaseHelper myDb;
    Cursor res3; Cursor res4; Cursor res; Cursor res_data;
    Button btn1; Button btn_export;
    TextView tv_dates;
    LocalDate startdate; LocalDate enddate;
    DateTimeFormatter formatter;
    StringBuilder data;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_export_data, container, false);
        view.setBackgroundColor(Color.WHITE);


        myDb = new DataBaseHelper(getActivity());
        //Cursor res = gets all data in the database table3
        res3 = myDb.get_setting();

        //Variables
        btn1 = view.findViewById(R.id.gobackBtn);
        tv_dates = view.findViewById(R.id.dates);
        btn_export = view.findViewById(R.id.btn_export);

        formatter = DateTimeFormatter.ofPattern("LLL dd, yyyy");

        //onstartup
        res3.moveToFirst();
        startdate = LocalDate.parse(res3.getString(0));
        enddate = LocalDate.parse(res3.getString(1));
        tv_dates.setText(startdate.format(formatter) + " ~ " + enddate.format(formatter));

        //build data to transfer as csv file
        data_builder();


        onClick_ExportBtn();

        //calling functions
        onClick_GoBackBtn ();

        return view;
    }

////////////////////////////////////////////////////////////////////////////////////////////////////

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void data_builder(){
        res_data = myDb.getDataDateRange(startdate.minusDays(1),enddate);
        //generate data
        data = new StringBuilder();
        data.append("Amount,Category,Description,Payment Type,Date");
        while(res_data.moveToNext()){
            data.append("\n" + res_data.getString(2) + ","
                    + res_data.getString(3) + ","
                    + res_data.getString(1) + ","
                    + res_data.getString(6) + ","
                    + res_data.getString(4) + ",");
        }


    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void export() {

        try {
            //saving the file into device
            FileOutputStream out = getActivity().openFileOutput("data.csv", getActivity().MODE_PRIVATE);
            out.write((data.toString()).getBytes());
            out.close();

//            //exporting
//            Context context = getActivity().getApplicationContext();
//            File filelocation = new File(context.getFilesDir(), "data.csv");
////            Uri path = Uri.fromFile(filelocation);
//            Uri path = FileProvider.getUriForFile(context, "com.example.moneymanagement3.fileprovider", filelocation);
//            Intent fileIntent = new Intent(Intent.ACTION_SEND);
//            fileIntent.setType("text/csv");
//            fileIntent.putExtra(Intent.EXTRA_SUBJECT, "Entries");
//            fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            fileIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//            fileIntent.putExtra(Intent.EXTRA_STREAM, path);
//            startActivity(Intent.createChooser(fileIntent, "Send mail"));

            //exporting
            Context context = getActivity().getApplicationContext();
            File filepath = new File(context.getFilesDir(),".");
            File filelocation = new File(filepath, "data.csv");
            Uri path = FileProvider.getUriForFile(context, "com.example.moneymanagement3.fileprovider", filelocation);
            Intent fileIntent = new Intent(Intent.ACTION_SEND);
            fileIntent.setType("text/csv");
            fileIntent.putExtra(Intent.EXTRA_SUBJECT, "Entries");
            fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            fileIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            fileIntent.putExtra(Intent.EXTRA_STREAM, path);
            startActivity(Intent.createChooser(fileIntent, "Send mail"));



        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void onClick_ExportBtn () {
        //Button to go back to settings
        btn_export.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void onClick(View v) {
                export();
            }
        });
    }


    public void onClick_GoBackBtn () {
        //Button to go back to settings
        btn1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SettingFragment frag= new SettingFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, frag, "settingFrag")
                        .commit();
            }
        });
    }





}
