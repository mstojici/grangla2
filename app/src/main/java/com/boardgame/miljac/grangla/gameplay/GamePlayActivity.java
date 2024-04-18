package com.boardgame.miljac.grangla.gameplay;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.boardgame.miljac.grangla.R;
import com.boardgame.miljac.grangla.gameUI.EndDialog;
import com.boardgame.miljac.grangla.gameUI.ResultBarAnimation;
import com.boardgame.miljac.grangla.gameUI.TableFragment;
import com.boardgame.miljac.grangla.gameUI.TableView;
import com.boardgame.miljac.grangla.high_scores.HighScoresHelper;
import com.boardgame.miljac.grangla.menu.SharedPreferencesKeys;
import com.boardgame.miljac.grangla.music.MusicPlayer;

import java.util.concurrent.CopyOnWriteArrayList;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class GamePlayActivity extends AppCompatActivity {
    SharedPreferences mPrefs;
    ToggleButton soundToggle;

    int player1Image;
    int player2Image;
    int player1Color;
    int player2Color;
    int player1ColorDesaturated;
    int player2ColorDesaturated;
    int level;
    Table table = new Table(3);
    int currentApiVersion;

    Boolean gameDone = false;
    Boolean gamePaused = false;
    Boolean muted = false;
    boolean firstTimeAnimatedProgress = true;
    EndDialog endDialog;

    Coordinates c;
    Coordinates lastMoveO;
    Coordinates lastMoveX;
    double result = 50;
    long waitingTimeCircle = 3000;
    long waitingMomentCircle = 0;
    boolean allowCircle = true;
    long waitingTimeCross = 3000;
    long waitingMomentCross = 0;
    long gameStartTime, currentTime, lastEventTime, pausedTime;
    boolean allowCross = true;
    boolean startCircleTime = false;
    boolean startCrossTime = false;
    String levelString;
    boolean gameEndSignal = false;

    ProgressBar resultBar, resultBar2, circleBar, crossBar;
    TextView textClock;
    TableFragment tableFragment;
    TableView tableView;
    Thread musicPlayerThread;
    MusicPlayer musicPlayer;

    CopyOnWriteArrayList movesO = new CopyOnWriteArrayList();
    CopyOnWriteArrayList movesX = new CopyOnWriteArrayList();

    int[][] sequenceDirections = new int[6][6];
    HighScoresHelper.HttpGetScoresAsyncTask getScoresTask;

    Thread tableViewRefreshingThread;
    Thread uIRefreshThread;

    public abstract void onFieldSelected(int x, int y);

    ImageButton imageButton;
    ResultBarAnimation animResult;

    public void setGameResult(double result) {
        this.result = result;
    }

    public double getGameResult() {
        return this.result;
    }

    void saveSharedPreferences(){
        if(mPrefs == null) {
            mPrefs = getSharedPreferences(SharedPreferencesKeys.SHARED_PREFERENCES, MODE_PRIVATE);
        }
        SharedPreferences.Editor ed = mPrefs.edit();
        ed.putBoolean(SharedPreferencesKeys.SOUND, soundToggle.isChecked());

        ed.commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        saveSharedPreferences();
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle inState){
        super.onRestoreInstanceState(inState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (currentApiVersion >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    protected void setPlayerImages(Intent intent) {
        player1Image = intent.getIntExtra("PLAYER1_IMG", R.drawable.pin39);
        player2Image = intent.getIntExtra("PLAYER2_IMG", R.drawable.pin40);

        this.table = new Table(level);

        if (player1Image == R.drawable.pin39) {
            player1Color = TableConfig.EYE_COLOR;
            player1ColorDesaturated = TableConfig.EYE_COLOR_DESATURATED;
        }
        if (player1Image == R.drawable.pin40) {
            player1Color = TableConfig.BUTTON_COLOR;
            player1ColorDesaturated = TableConfig.BUTTON_COLOR_DESATURATED;
        }
        if (player1Image == R.drawable.pin42) {
            player1Color = TableConfig.CLOVER_COLOR;
            player1ColorDesaturated = TableConfig.CLOVER_COLOR_DESATURATED;
        }
        if (player1Image == R.drawable.pin43) {
            player1Color = TableConfig.STAR_COLOR;
            player1ColorDesaturated = TableConfig.STAR_COLOR_DESATURATED;
        }

        if (player2Image == R.drawable.pin39) {
            player2Color = TableConfig.EYE_COLOR;
            player2ColorDesaturated = TableConfig.EYE_COLOR_DESATURATED;
        }
        if (player2Image == R.drawable.pin40) {
            player2Color = TableConfig.BUTTON_COLOR;
            player2ColorDesaturated = TableConfig.BUTTON_COLOR_DESATURATED;
        }
        if (player2Image == R.drawable.pin42) {
            player2Color = TableConfig.CLOVER_COLOR;
            player2ColorDesaturated = TableConfig.CLOVER_COLOR_DESATURATED;
        }
        if (player2Image == R.drawable.pin43) {
            player2Color = TableConfig.STAR_COLOR;
            player2ColorDesaturated = TableConfig.STAR_COLOR_DESATURATED;
        }
    }

    protected void setCommonVisualElements() {
        textClock = (TextView)findViewById(R.id.text_clock);
        textClock.setShadowLayer(2, 0, 0, Color.WHITE);

        resultBar = (ProgressBar)findViewById(R.id.result_bar);
        resultBar.setProgress(50);
        resultBar.getProgressDrawable().setColorFilter(player2Color,
                android.graphics.PorterDuff.Mode.SRC_IN);
        resultBar2 = (ProgressBar)findViewById(R.id.result_bar2);
        resultBar2.setProgress(50);
        resultBar2.getProgressDrawable().setColorFilter(player1Color,
                android.graphics.PorterDuff.Mode.SRC_IN);

        circleBar = (ProgressBar)findViewById(R.id.circle_time_bar);
        circleBar.setProgress(100);
        circleBar.invalidate();
        circleBar.getProgressDrawable().setColorFilter(
                player1Color & 0xA0FFFFFF,
                android.graphics.PorterDuff.Mode.SRC_IN);
        crossBar = (ProgressBar)findViewById(R.id.cross_time_bar);
        crossBar.setProgress(100);
        crossBar.invalidate();
        crossBar.getProgressDrawable().setColorFilter(
                player2Color & 0xA0FFFFFF,
                android.graphics.PorterDuff.Mode.SRC_IN);

        tableFragment = (TableFragment)
                getSupportFragmentManager().findFragmentById(R.id.Table);
        tableView = tableFragment.tableView;


        if(mPrefs == null) {
            mPrefs = getSharedPreferences(SharedPreferencesKeys.SHARED_PREFERENCES, MODE_PRIVATE);
        }

        soundToggle = (ToggleButton) findViewById(R.id.toggle_sound_button);


        if((mPrefs.getBoolean(SharedPreferencesKeys.SOUND, true))) {
            soundToggle.setChecked(true);
        } else {
            if(musicPlayer != null) {
                musicPlayer.mute();
            }
            musicPlayer = new MusicPlayer();
            musicPlayerThread = new Thread(musicPlayer);
            musicPlayer.setNoteDuration((long) (TableConfig.NOTE_DURATION_FACTOR * waitingTimeCross));
            muted = true;
        }

    }
}
