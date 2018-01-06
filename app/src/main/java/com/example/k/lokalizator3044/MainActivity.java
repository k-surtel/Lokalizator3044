package com.example.k.lokalizator3044;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import android.bluetooth.le.ScanSettings;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.support.v7.preference.PreferenceManager;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Switch;
import android.widget.Toast;
import com.example.k.lokalizator3044.DatabaseManagement.DBHelper;
import com.example.k.lokalizator3044.DatabaseManagement.MyContentProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {

    BluetoothManager mBluetoothManager;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothLeScanner mBluetoothScanner;
    private Handler mHandler = new Handler();
    private SimpleCursorAdapter cursorAdapter;

    ArrayList<BluetoothDevice> scannedDevices = new ArrayList<>();
    ArrayList<String> scannedDevicesNames = new ArrayList<>();

    HashMap<String, BluetoothDevice> myDevices = new HashMap<>();
    HashMap<String, BluetoothGatt> myGatts = new HashMap<>();


    ListView itagList;
    AlertDialog.Builder builder;
    BluetoothGatt bluetoothGatt;
    BluetoothGattCharacteristic bc;
    boolean addingMayFail;
    boolean alert = false;
    static Ringtone currentRingtone;
    boolean ringtone = false;
    Switch pressedSwitch;
    View itagAddress;
    View alarmButton;
    View settingsButton;
    LinearLayout linlaHeaderProgress;
    public static String NEW_ITAG_ACTIVITY = "new itag activity";
    public static String ITAG_ACTIVITY = "itag activity";
    boolean scanningActive;
    FloatingActionButton fab;
    NotificationCompat.Builder mBuilder;
    NotificationCompat.Builder mBuilder2;
    NotificationManager mNotifyMgr;
    ScanSettings settings;
    String switchDeviceAddress;
    boolean switchNewDevice;
    Vibrator v;
    boolean trybCichy = false;
    SharedPreferences prefs;
    GPSTracker gps = new GPSTracker(this);

    /**
     * STATIC VALUES
     */
    //(☞ ͡° ͜ʖ ͡°)☞ STOPS SCANNING AFTER 5 SECONDS
    private static final long SCAN_PERIOD = 5000;
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    private final static int REQUEST_ENABLE_BT = 1;
    public final static String ACTION_DATA_AVAILABLE = "com.example.k.lokalizator3044.ACTION_DATA_AVAILABLE";
    public static final UUID CHAR_BATTERY_LEVEL_UUID = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");
    public static final UUID CHAR_ALERT_LEVEL_UUID = UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        mBluetoothScanner = mBluetoothAdapter.getBluetoothLeScanner();

        checkBleSupport();
        if (!isBluetoothEnable()) requestBluetoothEnable();
        requestLocationPermission();

        settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).build();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewDevice();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        itagList = findViewById(R.id.itag_list);
        adaptItagList();

        itagList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            int sameCount = 1;
            boolean same;

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the cursor, positioned to the corresponding row in the result set
                Cursor cursor = (Cursor) itagList.getItemAtPosition(position);
                // Get the state's capital from this row in the database.
                final String address = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.ADDRESS));

                if (itagAddress == view.findViewById(R.id.itag_address)) {
                    same = true;
                    sameCount++;
                } else {
                    same = false;
                    sameCount = 1;
                }

                if ((itagAddress != null && itagAddress.getTag() != null && (boolean) itagAddress.getTag()) || same) {
                    itagAddress.setVisibility(View.GONE);
                    itagAddress.setTag(false);
                }
                if ((alarmButton != null && alarmButton.getTag() != null && (boolean) alarmButton.getTag()) || same) {
                    alarmButton.setVisibility(View.GONE);
                    alarmButton.setTag(false);
                }
                if ((settingsButton != null && settingsButton.getTag() != null && (boolean) settingsButton.getTag()) || same) {
                    settingsButton.setVisibility(View.GONE);
                    settingsButton.setTag(false);
                }

                if (!same || (same && sameCount % 2 != 0 && sameCount > 1)) {
                    itagAddress = view.findViewById(R.id.itag_address);
                    alarmButton = view.findViewById(R.id.itag_alarm);
                    settingsButton = view.findViewById(R.id.itag_settings);

                    itagAddress.setVisibility(View.VISIBLE);
                    itagAddress.setTag(true);
                    alarmButton.setVisibility(View.VISIBLE);
                    alarmButton.setTag(true);
                    settingsButton.setVisibility(View.VISIBLE);
                    settingsButton.setTag(true);
                }

                alarmButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final String addr = address;


                        if (myDevices.get(addr) != null && myGatts.get(addr) != null && !alert) {
                            BluetoothGatt gatt = myGatts.get(addr);
                            bc = findCharacteristic(addr, CHAR_ALERT_LEVEL_UUID);
                            if (bc != null) {
                                bc.setValue(new byte[]{(byte) 0x01});
                                alert = true;
                                gatt.writeCharacteristic(bc);
                            }
                        } else if (myDevices.get(addr) != null && myGatts.get(addr) != null && alert) {
                            BluetoothGatt gatt = myGatts.get(addr);
                            bc = findCharacteristic(addr, CHAR_ALERT_LEVEL_UUID);
                            if (bc != null) {
                                bc.setValue(new byte[]{(byte) 0x00});
                                alert = false;
                                gatt.writeCharacteristic(bc);
                            }
                        }
                    }
                });

                settingsButton.setOnClickListener(new View.OnClickListener() {

                    final String addr = address;

                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(), AddingActivity.class);
                        intent.putExtra(ITAG_ACTIVITY, addr);
                        intent.putExtra(NEW_ITAG_ACTIVITY, false);
                        startActivityForResult(intent, 1);
                    }
                });


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
                        removeRecords();
                        return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
            }
        });

        Intent intent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
                .setComponent(getPackageManager().getLaunchIntentForPackage(getPackageName()).getComponent());

        mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Lokalizator 3044")
                .setContentText("Aplikacja jest połączona do iTagów!")
                .setOngoing(true)
                .setContentIntent(PendingIntent.getActivity(this, 0, intent, 0));

        mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }


    public void checkRange() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final Handler h = new Handler();
        final int d = Integer.parseInt(prefs.getString("itag_interval", "15")) * 1000;


        h.postDelayed(new Runnable() {
            public void run() {

                Set<String> keys = myGatts.keySet();
                for (String k : keys) myGatts.get(k).readRemoteRssi();
                h.postDelayed(this, d);
            }
        }, d);
    }

    public void checkBleSupport() {
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

    private void adaptItagList() {
        String[] from = new String[]{DBHelper.NAME, DBHelper.ADDRESS, DBHelper.ID};
        int[] to = new int[]{R.id.itag_name, R.id.itag_address, R.id.if_enabled};

        cursorAdapter = new SimpleCursorAdapter(this, R.layout.selected_itag, null, from, to, 0);
        cursorAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(final View view, final Cursor cursor, int i) {

                if (i == cursor.getColumnIndexOrThrow(DBHelper.ID)) {
                    final Switch s = (Switch) view;

                    if (myGatts.get(cursor.getString(cursor.getColumnIndex(DBHelper.ADDRESS))) != null)
                        s.setChecked(true);
                    else s.setChecked(false);

                    if (view.getTag() == null)
                        view.setTag(cursor.getString(cursor.getColumnIndex(DBHelper.ADDRESS)));

                    s.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            pressedSwitch = s;
                            switchDeviceAddress = (String) view.getTag();
                            if (s.isChecked()) {
                                if (myGatts.get(switchDeviceAddress) == null) {
                                    switchNewDevice = false;
                                    startScan();
                                }
                            } else disconnectDeviceSelected(switchDeviceAddress);
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


    private void removeRecords() {
        long checkedRecords[] = itagList.getCheckedItemIds();
        Cursor cursor;
        String addr = "";
        for (int i = 0; i < checkedRecords.length; ++i) {
            cursor = getContentResolver().query(ContentUris.withAppendedId(MyContentProvider.URI_ZAWARTOSCI, checkedRecords[i]), null, null, null, null);
            if (cursor.moveToFirst())
                addr = cursor.getString(cursor.getColumnIndex(DBHelper.ADDRESS));
            if (myGatts.get(addr) != null && !myGatts.get(addr).equals(""))
                disconnectDeviceSelected(addr);
            getContentResolver().delete(ContentUris.withAppendedId(MyContentProvider.URI_ZAWARTOSCI, checkedRecords[i]), null, null);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 1) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                addingMayFail = false;

            } else if (resultCode == RESULT_CANCELED) {
                addingMayFail = false;
                if (data.getExtras() != null)
                    disconnectDeviceSelected(data.getExtras().getString("a"));
            }
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projekcja = {DBHelper.ID, DBHelper.ADDRESS, DBHelper.NAME, DBHelper.WORKING_MODE, DBHelper.RINGTONE, DBHelper.DISTANCE, DBHelper.CLICK};
        CursorLoader cursorLoader = new CursorLoader(this, MyContentProvider.URI_ZAWARTOSCI, projekcja, null, null, null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

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


    public void startScan() {
        linlaHeaderProgress = (LinearLayout) findViewById(R.id.linlaHeaderProgress);
        linlaHeaderProgress.setVisibility(View.VISIBLE);
        if (!scanningActive) {
            scanningActive = true;
            scannedDevicesNames.clear();
            scannedDevices.clear();

            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    mHandler.removeCallbacks(this);
                    mBluetoothScanner.startScan(null, settings, scanCallback);
                }
            });
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopScan();
                }

            }, SCAN_PERIOD);
        }
    }

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.d("MainActivity", "onScanResult()");

            if (!scannedDevices.contains(result.getDevice())) {
                boolean isInDatabase = false;
                if (switchNewDevice) {
                    Cursor cursor = getContentResolver()
                            .query(MyContentProvider.URI_ZAWARTOSCI, new String[]{DBHelper.ADDRESS}, null, null, null);

                    if (cursor.moveToFirst()) {
                        do {
                            if (cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.ADDRESS)).equals(result.getDevice().getAddress()))
                                isInDatabase = true;
                        } while (cursor.moveToNext());
                    }
                    cursor.close();
                }

                if (!isInDatabase || !switchNewDevice) {
                    scannedDevices.add(result.getDevice());
                    if (result.getDevice().getName() == null || result.getDevice().getName().equals("")) {
                        scannedDevicesNames.add(result.getDevice().getAddress());
                    } else {
                        scannedDevicesNames.add(result.getDevice().getName());
                    }
                }
            }
        }

    };


    public void stopScan() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                mBluetoothScanner.stopScan(scanCallback);
            }
        });
        if (switchNewDevice) {
            linlaHeaderProgress.setVisibility(View.GONE);
            createAvailableDevicesList();
        } else findDevice();
        scanningActive = false;
    }

    void createAvailableDevicesList() {
        if (!scannedDevices.isEmpty()) {
            builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Wybierz urządzenie");
            builder.setItems(scannedDevicesNames.toArray(new String[0]), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    myDevices.put(scannedDevices.get(which).getAddress(), scannedDevices.get(which));
                    connectToDeviceSelected(scannedDevices.get(which));
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else Toast.makeText(MainActivity.this, "Nie znaleziono żadnych urządzeń!", Toast.LENGTH_SHORT).show();
    }

    public void findDevice() {
        boolean found = false;
        for (BluetoothDevice bd : scannedDevices) {
            if (bd.getAddress().equals(switchDeviceAddress)) {
                found = true;
                connectToDeviceSelected(bd);
            }
        }

        if (found) pressedSwitch.setChecked(true);
        else {
            Toast.makeText(this, "Nie znaleziono urządzenia!", Toast.LENGTH_SHORT).show();
            linlaHeaderProgress.setVisibility(View.GONE);
            pressedSwitch.setChecked(false);
        }

    }

    public void addNewDevice() {
        addingMayFail = true;
        switchNewDevice = true;
        if (isBluetoothEnable()) {
            startScan();
        } else requestBluetoothEnable();
    }


    public void connectToDeviceSelected(BluetoothDevice bd) {
        bluetoothGatt = bd.connectGatt(this, false, gattCallback);

        if (myGatts.isEmpty()) {
            mNotifyMgr.notify(0, mBuilder.build());
            checkRange();
        }

        if (switchNewDevice) {
            Intent intent = new Intent(getApplicationContext(), AddingActivity.class);
            intent.putExtra(ITAG_ACTIVITY, bd.getAddress());
            intent.putExtra(NEW_ITAG_ACTIVITY, switchNewDevice);
            startActivityForResult(intent, 1);
        }

        myGatts.put(bd.getAddress(), bluetoothGatt);
        myDevices.put(bd.getAddress(), bd);
        Toast.makeText(MainActivity.this, "Połączono do " + bd.getAddress(), Toast.LENGTH_SHORT).show();
        linlaHeaderProgress.setVisibility(View.GONE);
    }

    public void disconnectDeviceSelected(String address) {
        Log.d("MainActivity", "disconnectDeviceSelected()");
        if (myGatts.get(address) != null) {
            myGatts.get(address).disconnect();
            myGatts.remove(address);

            myDevices.remove(address);

            if (!addingMayFail) {
                if (pressedSwitch.isChecked()) pressedSwitch.setChecked(false);
            }
            if (myGatts.isEmpty()) mNotifyMgr.cancel(0);

            if (myGatts.isEmpty()) {
                Log.e("MainActivity", "myGatts jest empty");
                mNotifyMgr.cancel(0);
            }
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


    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            // this will get called anytime you perform a read or write characteristic operation
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);

            String address = gatt.getDevice().getAddress();
            String action;

            Cursor c = getContentResolver().query(MyContentProvider.URI_ZAWARTOSCI, new String[]{DBHelper.RINGTONE, DBHelper.CLICK}, DBHelper.ADDRESS + "='" + address + "'", null, null, null);
            if (c.moveToFirst())
                Log.d("MainActivity", c.getString(c.getColumnIndexOrThrow(DBHelper.RINGTONE)));
            Uri sound = Uri.parse(c.getString(c.getColumnIndexOrThrow(DBHelper.RINGTONE)));

            action = c.getString(c.getColumnIndexOrThrow(DBHelper.CLICK));
            c.close();

            if (!ringtone) {
                ringtone = true;
                if (action.equals("Uruchom alarm")) startRing(sound);
                else startVibrate();
            } else {
                ringtone = false;
                if (action.equals("Uruchom alarm")) stopRing();
                else stopVibrate();
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
                            Log.d("MainActivity", "Nieznany stan urządzenia " + gatt.getDevice().getAddress());
                        }
                    });
                    break;
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("MainActivity", "onServicesDiscovered()");

                for (BluetoothGattService gattService : gatt.getServices()) {
                    List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
                    for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {

                        for (BluetoothGattDescriptor descriptor : gattCharacteristic.getDescriptors()) {
                            if (descriptor != null && gattCharacteristic.getUuid() == CHAR_ALERT_LEVEL_UUID) {
                                descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                                gatt.writeDescriptor(descriptor);
                            }
                        }
                        gatt.setCharacteristicNotification(gattCharacteristic, true);
                    }
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                Log.d("MainActivity", "UDAŁO SIĘ ODCZYTAĆ NAPEWNOOOOOOOOOOOOO");

                UUID uuid = characteristic.getUuid();

                if (uuid.toString().equals(CHAR_BATTERY_LEVEL_UUID.toString())) {

                    byte[] x = characteristic.getValue();
                    int uint8 = convertByteToInt(x);

                    Log.d("MainActivity", "X");
                    Log.d("MainActivity", "X = " + uint8);
                }
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

            runOnUiThread(new Runnable() {
                public void run() {
                }
            });
        }


        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {

                int dist = 0;
                String addr = gatt.getDevice().getAddress();
                Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                String currentMode = "";
                Cursor c = getContentResolver().query(MyContentProvider.URI_ZAWARTOSCI,
                        new String[]{DBHelper.NAME, DBHelper.DISTANCE, DBHelper.RINGTONE, DBHelper.WORKING_MODE},
                        DBHelper.ADDRESS + "='" + addr + "'", null, null, null);
                if (c.getCount() == 0) {
                    c.close();
                    return;
                }
                if (c.moveToFirst()) {
                    dist = Integer.parseInt(c.getString(c.getColumnIndexOrThrow(DBHelper.DISTANCE)));
                    uri = Uri.parse(c.getString(c.getColumnIndexOrThrow(DBHelper.RINGTONE)));
                    currentMode = c.getString(c.getColumnIndexOrThrow(DBHelper.WORKING_MODE));
                }

                prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                trybCichy = prefs.getBoolean("switch", false);
                if (trybCichy) trybCichy = prefs.getBoolean("tryb_cichy", false);

                if (Math.abs(rssi) > dist) {

                    mBuilder2 = new NotificationCompat.Builder(MainActivity.this)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("Lokalizator 3044")
                            .setContentText("Urządzenie " + c.getString(c.getColumnIndexOrThrow(DBHelper.NAME)) + " jest poza zasięgiem!");

                    if (trybCichy)
                        Toast.makeText(MainActivity.this, "currentMode cichy", Toast.LENGTH_LONG).show();

                    if (currentMode.equals("Tryb głośny"))
                        mBuilder2.setSound(uri);
                    mNotifyMgr.notify(1, mBuilder2.build());
                }
                c.close();
            }
        }
    };

    public int convertByteToInt(byte[] b) {
        int value = 0;
        for (int i = 0; i < b.length; i++)
            value = (value << 8) | b[i];
        return value;
    }

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        System.out.println(characteristic.getUuid());
    }

    private void startRing(Uri sound) {
        if (currentRingtone != null) {
            currentRingtone.stop();
            currentRingtone = null;
        }

        currentRingtone = RingtoneManager.getRingtone(this, sound);

        if (currentRingtone == null) {
            return;
        }

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        final int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
        audioManager.setStreamVolume(AudioManager.STREAM_RING, max, 0);

        currentRingtone.play();
    }

    private void stopRing() {
        if (currentRingtone != null) {
            currentRingtone.stop();
        }
    }

    private void startVibrate() {
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        long pattern[] = {50, 100, 100, 250, 150, 350};
        v.vibrate(pattern, 3);
    }

    private void stopVibrate() {
        v.cancel();
    }
}
