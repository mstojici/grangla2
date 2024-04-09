package com.boardgame.miljac.grangla.music;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.boardgame.miljac.grangla.music.MusicHelpers.getRandomInRange;

public class MusicGlobals {
    public static int jump = getRandomInRange(-6,6);
    public static boolean bassOn = false;
    public static boolean trumpetOn = false;
    public static boolean pianoHighOn = false;
    public static boolean pianoLowOn = false;
    public static boolean drumsMainOn = false;
    public static boolean drumsSimpleOn = false;
    public static List<Integer> inactiveInstruments = IntStream.range(0, 4).boxed().collect(Collectors.toList());
}
