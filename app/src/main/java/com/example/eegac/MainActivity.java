// MainActivity.java
package com.example.eegac;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String HARD_CODED_MAC_HEADSET = "74:E5:43:BE:40:2F";
    // --- 新增：你的单片机的蓝牙MAC地址 ---
    private static final String HARD_CODED_MAC_MCU = "00:1A:FF:09:05:3F"; // 请确保这是你单片机的正确地址

    private BluetoothAdapter btAdapter;
    private HeadsetConnector connector;
    private NeuroSkyReader neuroReader;
    private ImageView imgSignal;
    private TextView tvFreq;
    private com.example.eegac.BlinkCornerView blinkView;

    // --- 新增：蓝牙指令发送器 ---
    private BluetoothCommandSender commandSender;

    // ... (FFT 和缓冲区的代码保持不变)
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
                NeuroRawData raw = (NeuroRawData) msg.obj;
                buffer[bufCount++] = raw.getRawWaveValue();
                if (bufCount >= WINDOW_SIZE) {
                    onWindowReady();
                    bufCount = 0;
                }
            } else if (msg.what == NeuroStreamParser.MESSAGE_READ_DIGEST_DATA_PACKET) {
                NeuroData data = (NeuroData) msg.obj;
                signalQuality = data.getSignal();
                updateSignalIcon(signalQuality);
            }
        }
    };

    /**
     * 当一个数据窗口准备好时被调用。
     * 这是所有脑电波信号处理和决策的核心。
     */
    private void onWindowReady() {
        for (int i = 0; i < WINDOW_SIZE; i++) { real[i] = buffer[i]; imag[i] = 0.0; }
        fft.doFFT(real, imag, false);
        fft.computeAmplitude(real, imag, amp);
        int peak = fft.findMaxIndex(amp, 1, 64);

        if (peak == lastPeak) {
            repeatCount++;
        } else {
            repeatCount = 1;
            lastPeak = peak;
        }
        if (repeatCount < 2) return;
        repeatCount = 0;

        int canonical = canonicalizePeak(peak);
        if (canonical <= 0) {
            updateFreqText("其他诱发电位");
            return;
        }
        updateFreqText(canonical + " Hz");

        // --- 核心修改：在这里发送指令 ---
        // 发送前检查是否已连接
        if (commandSender == null) {
            // 如果你希望在未连接时提醒用户，可以取消下面的注释
            // Toast.makeText(this, "蓝牙设备未连接", Toast.LENGTH_SHORT).show();
            return;
        }

        BlinkCornerView.State currentState = blinkView.getCurrentState();

        if (currentState == BlinkCornerView.State.MAIN_MENU) {
            switch (canonical) {
                case 6:
                    Toast.makeText(this, "触发：开(6Hz)", Toast.LENGTH_SHORT).show();
                    commandSender.sendCommand(AcCommands.CMD_POWER_ON);
                    break;
                case 7:
                    Toast.makeText(this, "切换UI：进入模式菜单", Toast.LENGTH_SHORT).show();
                    blinkView.setState(BlinkCornerView.State.MODE_MENU);
                    break;
                case 9:
                    Toast.makeText(this, "触发：关(9Hz)", Toast.LENGTH_SHORT).show();
                    commandSender.sendCommand(AcCommands.CMD_POWER_OFF);
                    break;
                case 11:
                    Toast.makeText(this, "切换UI：进入温度菜单", Toast.LENGTH_SHORT).show();
                    blinkView.setState(BlinkCornerView.State.TEMP_MENU);
                    break;
            }
        } else if (currentState == BlinkCornerView.State.TEMP_MENU) {
            switch (canonical) {
                case 6:
                    Toast.makeText(this, "触发：温度+(6Hz)", Toast.LENGTH_SHORT).show();
                    commandSender.sendCommand(AcCommands.CMD_TEMP_UP);
                    break;
                case 9:
                    Toast.makeText(this, "触发：温度-(9Hz)", Toast.LENGTH_SHORT).show();
                    commandSender.sendCommand(AcCommands.CMD_TEMP_DOWN);
                    break;
                case 11:
                    Toast.makeText(this, "切换UI：返回主菜单", Toast.LENGTH_SHORT).show();
                    blinkView.setState(BlinkCornerView.State.MAIN_MENU);
                    break;
            }
        } else if (currentState == BlinkCornerView.State.MODE_MENU) {
            switch (canonical) {
                case 6:
                    Toast.makeText(this, "触发：制冷(6Hz)", Toast.LENGTH_SHORT).show();
                    commandSender.sendCommand(AcCommands.CMD_MODE_COOL);
                    break;
                case 7:
                    Toast.makeText(this, "触发：除湿(7Hz)", Toast.LENGTH_SHORT).show();
                    commandSender.sendCommand(AcCommands.CMD_MODE_DRY);
                    break;
                case 9:
                    Toast.makeText(this, "触发：制热(9Hz)", Toast.LENGTH_SHORT).show();
                    commandSender.sendCommand(AcCommands.CMD_MODE_HEAT);
                    break;
                case 11:
                    Toast.makeText(this, "切换UI：返回主菜单", Toast.LENGTH_SHORT).show();
                    blinkView.setState(BlinkCornerView.State.MAIN_MENU);
                    break;
            }
        }
    }

    private ActivityResultLauncher<String> btConnectPermissionLauncher;
    private ActivityResultLauncher<Intent> enableBtLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        connector = new HeadsetConnector(this);

        btConnectPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                ensureBluetoothEnabledThenConnect();
            } else {
                Toast.makeText(this, "缺少蓝牙连接权限", Toast.LENGTH_LONG).show();
            }
        });

        enableBtLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                doConnectHeadset();
            } else {
                Toast.makeText(this, "蓝牙未开启", Toast.LENGTH_LONG).show();
            }
        });

        View btnConnect = findViewById(R.id.btn_connect);
        if (btnConnect != null) {
            btnConnect.setOnClickListener(v -> showBottomMenu());
        }

        imgSignal = findViewById(R.id.img_signal);
        tvFreq = findViewById(R.id.tv_freq);
        blinkView = findViewById(R.id.blink_view);
    }

    private void showBottomMenu() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View content = LayoutInflater.from(this).inflate(R.layout.layout_bottom_menu, null);
        dialog.setContentView(content);

        MaterialButton btnConnectHeadset = content.findViewById(R.id.btn_connect_headset);
        MaterialButton btnConnectBluetooth = content.findViewById(R.id.btn_connect_bluetooth);
        // ... (测试按钮的代码保持不变)

        btnConnectHeadset.setOnClickListener(v -> {
            dialog.dismiss();
            Toast.makeText(this, "开始连接耳机", Toast.LENGTH_SHORT).show();
            connectHeadset();
        });

        btnConnectBluetooth.setOnClickListener(v -> {
            dialog.dismiss();
            Toast.makeText(this, "开始连接单片机", Toast.LENGTH_SHORT).show();
            connectBluetoothDevice(HARD_CODED_MAC_MCU);
        });

        dialog.show();
    }

    private void connectBluetoothDevice(String mac) {
        // ... (权限检查和蓝牙开启的代码保持不变)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                btConnectPermissionLauncher.launch(android.Manifest.permission.BLUETOOTH_CONNECT);
                return;
            }
        }
        if (btAdapter == null) { Toast.makeText(this, "此设备不支持蓝牙", Toast.LENGTH_LONG).show(); return; }
        if (!btAdapter.isEnabled()) {
            enableBtLauncher.launch(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
            return;
        }

        // --- 核心修改：连接成功后，初始化 commandSender ---
        connector.connectHardcoded(mac, new HeadsetConnector.Listener() {
            @Override
            public void onConnected(android.bluetooth.BluetoothSocket socket) {
                Toast.makeText(MainActivity.this, "单片机连接成功", Toast.LENGTH_LONG).show();
                try {
                    // 如果之前有连接，先断开
                    if (commandSender != null) {
                        commandSender.disconnect();
                    }
                    commandSender = new BluetoothCommandSender(socket);
                } catch (IOException e) {
                    Log.e("MainActivity", "Failed to create command sender", e);
                    Toast.makeText(MainActivity.this, "创建指令发送器失败", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String message, Throwable t) {
                String extra = (t != null && t.getMessage() != null) ? (":" + t.getMessage()) : "";
                Toast.makeText(MainActivity.this, "单片机连接失败: " + message + extra, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void connectHeadset() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                btConnectPermissionLauncher.launch(android.Manifest.permission.BLUETOOTH_CONNECT);
                return;
            }
        }
        ensureBluetoothEnabledThenConnect();
    }

    private void ensureBluetoothEnabledThenConnect() {
        if (btAdapter == null) { Toast.makeText(this, "此设备不支持蓝牙", Toast.LENGTH_LONG).show(); return; }
        if (!btAdapter.isEnabled()) {
            enableBtLauncher.launch(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
            return;
        }
        doConnectHeadset();
    }

    private void doConnectHeadset() {
        connector.connectHardcoded(HARD_CODED_MAC_HEADSET, new HeadsetConnector.Listener() {
            @Override
            public void onConnected(BluetoothSocket socket) {
                Toast.makeText(MainActivity.this, "耳机连接成功", Toast.LENGTH_LONG).show();
                if (neuroReader != null) {
                    neuroReader.shutdown();
                }
                neuroReader = new NeuroSkyReader(socket, eegHandler);
                neuroReader.start();
            }

            @Override
            public void onError(String message, Throwable t) {
                String extra = (t != null && t.getMessage() != null) ? (":" + t.getMessage()) : "";
                Toast.makeText(MainActivity.this, "耳机连接失败: " + message + extra, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (neuroReader != null) {
            neuroReader.shutdown();
            neuroReader = null;
        }
        // --- 新增：关闭蓝牙连接 ---
        if (commandSender != null) {
            commandSender.disconnect();
            commandSender = null;
        }
    }

    // ... (updateFreqText, canonicalizePeak, updateSignalIcon 方法保持不变)
    private void updateFreqText(String text) { if (tvFreq != null) tvFreq.setText(text); }
    private int canonicalizePeak(int peak) {
        if (peak == 6 || peak == 12) return 6;
        if (peak == 7 || peak == 14) return 7;
        if (peak == 9 || peak == 18) return 9;
        if (peak == 11 || peak == 22) return 11;
        return -1;
    }
    private void updateSignalIcon(int signal) {
        if (imgSignal == null) return;
        if (signal == 0) imgSignal.setImageResource(R.drawable.greenlight);
        else if (signal == 200) imgSignal.setImageResource(R.drawable.redlight);
        else imgSignal.setImageResource(R.drawable.yellowlight);
    }
}