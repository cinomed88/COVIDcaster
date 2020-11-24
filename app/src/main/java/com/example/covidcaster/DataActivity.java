package com.example.covidcaster;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DataActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    TextView tvTotal;
    TextView tvDeaths;
    TextView tvRecovered;
    RecyclerView rv;
    List<String> graphNames;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        Toolbar toolbar = findViewById(R.id.toolbar_data);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout_data);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawer,
                toolbar,
                R.string.nav_open_drawer,
                R.string.nav_close_drawer);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view_data);
        navigationView.setNavigationItemSelectedListener(this);


        tvTotal = findViewById(R.id.numberOfCases);
        tvRecovered = findViewById(R.id.recoverNumber);
        tvDeaths = findViewById(R.id.deathNumber);
        Calendar cal = Calendar.getInstance();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CANADA);
        String todayDate = dateFormat.format(cal.getTime());
        cal.add(Calendar.DATE, -1);
        String yesterdaysDate = dateFormat.format(cal.getTime());
        getData(todayDate, yesterdaysDate);
        rv = findViewById(R.id.graphNamesCards);
        GridLayoutManager glm = new GridLayoutManager(getBaseContext(), 2);
        rv.setLayoutManager(glm);
        Resources res = getResources();
        String[] graphs = res.getStringArray(R.array.statisticsList);
        graphNames = new ArrayList<>();
        graphNames.addAll(Arrays.asList(graphs));
        GraphNameView adapter = new GraphNameView(graphNames);
        rv.setAdapter(adapter);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Intent intent = null;

        switch(id) {
            case R.id.nav_map:
                intent = new Intent(this, RegionalDataActivity.class);
                break;
            case R.id.nav_home:
                intent = new Intent(this, MainActivity.class);
                break;
            case R.id.nav_cc:
                intent = new Intent(this, CollectionCentreActivity.class);
                break;
            case R.id.nav_news:
                intent = new Intent(this, NewsActivity.class);
                break;
            case R.id.nav_help:
                intent = new Intent(this, HelpActivity.class);
                break;
            default:
                intent = new Intent(this, DataActivity.class);
        }
        startActivity(intent);

        DrawerLayout drawer = findViewById(R.id.drawer_layout_data);
        drawer.closeDrawer(GravityCompat.START);
        return true;

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout_data);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void getData(String today, String yesterday) {
        String URL = "https://services9.arcgis.com/pJENMVYPQqZZe20v/arcgis/rest/services/" +
                "province_daily_totals/FeatureServer/0/query?where=Abbreviation%20%3D%20'BC" +
                "'%20AND%20SummaryDate%20%3E%3D%20TIMESTAMP%20'"+ yesterday +"%2000%3A00%3A00" +
                "'%20AND%20SummaryDate%20%3C%3D%20TIMESTAMP%20'"+ today +"%2000%3A00%3A00'" +
                "&outFields=TotalRecovered,TotalCases,TotalDeaths&outSR=4326&f=json";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int totalCases = 0;
                            int totalDeaths = 0;
                            int totalRecovered = 0;
                            JSONArray features =(JSONArray) response.get("features");
                            JSONObject attributesObj = (JSONObject) features.get(0);
                            JSONObject attributes = (JSONObject) attributesObj.get("attributes");
                            int deaths = (Integer) attributes.get("TotalDeaths");
                            int recovered = (Integer) attributes.get("TotalRecovered");
                            int cases = (Integer) attributes.get("TotalCases");
                            totalCases += cases;
                            totalDeaths += deaths;
                            totalRecovered += recovered;
                            tvTotal.setText(Integer.toString(totalCases));
                            tvRecovered.setText(Integer.toString(totalRecovered));
                            tvDeaths.setText(Integer.toString(totalDeaths));
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
