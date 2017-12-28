package com.example.k.lokalizator3044;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
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
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {


    //TODO: WYJEBAC NIEUZYWANE ZMIENNE
    BluetoothManager mBluetoothManager;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothLeScanner mBluetoothScanner;
    private Handler mHandler = new Handler();
    private SimpleCursorAdapter cursorAdapter;

    ArrayList<BluetoothDevice> scannedDevices = new ArrayList<>();
    ArrayList<String> scannedDevicesNames = new ArrayList<>();

    HashMap<String, BluetoothDevice> myDevices = new HashMap<>();
    HashMap<String, Boolean> myDevicesState = new HashMap<>();
    HashMap<String, BluetoothGatt> myGatts = new HashMap<>();

    int currentDeviceId = 0;
    int numberOfDevices = 0;
    Uri uri;
    ListView itagList;
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    //(☞ ͡° ͜ʖ ͡°)☞ STOPS SCANNING AFTER 5 SECONDS
    private static final long SCAN_PERIOD = 5000;
    private final static int REQUEST_ENABLE_BT = 1;
    int deviceIndex = 0;
    //String[] scannedDevices = new String[]{};
    ArrayAdapter<String> adapter;
    ListView rawItagList;
    Button cancelBtn;
    public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";
    public Map<String, String> uuids = new HashMap<String, String>();
    ArrayList<BluetoothGatt> bg;
    Button rawCancel;
    AlertDialog.Builder builder;
    BluetoothGatt bluetoothGatt;
    BluetoothDevice device;
    boolean itsNewDevice;
    DBHelper dbHelper = new DBHelper(this);
    boolean scanningActive;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestBluetoothPermission();
        requestBluetoothAdminPermission();

        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        mBluetoothScanner = mBluetoothAdapter.getBluetoothLeScanner();


        checkBleSupport();
        if (!isBluetoothEnable()) requestBluetoothEnable();
        requestLocationPermission();

        //(☞ ͡° ͜ʖ ͡°)☞ TOOLBAR - TEN PASEK NA GÓRZE
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //(☞ ͡° ͜ʖ ͡°)☞ LATAJĄCY PRZYCISK
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: TYLE WYSTARCZY?
                if (isBluetoothEnable()) {
                    itsNewDevice = true;
                    startScan(null);
                } else requestBluetoothEnable();
            }
        });

        //(☞ ͡° ͜ʖ ͡°)☞ TO MENU WYSUWANE Z BOKU PO LEWEJ
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        scanningActive = false;
        myDevicesState = dbHelper.setFalseForEveryRecord();

        itagList = findViewById(R.id.itag_list);
        wypelnijListe();

        itagList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(MainActivity.this, "Click id = "+id, Toast.LENGTH_SHORT).show();
                Log.d("MainActivity", "Click id = " + id);
                Log.d("MainActivity", "position = " + position);
//                uri = Uri.parse(MyContentProvider.URI_ZAWARTOSCI+"/"+id);
//                Cursor c = getContentResolver().query(uri,new String[] {DBHelper.BD_ADDRESS}, null, null, null);
//                c.moveToFirst();
//                Log.d("MainActivity", "Click address = "+c.getString(c.getColumnIndex(DBHelper.BD_ADDRESS)));
//                c.close();
//
//                Intent mIntent = new Intent(MainActivity.this, ItagActivity.class);
//                mIntent.putExtra("dev", uri);
//                mIntent.putExtra("a", true);
//                //mIntent.putExtra("item", item);
//                // MainActivity.this.startActivityForResult(mIntent, 1);
//                startActivity(mIntent);
            }
        });
    }

    //TODO: ZROBIĆ TO NA BLE NIE ZWYŁYM BT [https://www.bignerdranch.com/blog/bluetooth-low-energy-part-1/]
    public void checkBleSupport() {
        //(☞ ͡° ͜ʖ ͡°)☞ SPRAWDZA CZY TELEFON MA BLE
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Brak BLE");
            builder.setMessage("To urządzenie nie wspiera technologii Bluetooth Low Energy.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    finish();
                }
            });
            builder.show();
        }
    }

    public boolean isBluetoothEnable() {
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            return false;
        } else return true;
    }

    public void requestBluetoothEnable() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    public void requestLocationPermission() {
        if (!(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_FINE_LOCATION);
        }
    }

    public void requestBluetoothPermission() {
        if (!(checkSelfPermission(Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED)) {
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH}, PERMISSION_REQUEST_FINE_LOCATION);
        }
    }

    public void requestBluetoothAdminPermission() {
        if (!(checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED)) {
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH_ADMIN}, PERMISSION_REQUEST_FINE_LOCATION);
        }
    }

    private void wypelnijListe() {
        Log.d("LOKLIZATOR", "Wypełnij listę!");
        String[] mapujZ = new String[]{DBHelper.NAME, DBHelper.ADDRESS};
        int[] mapujDo = new int[]{R.id.itag_name, R.id.if_enabled};

        cursorAdapter = new SimpleCursorAdapter(this, R.layout.itag, null, mapujZ, mapujDo, 0);
        cursorAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(final View view, final Cursor cursor, int i) {
                numberOfDevices = cursorAdapter.getCount();
                Log.d("MainActivity", "numberOfDevices = " + numberOfDevices);

                if (i != cursor.getColumnIndexOrThrow(DBHelper.NAME)) {
                    final Switch s = (Switch) view;

                    if (myDevicesState.get(cursor.getString(cursor.getColumnIndex(DBHelper.ADDRESS))))
                        s.setChecked(true);
                    else s.setChecked(false);

                    view.setTag(cursor.getString(cursor.getColumnIndex(DBHelper.ADDRESS)));


                    s.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                            long id = cursorAdapter.getItemId(cursor.getPosition());
                            Log.d("LOKLIZATOR", "tag = " + view.getTag());

                            if (b) {
                                itsNewDevice = false;
                                startScan((String)view.getTag());
                            } else {
                                disconnectDeviceSelected((String)view.getTag());
                            }
                        }
                    });

                    return true;
                }
                return false;
            }
        });

        itagList.setAdapter(cursorAdapter);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 1) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {

            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projekcja = {DBHelper.ID, DBHelper.ADDRESS, DBHelper.NAME, DBHelper.WORKING_MODE, DBHelper.RINGTONE, DBHelper.DISTANCE, DBHelper.CLICK, DBHelper.DOUBLE_CLICK};
        CursorLoader loaderKursora = new CursorLoader(this, MyContentProvider.URI_ZAWARTOSCI, projekcja, null, null, null);
        return loaderKursora;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
    }

    //(☞ ͡° ͜ʖ ͡°)☞ CHOWAJKA PANELU WYSUWANEGO
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //(☞ ͡° ͜ʖ ͡°)☞ POKAZUJE TE OPCJE PO PRAWEJ NA TOOLBARZE
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //(☞ ͡° ͜ʖ ͡°)☞ POSZCZEGÓLNE OPCJE Z MENU WYSUWANEGO
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_main) {
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //(☞ ͡° ͜ʖ ͡°)☞ SPRAWDZA CZY PRZYZNANO PERMISSION
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_FINE_LOCATION: {
                if (!(grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Brak uprawnień");
                    builder.setMessage("Bez uprawnień lokalizacji aplikacja nie będzie w stanie wyszukiwać urządzeń.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }


    //(☞ ͡° ͜ʖ ͡°)☞ START WYSZUKIWANIA URZĄDZEŃ
    public void startScan(String address) {
        Log.d("MainActivity", "StartScan()");

        final String addr = address;

        if (!scanningActive) {

            scanningActive = true;
            scannedDevicesNames.clear();
            scannedDevices.clear();

            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    mHandler.removeCallbacks(this);
                    mBluetoothScanner.startScan(scanCallback);
                }
            });
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopScan(addr);
                }
            }, SCAN_PERIOD);
        }
    }

    //(☞ ͡° ͜ʖ ͡°)☞ DEVICE SCAN CALLBACK - WYNIKI WYSZUKANIA URZĄDZEŃ
    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.d("MainActivity", "onScanResult()");

            if (!scannedDevices.contains(result.getDevice())) {
                boolean isInDatabase = false;
                if(itsNewDevice) {
                    Cursor cursor = getContentResolver()
                            .query(MyContentProvider.URI_ZAWARTOSCI, new String[]{DBHelper.ADDRESS}, null, null, null);

                    if (cursor.moveToFirst()) {
                        do {
                            if (cursor.getString(cursor.getColumnIndex(DBHelper.ADDRESS)).equals(result.getDevice().getAddress()))
                                isInDatabase = true;
                        } while (cursor.moveToNext());
                    }
                    cursor.close();
                }

                if (!isInDatabase || !itsNewDevice) {
                    scannedDevices.add(result.getDevice());
                    if (result.getDevice().getName() == null || result.getDevice().getName().equals("")) {
                        Log.d("MainActivity", "onScanResult() - new device (no name)");
                        scannedDevicesNames.add(result.getDevice().getAddress());
                    } else {
                        Log.d("MainActivity", "onScanResult() - new iTag");
                        scannedDevicesNames.add(result.getDevice().getName());
                    }
                    Log.d("MainActivity", "newitag = true");
                } else {
                    Log.d("MainActivity", "newitag = false");
                }
            }
        }
    };

    //(☞ ͡° ͜ʖ ͡°)☞ STOP WYSZUKIWANIA URZĄDZEŃ
    public void stopScan(String address) {
        Log.d("BluetoothLEService", "StopScan()");
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                mBluetoothScanner.stopScan(scanCallback);
            }
        });
        if (itsNewDevice) adaptujListe();
        else findDevice(address);
        scanningActive = false;
    }

    //(☞ ͡° ͜ʖ ͡°)☞ WYPEŁNIA LISTĘ WYNIKAMI
    void adaptujListe() {
        Log.d("MainActivity", "adaptujListe()");

        if (!scannedDevices.isEmpty()) {
            Log.d("MainActivity", "devicesDiscovered nie jest empty");

            builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Wybierz urządzenie");

            builder.setItems(scannedDevicesNames.toArray(new String[0]), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    numberOfDevices++;
                    myDevices.put(scannedDevices.get(which).getAddress(), scannedDevices.get(which));
                    Log.d("MainActivity", "Id który będzie dodany: " + numberOfDevices);

                    connectToDeviceSelected(scannedDevices.get(which));
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        } else
            Toast.makeText(MainActivity.this, "Nie znaleziono żadnych urządzeń!", Toast.LENGTH_SHORT).show();
    }

    public void findDevice(String address) {
        Log.d("MainActivity", "findDevice()");
        Log.d("MainActivity", "findDevice() - address: "+address);
        Log.d("MainActivity", "devs size: "+scannedDevices.size());

        //if (!scannedDevices.isEmpty() && address != null && !address.equals("")) {
            Log.d("MainActivity", "lista zeskanowanych nie jest pusta, ani adres");
            for(BluetoothDevice bd : scannedDevices) {
                Log.d("MainActivity", "sprawdzanie elementu: " + bd.getAddress());
                if (bd.getAddress().equals(address)) {
                    connectToDeviceSelected(bd);
                }
            //}
        }
    }


    //(☞ ͡° ͜ʖ ͡°)☞ POŁĄCZENIE DO URZĄDZENIA
    public void connectToDeviceSelected(BluetoothDevice bd) {
        Log.d("MainActivity", "connectToDeviceSelected()");
        bluetoothGatt = bd.connectGatt(this, false, gattCallback);

        myGatts.put(bd.getAddress(), bluetoothGatt);
        myDevices.put(bd.getAddress(), bd);
        myDevicesState.put(bd.getAddress(), true);
        Toast.makeText(MainActivity.this, "Połączono do " + bd.getAddress(), Toast.LENGTH_SHORT).show();

        if (itsNewDevice) {
            myDevicesState.put(bd.getAddress(), true);

            Intent intent = new Intent(getApplicationContext(), AddingActivity.class);
            intent.putExtra("itag", bd);
            intent.putExtra("edit", false);
            startActivity(intent);
        }
    }

    public void disconnectDeviceSelected(String address) {
        Log.d("AddingActivity", "disconnectDeviceSelected()");
        if (myGatts.get(address) != null) {
            myGatts.get(address).disconnect();
            myGatts.remove(address);

            myDevices.remove(address);
            myDevicesState.put(address, false);
        }
    }


    //(☞ ͡° ͜ʖ ͡°)☞ CALLBACK DO DEVICE CONNECT
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            // this will get called anytime you perform a read or write characteristic operation
            MainActivity.this.runOnUiThread(new Runnable() {
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
                case BluetoothProfile.STATE_DISCONNECTED:
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Log.d("MainActivity", "ROZŁĄCZONO Z " + gatt.getDevice().getAddress());
                        }
                    });
                    break;
                case BluetoothProfile.STATE_CONNECTED:
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Log.d("MainActivity", "POŁĄCZONO Z " + gatt.getDevice().getAddress());

                        }
                    });

                    // discover services and characteristics for this device
                    bluetoothGatt.discoverServices();

                    break;
                default:
                    MainActivity.this.runOnUiThread(new Runnable() {
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
            MainActivity.this.runOnUiThread(new Runnable() {
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
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    //peripheralTextView.append("Service disovered: "+uuid+"\n");
                    Log.d("SERVICES", "Service disovered: "+uuid);
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
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        //peripheralTextView.append("Characteristic discovered for service: "+charUuid+"\n");
                        Log.d("CHARACTERISTICS", "Characteristic discovered for service: "+charUuid);
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
