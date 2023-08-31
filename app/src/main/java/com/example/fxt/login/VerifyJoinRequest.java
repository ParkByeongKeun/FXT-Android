package com.example.fxt.login;

public class VerifyJoinRequest {
    private final String userId;
    private final String verificationCode;
    public VerifyJoinRequest(String userId, String verificationCode) {
        this.userId = userId;
        this.verificationCode = verificationCode;
    }
}
