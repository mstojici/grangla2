package com.boardgame.miljac.grangla.music;

import android.util.Log;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static com.boardgame.miljac.grangla.music.MusicHelpers.*;

public class PianoGenerator {
    private ChordProgression progression;
    double[][] pattern31high = generatePianoPattern3BeatsHigh();
    double[][] pattern32high = generatePianoPattern3BeatsHigh();
    double[][] pattern31low = generatePianoPattern3BeatsLow();
    double[][] pattern32low = generatePianoPattern3BeatsLow();
    double[][] pattern41high = generatePianoPattern4BeatsHigh();
    double[][] pattern42high = generatePianoPattern4BeatsHigh();
    double[][] pattern41low = generatePianoPattern4BeatsLow();
    double[][] pattern42low = generatePianoPattern4BeatsLow();
    double[][] pattern51high = generatePianoPattern5BeatsHigh();
    double[][] pattern52high = generatePianoPattern5BeatsHigh();
    double[][] pattern51low = generatePianoPattern5BeatsLow();
    double[][] pattern52low = generatePianoPattern5BeatsLow();
    double[][] pattern21high = generatePianoPattern2BeatsHigh();
    double[][] pattern22high = generatePianoPattern2BeatsHigh();
    double[][] pattern21low = generatePianoPattern2BeatsLow();
    double[][] pattern22low = generatePianoPattern2BeatsLow();

    public PianoGenerator(ChordProgression progression){
        this.progression = progression;
    }

    private double[][] generatePianoPattern3BeatsHigh(){
        int first_up = 85;
        int first_range = 200;
        int second_up = 80;
        int second_range = 250;
        int third_up = 80;
        int third_range = 250;
        int limit = 65;

         double[][] pattern = {{randomBelow(first_up, first_range), randomBelow(second_up, second_range), randomBelow(third_up, third_range)},
            {randomBelow(first_up, first_range), randomBelow(second_up, second_range), randomBelow(third_up, third_range)},
            {randomBelow(first_up, first_range), randomBelow(second_up, second_range), randomBelow(third_up, third_range)},
            {randomBelow(first_up, first_range), randomBelow(second_up, second_range), randomBelow(third_up, third_range)}};

            IntStream.range(0, 4).forEachOrdered(x ->
                IntStream.range(0, 3).forEachOrdered(y -> {
                    if (pattern[x][y] < limit) {
                        pattern[x][y] = 0;
                    }
                })
            );

            return pattern;
    }

    private double[][] generatePianoPattern3BeatsLow(){
        int first_up = 70;
        int first_range = 40;
        int second_up = 50;
        int second_range = 50;
        int third_up = 50;
        int third_range = 50;
        int limit = 40;

    double[][] pattern = {{randomBelow(first_up, first_range), randomBelow(second_up, second_range), randomBelow(third_up, third_range)},
            {randomBelow(first_up, first_range), randomBelow(second_up, second_range), randomBelow(third_up, third_range)},
            {randomBelow(first_up, first_range), randomBelow(second_up, second_range), randomBelow(third_up, third_range)},
            {randomBelow(first_up, first_range), randomBelow(second_up, second_range), randomBelow(third_up, third_range)}};

    IntStream.range(0, 4).forEachOrdered(x ->
                IntStream.range(0, 3).forEachOrdered(y -> {
                    if (pattern[x][y] < limit) {
                        pattern[x][y] = 0;
                    }
                })
        );

        return pattern;
    }

    private double[][] generatePianoPattern4BeatsHigh(){
        int first_up = 85;
        int first_range = 200;
        int second_up = 75;
        int second_range = 250;
        int third_up = 80;
        int third_range = 250;
        int fourth_up = 75;
        int fourth_range = 250;
        int limit = 65;

        double[][] pattern = {{randomBelow(first_up, first_range), randomBelow(second_up, second_range), randomBelow(third_up, third_range), randomBelow(fourth_up, fourth_range)},
            {randomBelow(first_up, first_range), randomBelow(second_up, second_range), randomBelow(third_up, third_range), randomBelow(fourth_up, fourth_range)},
            {randomBelow(first_up, first_range), randomBelow(second_up, second_range), randomBelow(third_up, third_range), randomBelow(fourth_up, fourth_range)},
            {randomBelow(first_up, first_range), randomBelow(second_up, second_range), randomBelow(third_up, third_range), randomBelow(fourth_up, fourth_range)}};

        IntStream.range(0, 4).forEachOrdered(x ->
                IntStream.range(0, 4).forEachOrdered(y -> {
                    if (pattern[x][y] < limit) {
                        pattern[x][y] = 0;
                    }
                })
        );

        return pattern;
    }

    private double[][] generatePianoPattern4BeatsLow(){
        int first_up = 70;
        int first_range = 40;
        int second_up = 45;
        int second_range = 50;
        int third_up = 50;
        int third_range = 50;
        int fourth_up = 45;
        int fourth_range = 50;
        int limit = 40;

        double[][] pattern = {{randomBelow(first_up, first_range), randomBelow(second_up, second_range), randomBelow(third_up, third_range), randomBelow(fourth_up, fourth_range)},
            {randomBelow(first_up, first_range), randomBelow(second_up, second_range), randomBelow(third_up, third_range), randomBelow(fourth_up, fourth_range)},
            {randomBelow(first_up, first_range), randomBelow(second_up, second_range), randomBelow(third_up, third_range), randomBelow(fourth_up, fourth_range)},
            {randomBelow(first_up, first_range), randomBelow(second_up, second_range), randomBelow(third_up, third_range), randomBelow(fourth_up, fourth_range)}};


            IntStream.range(0, 4).forEachOrdered(x ->
                    IntStream.range(0, 4).forEachOrdered(y -> {
                if (pattern[x][y] < limit) {
                    pattern[x][y] = 0;
                }
            }));

            return pattern;
        }

    private double[][] generatePianoPattern5BeatsHigh(){
        int first_up = 85;
        int first_range = 200;
        int second_up = 77;
        int second_range = 250;
        int third_up = 77;
        int third_range = 250;
        int fourth_up = 77;
        int fourth_range = 250;
        int fifth_up = 77;
        int fifth_range = 250;
        int limit = 65;

        double[][] pattern = {{randomBelow(first_up, first_range), randomBelow(second_up, second_range), randomBelow(third_up, third_range), randomBelow(fourth_up, fourth_range), randomBelow(fifth_up, fifth_range)},
             {randomBelow(first_up, first_range), randomBelow(second_up, second_range), randomBelow(third_up, third_range), randomBelow(fourth_up, fourth_range), randomBelow(fifth_up, fifth_range)},
            {randomBelow(first_up, first_range), randomBelow(second_up, second_range), randomBelow(third_up, third_range), randomBelow(fourth_up, fourth_range), randomBelow(fifth_up, fifth_range)},
                {randomBelow(first_up, first_range), randomBelow(second_up, second_range), randomBelow(third_up, third_range), randomBelow(fourth_up, fourth_range), randomBelow(fifth_up, fifth_range)}};

            IntStream.range(0, 4).forEachOrdered(x ->
                IntStream.range(0, 5).forEachOrdered(y -> {
                    if (pattern[x][y] < limit) {
                        pattern[x][y] = 0;
                    }
                }));

        return pattern;
    }

    private double[][] generatePianoPattern5BeatsLow(){
        int first_up = 70;
        int first_range = 40;
        int second_up = 45;
        int second_range = 50;
        int third_up = 45;
        int third_range = 50;
        int fourth_up = 45;
        int fourth_range = 50;
        int fifth_up = 45;
        int fifth_range = 50;
        int limit = 40;

        double[][] pattern = {{randomBelow(first_up, first_range), randomBelow(second_up, second_range), randomBelow(third_up, third_range), randomBelow(fourth_up, fourth_range), randomBelow(fifth_up, fifth_range)},
                {randomBelow(first_up, first_range), randomBelow(second_up, second_range), randomBelow(third_up, third_range), randomBelow(fourth_up, fourth_range), randomBelow(fifth_up, fifth_range)},
                {randomBelow(first_up, first_range), randomBelow(second_up, second_range), randomBelow(third_up, third_range), randomBelow(fourth_up, fourth_range), randomBelow(fifth_up, fifth_range)},
                {randomBelow(first_up, first_range), randomBelow(second_up, second_range), randomBelow(third_up, third_range), randomBelow(fourth_up, fourth_range), randomBelow(fifth_up, fifth_range)}};

        IntStream.range(0, 4).forEachOrdered(x ->
                IntStream.range(0, 5).forEachOrdered(y -> {
                    if (pattern[x][y] < limit) {
                        pattern[x][y] = 0;
                    }
                }));

        return pattern;
    }

    private double[][] generatePianoPattern2BeatsHigh(){
    int first_up = 85;
    int first_range = 200;
    int second_up = 80;
    int second_range = 200;

    int limit = 65;

        double[][] pattern = {{randomBelow(first_up, first_range), randomBelow(second_up, second_range)},
        {randomBelow(first_up, first_range), randomBelow(second_up, second_range)},
        {randomBelow(first_up, first_range), randomBelow(second_up, second_range)},
        {randomBelow(first_up, first_range), randomBelow(second_up, second_range)}};

        IntStream.range(0, 4).forEachOrdered(x ->
                IntStream.range(0, 2).forEachOrdered(y -> {
                    if (pattern[x][y] < limit) {
                        pattern[x][y] = 0;
                    }
                }));

        return pattern;
    }

    private double[][] generatePianoPattern2BeatsLow(){
    int first_up = 70;
    int first_range = 40;
    int second_up = 50;
    int second_range = 50;
    int limit = 40;

        double[][] pattern = {{randomBelow(first_up, first_range), randomBelow(second_up, second_range)},
                {randomBelow(first_up, first_range), randomBelow(second_up, second_range)},
                {randomBelow(first_up, first_range), randomBelow(second_up, second_range)},
                {randomBelow(first_up, first_range), randomBelow(second_up, second_range)}};

        IntStream.range(0, 4).forEachOrdered(x ->
                IntStream.range(0, 2).forEachOrdered(y -> {
                    if (pattern[x][y] < limit) {
                        pattern[x][y] = 0;
                    }
                }));

        return pattern;
    }

    public Tune generateNextMeasure(int measure){
        Chord chord = progression.getCurrentChord();
        Tune nextMeasure = new Tune();
        List<Object> patternsListHigh = new ArrayList<>();
        List<Object> patternsListLow = new ArrayList<>();
        if (measure == 3) {
            patternsListHigh.add(pattern31high);
            patternsListHigh.add(pattern32high);
            patternsListLow.add(pattern31low);
            patternsListLow.add(pattern32low);
        } else if (measure == 4) {
            patternsListHigh.add(pattern41high);
            patternsListHigh.add(pattern42high);
            patternsListLow.add(pattern41low);
            patternsListLow.add(pattern42low);
        }  else if (measure == 5) {
            patternsListHigh.add(pattern51high);
            patternsListHigh.add(pattern52high);
            patternsListLow.add(pattern51low);
            patternsListLow.add(pattern52low);
        } else if (measure == 2) {
            patternsListHigh.add(pattern21high);
            patternsListHigh.add(pattern22high);
            patternsListLow.add(pattern21low);
            patternsListLow.add(pattern22low);
        }

        double[][] patternHigh = (double[][]) getRandomElement(patternsListHigh);

        double[][] patternLow = (double[][]) getRandomElement(patternsListLow);
        int measureLengthBeat = patternHigh[0].length;

        for(int x = 0; x < 4; x = x + 1){
            double count = 1;
            double lengthHigh = 1 - randomAround(0.14, 0.04);
            double lengthLow = 1 - randomAround(0.14, 0.04);

            for(int y = measureLengthBeat -1; y >= 0; y = y - 1){
                double velocityHigh = patternHigh[x][y];
                double startHigh = measureLengthBeat - count + x / randomAround(50, 7.5);
                double velocityLow = patternLow[x][y];
                double startLow = measureLengthBeat - count + x / randomAround(50, 7.5);

                if(velocityHigh>0) {
                    nextMeasure.addNote(new Note(startHigh, lengthHigh, chord.getPitches().get(x) + 24, velocityHigh, "pianoHigh"));

                    DecimalFormat df = new DecimalFormat("0.00");
                    Log.d("grangla_note", "pitch " + String.format("%02d", chord.getPitches().get(x)) +
                            "\t beat " + df.format(startHigh) +
                            "\t length " + df.format(lengthHigh) +
                            "\t velocity " + df.format(velocityHigh));

                    lengthHigh = 1 - randomAround(0.14, 0.04);
                } else {
                    lengthHigh = lengthHigh + 1;
                }

                if(velocityLow>0) {
                    nextMeasure.addNote(new Note(startLow, lengthLow, chord.getPitches().get(x), velocityLow, "pianoLow"));

                    DecimalFormat df = new DecimalFormat("0.00");
                    Log.d("grangla_note", "pitch " + String.format("%02d", chord.getPitches().get(x)) +
                            "\t beat " + df.format(startLow) +
                            "\t length " + df.format(lengthLow) +
                            "\t velocity " + df.format(velocityLow));

                    lengthLow = 1 - randomAround(0.14, 0.04);
                } else {
                    lengthLow = lengthLow + 1;
                }

                count = count + 1;
            }
        }
        nextMeasure.setTuneEndBeats(measureLengthBeat);
        return nextMeasure;
    }
}
