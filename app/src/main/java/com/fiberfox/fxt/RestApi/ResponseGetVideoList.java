package com.fiberfox.fxt.RestApi;

import java.util.ArrayList;

public class ResponseGetVideoList {
    private String msg;
    private int code;
    private String enmsg;
    private ArrayList<Object> videoList;

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

    public void setVideoList(ArrayList<Object> videoList) {
        this.videoList = videoList;
    }

    public ArrayList<Object> getVideoList() {
        return videoList;
    }
}
