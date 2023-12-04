package com.fiberfox.fxt.RestApi;

public class ResponseSendVerCode {
    private String msg;
    private int code;
    private String enmsg;
    private int smsId;
    private String verCode;

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return this.msg;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public void setEnmsg(String enmsg) {
        this.enmsg = enmsg;
    }

    public String getEnmsg() {
        return this.enmsg;
    }

    public void setSmsId(int smsId) {
        this.smsId = smsId;
    }

    public int getSmsId() {
        return this.smsId;
    }

    public void setVerCode(String verCode) {
        this.verCode = verCode;
    }

    public String getVerCode() {
        return this.verCode;
    }
}
