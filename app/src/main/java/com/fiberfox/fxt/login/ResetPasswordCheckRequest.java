package com.fiberfox.fxt.login;

public class ResetPasswordCheckRequest {
    private final String userId;
    private final String verificationCode;

    public ResetPasswordCheckRequest(String userId, String verificationCode) {
        this.userId = userId;
        this.verificationCode = verificationCode;
    }
}
