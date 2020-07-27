package com.example.moneymanagement3.ui.home;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class HomeFragment extends Fragment {

    //Class - Variable creations
    DataBaseHelper myDb;
    EditText et_name, et_amount;
    Button btn_add;
    ArrayList<String> categories;
    ArrayAdapter<String> spn_adapter;
    Spinner spn_category; TextView tv_payment;
    String category; String selected_payment_type;
    LocalDate currentDate;
    Cursor res2;
    Cursor res3;
    Cursor res4;
    View view;
    LocalDate startdate;
    LocalDate enddate;
    String cycle_input; String new_category;
    boolean category_added = false;
    String[] payment_types;


    @RequiresApi(api = Build.VERSION_CODES.O)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);
        et_name = view.findViewById(R.id.textEt);     //edit text
        et_amount = view.findViewById(R.id.amountEt);
        btn_add = view.findViewById(R.id.add1Btn);   //add button
        spn_category = view.findViewById(R.id.categorySpn);
        tv_payment = view.findViewById(R.id.paymentTv);

        //creates database
        myDb = new DataBaseHelper(getActivity());
        //Cursor res = gets all data in the database table2 and table3
        res2 = myDb.getAllData_categories();
        res3 = myDb.get_setting();
        res4 = myDb.get_cycles();

        //updates the cycle
        cycle_updater();

        //FOR DEVELOPER ONLY: DELETE TABLE3 (MISC/SETTING TABLE) IN DATABASE
        //////////////////////////////////
        //                             ///        -uses: for testing startup of app
        //  myDb.deleteAll_setting();  ///        -Deletes start date, end date, and cycle start day
        //                             ///        -Uncomment "myDb.deleteAll_setting()" and comment out "cycle_updater()" above to execute
        //////////////////////////////////


//        CycleUpdaterFragment updater = new CycleUpdaterFragment();
//        updater.cycle_updater();

        //creates the categories ArrayList and adds the categories from the database table2
        categories = new ArrayList<String>();
        categories.add(0, "CATEGORY*");  //this is created as default to prevent bugs when there are no categories
        res2 = myDb.getAllData_categories();
        while (res2.moveToNext()) {
            String category = res2.getString(1);
            categories.add(category);
        }
        categories.add("+Add New");


        //Creating the categories spinner using spinner_of_categories xml file in layout
        spn_adapter = new ArrayAdapter<String>(view.getContext(), R.layout.spinner_text, categories);
        spn_category.setAdapter(spn_adapter);
        spn_adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);


        //Payment type
        payment_types = new String[] {"Cash", "Credit", "Debit", "Check"};

        tv_payment.setText("Payment Type");
        selected_payment_type = "";

        tv_payment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                final AlertDialog.Builder alt_bld = new AlertDialog.Builder(view.getContext());
                //alt_bld.setIcon(R.drawable.icon);
                alt_bld.setTitle("Select Payment Type");
                alt_bld.setSingleChoiceItems(payment_types, -1, new DialogInterface
                        .OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        selected_payment_type = payment_types[item];
                        tv_payment.setText(selected_payment_type);
                        dialog.dismiss();
                    }
                });
                AlertDialog alert = alt_bld.create();
                alert.show();
                alert.setCanceledOnTouchOutside(false);


//                AlertDialog.Builder alt_bld = new AlertDialog.Builder(view.getContext());
//                //alt_bld.setIcon(R.drawable.icon);
//                alt_bld.setTitle("Select Payment Type");
//                alt_bld.setSingleChoiceItems(payment_types, -1, new DialogInterface
//                        .OnClickListener() {
//                    public void onClick(DialogInterface dialog, int item) {
//                        selected_payment_type = payment_types[item];
//                    }
//                });
//                alt_bld.setPositiveButton("Okay", new AlertDialog.OnClickListener() {
//                    @RequiresApi(api = Build.VERSION_CODES.O)
//                    public void onClick(DialogInterface dialog, int which) {
//                        if (selected_payment_type.equals("")){
//                            tv_payment.setText("Payment Type");
//                        }
//                        else{
//                            tv_payment.setText(selected_payment_type);
//                        }
//                        dialog.dismiss();// dismiss the alertbox after chose option
//                    }
//                });
//                AlertDialog alert = alt_bld.create();
//                alert.show();
//                alert.setCanceledOnTouchOutside(false);
            }
        });





        onSelect_Spinner(); //function call for selecting item in spinner_category

        onClick_addBtn();   //function call for clicking the add button

        return view;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public void onClick_addBtn() {
        //Button to add data in database
        btn_add.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void onClick(View v) {

                String text1 = et_name.getText().toString();     //gets text from edit text widget: name
                String text2 = et_amount.getText().toString();  //gets text from edit text widget: amount
                String text3 = category;
                String text4 = String.valueOf(currentDate);
                LocalDate text5 = LocalDate.now();
                String text6 = selected_payment_type;

                int decimal_places = 0;
                //divide the amount string at "." to get the number of decimal places if there is a "."
                if (text2.contains(".")) {
                    String[] divided_parts = text2.split("\\.");
                    decimal_places = divided_parts[1].length();
                }


                //if "+Add New" or "SELECT CATEGORY" is selected for category, display a warning message
                if (category.equals("+Add New") || category.equals("CATEGORY*")) {
                    //create an alert dialog
                    AlertDialog.Builder adb1 = new AlertDialog.Builder(view.getContext());
                    TextView msg1 = new TextView(view.getContext());    //create a message Tv for adb1
                    msg1.setText(R.string.select_category); //the message
                    msg1.setGravity(Gravity.CENTER);    //center
                    msg1.setTextSize(20);
                    msg1.setPadding(10, 5, 10, 20);
                    msg1.setTextColor(Color.GRAY);
                    TextView title1 = new TextView(view.getContext());    //create a message Tv for adb1
                    title1.setText("Notice"); //the title
                    title1.setGravity(Gravity.CENTER);    //center
                    title1.setTextSize(30);
                    title1.setPadding(10, 40, 10, 50);
                    title1.setTextColor(Color.BLACK);
//                    adb1.setTitle("Notice");
                    adb1.setPositiveButton("Okay", null);
                    adb1.setView(msg1);
                    adb1.setCustomTitle(title1);
                    adb1.show();
                }
                //if text2 is empty
                else if (text2.equals("")) {

                    //create an alert dialog
                    AlertDialog.Builder adb1 = new AlertDialog.Builder(view.getContext());
                    TextView msg1 = new TextView(view.getContext());    //create a message Tv for adb1
                    msg1.setText("Enter Amount"); //the message
                    msg1.setGravity(Gravity.CENTER);    //center
                    msg1.setTextSize(20);
                    msg1.setPadding(10, 5, 10, 20);
                    msg1.setTextColor(Color.GRAY);
                    TextView title1 = new TextView(view.getContext());    //create a message Tv for adb1
                    title1.setText("Notice"); //the title
                    title1.setGravity(Gravity.CENTER);    //center
                    title1.setTextSize(30);
                    title1.setPadding(10, 40, 10, 50);
                    title1.setTextColor(Color.BLACK);
//                    adb1.setTitle("Notice");
                    adb1.setPositiveButton("Okay", null);
                    adb1.setView(msg1);
                    adb1.setCustomTitle(title1);
                    adb1.show();

//                    //create an alert dialog
//                    AlertDialog.Builder adb2 = new AlertDialog.Builder(view.getContext());
//                    adb2.setTitle("Notice");
//                    adb2.setMessage("Enter Amount");
//                    adb2.setNeutralButton("Okay", null);
//                    adb2.show();

                }
                else if (decimal_places > 2) {
                    //create an alert dialog
                    AlertDialog.Builder adb3 = new AlertDialog.Builder(view.getContext());
                    adb3.setTitle("Notice");
                    adb3.setMessage("Too many decimal places");
                    adb3.setNeutralButton("Okay", null);
                    adb3.show();
                }
                //otherwise, store all the inputed values into database table1
                else {

                    //formats the "amount" to two decimal places
                    float amount_float = Float.parseFloat(text2);
                    DecimalFormat df = new DecimalFormat("0.00");
                    String text2_formatted = df.format(amount_float);

                    if (text1.equals("")){ //if description is empty, make it the category by default
                        text1 = text3;
                    }
                    if (text6.equals("")){
                        text6 = "None";
                    }

                    boolean isInserted = myDb.insertData(text1, text2_formatted, text3, text4, text5, text6); //insert data into database

                    //set spinner to 0
                    spn_category.setSelection(0);

                    if (isInserted)
                        Toast.makeText(view.getContext(), "Entry Inserted", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(view.getContext(), "Entry not Inserted", Toast.LENGTH_SHORT).show();

                    //clears the edit texts
                    et_name.getText().clear();
                    et_amount.getText().clear();

                    //reset payment type
                    tv_payment.setText("Payment Type");

                }

            }
        });
    }

    public void onSelect_Spinner() {
        //What the spinner does when item is selected / not selected
        spn_category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View v, final int position, long id) {
                final int position_ind = position;
                //stores the selected category into a String var
                Object item = adapterView.getItemAtPosition(position);
                category = item.toString();

                //if new category was added, point to that category
                if (category_added){
                    spn_category.setSelection(categories.size()-2);
                    category_added = false;
                }

                //if "+Add New" is selected
                if (category.equals("+Add New")) {
                    //create an alert dialog
                    AlertDialog.Builder adb = new AlertDialog.Builder(view.getContext());

                    //create an edit text in this alert dialog by linking to add_new_category xml file
                    LayoutInflater inflater = getLayoutInflater();
                    final View view = inflater.inflate(R.layout.add_new_category, null); //xml file used
                    final EditText et_addcategory = view.findViewById(R.id.add_categoryEt);

                    adb.setTitle("Alert");
                    adb.setMessage("Type in a new category");
                    adb.setView(view);
                    //cancel button
                    adb.setNeutralButton("Cancel", new AlertDialog.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //When cancel is pressed, spinner set to 0 so you can selected add new again
                            spn_category.setSelection(0);
                        }
                    });
                    //Add new category button
                    adb.setPositiveButton("Add", new AlertDialog.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            new_category = et_addcategory.getText().toString();
                            //Checks if it is empty or not
                            if (new_category.equals("")) {
                                Toast.makeText(view.getContext(), "Nothing was Added", Toast.LENGTH_SHORT).show();
                                spn_category.setSelection(0);
                            } else {
                                //Adds the new category to spinner
//                                categories.add(position_ind, new_category);
                                categories.add(categories.size()-1,new_category);
                                //Add the new category to database table 3
                                myDb.insertData_categories(new_category);
                                //after new category is added, the spinner is set to 0
                                spn_category.setSelection(0);

//----------------------------------
                                res4 = myDb.get_cycles();
                                StringBuilder categories_budget_list_as_string = new StringBuilder();
                                int categories_budget_list_length = 0;
                                if (res4.moveToLast()){
                                    categories_budget_list_as_string = new StringBuilder(res4.getString(4));
                                }
                                StringBuilder categories_list_as_string = new StringBuilder();
                                String[] categories_budget_list = categories_budget_list_as_string.toString().split("\\;");
                                int categories_list_length = 0;

                                res2 = myDb.getAllData_categories();
                                while (res2.moveToNext()) {
                                    String category = res2.getString(1);
                                    categories_list_as_string.append(category).append(";");
                                    categories_list_length++;
                                }

                                if (!categories_budget_list[0].equals("")){
                                    categories_budget_list_length = categories_budget_list.length;
                                }

                                int list_size_difference = categories_list_length - (categories_budget_list_length);
                                for (int i = 0; i < list_size_difference; i++) {
                                    categories_budget_list_as_string.append("0.00;");
                                }

                                //updates table4
                                myDb.update_cycles_table_Category(String.valueOf(startdate), categories_list_as_string.toString());

                                myDb.update_cycles_table_CatBudget(String.valueOf(startdate), categories_budget_list_as_string.toString());
//----------------------------------
                                category_added = true;

                            }

                        }
                    });
                    adb.setCancelable(false);
                    adb.show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Object item = adapterView.getItemAtPosition(0);
                category = item.toString();
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    //updates the start and end date of the cycle
    public void cycle_updater() {

        cycle_input = "01"; //sets the default cycle input as the first of the month
        currentDate = LocalDate.now(); //get current date

        if (res3 != null && res3.moveToFirst()) {  //makes sure table3 is not null
            cycle_input = res3.getString(2);
        }
        else {
            myDb.create_filler_setting_onStartup(cycle_input);
        }

        String currentDate_string = String.valueOf(currentDate);
        String currentMonth_string = "" + currentDate_string.substring(5, 7); //"MM" -- [start ind,end ind)

        String var_string = "" + currentDate_string.substring(0, 5) + currentMonth_string + "-" + cycle_input; //variable to compare current date with
        LocalDate var = LocalDate.parse(var_string);    //convert var into a localdate

        //determine and sets the start and end dates of the cycle in table3
        if (currentDate.isBefore(var)) {
            LocalDate var_new = var.plusMonths(-1);
            startdate = var_new;
            enddate = var.minusDays(1);
            //update database table3
            myDb.update_cycle_setting(String.valueOf(startdate), String.valueOf(enddate), cycle_input);
        }
        else {
            LocalDate var_new = var.plusMonths(1);
            startdate = var;
            enddate = var_new.minusDays(1);
            //update database table3
            myDb.update_cycle_setting(String.valueOf(startdate), String.valueOf(enddate), cycle_input);
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
                    enddate_temp = enddate_temp.plusMonths(1);
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