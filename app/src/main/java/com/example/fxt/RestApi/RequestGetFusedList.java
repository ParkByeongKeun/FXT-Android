package com.example.fxt.RestApi;

public class RequestGetFusedList {
    private final String macSerial;
    private final String project;
    private final String offset;
    private final String getType;
    public RequestGetFusedList(String macSerial, String project, String offset, String getType) {
        this.macSerial = macSerial;
        this.project = project;
        this.offset = offset;
        this.getType = getType;
    }
}
