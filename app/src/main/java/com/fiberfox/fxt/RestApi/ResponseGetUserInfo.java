package com.fiberfox.fxt.RestApi;

import com.fiberfox.fxt.utils.User;

public class ResponseGetUserInfo {

    private String msg;
    private int code;
    private String enmsg;
    private Object user;

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

    public void setUser(User user) {
        this.user = user;
    }

    public Object getUser() {
        return this.user;
    }
}
