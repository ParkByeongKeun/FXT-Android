package com.fiberfox.fxt.utils;

public class DocList {

    private boolean isPDF;
    private boolean isExcel;

    public DocList(boolean isPDF, boolean isExcel) {
        this.isPDF = isPDF;
        this.isExcel = isExcel;
    }

    public Boolean getisPDF() {
        return this.isPDF;
    }

    public Boolean getisExcel() {
        return this.isExcel;
    }

    public void setisPDF(Boolean isPDF) {
        this.isPDF = isPDF;
    }

    public void getisExcel(Boolean isExcel) {
        this.isExcel = isExcel;
    }
}
