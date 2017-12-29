package com.example.k.lokalizator3044;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AddingActivity extends AppCompatActivity {

    boolean newItag;
    Uri uri;
    EditText addName;
    Spinner modeSpinner;
    Spinner ringtoneSpinner;
    Spinner distanceSpinner;
    Spinner clickSpinner;
    Spinner doubleClickSpinner;
    String deviceAddress;
    Button cancelBtn;
    Button saveBtn;

    int id;
    String name;
    String workingMode;
    String ringtone;
    String distance;
    String click;
    String doubleClick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        Bundle myBundle = getIntent().getExtras();
        deviceAddress = myBundle.getString(MainActivity.ITAG_ACTIVITY);
        newItag = myBundle.getBoolean(MainActivity.NEW_ITAG_ACTIVITY);


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adding);

        addName = (EditText)findViewById(R.id.add_name);

        //(☞ ͡° ͜ʖ ͡°)☞ LISTY ROZWIJANE - SPINNERY
        modeSpinner = (Spinner)findViewById(R.id.add_mode_spinner);
        ArrayAdapter<CharSequence> modeAdapter = ArrayAdapter.createFromResource(this,
                R.array.mode_array, android.R.layout.simple_spinner_item);
        modeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modeSpinner.setAdapter(modeAdapter);

        ringtoneSpinner = (Spinner)findViewById(R.id.add_ringtone_spinner);
        ArrayAdapter<CharSequence> ringtoneAdapter = ArrayAdapter.createFromResource(this,
                R.array.ringtones_array, android.R.layout.simple_spinner_item);
        ringtoneAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ringtoneSpinner.setAdapter(ringtoneAdapter);

        distanceSpinner = (Spinner)findViewById(R.id.add_distance_spinner);
        ArrayAdapter<CharSequence> distanceAdapter = ArrayAdapter.createFromResource(this,
                R.array.distance_array, android.R.layout.simple_spinner_item);
        distanceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        distanceSpinner.setAdapter(distanceAdapter);

        clickSpinner = (Spinner)findViewById(R.id.add_button_spinner);
        ArrayAdapter<CharSequence> clickAdapter = ArrayAdapter.createFromResource(this,
                R.array.click_array, android.R.layout.simple_spinner_item);
        clickAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        clickSpinner.setAdapter(clickAdapter);

        doubleClickSpinner = (Spinner)findViewById(R.id.add_button_double_spinner);
        final ArrayAdapter<CharSequence> doubleClickAdapter = ArrayAdapter.createFromResource(this,
                R.array.double_click_array, android.R.layout.simple_spinner_item);
        doubleClickAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        doubleClickSpinner.setAdapter(doubleClickAdapter);

        if(!newItag) {
            Cursor cursor = getContentResolver().query(MyContentProvider.URI_ZAWARTOSCI, null, null, null, null);
            if(cursor.moveToFirst()){
                do {
                    if(deviceAddress.equals(cursor.getString(cursor.getColumnIndex(DBHelper.ADDRESS)))) {
                        id = cursor.getInt(cursor.getColumnIndex(DBHelper.ID));
                        name = cursor.getString(cursor.getColumnIndex(DBHelper.NAME));
                        workingMode = cursor.getString(cursor.getColumnIndex(DBHelper.WORKING_MODE));
                        ringtone = cursor.getString(cursor.getColumnIndex(DBHelper.RINGTONE));
                        distance = cursor.getString(cursor.getColumnIndex(DBHelper.DISTANCE));
                        click = cursor.getString(cursor.getColumnIndex(DBHelper.CLICK));
                        doubleClick = cursor.getString(cursor.getColumnIndex(DBHelper.DOUBLE_CLICK));
                    }
                } while (cursor.moveToNext());
            }

            addName.setText(name);
            modeSpinner.setSelection(modeAdapter.getPosition(workingMode));
            ringtoneSpinner.setSelection(ringtoneAdapter.getPosition(ringtone));
            distanceSpinner.setSelection(distanceAdapter.getPosition(distance));
            clickSpinner.setSelection(clickAdapter.getPosition(click));
            doubleClickSpinner.setSelection(doubleClickAdapter.getPosition(doubleClick));


            uri = Uri.parse(MyContentProvider.URI_ZAWARTOSCI + "/" + id);
        }

        cancelBtn = findViewById(R.id.add_cancel_btn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        saveBtn = findViewById(R.id.add_save_btn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("LOKLIZATOR", "Save button pressed!");
                ContentValues values = new ContentValues();
                values.put(DBHelper.ADDRESS, deviceAddress);
                values.put(DBHelper.NAME, addName.getText().toString());
                values.put(DBHelper.WORKING_MODE, modeSpinner.getSelectedItem().toString());
                values.put(DBHelper.RINGTONE, ringtoneSpinner.getSelectedItem().toString());
                values.put(DBHelper.DISTANCE, distanceSpinner.getSelectedItem().toString());
                values.put(DBHelper.WORKING_MODE, modeSpinner.getSelectedItem().toString());
                values.put(DBHelper.CLICK, clickSpinner.getSelectedItem().toString());
                values.put(DBHelper.DOUBLE_CLICK, doubleClickSpinner.getSelectedItem().toString());
                //values.put(DBHelper.IF_ENABLED, 1);

                if(newItag) getContentResolver().insert(MyContentProvider.URI_ZAWARTOSCI, values);
                else getContentResolver().update(uri, values, null, null);

                finish();
            }
        });

    }
}
