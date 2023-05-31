package com.example.fxt.RestApi;

/*
* ******************
* type
* 1 / FiberFox 회사 소개
* 2 / 파일 도움말 문서
* 3 / App 버전 정보
* ******************
*
* */
public class RequestGetBasic {
    private final String type;
    public RequestGetBasic(String type) {
        this.type = type;
    }
}
