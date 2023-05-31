package com.example.fxt.ble.api.event.base;

public class BaseEvent {

    public static final int CODE_ERROR_NOT_INIT = -1;
    public static final int CODE_ERROR_DISCONNECT = -3;
    public static final int CODE_ERROR_NOT_CONNECT = -31;
    public static final int CODE_ERROR_TIMEOUT = -4;
    public static final int CODE_ERROR_CONNECTION_TIMEOUT = -41;
    public static final int CODE_ERROR_SEND_TIMEOUT = -42;
    public static final int CODE_ERROR_NOTIFY_TIMEOUT = -43;
    public static final int CODE_ERROR_CMD_FORMAT = -5;
    public static final int CODE_ERROR_SEND = -6;
    public static final int CODE_ERROR_ADDRESS = -7;
    public static final int CODE_ERROR_ADDRESS_BOOTLOADER = -71;

    public static final int CODE_SUCCESS = 0;
    public static final int CODE_FAIL = 1;


    // 连接ble
    public static final int CODE_CONNECT_SUCCESS = -11;
    public static final int CODE_CONNECT_FAIL = -10;

    // 接收熔接数据，数据长度不对
    public static final int CODE_RECEIVE_DATA_LENGTH_ERROR = -12;
}
