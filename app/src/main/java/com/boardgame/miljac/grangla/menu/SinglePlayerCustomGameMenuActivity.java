package com.boardgame.miljac.grangla.menu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import com.boardgame.miljac.grangla.R;
import com.boardgame.miljac.grangla.gameplay.SinglePlayerGamePlayActivity;
import java.util.ArrayList;

public class SinglePlayerCustomGameMenuActivity extends AppCompatActivity {

    Spinner player1Spinner;
    Spinner spinnerPlayer2;
    PlayerImageListItem eyeListItem = new PlayerImageListItem();
    PlayerImageListItem buttonListItem = new PlayerImageListItem();
    PlayerImageListItem cloverListItem = new PlayerImageListItem();
    PlayerImageListItem starListItem = new PlayerImageListItem();
    private SeekBar levelSeekBar;
    private TextView levelTextView;
    private SharedPreferences sharedPrefs;

    private void setPlayerImagesOnSpinners() {
        eyeListItem.setData("EYE", R.drawable.pin39);
        buttonListItem.setData("BUTTON", R.drawable.pin40);
        cloverListItem.setData("CLOVER", R.drawable.pin42);
        starListItem.setData("STAR", R.drawable.pin43);

        player1Spinner = (Spinner) findViewById(R.id.spinner_player1);
        player1Spinner.setAdapter(new Player2PictureSpinnerAdapter(this, R.layout.row, getAllList()));
        player1Spinner.setBackgroundColor(Color.TRANSPARENT);
        player1Spinner.setDrawingCacheBackgroundColor(Color.TRANSPARENT);

        player1Spinner.setOnTouchListener(
                (new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent m) {
                        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                        }
                        return false;
                    }
                }));

        player1Spinner.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                }
            }
        });

        spinnerPlayer2 = (Spinner) findViewById(R.id.spinner_player2);
        final Player2PictureSpinnerAdapter adapterSpinner2 = new Player2PictureSpinnerAdapter(this, R.layout.row, new ArrayList<PlayerImageListItem>());
        spinnerPlayer2.setAdapter(adapterSpinner2);

        if (sharedPrefs == null) {
            sharedPrefs = getSharedPreferences("mrm", MODE_PRIVATE);
        }
        levelSeekBar.setProgress(sharedPrefs.getInt("mrm_LEVEL", 20));
        player1Spinner.setSelection(sharedPrefs.getInt("mrm_PLAYER_1_IMG", 0));

        adapterSpinner2.clear();

        if (!((sharedPrefs.getInt("mrm_PLAYER_1_IMG", 0) == 0))) {
            adapterSpinner2.add(eyeListItem);
        }

        if (!((sharedPrefs.getInt("mrm_PLAYER_1_IMG", 0) == 1))) {
            adapterSpinner2.add(buttonListItem);
        }

        if (!((sharedPrefs.getInt("mrm_PLAYER_1_IMG", 0) == 2))) {
            adapterSpinner2.add(cloverListItem);
        }

        if (!((sharedPrefs.getInt("mrm_PLAYER_1_IMG", 0) == 3))) {
            adapterSpinner2.add(starListItem);
        }

        spinnerPlayer2.setSelection(sharedPrefs.getInt("mrm_PLAYER_2_IMG", 0));

        player1Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                PlayerImageListItem selected2;
                selected2 = (PlayerImageListItem) spinnerPlayer2.getSelectedItem();
                adapterSpinner2.clear();
                if (!(arg2 == 0)) {
                    adapterSpinner2.add(eyeListItem);
                }
                if (!(arg2 == 1)) {
                    adapterSpinner2.add(buttonListItem);
                }
                if (!(arg2 == 2)) {
                    adapterSpinner2.add(cloverListItem);
                }
                if (!(arg2 == 3)) {
                    adapterSpinner2.add(starListItem);
                }
                spinnerPlayer2.setSelection(adapterSpinner2.getPosition(selected2));
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                adapterSpinner2.clear();
                adapterSpinner2.add(eyeListItem);
                adapterSpinner2.add(buttonListItem);
                adapterSpinner2.add(cloverListItem);
                adapterSpinner2.add(starListItem);
            }
        });
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        levelSeekBar = (SeekBar) findViewById(R.id.levelSeekBar);
        levelSeekBar.setThumbOffset(convertDipToPixels(8f));
        levelSeekBar.setProgress(20);
        levelTextView = (TextView) findViewById(R.id.level_text);
        levelSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                levelTextView.setText(String.valueOf(levelSeekBar.getProgress()));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        setPlayerImagesOnSpinners();

        if (savedInstanceState != null) {
            levelSeekBar.setProgress(savedInstanceState.getInt("mrm_LEVEL"));
            player1Spinner.setSelection(savedInstanceState.getInt("PLAYER_1_IMG"));
            spinnerPlayer2.setSelection(savedInstanceState.getInt("PLAYER_2_IMG"));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("mrm_LEVEL", levelSeekBar.getProgress());
        outState.putInt("PLAYER_1_IMG", player1Spinner.getSelectedItemPosition());
        outState.putInt("PLAYER_2_IMG", spinnerPlayer2.getSelectedItemPosition());

        if (sharedPrefs == null) {
            sharedPrefs = getSharedPreferences("mrm", MODE_PRIVATE);
        }
        SharedPreferences.Editor ed = sharedPrefs.edit();
        ed.putInt("mrm_LEVEL", levelSeekBar.getProgress());
        ed.putInt("mrm_PLAYER_1_IMG", player1Spinner.getSelectedItemPosition());
        ed.putInt("mrm_PLAYER_2_IMG", spinnerPlayer2.getSelectedItemPosition());
        ed.commit();
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (sharedPrefs == null) {
            sharedPrefs = getSharedPreferences("mrm", MODE_PRIVATE);
        }
        levelSeekBar.setProgress(sharedPrefs.getInt("mrm_LEVEL", 20));

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    /**
     * Called when the user clicks the Send button
     */
    public void startCustomGame(View view) {
        if (sharedPrefs == null) {
            sharedPrefs = getSharedPreferences("mrm", MODE_PRIVATE);
        }
        SharedPreferences.Editor ed = sharedPrefs.edit();
        ed.putInt("mrm_LEVEL", levelSeekBar.getProgress());
        ed.commit();

        Intent intent = new Intent(this, SinglePlayerGamePlayActivity.class);
        intent.putExtra("mrm_LEVEL", levelSeekBar.getProgress());
        intent.putExtra("PLAYER1_IMG", ((PlayerImageListItem) player1Spinner.getSelectedItem()).getLogo());
        intent.putExtra("PLAYER2_IMG", ((PlayerImageListItem) spinnerPlayer2.getSelectedItem()).getLogo());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public ArrayList<PlayerImageListItem> getAllList() {
        ArrayList<PlayerImageListItem> allList = new ArrayList<>();
        allList.add(eyeListItem);
        allList.add(buttonListItem);
        allList.add(cloverListItem);
        allList.add(starListItem);
        return allList;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    public void exit(View view) {
        finish();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    private int convertDipToPixels(float dip) {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float density = metrics.density;
        return (int) (dip * density);
    }

    public void decreaseLevel(View view) {
        int level = levelSeekBar.getProgress();
        level--;
        if ((level < 0) || (level > 100)) {
            return;
        }
        levelTextView.setText(String.valueOf(level));
        levelSeekBar.setProgress(level);
    }

    public void increaseLevel(View view) {
        int level = levelSeekBar.getProgress();
        level++;
        if ((level < 0) || (level > 100)) {
            return;
        }
        levelTextView.setText(String.valueOf(level));
        levelSeekBar.setProgress(level);
    }
}