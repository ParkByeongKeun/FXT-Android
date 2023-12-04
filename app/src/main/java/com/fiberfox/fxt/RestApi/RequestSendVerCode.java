package com.fiberfox.fxt.RestApi;

/*
* ******************
* action
* 0 / 계정신청
* 1 / 비번 찾기
* 2 / 비번 수정
*
* ******************
* * ******************
 * type
 * 1 / 국제 메시지
 *
 * ******************
* */
public class RequestSendVerCode {
    private final String phone;
    private final String action;
    private final String type;
    public RequestSendVerCode(String phone, String action, String type) {
        this.phone = phone;
        this.action = action;
        this.type = type;
    }
}
