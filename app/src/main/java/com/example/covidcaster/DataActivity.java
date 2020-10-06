package com.example.covidcaster;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
public class DataActivity extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);
        ListView statisticsList = findViewById(R.id.statisticsList);
        statisticsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String[] statistics = {"Active Cases", "Hospitalized", "ICU", "Age", "TAT"};
                if (i==0 || i==1 || i==2 || i==3|| i==4) {
                    Intent intent = new Intent(DataActivity.this, GraphActivity.class);
                    intent.putExtra("statisticName", statistics[i]);
                    startActivity(intent);
                }
            }
        });

    }
}
