package com.example.k.lokalizator3044;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ItagActivity extends AppCompatActivity {

    Button btn, btn2, btn3;
    BluetoothGatt bluetoothGatt;
    Uri uri;
    BluetoothDevice bd;
    BluetoothManager mBluetoothManager;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothLeScanner mBluetoothScanner;
    String addr;
    private Handler mHandler = new Handler();
    //(☞ ͡° ͜ʖ ͡°)☞ STOPS SCANNING AFTER 5 SECONDS
    private static final long SCAN_PERIOD = 5000;
    Cursor c;
    //(☞ ͡° ͜ʖ ͡°)☞ NIE WIEM
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itag);

        Bundle bundle = getIntent().getExtras();
        uri = (Uri)bundle.get("dev");
        Log.d("ItagActivity", uri.toString());
        boolean firstTimeCheck = bundle.getBoolean("a");

        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        mBluetoothScanner = mBluetoothAdapter.getBluetoothLeScanner();


        c = getContentResolver().query(uri,new String[] {DBHelper.BD_ADDRESS}, null, null, null);
        c.moveToFirst();
        Log.d("ItagActivity", "Click address = "+c.getString(c.getColumnIndex(DBHelper.BD_ADDRESS)));
        addr = c.getString(c.getColumnIndex(DBHelper.BD_ADDRESS));
        c.close();


        //beep
        btn = (Button)findViewById(R.id.button2);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        //odłącz
        btn2 = findViewById(R.id.button3);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                disconnectDeviceSelected();
                finish();
            }
        });
        btn2.setVisibility(View.GONE);

        btn3 = findViewById(R.id.button4);
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startScan();
            }
        });
        btn3.setVisibility(View.VISIBLE);
    }

    public void disconnectDeviceSelected() {
        Log.d("ItagActivity", "disconnectDeviceSelected()");
        bluetoothGatt.disconnect();
        bluetoothGatt.close();
    }

    //(☞ ͡° ͜ʖ ͡°)☞ START WYSZUKIWANIA URZĄDZEŃ
    public void startScan() {
        Log.d("BluetoothLEService", "StartScan()");
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
    }


    //(☞ ͡° ͜ʖ ͡°)☞ DEVICE SCAN CALLBACK - WYNIKI WYSZUKANIA URZĄDZEŃ
    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.d("ItagActivity", "onScanResult()");
            if(result.getDevice().getAddress().equals(addr)) {
                Log.d("ItagActivity", "MATCH FOUND");
                bd = result.getDevice();
                connectToDeviceSelected();
            }
        }
    };

    //(☞ ͡° ͜ʖ ͡°)☞ POŁĄCZENIE DO URZĄDZENIA
    public void connectToDeviceSelected() {
        //peripheralTextView.append("Trying to connect to device at index: " + deviceIndexInput.getText() + "\n");
        //int deviceSelected = Integer.parseInt(deviceIndexInput.getText().toString());
        bluetoothGatt = bd.connectGatt(this, true, btleGattCallback);
        Toast.makeText(ItagActivity.this, "Połączono", Toast.LENGTH_SHORT).show();
        btn2.setVisibility(View.VISIBLE);
        btn3.setVisibility(View.GONE);
    }

    //(☞ ͡° ͜ʖ ͡°)☞ CALLBACK DO DEVICE CONNECT
    private final BluetoothGattCallback btleGattCallback = new BluetoothGattCallback() {

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            // this will get called anytime you perform a read or write characteristic operation
            ItagActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    //peripheralTextView.append("device read or wrote to\n");
                }
            });
        }

        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            // this will get called when a device connects or disconnects
            System.out.println(newState);
            switch (newState) {
                case 0:
                    ItagActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            //peripheralTextView.append("device disconnected\n");
                            Log.d("LOKLIZATOR", "ROZŁĄCZONO!!!!!!!!!!!!!!!!!!");
                        }
                    });
                    break;
                case 2:
                    ItagActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            //peripheralTextView.append("device connected\n");
                            Toast.makeText(ItagActivity.this, "Połączono", Toast.LENGTH_LONG).show();
                        }
                    });

                    // discover services and characteristics for this device
                    bluetoothGatt.discoverServices();

                    break;
                default:
                    ItagActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            //peripheralTextView.append("we encounterned an unknown state, uh oh\n");
                        }
                    });
                    break;
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            // this will get called after the client initiates a 			BluetoothGatt.discoverServices() call
            ItagActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    // peripheralTextView.append("device services have been discovered\n");
                }
            });
            displayGattServices(bluetoothGatt.getServices());
        }

        @Override
        // Result of a characteristic read operation
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }
    };


    //(☞ ͡° ͜ʖ ͡°)☞ NIE WIEM
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {

            final String uuid = gattService.getUuid().toString();
            System.out.println("Service discovered: " + uuid);
            ItagActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    //peripheralTextView.append("Service disovered: "+uuid+"\n");
                }
            });
            new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic :
                    gattCharacteristics) {

                final String charUuid = gattCharacteristic.getUuid().toString();
                System.out.println("Characteristic discovered for service: " + charUuid);
                ItagActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        //peripheralTextView.append("Characteristic discovered for service: "+charUuid+"\n");
                    }
                });

            }
        }
    }
    //(☞ ͡° ͜ʖ ͡°)☞ NIE WIEM
    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {

        System.out.println(characteristic.getUuid());
    }
}
