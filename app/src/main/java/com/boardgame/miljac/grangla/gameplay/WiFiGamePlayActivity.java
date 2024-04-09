package com.boardgame.miljac.grangla.gameplay;

import static android.Manifest.*;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.blikoon.qrcodescanner.QrCodeActivity;
import com.boardgame.miljac.grangla.R;
import com.boardgame.miljac.grangla.music.MusicPlayer;
import com.boardgame.miljac.grangla.wifi.GranglaWiFiService;
import com.boardgame.miljac.grangla.wifi.ThinMidiWiFiProxy;
import com.boardgame.miljac.grangla.wifi.WifiConnectingSingleton;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.LockSupport;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class WiFiGamePlayActivity extends GamePlayActivity {
    private Dialog builder;
    int networkId;
    int retry = 0;
    WifiManager.LocalOnlyHotspotReservation mReservation;
    TableViewRefreshing tableViewRefreshing;
    Thread proxyThread;
    Socket granglaSocket;
    boolean server;
    GranglaWiFiService wifiService;
    boolean gameStarted = false;
    boolean gameInited = false;
    int stateSent = 0;
    final int PERMISSION_REQUEST_CODE = 1000;
    final IntentFilter intentFilter = new IntentFilter();
    boolean connected = false;
    WifiManager mWifiManager;
    WifiConfiguration mWifiConfig;
    boolean qrRequested = false;


    class TableViewRefreshing implements Runnable {
        UIRefresh uIPut = new UIRefresh(WiFiGamePlayActivity.this, server, true);

        public void run() {
            LockSupport.parkNanos(70_000_000);
            while (!gameDone) {
                if (gamePaused) continue;

                uIRefreshThread = new Thread(uIPut);
                runOnUiThread(uIRefreshThread);
                if(server) {
                    if(stateSent <= 0) {
                        wifiService.sendTableState();
                    } else {
                        stateSent--;
                    }
                }
                LockSupport.parkNanos(70_000_000);
            }

            if(server){
                wifiService.sendWin();
            }
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("granglawifi","action received: " + intent.toString());
            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                // Determine if Wi-Fi Direct mode is enabled or not, alert
                // the Activity.
                /*int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    activity.setIsWifiP2pEnabled(true);
                } else {
                    activity.setIsWifiP2pEnabled(false);
                }*/
            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

                // Connection state changed! We should probably do something about
                // that.

            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            }
        }
    };

    /**
     * SetUp Hotspot
     */
    private void setUpHotSpotStartServer() {
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        WifiConnectingSingleton.getInstance().setPort(ThreadLocalRandom.current().nextInt(5000, 6500));
        try {
            /*if (callBackListener != null) {
                callBackListener.showQRCodeProgressbar();
            }*/
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                belowOreoDevicesSetupHotSpot();
            } else {
                oreoAndAboveDevicesSetupHotspot();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                if(!connected) {
                    try {
                        ServerSocket serverSocket = new ServerSocket();
                        serverSocket.setReuseAddress(true);
                        serverSocket.bind(new InetSocketAddress(WifiConnectingSingleton.getInstance().getPort()));
                        Log.d("granglawifi", "port: " + WifiConnectingSingleton.getInstance().getPort());
                        Socket socket = serverSocket.accept();
                        builder.dismiss();
                        granglaSocket = socket;
                        server = true;
                        startGame();
                    } catch (IOException e) {
                        e.printStackTrace();
                        finish();
                    }
                }
            }
        }).start();

    }


    private void connectToHotSpotStartClient(){
        if(qrRequested) return;

        qrRequested = true;
        Intent i = new Intent(this, QrCodeActivity.class);
        startActivityForResult(i, 20);
        /*IntentIntegrator intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.setPrompt("Scan a barcode or QR Code");
        //intentIntegrator.setOrientationLocked(true);
        intentIntegrator.initiateScan();*/
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("granglawifi", "nestoo skenirao");

        new Thread(new Runnable() {
            @Override
            public void run() {

                if (requestCode == 20) {
                    if (data == null)
                        return;
                    //Getting the passed result
                    String result = data.getStringExtra("com.blikoon.qrcodescanner.got_qr_scan_relult");
                    Log.d("granglawifi", result);
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        String SSID = jsonObject.getString("SSID");
                        String password = jsonObject.getString("Password");
                        WifiConnectingSingleton.getInstance().setPort(jsonObject.getInt("Port"));

                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {

                            WifiNetworkSpecifier specifier = new WifiNetworkSpecifier.Builder()
                                    .setSsid(SSID)
                                    .setWpa2Passphrase(password)
                                    .build();

                            NetworkRequest request = new NetworkRequest.Builder()
                                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                                    .setNetworkSpecifier(specifier)
                                    .build();

                            ConnectivityManager connectivityManager =
                                    (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

                            ConnectivityManager.NetworkCallback networkCallback =
                                    new ConnectivityManager.NetworkCallback() {
                                        @Override
                                        public void onAvailable(Network network) {
                                            Log.d("granglawifi", "network available ");
                                            /*ConnectivityManager connectivityManager =
                                                    (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);*/
                                            boolean isConnected = connectivityManager.bindProcessToNetwork(network);
                                            try {
                                                final Handler handler = new Handler(Looper.getMainLooper());
                                                handler.postDelayed(() -> {

                                                    new Thread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            WiFiAddress address = new WiFiAddress(getApplicationContext());
                                                            try {
                                                                Socket socket = new Socket(address.getGatewayIPAddress(), WifiConnectingSingleton.getInstance().getPort());
                                                                Log.d("granglawifi", "port: " + WifiConnectingSingleton.getInstance().getPort());
                                                                granglaSocket = socket;
                                                                server = false;
                                                                startGame();
                                                            } catch (IOException e) {
                                                                Log.d("granglawifi", e.getMessage());
                                                            }
                                                        }
                                                    }).start();

                                                }, /*10000*/500);
                                                //do a callback or something else to alert your code that it's ok to send the message through socket now
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                //fragmentPlayListBinding.layProgress.setVisibility(View.GONE);
                                            }
                                        }

                                        @Override
                                        public void onLost(Network network) {
                                            // do something when network is lost
                                            finish();
                                        }
                                    };

                            connectivityManager.requestNetwork(request, networkCallback);

                            return;
                        } else {
                            int c = 0;
                            while (c < 15 && !connectToHotspot(SSID, password)) {
                                c++;
                                Thread.sleep(2000);
                            }

                            if (c == 15) {
                                finish();
                                return;
                            }

                            //if(connectToHotspot(SSID, Password)){
                            bindNetworkProcess();
                        }
                /*} else {
                    Log.d("granglawifi", "connect to hotspot failde..");
                }*/
                /*if (selectedItem.equals(SSID)) {
                    boolean isConnected = hotutil.connectToHotspot(SSID, Password);
                    if (isConnected)
                        bindNetworkProcess();
                    else {
                        fragmentPlayListBinding.layProgress.setVisibility(View.GONE);
                        Toast.makeText(mainActivity, "Failed to connect, Please try again...", Toast.LENGTH_SHORT).show();
                        resetEverything("QR code failed");
                    }

                } else {
                    Toast.makeText(mainActivity.getApplicationContext(), "Scanned wrong QR code", Toast.LENGTH_SHORT).show();
                }*/

                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        }).start();


    }


    /**
     * Create HotSpot
     */
    //@RequiresApi(api = Build.VERSION_CODES.O)
    private void oreoAndAboveDevicesSetupHotspot() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                mWifiManager.startLocalOnlyHotspot(new WifiManager.LocalOnlyHotspotCallback() {

                    @Override
                    public void onStarted(WifiManager.LocalOnlyHotspotReservation reservation) {
                        super.onStarted(reservation);
                        mReservation = reservation;

                        Log.d("granglawifi", "setting up new local hotspot..");
                        WifiConnectingSingleton.getInstance().setReservation(reservation);
                        retry = 0;
                        //startForeground(11,
                        //buildForegroundNotification());
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("SSID", reservation.getWifiConfiguration().SSID);
                            jsonObject.put("Password", reservation.getWifiConfiguration().preSharedKey);
                            jsonObject.put("Port", WifiConnectingSingleton.getInstance().getPort());
                            generateQRCode(reservation.getWifiConfiguration().SSID, jsonObject.toString());
                        } catch (Exception e) {
                            builder = null;
                            e.printStackTrace();
                            finish();
                        }
                    }

                    public void onFailed(int i) {
                        super.onFailed(i);
                        if (retry < 2) {
                            retry++;
                            oreoAndAboveDevicesSetupHotspot();
                        } else if (WifiConnectingSingleton.getInstance().getReservation() != null) {
                            Log.d("granglawifi", "searching for existing local hotspot..");
                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put("SSID", WifiConnectingSingleton.getInstance().getReservation().getWifiConfiguration().SSID);
                                jsonObject.put("Password", WifiConnectingSingleton.getInstance().getReservation().getWifiConfiguration().preSharedKey);
                                jsonObject.put("Port", WifiConnectingSingleton.getInstance().getPort());
                                generateQRCode(WifiConnectingSingleton.getInstance().getReservation().getWifiConfiguration().SSID, jsonObject.toString());
                            } catch (Exception e) {
                                builder = null;
                                e.printStackTrace();
                                finish();
                            }
                        }
                    }

                    public void onStopped() {
                        super.onStopped();

                    }
                }, null);
            } catch (IllegalStateException e){
                if (WifiConnectingSingleton.getInstance().getReservation() != null) {
                    Log.d("granglawifi", "searching for existing local hotspot..");
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("SSID", WifiConnectingSingleton.getInstance().getReservation().getWifiConfiguration().SSID);
                        jsonObject.put("Password", WifiConnectingSingleton.getInstance().getReservation().getWifiConfiguration().preSharedKey);
                        jsonObject.put("Port", WifiConnectingSingleton.getInstance().getPort());
                        generateQRCode(WifiConnectingSingleton.getInstance().getReservation().getWifiConfiguration().SSID, jsonObject.toString());
                    } catch (Exception ex) {
                        builder = null;
                        ex.printStackTrace();
                        finish();
                    }
                }
            }
        }

    }

    /**
     * Create HotSpot
     */
    private void belowOreoDevicesSetupHotSpot() {
        Method[] wmMethods = mWifiManager.getClass().getDeclaredMethods();
        WifiConfiguration wifiConfig = new WifiConfiguration();
        Boolean enabled = false;
        WifiConnectingSingleton.getInstance().setPort(ThreadLocalRandom.current().nextInt(5000, 6500));
        try {
            for (Method m : wmMethods) {
                if (m.getName().equals("isWifiApEnabled")) {

                    enabled = (Boolean) m.invoke(mWifiManager);

                }

                if (m.getName().equals("getWifiApConfiguration")) {

                    wifiConfig = (WifiConfiguration) m.invoke(mWifiManager, null);

                    wifiConfig.SSID = "AndroidShare_" + String.format("%04d", new Random().nextInt(10000));
                    Log.d("Generated SSID", wifiConfig.SSID);
                    wifiConfig.preSharedKey = getSSID();
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("SSID", wifiConfig.SSID);
                    jsonObject.put("Password", wifiConfig.preSharedKey);
                    jsonObject.put("Port", WifiConnectingSingleton.getInstance().getPort());

                    generateQRCode(wifiConfig.SSID, jsonObject.toString());
                    mWifiConfig = wifiConfig;
                    break;
                }

            }

            if (!enabled) {

                for (Method m : wmMethods) {
                    if (m.getName().equals("setWifiApEnabled")) {
                        try {
                            m.invoke(mWifiManager, wifiConfig, true);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            builder.dismiss();
                            finish();
                        }
                        break;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


//        if (callBackListener != null) {
//            callBackListener.hideProgress();
//        }
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onStart() {
        super.onStart();

        Intent intent = getIntent();
        level = intent.getIntExtra("mrm_LEVEL", 50);
        server = intent.getStringExtra("server").equals("yes");

        setPlayerImages(intent);

        setVisualElements();
        textClock.setText(getResources().getString(R.string.connecting_wifi));
        textClock.setTextColor(Color.WHITE);

        registerReceiver(receiver, intentFilter);


        if (mPrefs == null) {
            mPrefs = getSharedPreferences("mrm", MODE_PRIVATE);
        }

        if(server) {
            setUpHotSpotStartServer();
        } else {
            connectToHotSpotStartClient();
        }
    }

    public void startGame(){
        if(gameInited) {
            setResult(55);
            finish();
            return;
        }

        gameInited = true;

        gameStartTime = System.currentTimeMillis();
        lastEventTime = gameStartTime;

        table.server = server;

        wifiService = new GranglaWiFiService(granglaSocket, this);

        tableViewRefreshing = new TableViewRefreshing();
        tableViewRefreshingThread = new Thread(tableViewRefreshing);
        tableViewRefreshingThread.start();

        musicPlayer = new MusicPlayer();
        musicPlayer.server = server;
        musicPlayer.wifiService = wifiService;

        ThinMidiWiFiProxy proxy = new ThinMidiWiFiProxy(gameStartTime, musicPlayer.getMidiDriver(), musicPlayer.wifiService);
        wifiService.setProxy(proxy);

        if(muted){
            musicPlayer.silence();
        }

        proxyThread = new Thread(wifiService.getProxy());
        proxyThread.start();

        musicPlayerThread = new Thread(musicPlayer);
        musicPlayer.setNoteDuration((long) (TableConfig.NOTE_DURATION_FACTOR * waitingTimeCross));
        musicPlayerThread.start();

        if (gamePaused) gameStartTime += System.currentTimeMillis() - pausedTime;
        gamePaused = false;
        gameStarted = true;
    }

    private void setVisualElements() {
        setCommonVisualElements();

        soundToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!gameStarted) {
                    muted = !isChecked;
                    return;
                }
                if (isChecked) {
                    musicPlayer.unsilence();
                    muted = false;
                } else {
                    musicPlayer.silence();
                    muted = true;
                }
            }
        });

        imageButton = (ImageButton) findViewById(R.id.discard_button);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                saveSharedPreferences();
                if (wifiService != null) {
                    wifiService.sendExit();
                }
                finish();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WifiConnectingSingleton.getInstance().setWifi(true);

        this.requestPermissions(new String[]{permission.NEARBY_WIFI_DEVICES,
                permission.ACCESS_COARSE_LOCATION,
                permission.ACCESS_FINE_LOCATION,
                permission.ACCESS_WIFI_STATE,
                permission.CHANGE_WIFI_STATE,
                permission.INTERNET
        }, PERMISSION_REQUEST_CODE);

        currentApiVersion = Build.VERSION.SDK_INT;
        setContentView(R.layout.activity_game_play);

        // Indicates a change in the Wi-Fi Direct status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // Indicates the state of Wi-Fi Direct connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        intentFilter.addAction(WifiP2pManager.ACTION_WIFI_P2P_REQUEST_RESPONSE_CHANGED);
        intentFilter.addAction(WifiP2pManager.EXTRA_REQUEST_CONFIG);
        intentFilter.addAction(WifiP2pManager.EXTRA_REQUEST_RESPONSE);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

         Log.d("granglawifi", "onDestroy");
        connected = true;//to cancel discovery

        saveSharedPreferences();

        if (endDialog != null)
            endDialog.dismiss();

        gameDone = true;
        if(musicPlayer != null) musicPlayer.mute();

        try {
            if(tableViewRefreshingThread!= null)
                tableViewRefreshingThread.join();
        } catch (InterruptedException e) {
            //e.printStackTrace();
        }
        tableViewRefreshingThread = null;


        try {
            if(musicPlayerThread!= null)
                musicPlayerThread.join();
        } catch (InterruptedException e) {
            //e.printStackTrace();
        }
        musicPlayerThread = null;

        try {
            unregisterReceiver(receiver);
        } catch (IllegalArgumentException e) {

        }
        receiver = null;

        try {
            if (granglaSocket != null)
                granglaSocket.close();
        } catch (IOException e) {
            //e.printStackTrace();
        }

        try {
            if (wifiService != null) {
                wifiService.cancel();
                granglaSocket = null;
            }
        } catch (Exception e) {
            //e.printStackTrace();
            Log.d("granglawifi", "onDestroy service " + e);
        }

        closeConnection();

        Log.d("granglawifi", "onDestroy finished");
    }

    public void onFieldSelected(int x,int y) {
        if(!gameStarted) return;
        synchronized (table) {
            if(allowCircle) {
                if(!server){
                    if(this.table.get(x, y) == State.empty) {
                        wifiService.sendMove(x, y);
                    }
                } else {
                    if (this.table.putIfPossible(State.circle, x, y)) {
                        lastMoveO = new Coordinates(x, y);
                        waitingMomentCircle = System.currentTimeMillis() + waitingTimeCircle;
                        startCircleTime = true;
                        allowCircle = false;
                        movesO.add(0, lastMoveO);

                        if (!server) {
                            wifiService.sendMove(x, y);
                        } else {
                            wifiService.sendTableState();
                            stateSent = 1;
                        }
                    }
                }
            }
        }
    }


    /**
     * Generate SSID String
     *
     * @return SSID
     */
    protected String getSSID() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 13) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        return salt.toString();

    }

    /**
     * Generate SSID and Password QRCode
     *
     * @param SSID
     * @param preSharedKey SSID and Password
     */
    private void generateQRCode(String SSID, String preSharedKey) {
        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int width = point.x;
        int height = point.y;
        int smallerDimension = width < height ? width : height;
        smallerDimension = smallerDimension * 3 / 4;

        QRGEncoder qrgEncoder = new QRGEncoder(preSharedKey, null, QRGContents.Type.TEXT, smallerDimension);
        qrgEncoder.setColorBlack(Color.WHITE);
        qrgEncoder.setColorWhite(Color.BLACK);
       // try {
            // Getting QR-Code as Bitmap
            Bitmap bitmap = qrgEncoder.getBitmap();
            Log.d("granglawifi", bitmap.toString());

            /*if (callBackListener != null) {
                callBackListener.QRCodeGenerated(SSID, bitmap);
            }
            // Setting Bitmap to ImageView
        } catch (WriterException e) {
            e.printStackTrace();
        }*/

        builder = new Dialog(this);
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                //nothing;
            }
        });

        ImageView imageView = new ImageView(this);
        imageView.setImageBitmap(bitmap);
        builder.addContentView(imageView, new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        builder.show();
    }




    /**
     * Method for Connecting  to WiFi Network (hotspot)
     *
     * @param netSSID of WiFi Network (hotspot)
     * @param netPass put password or  "" for opened network
     *                return true if connected to hotspot successfully
     */
    boolean connectToHotspot(String netSSID, String netPass) {
        Log.d("granglawifi", "connect to hotspot");
        WifiConfiguration wifiConf = new WifiConfiguration();

        if (mWifiManager == null) mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }

        @SuppressLint("MissingPermission") List<ScanResult> scanResultList = mWifiManager.getScanResults();
        for (ScanResult result : scanResultList) {

            removeWifiNetwork(result.SSID);
            if (result.SSID.equals(netSSID)) {

                String mode = getSecurityMode(result);

                if (mode.equalsIgnoreCase("OPEN")) {
                    Log.d("granglawifi", "open");

                    wifiConf.SSID = "\"" + netSSID + "\"";
                    wifiConf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                    networkId = mWifiManager.addNetwork(wifiConf);
                    mWifiManager.enableNetwork(networkId, true);

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                    if (!mWifiManager.isWifiEnabled())
                        mWifiManager.setWifiEnabled(true);
                    return true;

                } else if (mode.equalsIgnoreCase("WEP")) {
                    Log.d("granglawifi", "wep");

                    wifiConf.SSID = "\"" + netSSID + "\"";
                    wifiConf.wepKeys[0] = "\"" + netPass + "\"";
                    wifiConf.wepTxKeyIndex = 0;
                    wifiConf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                    wifiConf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                    networkId = mWifiManager.addNetwork(wifiConf);
                    mWifiManager.enableNetwork(networkId, true);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                    if (!mWifiManager.isWifiEnabled())
                        mWifiManager.setWifiEnabled(true);
                    return true;

                } else {
                    Log.d("granglawifi", "wpa");
                    wifiConf.SSID = "\"" + netSSID + "\"";
                    wifiConf.preSharedKey = "\"" + netPass + "\"";
                    wifiConf.hiddenSSID = true;
                    wifiConf.priority = 40;
                    wifiConf.status = WifiConfiguration.Status.ENABLED;
                    wifiConf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                    wifiConf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                    wifiConf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                    wifiConf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                    wifiConf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                    wifiConf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                    wifiConf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                    wifiConf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                    wifiConf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);

                    WifiManager.AddNetworkResult netress = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                        netress = mWifiManager.addNetworkPrivileged(wifiConf);
                        Log.d("granglawifi", netress.toString());
                    } else {
                        networkId = mWifiManager.addNetwork(wifiConf);
                    }


                    if (networkId >= 0 || (netress != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                            netress.statusCode == 0)) {
                        mWifiManager.disconnect();
                        mWifiManager.enableNetwork(networkId, true);
                        mWifiManager.reconnect();
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException ie) {
                            ie.printStackTrace();
                        }
                        if (!mWifiManager.isWifiEnabled())
                            mWifiManager.setWifiEnabled(true);
                        return true;
                    } else {
                        Log.d("granglawifi", "networkId < 0");
                    }
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * shred  Configured wifi Network By SSID
     *
     * @param ssid of wifi Network
     */
    private void removeWifiNetwork(String ssid) {
        @SuppressLint("MissingPermission") List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
        if (configs != null) {
            for (WifiConfiguration config : configs) {
                if (config.SSID.contains(ssid)) {
                    mWifiManager.disableNetwork(config.networkId);
                    mWifiManager.removeNetwork(config.networkId);
                }
            }
        }
        mWifiManager.saveConfiguration();
    }


    /**
     * Method to Get Network Security Mode
     *
     * @return OPEN PSK EAP OR WEP
     */
    private String getSecurityMode(ScanResult scanResult) {
        final String cap = scanResult.capabilities;
        final String[] modes = {"WPA", "EAP", "WEP"};
        for (int i = modes.length - 1; i >= 0; i--) {
            if (cap.contains(modes[i])) {
                return modes[i];
            }
        }
        return "OPEN";
    }


    public void bindNetworkProcess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.runOnUiThread(() -> {

                final ConnectivityManager manager = (ConnectivityManager) this.getApplicationContext()
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkRequest.Builder builder;
                builder = new NetworkRequest.Builder();
                //set the transport type do WIFI
                builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);

                Log.d("granglawifi", "requesting network");

                manager.requestNetwork(builder.build(), new ConnectivityManager.NetworkCallback() {
                    @Override
                    public void onAvailable(Network network) {
                        Log.d("granglawifi", "network awailable");

                        //cancelTimer();

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            boolean isConnected = manager.bindProcessToNetwork(network);
                            try {
                                final Handler handler = new Handler(Looper.getMainLooper());
                                handler.postDelayed(() -> {

                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            WiFiAddress address = new WiFiAddress(getApplicationContext());
                                            try {
                                                Socket socket = new Socket(address.getGatewayIPAddress(), WifiConnectingSingleton.getInstance().getPort());
                                                Log.d("granglawifi", "port: " + WifiConnectingSingleton.getInstance().getPort());
                                                granglaSocket = socket;
                                                server = false;
                                                startGame();
                                            } catch (IOException e) {
                                                Log.d("granglawifi", e.getMessage());
                                            }
                                        }
                                    }).start();

                                }, /*10000*/5000);
                                //do a callback or something else to alert your code that it's ok to send the message through socket now
                            } catch (Exception e) {
                                e.printStackTrace();
                                //fragmentPlayListBinding.layProgress.setVisibility(View.GONE);
                            }
                        } else {
                            Log.d("Called multipel times", "Yes");
                            //This method was deprecated in API level 23
                            boolean isConnected = ConnectivityManager.setProcessDefaultNetwork(network);
                            try {
                                final Handler handler = new Handler(Looper.getMainLooper());
                                handler.postDelayed(() -> {

                                    WiFiAddress address = new WiFiAddress(getApplicationContext());
                                    //ConnectToServer(address.getGatewayIPAddress(), String.valueOf(WifiConnectingSingleton.getInstance().getPort()));
                                }, 5000);
                                //do a callback or something else to alert your code that it's ok to send the message through socket now
                            } catch (Exception e) {
                                e.printStackTrace();
                                //fragmentPlayListBinding.layProgress.setVisibility(View.GONE);
                            }

                        }
                        manager.unregisterNetworkCallback(this);
                    }
                });
            });
        }
    }


    @SuppressLint("NewApi")
    public class WiFiAddress {

        Context mContext;
        WifiManager mWifiManager;
        WifiInfo mWifiInfo;


        public WiFiAddress(Context c) {
            mContext = c;
            mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
            mWifiInfo = mWifiManager.getConnectionInfo();
        }

        /**
         * Method to Get Gateway Ip Address
         *
         * @return Gateway Ip Address as String
         */
        public String getGatewayIPAddress() {
            if (mWifiManager != null) {
                final DhcpInfo dhcp = mWifiManager.getDhcpInfo();
                return ipIntToString(dhcp.gateway);
            }
            return null;
        }


        /**
         * Method for Conversion Ip Address From Int to String
         *
         * @param ipInt Ip as Int
         * @return Ip as String
         */
        public String ipIntToString(int ipInt) {
            String ip = "";
            for (int i = 0; i < 4; i++) {
                ip = ip + ((ipInt >> i * 8) & 0xFF) + ".";
            }
            return ip.substring(0, ip.length() - 1);
        }


    }

    public void closeConnection() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            if (isWifiApEnabled())
                changeStateWifiAp(false);
        } else {
            if (mReservation != null) {
                mReservation.close();
                mReservation = null;
            }
        }
    }

    public boolean isWifiApEnabled() {
        try {
            Method method = mWifiManager.getClass().getMethod("getWifiApState");
            return ((int) method.invoke(mWifiManager) == 13 || (int) method.invoke(mWifiManager) == 12);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void changeStateWifiAp(boolean activated) {
        Method method;
        try {
            method = mWifiManager.getClass().getDeclaredMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
            method.invoke(mWifiManager, mWifiConfig, activated);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
