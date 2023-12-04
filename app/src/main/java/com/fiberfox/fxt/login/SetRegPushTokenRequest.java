package com.fiberfox.fxt.login;

public class SetRegPushTokenRequest {
    private final String UUID;
    private final String pushToken;
    public SetRegPushTokenRequest(String UUID, String pushToken) {
        this.UUID = UUID;
        this.pushToken = pushToken;
    }
}
