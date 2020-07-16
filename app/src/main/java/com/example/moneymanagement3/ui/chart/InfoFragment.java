package com.example.moneymanagement3.ui.chart;

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
import com.example.moneymanagement3.ui.setting.ManageCatFragment;
import com.example.moneymanagement3.ui.tracker.Entry;
import com.example.moneymanagement3.ui.tracker.EntryListAdapter;
import com.example.moneymanagement3.ui.tracker.TrackerFragment;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;

public class InfoFragment extends Fragment {
    View view;
    DataBaseHelper myDb;
    Button btn1;
    Cursor res3; Cursor res2; Cursor res4; Cursor res_byCategory; Cursor res;
    //cycle updater variables
    LocalDate startdate; LocalDate startdate_this;
    LocalDate enddate; LocalDate enddate_this;
    LocalDate currentDate;
    String cycle_input;
    ListView lv;
    TextView tv_total; TextView tv_customDates;
    String text;
    /////
    ArrayList<Entry> entries_arraylist; ArrayList<String> categories_inRange; ArrayList<String> paymentTypes_inRange;
    EntryListAdapter entries_adapter;
    double amount_total = 0;
    DateTimeFormatter formatter;
    Button btn_chooseToShow; Button btn_selectDates; Button btn_categories; Button btn_paymentTypes; Button btn_all;

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

            }
        });
    }


    public void onClick_chooseToShow () {
        //Button to show options
        btn_chooseToShow.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

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
                adb2.setNeutralButton("CANCEL", null);

                //creates the onShow function that keeps the alert dialog open unless specifically called to close
                final AlertDialog alertDialog = adb2.create();

                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(final DialogInterface dialog) {

                        //categories button
                        btn_categories.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                //creates boolean array of falses
                                if(categories_inRange!=null){
                                    final boolean[] bool_list = new boolean[categories_inRange.size()];
                                    //converts categories arraylist to char sequence array
                                    final CharSequence[] categories_inRange_list = new CharSequence[categories_inRange.size()];
                                    for (int i = 0; i < categories_inRange.size(); i++) {
                                        categories_inRange_list[i] = categories_inRange.get(i);
                                    }

                                    AlertDialog.Builder builder1 = new AlertDialog.Builder(view.getContext());
                                    builder1.setTitle("Select Category(s) to show");
                                    builder1.setPositiveButton("Select", null);
                                    builder1.setNeutralButton("Cancel", null);

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
                                            //cancel button
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
                            }
                        });

//                        //payment Type button
                        btn_paymentTypes.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                //creates boolean array of falses
                                if(paymentTypes_inRange!=null){
                                    final boolean[] bool_list = new boolean[paymentTypes_inRange.size()];
                                    //converts categories arraylist to char sequence array
                                    final CharSequence[] paymentTypes_inRange_list = new CharSequence[paymentTypes_inRange.size()];
                                    for (int i = 0; i < paymentTypes_inRange.size(); i++) {
                                        paymentTypes_inRange_list[i] = paymentTypes_inRange.get(i);
                                    }

                                    AlertDialog.Builder builder1 = new AlertDialog.Builder(view.getContext());
                                    builder1.setTitle("Select Payment Type(s) to show");
                                    builder1.setPositiveButton("Select", null);
                                    builder1.setNeutralButton("Cancel", null);

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
                                            //cancel button
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


                        //cancel button (first alertdialog)
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





//
//    @RequiresApi(api = Build.VERSION_CODES.O)
//    //updates the start and end date of the cycle
//    public void cycle_updater() {
//
//        cycle_input = "01"; //sets the default cycle input as the first of the month
//        currentDate = LocalDate.now();
//
//        if (res3 != null && res3.moveToFirst()) {  //makes sure table3 is not null
//            cycle_input = res3.getString(2);
//        } else {
//            myDb.create_filler_setting_onStartup(cycle_input);
//        }
//
//        String currentDate_string = String.valueOf(currentDate);
//        String currentMonth_string = "" + currentDate_string.substring(5, 7); //"MM" -- [start ind,end ind)
//
//        String var_string = "" + currentDate_string.substring(0, 5) + currentMonth_string + "-" + cycle_input; //variable to compare current date with
//        LocalDate var = LocalDate.parse(var_string);    //convert var into a localdate
//
//        //determine and sets the start and end dates of the cycle
//        if (currentDate.isBefore(var)) {
//            LocalDate var_new = var.plusMonths(-1);
//            startdate = var_new;
//            enddate = var.minusDays(1);
//            //update database table3
//            myDb.update_cycle_setting(String.valueOf(startdate), String.valueOf(enddate), cycle_input);
//        } else {
//            LocalDate var_new = var.plusMonths(1);
//            startdate = var;
//            enddate = var_new.minusDays(1);
//            //update database table3
//            myDb.update_cycle_setting(String.valueOf(startdate), String.valueOf(enddate), cycle_input);
//        }
//
//
//
//        //******************************************************************************new added
//
//
//        //dealing with table4 (cycle table) ---- for cycle spinner
//        res4 = myDb.get_cycles();
//        if (res4!=null && res4.moveToLast()) { //if table4 is not null on startup (run basically every time this fragment is selected)
//            String past_startdate = res4.getString(0);
//            String past_enddate = res4.getString(1);
//
//            if (!past_startdate.equals(String.valueOf(startdate)) && !past_enddate.equals(String.valueOf(enddate))) { //if a new cycle started (new month)
//                res4.moveToLast();
//                String cycle_budget = res4.getString(2);
//                String categories_list_as_string = res4.getString(3);
//                String categories_budget_list_as_string = res4.getString(4);
//                //inserts the start and end date of the cycle only if the dates changed
//                myDb.insert_new_cycle(String.valueOf(startdate), String.valueOf(enddate), cycle_budget,
//                        categories_list_as_string, categories_budget_list_as_string);
//            }
//        }
//
//        else { //if table4 null (only when first run)
//
//            StringBuilder categories_budget_list_as_string = new StringBuilder();
//            StringBuilder categories_list_as_string = new StringBuilder();
//            res2 = myDb.getAllData_categories();
//            if (res2 != null) { // if categories table3 is not empty
//                while (res2.moveToNext()) {
//                    String category = res2.getString(1);
//                    categories_list_as_string.append(category).append(";");
//                    categories_budget_list_as_string.append("0.00").append(";");
//                }
//            }
//            //inserts the start and end date of the cycle only if the dates changed
//            myDb.insert_new_cycle(String.valueOf(startdate), String.valueOf(enddate), "0.00",
//                    categories_list_as_string.toString(), categories_budget_list_as_string.toString());
//
//
//        }
//
//        //******************************************************************************new added
//
//    }


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
}
