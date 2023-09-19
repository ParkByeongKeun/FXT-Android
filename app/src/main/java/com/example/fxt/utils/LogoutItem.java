package com.example.fxt.utils;

public class LogoutItem {
    private String key;
    private String addr;
    private String date;
    public LogoutItem(String key, String addr, String date) {
        this.key = key;
        this.addr = addr;
        this.date = date;
    }

    public String getAddr() {
        return addr;
    }

    public String getDate() {
        return date;
    }

    public String getKey() {
        return key;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
