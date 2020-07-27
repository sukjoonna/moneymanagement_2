package com.example.moneymanagement3.ui.budget;

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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.moneymanagement3.DataBaseHelper;
import com.example.moneymanagement3.R;
import com.example.moneymanagement3.ui.setting.ManageCatFragment;
import com.example.moneymanagement3.ui.setting.SettingFragment;

import java.util.ArrayList;
import java.util.List;

public class ManageBudFragment extends Fragment {
    View view;
    DataBaseHelper myDb;
    ListView lv;
    String[] managebud_items;
    String categories; String categories_budget;
    ArrayAdapter<String> adapter_managebud;
    Button btn1;
    Cursor res3; Cursor res2; Cursor res4;
    TextView tv_notice;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_managebud, container, false);
        view.setBackgroundColor(Color.WHITE);

        myDb = new DataBaseHelper(getActivity());

        btn1 = view.findViewById(R.id.gobackBtn);
        lv = view.findViewById(R.id.manageBudLv);
        tv_notice = view.findViewById(R.id.noticeTv);

        TextView title = view.findViewById(R.id.manageBudTv);
        title.setText("Manage Budget");

        tv_notice.setText("*These budget selections are for this cycle only.\n" + "Previous cycle budgets cannot be modified.");
        tv_notice.setTextSize(16);

        managebud_items = new String[]{"Set Monthly Cycle Budget", "Set Budget per Category"};
        adapter_managebud = new ArrayAdapter<String>(view.getContext(), R.layout.manage_listview_text, R.id.manage_item, managebud_items);
        lv.setAdapter(adapter_managebud);


        onClick_itemselectedLv();
        onClick_GoBackBtn ();


        return view;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////

    public void onClick_itemselectedLv() {
        //Delete/edit selected items in the manageCat listview by selecting an item in the list view
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> a, View v, final int position, long id) {

                //if "Set Monthly Cycle Budget" is selected
                if (position == 0) {
                    //create an alert dialog1

                    AlertDialog.Builder adb=new AlertDialog.Builder(view.getContext());
                    adb.setTitle("Enter Monthly Budget Amount");
//                    adb.setMessage("What would you like to do with this entry?");

                    //budget_edittext.xml --- show edittext in alert
                    LayoutInflater inflater = getLayoutInflater();
                    final View setCycleBudget_view = inflater.inflate(R.layout.set_cycle_budget_alert,null); //xml file used
                    final EditText et = setCycleBudget_view.findViewById(R.id.setBudgetEt);
                    res4 = myDb.get_cycles();
                    res4.moveToLast();
                    final String old_cycle_budget = res4.getString(2);
                    final String old_startdate = res4.getString(0);
                    et.setText(old_cycle_budget);
                    adb.setView(setCycleBudget_view);

                    //buttons
                    adb.setPositiveButton("Set", null);
                    adb.setNeutralButton("Cancel", null);

                    final AlertDialog alertDialog = adb.create();

                    alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(final DialogInterface dialog) {
                            //set button
                            Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                            positiveButton.setOnClickListener(new View.OnClickListener() {
                                @RequiresApi(api = Build.VERSION_CODES.O)
                                @Override
                                public void onClick(View v) {
                                    if (et.getText().toString().equals("")){
                                        Toast.makeText(view.getContext(),"Enter Budget",Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        double budget_amount = Double.parseDouble(et.getText().toString());
                                        String new_cycle_budget = String.format("%.2f",budget_amount);
                                        myDb.update_cycles_table_Budget(old_startdate,new_cycle_budget);
                                        dialog.dismiss();
                                    }

                                }


                            });

                        }
                    });

                    alertDialog.show();


//                    //Cancel button
//                    adb.setNeutralButton("Cancel", new AlertDialog.OnClickListener() {
//                        @RequiresApi(api = Build.VERSION_CODES.O)
//                        public void onClick(DialogInterface dialog, int which) {
//
//                        }
//                    });
//
//                    //Set button
//                    adb.setPositiveButton("Set", new AlertDialog.OnClickListener() {
//                        @RequiresApi(api = Build.VERSION_CODES.O)
//                        public void onClick(DialogInterface dialog, int which) {
//                            double budget_amount = Double.parseDouble(et.getText().toString());
//                            String new_cycle_budget = String.format("%.2f",budget_amount);
//                            myDb.update_cycles_table_Budget(old_startdate,new_cycle_budget);
//
//                        //recreates TrackerFragement to update all changes
//                        getFragmentManager()
//                                .beginTransaction()
//                                .detach(ManageBudFragment.this)
//                                .attach(ManageBudFragment.this)
//                                .commit();
//                        }
//                    });

                }


                //if "Set Budget per Category" is selected
                else {

                    //starts new fragment "SetBudgetCatFragment"
                    SetBudgetCatFragment frag= new SetBudgetCatFragment();
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, frag, "setbudcatfrag")
                            .addToBackStack(null)
                            .commit();

                }
            }
        });
    }


    public void onClick_GoBackBtn () {
        //Button to go back to settings
        btn1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                BudgetFragment frag= new BudgetFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, frag, "budgetFrag")
                        .commit();
            }
        });
    }


}


