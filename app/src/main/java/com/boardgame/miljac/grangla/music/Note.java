package com.boardgame.miljac.grangla.music;

public class Note implements Cloneable {


    private double startBeat = 0;
    private double lengthBeat = 0;
    private double pitch = 0;
    private double velocity = 0;
    //String scaleName;
    private double multipleBendFreq = 0;
    private double multipleBendAmpSemitones = 0;
    private double multipleOtherBendFreq = 0;
    private double multipleOtherBendAmpSemitones = 0;
    private double linearBend = 0;
    private double linearBendAmp = 0;
    private double sinBend = 0;
    private double sinBendAmp = 0;
    private double cosBend = 0;
    private double cosBendAmp = 0;
    private double vibratoFreq = 0;
    private double vibratoAmpSemitones = 0;
    private double basePitchShift = 0;
    private double slideIn = 0;
    private double slideOut = 0;
    private String label;

    public Note(double startBeat, double lengthBeat, double pitch, double velocity, String label){
        this.startBeat = startBeat;
        this.lengthBeat = lengthBeat;
        this.pitch = pitch;
        this.velocity = velocity;
        this.label = label;
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public double getStartBeat() {
        return startBeat;
    }

    public void setStartBeat(double startBeat) {
        this.startBeat = startBeat;
    }

    public double getLengthBeat() {
        return lengthBeat;
    }

    public void setLengthBeat(double lengthBeat) {
        this.lengthBeat = lengthBeat;
    }

    public double getPitch() {
        return pitch;
    }

    public void setPitch(double pitch) {
        this.pitch = pitch;
    }

    public double getVelocity() {
        return velocity;
    }

    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }

    /*public String getScaleName() {
        return scaleName;
    }

    public void setScaleName(String scaleName) {
        this.scaleName = scaleName;
    }*/

    public double getMultipleBendFreq() {
        return multipleBendFreq;
    }

    public void setMultipleBendFreq(double multipleBendFreq) {
        this.multipleBendFreq = multipleBendFreq;
    }

    public double getMultipleBendAmpSemitones() {
        return multipleBendAmpSemitones;
    }

    public void setMultipleBendAmpSemitones(double multipleBendAmpSemitones) {
        this.multipleBendAmpSemitones = multipleBendAmpSemitones;
    }

    public double getLinearBend() {
        return linearBend;
    }

    public void setLinearBend(double linearBend) {
        this.linearBend = linearBend;
    }

    public double getLinearBendAmp() {
        return linearBendAmp;
    }

    public void setLinearBendAmp(double linearBendAmp) {
        this.linearBendAmp = linearBendAmp;
    }

    public double getSinBend() {
        return sinBend;
    }

    public void setSinBend(double sinBend) {
        this.sinBend = sinBend;
    }

    public double getSinBendAmp() {
        return sinBendAmp;
    }

    public void setSinBendAmp(double sinBendAmp) {
        this.sinBendAmp = sinBendAmp;
    }

    public double getVibratoFreq() {
        return vibratoFreq;
    }

    public void setVibratoFreq(double vibratoFreq) {
        this.vibratoFreq = vibratoFreq;
    }

    public double getVibratoAmpSemitones() {
        return vibratoAmpSemitones;
    }

    public void setVibratoAmpSemitones(double vibratoAmpSemitones) {
        this.vibratoAmpSemitones = vibratoAmpSemitones;
    }

    public double getCosBend() {
        return cosBend;
    }

    public void setCosBend(double cosBend) {
        this.cosBend = cosBend;
    }

    public double getCosBendAmp() {
        return cosBendAmp;
    }

    public void setCosBendAmp(double cosBendAmp) {
        this.cosBendAmp = cosBendAmp;
    }

    public double getBasePitchShift() {
        return basePitchShift;
    }

    public void setBasePitchShift(double basePitchShift) {
        this.basePitchShift = basePitchShift;
    }

    public double getSlideIn() {
        return slideIn;
    }

    public void setSlideIn(double slideIn) {
        this.slideIn = slideIn;
    }

    public double getSlideOut() {
        return slideOut;
    }

    public void setSlideOut(double slideOut) {
        this.slideOut = slideOut;
    }

    public double getMultipleOtherBendFreq() {
        return multipleOtherBendFreq;
    }

    public void setMultipleOtherBendFreq(double multipleOtherBendFreq) {
        this.multipleOtherBendFreq = multipleOtherBendFreq;
    }

    public double getMultipleOtherBendAmpSemitones() {
        return multipleOtherBendAmpSemitones;
    }

    public void setMultipleOtherBendAmpSemitones(double multipleOtherBendAmpSemitones) {
        this.multipleOtherBendAmpSemitones = multipleOtherBendAmpSemitones;
    }

    public String getLabel() {
        return label;
    }
}
