package com.example.fxt.RestApi;

public class RequestUploadHead {
    private final String image;
    private final String token;
    public RequestUploadHead(String image, String token) {
        this.image = image;
        this.token = token;
    }
}
