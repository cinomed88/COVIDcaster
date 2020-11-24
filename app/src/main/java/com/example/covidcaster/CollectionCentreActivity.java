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
import android.text.method.LinkMovementMethod;
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

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;


public class CollectionCentreActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private static final String sTag = "Gesture";
    private MapView mMapView;
    private FeatureLayer mFeatureLayer;
    String layerID = "125d693c953a483ba82c637166f201cb";
    Portal portal = new Portal("https://bcgov03.maps.arcgis.com", false);
    final PortalItem portalItem = new PortalItem(portal, layerID);

    TextView ccRegiontv;
    TextView ccNametv;
    TextView ccAddresstv;
    TextView ccHourstv;
    TextView ccDaystv;
    TextView ccSpecialtv;
    TextView ccPhonetv;
    TextView ccWebsitetv;
    TextView ccAppttv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_centre);

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

        mMapView = findViewById(R.id.ccmapView);
        setupMap();

        ccRegiontv = findViewById(R.id.cc_health_authority_tv);
        ccNametv = findViewById(R.id.cc_name_tv);
        ccAddresstv = findViewById(R.id.cc_address_tv);
        ccHourstv = findViewById(R.id.cc_hours_tv);
        ccDaystv = findViewById(R.id.cc_days_tv);
        ccPhonetv = findViewById(R.id.cc_phone_tv);
        ccWebsitetv = findViewById(R.id.cc_website_tv);
        ccAppttv = findViewById(R.id.cc_appointment_tv);

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

                                        Map<String, Object> ccAttributes = identifiedFeature.getAttributes();
                                        String ha = (String) ccAttributes.get("Health_Authority");
                                        String name = (String) ccAttributes.get("Community_Collection_Center_Nam");
                                        String address = (String) ccAttributes.get("Address");
                                        String hours = (String) ccAttributes.get("Monday_Hours");
                                        String days = (String) ccAttributes.get("Days");
                                        String phone = (String) ccAttributes.get("Phone");
                                        String website = (String) ccAttributes.get("Website");
                                        String appt = (String) ccAttributes.get("Referral_Required");

                                        ccRegiontv.setText(ha);
                                        ccNametv.setText(name);
                                        ccAddresstv.setText(address);
                                        ccHourstv.setText(hours);
                                        ccDaystv.setText(days);
                                        ccPhonetv.setText(phone == null ? phone : "Not available");
                                        ccWebsitetv.setText(website);
                                        ccWebsitetv.setMovementMethod(LinkMovementMethod.getInstance());
                                        ccAppttv.setText(appt);
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
                intent = new Intent(this, CollectionCentreActivity.class);
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