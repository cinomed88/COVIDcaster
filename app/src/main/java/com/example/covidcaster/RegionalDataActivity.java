package com.example.covidcaster;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.portal.PortalItem;


public class RegionalDataActivity extends AppCompatActivity {

    private MapView mMapView;
    private FeatureLayer mFeatureLayer;
    String layerID = "f7d1318260b14ac2b334e81e55ee5c9e";
    Portal portal = new Portal("https://bcgov03.maps.arcgis.com", false);
    final PortalItem portalItem = new PortalItem(portal, layerID);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regional_data);

        mMapView = findViewById(R.id.mapView);
        setupMap();
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
            // *** ADD ***
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
}