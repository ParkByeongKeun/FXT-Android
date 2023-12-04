package com.fiberfox.fxt.login;

public class ResetPasswordCheckResponse {

    private boolean success;
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

}