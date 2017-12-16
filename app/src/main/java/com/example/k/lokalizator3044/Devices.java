package com.example.k.lokalizator3044;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by K on 12.12.2017.
 */

public class Devices {
    //public HashMap<String, BluetoothDevice> bleDevices = new HashMap<>();
    BluetoothDevice bleDevice;
    BluetoothGatt bluetoothGatt;
    Context context;
    BluetoothGattCallback callback;

    //(☞ ͡° ͜ʖ ͡°)☞ POŁĄCZENIE DO URZĄDZENIA
    public void connectToDeviceSelected() {
        //peripheralTextView.append("Trying to connect to device at index: " + deviceIndexInput.getText() + "\n");
        //int deviceSelected = Integer.parseInt(deviceIndexInput.getText().toString());
        Log.d("Devices", "connectToDeviceSelected()");
        bluetoothGatt = bleDevice.connectGatt(context, false, callback);
        //Toast.makeText(AddingActivity.this, "Połączono", Toast.LENGTH_SHORT).show();
    }

    public void disconnectDeviceSelected() {
        Log.d("Devices", "disconnectDeviceSelected()");
        //bluetoothGatt = bleDevice.connectGatt(context, false, callback);
        bluetoothGatt.disconnect();
    }



}
