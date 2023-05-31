package com.example.fxt.RestApi;

public class ResponseUpdatePassword {
    private String msg;
    private int code;
    private String enmsg;
    private String token;

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

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return this.token;
    }
}
