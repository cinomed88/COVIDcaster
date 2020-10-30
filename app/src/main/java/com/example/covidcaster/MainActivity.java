package com.example.covidcaster;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.LruCache;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    String URL = "https://services1.arcgis.com/xeMpV7tU1t4KD3Ei/ArcGIS/rest/services/" +
            "COVID19_Cases_by_BC_Health_Authority/FeatureServer/0/query" +
            "?where=HA_ID+LIKE+%27%25HA%27&objectIds=&time=&geometry=" +
            "&geometryType=esriGeometryEnvelope&inSR=&spatialRel=esriSpatialRelIntersects" +
            "&resultType=none&distance=0.0&units=esriSRUnit_Meter&returnGeodetic=false" +
            "&outFields=*&returnGeometry=false&returnCentroid=false" +
            "&featureEncoding=esriDefault&multipatchOption=xyFootprint&maxAllowableOffset=" +
            "&geometryPrecision=&outSR=&datumTransformation=&applyVCSProjection=false" +
            "&returnIdsOnly=false&returnUniqueIdsOnly=false&returnCountOnly=false" +
            "&returnExtentOnly=false&returnQueryGeometry=false&returnDistinctValues=false" +
            "&cacheHint=false&orderByFields=&groupByFieldsForStatistics=&outStatistics=" +
            "&having=&resultOffset=&resultRecordCount=&returnZ=false&returnM=false" +
            "&returnExceededLimitFeatures=true&quantizationParameters=&sqlFormat=none" +
            "&f=pjson&token=";

    TextView tvTotal;
    TextView tvNew;
    TextView tvActive;
    TextView tvRecovered;
    TextView tvDeaths;
    TextView tvHospitalized;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Don't allow landscape screen
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        tvTotal = findViewById(R.id.totalCasesNum);
        tvActive = findViewById(R.id.activeCasesNum);
        tvNew = findViewById(R.id.newCasesNum);
        tvDeaths = findViewById(R.id.DeathsNum);
        tvHospitalized = findViewById(R.id.hospitalizedNum);

        getData();
    }

    public void OnClickRegionalData(View v) {
        Intent intent = new Intent(MainActivity.this, RegionalDataActivity.class);
        startActivity(intent);
    }

    public void onDataClick(View v) {
        Intent intent = new Intent(MainActivity.this, DataActivity.class);
        startActivity(intent);
    }

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

                                totalCases += cases;
                                totalActiveCases += activeCases;
                                totalNewCases += newCases;
                                totalRecovered += recovered;
                                totalDeaths += deaths;
                                totalHospitalized += hospitalized;

                            }

                            tvTotal.setText(Integer.toString(totalCases));
                            tvActive.setText(Integer.toString(totalActiveCases));
                            tvNew.setText(Integer.toString(totalNewCases));
                            tvDeaths.setText(Integer.toString(totalDeaths));
                            tvHospitalized.setText(Integer.toString(totalHospitalized));
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
