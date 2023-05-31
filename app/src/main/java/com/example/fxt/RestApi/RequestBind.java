package com.example.fxt.RestApi;

public class RequestBind {
    private final String token;
    private final String macSerial;
    private final String project;
    public RequestBind(String token, String macSerial, String project) {
        this.token = token;
        this.macSerial = macSerial;
        this.project = project;
    }
}
