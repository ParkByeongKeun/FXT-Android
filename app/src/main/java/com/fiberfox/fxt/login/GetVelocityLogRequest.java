package com.fiberfox.fxt.login;

public class GetVelocityLogRequest {
    private final String serial;
    private final String cursor;
    private final String count;
    public GetVelocityLogRequest(String serial,String cursor,String count) {
        this.serial = serial;
        this.cursor = cursor;
        this.count = count;
    }
}
