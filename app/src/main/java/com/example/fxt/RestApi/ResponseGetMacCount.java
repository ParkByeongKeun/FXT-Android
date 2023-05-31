package com.example.fxt.RestApi;

public class ResponseGetMacCount {
    private String msg;
    private int code;
    private String enmsg;
    private int count;

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

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return this.count;
    }
}
