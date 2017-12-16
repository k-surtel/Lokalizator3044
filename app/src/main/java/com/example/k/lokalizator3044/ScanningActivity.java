package com.example.k.lokalizator3044;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

public class ScanningActivity extends AppCompatActivity {

    /**
     * (☞ ✖ ╭╮ ✖)☞ TO TRZEBA OGARNĄĆĆĆĆĆĆĆĆĆĆĆĆĆĆ :<
     */
    boolean newitag;
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
    BluetoothLEService ble;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanning);

        Log.d("ScanningActivity", "OnCreate()");

        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        mBluetoothScanner = mBluetoothAdapter.getBluetoothLeScanner();

        ble = new BluetoothLEService(mBluetoothScanner, this, R.layout.raw_itag, R.id.raw_itag_name);

        /**
         * (☞ ╯︵╰, )☞ TO ZMIENIĆ ALE POTEM
         */
        iTagsList = findViewById(R.id.found_itags_list);
        iTagsList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("ScanningActivity", "Device chosen index = "+i);
                Intent intent = new Intent(getApplicationContext(), AddingActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                intent.putExtra("itag", devicesDiscovered.get(i));
                intent.putExtra("edit", false);
                startActivity(intent);
                finish();
            }
        });

        cancelBtn = findViewById(R.id.scanning_cancel_btn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //(☞ ͡° ͜ʖ ͡°)☞ TOOLBAR - TEN PASEK NA GÓRZE
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /**
         * (☞ ✖ ╭╮ ✖)☞ TO POTRZEBNE?????
         */
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        /**
         * (☞ ✖ ╭╮ ✖)☞ XXXXXXXXXXXX
         */
        Toast.makeText(ScanningActivity.this, "Trwa wyszukiwanie urządzeń", Toast.LENGTH_SHORT).show();

        startScan();
        //iTagsList.setAdapter(ble.adapter);
        //Log.d("ScanningActivity", "setAdapter()");
    }

    //(☞ ͡° ͜ʖ ͡°)☞ START WYSZUKIWANIA URZĄDZEŃ
    public void startScan() {
        Log.d("BluetoothLEService", "StartScan()");
        devicesDiscovered.clear();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                mBluetoothScanner.startScan(leScanCallback);
            }
        });
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopScan();
            }
        }, SCAN_PERIOD);
    }

    //(☞ ͡° ͜ʖ ͡°)☞ STOP WYSZUKIWANIA URZĄDZEŃ
    public void stopScan() {
        Log.d("BluetoothLEService", "StopScan()");
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                mBluetoothScanner.stopScan(leScanCallback);
            }
        });
        if (newitag) startScan();
        else adaptujListe();
    }



    //(☞ ͡° ͜ʖ ͡°)☞ WYPEŁNIA LISTĘ WYNIKAMI - ZMIEŃ NAZWĘ..................
    void adaptujListe() {
        Log.d("BluetoothLEService", "adaptujListe()");
        adapter = new ArrayAdapter<String>(this, R.layout.raw_itag, R.id.raw_itag_name, foundItags);
        iTagsList.setAdapter(adapter);
    }

    //(☞ ͡° ͜ʖ ͡°)☞ DEVICE SCAN CALLBACK - WYNIKI WYSZUKANIA URZĄDZEŃ
    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.d("BluetoothLEService", "onScanResult()");
            if (!foundIds.contains(result.getDevice().getAddress())) {
                foundIds.add(result.getDevice().getAddress());
                if (!(result.getDevice().getName() == null)) {
                    Log.d("BluetoothLEService", "onScanResult() - new iTag");
                    foundItags.add(result.getDevice().getName());
                }
                else {
                    Log.d("BluetoothLEService", "onScanResult() - new iTag - null name");
                    foundItags.add(result.getDevice().getAddress());
                }
                devicesDiscovered.add(result.getDevice());
                Log.d("ScanningActivity", "Device index in array = "+deviceIndex);
                deviceIndex++;
                newitag = true;
                Log.d("ScanningActivity", "newitag = true");
            } else {
                Log.d("ScanningActivity", "newitag = true");
                newitag = false;
            }
        }
    };

    //(☞ ͡° ͜ʖ ͡°)☞ POKAZUJE TE OPCJE PO PRAWEJ NA TOOLBARZE
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_refresh, menu);
        return true;
    }

    //(☞ ͡° ͜ʖ ͡°)☞ POSZCZEGÓLNE OPCJE Z PRZYCISKU PO PRAWEJ NA TOOLBARZE
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_reload) {
            Toast.makeText(ScanningActivity.this, "Trwa wyszukiwanie urządzeń", Toast.LENGTH_SHORT).show();
            startScan();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
