package com.example.fxt.ble.api.event;

import com.example.fxt.ble.api.bean.BleCmdBean;
import com.example.fxt.ble.api.event.base.BaseEvent;


public class WriteEvent extends BaseEvent {

    protected BleCmdBean mBleCmdBean;
    protected int code;
    protected String msg;

    /**
     * 写入成功的情况
     */
    public WriteEvent(BleCmdBean bleCmdBean) {
        this.code = CODE_SUCCESS;
        this.msg = null;
        this.mBleCmdBean = bleCmdBean;
    }

    /**
     * 写入失败时
     */
    public WriteEvent(BleCmdBean bleCmdBean, int code, String msg) {
        this(bleCmdBean);
        this.code = code;
        this.msg = msg;
    }

    public BleCmdBean getBleCmdBean() {
        return mBleCmdBean;
    }

    public void setBleCmdBean(BleCmdBean bleCmdBean) {
        mBleCmdBean = bleCmdBean;
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

    /**
     * 获取type，不存在则返回0xff
     */
    public byte getType(){
        if (mBleCmdBean!=null){
            return mBleCmdBean.getType();
        }else{
            return (byte) 0xff;
        }
    }
}
