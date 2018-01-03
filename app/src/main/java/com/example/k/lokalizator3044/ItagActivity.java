package com.example.k.lokalizator3044;

import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.k.lokalizator3044.DatabaseManagement.DBHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ItagActivity extends AppCompatActivity {

    Button btn, btn2, btn3;
    BluetoothGatt bluetoothGatt;
    Uri uri;
    BluetoothDevice bd;
    BluetoothManager mBluetoothManager;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothLeScanner mBluetoothScanner;
    BluetoothGattService immediateAlertService;
    String addr;
    private Handler mHandler = new Handler();
    //(☞ ͡° ͜ʖ ͡°)☞ STOPS SCANNING AFTER 5 SECONDS
    private static final long SCAN_PERIOD = 5000;
    Cursor c;
    //(☞ ͡° ͜ʖ ͡°)☞ NIE WIEM
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public static final int NO_ALERT = 0x00;
    public static final int MEDIUM_ALERT = 0x01;
    boolean connected = false;
    int alert;
    boolean a = true;
    public static final UUID IMMEDIATE_ALERT_SERVICE = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb");

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


        c = getContentResolver().query(uri,new String[] {DBHelper.ADDRESS}, null, null, null);
        c.moveToFirst();
        Log.d("ItagActivity", "Click address = "+c.getString(c.getColumnIndex(DBHelper.ADDRESS)));
        addr = c.getString(c.getColumnIndex(DBHelper.ADDRESS));
        c.close();



        //beep
        btn = (Button)findViewById(R.id.button2);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(a) {
                    alert = MEDIUM_ALERT;
                    a = false;
                } else {
                    alert = NO_ALERT;
                    a = true;
                }
                //final BluetoothGattCharacteristic characteristic = immediateAlertService.getCharacteristics().get(0);
                //characteristic.setValue(alert, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                //bluetoothGatt.writeCharacteristic(characteristic);
            }
        });

        //odłącz
        btn2 = findViewById(R.id.button3);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //while(bluetoothGatt != null && mBluetoothManager.getConnectionState(bd, BluetoothProfile.GATT) == 2) {
                    Log.d("ItagActivity", "bGatt not null");
                    if(bluetoothGatt != null) {
                        bluetoothGatt.disconnect();
                        bluetoothGatt = null;
                    }
                    broadcastUpdate(ACTION_GATT_DISCONNECTED);
                    //mBluetoothManager.getConnectionState(bd, BluetoothProfile.GATT);
                    Log.d("ItagActivity", "connection state = "+mBluetoothManager.getConnectionState(bd, BluetoothProfile.GATT));
                //}
                //bluetoothGatt = null;
                //disconnectDeviceSelected();
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

        if(mBluetoothManager != null) {
            Log.d("ItagActivity", "bluetoothMsnager not null");
            //if(bluetoothGatt != null) Log.d("ItagActivity", "bluetoothGatt not null");
            if(bd != null) {
                Log.d("ItagActivity", "bluetoothDevice not null");
                } else {
                    List<BluetoothDevice> bdList = mBluetoothManager.getConnectedDevices(BluetoothProfile.GATT);
                    for(BluetoothDevice b : bdList){
                        if(b.getAddress().equals(addr)) bd = b;
                    }
                Log.d("ItagActivity", "get bluetoothDevice from manager");
                    //bluetoothGatt = bd.connectGatt(this, true, btleGattCallback);
                }
            if (mBluetoothManager.getConnectionState(bd, BluetoothProfile.GATT) == 2) {
                Log.d("ItagActivity", "state: connected");
                bluetoothGatt = bd.connectGatt(this, true, btleGattCallback);
                btn2.setVisibility(View.VISIBLE);
                btn3.setVisibility(View.GONE);
            }
        }
    }


    public void disconnectDeviceSelected() {
        Log.d("ItagActivity", "disconnectDeviceSelected()");
        if(bluetoothGatt != null) {
            Log.d("ItagActivity", "bGatt not null");
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
        }
        bluetoothGatt = null;
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
                stopScan();
                bd = result.getDevice();
                connectToDeviceSelected();
            }
        }
    };

    //(☞ ͡° ͜ʖ ͡°)☞ POŁĄCZENIE DO URZĄDZENIA
    public void connectToDeviceSelected() {
        //peripheralTextView.append("Trying to connect to device at index: " + deviceIndexInput.getText() + "\n");
        //int deviceSelected = Integer.parseInt(deviceIndexInput.getText().toString());
        mBluetoothAdapter.cancelDiscovery();
        bluetoothGatt = bd.connectGatt(this, true, btleGattCallback);
        Toast.makeText(ItagActivity.this, "connectToDeviceSelected", Toast.LENGTH_SHORT).show();
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

        NotificationManager notificationManager;
        NotificationCompat.Builder mBuilder;

        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {

            notificationManager = (NotificationManager) ItagActivity.this.getSystemService(Context.NOTIFICATION_SERVICE);
            mBuilder =
                    new NotificationCompat.Builder(ItagActivity.this)
                            .setSmallIcon(R.drawable.common_ic_googleplayservices)
                            .setContentTitle("iTag is connected")
                            .setOngoing(true);

            // this will get called when a device connects or disconnects
            System.out.println(newState);
            switch (newState) {
                case 0:
                    ItagActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            //peripheralTextView.append("device disconnected\n");
                            Log.d("LOKLIZATOR", "ROZŁĄCZONO!!!!!!!!!!!!!!!!!!");
                            btn2.setVisibility(View.GONE);
                            btn3.setVisibility(View.VISIBLE);
                            notificationManager.cancel(1);
                        }
                    });
                    break;
                case 2:
                    ItagActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            //peripheralTextView.append("device connected\n");
                            Toast.makeText(ItagActivity.this, "Połączono", Toast.LENGTH_LONG).show();
                            btn2.setVisibility(View.VISIBLE);
                            btn3.setVisibility(View.GONE);


                            notificationManager.notify(1, mBuilder.build());
                        }
                    });

                    // discover services and characteristics for this device
                    //bluetoothGatt.discoverServices();

                    break;
                default:
                    ItagActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            //peripheralTextView.append("we encounterned an unknown state, uh oh\n");
                            Log.d("ItagActivity", "unknown state");
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
            for (BluetoothGattService service : gatt.getServices()) {
                if (IMMEDIATE_ALERT_SERVICE.equals(service.getUuid())) {
                    immediateAlertService = service;
                    //broadcaster.sendBroadcast(new Intent(IMMEDIATE_ALERT_AVAILABLE));
                    //gatt.readCharacteristic(getCharacteristic(gatt, IMMEDIATE_ALERT_SERVICE, ALERT_LEVEL_CHARACTERISTIC));
                    //setCharacteristicNotification(gatt, immediateAlertService.getCharacteristics().get(0), true);
                }
            }
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

    private void broadcastUpdate(final String action) {
        Log.d("ItagActivity", "broadcastUpdate: disconnect!!!");
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }
}
