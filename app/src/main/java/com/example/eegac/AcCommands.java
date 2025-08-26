// AcCommands.java
package com.example.eegac;

/**
 * 定义发送给空调单片机的蓝牙指令集。
 * 使用单个字节作为指令，效率高，便于单片机解析。
 */
public final class AcCommands {

    // 主菜单指令
    public static final byte CMD_POWER_ON  = 0x01; // 开
    public static final byte CMD_POWER_OFF = 0x02; // 关

    // 温度菜单指令
    public static final byte CMD_TEMP_UP   = 0x03; // 温度+
    public static final byte CMD_TEMP_DOWN = 0x04; // 温度-

    // 模式菜单指令
    public static final byte CMD_MODE_COOL = 0x05; // 制冷
    public static final byte CMD_MODE_HEAT = 0x06; // 制热
    public static final byte CMD_MODE_DRY  = 0x07; // 除湿
}//