package com.example.fxt.RestApi;

/*
* ******************
* getType
* 1 / Record 용접기록
* 2 / pdf 문서
* 3 / Excel 문서
* ******************
* */

import java.util.ArrayList;

public class ResponseGetFusedList {
    private String msg;
    private int code;
    private String enmsg;
    private ArrayList<Object> list;

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

    public void setList(ArrayList<Object> list) {
        this.list = list;
    }

    public ArrayList<Object> getList() {
        return this.list;
    }
}
