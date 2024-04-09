package com.boardgame.miljac.grangla.gameplay;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;

import com.boardgame.miljac.grangla.R;
import com.boardgame.miljac.grangla.music.MusicPlayer;
import com.boardgame.miljac.grangla.wifi.WifiConnectingSingleton;

import java.util.concurrent.locks.LockSupport;

public class SinglePlayerGamePlayActivity extends GamePlayActivity {

    private OtherPlayer otherPlayer = new OtherPlayer();
    TableViewRefreshing tableViewRefreshing;
    Thread otherPlayerThread;

    private class TableViewRefreshing implements Runnable {
        UIRefresh uIRefresh = new UIRefresh(SinglePlayerGamePlayActivity.this, true, false);

        public void run() {
            LockSupport.parkNanos(70_000_000);
            while (!gameDone) {
                if(gamePaused) continue;

                uIRefreshThread = new Thread(uIRefresh);
                runOnUiThread(uIRefreshThread);
                LockSupport.parkNanos(70_000_000);
            }
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        musicPlayer.mute();
        gamePaused = true;
        pausedTime = System.currentTimeMillis();
        saveSharedPreferences();
    }

    @Override
    protected void onResume(){
        super.onResume();

        if(musicPlayer == null){
            musicPlayer = new MusicPlayer();
        }

        if(!muted && !musicPlayer.isEndSong()) {
            musicPlayer = new MusicPlayer();
            musicPlayerThread = new Thread(musicPlayer);
            musicPlayer.setNoteDuration((long) (TableConfig.NOTE_DURATION_FACTOR * waitingTimeCross));
            musicPlayerThread.start();
        }

        if (gamePaused) gameStartTime += System.currentTimeMillis() - pausedTime;
        gamePaused = false;
    }

    private void setVisualElements(){
        setCommonVisualElements();

        soundToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if(musicPlayer != null) {
                        musicPlayer.mute();
                    }
                    musicPlayer = new MusicPlayer();
                    musicPlayerThread = new Thread(musicPlayer);
                    musicPlayer.setNoteDuration((long)(TableConfig.NOTE_DURATION_FACTOR * waitingTimeCross));
                    musicPlayerThread.start();
                    muted = false;
                } else {
                    musicPlayer.mute();
                    muted = true;
                }
            }
        });

        imageButton = (ImageButton) findViewById(R.id.discard_button);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                saveSharedPreferences();
                finish();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WifiConnectingSingleton.getInstance().setWifi(false);

        currentApiVersion = android.os.Build.VERSION.SDK_INT;
        setContentView(R.layout.activity_game_play);

        Intent intent = getIntent();
        level = intent.getIntExtra("mrm_LEVEL", 50);
        levelString = intent.getStringExtra("LEVEL_STRING");

        setPlayerImages(intent);

        setVisualElements();

        otherPlayerThread = new Thread(otherPlayer);
        otherPlayerThread.start();

        tableViewRefreshing = new TableViewRefreshing();
        tableViewRefreshingThread = new Thread(tableViewRefreshing);
        tableViewRefreshingThread.start();

        gameStartTime = System.currentTimeMillis();
        lastEventTime = gameStartTime;
    }



    @Override
    protected void onDestroy(){
        super.onDestroy();

        saveSharedPreferences();

        if (endDialog!=null)
            endDialog.dismiss();

        gameDone = true;
        musicPlayer.mute();

        try {
            otherPlayerThread.join();
        } catch (InterruptedException e) {
            //e.printStackTrace();
        }
        otherPlayerThread = null;

        try {
            tableViewRefreshingThread.join();
        } catch (InterruptedException e) {
            //e.printStackTrace();
        }
        tableViewRefreshingThread = null;

        try {
            musicPlayerThread.join();
        } catch (InterruptedException e) {
            //e.printStackTrace();
        }
        musicPlayerThread = null;

    }

    public void onFieldSelected(int x,int y) {
        synchronized (table) {
            if(allowCircle) {
                if(this.table.putIfPossible(State.circle, x, y)) {
                    lastMoveO = new Coordinates(x, y);
                    waitingMomentCircle = System.currentTimeMillis() + waitingTimeCircle;
                    startCircleTime = true;
                    allowCircle = false;
                    movesO.add(0, lastMoveO);
                }
            }
        }
    }

    private class OtherPlayer implements Runnable {
        private void thinkForAWhile(){
            try {
                double a = Math.random();
                a*= (0.7 + 0.42/50 * Math.abs(50 - (int)result));
                Thread.sleep(waitingTimeCross +
                        (long)(a*( (TableConfig.THINKING_TIME_MIN_LEVEL - TableConfig.THINKING_TIME_MAX_LEVEL) * (101-level) / 100 + TableConfig.THINKING_TIME_MAX_LEVEL)));
            } catch (InterruptedException e) {
                //e.printStackTrace();
            }

            if ((currentTime - lastEventTime) < 90){
                try {
                    Thread.sleep(90);
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                }
            }
        }

        public void run() {
            while(!gameDone){
                thinkForAWhile();

                if(gamePaused) continue;

                synchronized (table) {
                    c = table.calculateAndMakeAMove(State.cross);
                    lastMoveX = new Coordinates(c.x, c.y);
                    waitingMomentCross = System.currentTimeMillis() + waitingTimeCross;
                    startCrossTime = true;
                    allowCross = false;
                    movesX.add(0, lastMoveX);
                }
            }
        }
    }
}
