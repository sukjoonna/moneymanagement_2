package com.example.moneymanagement3.ui.setting;

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
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.moneymanagement3.DataBaseHelper;
import com.example.moneymanagement3.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ManageCatFragment extends Fragment {

    View view;
    DataBaseHelper myDb;
    Cursor res;
    Cursor res2; Cursor res4; Cursor res3;
    ListView lv;
    ArrayList<String> categories;
    String[] managecat_items;
    ArrayAdapter<String> adapter_managecat;
    Button btn1;
    CharSequence[] categories_list;
    String new_category;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_managecat, container, false);
        view.setBackgroundColor(Color.WHITE);

        myDb = new DataBaseHelper(getActivity());
        //Cursor res = gets all data in the database table2
        res2 = myDb.getAllData_categories();

        btn1 = view.findViewById(R.id.gobackBtn);
        lv = view.findViewById(R.id.manageCatLv);


        managecat_items = new String[]{"Add Category","Categories to delete", "Delete all categories"};
        adapter_managecat = new ArrayAdapter<String>(view.getContext(), R.layout.manage_listview_text, R.id.manage_item, managecat_items);
        lv.setAdapter(adapter_managecat);


        onClick_itemselectedLv();
        onClick_GoBackBtn ();


        return view;
    }

////////////////////////////////////////////////////////////////////////////////////////////////////

    public void onClick_itemselectedLv() {
        //Delete/edit selected items in the manageCat listview by selecting an item in the list view
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> a, View v, final int position, long id) {

                if(position==0){ //if "add category" is selected
                    //create an alert dialog
                    AlertDialog.Builder adb = new AlertDialog.Builder(view.getContext());

                    //create an edit text in this alert dialog by linking to add_new_category xml file
                    LayoutInflater inflater = getLayoutInflater();
                    final View view = inflater.inflate(R.layout.add_new_category, null); //xml file used
                    final EditText et_addcategory = view.findViewById(R.id.add_categoryEt);

                    adb.setTitle("Alert");
                    adb.setMessage("Type in a new category");
                    adb.setView(view);
                    //cancel button
                    adb.setNeutralButton("Cancel", null);
                    //Add new category button
                    adb.setPositiveButton("Add", new AlertDialog.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            new_category = et_addcategory.getText().toString();
                            //Checks if it is empty or not
                            if (new_category.equals("")) {
                                Toast.makeText(view.getContext(), "Nothing was Added", Toast.LENGTH_SHORT).show();
                            } else {
                                //Adds the new category to spinner
//                                categories.add(position_ind, new_category);
//                                categories.add(categories.size()-1,new_category);
                                //Add the new category to database table 3
                                myDb.insertData_categories(new_category);
//---------------------------------
                                res4 = myDb.get_cycles();
                                StringBuilder categories_budget_list_as_string = new StringBuilder();
                                int categories_budget_list_length = 0;
                                if (res4.moveToLast()){
                                    categories_budget_list_as_string = new StringBuilder(res4.getString(4));
                                }
                                StringBuilder categories_list_as_string = new StringBuilder();
                                String[] categories_budget_list = categories_budget_list_as_string.toString().split("\\;");
                                int categories_list_length = 0;

                                res2 = myDb.getAllData_categories();
                                while (res2.moveToNext()) {
                                    String category = res2.getString(1);
                                    categories_list_as_string.append(category).append(";");
                                    categories_list_length++;
                                }

                                if (!categories_budget_list[0].equals("")){
                                    categories_budget_list_length = categories_budget_list.length;
                                }

                                int list_size_difference = categories_list_length - (categories_budget_list_length);
                                for (int i = 0; i < list_size_difference; i++) {
                                    categories_budget_list_as_string.append("0.00;");
                                }

                                //updates table4
                                res3 = myDb.get_setting();
                                res3.moveToFirst();
                                String current_startdate = res3.getString(0);
                                myDb.update_cycles_table_Category(current_startdate, categories_list_as_string.toString());

                                myDb.update_cycles_table_CatBudget(current_startdate, categories_budget_list_as_string.toString());
//----------------------------------
                                //recreates SettingFragment so the checkbox list appears again after alertdialog closes
                                getFragmentManager()
                                        .beginTransaction()
                                        .detach(ManageCatFragment.this)
                                        .attach(ManageCatFragment.this)
                                        .commit();
                                dialog.dismiss();


                            }

                        }
                    });
                    adb.show();

                }

                //if "categories to delete" is selected
                else if (position == 1) {
                    //creates the categories arraylist from database table2
                    categories = new ArrayList<String>();
                    while (res2.moveToNext()) {
                        String category = res2.getString(1); //from database table2
                        categories.add(category);
                    }

                    //creates boolean array of falses
                    final boolean[] bool_list = new boolean[categories.size()];
                    //converts categories arraylist to char sequence array
                    categories_list = new CharSequence[categories.size()];
                    for (int i = 0; i < categories.size(); i++) {
                        categories_list[i] = categories.get(i);
                    }

                    //if there are no categories, show an alert saying "there are no categories"
                    if (categories_list.length == 0) {
                        AlertDialog.Builder builder0 = new AlertDialog.Builder(view.getContext());
                        builder0.setTitle("Alert");
                        builder0.setMessage("There are no categories");
                        builder0.setPositiveButton("Okay", null);
                        builder0.show();
                    }

                    //otherwise
                    else {
                        //create an alert dialog1 builder - "Manage Categories"
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(view.getContext());
                        builder1.setTitle("Select Categories to Delete");
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

                                        //********************************************************** new added

                                        //converts categories budget string in table4 into arraylist
                                        res4 = myDb.get_cycles();
                                        res4.moveToLast();
                                        String cat = res4.getString(3);
                                        String categories_budget = res4.getString(4);
                                        String[] cat_list = cat.split("\\;");
                                        String[] categories_budget_list = categories_budget.split("\\;");
                                        ArrayList<String> cat_arr = new ArrayList<>();
                                        ArrayList<String> categories_budget_arr = new ArrayList<>();

                                        //deletes the categories from database table2 if the boxes are checked
                                        for (int i = 0; i < categories_list.length; i++) {
                                            boolean checked = bool_list[i];
                                            if (checked) {
                                                res2.moveToPosition(i); //move to correct row in table2
                                                String category_id = res2.getString(0); //get the ID of category
                                                myDb.delete_categories(category_id);
                                            }
                                            else{
                                                //build new array of categories to remain after deleting
                                                cat_arr.add(cat_list[i]);
                                                categories_budget_arr.add(categories_budget_list[i]);
                                            }
                                        }

                                        //updates the categories budget col in table4
                                        StringBuilder new_cat_list = new StringBuilder();
                                        StringBuilder new_cat_bud_list = new StringBuilder();
                                        for (int i = 0; i < categories_budget_arr.size(); i++){
                                            new_cat_bud_list.append(categories_budget_arr.get(i)).append(";");
                                            new_cat_list.append(cat_arr.get(i)).append(";");
                                        }
                                        //get startdate of current cycle
                                        res3 = myDb.get_setting();
                                        res3.moveToFirst();
                                        String startdate = res3.getString(0);

                                        myDb.update_cycles_table_Category(startdate,new_cat_list.toString());
                                        myDb.update_cycles_table_CatBudget(startdate,new_cat_bud_list.toString());

                                        //********************************************************** new added


                                        if(cat_arr.size()==cat_list.length){
                                            Toast.makeText(view.getContext(),"Select Categories",Toast.LENGTH_SHORT).show();
                                        }
                                        else{
                                            //recreates SettingFragment so the checkbox list appears again after alertdialog closes
                                            getFragmentManager()
                                                    .beginTransaction()
                                                    .detach(ManageCatFragment.this)
                                                    .attach(ManageCatFragment.this)
                                                    .commit();
                                            dialog.dismiss();

                                        }

                                    }
                                });

                                //Cancel button
                                Button neutralButton = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                                neutralButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        //recreates SettingFragment so the checkbox list appears again after alertdialog closes
                                        getFragmentManager()
                                                .beginTransaction()
                                                .detach(ManageCatFragment.this)
                                                .attach(ManageCatFragment.this)
                                                .commit();
                                        dialog.dismiss();
                                    }
                                });
                            }
                        });
                        alertDialog.show();
                        alertDialog.setCanceledOnTouchOutside(false);
                    }
                }

                //if "delete all categories" is selected
                else {

                    ArrayList<String> arr = new ArrayList<String>();
                    while (res2.moveToNext()){
                        arr.add(res2.getString(1));
                    }
                    //if there are no categories, show an alert saying "there are no categories"
                    if (arr.isEmpty()) {
                        AlertDialog.Builder builder0 = new AlertDialog.Builder(view.getContext());
                        builder0.setTitle("Alert");
                        builder0.setMessage("There are no categories");
                        builder0.setPositiveButton("Okay", null);
                        builder0.show();
                    }
                    //otherwise delete all
                    else {
                        AlertDialog.Builder adb = new AlertDialog.Builder(view.getContext());
                        adb.setTitle("Alert");
                        adb.setMessage("Are you sure you want to delete all categories?");
                        adb.setNeutralButton("Cancel", new AlertDialog.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                //recreates SettingFragment so the checkbox list appears again after alertdialog closes
                                getFragmentManager()
                                        .beginTransaction()
                                        .detach(ManageCatFragment.this)
                                        .attach(ManageCatFragment.this)
                                        .commit();

                            }
                        });
                        adb.setPositiveButton("Yes", new AlertDialog.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //deletes all categories in database table2 and restores to default
                                int value = myDb.deleteAll_categories();
                                if(value > 0)
                                    Toast.makeText(view.getContext(),"Deleted all categories",Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(view.getContext(),"Data not Deleted",Toast.LENGTH_SHORT).show();

                            }
                        });
                        adb.show();
                    }

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

