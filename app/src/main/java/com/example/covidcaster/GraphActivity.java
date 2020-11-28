package com.example.covidcaster;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

public class GraphActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    int currentIndex;
    BarChart barChart;
    String currentMonth;
    String selectedStatistic;
    List<Float> selectedList;
    HashMap<String, List<Float>> statisticsMap;
    List<Float> casesByAge;
    List<Float> deathsByMonth;
    List<Float> recoveredByMonth;
    List<Float> activeByMonth;
    List<Float> casesByMonth;
    float femaleCases;
    float maleCases;
    // Display TextViews
    TextView statName;
    TextView tvTotal;
    TextView tvRecovered;
    TextView tvDeaths;
    Spinner spinner;

    List<String> dates;
    List<Float> newCasesByMonth;

    //Case numbers
    float aprilTotalCases;
    float mayTotalCases;
    float juneTotalCases;
    float julyTotalCases;
    float augustTotalCases;
    float septTotalCases;
    float octTotalCases;

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
        currentIndex = 0;
        tvTotal = findViewById(R.id.numberOfCases);
        tvRecovered = findViewById(R.id.recoverNumber);
        tvDeaths = findViewById(R.id.deathNumber);
        Calendar cal = Calendar.getInstance();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CANADA);
        String todayDate = dateFormat.format(cal.getTime());
        cal.add(Calendar.DATE, -1);
        String yesterdaysDate = dateFormat.format(cal.getTime());
        getLabelData(todayDate, yesterdaysDate);
        String statisticName = getIntent().getExtras().getString("statisticName");
        selectedStatistic = "Total Cases by Month";
        spinner = findViewById(R.id.spinner);

        if (statisticName != null && statisticName.equals("Totals by Month")) {

            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.Totals, android.R.layout.simple_list_item_1);
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView,
                                           int position, long id)
                {
                    selectedStatistic = (String) parentView.getItemAtPosition(position);
                    statName.setText(selectedStatistic);
                    deathsByMonth = new ArrayList<>();
                    casesByMonth = new ArrayList<>();
                    recoveredByMonth = new ArrayList<>();
                    activeByMonth = new ArrayList<>();
                    getAprilTotals();
                }
                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    selectedStatistic = "Total Cases by Month";
                    statName.setText(selectedStatistic);
                    deathsByMonth = new ArrayList<>();
                    casesByMonth = new ArrayList<>();
                    recoveredByMonth = new ArrayList<>();
                    activeByMonth = new ArrayList<>();
                    getAprilTotals();
                }
            });
        }
        // handle Cases by gender graph. Display Month selector in the spinner and queue the GET requests
        if (statisticName != null && statisticName.equals("Cases by Gender")) {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.monthsList, android.R.layout.simple_spinner_item);
            spinner.setAdapter(adapter);

            // Set up spinner selections
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    currentMonth = (String) parentView.getItemAtPosition(position);
                    switch (currentMonth) {
                        case "Apr 2020":
                            dates = Arrays.asList(getResources().getStringArray(R.array.aprilDates));
                            break;
                        case "May 2020":
                            dates = Arrays.asList(getResources().getStringArray(R.array.mayDates));
                            break;
                        case "Jun 2020":
                            dates = Arrays.asList(getResources().getStringArray(R.array.juneDates));
                            break;
                        case "Jul 2020":
                            dates = Arrays.asList(getResources().getStringArray(R.array.julyDates));
                            break;
                        case "Aug 2020":
                            dates = Arrays.asList(getResources().getStringArray(R.array.augustDates));
                            break;
                        case "Sep 2020":
                            dates = Arrays.asList(getResources().getStringArray(R.array.sepDates));
                            break;
                        case "Oct 2020":
                            dates = Arrays.asList(getResources().getStringArray(R.array.octDates));
                            break;
                    }

                    getMaleData(dates.get(0), dates.get(1));
                }
                //Setup default selection
                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    currentMonth = "Apr 2020";
                    getMaleData("2020-04-01", "2020-04-30");
                }
            });
        }
        // If New cases by month selected hide the spinner and queue the GET Request
        // to display the graph
        if (statisticName != null && statisticName.equals("New Cases by Month")) {
            spinner.setVisibility(View.INVISIBLE);
            newCasesByMonth = new ArrayList<>();
            getAprilData();
        }

        // If Total Cases by Age Group selected hide the spinner and queue the GET Request
        // to display the graph
        if (statisticName != null && statisticName.equals("Total Cases by Age Group")) {
            spinner.setVisibility(View.INVISIBLE);
            casesByAge = new ArrayList<>();
            getUnder20Data();
        }
        // Set the TextView for showing the selected graph and find the BarChart component
        statName = findViewById(R.id.statisticCategory);
        statName.setText(statisticName);
        barChart = findViewById(R.id.chart);

    }
    //Setup Navigation Menu
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

    //Get Male gender data between date1 and date2.
    public void getMaleData(String date1, String date2) {
        String URL = "https://services9.arcgis.com/pJENMVYPQqZZe20v/arcgis/rest/services/" +
                "Canada_COVID19_Case_Details/FeatureServer/0/query?where=province_abbr%20%3D%20" +
                "'BC'%20AND%20date_reported%20%3E%3D%20TIMESTAMP%20'" + date1 + "%2000%3A00%3A00'" +
                "%20AND%20date_reported%20%3C%3D%20TIMESTAMP%20'"+ date2 + "%2000%3A00%3A00'%20AND%20" +
                "gender%20%3D%20'MALE'&outFields=date_reported&returnCountOnly=true&outSR=4326&f=json";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int count = (Integer) response.get("count");
                            maleCases = (float) count;
                            getFemaleData(date1, date2);
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

    //Get female data between date1 and date2.
    public void getFemaleData(String date1, String date2) {
        String URL = "https://services9.arcgis.com/pJENMVYPQqZZe20v/arcgis/rest/services/" +
                "Canada_COVID19_Case_Details/FeatureServer/0/query?where=province_abbr%20%3D%20" +
                "'BC'%20AND%20date_reported%20%3E%3D%20TIMESTAMP%20'" + date1 + "%2000%3A00%3A00'" +
                "%20AND%20date_reported%20%3C%3D%20TIMESTAMP%20'"+ date2 + "%2000%3A00%3A00'%20AND%20" +
                "gender%20%3D%20'FEMALE'&outFields=date_reported&returnCountOnly=true&outSR=4326&f=json";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
                    public void displayGenderGraph() {
                        List<BarEntry> femaleEntries = new ArrayList<>();
                        List<BarEntry> maleEntries = new ArrayList<>();
                        femaleEntries.add(new BarEntry(0, maleCases));
                        maleEntries.add(new BarEntry(1, femaleCases));
                        BarDataSet set = new BarDataSet(femaleEntries, "Female");
                        BarDataSet set1 = new BarDataSet(maleEntries, "Male");
                        int[] colors = {Color.RED, Color.BLUE};
                        set1.setColors(colors);
                        List<IBarDataSet> dataSets = new ArrayList<>();
                        dataSets.add(set);
                        dataSets.add(set1);
                        BarData data = new BarData(dataSets);
                        data.setBarWidth(0.9f);
                        barChart.setData(data);
                        barChart.setFitBars(true);
                        barChart.invalidate();
                        barChart.setDrawValueAboveBar(true);
                        Legend l = barChart.getLegend();
                        l.setTextSize(12f);
                        barChart.getAxisLeft().setDrawGridLines(false);
                        barChart.getXAxis().setDrawGridLines(false);
                        barChart.getXAxis().setTextSize(0f);
                        barChart.getAxisLeft().setTextSize(15f);
                        barChart.getAxisRight().setTextSize(15f);
                        barChart.getBarData().setValueTextSize(15f);
                    }
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int count = (Integer) response.get("count");
                            femaleCases = (float) count;
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

    //Get April New cases
    public void getAprilData() {
        String URL = "https://services9.arcgis.com/pJENMVYPQqZZe20v/arcgis/rest/services/" +
                "Canada_COVID19_Case_Details/FeatureServer/0/query?where=province_abbr%20%3D%20'BC'" +
                "%20AND%20date_reported%20%3E%3D%20TIMESTAMP%20'2020-04-01%2000%3A00%3A00'%20" +
                "AND%20date_reported%20%3C%3D%20TIMESTAMP%20'2020-04-30%2000%3A00%3A00'" +
                "&outFields=date_reported&returnCountOnly=true&outSR=4326&f=json";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int count = (Integer) response.get("count");
                            aprilTotalCases = (float) count;
                            getMayData("2020-05-01", "2020-05-31");
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
    //Get May New cases
    public void getMayData(String date1, String date2) {
        String URL = "https://services9.arcgis.com/pJENMVYPQqZZe20v/arcgis/rest/services/" +
                "Canada_COVID19_Case_Details/FeatureServer/0/query?where=province_abbr%20%3D%20" +
                "'BC'%20AND%20date_reported%20%3E%3D%20TIMESTAMP%20'" + date1 + "%2000%3A00%3A00'" +
                "%20AND%20date_reported%20%3C%3D%20TIMESTAMP%20'"+ date2 + "%2000%3A00%3A00'&" +
                "outFields=date_reported&returnCountOnly=true&outSR=4326&f=json";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int count = (Integer) response.get("count");
                            mayTotalCases = (float) count;
                            getJuneData("2020-06-01", "2020-06-30");
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
    //Get June New cases
    public void getJuneData(String date1, String date2) {
        String URL = "https://services9.arcgis.com/pJENMVYPQqZZe20v/arcgis/rest/services/" +
                "Canada_COVID19_Case_Details/FeatureServer/0/query?where=province_abbr%20%3D%20" +
                "'BC'%20AND%20date_reported%20%3E%3D%20TIMESTAMP%20'" + date1 + "%2000%3A00%3A00'" +
                "%20AND%20date_reported%20%3C%3D%20TIMESTAMP%20'" + date2 + "%2000%3A00%3A00'&" +
                "outFields=date_reported&returnCountOnly=true&outSR=4326&f=json";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int count = (Integer) response.get("count");
                            juneTotalCases = (float) count;
                            getJulyData("2020-07-01", "2020-07-31");
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
    //Get July New cases
    public void getJulyData(String date1, String date2) {
        String URL = "https://services9.arcgis.com/pJENMVYPQqZZe20v/arcgis/rest/services/" +
                "Canada_COVID19_Case_Details/FeatureServer/0/query?where=province_abbr%20%3D%20" +
                "'BC'%20AND%20date_reported%20%3E%3D%20TIMESTAMP%20'" + date1 + "%2000%3A00%3A00'" +
                "%20AND%20date_reported%20%3C%3D%20TIMESTAMP%20'" + date2 + "%2000%3A00%3A00'&" +
                "outFields=date_reported&returnCountOnly=true&outSR=4326&f=json";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int count = (Integer) response.get("count");
                            julyTotalCases = (float) count;
                            getAugustData("2020-08-01", "2020-08-31");
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
    //Get August New cases
    public void getAugustData(String date1, String date2) {
        String URL = "https://services9.arcgis.com/pJENMVYPQqZZe20v/arcgis/rest/services/" +
                "Canada_COVID19_Case_Details/FeatureServer/0/query?where=province_abbr%20%3D%20" +
                "'BC'%20AND%20date_reported%20%3E%3D%20TIMESTAMP%20'" + date1 + "%2000%3A00%3A00'" +
                "%20AND%20date_reported%20%3C%3D%20TIMESTAMP%20'" + date2 + "%2000%3A00%3A00'&" +
                "outFields=date_reported&returnCountOnly=true&outSR=4326&f=json";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int count = (Integer) response.get("count");
                            augustTotalCases = (float) count;
                            getSeptemberData("2020-09-01", "2020-09-30");
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
    //Get September New cases
    public void getSeptemberData(String date1, String date2) {
        String URL = "https://services9.arcgis.com/pJENMVYPQqZZe20v/arcgis/rest/services/" +
                "Canada_COVID19_Case_Details/FeatureServer/0/query?where=province_abbr%20%3D%20" +
                "'BC'%20AND%20date_reported%20%3E%3D%20TIMESTAMP%20'" + date1 + "%2000%3A00%3A00'" +
                "%20AND%20date_reported%20%3C%3D%20TIMESTAMP%20'" + date2 + "%2000%3A00%3A00'&" +
                "outFields=date_reported&returnCountOnly=true&outSR=4326&f=json";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int count = (Integer) response.get("count");
                            septTotalCases = (float) count;
                            getOctoberData("2020-10-01", "2020-10-31");
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
    //Get October New cases
    public void getOctoberData(String date1, String date2) {
        String URL = "https://services9.arcgis.com/pJENMVYPQqZZe20v/arcgis/rest/services/" +
                "Canada_COVID19_Case_Details/FeatureServer/0/query?where=province_abbr%20%3D%20" +
                "'BC'%20AND%20date_reported%20%3E%3D%20TIMESTAMP%20'" + date1 + "%2000%3A00%3A00'" +
                "%20AND%20date_reported%20%3C%3D%20TIMESTAMP%20'" + date2 + "%2000%3A00%3A00'&" +
                "outFields=date_reported&returnCountOnly=true&outSR=4326&f=json";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
                    public void displayTotalGraph() {
                        // Entries
                        List<BarEntry> aprilEntries = new ArrayList<>();
                        List<BarEntry> mayEntries = new ArrayList<>();
                        List<BarEntry> juneEntries = new ArrayList<>();
                        List<BarEntry> julyEntries = new ArrayList<>();
                        List<BarEntry> augustEntries = new ArrayList<>();
                        List<BarEntry> septEntries = new ArrayList<>();
                        List<BarEntry> octEntries = new ArrayList<>();
                        aprilEntries.add(new BarEntry(0, aprilTotalCases));
                        mayEntries.add(new BarEntry(1, mayTotalCases));
                        juneEntries.add(new BarEntry(2, juneTotalCases));
                        julyEntries.add(new BarEntry(3, julyTotalCases));
                        augustEntries.add(new BarEntry(4, augustTotalCases));
                        septEntries.add(new BarEntry(5, septTotalCases));
                        octEntries.add(new BarEntry(6, octTotalCases));
                        //Create dataset
                        BarDataSet set = new BarDataSet(aprilEntries, "April");
                        BarDataSet set1 = new BarDataSet(mayEntries, "May");
                        BarDataSet set2 = new BarDataSet(juneEntries, "June");
                        BarDataSet set3 = new BarDataSet(julyEntries, "July");
                        BarDataSet set4 = new BarDataSet(augustEntries, "August");
                        BarDataSet set5 = new BarDataSet(septEntries, "September");
                        BarDataSet set6 = new BarDataSet(octEntries, "October");
                        List<IBarDataSet> dataSets = new ArrayList<>();
                        dataSets.add(set);
                        dataSets.add(set1);
                        dataSets.add(set2);
                        dataSets.add(set3);
                        dataSets.add(set4);
                        dataSets.add(set5);
                        dataSets.add(set6);
                        BarData data = new BarData(dataSets);
                        data.setBarWidth(0.5f);
                        //Display BarChart
                        barChart.setData(data);
                        barChart.setFitBars(true);
                        barChart.invalidate();
                        barChart.setDrawValueAboveBar(true);
                        Legend l = barChart.getLegend();
                        l.setTextSize(12f);
                        barChart.getAxisLeft().setDrawGridLines(false);
                        barChart.getXAxis().setDrawGridLines(false);
                        barChart.getXAxis().setTextSize(0f);
                        barChart.getAxisLeft().setTextSize(15f);
                        barChart.getAxisRight().setTextSize(15f);
                        barChart.getBarData().setValueTextSize(15f);
                    }

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int count = (Integer) response.get("count");
                            octTotalCases = (float) count;
                            displayTotalGraph();
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
    //Get under 20 total
    public void getUnder20Data() {
        String URL = "https://services9.arcgis.com/pJENMVYPQqZZe20v/arcgis/rest/services/" +
                "Canada_COVID19_Case_Details/FeatureServer/0/query?where=province_abbr%20%3D%20'" +
                "BC'%20AND%20age_group%20%3D%20'%3C20'&outFields=age_group&returnCountOnly=true&" +
                "outSR=4326&f=json";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int count = (Integer) response.get("count");
                            casesByAge.add((float) count);
                            getTwentiesData();
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
    //Get 20-29 total
    public void getTwentiesData() {
            String URL = "https://services9.arcgis.com/pJENMVYPQqZZe20v/arcgis/rest/services/" +
                    "Canada_COVID19_Case_Details/FeatureServer/0/query?where=province_abbr%20%3D%20" +
                    "'BC'%20AND%20age_group%20%3D%20'20-29'&outFields=" +
                    "age_group&returnCountOnly=true&outSR=4326&f=json";
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                int count = (Integer) response.get("count");
                                casesByAge.add((float) count);
                                getThirtiesData();
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
    //Get 30-39 total
    public void getThirtiesData() {
        String URL = "https://services9.arcgis.com/pJENMVYPQqZZe20v/arcgis/rest/services/" +
                "Canada_COVID19_Case_Details/FeatureServer/0/query?where=province_abbr%20%3D%20" +
                "'BC'%20AND%20age_group%20%3D%20'30-39'&outFields=" +
                "age_group&returnCountOnly=true&outSR=4326&f=json";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int count = (Integer) response.get("count");
                            casesByAge.add((float) count);
                            getFortiesData();
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
    //Get 40-49 total
    public void getFortiesData() {
        String URL = "https://services9.arcgis.com/pJENMVYPQqZZe20v/arcgis/rest/services/" +
                "Canada_COVID19_Case_Details/FeatureServer/0/query?where=province_abbr%20%3D%20" +
                "'BC'%20AND%20age_group%20%3D%20'40-49'&outFields=" +
                "age_group&returnCountOnly=true&outSR=4326&f=json";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int count = (Integer) response.get("count");
                            casesByAge.add((float) count);
                            getFiftiesData();
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
    //Get 50-59 total
    public void getFiftiesData() {
        String URL = "https://services9.arcgis.com/pJENMVYPQqZZe20v/arcgis/rest/services/" +
                "Canada_COVID19_Case_Details/FeatureServer/0/query?where=province_abbr%20%3D%20" +
                "'BC'%20AND%20age_group%20%3D%20'50-59'&outFields=" +
                "age_group&returnCountOnly=true&outSR=4326&f=json";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int count = (Integer) response.get("count");
                            casesByAge.add((float) count);
                            getSixtiesData();
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
    //Get 60-69 total
    public void getSixtiesData() {
        String URL = "https://services9.arcgis.com/pJENMVYPQqZZe20v/arcgis/rest/services/" +
                "Canada_COVID19_Case_Details/FeatureServer/0/query?where=province_abbr%20%3D%20" +
                "'BC'%20AND%20age_group%20%3D%20'60-69'&outFields=" +
                "age_group&returnCountOnly=true&outSR=4326&f=json";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int count = (Integer) response.get("count");
                            casesByAge.add((float) count);
                            getSeventiesData();
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
    //Get 70-79 total
    public void getSeventiesData() {
        String URL = "https://services9.arcgis.com/pJENMVYPQqZZe20v/arcgis/rest/services/" +
                "Canada_COVID19_Case_Details/FeatureServer/0/query?where=province_abbr%20%3D%20" +
                "'BC'%20AND%20age_group%20%3D%20'70-79'&outFields=" +
                "age_group&returnCountOnly=true&outSR=4326&f=json";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int count = (Integer) response.get("count");
                            casesByAge.add((float) count);
                            get80PlusData();
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
    //Get 80+ total
    public void get80PlusData() {
        String URL = "https://services9.arcgis.com/pJENMVYPQqZZe20v/arcgis/rest/services/" +
                "Canada_COVID19_Case_Details/FeatureServer/0/query?where=province_abbr%20%3D%20" +
                "'BC'%20AND%20age_group%20%3D%20'80+'&outFields=" +
                "age_group&returnCountOnly=true&outSR=4326&f=json";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {

                    public void displayAgeGraph() {
                        List<BarEntry> under20Entries = new ArrayList<>();
                        List<BarEntry> twentiesEntries = new ArrayList<>();
                        List<BarEntry> thirtiesEntries = new ArrayList<>();
                        List<BarEntry> fortiesEntries = new ArrayList<>();
                        List<BarEntry> fiftiesEntries = new ArrayList<>();
                        List<BarEntry> sixtiesEntries = new ArrayList<>();
                        List<BarEntry> seventiesEntries = new ArrayList<>();
                        List<BarEntry> over80Entries = new ArrayList<>();
                        under20Entries.add(new BarEntry(0, casesByAge.get(0)));
                        twentiesEntries.add(new BarEntry(1, casesByAge.get(1)));
                        thirtiesEntries.add(new BarEntry(2, casesByAge.get(2)));
                        fortiesEntries.add(new BarEntry(3, casesByAge.get(3)));
                        fiftiesEntries.add(new BarEntry(4, casesByAge.get(4)));
                        sixtiesEntries.add(new BarEntry(5, casesByAge.get(5)));
                        seventiesEntries.add(new BarEntry(6, casesByAge.get(6)));
                        over80Entries.add(new BarEntry(6, casesByAge.get(7)));
                        BarDataSet set = new BarDataSet(under20Entries, "<20");
                        BarDataSet set1 = new BarDataSet(twentiesEntries, "20-29");
                        BarDataSet set2 = new BarDataSet(thirtiesEntries, "30-39");
                        BarDataSet set3 = new BarDataSet(fortiesEntries, "40-49");
                        BarDataSet set4 = new BarDataSet(fiftiesEntries, "50-59");
                        BarDataSet set5 = new BarDataSet(sixtiesEntries, "60-69");
                        BarDataSet set6 = new BarDataSet(seventiesEntries, "70-79");
                        BarDataSet set7 = new BarDataSet(over80Entries, "80+");
                        List<IBarDataSet> dataSets = new ArrayList<>();
                        dataSets.add(set);
                        dataSets.add(set1);
                        dataSets.add(set2);
                        dataSets.add(set3);
                        dataSets.add(set4);
                        dataSets.add(set5);
                        dataSets.add(set6);
                        dataSets.add(set7);
                        BarData data = new BarData(dataSets);
                        data.setBarWidth(0.5f);
                        barChart.setData(data);
                        barChart.setFitBars(true);
                        barChart.invalidate();
                        Legend l = barChart.getLegend();
                        l.setTextSize(12f);
                        barChart.getAxisLeft().setDrawGridLines(false);
                        barChart.getXAxis().setDrawGridLines(false);
                        barChart.getXAxis().setTextSize(15f);
                        barChart.getAxisLeft().setTextSize(15f);
                        barChart.getAxisRight().setTextSize(15f);
                        barChart.getBarData().setValueTextSize(15f);
                    }

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int count = (Integer) response.get("count");
                            casesByAge.add((float) count);
                            displayAgeGraph();
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

    //Get totals for april
    public void getAprilTotals() {
        String URL = "https://services9.arcgis.com/pJENMVYPQqZZe20v/arcgis/rest/services/" +
                "province_daily_totals/FeatureServer/0/query?where=Abbreviation%20%3D%20" +
                "'BC'%20AND%20SummaryDate%20%3E%3D%20TIMESTAMP%20'2020-04-29%2000%3A00%3A00" +
                "'%20AND%20SummaryDate%20%3C%3D%20TIMESTAMP%20'2020-04-30%2000%3A00%3A00'" +
                "&outFields=TotalDeaths,TotalRecovered,TotalActive,TotalCases&outSR=4326&f=json";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray features =(JSONArray) response.get("features");
                            JSONObject attributesObj = (JSONObject) features.get(0);
                            JSONObject attributes = (JSONObject) attributesObj.get("attributes");
                            int deaths = (Integer) attributes.get("TotalDeaths");

                            int recovered = (Integer) attributes.get("TotalRecovered");
                            int cases = (Integer) attributes.get("TotalCases");
                            int active = (Integer) attributes.get("TotalActive");
                            deathsByMonth.add((float) deaths);
                            recoveredByMonth.add((float) recovered);
                            activeByMonth.add((float) active);
                            casesByMonth.add((float) cases);
                            getMayTotals();
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
    //Get totals for may
    public void getMayTotals() {
        String URL = "https://services9.arcgis.com/pJENMVYPQqZZe20v/arcgis/rest/services/" +
                "province_daily_totals/FeatureServer/0/query?where=Abbreviation%20%3D%20" +
                "'BC'%20AND%20SummaryDate%20%3E%3D%20TIMESTAMP%20'2020-05-30%2000%3A00%3A00" +
                "'%20AND%20SummaryDate%20%3C%3D%20TIMESTAMP%20'2020-05-31%2000%3A00%3A00'" +
                "&outFields=TotalDeaths,TotalRecovered,TotalActive,TotalCases&outSR=4326&f=json";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray features =(JSONArray) response.get("features");
                            JSONObject attributesObj = (JSONObject) features.get(0);
                            JSONObject attributes = (JSONObject) attributesObj.get("attributes");
                            int deaths = (Integer) attributes.get("TotalDeaths");
                            int recovered = (Integer) attributes.get("TotalRecovered");
                            int cases = (Integer) attributes.get("TotalCases");
                            int active = (Integer) attributes.get("TotalActive");
                            deathsByMonth.add((float) deaths);
                            recoveredByMonth.add((float) recovered);
                            activeByMonth.add((float) active);
                            casesByMonth.add((float) cases);
                            getJuneTotals();
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
    //Get totals for june
    public void getJuneTotals() {
        String URL = "https://services9.arcgis.com/pJENMVYPQqZZe20v/arcgis/rest/services/" +
                "province_daily_totals/FeatureServer/0/query?where=Abbreviation%20%3D%20" +
                "'BC'%20AND%20SummaryDate%20%3E%3D%20TIMESTAMP%20'2020-06-29%2000%3A00%3A00" +
                "'%20AND%20SummaryDate%20%3C%3D%20TIMESTAMP%20'2020-06-30%2000%3A00%3A00'" +
                "&outFields=TotalDeaths,TotalRecovered,TotalActive,TotalCases&outSR=4326&f=json";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray features =(JSONArray) response.get("features");
                            JSONObject attributesObj = (JSONObject) features.get(0);
                            JSONObject attributes = (JSONObject) attributesObj.get("attributes");
                            int deaths = (Integer) attributes.get("TotalDeaths");
                            int recovered = (Integer) attributes.get("TotalRecovered");
                            int cases = (Integer) attributes.get("TotalCases");
                            int active = (Integer) attributes.get("TotalActive");
                            deathsByMonth.add((float) deaths);
                            recoveredByMonth.add((float) recovered);
                            activeByMonth.add((float) active);
                            casesByMonth.add((float) cases);
                            getJulyTotals();
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
    //Get totals for july
    public void getJulyTotals() {
        String URL = "https://services9.arcgis.com/pJENMVYPQqZZe20v/arcgis/rest/services/" +
                "province_daily_totals/FeatureServer/0/query?where=Abbreviation%20%3D%20" +
                "'BC'%20AND%20SummaryDate%20%3E%3D%20TIMESTAMP%20'2020-07-30%2000%3A00%3A00" +
                "'%20AND%20SummaryDate%20%3C%3D%20TIMESTAMP%20'2020-07-31%2000%3A00%3A00'" +
                "&outFields=TotalDeaths,TotalRecovered,TotalActive,TotalCases&outSR=4326&f=json";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray features =(JSONArray) response.get("features");
                            JSONObject attributesObj = (JSONObject) features.get(0);
                            JSONObject attributes = (JSONObject) attributesObj.get("attributes");
                            int deaths = (Integer) attributes.get("TotalDeaths");
                            int recovered = (Integer) attributes.get("TotalRecovered");
                            int cases = (Integer) attributes.get("TotalCases");
                            int active = (Integer) attributes.get("TotalActive");
                            deathsByMonth.add((float) deaths);
                            recoveredByMonth.add((float) recovered);
                            activeByMonth.add((float) active);
                            casesByMonth.add((float) cases);
                            getAugustTotals();
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
    //Get totals for august
    public void getAugustTotals() {
        String URL = "https://services9.arcgis.com/pJENMVYPQqZZe20v/arcgis/rest/services/" +
                "province_daily_totals/FeatureServer/0/query?where=Abbreviation%20%3D%20" +
                "'BC'%20AND%20SummaryDate%20%3E%3D%20TIMESTAMP%20'2020-08-30%2000%3A00%3A00" +
                "'%20AND%20SummaryDate%20%3C%3D%20TIMESTAMP%20'2020-08-31%2000%3A00%3A00'" +
                "&outFields=TotalDeaths,TotalRecovered,TotalActive,TotalCases&outSR=4326&f=json";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray features =(JSONArray) response.get("features");
                            JSONObject attributesObj = (JSONObject) features.get(0);
                            JSONObject attributes = (JSONObject) attributesObj.get("attributes");
                            int deaths = (Integer) attributes.get("TotalDeaths");
                            int recovered = (Integer) attributes.get("TotalRecovered");
                            int cases = (Integer) attributes.get("TotalCases");
                            int active = (Integer) attributes.get("TotalActive");
                            deathsByMonth.add((float) deaths);
                            recoveredByMonth.add((float) recovered);
                            activeByMonth.add((float) active);
                            casesByMonth.add((float) cases);
                            getSeptemberTotals();
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
    //Get totals for sept
    public void getSeptemberTotals() {
        String URL = "https://services9.arcgis.com/pJENMVYPQqZZe20v/arcgis/rest/services/" +
                "province_daily_totals/FeatureServer/0/query?where=Abbreviation%20%3D%20" +
                "'BC'%20AND%20SummaryDate%20%3E%3D%20TIMESTAMP%20'2020-09-29%2000%3A00%3A00" +
                "'%20AND%20SummaryDate%20%3C%3D%20TIMESTAMP%20'2020-09-30%2000%3A00%3A00'" +
                "&outFields=TotalDeaths,TotalRecovered,TotalActive,TotalCases&outSR=4326&f=json";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray features =(JSONArray) response.get("features");
                            JSONObject attributesObj = (JSONObject) features.get(0);
                            JSONObject attributes = (JSONObject) attributesObj.get("attributes");
                            int deaths = (Integer) attributes.get("TotalDeaths");
                            int recovered = (Integer) attributes.get("TotalRecovered");
                            int cases = (Integer) attributes.get("TotalCases");
                            int active = (Integer) attributes.get("TotalActive");
                            deathsByMonth.add((float) deaths);
                            recoveredByMonth.add((float) recovered);
                            activeByMonth.add((float) active);
                            casesByMonth.add((float) cases);
                            getOctoberTotals();
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
    //Get totals for october
    public void getOctoberTotals() {
        String URL = "https://services9.arcgis.com/pJENMVYPQqZZe20v/arcgis/rest/services/" +
                "province_daily_totals/FeatureServer/0/query?where=Abbreviation%20%3D%20" +
                "'BC'%20AND%20SummaryDate%20%3E%3D%20TIMESTAMP%20'2020-10-30%2000%3A00%3A00" +
                "'%20AND%20SummaryDate%20%3C%3D%20TIMESTAMP%20'2020-10-31%2000%3A00%3A00'" +
                "&outFields=TotalDeaths,TotalRecovered,TotalActive,TotalCases&outSR=4326&f=json";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
                    public void displayTotalsGraph(List<Float> selectedList) {
                        List<BarEntry> aprilEntries = new ArrayList<>();
                        List<BarEntry> mayEntries = new ArrayList<>();
                        List<BarEntry> juneEntries = new ArrayList<>();
                        List<BarEntry> julyEntries = new ArrayList<>();
                        List<BarEntry> augustEntries = new ArrayList<>();
                        List<BarEntry> septemberEntries = new ArrayList<>();
                        List<BarEntry> octoberEntries = new ArrayList<>();
                        aprilEntries.add(new BarEntry(0, selectedList.get(0)));
                        mayEntries.add(new BarEntry(1, selectedList.get(1)));
                        juneEntries.add(new BarEntry(2, selectedList.get(2)));
                        julyEntries.add(new BarEntry(3, selectedList.get(3)));
                        augustEntries.add(new BarEntry(4, selectedList.get(4)));
                        septemberEntries.add(new BarEntry(5, selectedList.get(5)));
                        octoberEntries.add(new BarEntry(6, selectedList.get(6)));
                        BarDataSet set = new BarDataSet(aprilEntries, "April");
                        BarDataSet set1 = new BarDataSet(mayEntries, "May");
                        BarDataSet set2 = new BarDataSet(juneEntries, "June");
                        BarDataSet set3 = new BarDataSet(julyEntries, "July");
                        BarDataSet set4 = new BarDataSet(augustEntries, "August");
                        BarDataSet set5 = new BarDataSet(septemberEntries, "September");
                        BarDataSet set6 = new BarDataSet(octoberEntries, "October");
                        List<IBarDataSet> dataSets = new ArrayList<>();
                        dataSets.add(set);
                        dataSets.add(set1);
                        dataSets.add(set2);
                        dataSets.add(set3);
                        dataSets.add(set4);
                        dataSets.add(set5);
                        dataSets.add(set6);
                        BarData data = new BarData(dataSets);
                        data.setBarWidth(0.5f);
                        barChart.setData(data);
                        barChart.setFitBars(true);
                        barChart.invalidate();
                        Legend l = barChart.getLegend();
                        l.setTextSize(10f);
                        barChart.getAxisLeft().setDrawGridLines(false);
                        barChart.getXAxis().setDrawGridLines(false);
                        barChart.getXAxis().setTextSize(0f);
                        barChart.getAxisLeft().setTextSize(15f);
                        barChart.getAxisRight().setTextSize(15f);
                        barChart.getBarData().setValueTextSize(15f);
                        }

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray features =(JSONArray) response.get("features");
                            JSONObject attributesObj = (JSONObject) features.get(0);
                            JSONObject attributes = (JSONObject) attributesObj.get("attributes");
                            int deaths = (Integer) attributes.get("TotalDeaths");
                            int recovered = (Integer) attributes.get("TotalRecovered");
                            int cases = (Integer) attributes.get("TotalCases");
                            int active = (Integer) attributes.get("TotalActive");
                            deathsByMonth.add((float) deaths);
                            recoveredByMonth.add((float) recovered);
                            activeByMonth.add((float) active);
                            casesByMonth.add((float) cases);
                            statisticsMap = new HashMap<>();
                            statisticsMap.put("Total Deaths by Month", deathsByMonth);
                            statisticsMap.put("Total Recovered by Month", recoveredByMonth);
                            statisticsMap.put("Total Cases by Month", casesByMonth);
                            statisticsMap.put("Total Active by Month", activeByMonth);
                            selectedList = statisticsMap.get(selectedStatistic);
                            if (selectedList != null) {
                                displayTotalsGraph(selectedList);
                            }
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
    //Get data for textView labels
    public void getLabelData(String today, String yesterday) {
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
