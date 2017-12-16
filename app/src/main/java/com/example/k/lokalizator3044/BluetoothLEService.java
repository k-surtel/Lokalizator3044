package com.example.k.lokalizator3044;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by K on 15.12.2017.
 */

public class BluetoothLEService {

    /**
     * (☞ ✖ ╭╮ ✖)☞ TO TRZEBA OGARNĄĆĆĆĆĆĆĆĆĆĆĆĆĆĆ :<
     */
    BluetoothManager mBluetoothManager;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothLeScanner mBluetoothScanner;
    //(☞ ͡° ͜ʖ ͡°)☞ STOPS SCANNING AFTER 5 SECONDS
    private static final long SCAN_PERIOD = 5000;
    private final static int REQUEST_ENABLE_BT = 1;
    int deviceIndex = 0;
    ArrayList<BluetoothDevice> devicesDiscovered = new ArrayList<BluetoothDevice>();
    ArrayList<String> foundItags = new ArrayList<>();
    ArrayList<String> foundIds = new ArrayList<>();
    ArrayAdapter<String> adapter;
    ListView iTagsList;
    Button cancelBtn;
    public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";
    public Map<String, String> uuids = new HashMap<String, String>();
    private Handler mHandler = new Handler();
    Context c;
    int raw_itag;
    int raw_itag_name;




    BluetoothLEService(BluetoothLeScanner mBluetoothScanner, Context c, int raw_itag, int raw_itag_name) {
        Log.d("BluetoothLEService", "Constructor.");
        this.mBluetoothScanner = mBluetoothScanner;
        this.c = c;
        this.raw_itag = raw_itag;
        this.raw_itag_name = raw_itag_name;
    }


}
