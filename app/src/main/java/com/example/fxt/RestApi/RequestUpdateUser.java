package com.example.fxt.RestApi;

public class RequestUpdateUser {
    private final String token;
    private final String name;
    private final String sex;
    private final String company;
    public RequestUpdateUser(String token, String name, String sex, String company) {
        this.token = token;
        this.name = name;
        this.sex = sex;
        this.company = company;
    }
}
