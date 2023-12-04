package com.fiberfox.fxt.utils;

public class CustomMacList {

    private int img_name;
    private String title_1;
    private String title_2;

    public CustomMacList(int _img_name, String _title_1, String _title_2) {
        this.img_name = _img_name;
        this.title_1 = _title_1;
        this.title_2 = _title_2;
    }

    public int getImgName() {
        return this.img_name;
    }

    public String getTitle1() {
        return this.title_1;
    }

    public String getTitle2() {
        return this.title_2;
    }
}
