package com.fiberfox.fxt.ble.device.splicer.request;

import com.fiberfox.fxt.ble.device.base.BleBaseRequest;
import com.fiberfox.fxt.ble.device.splicer.BleSplicerCallback;


public class AddReceiveCallback extends BleBaseRequest {

    public AddReceiveCallback(String address, BleSplicerCallback bleSplicerCallback) {
        super(address, bleSplicerCallback);
    }

    @Override
    public void onSend(String str) {

    }
}
