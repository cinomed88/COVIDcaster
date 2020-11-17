package com.example.covidcaster;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GraphActivity extends AppCompatActivity {
    float currentCount;
    int currentIndex;
    BarChart barChart;
    List<BarEntry> barEntries;
    ArrayList<String> barLabels;
    List<IBarDataSet> dataSets;
    List<JSONObject> femaleCases;
    List<JSONObject> maleCases;
    TextView statName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        femaleCases = new ArrayList<>();
        maleCases = new ArrayList<>();
        currentIndex = 0;
        String statisticName = getIntent().getExtras().getString("statisticName");
        statName = findViewById(R.id.statisticCategory);
        barChart = findViewById(R.id.chart);
        getData();
    }

    public void getData() {
        String URL = "https://services9.arcgis.com/pJENMVYPQqZZe20v/arcgis/rest/services/" +
                "Canada_COVID19_Case_Details/FeatureServer/0/query?where=province_abbr%20%3D%20" +
                "'BC'&outFields=age_group,date_reported,gender&outSR=4326&f=json";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
                    public void displayGenderGraph() {
                        List<BarEntry> femaleEntries = new ArrayList<>();
                        List<BarEntry> maleEntries = new ArrayList<>();
                        femaleEntries.add(new BarEntry(0, (float) maleCases.size()));
                        maleEntries.add(new BarEntry(1, (float) femaleCases.size()));
                        BarDataSet set = new BarDataSet(femaleEntries, "Female");
                        BarDataSet set1 = new BarDataSet(maleEntries, "Male");
                        List<IBarDataSet> dataSets = new ArrayList<>();
                        dataSets.add(set);
                        dataSets.add(set1);
                        BarData data = new BarData(dataSets);
                        data.setBarWidth(0.9f);
                        barChart.setData(data);
                        barChart.setFitBars(true);
                        barChart.invalidate();
                    }

                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            JSONArray features = (JSONArray) response.get("features");
                            for (int index = 0; index < features.length(); index++) {
                                JSONObject data = (JSONObject) features.get(index);
                                JSONObject attributes = (JSONObject) data.get("attributes");
                                if (attributes.get("gender").equals("Female")) {
                                    femaleCases.add(attributes);
                                }
                                if (attributes.get("gender").equals("Male")) {
                                    maleCases.add(attributes);
                                }
                            }
                            displayGenderGraph();


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });

        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }
}