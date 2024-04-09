package com.boardgame.miljac.grangla.music;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import static com.boardgame.miljac.grangla.music.MusicHelpers.jumpDownscale;
import static com.boardgame.miljac.grangla.music.MusicHelpers.jumpUpscale;
import static com.boardgame.miljac.grangla.music.MusicHelpers.probabilityFulfilled;
import static com.boardgame.miljac.grangla.music.MusicHelpers.putInScale;
import static com.boardgame.miljac.grangla.music.MusicHelpers.randomAround;
import static com.boardgame.miljac.grangla.music.MusicHelpers.randomUp;
import static com.boardgame.miljac.grangla.music.MusicHelpers.removeChordFromScale;

import com.boardgame.miljac.grangla.wifi.WifiConnectingSingleton;


public class TrumpetGenerator {
    private ChordProgression progression;
    private List<Double> currentLengths;
    private List<Double> lengths1;
    private List<Double> lengths2;
    private List<Integer> currentSteps;
    private List<Integer> steps1;
    private List<Integer> steps2;
    private int lengthsPosition;
    private int lastTone;
    private double overrunBeats;


    public TrumpetGenerator(ChordProgression progression){
        this.progression = progression;
        lengths1 = generateLengths();
        lengths2 = generateLengths();
        steps1 = generateSteps(lengths1.size());
        steps2 = generateSteps(lengths2.size());
        if(probabilityFulfilled(0.5)){
            currentSteps = steps1;
            currentLengths = lengths1;
        } else {
            currentSteps = steps2;
            currentLengths = lengths2;
        }
        lengthsPosition = 0;
        lastTone = 72; // C
        overrunBeats = 0;
    }

    private List<Double> generateLengths(){
        List<Double> lengths = new ArrayList<Double>();
        int maxLenOneMode = 8;

        for (int i=0; i<7; i++){
            double mode = randomUp(0, 6.5);

            if (mode < 1) { //po pol udara
                int n = (int) Math.floor(randomUp(1, maxLenOneMode - 1)) * 2;
                for (int x = 0; x < n; x++) {
                    lengths.add(0.5);
                }
            } else if (mode < 2){  //po jedan udar
                int n = (int) Math.floor(randomUp(1, maxLenOneMode - 1)) ;
                for (int x = 0; x < n; x++) {
                    lengths.add(1d);
                }
            }  else if (mode < 3){  //po dva udara
                int n = (int) Math.floor(randomUp(2, maxLenOneMode - 2) / 2);
                for (int x = 0; x < n; x++) {
                    lengths.add(2d);
                }
            }  else if (mode < 4){  //po dvije trecine udara
                int n = (int) Math.floor(randomUp(1, maxLenOneMode - 2) / 2) * 3;
                for (int x = 0; x < n; x++) {
                    lengths.add(2/3d);
                }
            }  else if (mode < 5){  //po jedan sinkopirano
                int n = (int) Math.floor(randomUp(1, maxLenOneMode - 2) / 2);
                for (int x = 0; x < n; x++) {
                    lengths.add(1.5d);
                    lengths.add(0.5d);
                }
            } else { // po pola sinkopirano
                int n = (int) Math.floor(randomUp(1, maxLenOneMode - 1));
                for (int x = 0; x < n; x++) {
                    lengths.add(0.75d);
                    lengths.add(0.25d);
                }
            }
        }

        int x = 0;
        while (x < (lengths.size() - 1)){
            if(probabilityFulfilled(0.14)){
                lengths.set(x, lengths.get(x) + lengths.get(x+1));
                lengths.remove(x + 1);
            }
            x = x + 1;
        }
        return lengths;
    }

    private List<Integer> generateSteps(int n){
        List<Integer> steps = new ArrayList<Integer>();
        int maxStep = 5;
        double changeProbability = 0.23;
        int step1 = (int)Math.round(randomAround(0, maxStep));
        int step2 = (int)Math.round(randomAround(0, maxStep));
        int step3 = (int)Math.round(randomAround(0, maxStep));
        boolean twoOrThree = probabilityFulfilled(0.4);

        int c = 0;
        while(c < n){
            c = c + 1;
            if (probabilityFulfilled(changeProbability) || step1 == 0){
                step1 = (int)Math.round(randomAround(0, maxStep));
            }
            if (probabilityFulfilled(changeProbability) || step2 == 0){
                step2 = (int)Math.round(randomAround(0, maxStep));
            }
            if (probabilityFulfilled(changeProbability) || step3 == 0){
                step3 = (int)Math.round(randomAround(0, maxStep));
            }

            if(!twoOrThree){
                if(c%2 == 0){
                    steps.add(step1);
                } else {
                    steps.add(step2);
                }
            } else {
                if (c%3 == 0){
                    steps.add(step1);
                }
                if (c%3 == 1){
                    steps.add(step2);
                }
                if (c%3 == 2){
                    steps.add(step3);
                }
            }
        }

        return steps;
    }

    /*private List<Double> generateVelocities(int n){
        List<Double> velocities = new ArrayList<Double>();

        for(int x=0; x<n; x++){
            if(probabilityFulfilled(0.5)){

            }
        }
    }*/

    public Tune generateNextMeasure(int measure){
        Tune trumpet = new Tune();
        Chord chord = progression.getCurrentChord();
        double lengthSoFar = overrunBeats;

        while(lengthSoFar < measure){

            //int tone = lastTone + currentSteps.get(lengthsPosition);
            int tone;
            int steps = currentSteps.get(lengthsPosition);
            List<Integer> effectiveScale = removeChordFromScale/*integrateChordInScale*/(progression.getScale(),
                    progression.getCurrentChord().getPitches());

            if (steps < 0){
                tone = jumpDownscale(lastTone, effectiveScale, Math.abs(steps));
            } else {
                tone = jumpUpscale(lastTone, effectiveScale, steps);
            }

            if(tone > lastTone + 12) tone -= 12;
            if(tone < lastTone - 12) tone += 12;

            //if(0 == lengthSoFar) tone = putInScale(tone, progression.getScale());

            if (tone > 72 + 12 - MusicGlobals.jump){
                tone = tone - 12;
            } else if (tone < 72 - 12 - MusicGlobals.jump){
                tone = tone + 12;
            }
            double length = currentLengths.get(lengthsPosition);
            if((lengthSoFar + length

                    >= measure - 1) &&
                    (lengthSoFar + length < measure) &&
                    (measure != 2) &&
                    probabilityFulfilled(0.38)){
                length = measure - lengthSoFar;
            }

            if(lengthSoFar + length > measure - 0.01){
                tone = putInScale(tone, removeChordFromScale(effectiveScale,
                        progression.peakAtNextChord().getPitches()));
            }
            trumpet.addNote(new Note(lengthSoFar - overrunBeats, length, tone,
                    WifiConnectingSingleton.getInstance().isWifi() ? 75 : 100, "trumpet"));
            //if(0 == lengthSoFar) trumpet.addNote(new Note(lengthSoFar - overrunBeats, length, tone+7, 120d));

            lengthsPosition = lengthsPosition + 1;
            if (lengthsPosition >= currentLengths.size()){
                lengthsPosition = 0;
                if(probabilityFulfilled(0.5)){
                    currentSteps = steps1;
                    currentLengths = lengths1;
                } else {
                    currentSteps = steps2;
                    currentLengths = lengths2;
                }
            }

            lastTone = tone;
            lengthSoFar = lengthSoFar + length;
        }

        overrunBeats = lengthSoFar - (double)measure;
        if(Math.abs(overrunBeats - (double)Math.round(overrunBeats)) < 0.01) {
            overrunBeats = Math.round(overrunBeats);
        }
        Log.d("granglatrumpet", Double.toString(overrunBeats));
        return trumpet;
    }

}
