package com.fiberfox.fxt.login;

public class LoginRequest {
    private final String userId;
    private final String userPw;

    public LoginRequest(String userId, String userPw) {
        this.userId = userId;
        this.userPw = userPw;
    }
}
