package com.example.covidcaster;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.LruCache;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    String URL = "https://services1.arcgis.com/xeMpV7tU1t4KD3Ei/ArcGIS/rest/services/COVID19_Cases_by_BC_Health_Authority/FeatureServer/0/query?where=FID+%3C+7&objectIds=&time=&geometry=&geometryType=esriGeometryEnvelope&inSR=&spatialRel=esriSpatialRelIntersects&resultType=none&distance=0.0&units=esriSRUnit_Meter&returnGeodetic=false&outFields=*&returnGeometry=false&returnCentroid=false&featureEncoding=esriDefault&multipatchOption=xyFootprint&maxAllowableOffset=&geometryPrecision=&outSR=&datumTransformation=&applyVCSProjection=false&returnIdsOnly=false&returnUniqueIdsOnly=false&returnCountOnly=false&returnExtentOnly=false&returnQueryGeometry=false&returnDistinctValues=false&cacheHint=false&orderByFields=&groupByFieldsForStatistics=&outStatistics=&having=&resultOffset=&resultRecordCount=&returnZ=false&returnM=false&returnExceededLimitFeatures=false&quantizationParameters=&sqlFormat=none&f=pjson&token=";

    TextView tvUpdateTime;
    TextView tvTotal;
    TextView tvNew;
    TextView tvActive;
    TextView tvDeaths;
    TextView tvCurrentHosp;
    TextView tvHospitalized;
    TextView tvCurrentlyICU;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the drawer
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawer,
                toolbar,
                R.string.nav_open_drawer,
                R.string.nav_close_drawer);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Set Navigation Listener
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        tvUpdateTime = findViewById(R.id.updateTime);
        tvTotal = findViewById(R.id.totalCasesNum);
        tvActive = findViewById(R.id.activeCasesNum);
        tvNew = findViewById(R.id.newCasesNum);
        tvDeaths = findViewById(R.id.DeathsNum);
        tvCurrentHosp = findViewById(R.id.currentHospNum);
        tvHospitalized = findViewById(R.id.hospitalizedNum);
        tvCurrentlyICU = findViewById(R.id.currentICUNum);

        getData();

    }

    public void OnClickRegionalData(View v) {
        Intent intent = new Intent(MainActivity.this, RegionalDataActivity.class);
        startActivity(intent);
    }

    public void OnClickCollectionCentre(View v) {
        Intent intent = new Intent(MainActivity.this, CollectionCentreActivity.class);
        startActivity(intent);
    }

    public void onDataClick(View v) {
        Intent intent = new Intent(MainActivity.this, DataActivity.class);
        String totalCases = tvTotal.getText().toString();
        String activeCases = tvActive.getText().toString();
        String deaths = tvDeaths.getText().toString();
        intent.putExtra("totalCases",totalCases);
        intent.putExtra("activeCases", activeCases);
        intent.putExtra("deaths", deaths);
        startActivity(intent);
    }

    public void onNewsClick(View v) {
        Intent intent = new Intent(this, NewsActivity.class);
        startActivity(intent);
    }

    // Get JSON data and parse them
    public void getData() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray features = (JSONArray) response.get("features");
                            int totalCases = 0;
                            int totalActiveCases = 0;
                            int totalNewCases = 0;
                            int totalRecovered = 0;
                            int totalDeaths = 0;
                            int totalHospitalized = 0;
                            int totalCurrentlyHosp = 0;
                            int totalEverICU = 0;
                            int totalCurrentlyICU = 0;

                            for (int i = 0; i < features.length(); i++) {
                                JSONObject data = (JSONObject) features.get(i);
                                JSONObject region = (JSONObject) data.get("attributes");
                                String region_name = region.getString("HA_Name");
                                int cases = region.getInt("Cases");
                                int activeCases = region.getInt("ActiveCases");
                                int newCases = region.getInt("NewCases");
                                int recovered = region.getInt("Recovered");
                                int deaths = region.getInt("Deaths");
                                int hospitalized = region.getInt("Hospitalized");
                                int currentlyHosp = region.getInt("CurrentlyHosp");
                                int everICU = region.getInt("EverICU");
                                int currentlyICU = region.getInt("CurrentlyICU");

                                totalCases += cases;
                                totalActiveCases += activeCases;
                                totalNewCases += newCases;
                                totalRecovered += recovered;
                                totalDeaths += deaths;
                                totalHospitalized += hospitalized;
                                totalCurrentlyHosp += currentlyHosp;
                                totalEverICU += everICU;
                                totalCurrentlyICU += currentlyICU;

                            }
                            // calculate total numbers of the statistics
                            tvTotal.setText(Integer.toString(totalCases));
                            tvActive.setText(Integer.toString(totalActiveCases));
                            tvNew.setText(Integer.toString(totalNewCases));
                            tvDeaths.setText(Integer.toString(totalDeaths));
                            tvCurrentHosp.setText(Integer.toString(totalCurrentlyHosp));
                            tvHospitalized.setText(Integer.toString(totalHospitalized));
                            tvCurrentlyICU.setText(Integer.toString(totalCurrentlyICU));

                            // get update time
                            JSONObject data = (JSONObject) features.get(0);
                            JSONObject region_zero = (JSONObject)data.get("attributes");
                            long update_time = region_zero.getLong("Date_Updat");
                            Date d = new Date(update_time);
                            Calendar cal = Calendar.getInstance();
                            cal.setTimeInMillis(update_time);
                            cal.add(Calendar.HOUR, -8);
                            DateFormat df = new SimpleDateFormat("MM/dd/yyyy, aa h:mm");
                            tvUpdateTime.setText(df.format(cal.getTime()));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error", error.toString());
                    }
                });

        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Intent intent = null;

        switch(id) {
            case R.id.nav_map:
                intent = new Intent(this, RegionalDataActivity.class);
                break;
            case R.id.nav_cc:
                intent = new Intent(this, CollectionCentreActivity.class);
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

        // Set the navigation drawer behavior
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

}
