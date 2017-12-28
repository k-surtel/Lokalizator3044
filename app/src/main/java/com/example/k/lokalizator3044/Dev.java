package com.example.k.lokalizator3044;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by K on 23.12.2017.
 */

public class Dev {

    BluetoothDevice device;
    BluetoothGatt gatt;
    Context context;
    BluetoothGattCallback gattCallback;

    Dev(BluetoothDevice device, Context context, BluetoothGattCallback gattCallback) {
        this.device = device;
        this.context = context;
        this.gattCallback = gattCallback;
    }


    //(☞ ͡° ͜ʖ ͡°)☞ POŁĄCZENIE DO URZĄDZENIA
    public void connectToDeviceSelected() {
        gatt = device.connectGatt(context, false, gattCallback);
        Toast.makeText(context, "Połączono do " + device.getAddress(), Toast.LENGTH_SHORT).show();
    }

    public void disconnectDeviceSelected() {
        Log.d("AddingActivity", "disconnectDeviceSelected()");
        gatt.disconnect();
        Log.d("Dev", gatt.getDevice().getAddress());
    }


}