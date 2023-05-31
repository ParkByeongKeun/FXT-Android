package com.example.fxt.RestApi;

public class ResponseUploadHead {
    private String msg;
    private int code;
    private String enmsg;
    private String url;

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

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return this.url;
    }
}
