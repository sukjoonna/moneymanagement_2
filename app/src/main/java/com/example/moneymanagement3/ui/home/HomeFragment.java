package com.example.moneymanagement3.ui.home;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import com.example.moneymanagement3.MyDate;
import com.example.moneymanagement3.R;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {

    //Class - Variable creations
    DataBaseHelper myDb;
    EditText et_name, et_amount;
    Button btn_add;
    ArrayList<String> categories;
    Spinner spn_category;
    String category; String currentDate; String month; String year; String current_month; String current_year;
    Cursor res2; Cursor res3;
    View view;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);

        //Variable declarations
        et_name = view.findViewById(R.id.textEt);     //edit text
        et_amount = view.findViewById(R.id.amountEt);
        btn_add = view.findViewById(R.id.add1Btn);   //add button
        spn_category = view.findViewById(R.id.categorySpn);

        //creates database
        myDb = new DataBaseHelper(getActivity());
        //Cursor res = gets all data in the database table2 and table3
        res2 = myDb.getAllData_categories();
        //res3 = myDb.get_MonthAndYear();


        //get current date
        currentDate = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(new Date());

//        //get previous date
//        res3.moveToNext();
//        month = res3.getString(1);
//        year = res3.getString(0);



//        //update the month and year in database table3
//        update_MonthAndYear(month,year);


        //creates the categories ArrayList and adds the categories from the database table2
        categories = new ArrayList<String>();
        categories.add(0,"SELECT CATEGORY:");  //this is created as default to prevent bugs when there are no categories
        while (res2.moveToNext()) {
            String category = res2.getString(1);
            categories.add(category);
        }
        categories.add("+Add New");


        //Creating the categories spinner using spinner_of_categories xml file in layout
        ArrayAdapter<String> spn_adapter = new ArrayAdapter<String>(view.getContext(), R.layout.spinner_of_categories,categories);
        spn_category.setAdapter(spn_adapter);
        spn_adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

        onSelect_Spinner(); //function call for selecting item in spinner_category

        onClick_addBtn();   //function call for clicking the add button

        return view;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


//Public Functions

//    public void update_MonthAndYear(String month, String year){
//        current_month = MyDate.getMonth();
//        current_year = MyDate.getYear();
//        if (month==null && year==null){
//            myDb.replace_MonthAndYear(current_month,current_year);
//        }
//
//    }

    public void onClick_addBtn () {
        //Button to add data in database
        btn_add.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                String text1 = et_name.getText().toString();     //gets text from edit text widget: name
                String text2 = et_amount.getText().toString();  //gets text from edit text widget: amount
                String text3 = category;
                String text4 = currentDate;
                Timestamp text5 = new Timestamp(System.currentTimeMillis());


                //if "+Add New" or "SELECT CATEGORY:" is selected for category, display a warning message
                if (category.equals("+Add New") || category.equals("SELECT CATEGORY:")) {
                    //create an alert dialog
                    AlertDialog.Builder adb1 =new AlertDialog.Builder(view.getContext());
                    TextView msg1 = new TextView(view.getContext());    //create a message Tv for adb1
                    msg1.setText(R.string.select_category); //the message
                    msg1.setGravity(Gravity.CENTER);    //center
                    msg1.setTextSize(20);
                    msg1.setPadding(10, 5, 10, 20);
                    msg1.setTextColor(Color.GRAY);
                    TextView title1 = new TextView(view.getContext());    //create a message Tv for adb1
                    title1.setText("Notice"); //the title
                    title1.setGravity(Gravity.CENTER);    //center
                    title1.setTextSize(30);
                    title1.setPadding(10, 40, 10, 50);
                    title1.setTextColor(Color.BLACK);
//                    adb1.setTitle("Notice");
                    adb1.setPositiveButton("Okay", null);
                    adb1.setView(msg1);
                    adb1.setCustomTitle(title1);
                    adb1.show();
                }
                //if text1 or text2 is empty
                else if (text1.equals("")||text2.equals("")){
                    //create an alert dialog
                    AlertDialog.Builder adb2 =new AlertDialog.Builder(view.getContext());
                    adb2.setTitle("Notice");
                    adb2.setMessage("Enter all information");
                    adb2.setNeutralButton("Okay", null);
                    adb2.show();
                }
                //otherwise, store all the inputed values into database table1
                else {
                    boolean isInserted = myDb.insertData(text1, text2, text3, text4,text5); //insert data into database

                    if (isInserted == true)
                        Toast.makeText(view.getContext(), "Data Inserted", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(view.getContext(), "Data not Inserted", Toast.LENGTH_SHORT).show();

                    //clears the edit texts
                    et_name.getText().clear();
                    et_amount.getText().clear();
                }

            }
        });
    }

    public void onSelect_Spinner() {
        //What the spinner does when item is selected / not selected
        spn_category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View v, final int position, long id) {
                final int position_ind = position;
                //stores the selected category into a String var
                Object item = adapterView.getItemAtPosition(position);
                category = item.toString();

                //if "+Add New" is selected
                if (category.equals("+Add New")){
                    //create an alert dialog
                    AlertDialog.Builder adb=new AlertDialog.Builder(view.getContext());

                    //create an edit text in this alert dialog by linking to add_new_category xml file
                    LayoutInflater inflater = getLayoutInflater();
                    final View view = inflater.inflate(R.layout.add_new_category,null); //xml file used
                    final EditText et_addcategory = view.findViewById(R.id.add_categoryEt);

                    adb.setTitle("Alert");
                    adb.setMessage("Type in a new category");
                    adb.setView(view);
                    //cancel button
                    adb.setNeutralButton("Cancel", new AlertDialog.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //When cancel is pressed, spinner set to 0 so you can selected add new again
                            spn_category.setSelection(0);
                        }
                    });
                    //Add new category button
                    adb.setPositiveButton("Add", new AlertDialog.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            String new_category = et_addcategory.getText().toString();
                            //Checks if it is empty or not
                            if (new_category.equals("")){
                                Toast.makeText(view.getContext(), "Nothing was Added", Toast.LENGTH_SHORT).show();
                                spn_category.setSelection(0);
                            }
                            else{
                                //Adds the new category to spinner
                                categories.add(position_ind,new_category);

                                //Add the new category to database
                                myDb.insertData_categories(new_category);

                                //after new category is added, the spinner is set to 0
                                spn_category.setSelection(0);
                            }

                        }
                    });
                    adb.show();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Object item = adapterView.getItemAtPosition(0);
                category = item.toString();
            }
        });

    }


}