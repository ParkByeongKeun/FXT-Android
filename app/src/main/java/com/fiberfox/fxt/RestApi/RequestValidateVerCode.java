package com.fiberfox.fxt.RestApi;

public class RequestValidateVerCode {
    private final int smsId;
    private final String phone;
    private final String verCode;
    public RequestValidateVerCode(int smsId, String phone, String verCode) {
        this.smsId = smsId;
        this.phone = phone;
        this.verCode = verCode;
    }
}
