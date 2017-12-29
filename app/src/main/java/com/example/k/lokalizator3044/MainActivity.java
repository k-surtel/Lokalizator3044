package com.example.k.lokalizator3044;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.ActionMode;
import android.view.MenuInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    BluetoothGattCharacteristic bc;

    // 180A Device Information
    public static final UUID SERVICE_DEVICE_INFORMATION_UUID = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");
    public static final String SERVICE_DEVICE_INFORMATION = "0000180a-0000-1000-8000-00805f9b34fb";
    public static final UUID CHAR_MANUFACTURER_NAME_STRING_UUID = UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb");
    public static final String CHAR_MANUFACTURER_NAME_STRING = "00002a29-0000-1000-8000-00805f9b34fb";//
    public static final String CHAR_MODEL_NUMBER_STRING = "00002a24-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_SERIAL_NUMBEAR_STRING = "00002a25-0000-1000-8000-00805f9b34fb";

    // 1802 Immediate Alert
    public static final String SERVICE_IMMEDIATE_ALERT = "00001802-0000-1000-8000-00805f9b34fb";
    public static final UUID CHAR_ALERT_LEVEL_UUID = UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb");
    public static final String CHAR_ALERT_LEVEL = "00002a06-0000-1000-8000-00805f9b34fb"; ///
    // StickNFindではCHAR_ALERT_LEVELに0x01をWriteすると光り、0x02では音が鳴り、0x03では光って鳴る。

    // 180F Battery Service
    public static final String SERVICE_BATTERY_SERVICE = "0000180F-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_BATTERY_LEVEL = "00002a19-0000-1000-8000-00805f9b34fb";

    boolean devinfo = false;
    boolean alert = false;
    static Ringtone currentRingtone;
    private BroadcastReceiver receiver;
    boolean ringtone = false;

    boolean pSwitch = false;
    Switch pressedSwitch;


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
                // Get the cursor, positioned to the corresponding row in the result set
                Cursor cursor = (Cursor) itagList.getItemAtPosition(position);

                // Get the state's capital from this row in the database.
                String address = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.ADDRESS));
                Log.d("MainActivity", "ADDR = " + address);

                if (myDevices.get(address) != null && myDevicesState.get(address) && myGatts.get(address) != null && !alert) {
                    BluetoothGatt gatt = myGatts.get(address);
                    bc = findCharacteristic(address, CHAR_ALERT_LEVEL_UUID);
                    if (bc != null) {
                        bc.setValue(new byte[]{(byte) 0x01});
                        alert = true;
                        gatt.writeCharacteristic(bc);
                    }
                } else if (myDevices.get(address) != null && myDevicesState.get(address) && myGatts.get(address) != null && alert) {
                    BluetoothGatt gatt = myGatts.get(address);
                    bc = findCharacteristic(address, CHAR_ALERT_LEVEL_UUID);
                    if (bc != null) {
                        bc.setValue(new byte[]{(byte) 0x00});
                        alert = false;
                        gatt.writeCharacteristic(bc);
                    }
                }
            }
        });


        itagList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        itagList.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater menuInflater = mode.getMenuInflater();
                menuInflater.inflate(R.menu.menu_delete, menu);
                //Toolbar tb = findViewById(R.id.toolbar);
                //tb.setVisibility(View.GONE);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_delete:
                        kasujZaznaczone();
                        return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                //Toolbar tb = (Toolbar)findViewById(R.id.toolbar);
                //tb.setVisibility(View.VISIBLE);
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

                if (i == cursor.getColumnIndexOrThrow(DBHelper.ADDRESS)) {
                    final Switch s = (Switch) view;

                    if (myDevicesState.get(cursor.getString(cursor.getColumnIndex(DBHelper.ADDRESS))))
                        s.setChecked(true);
                    else s.setChecked(false);

                    view.setTag(cursor.getString(cursor.getColumnIndex(DBHelper.ADDRESS)));


                    s.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            pSwitch = true;
                            pressedSwitch = s;
                            if (b) {
                                itsNewDevice = false;
                                startScan((String) view.getTag());
                            } else {
                                disconnectDeviceSelected((String) view.getTag());
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


    private void kasujZaznaczone() {
        long zaznaczone[] = itagList.getCheckedItemIds();
        for (int i = 0; i < zaznaczone.length; ++i) {
            getContentResolver().delete(ContentUris.withAppendedId(MyContentProvider.URI_ZAWARTOSCI, zaznaczone[i]), null, null);
        }
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
                if (itsNewDevice) {
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
        boolean found = false;
        for (BluetoothDevice bd : scannedDevices) {
            if (bd.getAddress().equals(address)) {
                found = true;
                connectToDeviceSelected(bd);
            }
        }

        if (found) pressedSwitch.setChecked(true);
        else pressedSwitch.setChecked(false);

        pSwitch = false;
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

            pressedSwitch.setChecked(false);
            pSwitch = false;
        }
    }

    public BluetoothGattCharacteristic findCharacteristic(String macAddress, UUID characteristicUUID) {
        Log.d("AddingActivity", "findCharacteristic()");
        BluetoothGatt bluetoothGatt = myGatts.get(macAddress);

        if (bluetoothGatt == null) {
            return null;
        }

        for (BluetoothGattService service : bluetoothGatt.getServices()) {
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUUID);
            if (characteristic != null) {
                return characteristic;
            }
        }
        return null;
    }


    //(☞ ͡° ͜ʖ ͡°)☞ CALLBACK DO DEVICE CONNECT
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            // this will get called anytime you perform a read or write characteristic operation

            Log.d("MainActivity", "onCharacteristicChanged()");

            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);

            UUID uuid = characteristic.getUuid();
            String address = gatt.getDevice().getAddress();

            if (!ringtone) {
                ringtone = true;
                startRing(address);
            } else {
                ringtone = false;
                stopRing();
            }
        }

        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    gatt.discoverServices();
                    Log.d("MainActivity", "POŁĄCZONO Z " + gatt.getDevice().getAddress());

                    break;

                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.d("MainActivity", "ROZŁĄCZONO Z " + gatt.getDevice().getAddress());


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
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // this will get called after the client initiates a luetoothGatt.discoverServices() call


                Log.d("MainActivity", "onServicesDiscovered()");


                for (BluetoothGattService gattService : gatt.getServices()) {

                    List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();

                    for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {

                        for (BluetoothGattDescriptor descriptor : gattCharacteristic.getDescriptors()) {
                            if (descriptor != null) {
                                descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                                gatt.writeDescriptor(descriptor);
                            }
                        }
                        gatt.setCharacteristicNotification(gattCharacteristic, true);


                    }
                }


                for (BluetoothGattService service : gatt.getServices()) {
                    if ((service == null) || (service.getUuid() == null)) {
                        continue;
                    }
                    if (SERVICE_DEVICE_INFORMATION.equalsIgnoreCase(service.getUuid().toString())) {
                        //mReadManufacturerNameButton.setTag(service.getCharacteristic(UUID.fromString(BleUuid.CHAR_MANUFACTURER_NAME_STRING)));
                        //mReadSerialNumberButton.setTag(service.getCharacteristic(UUID.fromString(BleUuid.CHAR_SERIAL_NUMBEAR_STRING)));
                        Log.d("MainActivity", "UDAŁO SIĘ ODCZYTAĆ CHYYYYYYYYYYYBA");
                        devinfo = true;
                        /*runOnUiThread(new Runnable() {
                            public void run() {
                               // mReadManufacturerNameButton.setEnabled(true);
                               // mReadSerialNumberButton.setEnabled(true);
                            };
                        });*/
                    }
                    if (SERVICE_IMMEDIATE_ALERT.equalsIgnoreCase(service.getUuid().toString())) {
                        /*runOnUiThread(new Runnable() {
                            public void run() {
                                //mWriteAlertLevelButton.setEnabled(true);
                            }
                        });*/
                        //mWriteAlertLevelButton.setTag(service.getCharacteristic(UUID.fromString(BleUuid.CHAR_ALERT_LEVEL)));
                    }
                }

                /*runOnUiThread(new Runnable() {
                                  public void run() {
                                      setProgressBarIndeterminateVisibility(false);
                                  }


                              });*/

                displayGattServices(bluetoothGatt.getServices());
            } else {
                // failure
            }
        }

        @Override
        // Result of a characteristic read operation
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                Log.d("MainActivity", "UDAŁO SIĘ ODCZYTAĆ NAPEWNOOOOOOOOOOOOO");
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

            runOnUiThread(new Runnable() {
                public void run() {
                }
            });
        }
    };

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;

        for (BluetoothGattService gattService : gattServices) {
            final String uuid = gattService.getUuid().toString();
            Log.d("SERVICES", "Service disovered: " + uuid);

            //new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                final String charUuid = gattCharacteristic.getUuid().toString();
                Log.d("CHARACTERISTICS", "Characteristic discovered for service: " + charUuid);

                List<BluetoothGattDescriptor> gattDescriptors = gattCharacteristic.getDescriptors();
                for (BluetoothGattDescriptor gattDescriptor : gattDescriptors) {
                    final String descUuid = gattDescriptor.getUuid().toString();
                    Log.d("DESCRIPTORS", "Descriptor discovered for characteristic: " + descUuid);
                }
            }
        }
    }

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        System.out.println(characteristic.getUuid());
    }


    private void startRing(String addr) {
        Log.d("sylwka", "startRing()");
        if (currentRingtone != null) {
            currentRingtone.stop();
            currentRingtone = null;
        }
        final String address = addr;
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        currentRingtone = RingtoneManager.getRingtone(this, sound);

        if (currentRingtone == null) {
            Log.d("sylwka", "R.string.ring_tone_not_found");
            return;
        }

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        final int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
        audioManager.setStreamVolume(AudioManager.STREAM_RING, max, 0);

        currentRingtone.play();
    }

    private void stopRing() {
        Log.d("sylwka", "stopRing()");
        if (currentRingtone != null) {
            currentRingtone.stop();
        }
    }
}
