package com.boardgame.miljac.grangla.music;

import android.util.Log;

import org.billthefarmer.mididriver.MidiConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.boardgame.miljac.grangla.music.MusicHelpers.getRandomElement;
import static com.boardgame.miljac.grangla.music.MusicHelpers.probabilityFulfilled;
import static com.boardgame.miljac.grangla.music.MusicHelpers.randomAround;
import static com.boardgame.miljac.grangla.music.MusicHelpers.resetInstruments;

public class MidiGenerator {
    ChordProgression chordProgression = new ChordProgression();
    PianoGenerator pianoGenerator = new PianoGenerator(chordProgression);
    BassGenerator bassGenerator = new BassGenerator(chordProgression);
    TrumpetGenerator trumpetGenerator = new TrumpetGenerator(chordProgression);
    DrumGenerator drumsGenerator = new DrumGenerator();
    Tune pianoTune;
    Tune bassTune;
    Tune drumsTune;
    Tune trumpetTune;
    List<MidiMessageWithStartBeat> midiList = new ArrayList<>();
    int firstProcessingMeasureBegin;
    int firstProcessingMeasureLen;
    static double rythmPatternFactor = 0.025;
    static List rythmPattern1 = Arrays.asList(randomAround(1d + 2*rythmPatternFactor, rythmPatternFactor),
            randomAround(1.0, rythmPatternFactor),
            randomAround(1.0, rythmPatternFactor),
            randomAround(1.0, rythmPatternFactor),
            randomAround(1.0, rythmPatternFactor));
    static List rythmPattern2 = Arrays.asList(randomAround(1d + 2*rythmPatternFactor, rythmPatternFactor),
            randomAround(1.0, rythmPatternFactor),
            randomAround(1.0, rythmPatternFactor),
            randomAround(1.0, rythmPatternFactor),
            randomAround(1.0, rythmPatternFactor));

    static double trumpetPatternFactor = 0.2;
    static List trumpetPattern1 = Arrays.asList(randomAround(1.0, trumpetPatternFactor),
            randomAround(1.0, trumpetPatternFactor),
            randomAround(1.0, trumpetPatternFactor),
            randomAround(1.0, trumpetPatternFactor),
            randomAround(1.0, trumpetPatternFactor));
    static List trumpetPattern2 = Arrays.asList(randomAround(1.0, trumpetPatternFactor),
            randomAround(1.0, trumpetPatternFactor),
            randomAround(1.0, trumpetPatternFactor),
            randomAround(1.0, trumpetPatternFactor),
            randomAround(1.0, trumpetPatternFactor));
    static List trumpetPattern3 = Arrays.asList(randomAround(1.0, trumpetPatternFactor),
            randomAround(1.0, trumpetPatternFactor),
            randomAround(1.0, trumpetPatternFactor),
            randomAround(1.0, trumpetPatternFactor),
            randomAround(1.0, trumpetPatternFactor));


    public MidiGenerator() {
        resetInstruments();
        pianoTune = pianoGenerator.generateNextMeasure(3);
        pianoTune.appendTune(pianoGenerator.generateNextMeasure(3));
        pianoTune.appendTune(pianoGenerator.generateNextMeasure(3));
        //pianoTune.appendTune(pianoGenerator.generateNextMeasure(3));
        MusicProcessor.processPiano(pianoTune, 0, 0, 3, rythmPattern1);
        MusicProcessor.processPiano(pianoTune, 0, 3, 6, rythmPattern2);
        bassTune = bassGenerator.generateNextMeasure(3);
        bassTune.appendTune(bassGenerator.generateNextMeasure(3));
        bassTune.appendTune(bassGenerator.generateNextMeasure(3));
        //bassTune.appendTune(bassGenerator.generateNextMeasure(3));
        MusicProcessor.processBass(bassTune, 0, 0, 3, rythmPattern1);
        MusicProcessor.processBass(bassTune, 0, 3, 6, rythmPattern2);
        drumsTune = drumsGenerator.generateNextMeasure(3);
        drumsTune.appendTune(drumsGenerator.generateNextMeasure(3));
        drumsTune.appendTune(drumsGenerator.generateNextMeasure(3));
        MusicProcessor.processDrums(drumsTune, 0, 0, 3, rythmPattern1);
        MusicProcessor.processDrums(drumsTune, 0, 3, 6, rythmPattern2);
        trumpetTune = trumpetGenerator.generateNextMeasure(3);
        trumpetTune.appendTune(trumpetGenerator.generateNextMeasure(3));
        trumpetTune.appendTune(trumpetGenerator.generateNextMeasure(3));
        //MusicProcessor.processTrumpet(trumpetTune, 0, 0, 3, trumpetPattern1, chordProgression);
        //MusicProcessor.processTrumpet(trumpetTune, 0, 3, 6, trumpetPattern2, chordProgression);

        firstProcessingMeasureBegin = 6;
        firstProcessingMeasureLen = 3;

    }

    private void addMidiFirstBeatToMidiList(Tune tune, byte channel, boolean drums){
        tune.getNotes().stream()
                .filter(n -> n.getStartBeat() < 1)
                .forEach(n -> {
                    byte[] messageOn = new byte[] {
                            (byte) (MidiConstants.NOTE_ON | channel), // channel 1
                            (byte) (n.getPitch() + (!drums? MusicGlobals.jump:0)),
                            (byte) (isNoteToBePlayed(n)? n.getVelocity() : 0)};
                    midiList.add(new MidiMessageWithStartBeat(messageOn, n.getStartBeat()));
                    Log.d("grangla_calc", MusicHelpers.bytesToHex(messageOn) + " " + n.getStartBeat());

                    byte[] messageOff = new byte[] {
                            (byte) (MidiConstants.NOTE_OFF | channel), // channel 1
                            (byte) (n.getPitch() + (!drums? MusicGlobals.jump:0)),
                            (byte) n.getVelocity()};
                    midiList.add(new MidiMessageWithStartBeat(messageOff, n.getStartBeat() + n.getLengthBeat()));

                    double pitchShift = n.getBasePitchShift() * 31 + 64;

                    if(n.getLinearBend() != 0 ||
                            n.getSinBend() != 0 ||
                            n.getCosBend() != 0 ||
                            n.getMultipleBendFreq() != 0) {

                        for (int i = 0; i < 50; i++) {
                            double pitchBeat = (n.getLengthBeat() / 50 * i + n.getStartBeat());
                            pitchShift = n.getBasePitchShift() * 31 + 64;

                            /*if (n.getLinearBend() != 0) {
                                pitchShift = pitchShift + (((double)i - 25d) * n.getLinearBendAmp() / 25d * 31d);
                            }*/
                            if (n.getSinBend() != 0) {
                                pitchShift = pitchShift + Math.sin((double)i / 50d * Math.PI) * n.getSinBendAmp() * 31d;
                            }
                            if (n.getCosBend() != 0) {
                                pitchShift = pitchShift + Math.cos((double)i / 50 * Math.PI) * n.getCosBendAmp() * 31;
                            }
                            if (n.getMultipleBendFreq() != 0) {
                                pitchShift = pitchShift + Math.cos((double)i / 50 * Math.PI * n.getMultipleBendFreq()) *
                                        n.getMultipleBendAmpSemitones() * 31;
                            }
                            /*if (n.getMultipleOtherBendFreq() != 0) {
                                pitchShift = pitchShift + Math.sin((double)i / 50 * Math.PI * n.getMultipleOtherBendFreq()) *
                                        n.getMultipleBendAmpSemitones() * 31;
                            }*/


                            if (pitchShift > 127) pitchShift = 127;
                            if (pitchShift < 0) pitchShift = 0;

                            byte pitchMSB = (byte) Math.floor(pitchShift);
                            byte pitchLSB = (byte) Math.floor((pitchShift - pitchMSB) * 127);
                            byte[] messagePitch = new byte[]{
                                    (byte) (MidiConstants.PITCH_BEND | channel),
                                    (byte) pitchLSB,
                                    (byte) pitchMSB};
                            midiList.add(new MidiMessageWithStartBeat(messagePitch, pitchBeat));
                            if (n.getSinBend() != 0) Log.d("grangla_pitch", "\tpitch: " + pitchShift + " \tMSB: " + pitchMSB + " \tLSB: " + pitchLSB);
                        }


                    } else {
                        byte pitchMSB = (byte) Math.floor(pitchShift);
                        byte pitchLSB = (byte) Math.floor((pitchShift - pitchMSB) * 127);
                        byte[] messagePitch = new byte[]{
                                (byte) (MidiConstants.PITCH_BEND | channel),
                                (byte) pitchLSB,
                                (byte) pitchMSB};
                        midiList.add(new MidiMessageWithStartBeat(messagePitch, n.getStartBeat()));
                        Log.d("grangla_pitch", "\tpitch: " + pitchShift + " \tMSB: " + pitchMSB + " \tLSB: " + pitchLSB);
                    }
                });



        tune.getNotes().removeIf(n -> n.getStartBeat() < 1);
        tune.getNotes().stream()
                .forEach(n -> {
                    n.setStartBeat(n.getStartBeat() - 1);
                });
        tune.setTuneEndBeats(tune.getTuneEndBeats() - 1);

    }

    //generate midi messages for all notes with start in the first beat
    private void generateMidiFirstBeat(int measure, double processorFactor){
        addMidiFirstBeatToMidiList(pianoTune, (byte) 1, false);
        addMidiFirstBeatToMidiList(bassTune, (byte) 0, false);
        addMidiFirstBeatToMidiList(trumpetTune, (byte) 2, false);
        addMidiFirstBeatToMidiList(drumsTune, (byte) 9, true);

        Collections.sort(midiList);

        if(drumsTune.getTuneEndBeats() < 12){
            double processingBegin = drumsTune.getTuneEndBeats() - firstProcessingMeasureLen;

            chordProgression.getNextChord();
            pianoTune.appendTune(pianoGenerator.generateNextMeasure(measure));
            bassTune.appendTune(bassGenerator.generateNextMeasure(measure));
            trumpetTune.appendTune(trumpetGenerator.generateNextMeasure(measure));
            drumsTune.appendTune(drumsGenerator.generateNextMeasure(measure));

            //process(pianoTune, firstProcessingMeasureBegin, lastProcessingMeasureBegin);
            List rythmPattern = probabilityFulfilled(0.5) ? rythmPattern1 : rythmPattern2;
            List trumpetPattern = (List)getRandomElement(Arrays.asList(trumpetPattern1, trumpetPattern2, trumpetPattern3));
            MusicProcessor.processPiano(pianoTune, processorFactor, processingBegin, processingBegin + firstProcessingMeasureLen, rythmPattern);
            MusicProcessor.processDrums(drumsTune, processorFactor, processingBegin, processingBegin + firstProcessingMeasureLen, rythmPattern);
            MusicProcessor.processBass(bassTune, processorFactor, processingBegin, processingBegin + firstProcessingMeasureLen, rythmPattern);
            MusicProcessor.processTrumpet(trumpetTune, processorFactor, processingBegin, processingBegin + firstProcessingMeasureLen, trumpetPattern, chordProgression);

            firstProcessingMeasureBegin += firstProcessingMeasureLen;
            firstProcessingMeasureLen = measure;
        }
    }

    public List<MidiMessageWithStartBeat> getMidiFirstBeat(int measure, double processorFactor){
        generateMidiFirstBeat(measure, processorFactor);
        List<MidiMessageWithStartBeat> firstBeatMidiList = midiList.stream()
                                                        .filter(m -> m.getBeat() < 1)
                                                        .collect(Collectors.toList());
        midiList.removeIf(m -> m.getBeat() < 1);
        firstProcessingMeasureBegin--;

        midiList.stream()
                .forEach(m -> m.setBeat(m.getBeat() - 1));

        Log.d("grangla_firstBeatMidiList", "list:");
        firstBeatMidiList.stream()
                .forEach(m -> Log.d("grangla_firstBeatMidiList", "beat: " + m.getBeat() + "\t msg: " + MusicHelpers.bytesToHex(m.getMessage())));

        return firstBeatMidiList;
    }


    private boolean isNoteToBePlayed(Note note){
        if(MusicGlobals.trumpetOn && note.getLabel().equals("trumpet")) return true;
        if(MusicGlobals.pianoHighOn && note.getLabel().equals("pianoHigh")) return true;
        if(MusicGlobals.pianoLowOn && note.getLabel().equals("pianoLow")) return true;
        if(MusicGlobals.bassOn && note.getLabel().equals("bass")) return true;
        if(MusicGlobals.drumsMainOn && note.getLabel().equals("drumsMain")) return true;
        if(MusicGlobals.drumsSimpleOn && note.getLabel().equals("drumsSimple")) return true;

        return false;
    }

}
