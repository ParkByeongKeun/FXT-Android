package com.example.fxt.RestApi;

public class RequestLogin {
    private final String phone;
    private final String password;
    public RequestLogin(String phone, String password) {
        this.phone = phone;
        this.password = password;
    }
}
