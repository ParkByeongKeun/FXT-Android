package com.fiberfox.fxt.RestApi;

public class RequestDeleteRecord {
    private final String token;
    private final String ids;
    public RequestDeleteRecord(String token, String ids) {
        this.token = token;
        this.ids = ids;
    }
}
