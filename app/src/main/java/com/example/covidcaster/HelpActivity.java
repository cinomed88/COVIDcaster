package com.example.covidcaster;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class HelpActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        // Create the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_help);
        setSupportActionBar(toolbar);

        // Create the drawer
        DrawerLayout drawer = findViewById(R.id.drawer_layout_help);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawer,
                toolbar,
                R.string.nav_open_drawer,
                R.string.nav_close_drawer);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Set Navigation Listener
        NavigationView navigationView = findViewById(R.id.nav_view_help);
        navigationView.setNavigationItemSelectedListener(this);

        TextView tvEmail1 = (TextView) findViewById(R.id.email1);
        TextView tvEmail2 = (TextView) findViewById(R.id.email2);
        TextView tvEmail3 = (TextView) findViewById(R.id.email3);

        // The functionality for sending an email to developers.
        tvEmail1.setOnClickListener(new TextView.OnClickListener(){
            public void onClick(View view){
                Intent email = new Intent(Intent.ACTION_SEND);
                email.setType("plain/text");
                String address = tvEmail1.getText().toString();
                email.putExtra(Intent.EXTRA_EMAIL, address);
                email.putExtra(Intent.EXTRA_SUBJECT, "Feedback and Question about COVIDCaster");
                startActivity(email);
            }
        });
        tvEmail2.setOnClickListener(new TextView.OnClickListener(){
            public void onClick(View view){
                Intent email = new Intent(Intent.ACTION_SEND);
                email.setType("plain/text");
                String address = tvEmail2.getText().toString();
                email.putExtra(Intent.EXTRA_EMAIL, address);
                email.putExtra(Intent.EXTRA_SUBJECT, "Feedback and Question about COVIDCaster");
                startActivity(email);
            }
        });
        tvEmail3.setOnClickListener(new TextView.OnClickListener(){
            public void onClick(View view){
                Intent email = new Intent(Intent.ACTION_SEND);
                email.setType("plain/text");
                String address = tvEmail3.getText().toString();
                email.putExtra(Intent.EXTRA_EMAIL, address);
                email.putExtra(Intent.EXTRA_SUBJECT, "Feedback and Question about COVIDCaster");
                startActivity(email);
            }
        });


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
            case R.id.nav_home:
                intent = new Intent(this, MainActivity.class);
                break;
            default:
                intent = new Intent(this, HelpActivity.class);
        }
        startActivity(intent);

        DrawerLayout drawer = findViewById(R.id.drawer_layout_help);
        drawer.closeDrawer(GravityCompat.START);
        return true;

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout_help);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
