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

    @RequiresApi(api = Build.VERSION_CODES.O)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_setting, container, false);

        btn_reset = view.findViewById(R.id.resetBtn);
        lv_settings = view.findViewById(R.id.settingsLv);


        myDb = new DataBaseHelper(getActivity());
        //Cursor res = gets all data in the database table2
        res2 = myDb.getAllData_categories();

//        cycle_updater();

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




}