package com.example.fxt.ble.device.splicer.bean;


import static com.example.fxt.ble.api.event.base.BaseEvent.CODE_SUCCESS;

public class FeedbackBean {

    private int errcode;

    public FeedbackBean() {
        this.errcode = CODE_SUCCESS;
    }

    public FeedbackBean(int errcode) {
        this.errcode = errcode;
    }
}
