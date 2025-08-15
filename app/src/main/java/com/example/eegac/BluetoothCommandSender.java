// BluetoothCommandSender.java
package com.example.eegac;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 蓝牙指令发送器。
 * 负责通过蓝牙Socket向单片机发送指令。
 * 所有发送操作都在后台线程中执行，以避免阻塞UI线程。
 */
public class BluetoothCommandSender {

    private static final String TAG = "BtCommandSender";

    private final BluetoothSocket socket;
    private final OutputStream outputStream;
    private final ExecutorService executor;

    public BluetoothCommandSender(BluetoothSocket socket) throws IOException {
        this.socket = socket;
        this.outputStream = socket.getOutputStream();
        // 创建一个单线程的线程池，确保指令按顺序发送
        this.executor = Executors.newSingleThreadExecutor();
    }

    /**
     * 发送单个字节的指令。
     * @param command 要发送的指令 (来自 AcCommands 类)
     */
    public void sendCommand(byte command) {
        // 在后台线程中执行发送操作
        executor.submit(() -> {
            try {
                Log.d(TAG, "Sending command: " + String.format("0x%02X", command));
                outputStream.write(command);
                outputStream.flush(); // 确保数据被立即发送
            } catch (IOException e) {
                Log.e(TAG, "Error sending command", e);
                // 在这里可以添加错误处理逻辑，比如尝试重连或通知用户
            }
        });
    }

    /**
     * 关闭连接并释放资源。
     * 在Activity销毁时调用。
     */
    public void disconnect() {
        executor.shutdown(); // 停止接收新任务
        try {
            if (outputStream != null) {
                outputStream.close();
            }
            if (socket != null) {
                socket.close();
            }
            Log.d(TAG, "Bluetooth connection closed.");
        } catch (IOException e) {
            Log.e(TAG, "Error closing bluetooth socket", e);
        }
    }
}