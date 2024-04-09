package com.boardgame.miljac.grangla.music;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Chord {
    private String name;
    private List<Integer> pitches;

    private static int LOW_LIMIT = 43;
    private static int HIGH_LIMIT = 57;

    private static List<Integer> Dm_CHORD = new ArrayList<Integer>(Arrays.asList(50, 53, 57, 60));
    private static List<Integer> Dmb5_CHORD = new ArrayList<Integer>(Arrays.asList(50, 53, 56, 60));
    private static List<Integer> G7_CHORD = new ArrayList<Integer>(Arrays.asList(43, 47, 50, 53));
    private static List<Integer> C_CHORD = new ArrayList<Integer>(Arrays.asList(48, 52, 55, 59));
    private static List<Integer> Cdim7_CHORD = new ArrayList<Integer>(Arrays.asList(48, 51, 54, 57));
    private static List<Integer> Cm7Bb_CHORD =  new ArrayList<Integer>(Arrays.asList(46, 51, 55, 60));

    private static List<Integer> Em_CHORD = Dm_CHORD.stream().map(i -> i+2).collect(Collectors.toList());
    private static List<Integer> F_CHORD = C_CHORD.stream().map(i -> i+5).collect(Collectors.toList());
    private static List<Integer> G_CHORD = F_CHORD.stream().map(i -> i+2).collect(Collectors.toList());
    private static List<Integer> Ab_CHORD = G_CHORD.stream().map(i -> i+1).collect(Collectors.toList());
    private static List<Integer> Eb_CHORD = C_CHORD.stream().map(i -> i+3).collect(Collectors.toList());
    private static List<Integer> Hb_CHORD = C_CHORD.stream().map(i -> i+10).collect(Collectors.toList());
    private static List<Integer> Am_CHORD = Dm_CHORD.stream().map(i -> i+7).collect(Collectors.toList());
    private static List<Integer> Gm_CHORD = Dm_CHORD.stream().map(i -> i+5).collect(Collectors.toList());
    private static List<Integer> Cm_CHORD = Dm_CHORD.stream().map(i -> i-2).collect(Collectors.toList());
    private static List<Integer> Fm_CHORD = Dm_CHORD.stream().map(i -> i+3).collect(Collectors.toList());

    private static List<Integer> Abm_CHORD = Am_CHORD.stream().map(i -> i-1).collect(Collectors.toList());
    private static List<Integer> Ebm_CHORD = Fm_CHORD.stream().map(i -> i-2).collect(Collectors.toList());
    private static List<Integer> Hbm_CHORD = Am_CHORD.stream().map(i -> i+1).collect(Collectors.toList());

    private static List<Integer> A7_CHORD = G7_CHORD.stream().map(i -> i+2).collect(Collectors.toList());
    private static List<Integer> Ab7_CHORD = G7_CHORD.stream().map(i -> i+1).collect(Collectors.toList());
    private static List<Integer> D7_CHORD = G7_CHORD.stream().map(i -> i+7).collect(Collectors.toList());
    private static List<Integer> C7_CHORD = G7_CHORD.stream().map(i -> i+5).collect(Collectors.toList());
    private static List<Integer> F7_CHORD = G7_CHORD.stream().map(i -> i-2).collect(Collectors.toList());
    private static List<Integer> CisDim7_CHORD = Cdim7_CHORD.stream().map(i -> i+1).collect(Collectors.toList());
    private static List<Integer> DisDim7_CHORD = Cdim7_CHORD.stream().map(i -> i+3).collect(Collectors.toList());
    private static List<Integer> DDim7_CHORD = Cdim7_CHORD.stream().map(i -> i+2).collect(Collectors.toList());
    private static List<Integer> Amb5_CHORD = Dmb5_CHORD.stream().map(i -> i+7).collect(Collectors.toList());

    public static Chord Dm = new Chord("Dm", Dm_CHORD);
    public static Chord Dmb5 = new Chord("Dmb5", Dmb5_CHORD);
    public static Chord G7 = new Chord("G7", G7_CHORD);
    public static Chord C = new Chord("C", C_CHORD);
    public static Chord G = new Chord("G", G_CHORD);
    public static Chord Ab = new Chord("Ab", Ab_CHORD);
    public static Chord Eb = new Chord("Eb", Eb_CHORD);
    public static Chord Hb = new Chord("Hb", Hb_CHORD);
    public static Chord Cdim7 = new Chord("Cdim7", Cdim7_CHORD);
    public static Chord Cm7Bb = new Chord("Cm7Bb", Cm7Bb_CHORD);
    public static Chord Em = new Chord("Em", Em_CHORD);
    public static Chord F = new Chord("F", F_CHORD);
    public static Chord Am = new Chord("Am", Am_CHORD);
    public static Chord Gm = new Chord("Gm", Gm_CHORD);
    public static Chord Cm = new Chord("Cm", Cm_CHORD);
    public static Chord Fm = new Chord("Fm", Fm_CHORD);
    public static Chord Abm = new Chord("Abm", Abm_CHORD);
    public static Chord Ebm = new Chord("Ebm", Ebm_CHORD);
    public static Chord Hbm = new Chord("Hbm", Hbm_CHORD);

    public static Chord A7 = new Chord("A7", A7_CHORD);
    public static Chord Ab7 = new Chord("Ab7", Ab7_CHORD);
    public static Chord D7 = new Chord("D7", D7_CHORD);
    public static Chord C7 = new Chord("C7", C7_CHORD);
    public static Chord F7 = new Chord("F7", F7_CHORD);
    public static Chord CisDim7 = new Chord("CisDim7", CisDim7_CHORD);
    public static Chord DisDim7 = new Chord("DisDim7", DisDim7_CHORD);
    public static Chord DDim7 = new Chord("DDim7", DDim7_CHORD);
    public static Chord Amb5 = new Chord("Amb5", Amb5_CHORD);


    private Chord(String name, List<Integer> pitches){
        this.name = name;
        this.pitches = putInLimits(pitches);
    }

    private List<Integer> putInLimits(List<Integer> pitches){
        if(pitches.get(0) > HIGH_LIMIT - MusicGlobals.jump){
            return pitches.stream().map(i -> i - 12).collect(Collectors.toList());
        }
        if(pitches.get(0) < LOW_LIMIT - MusicGlobals.jump){
            return pitches.stream().map(i -> i + 12).collect(Collectors.toList());
        }
        return pitches;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Integer> getPitches() {
        return pitches;
    }

    public void setPitches(List<Integer> pitches) {
        this.pitches = pitches;
    }
}
