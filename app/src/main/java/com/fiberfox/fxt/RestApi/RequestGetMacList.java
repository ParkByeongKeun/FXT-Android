package com.fiberfox.fxt.RestApi;

public class RequestGetMacList {
    private final String project;
    private final String token;
    public RequestGetMacList(String project, String token) {
        this.project = project;
        this.token = token;
    }
}
