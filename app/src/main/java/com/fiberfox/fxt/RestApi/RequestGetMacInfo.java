package com.fiberfox.fxt.RestApi;

public class RequestGetMacInfo {
    private final String macSerial;
    private final String project;
    public RequestGetMacInfo(String macSerial, String project) {
        this.macSerial = macSerial;
        this.project = project;
    }
}
