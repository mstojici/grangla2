package com.boardgame.miljac.grangla.music;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Tune {
    int channel = 0;
    double tuneEndBeats = 0;

    List<Note> notes = new ArrayList<>();

    public Note getLastNote(){
        return notes.get(notes.size() - 1);
    }

    public void addNote(Note note){
        notes.add(note);
        double noteEnd = note.getStartBeat() + note.getLengthBeat();
        if(noteEnd > tuneEndBeats){
           tuneEndBeats = noteEnd;
        }

        sort();
    }

    public void appendTune(Tune otherTune){
        double start = tuneEndBeats;
        for(Note note : otherTune.getNotes()){
            Note newNote = (Note) note.clone();
            newNote.setStartBeat(newNote.getStartBeat() + start);
            notes.add(newNote);
        }
        tuneEndBeats += otherTune.getTuneEndBeats();
        sort();
    }

    public void addAfterLast(Note note){
        notes.add(note);
        tuneEndBeats = tuneEndBeats + note.getLengthBeat();
        sort();
    }

    public void addNotes(List<Note> list){
        notes.addAll(list);
        sort();
    }

    public void removeNotes(List<Note> list){
        notes.removeAll(list);
        sort();
    }

    public void addParallelToLast(Note note){
        Note lastNote = this.getLastNote();
        note.setStartBeat(lastNote.getStartBeat());
        note.setLengthBeat(lastNote.getLengthBeat());
        notes.add(note);
        sort();
    }

    private void sort(){
        Collections.sort(notes, (n1, n2) ->
                        Double.compare(n1.getStartBeat(), n2.getStartBeat()) );
    }

    public List<Note> getNotes() {
        return notes;
    }
    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }

    public double getTuneEndBeats() {
        return tuneEndBeats;
    }

    public void setTuneEndBeats(double tuneEndBeats) {
        this.tuneEndBeats = tuneEndBeats;
    }
}
