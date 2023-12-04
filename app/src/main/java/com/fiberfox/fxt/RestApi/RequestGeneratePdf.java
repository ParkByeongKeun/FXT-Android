package com.fiberfox.fxt.RestApi;

public class RequestGeneratePdf {
    private final String token;
    private final String ids;
    private final String remark;
    private final String dloc;
    private final String loc;
    private final int type;
    public RequestGeneratePdf(String token, String ids, String remark, String dloc, String loc, int type) {
        this.token = token;
        this.ids = ids;
        this.remark = remark;
        this.dloc = dloc;
        this.loc = loc;
        this.type = type;
    }
}
