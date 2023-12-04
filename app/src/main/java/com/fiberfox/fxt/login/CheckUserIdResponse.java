package com.fiberfox.fxt.login;

public class CheckUserIdResponse {
    private final boolean success;
    private int errno;
    private boolean isExist;

    public CheckUserIdResponse(boolean success) {
        this.success = success;
    }

    public boolean getSuccess() {
        return success;
    }

    public int getErrNo() {
        return errno;
    }

    public boolean getIsExist() {
        return isExist;
    }
}
