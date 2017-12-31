package com.example.k.lokalizator3044;

import android.app.Fragment;
import android.content.Intent;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;


public class SettingsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_no_fab);


        //(☞ ͡° ͜ʖ ͡°)☞ USTAWIENIA WIDOCZKÓFF
        findViewById(R.id.about_content).setVisibility(View.GONE);
        //findViewById(R.id.settings_content).setVisibility(View.VISIBLE);


        //(☞ ͡° ͜ʖ ͡°)☞ TOOLBAR - TEN PASEK NA GÓRZE
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //(☞ ͡° ͜ʖ ͡°)☞ TO MENU WYSUWANE Z BOKU PO LEWEJ
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);





//        //(☞ ͡° ͜ʖ ͡°)☞ LISTY ROZWIJANE - SPINNER
//        Spinner modeSpinner = (Spinner)findViewById(R.id.edit_mode_spinner);
//        // Create an ArrayAdapter using the string array and a default spinner layout
//        ArrayAdapter<CharSequence> modeAdapter = ArrayAdapter.createFromResource(this,
//                R.array.mode_array, android.R.layout.simple_spinner_item);
//        // Specify the layout to use when the list of choices appears
//        modeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        // Apply the adapter to the spinner
//        modeSpinner.setAdapter(modeAdapter);
//
//        Spinner ringtoneSpinner = (Spinner)findViewById(R.id.edit_ringtone_spinner);
//        ArrayAdapter<CharSequence> ringtoneAdapter = ArrayAdapter.createFromResource(this,
//                R.array.ringtones_array, android.R.layout.simple_spinner_item);
//        ringtoneAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        ringtoneSpinner.setAdapter(ringtoneAdapter);
//
//        Spinner distanceSpinner = (Spinner)findViewById(R.id.edit_distance_spinner);
//        ArrayAdapter<CharSequence> distanceAdapter = ArrayAdapter.createFromResource(this,
//                R.array.distance_array, android.R.layout.simple_spinner_item);
//        distanceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        distanceSpinner.setAdapter(distanceAdapter);
//
//        Spinner buttonSpinner = (Spinner)findViewById(R.id.edit_button_spinner);
//        ArrayAdapter<CharSequence> buttonAdapter = ArrayAdapter.createFromResource(this,
//                R.array.click_array, android.R.layout.simple_spinner_item);
//        buttonAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        buttonSpinner.setAdapter(buttonAdapter);
    }

    //(☞ ͡° ͜ʖ ͡°)☞ CHOWAJKA WYSUWANEGO PANELU
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //(☞ ͡° ͜ʖ ͡°)☞ POKAZUJE TE OPCJE PO PRAWEJ NA TOOLBARZE
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    //(☞ ͡° ͜ʖ ͡°)☞ POSZCZEGÓLNE OPCJE Z PRZYCISKU PO PRAWEJ NA TOOLBARZE
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_reload) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //(☞ ͡° ͜ʖ ͡°)☞ POSZCZEGÓLNE OPCJE Z MENU WYSUWANEGO
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_main) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
