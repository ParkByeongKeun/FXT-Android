package com.fiberfox.fxt.RestApi;

public class RequestUpdatePassword {
    private final String phone;
    private final String password;
    public RequestUpdatePassword(String phone, String password) {
        this.phone = phone;
        this.password = password;
    }
}
