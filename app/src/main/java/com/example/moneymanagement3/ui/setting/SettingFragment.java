package com.example.moneymanagement3.ui.setting;

import android.app.Dialog;
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
    ArrayList<String> categories;
    Intent intent;
    ListView lv_settings;
    String[] setting_items;
    ArrayAdapter<String> adapter_settings; ArrayAdapter<String> adapter_categories;

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


        onClick_resetBtn();
        onClick_itemselectedLv();


        return view;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public void onClick_itemselectedLv() {
        //Delete/edit selected items in the listview by selecting an item in the list view
        lv_settings.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> a, View v, final int position, long id) {

                //create an alert dialog1 - "Manage Categories"
                AlertDialog.Builder adb1 = new AlertDialog.Builder(view.getContext());
                adb1.setMessage("Select Categories to delete");

                //create categories arraylist from database table2
                categories = new ArrayList<String>();
                while (res2.moveToNext()) {
                    String category = res2.getString(1); //from database table2
                    categories.add(category);
                }
                ListView lv_categories = view.findViewById(R.id.categoriesLv);
                adapter_categories = new ArrayAdapter<String>(view.getContext(),
                        R.layout.categories_listview,categories);
                lv_categories.setAdapter(adapter_categories);


                // (That new View is just there to have something inside the dialog that can grow big enough to cover the whole screen.)
                Dialog d = adb1.setView(new View(view.getContext())).create();

                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(d.getWindow().getAttributes());
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.MATCH_PARENT;
                d.setTitle("Manage Categories");
                d.show();
                d.getWindow().setAttributes(lp);

//                adb1.setTitle("Manage Categories");

//                //go back button
//                adb1.setNeutralButton("Go back", null);
//                adb1.show();

            }

        });
    }

    public void onClick_resetBtn() {
        //Button to start Third Activity
        btn_reset.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //deletes all newly created categories and restores to default
                int value = myDb.deleteAll_categories();
                if(value > 0)
                    Toast.makeText(view.getContext(),"Deleted all categories",Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(view.getContext(),"Data not Deleted",Toast.LENGTH_SHORT).show();

            }
        });
    }


}