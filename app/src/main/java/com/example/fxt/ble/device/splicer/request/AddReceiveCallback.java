package com.example.fxt.ble.device.splicer.request;

import com.example.fxt.ble.device.base.BleBaseRequest;
import com.example.fxt.ble.device.splicer.BleSplicerCallback;


public class AddReceiveCallback extends BleBaseRequest {

    public AddReceiveCallback(String address, BleSplicerCallback bleSplicerCallback) {
        super(address, bleSplicerCallback);
    }
}
