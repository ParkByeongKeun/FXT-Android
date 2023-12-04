package com.fiberfox.fxt.RestApi;

/*
* ******************
* type
* 0 / all
* 1 / 유지보수
* 2 / 작업
* 3 / 고장 처리
* ******************
* */

public class RequestGetVideoList {
    private final String type;
    private final String title;
    public RequestGetVideoList(String type, String title) {
        this.type = type;
        this.title = title;
    }
}
