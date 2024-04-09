package com.boardgame.miljac.grangla.music;

import android.util.Log;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MusicHelpers {

    static public int getRandomInRange(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }
    static public double getRandomInRange(double min, double max) {
        return (Math.random() * (max - min)) + min;
    }

    static public double randomUp(double center, double width) {
        double rnd = Math.random();
        rnd = rnd * rnd;
        return center + width * rnd;
    }

    static public double randomBelow(double center, double width) {
        double rnd = Math.random();
        rnd = rnd * rnd;
        return center - width * rnd;
    }


    static public double randomAround(double center, double width) {
        double rnd2 = Math.random();
        if (rnd2 > 0.5) {
            return randomUp(center, width);
        } else {
            return randomBelow(center, width);
        }
    }

    public static Object getRandomElement(List<Object> list)
    {
        Random rand = new Random();
        Log.d("grangla_cc", Integer.toString(list.size()));
        return list.get(rand.nextInt(list.size()));

    }

    public static boolean probabilityFulfilled(double probability) {
        if (Math.random() <= probability) {
            //Log.d("grangla_processorfactor", "true");
            return true;
        } else {
            //Log.d("grangla_processorfactor", "false");
            return false;
        }
    }

    public static double currentValue(double begin, double end, double factor){
        double res = begin + (end - begin) * factor;
        //Log.d("grangla_processorfactor", "begin: " + begin + " end: " + end + " factor: " + factor + "    result: " + res);
        return begin + (end - begin) * factor;
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }


    public static List<Integer> normalizeScaleList(List<Integer> scale){
        return scale.stream().map(tone -> tone % 12).collect(Collectors.toList());
    }

    public static int nextInScale(int pitch, List<Integer> scale){
        int ret;
        int scaleNo = (pitch + 1) / 12;
        int normalisedPitch = (pitch + 1) % 12;
        if(normalizeScaleList(scale).contains(normalisedPitch)){
            ret = normalisedPitch + scaleNo * 12;
        } else {
            ret = nextInScale(normalisedPitch, scale) + scaleNo * 12;
        }

        if(ret < pitch){
            return ret + 12;
        } else {
            return ret;
        }
    }

    public static int previousInScale(int pitch, List<Integer> scale){
        int ret;
        int scaleNo = (pitch + 12 - 1) / 12;
        int normalisedPitch = (pitch + 12 - 1) % 12;
        if(normalizeScaleList(scale).contains(normalisedPitch)){
            ret = normalisedPitch + scaleNo * 12;
        } else {
            ret = previousInScale(normalisedPitch, scale) + scaleNo * 12;
        }

        if(ret < 0){
            ret = ret + 12;
        }

        if(ret > pitch){
            return ret - 12;
        } else {
            return ret;
        }
    }

    public static int jumpUpscale(int pitch, List<Integer> scale, int steps){
        if(steps == 0){
            return pitch;
        } else {
            return jumpUpscale(nextInScale(pitch, scale), scale, steps - 1);
        }
    }

    public static int jumpDownscale(int pitch, List<Integer> scale, int steps){
        if(steps == 0){
            return pitch;
        } else {
            return jumpDownscale(previousInScale(pitch, scale), scale, steps - 1);
        }
    }

    public static List<Integer> integrateChordInScale(List<Integer> scale, List<Integer> chord){
        List<Integer> normalisedScale = normalizeScaleList(scale);
        List<Integer> normalisedChord = normalizeScaleList(chord);

        normalisedChord.stream().forEach(chordTone -> {
            if (!normalisedScale.contains(chordTone)){
                normalisedScale.remove((Object)(chordTone+1));
                normalisedScale.remove((Object)(chordTone-1));
                normalisedScale.add(chordTone);
            }
        });

        Log.d("granglascale", Arrays.toString(normalisedScale.toArray()));

        return normalisedScale;
    }
    public static List<Integer> removeChordFromScale(List<Integer> scale, List<Integer> chord){
        List<Integer> normalisedScale = normalizeScaleList(scale);
        List<Integer> normalisedChord = normalizeScaleList(chord);

        normalisedChord.stream().forEach(chordTone -> {
            if (!normalisedScale.contains(chordTone)){
                normalisedScale.remove((Object)(chordTone+1));
                normalisedScale.remove((Object)(chordTone-1));
            }
        });

        Log.d("granglascale", Arrays.toString(normalisedScale.toArray()));

        return normalisedScale;
    }

    public static int putInScale(int pitch, List<Integer> scale){
        return nextInScale(previousInScale(pitch, scale), scale);
    }

    public static void turnSomeInstrumentOn(){
        if(MusicGlobals.inactiveInstruments.size() == 0){
            if(!MusicGlobals.drumsSimpleOn && !MusicGlobals.drumsMainOn){
                if(probabilityFulfilled(0.5)){
                    MusicGlobals.drumsMainOn = true;
                } else {
                    MusicGlobals.drumsSimpleOn = true;
                }
            } else {
                MusicGlobals.drumsMainOn = true;
                MusicGlobals.drumsSimpleOn = true;
            }
            return;
        }

        int rand = (int) ((Math.random() * MusicGlobals.inactiveInstruments.size()));
        if(MusicGlobals.inactiveInstruments.size() == 4)
            rand = probabilityFulfilled(0.3) ? 3 :
                    probabilityFulfilled(0.5) ? 0 : 1;

        while(MusicGlobals.inactiveInstruments.size() == 3 && MusicGlobals.inactiveInstruments.get(rand) == 2)
            rand = (int) ((Math.random() * MusicGlobals.inactiveInstruments.size()));

        switch (MusicGlobals.inactiveInstruments.remove(rand)){
            case 0:
                MusicGlobals.pianoLowOn = true;
                break;
            case 1:
                MusicGlobals.bassOn = true;
                break;
            case 2:
                MusicGlobals.trumpetOn = true;
                break;
            case 3:
                MusicGlobals.pianoHighOn = true;
                break;

        }
    }

    public static void resetInstruments(){
        MusicGlobals.bassOn = false;
        MusicGlobals.trumpetOn = false;
        MusicGlobals.pianoHighOn = false;
        MusicGlobals.pianoLowOn = false;
        MusicGlobals.drumsSimpleOn = false;
        MusicGlobals.drumsMainOn = false;

        MusicGlobals.inactiveInstruments = IntStream.range(0, 4).boxed().collect(Collectors.toList());

        turnSomeInstrumentOn();
        turnSomeInstrumentOn();
    }

    public static void onlyTrumpetEnd(){
        MusicGlobals.bassOn = false;
        MusicGlobals.trumpetOn = true;
        MusicGlobals.pianoHighOn = false;
        MusicGlobals.pianoLowOn = false;
        MusicGlobals.drumsSimpleOn = false;
        MusicGlobals.drumsMainOn = false;
    }

    public static void oneMoreInTheEnd(){
        switch(getRandomInRange(0, 2)){
            case 0:
                MusicGlobals.pianoHighOn = true;
                break;
            case 1:
                MusicGlobals.pianoLowOn = true;
                break;
            case 2:
                MusicGlobals.bassOn = true;
                MusicGlobals.drumsSimpleOn = true;
                return;
        }

        switch(getRandomInRange(0, 1)){
            case 0:
                MusicGlobals.bassOn = true;
                break;
            case 1:
                MusicGlobals.drumsSimpleOn = true;
                break;
        }
    }
}
