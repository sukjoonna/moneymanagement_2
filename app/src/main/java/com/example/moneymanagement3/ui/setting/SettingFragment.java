package com.example.moneymanagement3.ui.setting;

import android.app.Dialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.moneymanagement3.DataBaseHelper;
import com.example.moneymanagement3.R;
import com.example.moneymanagement3.ui.tracker.TrackerFragment;

import java.util.ArrayList;

public class SettingFragment extends Fragment {

    View view;
    Button btn_reset;
    DataBaseHelper myDb;
    Cursor res2;
    ListView lv_settings;
    String[] setting_items;
    ArrayAdapter<String> adapter_settings;
    ArrayList<String> categories;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_setting, container, false);

        btn_reset = view.findViewById(R.id.resetBtn);
        lv_settings = view.findViewById(R.id.settingsLv);


        myDb = new DataBaseHelper(getActivity());
        //Cursor res = gets all data in the database table2
        res2 = myDb.getAllData_categories();

        //Building and creating the listview
        setting_items = new String[]{"Manage Categories","Manage Cycles"};
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
                else{

                }

            }
        });

    }



}