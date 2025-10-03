// MainActivity.java
package com.example.eegac;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String HARD_CODED_MAC_HEADSET = "35:53:17:04:14:4B";
    private static final String HARD_CODED_MAC_MCU = "00:1A:FF:09:05:3F";

    private BluetoothAdapter btAdapter;
    private HeadsetConnector connector;
    private NeuroSkyReader neuroReader;
    private TextView tvConnectionStatus;
    private TextView tvTriggeredFreq;
    private com.example.eegac.BlinkCornerView blinkView;
    private BluetoothCommandSender commandSender;

    private static final int FFT_BITS = 9;
    private static final int WINDOW_SIZE = 1 << FFT_BITS;
    private final FFT fft = new FFT(FFT_BITS);
    private final double[] real = new double[WINDOW_SIZE];
    private final double[] imag = new double[WINDOW_SIZE];
    private final double[] amp = new double[WINDOW_SIZE];
    private final int[] buffer = new int[WINDOW_SIZE];
    private int bufCount = 0;
    private int lastPeak = -1;
    private int repeatCount = 0;
    private int signalQuality = 200;

    private final Handler eegHandler = new Handler(Looper.getMainLooper()) {
        @Override public void handleMessage(Message msg) {
            if (msg.what == NeuroStreamParser.MESSAGE_READ_RAW_DATA_PACKET) {
                if (signalQuality == 0) {
                    NeuroRawData raw = (NeuroRawData) msg.obj;
                    buffer[bufCount++] = raw.getRawWaveValue();
                    if (bufCount >= WINDOW_SIZE) {
                        onWindowReady();
                        bufCount = 0;
                    }
                }
            } else if (msg.what == NeuroStreamParser.MESSAGE_READ_DIGEST_DATA_PACKET) {
                NeuroData data = (NeuroData) msg.obj;
                signalQuality = data.getSignal();
            }
        }
    };

    private void onWindowReady() {
        for (int i = 0; i < WINDOW_SIZE; i++) { real[i] = buffer[i]; imag[i] = 0.0; }
        fft.doFFT(real, imag, false);
        fft.computeAmplitude(real, imag, amp);

        int[] targetFreqs = {6, 8, 11, 13};
        double maxCombinedSnr = -1.0;
        int bestPeak = -1;

        for (int freq : targetFreqs) {
            double snrFundamental = calculateSnr(freq);
            int harmonicFreq = freq * 2;
            double snrHarmonic = calculateSnr(harmonicFreq);
            double combinedSnr = snrFundamental + 0.5 * snrHarmonic;

            if (combinedSnr > maxCombinedSnr) {
                maxCombinedSnr = combinedSnr;
                bestPeak = freq;
            }
        }

        int peak = bestPeak;

        if (peak == lastPeak) {
            repeatCount++;
        } else {
            repeatCount = 1;
            lastPeak = peak;
        }

        // --- 修改点：将确认次数从 2 改回 3 ---
        if (repeatCount < 3) return;

        repeatCount = 0;

        int canonical = canonicalizePeak(peak);
        updateTriggeredFrequency(canonical, maxCombinedSnr);
        if (blinkView != null) {
            blinkView.highlightFrequency(canonical);
        }

        if (canonical <= 0) {
            return;
        }

        if (commandSender == null) {
            return;
        }

        switch (canonical) {
            case 6: commandSender.sendCommand(AcCommands.CMD_FORWARD); break;
            case 8: commandSender.sendCommand(AcCommands.CMD_TURN_RIGHT); break;
            case 11: commandSender.sendCommand(AcCommands.CMD_TURN_LEFT); break;
            case 13: commandSender.sendCommand(AcCommands.CMD_BACKWARD); break;
        }
    }

    private double calculateSnr(int freq) {
        if (freq <= 0 || freq >= amp.length) {
            return 0;
        }
        double signal = amp[freq];
        double noise = 0;
        int noiseBins = 0;
        for (int offset = -2; offset <= 2; offset++) {
            if (offset == 0) continue;
            int bin = freq + offset;
            if (bin > 0 && bin < amp.length) {
                noise += amp[bin];
                noiseBins++;
            }
        }
        double meanNoise = (noiseBins > 0) ? (noise / noiseBins) : 1.0;
        return (meanNoise > 0) ? (signal / meanNoise) : 0;
    }


    private int canonicalizePeak(int peak) {
        if (peak >= 5 && peak <= 7) return 6;
        if (peak > 7 && peak <= 9) return 8;
        if (peak > 10 && peak <= 12) return 11;
        if (peak > 12 && peak <= 14) return 13;
        return -1;
    }

    private ActivityResultLauncher<String[]> permissionLauncher;
    private ActivityResultLauncher<Intent> enableBtLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            Toast.makeText(this, "此设备不支持蓝牙", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        connector = new HeadsetConnector(this);
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
            if (Boolean.TRUE.equals(permissions.get(Manifest.permission.BLUETOOTH_CONNECT)) &&
                    Boolean.TRUE.equals(permissions.get(Manifest.permission.BLUETOOTH_SCAN))) {
                Toast.makeText(this, "蓝牙权限已获取", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "需要蓝牙连接和扫描权限才能继续", Toast.LENGTH_LONG).show();
            }
        });
        enableBtLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() != Activity.RESULT_OK) {
                Toast.makeText(this, "蓝牙未开启", Toast.LENGTH_LONG).show();
            }
        });
        findViewById(R.id.btn_connect).setOnClickListener(v -> showBottomMenu());
        tvConnectionStatus = findViewById(R.id.tv_connection_status);
        tvTriggeredFreq = findViewById(R.id.tv_triggered_freq);
        blinkView = findViewById(R.id.blink_view);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }

    private void showBottomMenu() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View content = LayoutInflater.from(this).inflate(R.layout.layout_bottom_menu, null);
        dialog.setContentView(content);
        MaterialButton btnConnectHeadset = content.findViewById(R.id.btn_connect_headset);
        MaterialButton btnConnectBluetooth = content.findViewById(R.id.btn_connect_bluetooth);
        btnConnectHeadset.setOnClickListener(v -> {
            dialog.dismiss();
            connectHeadset();
        });
        btnConnectBluetooth.setOnClickListener(v -> {
            dialog.dismiss();
            connectBluetoothDevice(HARD_CODED_MAC_MCU);
        });
        dialog.show();
    }

    private void connectBluetoothDevice(String mac) {
        if (!checkPermissionsAndBluetooth()) return;
        Toast.makeText(this, "开始连接单片机", Toast.LENGTH_SHORT).show();
        connector.connectHardcoded(mac, new HeadsetConnector.Listener() {
            @Override
            public void onConnected(android.bluetooth.BluetoothSocket socket) {
                Toast.makeText(MainActivity.this, "单片机连接成功", Toast.LENGTH_LONG).show();
                try {
                    if (commandSender != null) { commandSender.disconnect(); }
                    commandSender = new BluetoothCommandSender(socket);
                } catch (IOException e) {
                    Log.e("MainActivity", "Failed to create command sender", e);
                }
            }
            @Override
            public void onError(String message, Throwable t) {
                Toast.makeText(MainActivity.this, "单片机连接失败: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void connectHeadset() {
        if (!checkPermissionsAndBluetooth()) return;
        Toast.makeText(this, "开始连接耳机", Toast.LENGTH_SHORT).show();
        doConnectHeadset();
    }

    private boolean checkPermissionsAndBluetooth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(new String[]{ Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN });
                return false;
            }
        }
        if (!btAdapter.isEnabled()) {
            enableBtLauncher.launch(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
            return false;
        }
        return true;
    }

    private void doConnectHeadset() {
        connector.connectHardcoded(HARD_CODED_MAC_HEADSET, new HeadsetConnector.Listener() {
            @Override
            public void onConnected(BluetoothSocket socket) {
                Toast.makeText(MainActivity.this, "耳机连接成功", Toast.LENGTH_LONG).show();
                updateConnectionStatus(true);
                updateTriggeredFrequency(-1, 0);
                if (neuroReader != null) { neuroReader.shutdown(); }
                neuroReader = new NeuroSkyReader(socket, eegHandler);
                neuroReader.start();
            }
            @Override
            public void onError(String message, Throwable t) {
                Toast.makeText(MainActivity.this, "耳机连接失败: " + message, Toast.LENGTH_LONG).show();
                updateConnectionStatus(false);
                updateTriggeredFrequency(-1, 0);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (neuroReader != null) { neuroReader.shutdown(); }
        if (commandSender != null) { commandSender.disconnect(); }
        updateConnectionStatus(false);
    }

    private void updateConnectionStatus(boolean isConnected) {
        if (tvConnectionStatus != null) {
            tvConnectionStatus.setText(isConnected ? "脑波设备已连接" : "脑波设备未连接");
        }
    }

    private void updateTriggeredFrequency(int freq, double snr) {
        if (tvTriggeredFreq != null) {
            if (freq > 0) {
                tvTriggeredFreq.setText(String.format(Locale.US, "已触发%dHZ电位", freq));
                tvTriggeredFreq.setVisibility(View.VISIBLE);
            } else {
                tvTriggeredFreq.setVisibility(View.GONE);
            }
        }
    }
}