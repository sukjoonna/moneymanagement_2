package com.example.moneymanagement3.ui.setting;

import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.moneymanagement3.DataBaseHelper;
import com.example.moneymanagement3.R;

import java.util.ArrayList;

public class ManageCycFragment extends Fragment {
    View view;
    DataBaseHelper myDb;
    Cursor res3;
    ListView lv;
    String[] managecyc_items;
    ArrayAdapter<String> adapter_managecyc;
    Button btn1;
    String cycle_start_day; String old_cycle_start_day;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragments_settings_manage, container, false);
        view.setBackgroundColor(Color.WHITE);

        myDb = new DataBaseHelper(getActivity());
        //Cursor res = gets all data in the database table3
        res3 = myDb.get_setting();

        //Variables
        btn1 = view.findViewById(R.id.gobackBtn);
        lv = view.findViewById(R.id.manageLv);

        //create listview for "Manage cycles" setting
        managecyc_items = new String[]{"Select Monthly Cycle Start Day", "Delete Previous Cycles", "Delete All Cycles"}; //settings in manage cycles
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
                                    boolean wasupdated = myDb.update_cycle_input(old_cycle_start_day, cycle_start_day);
                                    if(wasupdated)
                                        Toast.makeText(view.getContext(),"You changed your cycle start day",Toast.LENGTH_SHORT).show();
                                    else
                                        Toast.makeText(view.getContext(),"Not updated",Toast.LENGTH_SHORT).show();
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

                }

                //else if "delete all cycles" is selected
                else {

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
}
