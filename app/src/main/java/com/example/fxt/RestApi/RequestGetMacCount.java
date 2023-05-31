package com.example.fxt.RestApi;

public class RequestGetMacCount {
    private final String project;
    private final String token;
    public RequestGetMacCount(String project, String token) {
        this.project = project;
        this.token = token;
    }
}
