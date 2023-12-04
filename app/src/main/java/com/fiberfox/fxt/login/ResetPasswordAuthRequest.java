package com.fiberfox.fxt.login;

public class ResetPasswordAuthRequest {
    private final String userId;
    private final String password;
    private final String verificationCode;

    public ResetPasswordAuthRequest(String userId, String password, String verificationCode) {
        this.userId = userId;
        this.password = password;
        this.verificationCode = verificationCode;
    }
}
