// AcCommands.java
package com.example.eegac;

/**
 * 定义发送给轮椅单片机的蓝牙指令集。
 * 使用单个字节作为指令。
 */
public final class AcCommands {

    // --- 修改点：简化为四个核心指令 ---
    public static final byte CMD_FORWARD      = 0x11; // 前进 (左上)
    public static final byte CMD_BACKWARD     = 0x13; // 后退 (右下)
    public static final byte CMD_TURN_LEFT    = 0x14; // 左转 (左下)
    public static final byte CMD_TURN_RIGHT   = 0x16; // 右转 (右上)

    // 保留一个停止指令
    public static final byte CMD_STOP         = 0x00;
}