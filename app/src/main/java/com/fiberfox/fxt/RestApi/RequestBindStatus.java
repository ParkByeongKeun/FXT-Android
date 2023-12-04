package com.fiberfox.fxt.RestApi;

public class RequestBindStatus {
    private final String macSerial;
    private final String project;
    public RequestBindStatus(String macSerial, String project) {
        this.macSerial = macSerial;
        this.project = project;
    }
}
