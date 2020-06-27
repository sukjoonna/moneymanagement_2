package com.example.moneymanagement3.ui.tracker;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;

import com.example.moneymanagement3.DataBaseHelper;
import com.example.moneymanagement3.R;


import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class TrackerFragment extends Fragment {

    View view;
    DataBaseHelper myDb;
    Cursor res; Cursor res2; Cursor res3;
    TextView tv_total; TextView tv_cycle;
    Button btn;
    ListView lv;
    ArrayList<String> arrayList;
    ArrayAdapter<String> adapter;
    String text;
    double amount_total;
    String category;
    LocalDate startdate; LocalDate enddate;

    @RequiresApi(api = Build.VERSION_CODES.O) // this might need to be change to use a different package
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_tracker, container, false);

        btn = view.findViewById(R.id.resetBtn);
        lv = view.findViewById(R.id.listView);
        tv_total = view.findViewById(R.id.totalTv);
        tv_cycle = view.findViewById(R.id.cycleTv);
        text = "";
        amount_total = (double) 0;

        myDb = new DataBaseHelper(getActivity());

        //Cursor res2 res3 = gets all data in the database table2 and table3
        res2 = myDb.getAllData_categories();
        res3 = myDb.get_setting();
        res3.moveToFirst();

        //get startdate and enddate of cycle and convert to localdate
        startdate = LocalDate.parse(res3.getString(0)); // get startdate of cycle from database table3 as a localdate
        enddate = LocalDate.parse(res3.getString(1)); // get enddate of cycle from database table3 as a localdate

        //Set the cycle textview with the current start and end dates of cycle
        tv_cycle.setText(startdate + "  to  " + enddate);

        //Get data from database table1
        res = myDb.getDataDateRange(startdate,enddate);
//        res = myDb.getAllData();
//        res = myDb.getDataDefault(); //testing to see if the default monthly cycle works


        //creates an arraylist and adapter that will take the arraylist and place its values into a listview
        arrayList = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(view.getContext(),android.R.layout.simple_list_item_1,arrayList);

        build_arrayList(); //builds arraylist to pass into listview
        set_total(); //set total amount

        //puts the arraylist into the listview
        lv.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        //calls function when entry/item is selected from Listview
        onClick_itemselectedLv();

        //calls function to reset the data and listview
        onClick_resetBtn();


        return view;
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //Public functions
    public void build_arrayList() {
        //takes the values out from database and puts it into the arraylist &&&& calculates total amount
        while (res.moveToNext()) {
            arrayList.add("Description: " + res.getString(1) + "\nAmount: $"+ res.getString(2)
                    + "\nCategory: " + res.getString(3) + "\nDate: " + res.getString(4));
            double amount = Double.parseDouble(res.getString(2));
            amount_total += amount;
        }
    }

    public void set_total() {
        //Updates the total at the top
        text = "-$" + String.format("%.2f",amount_total);
        tv_total.setText(text);
    }

    public void onClick_resetBtn () {
        //deletes all data in the list and resets the listview
        btn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            public void onClick(View v) {

                //An alert dialog box pops up to make sure you want to delete/reset everything
                AlertDialog alertDialog = new AlertDialog.Builder(view.getContext()).create();
                alertDialog.setTitle("Alert");
                alertDialog.setMessage("Do you want to reset everything?");
                //Make an "ok" button
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            //OnClick:
                            public void onClick(DialogInterface dialog, int which) {
                                int deletedRows = 0;

                                //deleteAll() returns the number of rows in the database table deleted
                                deletedRows = myDb.deleteAll();

                                //clears the listview
                                adapter.clear();

                                //makes a toast
                                if(deletedRows > 0)
                                    Toast.makeText(view.getContext(),"Deleted all entries",Toast.LENGTH_SHORT).show();
//                                else
//                                    Toast.makeText(SecondActivity.this,"Data not Deleted",Toast.LENGTH_SHORT).show();

                                //set total to 0
                                tv_total.setText("-$0.00");

                                //dismiss dialog
                                dialog.dismiss();
                            }
                        });

                //make a "cancel" button
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
                        new DialogInterface.OnClickListener() {
                            //OnClick:
                            public void onClick(DialogInterface dialog, int which) {
                                //dismiss dialog
                                dialog.dismiss();
                            }
                        });

                alertDialog.show();

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
                    public void onClick(DialogInterface dialog, int which) {
                        //deletes the entry from the listview
                        arrayList.remove(position_ind);
                        adapter.notifyDataSetChanged();

                        //Deletes the applicable row in database
                        int deletedRow = 0;
                        res.moveToPosition(position_ind);
                        String db_id = res.getString(0);
                        deletedRow = myDb.deleteData(db_id);
                        //makes a toast
                        if(deletedRow > 0)
                            Toast.makeText(view.getContext(),"Data Deleted",Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(view.getContext(),"Data not Deleted",Toast.LENGTH_SHORT).show();

                        //recreates TrackerFragement to update all changes
                        getFragmentManager()
                                .beginTransaction()
                                .detach(TrackerFragment.this)
                                .attach(TrackerFragment.this)
                                .commit();
                    }
                });

                //edit button
                adb.setNegativeButton("Edit", new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        //creates a second alert dialog
                        AlertDialog.Builder adb2 = new AlertDialog.Builder(view.getContext());

                        //creates edit texts in the second alert dialog
                        LayoutInflater inflater = getLayoutInflater();
                        final View view = inflater.inflate(R.layout.edit_inputs_layout,null); //xml file used
                        final EditText et1 = view.findViewById(R.id.edit_amountEt);
                        final EditText et2 = view.findViewById(R.id.edit_textEt);
                        final EditText et3 = view.findViewById(R.id.edit_dateEt);
//                      //sets default values in these edit texts as the values previously inputted
                        res.moveToPosition(position_ind);
                        et1.setText(res.getString(2));
                        et2.setText(res.getString(1));
                        et3.setText(res.getString(4));
                        String entry_category = res.getString(3);


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

                        adb2.setTitle("Edit");
                        adb2.setMessage("Edit your entry");
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
                                        String edited_date = et3.getText().toString();
                                        LocalDate text5 = LocalDate.now();

                                        if (edited_amount.equals("") || edited_text.equals("") ) {
                                            Toast.makeText(view.getContext(),"There are Blank Fields",Toast.LENGTH_SHORT).show();
                                        }
                                        else {
                                            //update the listview
                                            String updated_entry = "Description: " + edited_text + "\nAmount: $"+ edited_amount
                                                    + "\nCategory: " + edited_category + "\nDate: " + edited_date;
                                            arrayList.set(position_ind,updated_entry);
                                            adapter.notifyDataSetChanged();

                                            //update data in database
                                            boolean wasUpdated = myDb.updateData(entry_id,edited_text,edited_amount,edited_category,edited_date,text5);

                                            //recreates TrackerFragement to update all changes
                                            getFragmentManager()
                                                    .beginTransaction()
                                                    .detach(TrackerFragment.this)
                                                    .attach(TrackerFragment.this)
                                                    .commit();

                                            //makes a toast to check if data was updated
                                            if(wasUpdated == Boolean.TRUE)
                                                Toast.makeText(view.getContext(),"Data Updated",Toast.LENGTH_SHORT).show();
                                            else
                                                Toast.makeText(view.getContext(),"Data not Updated",Toast.LENGTH_SHORT).show();

                                            dialog.dismiss();

                                        }

                                    }
                                });

                                //cancel button
                                Button neutralButton = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                                neutralButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        //recreates TrackerFragement to update all changes
                                        getFragmentManager()
                                                .beginTransaction()
                                                .detach(TrackerFragment.this)
                                                .attach(TrackerFragment.this)
                                                .commit();
                                        //CLOSE THE DIALOG
                                        dialog.dismiss();
                                    }
                                });
                            }
                        });

                        alertDialog.show();

                    }
                });
                adb.show();
            }
        });

    }





//    @RequiresApi(api = Build.VERSION_CODES.O)
//    //updates the start and end date of the cycle
//    public void cycle_updater() {
//
//        cycle_input = "01"; //sets the default cycle input as the first of the month
//        currentDate = LocalDate.now();
//
//        if (res3!=null && res3.moveToFirst()){  //makes sure table3 is not null
//            cycle_input = res3.getString(2);;
//        }
//        String currentDate_string = String.valueOf(currentDate);
//        String currentMonth_string = ""+ currentDate_string.substring(5,7); //"MM" -- [start ind,end ind)
//
//        String var_string = ""+currentDate_string.substring(0,5) + currentMonth_string + "-" + cycle_input; //variable to compare current date with
//        LocalDate var = LocalDate.parse(var_string);    //convert var into a localdate
//
//        //determine and sets the start and end dates of the cycle
//        if (currentDate.isBefore(var)){
//            LocalDate var_new = var.plusMonths(-1);
//            startdate = var_new;
//            enddate = var;
//            //update database table3
//            myDb.replace_setting(String.valueOf(startdate) , String.valueOf(enddate) , cycle_input );
//        }
//        else {
//            LocalDate var_new = var.plusMonths(1);
//            startdate = var;
//            enddate = var_new;
//            //update database table3
//            myDb.replace_setting(String.valueOf(startdate), String.valueOf(enddate), cycle_input);
//        }
//
//    }





}

