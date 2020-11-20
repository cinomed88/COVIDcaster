package com.example.covidcaster;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;

import androidx.annotation.Nullable;


import android.app.DownloadManager;


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

public class GraphActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
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

        Toolbar toolbar = findViewById(R.id.toolbar_graph);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout_graph);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawer,
                toolbar,
                R.string.nav_open_drawer,
                R.string.nav_close_drawer);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view_graph);
        navigationView.setNavigationItemSelectedListener(this);

        femaleCases = new ArrayList<>();
        maleCases = new ArrayList<>();
        currentIndex = 0;
        String statisticName = getIntent().getExtras().getString("statisticName");
        statName = findViewById(R.id.statisticCategory);
        barChart = findViewById(R.id.chart);
        getData();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Intent intent = null;

        switch(id) {
            case R.id.nav_map:
                intent = new Intent(this, RegionalDataActivity.class);
                break;
            case R.id.nav_graph:
                intent = new Intent(this, DataActivity.class);
                break;
            case R.id.nav_news:
                intent = new Intent(this, NewsActivity.class);
                break;
            case R.id.nav_help:
                intent = new Intent(this, HelpActivity.class);
                break;
            default:
                intent = new Intent(this, MainActivity.class);
        }
        startActivity(intent);

        DrawerLayout drawer = findViewById(R.id.drawer_layout_graph);
        drawer.closeDrawer(GravityCompat.START);
        return true;

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout_graph);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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
                        // TODO: Handle error

                    }
                });

        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }
}