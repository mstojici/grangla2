package com.boardgame.miljac.grangla.music;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.boardgame.miljac.grangla.music.MusicHelpers.*;
import com.boardgame.miljac.grangla.music.MusicProcessorTransformation.*;

public class MusicProcessor {



    public static void transformOneMeasure(Tune tune, double start, double end, double probabilityStart, double probabilityEnd, double probabilityAndProcessFactor,  NoteTransformation noteTransformation, List<Object> argList){
        List<Note> notesToAdd = new ArrayList<>();
        List<Note> notesToRemove = new ArrayList<>();
        AtomicInteger notesToJump = new AtomicInteger();

        double probability = currentValue(probabilityStart, probabilityEnd, probabilityAndProcessFactor);

        tune.getNotes().stream()
                .filter(n -> (n.getStartBeat() >= start) && (n.getStartBeat() < end))
                .forEach(n -> {
                    if(probabilityFulfilled(probability) && notesToJump.get() <= 0){
                        TransformationResult result = noteTransformation.transformNote(tune, tune.getNotes().indexOf(n), probabilityAndProcessFactor, argList);
                        notesToJump.set((int) result.getNotesToJump());
                        notesToAdd.addAll((List) result.getNotesToAdd());
                        notesToRemove.addAll((List) result.getNotesToRemove());
                    } else {
                        notesToJump.set(notesToJump.get() - 1);
                    }
                });

        tune.addNotes(notesToAdd);
        tune.removeNotes(notesToRemove);
    }

    public static void transformLastNoteInOneMeasure(Tune tune, double start, double end, double probabilityStart, double probabilityEnd, double probabilityAndProcessFactor,  NoteTransformation noteTransformation, List<Object> argList){
        List<Note> notesToAdd = new ArrayList<>();
        List<Note> notesToRemove = new ArrayList<>();
        AtomicInteger notesToJump = new AtomicInteger();

        double probability = currentValue(probabilityStart, probabilityEnd, probabilityAndProcessFactor);

        tune.getNotes().stream()
                .filter(n -> (Math.abs(n.getStartBeat() + n.getLengthBeat() - end) < 0.1))
                .forEach(n -> {
                    Log.d("transformLastNoteInOneMeasure", "n.getStartBeat():" + n.getStartBeat() + " \t n.getLengthBeat():" + n.getLengthBeat() + " \t end:" + end);
                    Log.d("transformLastNoteInOneMeasure", "probability:b" + probability);
                    if(probabilityFulfilled(probability) && notesToJump.get() <= 0){
                        TransformationResult result = noteTransformation.transformNote(tune, tune.getNotes().indexOf(n), probabilityAndProcessFactor, argList);
                        notesToJump.set((int) result.getNotesToJump());
                        notesToAdd.addAll((List) result.getNotesToAdd());
                        notesToRemove.addAll((List) result.getNotesToRemove());
                    } else {
                        notesToJump.set(notesToJump.get() - 1);
                    }
                });

        tune.addNotes(notesToAdd);
        tune.removeNotes(notesToRemove);
    }
    public static void transformAllButLastBeatInOneMeasure(Tune tune, double start, double end, double probabilityStart, double probabilityEnd, double probabilityAndProcessFactor,  NoteTransformation noteTransformation, List<Object> argList){
        List<Note> notesToAdd = new ArrayList<>();
        List<Note> notesToRemove = new ArrayList<>();
        AtomicInteger notesToJump = new AtomicInteger();

        double probability = currentValue(probabilityStart, probabilityEnd, probabilityAndProcessFactor);

        tune.getNotes().stream()
                .filter(n -> (n.getStartBeat() >= start) &&
                        (Math.abs(n.getStartBeat() + n.getLengthBeat()) < end - 0.9))
                .forEach(n -> {
                    Log.d("transformLastNoteInOneMeasure", "n.getStartBeat():" + n.getStartBeat() + " \t n.getLengthBeat():" + n.getLengthBeat() + " \t end:" + end);
                    Log.d("transformLastNoteInOneMeasure", "probability:b" + probability);
                    if(probabilityFulfilled(probability) && notesToJump.get() <= 0){
                        TransformationResult result = noteTransformation.transformNote(tune, tune.getNotes().indexOf(n), probabilityAndProcessFactor, argList);
                        notesToJump.set((int) result.getNotesToJump());
                        notesToAdd.addAll((List) result.getNotesToAdd());
                        notesToRemove.addAll((List) result.getNotesToRemove());
                    } else {
                        notesToJump.set(notesToJump.get() - 1);
                    }
                });

        tune.addNotes(notesToAdd);
        tune.removeNotes(notesToRemove);
    }

    public static void applyOneRythmPattern(Tune tune, double start, double end, List<Double> weightsPattern){
        double n = end - start;
        double s = weightsPattern.stream().mapToDouble(x -> x).limit((int)n).sum();
        double l = end - start;
        double unit = l/n;

        List<Double> finalWeightsPattern = weightsPattern.stream().map(x -> x/s*n).collect(Collectors.toList());

        tune.getNotes().stream().forEach(note -> {
            int currentIndex = tune.getNotes().indexOf(note);

            if((note.getStartBeat() > start) && (note.getStartBeat() < end)){
                double x = note.getStartBeat() - start;
                int i = (int)Math.floor(x / unit);
                double x2 = 0;

                for(int j=0; j<i; j++){
                    x2 = x2 + unit * finalWeightsPattern.get(j);
                }

                x2 = x2 + finalWeightsPattern.get(i) * (x - (double)i*unit);
                note.setLengthBeat(note.getLengthBeat() + x - x2);
                note.setStartBeat(start + x2);
                note.setVelocity(note.getVelocity() * Math.sqrt(finalWeightsPattern.get(i)));
            }

            if((note.getStartBeat() + note.getLengthBeat() > start) && (note.getStartBeat() + note.getLengthBeat() < end)){
                double x = note.getStartBeat() + note.getLengthBeat() - start;
                int i = (int)Math.floor(x / unit);
                double x2 = 0;

                for(int j=0; j<i; j++){
                    x2 = x2 + unit * finalWeightsPattern.get(j);
                }

                x2 = x2 + finalWeightsPattern.get(i) * (x - (double)i*unit);
                note.setLengthBeat(start + x2 - note.getStartBeat());
                //note.setVelocity(note.getVelocity() * Math.sqrt(weightsPattern.get(i)));
            }

            tune.getNotes().set(currentIndex, note);
        });
    }


    public static void addSnareByRythmPattern(Tune tune, double start, double end, List<Double> weightsPattern){
        double n = end - start;
        for(int x=0; x<n; x++){
            if(weightsPattern.get(x) > 1) {
                tune.addNote(new Note(start + x, 1, 38, 90, "drumsMain"));
            }
        }
    }




    public static void processPiano(Tune piano, double probabilityAndProcessFactor, double start, double end, List<Double> weightsPattern){
        double smallestInTheMiddle = Math.abs((probabilityAndProcessFactor - 0.5) * 2);
        double biggestInTheMiddle = 1 - Math.abs((0.5 - probabilityAndProcessFactor) * 2);
        double invert = 1d - probabilityAndProcessFactor;

        //transformOneMeasure(piano, start, end, 1, 1, probabilityAndProcessFactor, MusicProcessorTransformation.scaleVelocity, Arrays.asList(0.07, 0.07d));

        applyOneRythmPattern(piano, start, end, weightsPattern);

        transformOneMeasure(piano, start, end, 0, 1, biggestInTheMiddle, MusicProcessorTransformation.randomUpVelocity, Arrays.asList(-4d, 6d));
        transformOneMeasure(piano, start, end, 0.1, 0.1, probabilityAndProcessFactor, MusicProcessorTransformation.moveNoteConnectNeighbors, Arrays.asList(0.01d, 0.03d, 5d, 5d));
        transformOneMeasure(piano, start, end, 0.1, 0.1, probabilityAndProcessFactor, MusicProcessorTransformation.moveNoteConnectNeighbors, Arrays.asList(0.01d, 0.03d, 1.5d, 1.5d));

        //transformOneMeasure(piano, start, end, 1, 1, probabilityAndProcessFactor, MusicProcessorTransformation.transposePitch, Arrays.asList((double)Globals.jump));
    }

    public static void processDrums(Tune drums, double probabilityAndProcessFactor, double start, double end, List<Double> weightsPattern){
        double smallestInTheMiddle = Math.abs((probabilityAndProcessFactor - 0.5) * 2);
        double biggestInTheMiddle = 1 - Math.abs((0.5 - probabilityAndProcessFactor) * 2);
        double invert = 1d - probabilityAndProcessFactor;

        List currentRythmPattern = weightsPattern;
        addSnareByRythmPattern(drums, start, end, currentRythmPattern);
        //transformOneMeasure(drums, start, end, 1, 1, probabilityAndProcessFactor, MusicProcessorTransformation.scaleVelocity, Arrays.asList(1.2, 1.2));

        applyOneRythmPattern(drums, start, end, currentRythmPattern);

        transformOneMeasure(drums, start, end, 1, 1, probabilityAndProcessFactor, MusicProcessorTransformation.randomUpVelocity, Arrays.asList(-5d, 5d));
        transformOneMeasure(drums, start, end, 0.2, 0.2, probabilityAndProcessFactor, MusicProcessorTransformation.moveNoteConnectNeighbors, Arrays.asList(0d, 0.013d, 4d, 4d));
        transformOneMeasure(drums, start, end, 0.1, 0.1, probabilityAndProcessFactor, MusicProcessorTransformation.moveNoteConnectNeighbors, Arrays.asList(0.01d, 0.03d, 1.5d, 1.5d));
    }

    public static void processBass(Tune bass, double probabilityAndProcessFactor, double start, double end, List<Double> weightsPattern){
        double smallestInTheMiddle = Math.abs((probabilityAndProcessFactor - 0.5) * 2);
        double biggestInTheMiddle = 1 - Math.abs((0.5 - probabilityAndProcessFactor) * 2);
        double invert = 1d - probabilityAndProcessFactor;

        //transformOneMeasure(bass, start, end, 1, 1, probabilityAndProcessFactor, MusicProcessorTransformation.scaleVelocity, Arrays.asList(0.07, 0.07d));

        applyOneRythmPattern(bass, start, end, weightsPattern);

        transformOneMeasure(bass, start, end, 0.1, 0.1, biggestInTheMiddle, MusicProcessorTransformation.moveNoteConnectNeighbors, Arrays.asList(0.03d, 0.1d, 5d, 5d));
        transformOneMeasure(bass, start, end, 0.1, 0.1, probabilityAndProcessFactor, MusicProcessorTransformation.moveNoteConnectNeighbors, Arrays.asList(0.01d, 0.03d, 1.5d, 1.5d));
        transformOneMeasure(bass, start, end, 1, 1, biggestInTheMiddle, MusicProcessorTransformation.randomPitchShift, Arrays.asList(0.03d, 0.06d));
        transformOneMeasure(bass, start, end, 1, 1, biggestInTheMiddle, MusicProcessorTransformation.linearBend, Arrays.asList(0.02d, 0.06d));
        transformOneMeasure(bass, start, end, 1, 1, biggestInTheMiddle, MusicProcessorTransformation.sinBend, Arrays.asList(0.02d, 0.06d));

        //transformOneMeasure(bass, start, end, 1, 1, probabilityAndProcessFactor, MusicProcessorTransformation.transposePitch, Arrays.asList((double)Globals.jump));
    }


    public static void processTrumpet(Tune trumpet, double probabilityAndProcessFactor, double start, double end, List<Double> weightsPattern, ChordProgression progression){
        double smallestInTheMiddle = Math.abs((probabilityAndProcessFactor - 0.5) * 2);
        double biggestInTheMiddle = 1 - Math.abs((0.5 - probabilityAndProcessFactor) * 2);
        double invert = 1d - probabilityAndProcessFactor;

        applyOneRythmPattern(trumpet, start, end, weightsPattern);
        transformLastNoteInOneMeasure(trumpet, start, end, 0.3, -0.2, probabilityAndProcessFactor, MusicProcessorTransformation.makeSilent, null);
        transformOneMeasure(trumpet, start, end, 0.0, 0.15, probabilityAndProcessFactor, MusicProcessorTransformation.makeSilent, null);
        transformAllButLastBeatInOneMeasure(trumpet, start, end, 0.1, 0.1, probabilityAndProcessFactor, MusicProcessorTransformation.deleteNoteConnectNeighbours, Arrays.asList(1d));
        //transformAllButLastBeatInOneMeasure(trumpet, start, end, 0.03, 0.13, probabilityAndProcessFactor, MusicProcessorTransformation.deleteNoteConnectNeighbours, Arrays.asList(1/4d));

        transformOneMeasure(trumpet, start, end, 0.2, 0.2, biggestInTheMiddle, MusicProcessorTransformation.moveNoteConnectNeighbors, Arrays.asList(0.06d, 0.17d, 3d, 3d));
        transformAllButLastBeatInOneMeasure(trumpet, start, end, 0d, 0.18, biggestInTheMiddle, MusicProcessorTransformation.splitNote, Arrays.asList(1.25d, 0.3d, progression));
        transformAllButLastBeatInOneMeasure(trumpet, start, end, 0.04d, 0.25, probabilityAndProcessFactor, MusicProcessorTransformation.splitNote, Arrays.asList(1d, 0.27d, progression));

        transformOneMeasure(trumpet, start, end, 1, 1, probabilityAndProcessFactor, MusicProcessorTransformation.randomUpVelocity, Arrays.asList(-4d, 8d));
        transformOneMeasure(trumpet, start, end, 0.1, 0.3, probabilityAndProcessFactor, MusicProcessorTransformation.staccato, Arrays.asList(0.05d, 0.15d));

        transformOneMeasure(trumpet, start, end, 0.4, 0.6, probabilityAndProcessFactor, MusicProcessorTransformation.combinedRandomBend, Arrays.asList(0d, 1d/8d, 1d/2d, 1d/4d));
        transformOneMeasure(trumpet, start, end, 1, 1, biggestInTheMiddle, MusicProcessorTransformation.randomPitchShift, Arrays.asList(0.02d, 0.02d));

        //transformOneMeasure(trumpet, start, end, 1, 1, probabilityAndProcessFactor, MusicProcessorTransformation.transposePitch, Arrays.asList((double)Globals.jump));
    }


}
