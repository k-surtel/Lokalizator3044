package com.example.k.lokalizator3044;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceScreen;
import android.widget.Toast;

/**
 * Created by K on 31.12.2017.
 */

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.content_settings);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // To get a preference
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
//        GPSTracker gps = new GPSTracker(getContext());
//
//
//        Preference getLocalization = preferenceScreen.findPreference("gps_localization");
//        getLocalization.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//            @Override
//            public boolean onPreferenceClick(Preference preference) {
//                // check if GPS enabled
//                if(gps.canGetLocation()){
//                    double latitude = gps.getLatitude();
//                    double longitude = gps.getLongitude();
//
//                    getLocalization.setSummary("Szerokość: "+latitude+"/nDługość:"+longitude);
//
//                }else{
//                    gps.showSettingsAlert();
//                }
//                return true;
//            }
//        });
    }
}