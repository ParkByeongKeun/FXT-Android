package com.fiberfox.fxt.RestApi;

public class RequestSetLocMacInfo {
    private final String macSerial;
    private final String project;
    private final String loc;
    private final String dloc;
    public RequestSetLocMacInfo(String macSerial, String project, String loc, String dloc) {
        this.macSerial = macSerial;
        this.project = project;
        this.loc = loc;
        this.dloc = dloc;
    }
}
