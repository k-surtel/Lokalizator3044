package com.example.k.lokalizator3044;

import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.text.InputType;
import android.util.Log;
import android.widget.TextView;

import static android.app.Activity.RESULT_OK;

/**
 * Created by K on 31.12.2017.
 */

public class ItagSettingFragment extends PreferenceFragmentCompat {

    String address;
    boolean newItag;
    EditTextPreference itagName;
    EditTextPreference itagDistance;
    Preference ringtonePreference;

    private static final int RINGTONE_REQUEST = 1;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.content_change_itag);

        address = ((AddingActivity)getActivity()).getAddress();
        newItag = ((AddingActivity)getActivity()).isNewItag();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        itagName = (EditTextPreference)findPreference("itag_name");
        if(!newItag) {
            itagName.setText(((AddingActivity)getActivity()).name);
            itagName.setSummary(((AddingActivity)getActivity()).name);
        } else {
            itagName.setText("");
            itagName.setSummary("Podaj nazwę urządzenia");
        }

        itagName.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                ((AddingActivity)getActivity()).name = newValue.toString();
                if(!newValue.equals("")) preference.setSummary(newValue.toString());
                else preference.setSummary("Wpisz nazwę urządzenia");
                return true;
            }
        });

        ListPreference itagWorkingMode = (ListPreference)findPreference("itag_mode");
        if(!newItag) itagWorkingMode.setValue(((AddingActivity)getActivity()).workingMode);
        else ((AddingActivity)getActivity()).workingMode = itagWorkingMode.getValue();
        itagWorkingMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                ((AddingActivity)getActivity()).workingMode = newValue.toString();
                return true;
            }
        });


        ringtonePreference = findPreference("ringtone_preference");
        if(!newItag) ringtonePreference.setSummary(((AddingActivity)getActivity()).ringtone);
        ringtonePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Ringtone");
                if (((AddingActivity)getActivity()).ringtone != null)
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(((AddingActivity)getActivity()).ringtone));
                else intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);

                startActivityForResult(intent, RINGTONE_REQUEST);
                return true;
            }
        });


        itagDistance = (EditTextPreference)findPreference("itag_distance");
        if(!newItag) {
            itagDistance.setText(((AddingActivity)getActivity()).distance);
            itagDistance.setSummary(((AddingActivity)getActivity()).distance);
        } else {
            itagDistance.setText("");
            itagDistance.setSummary("Podaj odległość od urządzenia, po której dostaniesz powiadomienie");
        }
        itagDistance.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                ((AddingActivity)getActivity()).distance = newValue.toString();
                if(newValue != null && !newValue.equals("")) {
                    preference.setSummary(newValue.toString());
                    return true;
                } else {
                    preference.setSummary("Podaj odległość od urządzenia, po której dostaniesz powiadomienie");
                    return true;
                }
            }
        });

        ListPreference itagClick = (ListPreference)findPreference("itag_click");
        if(!newItag) itagClick.setValue(((AddingActivity)getActivity()).click);
        else ((AddingActivity)getActivity()).click = itagClick.getValue();
        itagClick.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                ((AddingActivity)getActivity()).click = newValue.toString();
                return true;
            }
        });


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RINGTONE_REQUEST && resultCode == RESULT_OK) {
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);

            if (uri == null) Log.d("tag", "nie ma uri");
            else {
                ringtonePreference.setSummary(uri.toString());
                ((AddingActivity)getActivity()).ringtone = uri.toString();
            }
        }
    }
}
