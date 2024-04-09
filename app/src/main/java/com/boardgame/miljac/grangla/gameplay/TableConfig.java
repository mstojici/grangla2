package com.boardgame.miljac.grangla.gameplay;


import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.boardgame.miljac.grangla.R;


public class TableConfig {
    public static int TABLE_SIZE = 6;
    public static int MAX_WAITING_TIME = 2200;
    public static int MIN_WAITING_TIME = 750;
    public static int MAX_PIECES = 9;
    public static double RESULT_FACTOR = 1.2;
    public static int HALF_LIFE = 240000;
    public static int NO_OF_ROCKS = 6;
    public static int ROCK_MOVEMENT_PROBABILITY = 10000;//8000;
    public static int THINKING_TIME_MIN_LEVEL = 6400;
    public static int THINKING_TIME_MAX_LEVEL = 200;
    public static float NOTE_DURATION_FACTOR = 0.12F;

    public static int EYE_COLOR = 0xFF5BE1CA;
    public static int BUTTON_COLOR = 0xFFEB68C9 ;
    public static int CLOVER_COLOR = 0xFFA4F80F;
    public static int STAR_COLOR = 0xFFED9D1D;

    public static int EYE_COLOR_DESATURATED = 0xFF9e9e9e;
    public static int BUTTON_COLOR_DESATURATED = 0xFF9a9a9a;
    public static int CLOVER_COLOR_DESATURATED = 0xFF838383;
    public static int STAR_COLOR_DESATURATED = 0xFF858585;

    public static int pinBackground = R.drawable.pin41;
    public static int pinRock = R.drawable.pin20;


    public static Animation getFadeInAnim(Context ctx) {
        return  AnimationUtils.loadAnimation(ctx.getApplicationContext(), R.anim.fadein);
    }
    public static Animation getUpAnim(Context ctx) {
        return  AnimationUtils.loadAnimation(ctx.getApplicationContext(), R.anim.up);
    }
    public static Animation getDownAnim(Context ctx) {
        return  AnimationUtils.loadAnimation(ctx.getApplicationContext(), R.anim.down);
    }
    public static Animation getHorizontalAnim(Context ctx) {
        return  AnimationUtils.loadAnimation(ctx.getApplicationContext(), R.anim.horizontal);
    }
    public static Animation getVerticalAnim(Context ctx) {
        return  AnimationUtils.loadAnimation(ctx.getApplicationContext(), R.anim.vertical);
    }
    public static Animation getNormalAnim(Context ctx) {
        return  AnimationUtils.loadAnimation(ctx.getApplicationContext(), R.anim.normal);
    }
    public static Animation getNoneAnim(Context ctx) {
        return  AnimationUtils.loadAnimation(ctx.getApplicationContext(), R.anim.none);
    }
}