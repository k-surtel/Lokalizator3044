package com.example.k.lokalizator3044;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.Switch;
import android.widget.TextView;

import java.util.HashMap;

/**
 * Created by K on 27.12.2017.
 */

public class CustomCursorAdapter extends CursorAdapter {

    HashMap<Integer, Boolean> deviceState = new HashMap<>();
    MainActivity activity;

    public CustomCursorAdapter(Context context, Cursor c, HashMap<Integer, Boolean> deviceState, MainActivity activity) {
        super(context, c);
        this.deviceState = deviceState;
        this.activity = activity;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // when the view will be created for first time,
        // we need to tell the adapters, how each item will look
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View retView = inflater.inflate(R.layout.itag, parent, false);

        return retView;
    }

    @Override
    public void bindView(View view, Context context, final Cursor cursor) {
        // here we are setting our data
        // that means, take the data from the cursor and put it in views

        TextView itagName = view.findViewById(R.id.itag_name);
        itagName.setText(cursor.getString(cursor.getColumnIndex(cursor.getColumnName(cursor.getColumnIndex(DBHelper.NAME)))));

        Switch ifEnabled = view.findViewById(R.id.if_enabled);
        if(deviceState.get(cursor.getInt(cursor.getColumnIndex(DBHelper.ID)))) ifEnabled.setChecked(true);
        else ifEnabled.setChecked(false);

        ifEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Log.d("MainActivity", "kurrrrr "+cursor.getPosition());
                //if(b)
            }
        });
    }
}
