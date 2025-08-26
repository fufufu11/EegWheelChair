// AcCommands.java
package com.example.eegac;

/**
 * 定义发送给轮椅单片机的蓝牙指令集。
 * 使用单个字节作为指令。
 */
public final class AcCommands {

    // --- 新的指令集 ---
    public static final byte CMD_FORWARD      = 0x11; // F (前进)
    public static final byte CMD_SPEED_UP     = 0x12; // + (加速)
    public static final byte CMD_BACKWARD     = 0x13; // B (后退)
    public static final byte CMD_TURN_LEFT    = 0x14; // L (左转)
    public static final byte CMD_SPEED_DOWN   = 0x15; // - (减速)
    public static final byte CMD_TURN_RIGHT   = 0x16; // R (右转)

    // 你可以保留一个停止指令，以防万一
    public static final byte CMD_STOP         = 0x00; // 停止
}