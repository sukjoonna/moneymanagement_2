package com.example.moneymanagement3.ui.setting;

import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.moneymanagement3.DataBaseHelper;
import com.example.moneymanagement3.R;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class ManageCycFragment extends Fragment {
    View view;
    DataBaseHelper myDb;
    Cursor res3; Cursor res4; Cursor res;
    ListView lv;
    String[] managecyc_items;
    ArrayAdapter<String> adapter_managecyc;
    ArrayList<String> cycles;
    Button btn1;
    String cycle_start_day; String old_cycle_start_day; String cycle_input;
    CharSequence[] cycles_list;
    LocalDate startdate; LocalDate enddate; LocalDate currentDate;



    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_managecyc, container, false);
        view.setBackgroundColor(Color.WHITE);

        myDb = new DataBaseHelper(getActivity());
        //Cursor res = gets all data in the database table3
        res3 = myDb.get_setting();

        //Variables
        btn1 = view.findViewById(R.id.gobackBtn);
        lv = view.findViewById(R.id.manageCycLv);

        //create listview for "Manage cycles" setting
        managecyc_items = new String[]{"Select Monthly Cycle Start Day", "Delete Previous Cycles", "Reset All Entries"}; //settings in manage cycles
        adapter_managecyc = new ArrayAdapter<String>(view.getContext(), R.layout.manage_listview_text, R.id.manage_item, managecyc_items);
        lv.setAdapter(adapter_managecyc); //set the listview with the managecyc_items

        //calling functions
        onClick_itemselectedLv();
        onClick_GoBackBtn ();

        return view;
    }

////////////////////////////////////////////////////////////////////////////////////////////////////

    public void onClick_itemselectedLv() {
        //Delete/update selected items in the manageCyc listview by selecting an item in the list view
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void onItemClick(AdapterView<?> a, View v, final int position, long id) {

                //if "select cycle start day" is selected
                if (position == 0) {

                    //gets the view for the spinner layout
                    LayoutInflater inflater = getLayoutInflater();
                    final View spinner_view = inflater.inflate(R.layout.alert_spinner,null);
                    //create spinner that shows days 1-28
                    String[] days = new String[] {"01","02","03","04","05","06","07","08","09","10",
                            "11","12","13","14","15","16","17","18","19",
                            "20","21","22","23","24","25","26","27","28"};
                    final Spinner spn1 = spinner_view.findViewById(R.id.the_spinner); //in alert_spinner.xml
                    ArrayAdapter<String> spn_adapter = new ArrayAdapter<String>(spinner_view.getContext(), R.layout.spinner_text,days);
                    spn1.setAdapter(spn_adapter);
                    spn_adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

                    //What the spinner does when item is selected / not selected
                    spn1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        //when item on spinner is selected
                        public void onItemSelected(AdapterView<?> adapterView, View v, int position, long id) {
                            Object item = adapterView.getItemAtPosition(position);
                            cycle_start_day = item.toString();
                        }
                        @Override
                        //when it is nothing is selected
                        public void onNothingSelected(AdapterView<?> adapterView) {
                            Object item = adapterView.getItemAtPosition(0);
                            cycle_start_day = item.toString();
                        }
                    });

                    //set spinner selection as the previous cycle start day (or "01" by default)
                    res3.moveToFirst();
                    old_cycle_start_day = res3.getString(2);
                    int spn1_position = Integer.parseInt(old_cycle_start_day) - 1;
                    spn1.setSelection(spn1_position);

                    //create an alert dialog1 builder for user to select cycle start day
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(view.getContext());
                    builder1.setTitle("Choose the day you want your cycle to begin:");
                    builder1.setPositiveButton("Update", null);
                    builder1.setNeutralButton("Cancel", null);
                    builder1.setView(spinner_view); //sets the actual spinner in the alertdialog

                    //creates the alert dialog from the builder
                    final AlertDialog alertDialog = builder1.create();

                    //display the alert dialog with the buttons
                    alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(final DialogInterface dialog) {

                            //update button
                            Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                            positiveButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    //Update the cycle start day in table3
                                    boolean wasupdated = myDb.update_cycle_input(old_cycle_start_day, cycle_start_day);
                                    if(wasupdated)
                                        Toast.makeText(view.getContext(),"You changed your cycle start day",Toast.LENGTH_SHORT).show();
                                    else
                                        Toast.makeText(view.getContext(),"Not updated",Toast.LENGTH_SHORT).show();

                                    ///// updates table4////////////////////////////////////////////////////////
                                    //--------------------only works in the condition that between cycles are not deleted

                                    cycle_updater();

                                    res4 = myDb.get_cycles();
                                    int number_of_cycles = 0;
                                    while (res4.moveToNext()){
                                        number_of_cycles++;
                                    }

                                    res3 = myDb.get_setting();
                                    res3.moveToFirst();
                                    String cyc_startdate = res3.getString(0);
                                    String cyc_enddate = res3.getString(1);
                                    LocalDate cyc_startdate_ld = LocalDate.parse(cyc_startdate);
                                    LocalDate cyc_enddate_ld = LocalDate.parse(cyc_enddate);

                                    ArrayList<String> new_startdates = new ArrayList<String>();
                                    ArrayList<String> new_enddates = new ArrayList<String>();

                                    for(int i = 0; i < number_of_cycles-1; i++){
                                        cyc_startdate_ld = cyc_startdate_ld.minusMonths(1);
                                        cyc_enddate_ld = cyc_enddate_ld.minusMonths(1);
                                        cyc_startdate = String.valueOf(cyc_startdate_ld);
                                        cyc_enddate = String.valueOf(cyc_enddate_ld);

                                        new_startdates.add(cyc_startdate);
                                        new_enddates.add(cyc_enddate);
                                    }
                                    Collections.reverse(new_startdates);
                                    Collections.reverse(new_enddates);

                                    myDb.deleteAll_cycles();
                                    myDb.get_cycles();
                                    for(int i = 0; i < number_of_cycles-1; i++){
                                        myDb.insert_cycle(new_startdates.get(i), new_enddates.get(i));
                                    }

                                    res4 = myDb.get_cycles();
                                    res4.moveToFirst();
                                    myDb.delete_cycle(res4.getString(0),res4.getString(1));
                                    /////////////////////////////////////////////////////////////////////////////////////////////



                                    //recreates current Fragment it refreshes after alertdialog closes
                                    getFragmentManager()
                                            .beginTransaction()
                                            .detach(ManageCycFragment.this)
                                            .attach(ManageCycFragment.this)
                                            .commit();
                                    dialog.dismiss();



                                }
                            });

                            //Cancel button
                            Button neutralButton = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                            neutralButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //recreates current Fragment it refreshes after alertdialog closes
                                    getFragmentManager()
                                            .beginTransaction()
                                            .detach(ManageCycFragment.this)
                                            .attach(ManageCycFragment.this)
                                            .commit();
                                    dialog.dismiss();
                                }
                            });
                        }
                    });
                    alertDialog.show();

                }



                //if "delete previous cycles" is selected
                else if (position==1){

                    //creates the cycles arraylist from database table4
                    res4 = myDb.get_cycles();
                    cycles = new ArrayList<String>();
                    while(res4.moveToNext()){
                        String cyc_startdate = res4.getString(0);
                        String cyc_enddate = res4.getString(1);
                        LocalDate cyc_startdate_localdate = LocalDate.parse(cyc_startdate);
                        LocalDate cyc_enddate_localdate = LocalDate.parse(cyc_enddate);

                        //Formatting the localdate ==> custom string format (Month name dd, yyyy)
                        DateTimeFormatter cyc_formatter = DateTimeFormatter.ofPattern("LLL dd, yy");
                        String cyc_startdate_formatted = cyc_startdate_localdate.format(cyc_formatter);
                        String cyc_enddate_formatted = cyc_enddate_localdate.format(cyc_formatter);

                        String formatted_dates = cyc_startdate_formatted + " - " + cyc_enddate_formatted;
                        cycles.add(formatted_dates);
                    }
                    Collections.reverse(cycles);

                    //creates boolean array of falses
                    final boolean[] bool_list = new boolean[cycles.size()];
                    //converts cycles arraylist to char sequence array
                    cycles_list = new CharSequence[cycles.size()];
                    for (int i = 0; i < cycles.size(); i++) {
                        cycles_list[i] = cycles.get(i);
                    }

                    //if there are no cycles, show an alert saying "there are no categories"
                    if (cycles_list.length == 0) {
                        AlertDialog.Builder builder0 = new AlertDialog.Builder(view.getContext());
                        builder0.setTitle("Alert");
                        builder0.setMessage("There are no cycles");
                        builder0.setPositiveButton("Okay", null);
                        builder0.show();
                    }

                    //otherwise
                    else {
                        //create an alert dialog1 builder - "Select Cycles to delete"
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(view.getContext());
                        builder1.setTitle("Select Cycles to Delete");
                        builder1.setPositiveButton("Delete", null);
                        builder1.setNeutralButton("Cancel", null);

                        //set the checkbox listview
                        builder1.setMultiChoiceItems(cycles_list, bool_list,
                                new DialogInterface.OnMultiChoiceClickListener() {
                                    @Override
                                    // indexSelected contains the index of item (of which checkbox checked)
                                    //checks and unchecks the boxes when clicked
                                    public void onClick(DialogInterface dialog, int indexSelected, boolean isChecked) {
                                        bool_list[indexSelected] = isChecked;
                                        String current_item = cycles_list[indexSelected].toString();
                                    }
                                });
                        //creates the alert dialog from the builder
                        final AlertDialog alertDialog = builder1.create();

                        //display the alert dialog with the buttons
                        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(final DialogInterface dialog) {

                                //delete button
                                Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                                positiveButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        //deletes the cycles from database table4 and table1 if the boxes are checked
                                        for (int i = 0; i < cycles_list.length; i++) {
                                            boolean checked = bool_list[i];
                                            if (checked) {
                                                //delete the physical cycle in database table4
                                                int inverted_pos = (cycles.size() - 1)- i; //this is important bc multichoice list shows new-->old, but in the database table4, it's indexed old--->new
                                                res4.moveToPosition(inverted_pos); //move to correct row in table2
                                                String cycle_startdate = res4.getString(0);
                                                String cycle_enddate = res4.getString(1);
                                                myDb.delete_cycle(cycle_startdate,cycle_enddate);

                                                //delete the actual entries within that cycle in database table1
                                                LocalDate cycle_startdate_ld = LocalDate.parse(cycle_startdate).minusDays(1); //(startdate,enddate]
                                                LocalDate cycle_enddate_ld = LocalDate.parse(cycle_enddate);
                                                myDb.deletedDataDateRange(cycle_startdate_ld,cycle_enddate_ld);

                                            }
                                        }
                                        //recreates SettingFragment so the checkbox list appears again after alertdialog closes
                                        getFragmentManager()
                                                .beginTransaction()
                                                .detach(ManageCycFragment.this)
                                                .attach(ManageCycFragment.this)
                                                .commit();
                                        dialog.dismiss();
                                    }
                                });

                                //Cancel button
                                Button neutralButton = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                                neutralButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        //recreates SettingFragment so the checkbox list appears again after alertdialog closes
                                        getFragmentManager()
                                                .beginTransaction()
                                                .detach(ManageCycFragment.this)
                                                .attach(ManageCycFragment.this)
                                                .commit();
                                        dialog.dismiss();
                                    }
                                });
                            }
                        });
                        alertDialog.show();
                    }


                }





                //else if "reset all entries" is selected
                else {
                    //An alert dialog box pops up to make sure you want to delete/reset everything
                    AlertDialog alertDialog = new AlertDialog.Builder(view.getContext()).create();
                    alertDialog.setTitle("Alert");
                    alertDialog.setMessage("Do you want to reset everything?");
                    //Make an "ok" button
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                            new DialogInterface.OnClickListener() {
                                //OnClick:
                                public void onClick(DialogInterface dialog, int which) {
                                    int deletedRows = 0;

                                    //deleteAll() returns the number of rows in the database table deleted
                                    deletedRows = myDb.deleteAll();

                                    //makes a toast
                                    if(deletedRows > 0)
                                        Toast.makeText(view.getContext(),"Deleted all entries",Toast.LENGTH_SHORT).show();
//                                else
//                                    Toast.makeText(SecondActivity.this,"Data not Deleted",Toast.LENGTH_SHORT).show();

                                    //dismiss dialog
                                    dialog.dismiss();
                                }
                            });

                    //make a "cancel" button
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Cancel",
                            new DialogInterface.OnClickListener() {
                                //OnClick:
                                public void onClick(DialogInterface dialog, int which) {
                                    //dismiss dialog
                                    dialog.dismiss();
                                }
                            });

                    alertDialog.show();

                }

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


    @RequiresApi(api = Build.VERSION_CODES.O)
    //updates the start and end date of the cycle
    public void cycle_updater() {

        cycle_input = cycle_start_day;
        currentDate = LocalDate.now();

        String currentDate_string = String.valueOf(currentDate);
        String currentMonth_string = ""+ currentDate_string.substring(5,7); //"MM" -- [start ind,end ind)

        String var_string = ""+currentDate_string.substring(0,5) + currentMonth_string + "-" + cycle_input; //variable to compare current date with
        LocalDate var = LocalDate.parse(var_string);    //convert var into a localdate

        //determine and sets the new start and end dates of the cycle
        if (currentDate.isBefore(var)){
            LocalDate var_new = var.plusMonths(-1);
            startdate = var_new;
            enddate = var.minusDays(1);
            //update database table3
            myDb.update_cycle_setting(String.valueOf(startdate) , String.valueOf(enddate) , cycle_input );
        }
        else {
            LocalDate var_new = var.plusMonths(1);
            startdate = var;
            enddate = var_new.minusDays(1);
            //update database table3
            myDb.update_cycle_setting(String.valueOf(startdate) , String.valueOf(enddate) , cycle_input );
        }

        //dealing with table4 (cycle table) ---- for cycle spinner
        res4 = myDb.get_cycles();
        if (res4!=null && res4.moveToLast()){  //makes sure table3 is not null
            String past_startdate = res4.getString(0);
            String past_enddate = res4.getString(1);
            if (!past_startdate.equals(String.valueOf(startdate))  &&  !past_enddate.equals(String.valueOf(enddate))   ){
                //inserts the start and end date of the cycle only if the dates changed
                myDb.insert_cycle(String.valueOf(startdate),String.valueOf(enddate));
            }
        }
        else {
            //inserts the start and end date of the cycle for when the first time app is run
            myDb.insert_cycle(String.valueOf(startdate),String.valueOf(enddate));
        }

    }




}
