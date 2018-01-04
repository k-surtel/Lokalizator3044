package com.example.k.lokalizator3044;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by K on 31.12.2017.
 */

public class SettingsFragment extends PreferenceFragmentCompat {

    EditTextPreference radius;

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

        double latitude = Double.longBitsToDouble(sp.getLong("latitude", 0));
        double longitude = Double.longBitsToDouble(sp.getLong("longitude", 0));

        Log.d("USTAWIENIA", "latitude = "+latitude);
        Log.d("USTAWIENIA", "longitude = "+longitude);

        GPSTracker gps = new GPSTracker(getContext());



        Preference getLocalization = preferenceScreen.findPreference("gps_localization");
        if(latitude != 0 && longitude != 0)
            getLocalization.setSummary("Szerokość: "+latitude+"\nDługość: "+longitude);


        getLocalization.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // check if GPS enabled
                if(gps.canGetLocation()){
                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();

                    SharedPreferences.Editor edit = sp.edit();
                    edit.putLong("latitude", Double.doubleToRawLongBits(latitude));
                    edit.putLong("longitude", Double.doubleToRawLongBits(longitude));
                    edit.commit();


                    getLocalization.setSummary("Szerokość: "+latitude+"\nDługość: "+longitude);

                }else{
                    gps.showSettingsAlert();
                }
                return true;
            }
        });


        radius = (EditTextPreference)findPreference("gps_radius");
        if(radius.getText() != null && !radius.getText().equals(""))
            radius.setSummary(radius.getText());
        radius.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (newValue != null && !newValue.equals("")) {
                    preference.setSummary(newValue.toString());
                    return true;
                }
                return true;
            }
        });
    }
}