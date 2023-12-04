package com.fiberfox.fxt.RestApi;

public class RequestRegister {
    private final String phone;
    private final String password;
    public RequestRegister(String phone, String password) {
        this.phone = phone;
        this.password = password;
    }
}
