package com.boardgame.miljac.grangla.music;

import android.util.Log;

import com.boardgame.miljac.grangla.gameplay.TableConfig;
import com.boardgame.miljac.grangla.wifi.GranglaWiFiService;

import org.billthefarmer.mididriver.GeneralMidiConstants;
import org.billthefarmer.mididriver.MidiConstants;
import org.billthefarmer.mididriver.MidiDriver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.LockSupport;

import static com.boardgame.miljac.grangla.music.MusicHelpers.getRandomElement;
import static com.boardgame.miljac.grangla.music.MusicHelpers.getRandomInRange;


public class MusicPlayer implements Runnable {
    boolean mute = false;
    boolean endSong = false;
    private MidiDriver midiDriver = MidiDriver.getInstance();
    long noteDuration;
    double processorFactor;
    int measure = 3;
    public boolean server = true;
    public GranglaWiFiService wifiService = null;

    public void setEndSong() {
        endSong = true;
    }

    public boolean isEndSong() {
        return endSong;
    }

    public MidiDriver getMidiDriver() {
        return midiDriver;
    }

    public void setMidiDriver(MidiDriver midiDriver) {
        this.midiDriver = midiDriver;
    }

    public void mute() {
        mute = true;
    }

    public void silence(){
        wifiService.getProxy().silence = true;
    }

    public void unsilence() {
        wifiService.getProxy().silence = false;
    }

    public void run() {
        int[] config;
        midiDriver.start();
        config = midiDriver.config();

        // Print out the details.
        Log.d(this.getClass().getName(), "maxVoices: " + config[0]);
        Log.d(this.getClass().getName(), "numChannels: " + config[1]);
        Log.d(this.getClass().getName(), "sampleRate: " + config[2]);
        Log.d(this.getClass().getName(), "mixBufferSize: " + config[3]);

        byte pianoInstrument = (byte)getRandomElement(Arrays.asList(
                GeneralMidiConstants.ACOUSTIC_GRAND_PIANO,
                GeneralMidiConstants.BRIGHT_ACOUSTIC_PIANO,
                //GeneralMidiConstants.ELECTRIC_GRAND_PIANO,
                GeneralMidiConstants.HONKY_TONK_PIANO,
                //GeneralMidiConstants.ELECTRIC_PIANO_0,
                //GeneralMidiConstants.ELECTRIC_PIANO_1,
                //GeneralMidiConstants.HARPSICHORD,
                //GeneralMidiConstants.CELESTA,
                GeneralMidiConstants.VIBRAPHONE
                //GeneralMidiConstants.MARIMBA
                //GeneralMidiConstants.ROCK_ORGAN,
                //GeneralMidiConstants.XYLOPHONE
        ));

        byte bassInstrument =  (byte)getRandomElement(Arrays.asList(
                GeneralMidiConstants.ACOUSTIC_BASS,
                GeneralMidiConstants.ELECTRIC_BASS_FINGER,
                GeneralMidiConstants.ELECTRIC_BASS_PICK,
                GeneralMidiConstants.FRETLESS_BASS
                /*GeneralMidiConstants.SLAP_BASS_0
                GeneralMidiConstants.SLAP_BASS_1
                GeneralMidiConstants.SYNTH_BASS_0,
                GeneralMidiConstants.SYNTH_BASS_1*/
        ));

        byte drumsInstrument = (byte)getRandomElement(Arrays.asList(
                GeneralMidiConstants.STEEL_DRUMS,
                GeneralMidiConstants.SYNTH_DRUM));

        byte trumpetInstrument = (byte)getRandomElement(Arrays.asList(
                GeneralMidiConstants.TRUMPET,
                GeneralMidiConstants.TROMBONE,
                GeneralMidiConstants.TUBA,
                //GeneralMidiConstants.MUTED_TRUMPET,
                //GeneralMidiConstants.FRENCH_HORN,
                GeneralMidiConstants.SOPRANO,
                GeneralMidiConstants.ALTO_SAX,
                //GeneralMidiConstants.TENOR_SAX,
                GeneralMidiConstants.BARITONE_SAX,
                GeneralMidiConstants.OBOE,
                //GeneralMidiConstants.ENGLISH_HORN,
                //GeneralMidiConstants.BASSOON,
                GeneralMidiConstants.CLARINET,
                GeneralMidiConstants.PICCOLO,
                //GeneralMidiConstants.FLUTE,
                GeneralMidiConstants.RECORDER,
                //GeneralMidiConstants.PAN_FLUTE,
                //GeneralMidiConstants.SHAKUHACHI,
                GeneralMidiConstants.OCARINA));


        sendMidi(MidiConstants.PROGRAM_CHANGE | 0x01, pianoInstrument);
        sendMidi((MidiConstants.PROGRAM_CHANGE), bassInstrument);
        sendMidi((MidiConstants.PROGRAM_CHANGE | 0x09), drumsInstrument);
        sendMidi((MidiConstants.PROGRAM_CHANGE | 0x02), trumpetInstrument);

        Log.d("grangleninstr", "pianoInstrument: " + pianoInstrument);
        Log.d("grangleninstr", "bassInstrument: " + bassInstrument);
        Log.d("grangleninstr", "drumsInstrument: " + drumsInstrument);
        Log.d("grangleninstr", "trumpetInstrument: " + trumpetInstrument);

        MidiGenerator midiGenerator = new MidiGenerator();

        long beatStartTime = System.currentTimeMillis();;
        double beatLengthMilis = noteDuration*2.5;
        int currentMeasure = measure;
        MusicGlobals.jump = getRandomInRange(-6,6);
        List<MidiMessageWithStartBeat> thisBeat =  midiGenerator.getMidiFirstBeat(currentMeasure, processorFactor);
        GeneratorRunnable generatorRunnable = new GeneratorRunnable(midiGenerator, measure, processorFactor);
        Thread generatorThread = new Thread(generatorRunnable);
        generatorThread.start();
        while (!mute) {


            if(thisBeat.size() == 0 &&
                    (System.currentTimeMillis() - beatStartTime) >= beatLengthMilis){   //new beat


                thisBeat = generatorRunnable.getNextBeat();
                if(! generatorThread.isAlive()){
                    generatorRunnable = new GeneratorRunnable(midiGenerator, measure, processorFactor);
                    generatorThread = new Thread(generatorRunnable);
                    generatorThread.start();
                }


                beatStartTime = System.currentTimeMillis();
                beatLengthMilis = (4*beatLengthMilis + noteDuration*2.5) / 5;
            }

            MidiMessageWithStartBeat currentMidiMessageWithStartBeat = null;
            double waitForTime = beatLengthMilis;
            if(thisBeat.size() != 0) {
                currentMidiMessageWithStartBeat = thisBeat.remove(0);
                waitForTime = currentMidiMessageWithStartBeat.getBeat() * beatLengthMilis;
            }

            while (System.currentTimeMillis() < (beatStartTime + waitForTime)) {
                LockSupport.parkNanos(100_000);
            }

            if(currentMidiMessageWithStartBeat != null) {
                byte[] msg = currentMidiMessageWithStartBeat.getMessage();
                if(wifiService == null){
                    midiDriver.write(msg);
                } else {
                    if (server) {
                        if(((byte)(msg[0] & 0x0F) == (byte)0x01)){
                            wifiService.getProxy().send(msg);
                        } else if(((byte)(msg[0] & 0x0F) == (byte)0x09) ||
                                ((byte)(msg[0] & 0x0F) == (byte)0x00)){
                            wifiService.getProxy().serverPlay(msg);
                        } else {
                            wifiService.getProxy().sendAndPlay(msg);
                        }
                    }
                }
            }
        }

        midiDriver.stop();
        if(wifiService != null) {
            wifiService.getProxy().stop();
        }
    }

    private class GeneratorRunnable implements Runnable {
        private volatile List<MidiMessageWithStartBeat> nextBeat;
        private MidiGenerator midiGenerator;
        private int measure;
        private double processorFactor;

        public GeneratorRunnable(MidiGenerator midiGenerator, int measure, double processorFactor){
            this.midiGenerator = midiGenerator;
            this.measure = measure;
            this.processorFactor = processorFactor;
        }

        @Override
        public synchronized void run() {
            nextBeat = new ArrayList<>(midiGenerator.getMidiFirstBeat(measure, processorFactor));
        }

        public List<MidiMessageWithStartBeat> getNextBeat() {
            return nextBeat;
        }
    }


    public void setNoteDuration(long duration) {
        this.noteDuration = duration;

        double waitingTime = duration / TableConfig.NOTE_DURATION_FACTOR;

        if (waitingTime > ((TableConfig.MAX_WAITING_TIME - TableConfig.MIN_WAITING_TIME) * 10 / 16 + TableConfig.MIN_WAITING_TIME)) {// *12/16
            setMeasure(3);
        } else if (waitingTime > ((TableConfig.MAX_WAITING_TIME - TableConfig.MIN_WAITING_TIME) * 4 / 16 + TableConfig.MIN_WAITING_TIME)) {// 8/16
            setMeasure(4);
        } else if (waitingTime > ((TableConfig.MAX_WAITING_TIME - TableConfig.MIN_WAITING_TIME) * getRandomInRange(0d, 5d) / 16 + TableConfig.MIN_WAITING_TIME)) {// 4/16
            setMeasure(5);
        } else {
            setMeasure(2);
        }

        if (isEndSong()) {
            setMeasure(2);
        }

        processorFactor = 1 - ((waitingTime - TableConfig.MIN_WAITING_TIME) / (TableConfig.MAX_WAITING_TIME - TableConfig.MIN_WAITING_TIME));
    }

    private void setMeasure(int measure) {
        this.measure = measure;
    }

    // Send a midi message, 2 bytes
    protected void sendMidi(int m, int n)
    {
        byte msg[] = new byte[2];
        msg[0] = (byte) m;
        msg[1] = (byte) n;
        midiDriver.write(msg);
    }
}

