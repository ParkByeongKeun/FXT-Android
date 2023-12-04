package com.fiberfox.fxt.RestApi;

import com.fiberfox.fxt.utils.MacInfoDO;

public class ResponseGetMacInfo {
    private String msg;
    private int code;
    private Object domain;
    private String enmsg;

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

    public void setDomain(MacInfoDO domain) {
        this.domain = domain;
    }

    public Object getDomain() {
        return this.domain;
    }
}
