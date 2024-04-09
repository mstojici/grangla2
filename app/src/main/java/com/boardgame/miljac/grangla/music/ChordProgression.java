package com.boardgame.miljac.grangla.music;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.boardgame.miljac.grangla.music.Chord.*;
import static com.boardgame.miljac.grangla.music.MusicHelpers.*;

public class ChordProgression {
    private String name;
    private List<Chord> chords;
    private int current = 0;
    private List<Integer> C_scale  = new ArrayList<Integer>(Arrays.asList(48, 50, 52, 53, 55, 57, 59));
    private List<Integer> Cm_scale  = new ArrayList<Integer>(Arrays.asList(48, 50, 51, 53, 55, 56, 58));
    private List<Integer> scale;

    ChordProgression(){
        configRandomProgression();
    }

    private void configRandomProgression(){
        switch(getRandomInRange(1, 12) /*(int)getRandomElement(Arrays.asList(6, 11, 12))*/) {
            case 1:
                chords = Arrays.asList(C, C, Dm, G7);
                name = "II V I major";
                scale = C_scale;
                break;
            case 2:
                chords = Arrays.asList(Em, A7, Dm, G7, C, Am, Dm, G7);
                name = "rhythm changes";
                scale = C_scale;
                break;
            case 3:
                chords = Arrays.asList(Dm, DisDim7, Em, A7, C, CisDim7);
                name = "dim7 passing chords";
                scale = C_scale;
                break;
            case 4:
                chords = Arrays.asList(D7, D7, Dm, G7, C, C, C, C);
                name = "take the a train";
                scale = C_scale;
                break;
            case 5:
                chords = Arrays.asList(Gm, C7, F, F, C, C, C, C);
                name = "I to IV";
                scale = C_scale;
                break;
            case 6:
                chords = Arrays.asList(Em, A7, Dm, G7, C, C7, F, Fm);
                name = "IV to IV minor";//tu je nekaj cudno
                scale = C_scale;
                break;
            case 7:
                chords = Arrays.asList(Cm, Cm, Dmb5, G7);
                name = "II V I minor";
                scale = Cm_scale;
                break;
            case 8:
                chords = Arrays.asList(Cm, Cm7Bb, Ab7, G7);
                name = "stray cat strut";
                scale = Cm_scale;
                break;
            case 9:
                chords = Arrays.asList(Cm, Amb5, Dmb5, G7);
                name = "minor i vi ii V";
                scale = Cm_scale;
                break;
            case 10:
                chords = Arrays.asList(Cm, Cm, Fm, Gm);
                name = " minor i iv v";
                scale = Cm_scale;
                break;
            case 11:
                chords = Arrays.asList(Cm, Ab, Eb, Fm);
                name = "minor i-VI-III-iv";
                scale = Cm_scale;
                break;
            case 12:
                chords = Arrays.asList(Cm, Ab, Eb, Hb);
                name = "minor i VI III VII";
                scale = Cm_scale;
                break;
            default:
                Log.e("GRANGLA", "ChordProgression error!");
        }
        Log.d("grangleninstr: ", name);
    }

    public String getName() {
        return name;
    }

    public Chord getNextChord(){
        current = current + 1;
        if(current >= chords.size()){
            current = 0;
        }
        return chords.get(current);
    }

    public Chord peakAtNextChord(){
        return chords.get((current + 1) % chords.size());
    }

    public Chord getCurrentChord(){
        return chords.get(current);
    }

    public List<Integer> getScale() {
        return scale;
    }
}
