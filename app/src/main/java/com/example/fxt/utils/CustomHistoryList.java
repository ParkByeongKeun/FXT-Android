package com.example.fxt.utils;

public class CustomHistoryList {

    private boolean isShare;
    private String title_1;
    private String title_2;

    public CustomHistoryList(boolean _isShare, String _title_1, String _title_2) {
        this.isShare = _isShare;
        this.title_1 = _title_1;
        this.title_2 = _title_2;
    }

    public Boolean getIsShare() {
        return this.isShare;
    }

    public String getTitle1() {
        return this.title_1;
    }

    public String getTitle2() {
        return this.title_2;
    }
}
