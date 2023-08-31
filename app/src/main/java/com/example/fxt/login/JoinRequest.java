package com.example.fxt.login;

public class JoinRequest {
    private final String userId;
    private final String userPw;
    private final String userRePw;
    private final String userName;


    public JoinRequest(String userId, String userPw, String userRePw, String userName) {
        this.userId = userId;
        this.userPw = userPw;
        this.userRePw = userRePw;
        this.userName = userName;
    }
}
