package com.example.moneymanagement3.ui.tracker;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.moneymanagement3.R;

import java.util.List;

public class EntryListAdapter extends ArrayAdapter<Entry> {

    private static final String TAG = "EntryListAdapter";
    private Context mContext;
    int mResource;


    public EntryListAdapter(@NonNull Context context, int resource, @NonNull List<Entry> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //get the entry information
        String description = getItem(position).getDescription();
        String category = getItem(position).getCategory();
        String amount = getItem(position).getAmount();
        String date = getItem(position).getDate();

        //create the entry object to hold the information
        Entry entry = new Entry(description,category,amount,date);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource,parent,false);

        TextView tv_description = convertView.findViewById(R.id.descriptionTv);
        TextView tv_category = convertView.findViewById(R.id.categoryTv);
        TextView tv_entry_amount = convertView.findViewById(R.id.entry_amountTv);
        TextView tv_date = convertView.findViewById(R.id.dateTv);

        tv_description.setText(description);
        tv_category.setText(category);
        tv_entry_amount.setText(amount);
        tv_date.setText(date);

        return convertView;

    }


}
