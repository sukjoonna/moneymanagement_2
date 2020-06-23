package com.example.moneymanagement3;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CycleManager {

    String current_date;

    public void create_cycle(){
        current_date = new SimpleDateFormat("MM-dd-yyyy",Locale.getDefault()).format(new Date());


    }



}
