package com.example.moneymanagement3.ui.chart;

import android.view.View;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.example.moneymanagement3.R;

public class InfoFragment extends Fragment {
    Button btn1;









    public void onClick_GoBackBtn () {
        //Button to go back to settings
        btn1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ChartFragment frag= new ChartFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, frag, "piechartFrag")
                        .commit();
            }
        });
    }
}
