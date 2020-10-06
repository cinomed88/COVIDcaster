package com.example.covidcaster;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class Data extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);
    }

    public static final String[] regions = {
            "Fraser Valley", "Van. Coastal", "Interior", "Northern", "Van. Island", "Out of Canada"
    };

}