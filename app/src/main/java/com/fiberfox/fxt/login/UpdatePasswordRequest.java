package com.fiberfox.fxt.login;

public class UpdatePasswordRequest {
    private final String userPw;
    private final String userPwNew;


    public UpdatePasswordRequest(String userPw, String userPwNew) {
        this.userPw = userPw;
        this.userPwNew = userPwNew;
    }
}
