package com.example.k.lokalizator3044;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toolbar;

public class AddingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adding);

        //(☞ ͡° ͜ʖ ͡°)☞ LISTY ROZWIJANE - SPINNER
        Spinner modeSpinner = (Spinner)findViewById(R.id.add_mode_spinner);
        ArrayAdapter<CharSequence> modeAdapter = ArrayAdapter.createFromResource(this,
                R.array.mode_array, android.R.layout.simple_spinner_item);
        modeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modeSpinner.setAdapter(modeAdapter);

        Spinner ringtoneSpinner = (Spinner)findViewById(R.id.add_ringtone_spinner);
        ArrayAdapter<CharSequence> ringtoneAdapter = ArrayAdapter.createFromResource(this,
                R.array.ringtones_array, android.R.layout.simple_spinner_item);
        ringtoneAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ringtoneSpinner.setAdapter(ringtoneAdapter);

        Spinner distanceSpinner = (Spinner)findViewById(R.id.add_distance_spinner);
        ArrayAdapter<CharSequence> distanceAdapter = ArrayAdapter.createFromResource(this,
                R.array.distance_array, android.R.layout.simple_spinner_item);
        distanceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        distanceSpinner.setAdapter(distanceAdapter);

        Spinner buttonSpinner = (Spinner)findViewById(R.id.add_button_spinner);
        ArrayAdapter<CharSequence> buttonAdapter = ArrayAdapter.createFromResource(this,
                R.array.button_array, android.R.layout.simple_spinner_item);
        buttonAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        buttonSpinner.setAdapter(buttonAdapter);

    }
}
