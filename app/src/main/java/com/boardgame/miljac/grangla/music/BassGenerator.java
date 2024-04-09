package com.boardgame.miljac.grangla.music;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static com.boardgame.miljac.grangla.music.MusicHelpers.*;


public class BassGenerator {
    private ChordProgression progression;
    List<Integer> pattern1 = generateBassPattern();
    List<Integer> pattern2 = generateBassPattern();

    public BassGenerator(ChordProgression progression){
        this.progression = progression;
    }

    private List<Integer> generateBassPattern(){
        List<Integer> pattern = new ArrayList<>();
        for (int x=0; x<5; x++){
            pattern.add((int)getRandomElement(Arrays.asList(0, 1, 2, 3)));
        }
        return pattern;
    }

    private Tune generateBassBar(Chord chord, int measure, List<Integer> pattern){
        Tune bass = new Tune();

        for (int x=0; x<measure; x++){
            bass.addNote(new Note(x, 1, chord.getPitches().get(pattern.get(x)) - 12, 100, "bass"));
        }

        return bass;
    }

    public Tune generateNextMeasure(int measure){
        Chord chord = progression.getCurrentChord();

        return generateBassBar(chord, measure, probabilityFulfilled(0.5) ? pattern1 : pattern2);
    }
}
