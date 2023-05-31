package com.example.fxt.ble.api.event;


import com.example.fxt.ble.api.bean.BleResultBean;
import com.example.fxt.ble.api.event.base.BaseEvent;


public class ResultEvent extends BaseEvent {

    protected int code;
    protected String msg;
    protected BleResultBean resultBean;

    public ResultEvent() {
        this.code = CODE_SUCCESS;
        this.msg = null;
    }

    public ResultEvent(byte[] rawValue) {
        this();
        resultBean = new BleResultBean(rawValue);
    }

    public ResultEvent(int code, String msg) {
        this.code = code;
        this.msg = msg;
        resultBean = new BleResultBean();
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public BleResultBean getResultBean() {
        return resultBean;
    }

    public void setResultBean(BleResultBean resultBean) {
        this.resultBean = resultBean;
    }
}
