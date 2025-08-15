package com.example.eegac;

import java.io.Serializable;

public class NeuroRawData implements Serializable {
    private static final long serialVersionUID = 1L;
    private int rawWaveValue;

    public NeuroRawData() {}
    public NeuroRawData(int rawWaveValue) { this.rawWaveValue = rawWaveValue; }
    public int getRawWaveValue() { return rawWaveValue; }
    public void setRawWaveValue(int rawWaveValue) { this.rawWaveValue = rawWaveValue; }
}


