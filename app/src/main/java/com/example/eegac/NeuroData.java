package com.example.eegac;

import java.io.Serializable;

public class NeuroData implements Serializable {
    private static final long serialVersionUID = 1L;

    private int signal;
    private int delta;
    private int theta;
    private int lowAlpha;
    private int highAlpha;
    private int lowBeta;
    private int highBeta;
    private int lowGamma;
    private int midGamma;
    private int attention;
    private int meditation;

    public NeuroData() {}

    public NeuroData(int signal, int delta, int theta, int lowAlpha, int highAlpha,
                     int lowBeta, int highBeta, int lowGamma, int midGamma,
                     int attention, int meditation) {
        this.signal = signal;
        this.delta = delta;
        this.theta = theta;
        this.lowAlpha = lowAlpha;
        this.highAlpha = highAlpha;
        this.lowBeta = lowBeta;
        this.highBeta = highBeta;
        this.lowGamma = lowGamma;
        this.midGamma = midGamma;
        this.attention = attention;
        this.meditation = meditation;
    }

    public int getSignal() { return signal; }
    public int getDelta() { return delta; }
    public int getTheta() { return theta; }
    public int getLowAlpha() { return lowAlpha; }
    public int getHighAlpha() { return highAlpha; }
    public int getLowBeta() { return lowBeta; }
    public int getHighBeta() { return highBeta; }
    public int getLowGamma() { return lowGamma; }
    public int getMidGamma() { return midGamma; }
    public int getAttention() { return attention; }
    public int getMeditation() { return meditation; }

    public void setSignal(int v) { signal = v; }
    public void setDelta(int v) { delta = v; }
    public void setTheta(int v) { theta = v; }
    public void setLowAlpha(int v) { lowAlpha = v; }
    public void setHighAlpha(int v) { highAlpha = v; }
    public void setLowBeta(int v) { lowBeta = v; }
    public void setHighBeta(int v) { highBeta = v; }
    public void setLowGamma(int v) { lowGamma = v; }
    public void setMidGamma(int v) { midGamma = v; }
    public void setAttention(int v) { attention = v; }
    public void setMeditation(int v) { meditation = v; }
}


