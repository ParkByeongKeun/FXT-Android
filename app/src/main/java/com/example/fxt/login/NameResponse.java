package com.example.fxt.login;

public class NameResponse {

    private boolean success;
    private String name;

    public void setSuccess(boolean success) {
        this.success = success;
    }
    public boolean getSuccess() {
        return success;
    }

    public String getName() {
        return this.name;
    }
}