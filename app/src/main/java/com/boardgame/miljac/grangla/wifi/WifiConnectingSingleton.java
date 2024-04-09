package com.boardgame.miljac.grangla.wifi;

import android.net.wifi.WifiManager;

public class WifiConnectingSingleton {
    private static WifiConnectingSingleton single_instance = null;
    private boolean isWifi = false;
    private WifiManager.LocalOnlyHotspotReservation reservation;
    private int port;

    public boolean isWifi() {
        return isWifi;
    }

    public void setWifi(boolean wifi) {
        isWifi = wifi;
    }

    public WifiManager.LocalOnlyHotspotReservation getReservation() {
        return reservation;
    }

    public void setReservation(WifiManager.LocalOnlyHotspotReservation reservation) {
        this.reservation = reservation;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    private WifiConnectingSingleton()
    {
    }

    public static WifiConnectingSingleton getInstance()
    {
        if (single_instance == null)
            single_instance = new WifiConnectingSingleton();
        return single_instance;
    }
}
