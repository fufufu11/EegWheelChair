package com.example.eegac;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

/**
 * 从已建立的 BluetoothSocket 读取字节流并交给 NeuroStreamParser。
 * 你可以在 HeadsetConnector 连接成功后，传入 socket 来启动读取。
 */
public class NeuroSkyReader extends Thread {

    private static final String TAG = "NeuroSkyReader";
    private final BluetoothSocket socket;
    private final NeuroStreamParser parser;
    private volatile boolean running = true;

    public NeuroSkyReader(BluetoothSocket socket, Handler handler) {
        this.socket = socket;
        this.parser = new NeuroStreamParser(handler);
        setName("NeuroSkyReader");
    }

    @Override
    public void run() {
        try (InputStream in = socket.getInputStream()) {
            byte[] one = new byte[1];
            while (running) {
                int r = in.read(one);
                if (r == -1) break;
                parser.parseByte(one[0]);
            }
        } catch (IOException e) {
            Log.e(TAG, "read loop error", e);
        }
    }

    public void shutdown() {
        running = false;
        try { socket.close(); } catch (IOException ignored) {}
    }
}


