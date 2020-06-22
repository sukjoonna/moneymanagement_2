package com.example.moneymanagement3;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.moneymanagement3.ui.budget.BudgetFragment;
import com.example.moneymanagement3.ui.chart.ChartFragment;
import com.example.moneymanagement3.ui.home.HomeFragment;
import com.example.moneymanagement3.ui.setting.SettingFragment;
import com.example.moneymanagement3.ui.tracker.TrackerFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.nav_view); //from activity_main.xml
        //Calls function for when menu item is selected from the nav menu
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        //Set the bottom nav menu to "Add" on startup
        int homefragment_itemID = R.id.navigation_home; //from bottom_nav_menu.xml
        bottomNav.setSelectedItemId(homefragment_itemID);

    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;


                    switch (item.getItemId()) {
                        case R.id.navigation_chart:
                            selectedFragment = new ChartFragment();
                            break;
                        case R.id.navigation_budget:
                            selectedFragment = new BudgetFragment();
                            break;
                        case R.id.navigation_home:
                            selectedFragment = new HomeFragment();
                            break;
                        case R.id.navigation_tracker:
                            selectedFragment = new TrackerFragment();
                            break;
                        case R.id.navigation_setting:
                            selectedFragment = new SettingFragment();
                            break;
                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            selectedFragment).commit();

                    return true;
                }
            };

}