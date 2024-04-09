package com.boardgame.miljac.grangla.music;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.boardgame.miljac.grangla.music.MusicHelpers.*;

public class MusicProcessorTransformation {
    public interface NoteTransformation {
        public TransformationResult transformNote(Tune tune, int index, double processFactor, List<Object> argList);
    }

    public static class TransformationResult{
        int notesToJump;

        public int getNotesToJump() {
            return notesToJump;
        }

        public List<Note> getNotesToAdd() {
            return notesToAdd;
        }

        public List<Note> getNotesToRemove() {
            return notesToRemove;
        }

        List<Note> notesToAdd = new ArrayList<>();
        List<Note> notesToRemove = new ArrayList<>();

        public TransformationResult(int notesToJump, List<Note> notesToAdd, List<Note> notesToRemove){
            this.notesToJump = notesToJump;
            this.notesToAdd = notesToAdd == null ? new ArrayList<>() : notesToAdd;
            this.notesToRemove = notesToRemove  == null ? new ArrayList<>() : notesToRemove;
        }
    }





    public static NoteTransformation randomUpVelocity = (tune, index, processFactor, argList) -> {
        Note note = tune.getNotes().get(index);

        double velocityMaxChange = currentValue((double)argList.get(0), (double)argList.get(1), processFactor);
        double velocityChange = randomUp(velocityMaxChange/2, velocityMaxChange/2);

        double newVelocity = note.getVelocity() + velocityChange;
        if (newVelocity > 127) newVelocity = 127;
        note.setVelocity(newVelocity);
        tune.getNotes().set(index, note);

        return new TransformationResult(0, null, null);
    };

    public static NoteTransformation randomPitchShift = (tune, index, processFactor, argList) -> {
        Note note = tune.getNotes().get(index);

        double pitchMaxShift = currentValue((double)argList.get(0), (double)argList.get(1), processFactor);
        double pitchShift = randomAround(0, pitchMaxShift);

        note.setBasePitchShift(pitchShift);
        tune.getNotes().set(index, note);

        return new TransformationResult(0, null, null);
    };

    public static NoteTransformation transposePitch = (tune, index, processFactor, argList) -> {
        Note note = tune.getNotes().get(index);
        double pitchShift = (double)argList.get(0);

        note.setPitch(note.getPitch() + pitchShift);
        tune.getNotes().set(index, note);

        return new TransformationResult(0, null, null);
    };

    public static NoteTransformation scaleVelocity = (tune, index, processFactor, argList) -> {
        Note note = tune.getNotes().get(index);
        double scaleFactor = currentValue((double)argList.get(0), (double)argList.get(1), processFactor);

        note.setVelocity(note.getVelocity() * scaleFactor);
        tune.getNotes().set(index, note);

        return new TransformationResult(0, null, null);
    };

    public static NoteTransformation linearBend = (tune, index, processFactor, argList) -> {
        Note note = tune.getNotes().get(index);
        double maxAmp = currentValue((double)argList.get(0), (double)argList.get(1), processFactor);

        note.setLinearBend(1);
        note.setLinearBendAmp(randomAround(0, maxAmp));
        tune.getNotes().set(index, note);

        return new TransformationResult(0, null, null);
    };

    public static NoteTransformation sinBend = (tune, index, processFactor, argList) -> {
        Note note = tune.getNotes().get(index);
        double maxAmp = currentValue((double)argList.get(0), (double)argList.get(1), processFactor);

        note.setSinBend(1);
        note.setSinBendAmp(randomAround(0, maxAmp));
        tune.getNotes().set(index, note);

        return new TransformationResult(0, null, null);
    };

    public static NoteTransformation combinedRandomBend = (tune, index, processFactor, argList) -> {
        Note note = tune.getNotes().get(index);
        double maxAmp = currentValue((double)argList.get(0), (double)argList.get(1), processFactor);//1/8
        double currentMinNote = currentValue((double)argList.get(2), (double)argList.get(3), processFactor);

        if(note.getLengthBeat() < currentMinNote)  return new TransformationResult(0, null, null);

        note.setSinBend(1);
        note.setSinBendAmp(randomUp(0, maxAmp));
        note.setCosBend(1);
        note.setCosBendAmp(randomUp(0, maxAmp));
        /*note.setLinearBend(probabilityFulfilled(currentProbability) ? 1 : 0);
        note.setLinearBendAmp((double)getRandomElement(Arrays.asList(1d/16d, 1d/8d, -1d/16d, -1d/8d)));*/

        note.setMultipleBendFreq((double)getRandomElement(Arrays.asList(2d, 3d, 4d)));
        note.setMultipleBendAmpSemitones(randomUp(0, maxAmp));

        return new TransformationResult(0, null, null);
    };

    public static NoteTransformation makeSilent = (tune, index, processFactor, argList) -> {
        Note note = tune.getNotes().get(index);
        note.setVelocity(0);
        tune.getNotes().set(index, note);

        return new TransformationResult(0, null, null);
    };

    public static NoteTransformation staccato = (tune, index, processFactor, argList) -> {
        double maxDelta = currentValue((double)argList.get(0), (double)argList.get(1), processFactor);
        Note note = tune.getNotes().get(index);
        double newLength = note.getLengthBeat() - randomBelow(maxDelta, maxDelta);
        if (newLength > 0) note.setLengthBeat(newLength);
        tune.getNotes().set(index, note);

        return new TransformationResult(0, null, null);
    };

    public static NoteTransformation deleteNoteConnectNeighbours = (tune, index, processFactor, argList) -> {
        double biggestNote = (double)argList.get(0);
        Note note = tune.getNotes().get(index);
        if(note.getLengthBeat() > biggestNote){
            return new TransformationResult(0, null, null);
        }

        Note previousNote = tune.getNotes().get(index - 1);
        Note nextNote = tune.getNotes().get(index + 1);

        previousNote.setLengthBeat(previousNote.getLengthBeat() + note.getLengthBeat() / 2);

        nextNote.setStartBeat(nextNote.getStartBeat() - note.getLengthBeat() / 2);
        nextNote.setLengthBeat(nextNote.getLengthBeat() + note.getLengthBeat() / 2);

        tune.getNotes().set(index - 1, previousNote);
        tune.getNotes().set(index + 1, nextNote);

        return new TransformationResult(0, null, Arrays.asList(note));
    };

    public static NoteTransformation splitNote = (tune, index, processFactor, argList) -> {
        double biggestNote = (double)argList.get(0);
        double smallestNote = (double)argList.get(1);
        ChordProgression progression = (ChordProgression)argList.get(2);

        Note note = tune.getNotes().get(index);
        if((note.getLengthBeat() > biggestNote) ||
                (note.getLengthBeat() < smallestNote)){
            return new TransformationResult(0, null, null);
        }

        List<Integer> effectiveScale = removeChordFromScale(progression.getScale(),
                progression.getCurrentChord().getPitches());
        Note nextNote = tune.getNotes().get(index + 1);
        double newNotePitch;

        if(nextNote.getPitch() > note.getPitch()){
            newNotePitch = jumpUpscale((int) note.getPitch(), effectiveScale, 1);
        } else {
            newNotePitch = jumpDownscale((int) note.getPitch(), effectiveScale, 1);
        }

        if(newNotePitch == nextNote.getPitch()){
            return new TransformationResult(0, null, null);
        }

        double lengthFactor = getRandomInRange(4d/3d, 2d);
        double newLength = note.getLengthBeat() - (note.getLengthBeat() / lengthFactor);
        note.setLengthBeat(note.getLengthBeat() / lengthFactor);

        tune.getNotes().set(index, note);

        return new TransformationResult(0, Arrays.asList(
                new Note(note.getStartBeat() + note.getLengthBeat(), newLength, newNotePitch, note.getVelocity(), note.getLabel())),
                null);
    };



    public static NoteTransformation moveNoteConnectNeighbors = (tune, index, processFactor, argList) -> {
        Note currentNote = tune.getNotes().get(index);

        double maxTimeShift = currentValue((double)argList.get(0), (double)argList.get(1), processFactor);
        double maxWidthBeats = currentValue((double)argList.get(2), (double)argList.get(3), processFactor);

        double timeShift = randomAround(0, maxTimeShift);
        double widthBeatsBefore =  (maxWidthBeats <= timeShift*1.5) ? timeShift*1.5 : maxWidthBeats;
        double widthBeatsAfter =  (maxWidthBeats <= timeShift*1.5) ? timeShift*1.5 : maxWidthBeats;

        double shiftStart = currentNote.getStartBeat() - widthBeatsBefore;
        double shiftEnd = currentNote.getStartBeat() + widthBeatsAfter;

        double squashFactorBefore = (currentNote.getStartBeat() + timeShift - shiftStart) / (currentNote.getStartBeat() - shiftStart);
        double squashFactorAfter = (shiftEnd - currentNote.getStartBeat() - timeShift) / (shiftEnd - currentNote.getStartBeat());

        AtomicInteger jumpCounter = new AtomicInteger();

        tune.getNotes().stream()
                .forEach(note -> {
                    double noteEnd = note.getStartBeat() + note.getLengthBeat();
                    int i = tune.getNotes().indexOf(note);

                    double newStart = note.getStartBeat();
                    double newEnd = note.getStartBeat() + note.getLengthBeat();

                    if((note.getStartBeat() < currentNote.getStartBeat()) &&
                            (note.getStartBeat() >= shiftStart)){
                        newStart = (note.getStartBeat() - shiftStart) * squashFactorBefore + shiftStart;
                    }
                    if((note.getStartBeat() >= currentNote.getStartBeat()) &&
                            (note.getStartBeat() < shiftEnd)){
                        newStart = shiftEnd - (shiftEnd - note.getStartBeat()) * squashFactorAfter;
                    }

                    if((noteEnd < currentNote.getStartBeat()) &&
                            (noteEnd >= shiftStart)){
                        newEnd = (noteEnd - shiftStart) * squashFactorBefore + shiftStart;
                    }
                    if((noteEnd >= currentNote.getStartBeat()) &&
                            (noteEnd < shiftEnd)){
                        newEnd = shiftEnd - (shiftEnd - noteEnd) * squashFactorAfter;
                    }

                    note.setStartBeat(newStart);
                    note.setLengthBeat(newEnd - note.getStartBeat());

                    tune.getNotes().set(i, note);

                });

        return new TransformationResult(jumpCounter.get(), null, null);
    };
}
