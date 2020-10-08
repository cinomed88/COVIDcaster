package com.example.covidcaster;

import androidx.appcompat.app.AppCompatActivity;
import android.content.pm.ActivityInfo;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Don't allow landscape screen
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    public void OnClickRegionalData(View v) {
        Intent intent = new Intent(MainActivity.this, RegionalDataActivity.class);
        startActivity(intent);
    }

    public void onDataClick(View v) {
        Intent intent = new Intent(MainActivity.this, DataActivity.class);
        startActivity(intent);
    }


}