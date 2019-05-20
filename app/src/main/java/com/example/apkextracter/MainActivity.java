package com.example.apkextracter;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(findViewById(R.id.container)!=null)
        {
            if(savedInstanceState != null)
            {
                return;
            }
            AppListFragment listFragment = new AppListFragment();
            listFragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container,listFragment)
                    .commit();
        }
    }
}
