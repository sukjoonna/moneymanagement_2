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


        onClick_resetBtn();
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
                    //creates the categories arraylist from database table2
                    categories = new ArrayList<String>();
                    while (res2.moveToNext()) {
                        String category = res2.getString(1); //from database table2
                        categories.add(category);
                    }

                    //creates boolean array of falses
                    final boolean[] bool_list= new boolean[categories.size()];
                    //converts categories arraylist to char sequence array
                    final CharSequence[] categories_list = new CharSequence[categories.size()];
                    for (int i = 0; i < categories.size(); i++){
                        categories_list[i] = categories.get(i);
                    }

                    //create an alert dialog1 builder - "Manage Categories"
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(view.getContext());
                    builder1.setTitle("Manage Categories");
                    builder1.setPositiveButton("Delete", null);
                    builder1.setNeutralButton("Cancel", null);

                    //set the checkbox listview
                    builder1.setMultiChoiceItems(categories_list, bool_list,
                            new DialogInterface.OnMultiChoiceClickListener() {
                                @Override
                                // indexSelected contains the index of item (of which checkbox checked)
                                //checks and unchecks the boxes when clicked
                                public void onClick(DialogInterface dialog, int indexSelected, boolean isChecked) {
                                    bool_list[indexSelected] = isChecked;
                                    String current_item = categories_list[indexSelected].toString();
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
                                    //deletes the categories from database table2 if the boxes are checked
                                    for (int i = 0; i < categories_list.length; i++){
                                        boolean checked = bool_list[i];
                                        if (checked){
                                            res2.moveToPosition(i); //move to correct row in table2
                                            String category_id = res2.getString(0); //get the ID of category
                                            myDb.delete_categories(category_id);
                                        }
                                    }
                                    //recreates SettingFragment so the checkbox list appears again after alertdialog closes
                                    getFragmentManager()
                                            .beginTransaction()
                                            .detach(SettingFragment.this)
                                            .attach(SettingFragment.this)
                                            .commit();
                                    dialog.dismiss();
                                }
                            });

                            //cancel button
                            Button neutralButton = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                            neutralButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //recreates SettingFragment so the checkbox list appears again after alertdialog closes
                                    getFragmentManager()
                                            .beginTransaction()
                                            .detach(SettingFragment.this)
                                            .attach(SettingFragment.this)
                                            .commit();
                                    dialog.dismiss();
                                }
                            });
                        }
                    });
                    alertDialog.show();

                }

            }
        });

    }


    public void onClick_resetBtn() {
        //Button to reset all categories
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