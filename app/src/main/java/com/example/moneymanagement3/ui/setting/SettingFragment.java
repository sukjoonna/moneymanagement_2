package com.example.moneymanagement3.ui.setting;

import android.app.Dialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
import androidx.lifecycle.ViewModelProviders;

import com.example.moneymanagement3.DataBaseHelper;
import com.example.moneymanagement3.R;
import com.example.moneymanagement3.ui.tracker.TrackerFragment;

import java.time.LocalDate;
import java.util.ArrayList;

public class SettingFragment extends Fragment {

    View view;
    Button btn_reset;
    DataBaseHelper myDb;
    Cursor res2; Cursor res3; Cursor res4;
    ListView lv_settings;
    String[] setting_items;
    ArrayAdapter<String> adapter_settings;
    LocalDate startdate; LocalDate enddate; LocalDate currentDate;
    String cycle_input;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_setting, container, false);

        btn_reset = view.findViewById(R.id.resetBtn);
        lv_settings = view.findViewById(R.id.settingsLv);


        myDb = new DataBaseHelper(getActivity());
        //Cursor res = gets all data in the database table2
        res2 = myDb.getAllData_categories();

        //Building and creating the listview
        setting_items = new String[]{"Manage Categories","Manage Cycles","Hard Reset"};
        adapter_settings = new ArrayAdapter<String>(view.getContext(),R.layout.setting_listview_texts,R.id.setting_item,setting_items);
        lv_settings.setAdapter(adapter_settings);


        onClick_itemselectedLv();


        return view;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public void onClick_itemselectedLv() {
        //Delete/edit selected items in the settings listview by selecting an item in the settings list view
        lv_settings.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> a, View v, final int position, long id) {

                //If "Manage Categories" is selected
                if (position==0){
                    //starts new fragment "ManageCatFragment"
                    ManageCatFragment frag= new ManageCatFragment();
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, frag, "manageCatFrag")
                            .addToBackStack(null)
                            .commit();
                }
                //If "Manage cycles" is selected"
                else if (position==1){
                    //starts new fragment "ManageCatFragment"
                    ManageCycFragment frag= new ManageCycFragment();
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, frag, "manageCycFrag")
                            .addToBackStack(null)
                            .commit();
                }
                //if hard reset is selected
                else if (position==2){
                    //An alert dialog box pops up to make sure you want to delete/reset everything
                    AlertDialog alertDialog = new AlertDialog.Builder(view.getContext()).create();
                    alertDialog.setTitle("HARD RESET");
                    alertDialog.setMessage("Do you want to reset the whole app?");
                    //Make an "ok" button
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Yes, reset",
                            new DialogInterface.OnClickListener() {
                                //OnClick:
                                public void onClick(DialogInterface dialog, int which) {
                                    int deletedRows1 = 0;
                                    int deletedRows2 = 0;
                                    int deletedRows3 = 0;
                                    int deletedRows4 = 0;

                                    //deleteAll() returns the number of rows in the database table1 deleted
                                    deletedRows1 = myDb.deleteAll();
                                    //deleteAll_categories() returns the number of rows in the database table2 deleted
                                    deletedRows2 = myDb.deleteAll_categories();
                                    //deleteAll_settings() returns the number of rows in the database table3 deleted
                                    deletedRows3 = myDb.deleteAll_setting();
                                    //deleteAll_cycles() returns the number of rows in the database table4 deleted
                                    deletedRows4 = myDb.deleteAll_cycles();

                                    //makes a toast
                                    if((deletedRows1 > 0 && deletedRows2 > 0) && (deletedRows3 > 0 && deletedRows4 > 0))
                                        Toast.makeText(view.getContext(),"App reset",Toast.LENGTH_SHORT).show();

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




            }
        });

    }



//    @RequiresApi(api = Build.VERSION_CODES.O)
//    //updates the start and end date of the cycle
//    public void cycle_updater() {
//
//        cycle_input = "01"; //sets the default cycle input as the first of the month
//        currentDate = LocalDate.now(); //get current date
//
//        if (res3 != null && res3.moveToFirst()) {  //makes sure table3 is not null
//            cycle_input = res3.getString(2);
//        }
//        else {
//            myDb.create_filler_setting_onStartup(cycle_input);
//        }
//
//        String currentDate_string = String.valueOf(currentDate);
//        String currentMonth_string = "" + currentDate_string.substring(5, 7); //"MM" -- [start ind,end ind)
//
//        String var_string = "" + currentDate_string.substring(0, 5) + currentMonth_string + "-" + cycle_input; //variable to compare current date with
//        LocalDate var = LocalDate.parse(var_string);    //convert var into a localdate
//
//        //determine and sets the start and end dates of the cycle in table3
//        if (currentDate.isBefore(var)) {
//            LocalDate var_new = var.plusMonths(-1);
//            startdate = var_new;
//            enddate = var.minusDays(1);
//            //update database table3
//            myDb.update_cycle_setting(String.valueOf(startdate), String.valueOf(enddate), cycle_input);
//        }
//        else {
//            LocalDate var_new = var.plusMonths(1);
//            startdate = var;
//            enddate = var_new.minusDays(1);
//            //update database table3
//            myDb.update_cycle_setting(String.valueOf(startdate), String.valueOf(enddate), cycle_input);
//        }
//
//
//        //******************************************************************************new added
//
//
//        //dealing with table4 (cycle table) ---- for cycle spinner
//        if (res4!=null && res4.moveToLast()) { //if table4 is not null on startup (run basically every time this fragment is selected)
//            String past_startdate = res3.getString(0);
//            String past_enddate = res3.getString(1);
//
//            if (!past_startdate.equals(String.valueOf(startdate)) && !past_enddate.equals(String.valueOf(enddate))) { //if a new cycle started (new month)
//                StringBuilder categories_budget_list_as_string = new StringBuilder("");
//                StringBuilder categories_list_as_string = new StringBuilder("");
//                res2 = myDb.getAllData_categories();
//                if (res2 != null) { // if categories table3 is not empty
//                    while (res2.moveToNext()) {
//                        String category = res2.getString(1);
//                        categories_list_as_string.append(";").append(category);
//                        categories_budget_list_as_string.append(";").append("0.00");
//                    }
//                }
//                //inserts the start and end date of the cycle only if the dates changed
//                myDb.insert_new_cycle(String.valueOf(startdate), String.valueOf(enddate), "0.00",
//                        categories_list_as_string.toString(), categories_budget_list_as_string.toString());
//            } else { //if new cycle not started
//                res4.moveToLast();
//                StringBuilder categories_budget_list_as_string = new StringBuilder(res4.getString(4));
//                StringBuilder categories_list_as_string = new StringBuilder("");
//                String[] categories_budget_list = categories_budget_list_as_string.toString().split("\\;");
//                int categories_list_length = 0;
//
//                if (res2 != null) { // if categories table3 is not empty
//                    res2 = myDb.getAllData_categories();
//                    while (res2.moveToNext()) {
//                        String category = res2.getString(1);
//                        categories_list_as_string.append(";").append(category);
//                        categories_list_length++;
//                    }
//                }
//
//                int list_size_difference = categories_list_length - (categories_budget_list.length-1);
//                if (list_size_difference > 0) { //more categories than cat budget
//                    for (int i = 0; i < list_size_difference; i++) {
//                        categories_budget_list_as_string.append(";0.00");
//                    }
//                }
//
//                //updates table4
//                myDb.update_cycles_table_Category(String.valueOf(startdate), categories_list_as_string.toString());
//                myDb.update_cycles_table_CatBudget(String.valueOf(startdate), categories_budget_list_as_string.toString());
//            }
//        }
//        else { //if table4 null (only when first run)
//
//            StringBuilder categories_budget_list_as_string = new StringBuilder("");
//            StringBuilder categories_list_as_string = new StringBuilder("");
//            res2 = myDb.getAllData_categories();
//            if (res2 != null) { // if categories table3 is not empty
//                while (res2.moveToNext()) {
//                    String category = res2.getString(1);
//                    categories_list_as_string.append(";").append(category);
//                    categories_budget_list_as_string.append(";").append("0.00");
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



}