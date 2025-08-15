package com.example.eegac;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class NeuroSkyUtil {

    public static int getRawWaveValue(byte high, byte low) {
        int hi = (high < 0) ? high + 256 : high;
        int lo = (low < 0) ? low + 256 : low;
        int value = hi * 256 + lo;
        if (value >= 32768) value -= 65536;
        return value;
    }

    public static int getEEGPowerValue(byte h, byte m, byte l) {
        return (h & 0xFF) * 65536 + (m & 0xFF) * 256 + (l & 0xFF);
    }

    public static void notify2Toast(Handler handler, int what, String text) {
        Message msg = handler.obtainMessage(what);
        Bundle b = new Bundle();
        b.putString("toast", text);
        msg.setData(b);
        handler.sendMessage(msg);
    }
}


