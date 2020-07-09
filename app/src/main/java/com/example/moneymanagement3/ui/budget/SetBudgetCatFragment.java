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

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.moneymanagement3.DataBaseHelper;
import com.example.moneymanagement3.R;

import java.util.ArrayList;

public class SetBudgetCatFragment extends Fragment {
    View view;
    DataBaseHelper myDb;
    ListView lv;
    String categories; String categories_budget;
    ArrayAdapter<String> adapter_setcatbud;
    Button btn1;
    Cursor res3; Cursor res2; Cursor res4;
    String [] categories_list;
    String [] categories_budget_list;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_managebud, container, false);
        view.setBackgroundColor(Color.WHITE);

        myDb = new DataBaseHelper(getActivity());

        btn1 = view.findViewById(R.id.gobackBtn);
        lv = view.findViewById(R.id.manageBudLv);
        TextView title = view.findViewById(R.id.manageBudTv);
        title.setText("Set Category Budget");


        categories_list = get_categories_from_Table4();
        categories_budget_list = get_categories_budget_from_Table4();

        adapter_setcatbud = new ArrayAdapter<String>(view.getContext(), R.layout.manage_listview_text, R.id.manage_item, categories_list);
        lv.setAdapter(adapter_setcatbud);


        onClick_itemselectedLv();
        onClick_GoBackBtn ();


        return view;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////

    public void onClick_itemselectedLv() {
        //when category is selected
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> a, View v, final int position, long id) {

                AlertDialog.Builder adb=new AlertDialog.Builder(view.getContext());
                adb.setTitle("Enter budget amount");
//                    adb.setMessage("What would you like to do with this entry?");

                //budget_edittext.xml --- show edittext in alert
                LayoutInflater inflater = getLayoutInflater();
                final View setCycleBudget_view = inflater.inflate(R.layout.set_cycle_budget_alert,null); //xml file used
                final EditText et = setCycleBudget_view.findViewById(R.id.setBudgetEt);
                res4 = myDb.get_cycles();
                res4.moveToLast();
                final String old_category_budget = categories_budget_list[position];
                final String old_startdate = res4.getString(0);
                et.setText(old_category_budget);
                adb.setView(setCycleBudget_view);

                //Cancel button
                adb.setNeutralButton("Cancel", new AlertDialog.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                //Set button
                adb.setPositiveButton("Set", new AlertDialog.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    public void onClick(DialogInterface dialog, int which) {
                        double budget_amount = Double.parseDouble(et.getText().toString());
                        String new_category_budget = String.format("%.2f",budget_amount);

                        categories_budget_list[position] = new_category_budget;
                        StringBuilder categories_budget_list_stringbuilder = new StringBuilder();

                        for (int i = 0; i < categories_budget_list.length; i++){
                            categories_budget_list_stringbuilder.append(categories_budget_list[i]).append(";");
                        }

                        myDb.update_cycles_table_CatBudget(old_startdate,categories_budget_list_stringbuilder.toString());

                        //recreates TrackerFragement to update all changes
                        getFragmentManager()
                                .beginTransaction()
                                .detach(SetBudgetCatFragment.this)
                                .attach(SetBudgetCatFragment.this)
                                .commit();
                    }
                });

                adb.show();


            }
        });
    }


    public void onClick_GoBackBtn () {
        //Button to go back to settings
        btn1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ManageBudFragment frag= new ManageBudFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, frag, "setbudcatfrag")
                        .commit();
            }
        });
    }

    public String[] get_categories_from_Table4 (){
        res4 = myDb.get_cycles();
        res4.moveToLast();
        categories = res4.getString(3);
        return categories.split("\\;");
    }

    public String[] get_categories_budget_from_Table4 (){
        res4 = myDb.get_cycles();
        res4.moveToLast();
        categories_budget = res4.getString(4);
        return categories_budget.split("\\;");
    }


}
