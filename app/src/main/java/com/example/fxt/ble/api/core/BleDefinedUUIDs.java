package com.example.fxt.ble.api.core;

import java.util.UUID;

/**
 * 不同的ble设备的UUID不相同
 */
public class BleDefinedUUIDs {

    private BleDefinedUUIDs(){}

    /**
     * 100HA 熔接机
     */
    static String spliceUUID = "0000a002-0000-1000-8000-00805f9b34fb";

    static class SpliceCharacter{
        private SpliceCharacter(){}

        static final UUID WRITE = UUID.fromString("0000c304-0000-1000-8000-00805f9b34fb");

        static final UUID NOTIFY = UUID.fromString("0000c305-0000-1000-8000-00805f9b34fb");

    }
}
