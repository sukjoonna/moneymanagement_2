package com.example.moneymanagement3.ui.tracker;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;


import com.example.moneymanagement3.DataBaseHelper;
import com.example.moneymanagement3.R;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;


public class TrackerFragment extends Fragment {

    View view;
    DataBaseHelper myDb;
    Cursor res; Cursor res2; Cursor res3; Cursor res4;
    TextView tv_total; TextView tv_today; TextView tv_notice;
    ListView lv;
    ArrayList<String> arrayList; ArrayList<String> cycles; ArrayList<String> all_cycles;
    ArrayAdapter<String> adapter; ArrayAdapter<String> spn_cyc_adapter;
    ArrayList<Entry> entries_arraylist;
    EntryListAdapter entries_adapter;
    String text;
    double amount_total;
    String category; String cycle_input; String new_date; String selected_payment_type;
    LocalDate startdate; LocalDate enddate; LocalDate currentDate;
    Spinner spinner_cycles;
    DateTimeFormatter formatter;
    DatePickerDialog.OnDateSetListener mDateSetListener;
    String[] payment_types;
    int count;

    @RequiresApi(api = Build.VERSION_CODES.O) // this might need to be change to use a different package
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_tracker, container, false);

        //variable declarations
        lv = view.findViewById(R.id.listView);
        tv_total = view.findViewById(R.id.totalTv);
        tv_today = view.findViewById(R.id.todayTv);
        tv_notice = view.findViewById(R.id.noticeTv);
        spinner_cycles = view.findViewById(R.id.cycleSpn);
        text = "";
        amount_total = (double) 0;
        selected_payment_type = "";

        //get database
        myDb = new DataBaseHelper(getActivity());

        //Cursor res2 res3 = gets all data in the database table2 and table3
        res2 = myDb.getAllData_categories();


        //------------------------CYCLE CREATE AND UPDATER in DB (ALONG WITH SPINNER) -------------------------//                   *Make sure this is at top

        //Cycle updater
        cycle_updater();

        res3 = myDb.get_setting();
        res3.moveToFirst();

        String num_of_cycles = res3.getString(3);
        if(num_of_cycles.equals("All")){
            num_of_cycles = "1000000";
        }
        int count = 0;


        spinner_cycles = view.findViewById(R.id.cycleSpn);

        //Create Cycle Spinner --- from table4
        cycles = new ArrayList<String>();
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
            cycles.add(formatted_dates);
        }

        if(cycles.size() > Integer.parseInt(num_of_cycles)){
            while(cycles.size() > Integer.parseInt(num_of_cycles)){
                cycles.remove(0);
            }
        }
        Collections.reverse(cycles);
        ArrayAdapter<String> spn_cyc_adapter = new ArrayAdapter<String>(view.getContext(), R.layout.spinner_text,cycles);
        spinner_cycles.setAdapter(spn_cyc_adapter);

        //------------------------------------------------END-----------------------------------------------//



        //Set current date Tv
        formatter = DateTimeFormatter.ofPattern("LLLL dd, yyyy");
        tv_today.setText("Today\n" + currentDate.format(formatter));

        build_List(); //builds listview
        set_total(); //set total amount


        //calls function when entry/item is selected from Listview
        onClick_itemselectedLv();

        //calls onselect cycle spinner
        onSelect_CycleSpinner();



        return view;
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //Public functions



    @RequiresApi(api = Build.VERSION_CODES.O)
    public void build_List() {

        //Get data from database table1
        LocalDate startdate_selected = startdate;
        res = myDb.getDataDateRange(startdate_selected.minusDays(1),enddate); //

        amount_total = 0;
        if(res!=null){

            entries_arraylist = new ArrayList<Entry>();
            entries_adapter = new EntryListAdapter(view.getContext(),R.layout.adapter_view_layout,entries_arraylist);


            //takes the values out from database and puts it into the arraylist &&&& calculates total amount
            while (res.moveToNext()) {
                String dscpt = res.getString(1);
                String amt = "-$" + res.getString(2);
                String cat = res.getString(3);
                LocalDate date = LocalDate.parse(res.getString(4));
                String payment = res.getString(6);

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
                double amount = Double.parseDouble(res.getString(2));
                amount_total += amount;


            }

            //puts the arraylist into the listview
            lv.setAdapter(entries_adapter);
            entries_adapter.notifyDataSetChanged();
        }


    }

    public void set_total() {
        if(amount_total==0){
            tv_notice.setText("There are no entries");
        }
        else{
            tv_notice.setText("");
        }
        //Updates the total at the top
        text = "-$" + String.format("%.2f",amount_total);
        tv_total.setText(text);


    }


    public void onSelect_CycleSpinner() {

        //What the spinner does when item is selected / not selected
        spinner_cycles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View v, final int position, long id) {

                //this is important bc "cycles"/spinner shows new-->old, but in the database table4, it's indexed old--->new
                int inverted_pos = (cycles.size() - 1) - position;

                res4.moveToPosition(inverted_pos);
                startdate = LocalDate.parse(res4.getString(0));
                enddate = LocalDate.parse(res4.getString(1));

                build_List(); //builds arraylist to pass into listview
                set_total(); //set total amount

                //calls function when entry/item is selected from Listview
                onClick_itemselectedLv();


            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Object item = adapterView.getItemAtPosition(0);

            }
        });


    }




    public void onClick_itemselectedLv() {
        //Delete/edit selected items in the listview by selecting an item in the list view
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> a, View v, final int position, long id) {

                //create an alert dialog1
                AlertDialog.Builder adb=new AlertDialog.Builder(view.getContext());
                adb.setTitle("Notice");
                adb.setMessage("What would you like to do with this entry?");
                final int position_ind = position;

                //cancel button
                adb.setNeutralButton("Cancel", null);

                //Delete button
                adb.setPositiveButton("Delete", new AlertDialog.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    public void onClick(DialogInterface dialog, int which) {

                        //Deletes the applicable row in database
                        int deletedRow = 0;
                        res.moveToPosition(position_ind);
                        String db_id = res.getString(0);
                        deletedRow = myDb.deleteData(db_id);
                        //makes a toast
                        if(deletedRow > 0)
                            Toast.makeText(view.getContext(),"Entry Deleted",Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(view.getContext(),"Entry not Deleted",Toast.LENGTH_SHORT).show();

                        build_List();
                        set_total(); //set total amount
//                        //recreates TrackerFragement to update all changes
//                        getFragmentManager()
//                                .beginTransaction()
//                                .detach(TrackerFragment.this)
//                                .attach(TrackerFragment.this)
//                                .commit();
                    }
                });

                //edit button
                adb.setNegativeButton("Edit", new AlertDialog.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    public void onClick(DialogInterface dialog, int which) {

                        //creates a second alert dialog
                        AlertDialog.Builder adb2 = new AlertDialog.Builder(view.getContext());

                        //creates edit texts in the second alert dialog
                        LayoutInflater inflater = getLayoutInflater();
                        final View view = inflater.inflate(R.layout.edit_inputs_layout,null); //xml file used
                        final EditText et1 = view.findViewById(R.id.edit_amountEt);
                        final EditText et2 = view.findViewById(R.id.edit_textEt);
                        final TextView tv3 = view.findViewById(R.id.edit_dateTv);
                        final TextView tv4 = view.findViewById(R.id.edit_paymentTv);

//                      //sets default values in these edit texts as the values previously inputted
                        res.moveToPosition(position_ind);
                        et1.setText(res.getString(2));
                        et2.setText(res.getString(1));
                        String entry_category = res.getString(3);

                        String abc = res.getString(6);

                        if (res.getString(6).equals("None")){
                            tv4.setText("Payment Type");
                        }
                        else {
                            tv4.setText(res.getString(6));
                        }


                        final LocalDate date_ld = LocalDate.parse(res.getString(4));
                        tv3.setText(date_ld.format(formatter));
                        new_date = String.valueOf(date_ld);

                        //Creating the categories spinner (from xml spinner of categories) in second alert dialog (code copied from MainActivity)
                        ArrayList<String> categories = new ArrayList<String>();
                        while (res2.moveToNext()) {
                            String category = res2.getString(1); //from database table2
                            categories.add(category);
                        }
                        final Spinner spn1 = view.findViewById(R.id.edit_spinner);
                        ArrayAdapter<String> spn_adapter = new ArrayAdapter<String>(view.getContext(),
                                R.layout.spinner_text,categories);
                        spn1.setAdapter(spn_adapter);
                        spn_adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

                        //Sets default spinner location to previous entry
                        int spn1_ind = categories.indexOf(entry_category);
                        spn1.setSelection(spn1_ind);


                        //datepicker for edit date textview
                        tv3.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Calendar cal = Calendar.getInstance();
                                java.util.Date date_date = java.sql.Date.valueOf(String.valueOf(date_ld));
                                cal.setTime(date_date);
                                int year = cal.get(Calendar.YEAR);
                                int month = cal.get(Calendar.MONTH);
                                int day = cal.get(Calendar.DAY_OF_MONTH);


                                DatePickerDialog dialog = new DatePickerDialog(
                                        view.getContext(),
                                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                                        mDateSetListener,
                                        year,month,day);
                                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                dialog.show();
                            }
                        });

                        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
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
                                LocalDate date_ld = LocalDate.parse(date);
                                tv3.setText(date_ld.format(formatter));
                                new_date = date;
                            }
                        };



                        //What the spinner does when item is selected / not selected
                        spn1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View v, int position, long id) {
                                Object item = adapterView.getItemAtPosition(position);
                                category = item.toString();
                            }
                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {
                                Object item = adapterView.getItemAtPosition(0);
                                category = item.toString();
                            }
                        });


                        //Payment type
                        selected_payment_type = res.getString(6);
                        payment_types = new String[] {"Cash", "Credit", "Debit", "Check"};
                        tv4.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(final View view) {


                                final AlertDialog.Builder alt_bld = new AlertDialog.Builder(view.getContext());

                                //alt_bld.setIcon(R.drawable.icon);
                                alt_bld.setTitle("Select Payment Type");
                                alt_bld.setSingleChoiceItems(payment_types, -1, new DialogInterface
                                        .OnClickListener() {
                                    public void onClick(DialogInterface dialog, int item) {
                                        selected_payment_type = payment_types[item];
                                    }

                                });
                                alt_bld.setPositiveButton("Okay", new AlertDialog.OnClickListener() {
                                    @RequiresApi(api = Build.VERSION_CODES.O)
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (selected_payment_type.equals("None")){
                                            alt_bld.setSingleChoiceItems(payment_types,-1,null);
                                            tv4.setText("Payment Type");
                                        }
                                        else{
                                            tv4.setText(selected_payment_type);
                                        }
                                        dialog.dismiss();// dismiss the alertbox after chose option
                                    }
                                });
                                AlertDialog alert = alt_bld.create();
                                alert.show();
                            }
                        });




                        adb2.setTitle("Edit");
//                        adb2.setMessage("Edit your entry");
                        adb2.setView(view); //shows the edit texts from the xml file in the alert dialog
                        adb2.setPositiveButton("UPDATE", null);
                        adb2.setNeutralButton("CANCEL", null);

                        //creates the onShow function that keeps the alert dialog open unless specifically called to close
                        final AlertDialog alertDialog = adb2.create();
                        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(final DialogInterface dialog) {

                                //update button
                                Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                                positiveButton.setOnClickListener(new View.OnClickListener() {
                                    @RequiresApi(api = Build.VERSION_CODES.O)
                                    @Override
                                    public void onClick(View v) {
                                        res.moveToPosition(position_ind);
                                        String entry_id = res.getString(0);
                                        String edited_amount = et1.getText().toString();
                                        String edited_text = et2.getText().toString();
                                        String edited_category = category;
                                        String edited_date = new_date;
                                        LocalDate text5 = LocalDate.parse(edited_date); //in format yyyy-mm-dd
                                        String edited_payment_type = selected_payment_type;

//                                        if (edited_payment_type.equals("")){
//                                            edited_payment_type = "None";
//                                        }

                                        int decimal_places = 0;
                                        //divide the amount string at "." to get the number of decimal places if there is a "."
                                        if (edited_amount.contains(".")) {
                                            String[] divided_parts = edited_amount.split("\\.");
                                            decimal_places = divided_parts[1].length();
                                        }


                                        if (edited_amount.equals("")) {
                                            Toast.makeText(view.getContext(),"Enter Amount",Toast.LENGTH_SHORT).show();
                                        }
                                        else if (decimal_places > 2){
                                            //create an alert dialog
                                            AlertDialog.Builder adb3 =new AlertDialog.Builder(view.getContext());
                                            adb3.setTitle("Notice");
                                            adb3.setMessage("Too many decimal places");
                                            adb3.setNeutralButton("Okay", null);
                                            adb3.show();
                                        }
                                        else {
                                            if (edited_text.equals("") ) {
                                                edited_text=edited_category;
                                            }

                                            //formats the "edited_amount" to two decimal places
                                            float amount_float = Float.parseFloat(edited_amount);
                                            DecimalFormat df = new DecimalFormat("0.00");
                                            String edited_amount_formatted = df.format(amount_float);


                                            //update data in database
                                            boolean wasUpdated = myDb.updateData(entry_id,edited_text,edited_amount_formatted,edited_category,edited_date,text5,edited_payment_type);


                                            build_List();
                                            set_total(); //set total amount

                                            //makes a toast to check if data was updated
                                            if(wasUpdated == Boolean.TRUE)
                                                Toast.makeText(view.getContext(),"Entry Updated",Toast.LENGTH_SHORT).show();
                                            else
                                                Toast.makeText(view.getContext(),"Entry not Updated",Toast.LENGTH_SHORT).show();

                                             //recreates TrackerFragement to update all changes
                                            getFragmentManager()
                                                    .beginTransaction()
                                                    .detach(TrackerFragment.this)
                                                    .attach(TrackerFragment.this)
                                                    .commit();

                                            dialog.dismiss();

                                        }

                                    }
                                });

                                //cancel button
                                Button neutralButton = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                                neutralButton.setOnClickListener(new View.OnClickListener() {
                                    @RequiresApi(api = Build.VERSION_CODES.O)
                                    @Override
                                    public void onClick(View v) {

                                        build_List();
                                        set_total(); //set total amount
                                        //recreates TrackerFragement to update all changes --mainly for categories spinner to be recreated each time
                                        getFragmentManager()
                                                .beginTransaction()
                                                .detach(TrackerFragment.this)
                                                .attach(TrackerFragment.this)
                                                .commit();

                                        dialog.dismiss();
                                    }
                                });
                            }
                        });

                        alertDialog.show();
                        alertDialog.setCanceledOnTouchOutside(false);

                    }
                });
                adb.show();
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

