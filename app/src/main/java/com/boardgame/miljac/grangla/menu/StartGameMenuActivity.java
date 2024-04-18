package com.boardgame.miljac.grangla.menu;

import static com.boardgame.miljac.grangla.music.MusicHelpers.getRandomElement;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;
import com.boardgame.miljac.grangla.R;
import com.boardgame.miljac.grangla.gameplay.Level;
import com.boardgame.miljac.grangla.gameplay.SinglePlayerGamePlayActivity;
import com.boardgame.miljac.grangla.gameplay.WiFiGamePlayActivity;
import com.boardgame.miljac.grangla.high_scores.HighScoresActivity;

import java.util.Arrays;

public class StartGameMenuActivity extends AppCompatActivity {
    private int currentApiVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentApiVersion = Build.VERSION.SDK_INT;
        setContentView(R.layout.activity_second_menu);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        setUiFlags();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setUiFlags();
    }

    @Override
    public void onResume() {
        super.onResume();
        setUiFlags();
    }

    private void setUiFlags(){
        if (currentApiVersion >= Build.VERSION_CODES.KITKAT)
        {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    public void beginner(View view) {
        Intent intent = new Intent(this, SinglePlayerGamePlayActivity.class);
        intent.putExtra("mrm_LEVEL", 15);
        intent.putExtra("LEVEL_STRING", Level.BEGINNER.name());
        intent.putExtra("PLAYER1_IMG", R.drawable.pin42);
        intent.putExtra("PLAYER2_IMG", R.drawable.pin43);
        startActivity(intent);
    }

    public void intermediate(View view) {
        Intent intent = new Intent(this, SinglePlayerGamePlayActivity.class);
        intent.putExtra("mrm_LEVEL", 55);
        intent.putExtra("LEVEL_STRING", Level.MID.name());
        intent.putExtra("PLAYER1_IMG", R.drawable.pin42);
        intent.putExtra("PLAYER2_IMG", R.drawable.pin40);
        startActivity(intent);
    }

    public void expert(View view) {
        Intent intent = new Intent(this, SinglePlayerGamePlayActivity.class);
        intent.putExtra("mrm_LEVEL", 90);
        intent.putExtra("LEVEL_STRING", Level.EXPERT.name());
        intent.putExtra("PLAYER1_IMG", R.drawable.pin42);
        intent.putExtra("PLAYER2_IMG", R.drawable.pin39);
        startActivity(intent);
    }

    public void bluetoothServer(View view) {
        WifiManager wifi = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
        if (wifi.isWifiEnabled()){
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.turn_wifi_off),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.turn_location_on),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String[] perms = {"android.permission.FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION"};

        int permsRequestCode = 300;
        requestPermissions(new String[]{Manifest.permission.NEARBY_WIFI_DEVICES,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                //Manifest.permission.INTERNET
        }, permsRequestCode);
    }


    public void bluetoothClient(View view) {
        WifiManager wifi = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
        if (!wifi.isWifiEnabled()){
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.turn_wifi_on),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.turn_location_on),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String[] perms = {"android.permission.FINE_LOCATION", "android.permission.CAMERA"};

        int permsRequestCode = 200;
        requestPermissions(perms, permsRequestCode);


    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults){

        switch(permsRequestCode){

            case 200:
                Intent intent = new Intent(this, WiFiGamePlayActivity.class);
                intent.putExtra("PLAYER1_IMG", R.drawable.pin42);
                intent.putExtra("PLAYER2_IMG",
                        (int)getRandomElement(Arrays.asList(
                                R.drawable.pin43,
                                R.drawable.pin40,
                                R.drawable.pin39)));
                intent.putExtra("server", "no");
                startActivityForResult(intent, 55);
                break;
            case 300:
                Intent intent2 = new Intent(this, WiFiGamePlayActivity.class);
                intent2.putExtra("PLAYER1_IMG", R.drawable.pin42);
                intent2.putExtra("PLAYER2_IMG",
                        (int)getRandomElement(Arrays.asList(
                                R.drawable.pin43,
                                R.drawable.pin40,
                                R.drawable.pin39)));
                intent2.putExtra("server", "yes");
                startActivityForResult(intent2, 55);
                break;
        }
    }

    public void customLevel(View view) {
        Intent intent = new Intent(this, SinglePlayerCustomGameMenuActivity.class);
        startActivity(intent);
    }

    public void highScores(View view) {
        Intent intent = new Intent(this, HighScoresActivity.class);
        startActivity(intent);
    }

    public void exit(View view) {
        this.finish();
    }
}