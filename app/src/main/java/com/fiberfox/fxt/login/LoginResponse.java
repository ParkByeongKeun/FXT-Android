package com.fiberfox.fxt.login;

public class LoginResponse {

    private boolean success;
    private String accessToken;
    private int errno;

    public void setSuccess(boolean success) {
        this.success = success;
    }
    public boolean getSuccess() {
        return success;
    }

    public int getErrNo() {
        return errno;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    public String getAccessToken() {
        return this.accessToken;
    }

}