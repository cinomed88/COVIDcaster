package com.example.covidcaster;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class DataActivity extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);
        ListView statisticsList = findViewById(R.id.statisticsList);
        TextView tvTotal = findViewById(R.id.numberOfCases);
        TextView tvRecovered = findViewById(R.id.recoverNumber);
        TextView tvDeaths = findViewById(R.id.deathNumber);
        String totalCases = getIntent().getExtras().getString("totalCases");
        String activeCases = getIntent().getExtras().getString("activeCases");
        String deaths = getIntent().getExtras().getString("deaths");
        if (totalCases != null && activeCases != null && deaths != null) {
            int recovered = Integer.parseInt(totalCases) - Integer.parseInt(activeCases);
            tvTotal.setText(totalCases);
            tvRecovered.setText(Integer.toString(recovered));
            tvDeaths.setText(deaths);
        }
        statisticsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String[] statistics = {"Active Cases", "Hospitalized", "ICU", "Age", "TAT"};
                Intent intent = new Intent(DataActivity.this, GraphActivity.class);
                intent.putExtra("statisticName", statistics[i]);
                startActivity(intent);
            }
        });

    }
}
