package com.fiberfox.fxt.utils;

public class User {
    Double id;
    String urName;
    String urHeadUrl;
    String urSex;
    String urPhone;
    String urCompany;
    String urPassword;
    String token;
    String isDisplay;
    String createdTime;
    String updatedTime;


    public User(Double id, String urName, String urHeadUrl, String urSex, String urPhone
            , String urCompany, String urPassword, String token, String isDisplay
            , String createdTime, String updatedTime) {
        this.id = id;
        this.urName = urName;
        this.urHeadUrl = urHeadUrl;
        this.urSex = urSex;
        this.urPhone = urPhone;
        this.urCompany = urCompany;
        this.urPassword = urPassword;
        this.token = token;
        this.isDisplay = isDisplay;
        this.createdTime = createdTime;
        this.updatedTime = updatedTime;
    }

    public void setName(String name) {
        this.urName = name;
    }
    public String getName() {
        return urName;
    }

    public void setId(Double id) {
        this.id = id;
    }
    public Double getId() {
        return id;
    }

    public void setHeadUrl(String headUrl) {
        this.urHeadUrl = headUrl;
    }
    public String getHeadUrl() {
        return this.urHeadUrl;
    }

    public void setSex(String sex) {
        this.urSex = sex;
    }

    public String getSex() {
        return this.urSex;
    }

    public void setPhone(String phone) {
        this.urPhone = phone;
    }

    public String getPhone() {
        return this.urPhone;
    }

    public void setCompany(String company) {
        this.urCompany = company;
    }

    public String getCompany() {
        return urCompany;
    }

    public void setPassword(String password) {
        this.urPassword = password;
    }

    public String getPassword() {
        return urPassword;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setIsDisplay(String isDisplay) {
        this.isDisplay = isDisplay;
    }

    public String getIsDisplay() {
        return isDisplay;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setUpdatedTime(String updatedTime) {
        this.updatedTime = updatedTime;
    }

    public String getUpdatedTime() {
        return updatedTime;
    }
}