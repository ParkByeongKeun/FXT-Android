package com.fiberfox.fxt.RestApi;

import com.fiberfox.fxt.utils.Basic;

public class ResponseGetBasic {
    private String msg;
    private int code;
    private String enmsg;
    private Object basic;


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

    public void setBasic(Basic basic) {
        this.basic = basic;
    }

    public Object getBasic() {
        return this.basic;
    }
}
