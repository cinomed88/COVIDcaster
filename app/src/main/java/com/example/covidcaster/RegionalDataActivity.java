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
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.TextView;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.GeoElement;
import com.esri.arcgisruntime.mapping.view.Callout;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.portal.PortalItem;
import com.google.android.material.navigation.NavigationView;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Loads an Arcgis map layer and map feature layer for interaction to allow user to select region
 * and read the data in that region.
 */
public class RegionalDataActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private static final String sTag = "Gesture";
    private MapView mMapView;
    private FeatureLayer mFeatureLayer;
    String layerID = "f7d1318260b14ac2b334e81e55ee5c9e"; //The layer id that show region (used) and Collection Centres (unused in this activity)
    Portal portal = new Portal("https://bcgov03.maps.arcgis.com", false); //Opens up a portal to the BC Government map layer
    final PortalItem portalItem = new PortalItem(portal, layerID);

    TextView detailtv;
    TextView totaltv;
    TextView activetv;
    TextView hospitalizedtv;
    TextView icutv;
    TextView deathtv;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regional_data);

        Toolbar toolbar = findViewById(R.id.toolbar_map);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout_map);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawer,
                toolbar,
                R.string.nav_open_drawer,
                R.string.nav_close_drawer);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view_map);
        navigationView.setNavigationItemSelectedListener(this);

        mMapView = findViewById(R.id.mapView);
        setupMap();

        detailtv = findViewById(R.id.regional_detail_name_tv);
        detailtv.setText(R.string.regionalMapPrompt);
        totaltv = findViewById(R.id.regional_detail_total_tv);
        activetv = findViewById(R.id.regional_detail_active_tv);
        hospitalizedtv = findViewById(R.id.regional_detail_hospitalized_tv);
        icutv = findViewById(R.id.regional_detail_icu_tv);
        deathtv = findViewById(R.id.regional_detail_death_tv);

        /**
         * Sets an on (single) click listener that selects and highlights a regional data.
         * It also shows the following regional attributes: Active Cases, Cases, Hospitalized, ICU,
         * and Deaths.
         */
        mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {

            @Override
            public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
                Log.d(sTag, "onSingleTapConfirmed: " + motionEvent.toString());

                android.graphics.Point screenPoint = new android.graphics.Point(Math.round(motionEvent.getX()),
                        Math.round(motionEvent.getY()));

                ListenableFuture<IdentifyLayerResult> identifyFuture =
                        mMapView.identifyLayerAsync(mFeatureLayer, screenPoint, 25, false);
                identifyFuture.addDoneListener(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            IdentifyLayerResult identifyLayerResult = identifyFuture.get();

                            FeatureLayer featureLayer = null;
                            if (identifyLayerResult.getLayerContent() instanceof FeatureLayer) {
                                featureLayer = (FeatureLayer) identifyLayerResult.getLayerContent();
                            }

                            for (GeoElement identifiedElement : identifyLayerResult.getElements()) {
                                if (identifiedElement instanceof Feature) {
                                    Feature identifiedFeature = (Feature) identifiedElement;
                                    if (featureLayer != null) {
                                        featureLayer.clearSelection();
                                        featureLayer.selectFeature(identifiedFeature);
                                        Map<String, Object> regionalAttributes = identifiedFeature.getAttributes();
                                        String name = (String) regionalAttributes.get("HA_Name");
                                        int activeCases = (int) regionalAttributes.get("ActiveCases");
                                        int totalCases = (int) regionalAttributes.get("Cases");
                                        int hospitalizedCases = (int) regionalAttributes.get("CurrentlyHosp");
                                        int icuCases = (int) regionalAttributes.get("CurrentlyICU");
                                        int deathCases = (int) regionalAttributes.get("Deaths");

                                        detailtv.setText(name);
                                        totaltv.setText(Integer.toString(totalCases));
                                        activetv.setText(Integer.toString(activeCases));
                                        hospitalizedtv.setText(Integer.toString(hospitalizedCases));
                                        icutv.setText(Integer.toString(icuCases));
                                        deathtv.setText(Integer.toString(deathCases));
                                    }
                                }
                            }
                        } catch (InterruptedException | ExecutionException ex){
                            Log.d(sTag, ex.toString());
                        }
                    }
                });

                Point mapPoint = mMapView.screenToLocation(screenPoint);
                mMapView.setViewpointCenterAsync(mapPoint);

                return true;
            }
        });
    }

    /**
     * Sets up the base BC Regional Map with Collection centre and adds the Regional Covid Data
     * layer on top.
     */
    private void setupMap() {
        if (mMapView != null) {
            String itemId = "eea6c3fb2b654bb6a4d935666a448bd1";
            PortalItem portalItem = new PortalItem(portal, itemId);
            ArcGISMap map = new ArcGISMap(portalItem);
            mMapView.setMap(map);
            addLayer(map);
        }
    }

    private void addLayer(final ArcGISMap map) {
        mFeatureLayer = new FeatureLayer(portalItem,0);
        mFeatureLayer.addDoneLoadingListener(new Runnable() {

            @Override public void run() {
                if (mFeatureLayer.getLoadStatus() == LoadStatus.LOADED) {
                    map.getOperationalLayers().add(mFeatureLayer);
                }
            }
        });
        mFeatureLayer.loadAsync();
    }



    @Override
    protected void onPause() {
        if (mMapView != null) {
            mMapView.pause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMapView != null) {
            mMapView.resume();
        }
    }

    @Override
    protected void onDestroy() {
        if (mMapView != null) {
            mMapView.dispose();
        }
        super.onDestroy();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Intent intent = null;

        switch(id) {
            case R.id.nav_home:
                intent = new Intent(this, MainActivity.class);
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
                intent = new Intent(this, RegionalDataActivity.class);
        }
        startActivity(intent);

        DrawerLayout drawer = findViewById(R.id.drawer_layout_map);
        drawer.closeDrawer(GravityCompat.START);
        return true;

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout_map);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}