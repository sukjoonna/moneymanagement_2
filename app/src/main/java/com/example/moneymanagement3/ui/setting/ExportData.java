package com.example.moneymanagement3.ui.setting;

import android.app.Activity;
import android.app.DatePickerDialog;
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
import android.widget.DatePicker;
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
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MONTHS;

public class ExportData extends Fragment {
    View view;
    DataBaseHelper myDb;
    Cursor res3; Cursor res4; Cursor res; Cursor res_data;
    Button btn1; Button btn_export; Button btn_currentCycle; Button btn_cycles; Button btn_customDates;
    Button btn_setStartDate; Button btn_setEndDate;
    TextView tv_dates;
    LocalDate startdate; LocalDate enddate;
    DateTimeFormatter formatter;
    StringBuilder data;
    Spinner spinner_cycles;
    LocalDate startdate_temp; LocalDate enddate_temp;
    DatePickerDialog.OnDateSetListener mDateSetListener_start;     DatePickerDialog.OnDateSetListener mDateSetListener_end;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_export_data, container, false);
        view.setBackgroundColor(Color.WHITE);


        myDb = new DataBaseHelper(getActivity());
        //Cursor res = gets all data in the database table3
        res3 = myDb.get_setting();

        //Variables
        btn1 = view.findViewById(R.id.gobackBtn);
        btn_currentCycle = view.findViewById(R.id.btnCurrentCycle);
        btn_cycles = view.findViewById(R.id.btnCycles);
        btn_customDates = view.findViewById(R.id.btnCustomDates);
        tv_dates = view.findViewById(R.id.dates);
        btn_export = view.findViewById(R.id.btn_export);

        formatter = DateTimeFormatter.ofPattern("LLL dd, yyyy");

        //onstartup
        res3.moveToFirst();
        startdate = LocalDate.parse(res3.getString(0));
        enddate = LocalDate.parse(res3.getString(1));
        tv_dates.setText(startdate.format(formatter) + " ~ " + enddate.format(formatter));

        onClick_CurrentCycleBtn();
        onClick_CyclesBtn();
        onClick_CustomDatesBtn();

        //build data to transfer as csv file
        data_builder();


        onClick_ExportBtn();

        //calling functions
        onClick_GoBackBtn ();

        return view;
    }

////////////////////////////////////////////////////////////////////////////////////////////////////


    public void onClick_CurrentCycleBtn () {
        //Button to go back to settings
        btn_currentCycle.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void onClick(View v) {
                res3 = myDb.get_setting();
                res3.moveToFirst();
                startdate = LocalDate.parse(res3.getString(0));
                enddate = LocalDate.parse(res3.getString(1));
                data_builder();
                tv_dates.setText(startdate.format(formatter) + " ~ " + enddate.format(formatter));
            }
        });
    }

    public void onClick_CyclesBtn () {
        //Button to go back to settings
        btn_cycles.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void onClick(View v) {
                final ArrayList<String> cycles_startToEnd = new ArrayList<>();

                LayoutInflater inflater = getLayoutInflater();
                final View view_alertSpinner = inflater.inflate(R.layout.alert_spinner, null);
                spinner_cycles = view_alertSpinner.findViewById(R.id.the_spinner);
                //Create Cycle Spinner --- from table4

                //------------------------CYCLE CREATE AND UPDATER in DB (ALONG WITH SPINNER) -------------------------//                   *Make sure this is at top
                res3 = myDb.get_setting();
                res3.moveToFirst();

                String num_of_cycles = res3.getString(3);
                if(num_of_cycles.equals("All")){
                    num_of_cycles = "1000000";
                }
                int count = 0;

                //Create Cycle Spinner --- from table4
                res4 = myDb.get_cycles();
                while(res4.moveToNext()){
                    String cyc_startdate = res4.getString(0);
                    String cyc_enddate = res4.getString(1);
                    LocalDate cyc_startdate_localdate = LocalDate.parse(cyc_startdate);
                    LocalDate cyc_enddate_localdate = LocalDate.parse(cyc_enddate);

                    //Formatting the localdate ==> custom string format (Month name dd, yyyy)
                    DateTimeFormatter cyc_formatter = DateTimeFormatter.ofPattern("LLL dd, yy");
                    String cyc_startdate_formatted = cyc_startdate_localdate.format(cyc_formatter);
                    String cyc_enddate_formatted = cyc_enddate_localdate.format(cyc_formatter);

                    String formatted_dates = cyc_startdate_formatted + " ~ " + cyc_enddate_formatted;
                    cycles_startToEnd.add(formatted_dates);
                }

                if(cycles_startToEnd.size() > Integer.parseInt(num_of_cycles)){
                    while(cycles_startToEnd.size() > Integer.parseInt(num_of_cycles)){
                        cycles_startToEnd.remove(0);
                    }
                }
                Collections.reverse(cycles_startToEnd);
                ArrayAdapter<String> spn_cyc_adapter = new ArrayAdapter<String>(view.getContext(), R.layout.spinner_text,cycles_startToEnd);
                spinner_cycles.setAdapter(spn_cyc_adapter);

                //------------------------------------------------END-----------------------------------------------//


                //What the spinner does when item is selected / not selected
                spinner_cycles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View v, final int position, long id) {
                        //this is important bc "cycles"/spinner shows new-->old, but in the database table4, it's indexed old--->new
                        int inverted_pos = (cycles_startToEnd.size() - 1) - position;
                        res4.moveToPosition(inverted_pos);
                        startdate_temp = LocalDate.parse(res4.getString(0));
                        enddate_temp = LocalDate.parse(res4.getString(1));
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                        Object item = adapterView.getItemAtPosition(0);
                    }
                });


                //create the alertdialog
                AlertDialog.Builder adb = new AlertDialog.Builder(view.getContext());
                adb.setView(view_alertSpinner);
                adb.setTitle("Select cycle:");
                adb.setPositiveButton("Select",null);
                adb.setNeutralButton("Back",null);

                final AlertDialog alertDialog2 = adb.create();
                alertDialog2.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(final DialogInterface dialog) {
                        //Select button (first alertdialog)
                        Button positiveButton = alertDialog2.getButton(AlertDialog.BUTTON_POSITIVE);
                        positiveButton.setOnClickListener(new View.OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void onClick(View v) {
                                startdate = startdate_temp;
                                enddate = enddate_temp;
                                data_builder();
                                tv_dates.setText(startdate.format(formatter) + " ~ " + enddate.format(formatter));
                                dialog.dismiss();

                            }
                        });

                        //Back button (first alertdialog)
                        Button neutralButton = alertDialog2.getButton(AlertDialog.BUTTON_NEUTRAL);
                        neutralButton.setOnClickListener(new View.OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                    }
                });

                alertDialog2.show();


            }
        });
    }

    public void onClick_CustomDatesBtn () {
        //Button to go back to settings
        btn_customDates.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void onClick(View v) {
                startdate_temp = null;
                enddate_temp = null;

                AlertDialog.Builder adb = new AlertDialog.Builder(view.getContext());
                LayoutInflater inflater = getLayoutInflater();
                final View view_setCustomDateBtns = inflater.inflate(R.layout.set_custom_dates, null);
                adb.setView(view_setCustomDateBtns);
                adb.setNeutralButton("Back",null);
                adb.setPositiveButton("Set",null);

                final AlertDialog alertDialog3 = adb.create();

                alertDialog3.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(final DialogInterface dialog) {
                        btn_setStartDate = view_setCustomDateBtns.findViewById(R.id.startBtn);
                        btn_setEndDate = view_setCustomDateBtns.findViewById(R.id.endBtn);

                        btn_setStartDate.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                DatePickerDialog datePickerDialog = new DatePickerDialog(view.getContext(), mDateSetListener_start,
                                        Calendar.getInstance().get(Calendar.YEAR),
                                        Calendar.getInstance().get(Calendar.MONTH),
                                        Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
                                datePickerDialog.show();
                            }
                        });

                        btn_setEndDate.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                if(startdate_temp!=null){
                                    DatePickerDialog datePickerDialog = new DatePickerDialog(view.getContext(), mDateSetListener_end,
                                            startdate_temp.getYear(),
                                            startdate_temp.getMonthValue()-1,
                                            startdate_temp.getDayOfMonth());
                                    datePickerDialog.show();
                                }
                                else{
                                    DatePickerDialog datePickerDialog = new DatePickerDialog(view.getContext(), mDateSetListener_end,
                                            Calendar.getInstance().get(Calendar.YEAR),
                                            Calendar.getInstance().get(Calendar.MONTH),
                                            Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
                                    datePickerDialog.show();
                                }
                            }
                        });

                        mDateSetListener_start = new DatePickerDialog.OnDateSetListener() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                month = month + 1;

                                String month_string = String.valueOf(month);
                                String day_string = String.valueOf(day);
                                if (month_string.length() < 2){
                                    month_string = "0" + month_string;
                                }
                                if (day_string.length() < 2){
                                    day_string = "0" + day_string;
                                }
                                String date = year + "-" + month_string + "-" + day_string;
                                startdate_temp = LocalDate.parse(date);
                                btn_setStartDate.setText(startdate_temp.format(formatter));

                            }
                        };

                        mDateSetListener_end = new DatePickerDialog.OnDateSetListener() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                month = month + 1;

                                String month_string = String.valueOf(month);
                                String day_string = String.valueOf(day);
                                if (month_string.length() < 2){
                                    month_string = "0" + month_string;
                                }
                                if (day_string.length() < 2){
                                    day_string = "0" + day_string;
                                }
                                String date = year + "-" + month_string + "-" + day_string;
                                enddate_temp = LocalDate.parse(date);
                                btn_setEndDate.setText(enddate_temp.format(formatter));
                            }
                        };

                        //set button (first alertdialog)
                        Button positiveButton = alertDialog3.getButton(AlertDialog.BUTTON_POSITIVE);
                        positiveButton.setOnClickListener(new View.OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void onClick(View v) {

                                if (startdate_temp==null && enddate_temp==null){
                                    Toast.makeText(view.getContext(), "Select dates", Toast.LENGTH_SHORT).show();
                                }
                                else if(startdate_temp==null){
                                    Toast.makeText(view.getContext(), "Select start date", Toast.LENGTH_SHORT).show();
                                }
                                else if(enddate_temp==null){
                                    Toast.makeText(view.getContext(), "Select end date", Toast.LENGTH_SHORT).show();
                                }
                                else if (startdate_temp.isAfter(enddate_temp)){
                                    AlertDialog.Builder adb = new AlertDialog.Builder(view.getContext());
                                    adb.setTitle("The start date is after the end date");
                                    btn_setEndDate.setText("End Date");
                                    adb.setPositiveButton("Okay", null);
                                    adb.show();
                                }
                                else{
                                    startdate = startdate_temp;
                                    enddate = enddate_temp;
                                    data_builder();
                                    tv_dates.setText(startdate.format(formatter) + " ~ " + enddate.format(formatter));
                                    dialog.dismiss();


                                }

                            }
                        });

                    }
                });
                alertDialog3.show();


            }
        });
    }


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
                getActivity().onBackPressed();
            }
        });
    }





}
