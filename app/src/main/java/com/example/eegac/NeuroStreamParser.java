package com.example.eegac;

import android.os.Handler;

public class NeuroStreamParser {
    public static final int MESSAGE_READ_RAW_DATA_PACKET = 17;
    public static final int MESSAGE_READ_DIGEST_DATA_PACKET = 18;

    private final Handler handler;
    private int state = 1;
    private int payloadSum;
    private int checksum;
    private int signal;
    private int rawWaveValue;
    private final byte[] payload = new byte[5];

    public NeuroStreamParser(Handler handler) {
        this.handler = handler;
    }

    public int parseByte(byte b) {
        int ub = b & 0xFF;
        switch (state) {
            case 1:
                state = (ub == 0xAA) ? 2 : 1;
                break;
            case 2:
                state = (ub == 0xAA) ? 3 : 1;
                break;
            case 3:
                if (ub == 32) { // digest-like short frame (signal only)
                    state = 4; payloadSum = 0;
                } else if (ub == 4) { // raw frame header length field incoming
                    state = 37; payloadSum = 0;
                } else {
                    state = 1;
                }
                break;
            case 4:
                if (ub == 2) { state = 5; payloadSum += ub; } else state = 1;
                break;
            case 5:
                signal = ub;
                NeuroData data = new NeuroData(signal,0,0,0,0,0,0,0,0,0,0);
                handler.obtainMessage(MESSAGE_READ_DIGEST_DATA_PACKET, data).sendToTarget();
                payloadSum += ub;
                state = 1;
                break;
            case 37:
                if (ub == 128) { state = 38; payloadSum += ub; } else state = 1;
                break;
            case 38:
                if (ub == 2) { state = 39; payloadSum += ub; } else state = 1;
                break;
            case 39:
                payload[1] = (byte) ub; payloadSum += ub; state = 40; break;
            case 40:
                payload[2] = (byte) ub; rawWaveValue = NeuroSkyUtil.getRawWaveValue(payload[1], payload[2]); payloadSum += ub; state = 41; break;
            case 41:
                checksum = ub;
                if (checksum == ((payloadSum ^ 0xFFFFFFFF) & 0xFF)) {
                    NeuroRawData raw = new NeuroRawData(rawWaveValue);
                    handler.obtainMessage(MESSAGE_READ_RAW_DATA_PACKET, raw).sendToTarget();
                }
                state = 1; break;
        }
        return 0;
    }
}


