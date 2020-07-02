package com.example.moneymanagement3.ui.budget;

import android.annotation.SuppressLint;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;

public class BudgetFragment extends Fragment {

    //cycle updater variables
    DataBaseHelper myDb;
    Cursor res3; Cursor res4; Cursor res;
    LocalDate startdate; LocalDate enddate; LocalDate currentDate;
    String cycle_input;
    ArrayList<String> cycles;
    Spinner spinner_cycles;
    /////
    Button btn_setBudget;
    double amount_total;
    TextView tv_cycleAmountTotal; TextView tv_cycleBudgetAmount; TextView tv_amountLeft;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget, container, false);

        //get database
        myDb = new DataBaseHelper(getActivity());

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

            String formatted_dates = cyc_startdate_formatted + " - " + cyc_enddate_formatted;
            cycles.add(formatted_dates);
        }
        Collections.reverse(cycles);
        ArrayAdapter<String> spn_cyc_adapter = new ArrayAdapter<String>(view.getContext(), R.layout.spinner_text,cycles);
        spinner_cycles.setAdapter(spn_cyc_adapter);

        //------------------------------------------------END-----------------------------------------------//

        btn_setBudget = view.findViewById(R.id.setBudgetBtn);
        tv_cycleAmountTotal = view.findViewById(R.id.totalTv);
        tv_cycleBudgetAmount = view.findViewById(R.id.cycleBudgetAmountTv);
        tv_amountLeft = view.findViewById(R.id.cycleAmountLeftTv);



        calculate_and_set_cycleAmount();
        calculate_and_set_cycleBudget();

        onClick_Btn_setBudget();


        onSelect_CycleSpinner();

        return view;
    }


//////////////////////////////////////////////////////////////////////////////////////////////////////

    public Cursor getDataInRange(LocalDate startdate, LocalDate enddate) {
        return myDb.getDataDateRange(startdate,enddate);
    }

    public void calculate_and_set_cycleAmount(){
        res = getDataInRange(startdate,enddate);
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

    public void calculate_and_set_cycleBudget(){
        //get and set cycle budget from table3
        res3 = myDb.get_setting();
        res3.moveToFirst();
        String cycle_budget = res3.getString(3);
        tv_cycleBudgetAmount.setText("/$"+cycle_budget);

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
                calculate_and_set_cycleBudget();


                onClick_Btn_setBudget();



//                build_List(); //builds arraylist to pass into listview
//                onClick_itemselectedLv();

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

        //dealing with table4 (cycle table) ---- for cycle spinner
        res4 = myDb.get_cycles();
        if (res4!=null && res4.moveToLast()){  //makes sure table3 is not null
            String past_startdate = res4.getString(0);
            String past_enddate = res4.getString(1);
            if (!past_startdate.equals(String.valueOf(startdate))  &&  !past_enddate.equals(String.valueOf(enddate))   ){
                //inserts the start and end date of the cycle only if the dates changed
                myDb.insert_cycle(String.valueOf(startdate),String.valueOf(enddate));
            }
        }
        else {
            //inserts the start and end date of the cycle for when the first time app is run
            myDb.insert_cycle(String.valueOf(startdate),String.valueOf(enddate));
        }

    }



}