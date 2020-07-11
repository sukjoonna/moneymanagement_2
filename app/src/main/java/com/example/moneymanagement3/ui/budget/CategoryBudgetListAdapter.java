package com.example.moneymanagement3.ui.budget;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.moneymanagement3.R;

import java.util.ArrayList;
import java.util.List;

public class CategoryBudgetListAdapter extends ArrayAdapter<CategoryBudget> {

    private static final String TAG = "CategoryBudgetListAdapter";
    private Context mContext;
    int mResource;


    public CategoryBudgetListAdapter(@NonNull Context context, int resource, @NonNull List<CategoryBudget> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //get the entry information
        String category = getItem(position).getCategory();
        String amount = getItem(position).getAmount();
        String budget = getItem(position).getBudget();

        //create the entry object to hold the information
        CategoryBudget categoryBudget = new CategoryBudget(category,amount,budget);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource,parent,false);

        TextView tv_category = convertView.findViewById(R.id.categoryTv);
        TextView tv_entry_amount = convertView.findViewById(R.id.entry_amountTv);
        TextView tv_budget = convertView.findViewById(R.id.budgetTv);

        tv_category.setText(category);
        tv_entry_amount.setText(amount);
        tv_budget.setText(budget);

        return convertView;

    }


}
