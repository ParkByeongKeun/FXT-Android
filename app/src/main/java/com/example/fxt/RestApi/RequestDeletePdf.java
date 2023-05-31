package com.example.fxt.RestApi;

public class RequestDeletePdf {
    private final String token;
    private final String ids;
    private final int type;
    public RequestDeletePdf(String token, String ids, int type) {
        this.token = token;
        this.ids = ids;
        this.type = type;
    }
}
