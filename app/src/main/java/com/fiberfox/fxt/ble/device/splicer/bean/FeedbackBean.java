package com.fiberfox.fxt.ble.device.splicer.bean;


import com.fiberfox.fxt.ble.api.event.base.BaseEvent;

public class FeedbackBean {

    private int errcode;

    public FeedbackBean() {
        this.errcode = BaseEvent.CODE_SUCCESS;
    }

    public FeedbackBean(int errcode) {
        this.errcode = errcode;
    }
}
