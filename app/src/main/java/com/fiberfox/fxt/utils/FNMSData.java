package com.fiberfox.fxt.utils;

public class FNMSData {

    private String left;
    private String right;

    public FNMSData(String left, String right) {
        this.left = left;
        this.right = right;
    }

    public void setRight(String right) {
        this.right = right;
    }

    public String getRight() {
        return right;
    }

    public void setLeft(String left) {
        this.left = left;
    }

    public String getLeft() {
        return left;
    }
}
