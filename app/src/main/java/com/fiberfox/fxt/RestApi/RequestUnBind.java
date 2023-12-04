package com.fiberfox.fxt.RestApi;

public class RequestUnBind {
    private final String token;
    private final String macSerial;
    private final String project;
    public RequestUnBind(String token, String macSerial, String project) {
        this.token = token;
        this.macSerial = macSerial;
        this.project = project;
    }
}
