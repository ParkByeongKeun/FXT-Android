package com.example.fxt.RestApi;

public class RequestSetFusedInfo {
    private final int id;
    private final String loc;
    private final String dloc;
    private final String remark;
    public RequestSetFusedInfo(int id, String loc, String dloc, String remark) {
        this.id = id;
        this.loc = loc;
        this.dloc = dloc;
        this.remark = remark;
    }
}
