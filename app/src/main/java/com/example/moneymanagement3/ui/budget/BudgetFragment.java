package com.example.moneymanagement3.ui.budget;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.moneymanagement3.ui.setting.ManageCatFragment;
import com.example.moneymanagement3.ui.tracker.Entry;
import com.example.moneymanagement3.ui.tracker.EntryListAdapter;
import com.example.moneymanagement3.ui.tracker.TrackerFragment;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;

public class BudgetFragment extends Fragment {

    //cycle updater variables
    DataBaseHelper myDb;
    Cursor res3; Cursor res4; Cursor res; Cursor res2; Cursor res_cat_amounts;
    LocalDate startdate; LocalDate enddate; LocalDate currentDate;
    String cycle_input;
    ArrayList<String> cycles;
    Spinner spinner_cycles;
    /////
    Button btn_setBudget;
    double amount_total;
    TextView tv_cycleAmountTotal; TextView tv_cycleBudgetAmount; TextView tv_amountLeft; TextView tv_by_cat;
    ListView budget_lv;
    ArrayList<CategoryBudget> category_budget_arraylist;
    CategoryBudgetListAdapter categoryBudgetListAdapter;
    View view;
    String categories; String categories_budget;
    ArrayList<String> categories_arraylist; ArrayList<Double> amounts_arraylist;ArrayList<String> cat_budget_arraylist;
    ArrayList<String> difference_arraylist; ArrayList<String> difference_arraylist_sorted;



    @RequiresApi(api = Build.VERSION_CODES.O)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_budget, container, false);

        //get database
        myDb = new DataBaseHelper(getActivity());

        btn_setBudget = view.findViewById(R.id.setBudgetBtn);
        tv_cycleAmountTotal = view.findViewById(R.id.totalTv);
        tv_cycleBudgetAmount = view.findViewById(R.id.cycleBudgetAmountTv);
        tv_amountLeft = view.findViewById(R.id.cycleAmountLeftTv);
        tv_by_cat = view.findViewById(R.id.by_cat_tv);

        budget_lv = view.findViewById(R.id.budgetLv);

        categories_arraylist = new ArrayList<String>();
        cat_budget_arraylist = new ArrayList<String>();
        amounts_arraylist = new ArrayList<Double>();
        difference_arraylist = new ArrayList<String>();

        //underline "by category:"
        SpannableString content = new SpannableString("By Category:");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        tv_by_cat.setText(content);



        //------------------------CYCLE CREATE AND UPDATER in DB (ALONG WITH SPINNER) -------------------------//                   *Make sure this is at top
        res3 = myDb.get_setting();
        res3.moveToFirst();

        //Cycle updater
        cycle_updater();

        spinner_cycles = view.findViewById(R.id.cycleSpn);

        //Create Cycle Spinner
        cycles = new ArrayList<String>();
        res4 = myDb.get_cycles();
        while(res4.moveToNext()){
            String cyc_startdate = res4.getString(0);
            String cyc_enddate = res4.getString(1);
            LocalDate cyc_startdate_localdate = LocalDate.parse(cyc_startdate);
            LocalDate cyc_enddate_localdate = LocalDate.parse(cyc_enddate);

            //Formatting the localdate ==> custom string format (Month name dd, yyyy)
            DateTimeFormatter cyc_formatter = DateTimeFormatter.ofPattern("LLL dd, yy");
            String cyc_startdate_formatted = cyc_startdate_localdate.format(cyc_formatter);
            String cyc_enddate_formatted = cyc_enddate_localdate.format(cyc_formatter);

            String formatted_dates = cyc_startdate_formatted + " ~ " + cyc_enddate_formatted;
            cycles.add(formatted_dates);
        }
        Collections.reverse(cycles);
        ArrayAdapter<String> spn_cyc_adapter = new ArrayAdapter<String>(view.getContext(), R.layout.spinner_text,cycles);
        spinner_cycles.setAdapter(spn_cyc_adapter);

        //------------------------------------------------END-----------------------------------------------//


        calculate_and_set_cycleAmount();

        res4.moveToLast();
        calculate_and_set_cycleBudget(res4.getPosition());

        build_List();
        onClick_itemselectedLv();

        onClick_Btn_setBudget();

        onSelect_CycleSpinner();




        return view;
    }


//////////////////////////////////////////////////////////////////////////////////////////////////////


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void build_List() {

        //Get data from database table1
        LocalDate startdate_selected = startdate;
        String[] categories_budget_aslist = get_categories_budget_from_Table4();
        String[] categories_aslist = get_categories_from_Table4();
        res_cat_amounts = myDb.getCategoricalBudgetDateRange(startdate_selected.minusDays(1),enddate); // (startdate,enddate]

        if(res_cat_amounts!=null){

            category_budget_arraylist = new ArrayList<CategoryBudget>();
            categoryBudgetListAdapter = new CategoryBudgetListAdapter(view.getContext(),R.layout.adapter_budgetview_layout,category_budget_arraylist);

            //create arraylist for each category, amount, budget, difference, so we can sort it in descending order
            while (res_cat_amounts.moveToNext()) {
                String category = res_cat_amounts.getString(0);
                String amount = res_cat_amounts.getString(1);

                //find the index of budget of category
                int ind = -1;
                for (int i = 0; i < categories_aslist.length; i++) {
                    if (category.equals(categories_aslist[i])) {
                        ind = i;
                    }
                }

                //to account for when category is deleted
                String budget;
                double difference;
                if (ind ==-1){
                    budget = "----------";
                    difference = 1000000;
                }
                else{
                    budget = categories_budget_aslist[ind];
                    difference = Double.parseDouble(budget) - Double.parseDouble(amount);
                }


                categories_arraylist.add(category);
                amounts_arraylist.add(Double.parseDouble(amount));
                cat_budget_arraylist.add(budget);
                difference_arraylist.add(String.format("%.2f",difference));
            }

            //building the adapter for listview
            difference_arraylist_sorted = new ArrayList<String>();
            int size = categories_arraylist.size();
            for (int i = 0; i <size; i++){
                double max_amount = Collections.max(amounts_arraylist);
                int max_amount_ind = amounts_arraylist.indexOf(max_amount);

                String rank = String.valueOf(i+1);

                //creating the entry object and putting it into the entries arraylist
                CategoryBudget categoryBudget = new CategoryBudget(
                        rank + ". " + categories_arraylist.get(max_amount_ind),
                        "-$" + String.format("%.2f",max_amount),
                        "/$" + cat_budget_arraylist.get(max_amount_ind));
                category_budget_arraylist.add(categoryBudget);

                difference_arraylist_sorted.add(difference_arraylist.get(max_amount_ind));

                categories_arraylist.remove(max_amount_ind);
                amounts_arraylist.remove(max_amount_ind);
                cat_budget_arraylist.remove(max_amount_ind);
                difference_arraylist.remove(max_amount_ind);
            }

            //puts the arraylist into the listview
            budget_lv.setAdapter(categoryBudgetListAdapter);
            categoryBudgetListAdapter.notifyDataSetChanged();

        }

    }




//    @RequiresApi(api = Build.VERSION_CODES.O)
//    public void build_List() {
//
//        //Get data from database table1
//        LocalDate startdate_selected = startdate;
//        String[] categories_budget_aslist = get_categories_budget_from_Table4();
//        String[] categories_aslist = get_categories_from_Table4();
//        res_cat_amounts = myDb.getCategoricalBudgetDateRange(startdate_selected.minusDays(1),enddate); // (startdate,enddate]
//
//        if(res_cat_amounts!=null){
//
//            category_budget_arraylist = new ArrayList<CategoryBudget>();
//            categoryBudgetListAdapter = new CategoryBudgetListAdapter(view.getContext(),R.layout.adapter_budgetview_layout,category_budget_arraylist);
//
//            while (res_cat_amounts.moveToNext()) {
//                String category = res_cat_amounts.getString(0);
//                String amount = res_cat_amounts.getString(1);
//
//                //find the index of budget of category
//                int ind = 0;
//                for (int i = 0; i < categories_aslist.length; i++){
//                    if (category.equals(categories_aslist[i])){
//                        ind = i;
//                    }
//                }
//
//                String budget = categories_budget_aslist[ind];
//
//                //creating the entry object and putting it into the entries arraylist
//                CategoryBudget categoryBudget = new CategoryBudget(category,"-$" + String.format("%.2f",Double.parseDouble(amount)),budget);
//                category_budget_arraylist.add(categoryBudget);
//
//            }
//            //puts the arraylist into the listview
//            budget_lv.setAdapter(categoryBudgetListAdapter);
//            categoryBudgetListAdapter.notifyDataSetChanged();
//
//        }
//
//    }

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

    public Cursor getDataInRange(LocalDate startdate, LocalDate enddate) {
        return myDb.getDataDateRange(startdate,enddate);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void calculate_and_set_cycleAmount(){
        res = getDataInRange(startdate.minusDays(1),enddate);
        amount_total = 0;
        if (res!=null){
            while(res.moveToNext()){
                //summing the total spent
                double amount = Double.parseDouble(res.getString(2));
                amount_total += amount;
            }
            String text = "-$" + String.format("%.2f",amount_total);
            tv_cycleAmountTotal.setText(text);
        }
    }

    public void calculate_and_set_cycleBudget(int position){
        //get and set cycle budget from table3
        res4 = myDb.get_cycles();
        res4.moveToPosition(position);
        String cycle_budget = res4.getString(2);
        tv_cycleBudgetAmount.setText("Budget: $"+cycle_budget);

        //calculate difference between total amount and cycle budget and set
        double cycle_budget_double = Double.parseDouble(cycle_budget);
        double difference = cycle_budget_double - amount_total;
        String difference_formatted = "$" + String.format("%.2f",difference);

        if(difference>0){
            tv_amountLeft.setText("+" + difference_formatted);
            tv_amountLeft.setTextColor(Color.parseColor("#258C4A"));
        }
        else if (difference<0){
            tv_amountLeft.setText("-$" + difference_formatted.substring(2));
            tv_amountLeft.setTextColor(Color.parseColor("#C62828"));
        }
        else{
            tv_amountLeft.setText(difference_formatted);
            tv_amountLeft.setTextColor(Color.parseColor("#E3BA27"));
        }

    }



    public void onClick_Btn_setBudget () {
        //takes to set budget fragment
        btn_setBudget.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            public void onClick(View v) {
                //starts new fragment "ManageBudFragment"
                ManageBudFragment frag= new ManageBudFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, frag, "manageBudFrag")
                        .addToBackStack(null)
                        .commit();
            }
        });
    }



    public void onClick_itemselectedLv() {
        //Delete/edit selected items in the listview by selecting an item in the list view
        budget_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> a, View v, final int position, long id) {

                String difference = difference_arraylist_sorted.get(position);

                //create a custom alert dialog
                AlertDialog.Builder adb1 = new AlertDialog.Builder(view.getContext());
                TextView msg1 = new TextView(view.getContext());    //create a message Tv for adb1
                TextView title1 = new TextView(view.getContext());    //create a message Tv for adb1

                if (Double.parseDouble(difference) < 0) { //if over budget
                    String msg = "-$" + difference.substring(1);

                    msg1.setText(msg); //the message
                    msg1.setTextColor(Color.parseColor("#C62828"));

                    title1.setText("You have exceeded your budget"); //the title
                }
                else if (Double.parseDouble(difference) > 0 && Double.parseDouble(difference) < 1000000){ //if under budget
                    String msg = "+$" + difference;

                    msg1.setText(msg); //the message
                    msg1.setTextColor(Color.parseColor("#258C4A"));

                    title1.setText("How much you have left:"); //the title
                }
                else if (Double.parseDouble(difference) == 0){ //if difference is 0
                    String msg = "$" + difference;

                    msg1.setText(msg); //the message
                    msg1.setTextColor(Color.parseColor("#E3BA27"));

                    title1.setText("How much you have left:"); //the title
                }
                else { //if difference is 100000 (cat deleted)
                    String msg = "This category was deleted";

                    msg1.setText(msg); //the message
                    msg1.setTextColor(Color.parseColor("#807979"));

                    title1.setText("Notice"); //the title
                }

                msg1.setGravity(Gravity.CENTER);    //center
                msg1.setTextSize(20);
                msg1.setPadding(10, 30, 10, 10);
                title1.setGravity(Gravity.CENTER);    //center
                title1.setTextSize(20);
                title1.setPadding(10, 50, 10, 50);
                title1.setTextColor(Color.BLACK);
//                    adb1.setTitle("Notice");
                adb1.setPositiveButton("Okay", null);
                adb1.setView(msg1);
                adb1.setCustomTitle(title1);
                adb1.show();

            }
        });

    }





    public void onSelect_CycleSpinner() {

        //What the spinner does when item is selected / not selected
        spinner_cycles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View v, final int position, long id) {

                //this is important bc "cycles"/spinner shows new-->old, but in the database table4, it's indexed old--->new
                int inverted_pos = (cycles.size() - 1) - position;

                res4.moveToPosition(inverted_pos);
                startdate = LocalDate.parse(res4.getString(0));
                enddate = LocalDate.parse(res4.getString(1));

                calculate_and_set_cycleAmount();
                calculate_and_set_cycleBudget(inverted_pos);

                onClick_Btn_setBudget();

                build_List(); //builds arraylist to pass into listview
                onClick_itemselectedLv();

            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Object item = adapterView.getItemAtPosition(0);

            }
        });



    }




    @RequiresApi(api = Build.VERSION_CODES.O)
    //updates the start and end date of the cycle
    public void cycle_updater() {

        cycle_input = "01"; //sets the default cycle input as the first of the month
        currentDate = LocalDate.now();

        if (res3!=null && res3.moveToFirst()){  //makes sure table3 is not null
            cycle_input = res3.getString(2);
        }
        else{
            myDb.create_filler_setting_onStartup(cycle_input);
        }

        String currentDate_string = String.valueOf(currentDate);
        String currentMonth_string = ""+ currentDate_string.substring(5,7); //"MM" -- [start ind,end ind)

        String var_string = ""+currentDate_string.substring(0,5) + currentMonth_string + "-" + cycle_input; //variable to compare current date with
        LocalDate var = LocalDate.parse(var_string);    //convert var into a localdate

        //determine and sets the start and end dates of the cycle
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

//******************************************************************************new added


        //dealing with table4 (cycle table) ---- for cycle spinner
        res4 = myDb.get_cycles();
        if (res4!=null && res4.moveToLast()) { //if table4 is not null on startup (run basically every time this fragment is selected)
            String past_startdate = res4.getString(0);
            String past_enddate = res4.getString(1);

            if (!past_startdate.equals(String.valueOf(startdate)) && !past_enddate.equals(String.valueOf(enddate))) { //if a new cycle started (new month)
                res4.moveToLast();
                String cycle_budget = res4.getString(2);
                String categories_list_as_string = res4.getString(3);
                String categories_budget_list_as_string = res4.getString(4);
                //inserts the start and end date of the cycle only if the dates changed
                myDb.insert_new_cycle(String.valueOf(startdate), String.valueOf(enddate), cycle_budget,
                        categories_list_as_string, categories_budget_list_as_string);
            }
        }

        else { //if table4 null (only when first run)

            StringBuilder categories_budget_list_as_string = new StringBuilder();
            StringBuilder categories_list_as_string = new StringBuilder();
            res2 = myDb.getAllData_categories();
            if (res2 != null) { // if categories table3 is not empty
                while (res2.moveToNext()) {
                    String category = res2.getString(1);
                    categories_list_as_string.append(category).append(";");
                    categories_budget_list_as_string.append("0.00").append(";");
                }
            }
            //inserts the start and end date of the cycle only if the dates changed
            myDb.insert_new_cycle(String.valueOf(startdate), String.valueOf(enddate), "0.00",
                    categories_list_as_string.toString(), categories_budget_list_as_string.toString());


        }

        //******************************************************************************new added

    }



}