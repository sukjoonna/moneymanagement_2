package com.example.moneymanagement3.ui.chart;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.icu.text.IDNA;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;

import com.example.moneymanagement3.DataBaseHelper;
import com.example.moneymanagement3.R;
import com.example.moneymanagement3.ui.setting.ManageCatFragment;
import com.example.moneymanagement3.ui.tracker.Entry;
import com.example.moneymanagement3.ui.tracker.EntryListAdapter;
import com.example.moneymanagement3.ui.tracker.TrackerFragment;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

public class InfoFragment extends Fragment implements DatePickerDialog.OnDateSetListener {
    View view;
    DataBaseHelper myDb;
    Button btn1;
    Cursor res3; Cursor res2; Cursor res4; Cursor res_byCategory; Cursor res;
    //cycle updater variables
    LocalDate startdate; LocalDate startdate_this;
    LocalDate enddate; LocalDate enddate_this;
    ListView lv;
    TextView tv_total; TextView tv_customDates; TextView tv_spinner;
    String text;
    Spinner spinner_cycles;
    ArrayList<Entry> entries_arraylist; ArrayList<String> categories_inRange; ArrayList<String> paymentTypes_inRange;
    EntryListAdapter entries_adapter;
    double amount_total = 0;
    DateTimeFormatter formatter;
    Button btn_chooseToShow; Button btn_selectDates; Button btn_categories; Button btn_paymentTypes; Button btn_all;
    LocalDate cycle_startdate;
    LocalDate cycle_enddate;
    Button btn_setStartDate;
    Button btn_setEndDate;
    DatePickerDialog.OnDateSetListener mDateSetListener_start;     DatePickerDialog.OnDateSetListener mDateSetListener_end;
    LocalDate temp_startdate;
    LocalDate temp_enddate;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_infochart, container, false);
        view.setBackgroundColor(Color.WHITE);
        btn1 = view.findViewById(R.id.gobackBtn);

        //get database
        myDb = new DataBaseHelper(getActivity());

        lv = view.findViewById(R.id.listView);
        tv_total = view.findViewById(R.id.totalTv);
        btn_selectDates = view.findViewById(R.id.setDatesBtn);
        btn_chooseToShow = view.findViewById(R.id.chooseToShowBtn);
        tv_customDates = view.findViewById(R.id.customDatesTv);

        formatter = DateTimeFormatter.ofPattern("LLL dd, yyyy");

//        cycle_updater();
//        startdate_this = startdate;
//        enddate_this = enddate;

        Cursor res_startup = myDb.get_setting();
        res_startup.moveToFirst();
        startdate_this = LocalDate.parse(res_startup.getString(0));
        enddate_this = LocalDate.parse(res_startup.getString(1));
        setArrayLists_categories_and_paymentTypes_InRange(startdate_this,enddate_this);

        res = myDb.getDataDateRange(startdate_this,enddate_this);
        ArrayList<Cursor> startup_res_arraylist = new ArrayList<>();
        startup_res_arraylist.add(res);

        build_List(startup_res_arraylist);
        set_listview();
        set_total();

        onClick_chooseToShow();
        onClick_selectDates();








        onClick_GoBackBtn();

        return view;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void build_List(ArrayList<Cursor> passed_res_arraylist) {

        entries_arraylist = new ArrayList<Entry>();
        entries_adapter = new EntryListAdapter(view.getContext(),R.layout.adapter_view_layout,entries_arraylist);
        amount_total = 0;

        for (int i = 0; i < passed_res_arraylist.size(); i++){
            Cursor passed_res = passed_res_arraylist.get(i);

            if(passed_res!=null){

                //takes the values out from database and puts it into the arraylist &&&& calculates total amount
                while (passed_res.moveToNext()) {
                    String dscpt = passed_res.getString(1);
                    String amt = "-$" + passed_res.getString(2);
                    String cat = passed_res.getString(3);
                    LocalDate date = LocalDate.parse(passed_res.getString(4));
                    String payment = passed_res.getString(6);

                    if (payment.equals("None")){
                        payment = "";
                    }

                    //Formatting the localdate ==> custom string format (Month name dd, yyyy)
                    formatter = DateTimeFormatter.ofPattern("LLL dd, yyyy");
                    String date_formatted = date.format(formatter);

                    String cat_plus_payment = "";
                    if (payment.equals("")){
                        cat_plus_payment = cat ;
                    }
                    else{
                        cat_plus_payment = cat + " | " + payment;
                    }

                    //creating the entry object and putting it into the entries arraylist
                    Entry entry = new Entry(dscpt,cat_plus_payment,amt,date_formatted);
                    entries_arraylist.add(entry);

                    //summing the total spent
                    double amount = Double.parseDouble(passed_res.getString(2));
                    amount_total += amount;


                }

            }
        }



    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public  void set_listview() { // also sets the customized dates textview
        //puts the arraylist into the listview
        lv.setAdapter(entries_adapter);
        entries_adapter.notifyDataSetChanged();

        //Formatting the localdate ==> custom string format (Month name dd, yyyy)
        String startdate_formatted = startdate_this.format(formatter);
        String enddate_formatted = enddate_this.format(formatter);
        tv_customDates.setText(startdate_formatted + " ~ " + enddate_formatted);
    }

    public void set_total() {
        //Updates the total at the top
        text = "-$" + String.format("%.2f",amount_total);
        tv_total.setText(text);
    }


    ArrayList<String> holder;ArrayList<String> holder2;
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void setArrayLists_categories_and_paymentTypes_InRange(LocalDate startdate, LocalDate enddate){
        categories_inRange = new ArrayList<>();
        paymentTypes_inRange = new ArrayList<>();
        holder = new ArrayList<>();
        holder2 = new ArrayList<>();
        res = myDb.getDataDateRange(startdate.minusDays(1),enddate);
        while(res.moveToNext()){
            holder.add(res.getString(3));
            holder2.add(res.getString(6));
        }
        for (int i = 0; i < holder.size(); i++){
            if(!categories_inRange.contains(holder.get(i))){
                categories_inRange.add(holder.get(i));
            }
        }
        for(int i = 0; i < holder2.size(); i++){
            if(!paymentTypes_inRange.contains(holder2.get(i))){
                paymentTypes_inRange.add(holder2.get(i));
            }
        }


    }



    public void onClick_selectDates() {
        //Button to go back to settings
        btn_selectDates.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //creates a second alert dialog
                AlertDialog.Builder adb2 = new AlertDialog.Builder(view.getContext());
                //creates edit texts in the second alert dialog
                final LayoutInflater inflater = getLayoutInflater();
                final View view_alertButtons = inflater.inflate(R.layout.alert_buttons_layout, null);

                final Button btn_currentCycle = view_alertButtons.findViewById(R.id.btn1);
                final Button btn_cycles = view_alertButtons.findViewById(R.id.btn2);
                final Button btn_customDates = view_alertButtons.findViewById(R.id.btn3);

                btn_cycles.setText("Cycles");
                btn_customDates.setText("Custom Dates");
                btn_currentCycle.setText("Current Cycle");

                adb2.setTitle("Select Date Range:");

                adb2.setView(view_alertButtons); //shows the edit texts from the xml file in the alert dialog
                adb2.setNeutralButton("Back", null);

                //creates the onShow function that keeps the alert dialog open unless specifically called to close
                final AlertDialog alertDialog = adb2.create();

                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(final DialogInterface dialog) {

                        //current cycle button
                        btn_currentCycle.setOnClickListener(new View.OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            public void onClick(View v) {
                                res3 = myDb.get_setting();
                                res3.moveToFirst();
                                startdate_this = LocalDate.parse(res3.getString(0));
                                enddate_this = LocalDate.parse(res3.getString(1));
                                setArrayLists_categories_and_paymentTypes_InRange(startdate_this,enddate_this);
                                ArrayList<Cursor> arr = new ArrayList<>();
                                arr.add(myDb.getDataDateRange(startdate_this,enddate_this));
                                build_List(arr);
                                set_listview();
                                set_total();
                                alertDialog.dismiss();
                            }
                        });

//                        //cycles button
                        btn_cycles.setOnClickListener(new View.OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            public void onClick(View v) {
                                final ArrayList<String> cycles_startToEnd = new ArrayList<>();

                                LayoutInflater inflater = getLayoutInflater();
                                final View view_alertSpinner = inflater.inflate(R.layout.alert_spinner, null);
                                spinner_cycles = view_alertSpinner.findViewById(R.id.the_spinner);
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
                                Collections.reverse(cycles_startToEnd);
                                ArrayAdapter<String> spn_cyc_adapter = new ArrayAdapter<String>(view.getContext(), R.layout.spinner_text2,cycles_startToEnd);
                                spinner_cycles.setAdapter(spn_cyc_adapter);


                                //What the spinner does when item is selected / not selected
                                spinner_cycles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @RequiresApi(api = Build.VERSION_CODES.O)
                                    @Override
                                    public void onItemSelected(AdapterView<?> adapterView, View v, final int position, long id) {
                                        //this is important bc "cycles"/spinner shows new-->old, but in the database table4, it's indexed old--->new
                                        int inverted_pos = (cycles_startToEnd.size() - 1) - position;
                                        res4.moveToPosition(inverted_pos);
                                        startdate_this = LocalDate.parse(res4.getString(0));
                                        enddate_this = LocalDate.parse(res4.getString(1));
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
                                        //Back button (first alertdialog)
                                        Button positiveButton = alertDialog2.getButton(AlertDialog.BUTTON_POSITIVE);
                                        positiveButton.setOnClickListener(new View.OnClickListener() {
                                            @RequiresApi(api = Build.VERSION_CODES.O)
                                            @Override
                                            public void onClick(View v) {

                                                ArrayList<Cursor> arr = new ArrayList<>();
                                                arr.add(myDb.getDataDateRange(startdate_this,enddate_this));
                                                build_List(arr);
                                                set_total();
                                                set_listview();

                                                dialog.dismiss();
                                                alertDialog.dismiss();


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

//

                        //customdates button
                        btn_customDates.setOnClickListener(new View.OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            public void onClick(View v) {

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
                                                if(temp_startdate!=null){
                                                    DatePickerDialog datePickerDialog = new DatePickerDialog(view.getContext(), mDateSetListener_end,
                                                            temp_startdate.getYear(),
                                                            temp_startdate.getMonthValue()-1,
                                                            temp_startdate.getDayOfMonth());
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
                                                temp_startdate = LocalDate.parse(date);
                                                btn_setStartDate.setText(temp_startdate.format(formatter));

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
                                                temp_enddate = LocalDate.parse(date);
                                                btn_setEndDate.setText(temp_enddate.format(formatter));
                                            }
                                        };

                                        //set button (first alertdialog)
                                        Button positiveButton = alertDialog3.getButton(AlertDialog.BUTTON_POSITIVE);
                                        positiveButton.setOnClickListener(new View.OnClickListener() {
                                            @RequiresApi(api = Build.VERSION_CODES.O)
                                            @Override
                                            public void onClick(View v) {

                                                if (temp_startdate==null && temp_enddate==null){
                                                    Toast.makeText(view.getContext(), "Select dates", Toast.LENGTH_SHORT).show();
                                                }
                                                else if(temp_startdate==null){
                                                    Toast.makeText(view.getContext(), "Select start date", Toast.LENGTH_SHORT).show();
                                                }
                                                else if(temp_enddate==null){
                                                    Toast.makeText(view.getContext(), "Select end date", Toast.LENGTH_SHORT).show();
                                                }
                                                else if (temp_startdate.isAfter(temp_enddate)){
                                                    AlertDialog.Builder adb = new AlertDialog.Builder(view.getContext());
                                                    adb.setTitle("The start date is after the end date");
                                                    btn_setEndDate.setText("End Date");
                                                    adb.setPositiveButton("Okay", null);
                                                    adb.show();
                                                }
                                                else{
                                                    startdate_this = temp_startdate;
                                                    enddate_this = temp_enddate;

                                                    ArrayList<Cursor> arr = new ArrayList<>();
                                                    arr.add(myDb.getDataDateRange(startdate_this,enddate_this));
                                                    build_List(arr);
                                                    set_total();
                                                    set_listview();

                                                    dialog.dismiss();
                                                    alertDialog.dismiss();

                                                }

                                            }
                                        });

                                    }
                                });
                                alertDialog3.show();

                            }

                        });


                        //Back button (first alertdialog)
                        Button neutralButton = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                        neutralButton.setOnClickListener(new View.OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void onClick(View v) {

                                dialog.dismiss();

                            }
                        });
                    }
                });

                alertDialog.show();

            }
        });

    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onClick_chooseToShow () {
        //Button to show options
        btn_chooseToShow.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setArrayLists_categories_and_paymentTypes_InRange(startdate_this,enddate_this);

                //creates a second alert dialog
                AlertDialog.Builder adb2 = new AlertDialog.Builder(view.getContext());
                //creates edit texts in the second alert dialog
                LayoutInflater inflater = getLayoutInflater();
                final View view_alertButtons = inflater.inflate(R.layout.alert_buttons_layout, null);

                btn_categories = view_alertButtons.findViewById(R.id.btn1);
                btn_paymentTypes = view_alertButtons.findViewById(R.id.btn2);
                btn_all = view_alertButtons.findViewById(R.id.btn3);

                adb2.setTitle("Choose to show:");
//                adb2.setMessage("Edit your entry");

                adb2.setView(view_alertButtons); //shows the edit texts from the xml file in the alert dialog
                adb2.setNeutralButton("Back", null);

                //creates the onShow function that keeps the alert dialog open unless specifically called to close
                final AlertDialog alertDialog = adb2.create();

                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(final DialogInterface dialog) {

                        //categories button
                        btn_categories.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                //creates boolean array of falses
                                if(categories_inRange.size() > 0){
                                    final boolean[] bool_list = new boolean[categories_inRange.size()];
                                    //converts categories arraylist to char sequence array
                                    final CharSequence[] categories_inRange_list = new CharSequence[categories_inRange.size()];
                                    for (int i = 0; i < categories_inRange.size(); i++) {
                                        categories_inRange_list[i] = categories_inRange.get(i);
                                    }

                                    AlertDialog.Builder builder1 = new AlertDialog.Builder(view.getContext());
                                    builder1.setTitle("Select Category(s) to show");
                                    builder1.setPositiveButton("Select", null);
                                    builder1.setNeutralButton("Back", null);

                                    //set the checkbox listview
                                    builder1.setMultiChoiceItems(categories_inRange_list, bool_list,
                                            new DialogInterface.OnMultiChoiceClickListener() {
                                                @Override
                                                // indexSelected contains the index of item (of which checkbox checked)
                                                //checks and unchecks the boxes when clicked
                                                public void onClick(DialogInterface dialog, int indexSelected, boolean isChecked) {
                                                    bool_list[indexSelected] = isChecked;
                                                    String current_item = categories_inRange_list[indexSelected].toString();
                                                }
                                            });
                                    //creates the alert dialog from the builder
                                    final AlertDialog alertDialog1  = builder1.create();

                                    alertDialog1.setOnShowListener(new DialogInterface.OnShowListener() {
                                        @Override
                                        public void onShow(final DialogInterface dialog) {
                                            //select button
                                            Button positiveButton = alertDialog1.getButton(AlertDialog.BUTTON_POSITIVE);
                                            positiveButton.setOnClickListener(new View.OnClickListener() {
                                                @RequiresApi(api = Build.VERSION_CODES.O)
                                                @Override
                                                public void onClick(View v) {
                                                    ArrayList<Cursor> res_arraylist = new ArrayList<>();
                                                    for(int i = 0; i < bool_list.length; i++){
                                                        boolean checked = bool_list[i];
                                                        if(checked){
                                                            res_arraylist.add(myDb.getDataByCategory(startdate_this.minusDays(1),enddate_this,categories_inRange.get(i))); // (startdate,enddate]
                                                        }
                                                    }
                                                    build_List(res_arraylist);
                                                    set_listview();
                                                    set_total();

                                                    dialog.dismiss();
                                                    alertDialog.dismiss();
                                                }
                                            });
                                            //Back button
                                            Button neutralButton = alertDialog1.getButton(AlertDialog.BUTTON_NEUTRAL);
                                            neutralButton.setOnClickListener(new View.OnClickListener() {
                                                @RequiresApi(api = Build.VERSION_CODES.O)
                                                @Override
                                                public void onClick(View v) {

                                                    dialog.dismiss();
                                                }
                                            });
                                        }
                                    });
                                    alertDialog1.show();
                                }
                                else{//if no entries
                                    AlertDialog.Builder adb = new AlertDialog.Builder(view.getContext());
                                    adb.setTitle("There are no entries in this date range");
                                    adb.setPositiveButton("Okay", null);
                                    adb.show();

                                }
                            }
                        });

//                        //payment Type button
                        btn_paymentTypes.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                //creates boolean array of falses
                                if(paymentTypes_inRange.size() > 0){
                                    final boolean[] bool_list = new boolean[paymentTypes_inRange.size()];
                                    //converts categories arraylist to char sequence array
                                    final CharSequence[] paymentTypes_inRange_list = new CharSequence[paymentTypes_inRange.size()];
                                    for (int i = 0; i < paymentTypes_inRange.size(); i++) {
                                        paymentTypes_inRange_list[i] = paymentTypes_inRange.get(i);
                                    }

                                    AlertDialog.Builder builder1 = new AlertDialog.Builder(view.getContext());
                                    builder1.setTitle("Select Payment Type(s) to show");
                                    builder1.setPositiveButton("Select", null);
                                    builder1.setNeutralButton("Back", null);

                                    //set the checkbox listview
                                    builder1.setMultiChoiceItems(paymentTypes_inRange_list, bool_list,
                                            new DialogInterface.OnMultiChoiceClickListener() {
                                                @Override
                                                // indexSelected contains the index of item (of which checkbox checked)
                                                //checks and unchecks the boxes when clicked
                                                public void onClick(DialogInterface dialog, int indexSelected, boolean isChecked) {
                                                    bool_list[indexSelected] = isChecked;
                                                    String current_item = paymentTypes_inRange_list[indexSelected].toString();
                                                }
                                            });
                                    //creates the alert dialog from the builder
                                    final AlertDialog alertDialog1  = builder1.create();

                                    alertDialog1.setOnShowListener(new DialogInterface.OnShowListener() {
                                        @Override
                                        public void onShow(final DialogInterface dialog) {
                                            //select button
                                            Button positiveButton = alertDialog1.getButton(AlertDialog.BUTTON_POSITIVE);
                                            positiveButton.setOnClickListener(new View.OnClickListener() {
                                                @RequiresApi(api = Build.VERSION_CODES.O)
                                                @Override
                                                public void onClick(View v) {

                                                    ArrayList<Cursor> res_arraylist = new ArrayList<>();
                                                    for(int i = 0; i < bool_list.length; i++){
                                                        boolean checked = bool_list[i];
                                                        if(checked){
                                                            res_arraylist.add(myDb.getDataByPaymentType(startdate_this.minusDays(1),enddate_this,paymentTypes_inRange.get(i))); // (startdate,enddate]
                                                        }
                                                    }
                                                    build_List(res_arraylist);
                                                    set_listview();
                                                    set_total();

                                                    dialog.dismiss();
                                                    alertDialog.dismiss();
                                                }
                                            });
                                            //Back button
                                            Button neutralButton = alertDialog1.getButton(AlertDialog.BUTTON_NEUTRAL);
                                            neutralButton.setOnClickListener(new View.OnClickListener() {
                                                @RequiresApi(api = Build.VERSION_CODES.O)
                                                @Override
                                                public void onClick(View v) {

                                                    dialog.dismiss();
                                                }
                                            });
                                        }
                                    });
                                    alertDialog1.show();
                                }
                                else{//if no entries
                                    AlertDialog.Builder adb = new AlertDialog.Builder(view.getContext());
                                    adb.setTitle("There are no entries in this date range");
                                    adb.setPositiveButton("Okay", null);
                                    adb.show();

                                }
                            }
                        });

//
                        //all button
                        btn_all.setOnClickListener(new View.OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            public void onClick(View v) {
                                res = myDb.getDataDateRange(startdate_this,enddate_this);
                                ArrayList<Cursor> startup_res_arraylist = new ArrayList<>();
                                startup_res_arraylist.add(res);

                                build_List(startup_res_arraylist);
                                set_listview();
                                set_total();

                                dialog.dismiss();
                            }

                        });


                        //Back button (first alertdialog)
                        Button neutralButton = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                        neutralButton.setOnClickListener(new View.OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void onClick(View v) {

                                dialog.dismiss();

                            }
                        });
                    }
                });

                alertDialog.show();

            }
        });


    }



    public void onClick_GoBackBtn () {
        //Button to go back to settings
        btn1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ChartFragment frag= new ChartFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, frag, "piechartFrag")
                        .commit();
            }
        });
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {


    }
}
