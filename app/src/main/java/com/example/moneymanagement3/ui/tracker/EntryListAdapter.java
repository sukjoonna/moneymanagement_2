package com.example.moneymanagement3.ui.tracker;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
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
        String category = getItem(position).getCategory(); //in effect this textview will include category and payment type
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
        tv_entry_amount.setText(amount);
        tv_date.setText(date);

        //bold just the category and italicize the payment type
        int ind = category.indexOf("|");
        SpannableStringBuilder cat_payment_span = new SpannableStringBuilder(category);

        if (ind ==-1){
            ind = category.length();
            cat_payment_span.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, ind, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        else{
            cat_payment_span.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, ind, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            cat_payment_span.setSpan(new android.text.style.StyleSpan(Typeface.ITALIC), ind+1, category.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        tv_category.setText(cat_payment_span);

        return convertView;

    }


}
