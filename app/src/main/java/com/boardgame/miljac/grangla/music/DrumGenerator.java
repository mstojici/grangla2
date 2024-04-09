package com.boardgame.miljac.grangla.music;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.boardgame.miljac.grangla.music.MusicHelpers.getRandomElement;
import static com.boardgame.miljac.grangla.music.MusicHelpers.probabilityFulfilled;


public class DrumGenerator {
    List<Integer> pattern1 = generateTomPattern();


    private List<Integer> generateTomPattern(){
        List<Integer> pattern = new ArrayList<>();
        for (int x=0; x<10; x++){
            if((x % 2) == 0){
                pattern.add((int)getRandomElement(Arrays.asList(0, 0, 0, 0,
                        0,0,0,0,
                        0,0,0,0,
                        1, 2, 3, 4)));
            } else {
                pattern.add((int)getRandomElement(Arrays.asList(0,0,0,0,
                        0,0,0,0,
                        0,0,0,0,
                        0,0,0,0,
                        0,0,0,0,
                        1, 2, 3, 4)));
            }

        }
        return pattern;
    }


    public Tune generateNextMeasure(int measure){
        List<Integer> pattern = pattern1;//probabilityFulfilled(0.5) ? pattern1 : pattern2;

        Tune drums = new Tune();
        drums.addNote(new Note(0, 1/2, 36, 85, "drumsMain"));

        for(int x=0; x<(2*measure); x++){
            if((x%2)==0) {
                if (probabilityFulfilled(0.5)) {
                    drums.addNote(new Note(x/2, 1, (probabilityFulfilled(0.5) ? 46 : 42), 85, "drumsSimple"));
                } else {
                    drums.addNote(new Note(x/2, 3d / 4, (probabilityFulfilled(0.5) ? 46 : 42), 85, "drumsSimple"));
                    drums.addNote(new Note(x/2 + 3d / 4, 1d / 4, (probabilityFulfilled(0.5) ? 46 : 42), 85, "drumsSimple"));
                }
            }

            if(pattern.get(x) == 1){
                drums.addNote(new Note((double)x/2, 1/2, 45, 85, "drumsMain"));
            }
            if(pattern.get(x) == 2){
                drums.addNote(new Note((double)x/2, 1/2, 47, 85, "drumsMain"));
            }
            if(pattern.get(x) == 3){
                drums.addNote(new Note((double)x/2, 1/2, 48, 85, "drumsMain"));
            }
            if(pattern.get(x) == 4){
                drums.addNote(new Note((double)x/2, 1/2, 50, 85, "drumsMain"));
            }

        }

        return drums;
    }
}
