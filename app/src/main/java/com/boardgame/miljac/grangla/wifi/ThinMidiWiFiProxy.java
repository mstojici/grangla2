package com.boardgame.miljac.grangla.wifi;

import android.util.Log;
import android.util.Pair;

import com.boardgame.miljac.grangla.wifi.GranglaWiFiService;

import org.billthefarmer.mididriver.MidiDriver;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.LockSupport;

public class ThinMidiWiFiProxy implements Runnable {
    boolean stop = false;
    MidiDriver midiDriver;
    long gameStartTime;
    ConcurrentLinkedQueue<Pair<Integer, byte[]>> q = new ConcurrentLinkedQueue<>();
    public GranglaWiFiService wifiService = null;
    Pair<Integer, byte[]> current;
    public boolean silence = false;

    public ThinMidiWiFiProxy(long gameStartTime, MidiDriver midiDriver, GranglaWiFiService wifiService){
        this.gameStartTime = gameStartTime;
        this.midiDriver = midiDriver;
        this.wifiService = wifiService;
    }

    public void stop(){
        stop = true;
    }

    public void sendAndPlay(byte[] msg){
        int time = (int)(System.currentTimeMillis() - gameStartTime + 1500);

        q.add(new Pair<>(time, msg));
        wifiService.sendMidi(time, msg);
    }

    public void send(byte[] msg){
        int time = (int)(System.currentTimeMillis() - gameStartTime + 1500);
        wifiService.sendMidi(time, msg);
    }

    public void serverPlay(byte[] msg){
        int time = (int)(System.currentTimeMillis() - gameStartTime + 1500);
        q.add(new Pair<>(time, msg));
    }

    public void clientPlay(int time, byte[] msg){
        q.add(new Pair<>(time, msg));
    }


    @Override
    public void run() {
        while(!stop){
            current = q.poll();

            if(current == null){
                LockSupport.parkNanos(1_000_000);
                continue;
            }

            while (System.currentTimeMillis() < (gameStartTime + current.first)) {
                LockSupport.parkNanos(1_000_000);
            }

            Log.d("granglaproxy", "time:"+current.first + "\t waiting:" + q.size());

            byte[] msg = current.second;
            boolean isNoteOnMsg = (byte)(msg[0] & 0xF0) == (byte)0x90;

            if(!silence || !isNoteOnMsg){
                midiDriver.write(current.second);
            }
        }
    }
}
