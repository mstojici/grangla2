package com.boardgame.miljac.grangla.wifi;

import android.util.Log;

import com.boardgame.miljac.grangla.gameplay.Coordinates;
import com.boardgame.miljac.grangla.gameplay.TableConfig;
import com.boardgame.miljac.grangla.gameplay.WiFiGamePlayActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.Getter;
import lombok.Setter;


public class GranglaWiFiService extends Thread{
    final Socket mmSocket;
    final InputStream mmInStream;
    final OutputStream mmOutStream;
    byte[] mmBuffer;
    byte[] mmStateBuffer;
    WiFiGamePlayActivity wifiActivity;
    @Setter
    @Getter
    private ThinMidiWiFiProxy proxy;
    long checkTime = System.currentTimeMillis();
    boolean checkWaiting = false;
    ExecutorService writeExecutor  = Executors.newSingleThreadExecutor();

    public GranglaWiFiService(Socket socket, WiFiGamePlayActivity wifiActivity) {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        this.wifiActivity = wifiActivity;

        // Get the input and output streams; using temp objects because
        // member streams are final.
        try {
            tmpIn = socket.getInputStream();
        } catch (IOException e) {
            Log.e("grangladevice", "Error occurred when creating input stream", e);
        }
        try {
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e("grangladevice", "Error occurred when creating output stream", e);
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;

        this.start();
    }

    public void run() {
        mmBuffer = new byte[1024];
        mmStateBuffer = new byte[1024];
        int numBytes; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs.
        while (true) {
            try {
                // Read from the InputStream.
                char c = (char) mmInStream.read();
                Log.d("grangladevice", "" + c);
                switch(c){
                    case 'S':
                        Log.d("grangladevice", "" +mmInStream.read(mmBuffer, 0,41));
                        updateTableState();
                        break;
                    case 'M':
                        mmInStream.read(mmBuffer, 0,2);
                        receiveMove();
                        break;
                    case 'R':
                        if(checkWaiting && (System.currentTimeMillis() - checkTime < 500)){
                            Log.d("grangladevicee1", "" + mmInStream.read(mmBuffer, 0, 41 + 36));//ovdje odlazi u smece zapravo
                            break;
                        }

                        if(!checkWaiting) {
                            Log.d("grangladevicee2", "" + mmInStream.read(mmStateBuffer, 0, 41 + 36));
                            if(checkSpaceHeading()){
                                checkTime = System.currentTimeMillis();
                                checkWaiting = true;
                                break;
                            }
                        } else {
                            Log.d("grangladevicee3", "" + mmInStream.read(mmBuffer, 0, 41 + 36));//ovdje odlazi u smece zapravo
                        }
                        checkWaiting = false;

                        updateSpaceHeading();
                        updateTableState();
                        break;
                    case 'G':
                        int len = mmInStream.read();
                        Log.d("grangladevice", ""+len);

                        byte[] timeArr = new byte[4];
                        mmInStream.read(timeArr, 0, 4);
                        int time = ByteBuffer.wrap(timeArr).order(ByteOrder.LITTLE_ENDIAN).getInt();

                        byte[] midi = new byte[len];
                        Log.d("grangladevice", ""+mmInStream.read(midi, 0,len));

                        if(proxy != null) {
                            proxy.clientPlay(time, midi);
                        }
                        break;
                    case 'W':
                        wifiActivity.setGameEndSignal(true);
                        break;
                    case 'X':
                        wifiActivity.setResult(54);
                        wifiActivity.finish();
                        break;
                    default:
                        Log.d("grangladevice", "invalid message: " + c);
                        return;
                        //cancel();
                        /*bluetoothActivity.setResult(55);
                        bluetoothActivity.finish();*/
                }
                // parsiraj poruku
                // reagiraj u activitiju
            } catch (IOException e) {
                Log.d("grangladevice", "Input stream was disconnected", e);
                //cancel();
                if(!wifiActivity.getGameDone()) {
                    wifiActivity.setResult(55);
                    wifiActivity.finish();
                }

                break;
            }
        }
    }

    // Call this from the main activity to send data to the remote device.
    public void write(byte[] bytes) {
        writeExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    mmOutStream.write(bytes);
                    //TODO
                    mmOutStream.flush(); //probaj s tim !!
                } catch (Exception e) {
                    Log.e("grangladevice", "Error occurred when sending data", e);

                    if (!wifiActivity.getGameDone()) {
                        wifiActivity.setResult(55);
                        wifiActivity.finish();
                    }
                }
            }
        });
    }

    // Call this method from the main activity to shut down the connection.
    public void cancel() {
        try {
            Log.d("granglawifi", "grangla service cancel");
            mmSocket.close();
            Log.d("granglawifi", "grangla service canceled");
        } catch (IOException e) {
            Log.d("granglawifi", "Could not close the connect socket", e);
        }
    }

    public void sendMidi(int time, byte[] midi){
        byte[] msg = new byte[1 + 1 + 4 + midi.length];
        msg[0] = 'G';
        msg[1] = (byte) midi.length;

        ByteBuffer timeBuff = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(time);
        timeBuff.position(0);
        timeBuff.get(msg, 2, 4);

        for (int i = 0; i < midi.length; i++) {
            msg[i+6] = midi[i];
        }
        write(msg);
    }

    public void sendTableState(){
        byte[] msg = new byte[TableConfig.TABLE_SIZE * TableConfig.TABLE_SIZE + 1 + 4 + 1 + 36];
        msg[0] = 'R';
        int c = 1;
        for (int i = 0; i < TableConfig.TABLE_SIZE; i++) {
            for (int j = 0; j < TableConfig.TABLE_SIZE; j++) {
                com.boardgame.miljac.grangla.gameplay.State state = wifiActivity.getTable().get(i, j);
                if (state == com.boardgame.miljac.grangla.gameplay.State.circle) {
                    msg[c] = 1;
                }
                if (state == com.boardgame.miljac.grangla.gameplay.State.cross) {
                    msg[c] = 2;
                }
                if (state == com.boardgame.miljac.grangla.gameplay.State.rock) {
                    msg[c] = 3;
                }
                if (state == com.boardgame.miljac.grangla.gameplay.State.empty) {
                    msg[c] = 4;
                }
                c = c + 1;
            }
        }
        msg[c] = (byte) (wifiActivity.getWaitingTimeCircle() % 256);
        msg[c+1] = (byte) (wifiActivity.getWaitingTimeCircle() / 256);
        msg[c+2] = (byte) (wifiActivity.getWaitingTimeCross() % 256);
        msg[c+3] = (byte) (wifiActivity.getWaitingTimeCross() / 256);

        msg[c+4] = (byte) wifiActivity.getGameResult();

        c = c + 5;
        for (int i = 0; i < TableConfig.TABLE_SIZE; i++) {
            for (int j = 0; j < TableConfig.TABLE_SIZE; j++) {
                msg[c] = (byte) wifiActivity.getSequenceDirections()[i][j];
                c++;
            }
        }

        write(msg);
    }

    public void sendMove(int i, int j){
        byte[] msg = new byte[3];
        msg[0] = 'M';
        msg[1] = (byte) i;
        msg[2] = (byte) j;
        write(msg);
    }

    public void sendWin(){
        byte[] msg = new byte[1];
        msg[0] = 'W';
        write(msg);
    }

    public void sendExit(){
        byte[] msg = new byte[1];
        msg[0] = 'X';
        write(msg);
    }

    public void updateTableState(){
        int c=0;
        for (int i = 0; i < TableConfig.TABLE_SIZE; i++) {
            for (int j = 0; j < TableConfig.TABLE_SIZE; j++) {
                if(mmStateBuffer[c] == 2){
                    if(wifiActivity.getTable().get(i, j) == com.boardgame.miljac.grangla.gameplay.State.empty) {
                        wifiActivity.setLastMoveO(new Coordinates(i, j));
                        wifiActivity.setWaitingMomentCircle(System.currentTimeMillis() + wifiActivity.getWaitingTimeCircle());
                        wifiActivity.setStartCircleTime(true);
                        wifiActivity.setAllowCircle(false);
                        wifiActivity.getMovesO().add(0, wifiActivity.getLastMoveO());
                    }
                    wifiActivity.getTable().put(com.boardgame.miljac.grangla.gameplay.State.circle, i, j);
                }
                if(mmStateBuffer[c] == 1){
                    if(wifiActivity.getTable().get(i, j) == com.boardgame.miljac.grangla.gameplay.State.empty) {
                        wifiActivity.setLastMoveX(new Coordinates(i, j));
                        wifiActivity.setWaitingMomentCross(System.currentTimeMillis() + wifiActivity.getWaitingTimeCross());
                        wifiActivity.setStartCrossTime(true);
                        wifiActivity.setAllowCross(false);
                        wifiActivity.getMovesX().add(0, wifiActivity.getLastMoveX());
                    }
                    wifiActivity.getTable().put(com.boardgame.miljac.grangla.gameplay.State.cross, i, j);
                }
                if(mmStateBuffer[c] == 3){
                    wifiActivity.getTable().put(com.boardgame.miljac.grangla.gameplay.State.rock, i, j);
                }
                if(mmStateBuffer[c] == 4){
                    wifiActivity.getTable().put(com.boardgame.miljac.grangla.gameplay.State.empty, i, j);
                }
                c = c + 1;
            }
        }

        wifiActivity.setWaitingTimeCross((long)mmStateBuffer[c+1] * 256 + mmStateBuffer[c]);
        wifiActivity.setWaitingTimeCircle((long)mmStateBuffer[c+3] * 256 + mmStateBuffer[c+2]);
        wifiActivity.setGameResult(100 - mmStateBuffer[c+4]);
    }

    private boolean checkSpaceHeading(){
        int c = 41;

        for(int x = c; x < c + 36; x = x + 1){
            if(mmStateBuffer[x] != 0) {
                return true;
            }
        }
        return false;
    }

    private void updateSpaceHeading(){
        int c = 41;

        for (int i = 0; i < TableConfig.TABLE_SIZE; i++) {
            for (int j = 0; j < TableConfig.TABLE_SIZE; j++) {
                wifiActivity.getSequenceDirections()[i][j] = mmStateBuffer[c];
                c = c + 1;
            }
        }
    }

    public void receiveMove(){
        if(!wifiActivity.isAllowCross()){
            return;
        }
        int i = mmBuffer[0];
        int j = mmBuffer[1];

        if(wifiActivity.getTable().get(i, j) == com.boardgame.miljac.grangla.gameplay.State.circle){
            if((i+j)%2 == 0) {
                wifiActivity.getTable().put(com.boardgame.miljac.grangla.gameplay.State.cross, i, j);
            } else {
                return;
            }
        }

        wifiActivity.getTable().putIfPossible(com.boardgame.miljac.grangla.gameplay.State.cross, i, j);

        wifiActivity.setLastMoveX(new Coordinates(i, j));
        wifiActivity.setWaitingMomentCross(System.currentTimeMillis() + wifiActivity.getWaitingTimeCross());
        wifiActivity.setStartCrossTime(true);
        wifiActivity.setAllowCross(false);
        wifiActivity.getMovesX().add(0, wifiActivity.getLastMoveX());
    }
}
