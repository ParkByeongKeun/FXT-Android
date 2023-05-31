package com.example.fxt.utils;

public class CustomDevice {

    private int img_name;
    private String title_1;
    private String title_2;
    private String title_3;
    private String title_4;
    private String title_5;

    public CustomDevice(int _img_name, String _title_1, String _title_2, String _title_3, String _title_4, String _title_5) {
        this.img_name = _img_name;
        this.title_1 = _title_1;
        this.title_2 = _title_2;
        this.title_3 = _title_3;
        this.title_4 = _title_4;
        this.title_5 = _title_5;
    }

    public Integer getImgName() {
        return this.img_name;
    }

    public String getTitle1() {
        return this.title_1;
    }

    public String getTitle2() {
        return this.title_2;
    }

    public String getTitle3() {
        return this.title_3;
    }

    public String getTitle4() {
        return this.title_4;
    }

    public String getTitle5() {
        return title_5;
    }
}
