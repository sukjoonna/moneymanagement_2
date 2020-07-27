package com.example.moneymanagement3.ui.budget;

import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.moneymanagement3.DataBaseHelper;
import com.example.moneymanagement3.R;
import com.example.moneymanagement3.ui.tracker.TrackerFragment;

import java.text.DecimalFormat;
import java.time.LocalDate;
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
        title.setText("Set Category by Budget");


        categories_list = get_categories_from_Table4();
        categories_budget_list = get_categories_budget_from_Table4();

        if (categories_list.length == 1 && categories_list[0].equals("")){
            TextView tv_notice = view.findViewById(R.id.noticeTv);
            tv_notice.setText("There are no categories");
        }
        else{
            adapter_setcatbud = new ArrayAdapter<String>(view.getContext(), R.layout.manage_listview_text, R.id.manage_item, categories_list);
            lv.setAdapter(adapter_setcatbud);
        }


        onClick_itemselectedLv();
        onClick_GoBackBtn ();


        return view;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////

    public void onClick_itemselectedLv() {
        //when category is selected
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> a, View v, final int position, long id) {

                //create a custom alert dialog
                AlertDialog.Builder adb1 = new AlertDialog.Builder(view.getContext());
                //create views
                final EditText et1 = new EditText(view.getContext());
                TextView msg1 = new TextView(view.getContext());    //create a message Tv for adb1
                TextView title1 = new TextView(view.getContext());    //create a message Tv for adb1

                //get necessary data from database
                res4 = myDb.get_cycles();
                res4.moveToLast();
                final String old_category_budget = categories_budget_list[position];
                final String old_startdate = res4.getString(0);
                final String old_cycle_budget = res4.getString(2);

                //initialize and set views
                et1.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                et1.setText(old_category_budget);

                double sum = 0;
                for (int i = 0; i < categories_budget_list.length; i++){ //sum the category budgets
                    sum += Double.parseDouble(categories_budget_list[i]);
                }
                final double difference = Double.parseDouble(old_cycle_budget) - (sum - Double.parseDouble(old_category_budget));

                title1.setText("Enter budget amount:"); //the title
                String msg = "Notice: Enter up to $" + String.format("%.2f",difference) + ".\n"
                        + "(Amount remaining from your monthly budget)";
                msg1.setText(msg); //the message


                //customize views
                et1.setGravity(Gravity.CENTER);
                et1.setBackgroundResource(R.drawable.light_rectangle);
                et1.setMaxWidth(50);
                msg1.setTextColor(Color.parseColor("#B3BDAC"));
                msg1.setTextSize(15);
                msg1.setPadding(50, 5, 20, 10);
                title1.setGravity(Gravity.CENTER);    //center
                title1.setTextSize(20);
                title1.setPadding(10, 50, 10, 10);
                title1.setTextColor(Color.BLACK);

                //buttons
                adb1.setPositiveButton("Set", null);
                adb1.setNeutralButton("Cancel", null);

                Space space1 = new Space(view.getContext());
                space1.setMinimumHeight(15);
                Space space2 = new Space(view.getContext());
                space2.setMinimumHeight(15);

                //add the et and msg into a linear layout
                LinearLayout linearLayout = new LinearLayout(view.getContext());
                linearLayout.setOrientation(LinearLayout.VERTICAL);
//                linearLayout.setPadding(20,0,20,0);
                linearLayout.addView(space1);
                linearLayout.addView(msg1);
                linearLayout.addView(space2);
                linearLayout.addView(et1);

                adb1.setView(linearLayout);
                adb1.setCustomTitle(title1);

                final AlertDialog alertDialog = adb1.create();

                final double finalSum = sum - Double.parseDouble(categories_budget_list[position]);
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(final DialogInterface dialog) {
                        //set button
                        Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        positiveButton.setOnClickListener(new View.OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void onClick(View v) {

                                if(et1.getText().toString().equals("")){
                                    Toast.makeText(view.getContext(),"Enter Budget",Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    double budget_amount = Double.parseDouble(et1.getText().toString());

                                    double new_difference = Double.parseDouble(old_cycle_budget) - (finalSum + budget_amount);

                                    if (new_difference >= 0){

                                        String new_category_budget = String.format("%.2f",budget_amount);

                                        categories_budget_list[position] = new_category_budget;
                                        StringBuilder categories_budget_list_stringbuilder = new StringBuilder();

                                        for (String s : categories_budget_list) {
                                            categories_budget_list_stringbuilder.append(s).append(";");
                                        }

                                        myDb.update_cycles_table_CatBudget(old_startdate,categories_budget_list_stringbuilder.toString());

                                        Toast.makeText(view.getContext(),"Budget set",Toast.LENGTH_LONG).show();

                                        //recreates TrackerFragement to update all changes
                                        getFragmentManager()
                                                .beginTransaction()
                                                .detach(SetBudgetCatFragment.this)
                                                .attach(SetBudgetCatFragment.this)
                                                .commit();

                                        dialog.dismiss();
                                    }
                                    else{
                                        Toast.makeText(view.getContext(),"Amount exceeds monthly budget",Toast.LENGTH_LONG).show();
                                    }
                                }


                            }


                        });

                    }
                });

                alertDialog.show();

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
