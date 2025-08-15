package com.example.eegac;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.util.UUID;

/**
 * 经典蓝牙 SPP 耳机连接器（硬编码 MAC）。
 */
public class HeadsetConnector {

    public interface Listener {
        @MainThread void onConnected(@NonNull BluetoothSocket socket);
        @MainThread void onError(@NonNull String message, @Nullable Throwable t);
    }

    private static final String TAG = "HeadsetConnector";
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final Context context;
    private final BluetoothAdapter adapter;
    private BluetoothSocket socket;

    public HeadsetConnector(Context context) {
        this.context = context.getApplicationContext();
        this.adapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void connectHardcoded(@NonNull String mac, @NonNull Listener listener) {
        if (adapter == null) {
            listener.onError("此设备不支持蓝牙", null);
            return;
        }
        if (!adapter.isEnabled()) {
            listener.onError("蓝牙未开启", null);
            return;
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    listener.onError("缺少 BLUETOOTH_CONNECT 权限", null);
                    return;
                }
            }
            BluetoothDevice device = adapter.getRemoteDevice(mac);
            socket = device.createRfcommSocketToServiceRecord(SPP_UUID);
            // 取消发现以加速连接
            if (adapter.isDiscovering()) adapter.cancelDiscovery();
            new Thread(() -> {
                try {
                    socket.connect();
                    BluetoothSocket connected = socket;
                    runOnMain(() -> listener.onConnected(connected));
                } catch (IOException e) {
                    // fallback: 反射通道1
                    try {
                        closeQuietly();
                        socket = (BluetoothSocket) device.getClass().getMethod("createRfcommSocket", int.class).invoke(device, 1);
                        if (adapter.isDiscovering()) adapter.cancelDiscovery();
                        socket.connect();
                        BluetoothSocket connected = socket;
                        runOnMain(() -> listener.onConnected(connected));
                        return;
                    } catch (Exception reflectEx) {
                        closeQuietly();
                        Log.e(TAG, "fallback connect error", reflectEx);
                        runOnMain(() -> listener.onError("连接失败(通道)", reflectEx));
                        return;
                    }
                }
            }).start();
        } catch (Exception e) {
            Log.e(TAG, "prepare connect error", e);
            listener.onError("连接初始化失败", e);
        }
    }

    public void closeQuietly() {
        try { if (socket != null) socket.close(); } catch (IOException ignored) { }
        socket = null;
    }

    private void runOnMain(Runnable r) {
        android.os.Handler h = new android.os.Handler(context.getMainLooper());
        h.post(r);
    }
}


