package com.boardgame.miljac.grangla.music;

public class MidiMessageWithStartBeat implements Comparable<MidiMessageWithStartBeat> {
    public MidiMessageWithStartBeat(byte[] message, double beat) {
        this.message = message;
        this.beat = beat;
    }

    private byte[] message;
    private double beat;

    public byte[] getMessage() {
        return message;
    }

    public void setMessage(byte[] message) {
        this.message = message;
    }

    public double getBeat() {
        return beat;
    }

    public void setBeat(double beat) {
        this.beat = beat;
    }

    @Override
    public int compareTo(MidiMessageWithStartBeat other) {
        return Double.compare(this.beat, other.getBeat());
    }
}
