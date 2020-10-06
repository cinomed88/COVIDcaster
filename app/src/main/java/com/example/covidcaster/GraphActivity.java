package com.example.covidcaster;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public class GraphActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        String statisticName = getIntent().getExtras().getString("statisticName");
        TextView statName = findViewById(R.id.statisticCategory);
        statName.setText(statisticName);
    }


}