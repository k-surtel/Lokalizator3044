package com.example.k.lokalizator3044;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.k.lokalizator3044.DatabaseManagement.DBHelper;
import com.example.k.lokalizator3044.DatabaseManagement.MyContentProvider;

public class AddingActivity extends AppCompatActivity {

    boolean newItag;
    Uri uri;
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

    Intent returnIntent = new Intent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        Bundle myBundle = getIntent().getExtras();
        deviceAddress = myBundle.getString(MainActivity.ITAG_ACTIVITY);
        newItag = myBundle.getBoolean(MainActivity.NEW_ITAG_ACTIVITY);


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adding);

        if (!newItag) {
            Cursor cursor = getContentResolver().query(MyContentProvider.URI_ZAWARTOSCI, null, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    if (deviceAddress.equals(cursor.getString(cursor.getColumnIndex(DBHelper.ADDRESS)))) {
                        id = cursor.getInt(cursor.getColumnIndex(DBHelper.ID));
                        name = cursor.getString(cursor.getColumnIndex(DBHelper.NAME));
                        workingMode = cursor.getString(cursor.getColumnIndex(DBHelper.WORKING_MODE));
                        ringtone = cursor.getString(cursor.getColumnIndex(DBHelper.RINGTONE));
                        distance = cursor.getString(cursor.getColumnIndex(DBHelper.DISTANCE));
                        click = cursor.getString(cursor.getColumnIndex(DBHelper.CLICK));
                        doubleClick = cursor.getString(cursor.getColumnIndex(DBHelper.DOUBLE_CLICK));
                        uri = Uri.parse(MyContentProvider.URI_ZAWARTOSCI + "/" + id);
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        cancelBtn = findViewById(R.id.add_cancel_btn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(newItag){
                    returnIntent.putExtra("a", deviceAddress);
                    setResult(RESULT_CANCELED, returnIntent);
                } else setResult(RESULT_OK, returnIntent);
                finish();
            }
        });

        saveBtn = findViewById(R.id.add_save_btn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("LOKLIZATOR", "Save button pressed!");

                if (!isEverythingSet())
                    Toast.makeText(AddingActivity.this, "Wypełnij wszystkie pola!", Toast.LENGTH_SHORT).show();
                else {
                    ContentValues values = new ContentValues();
                    values.put(DBHelper.ADDRESS, deviceAddress);
                    values.put(DBHelper.NAME, name);
                    values.put(DBHelper.WORKING_MODE, workingMode);
                    values.put(DBHelper.RINGTONE, ringtone);
                    values.put(DBHelper.DISTANCE, distance);
                    values.put(DBHelper.CLICK, click);
                    values.put(DBHelper.DOUBLE_CLICK, doubleClick);

                    if (newItag)
                        getContentResolver().insert(MyContentProvider.URI_ZAWARTOSCI, values);
                    else getContentResolver().update(uri, values, null, null);

                    setResult(RESULT_OK, returnIntent);
                    finish();
                }
            }

        });

    }

    public boolean isNewItag() {
        return newItag;
    }

    public String getAddress(){
        return deviceAddress;
    }

    private boolean isEverythingSet() {
        if(name == null || workingMode == null || ringtone == null || distance == null || click == null || doubleClick == null)
            return false;
        else return true;
    }
}
