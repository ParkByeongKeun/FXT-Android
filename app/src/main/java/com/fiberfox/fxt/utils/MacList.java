package com.fiberfox.fxt.utils;

public class MacList {
    Double id;
    Double bdUserId;
    Double bdDeviceId;
    String project;
    MacInfoDO macInfoDO;
    String isDisplay;
    String createdTime;
    String updatedTime;


    public MacList(Double id, Double bdUserId, Double bdDeviceId, String project, MacInfoDO macInfoDO,String isDisplay
            , String createdTime, String updatedTime) {
        this.id = id;
        this.bdUserId = bdUserId;
        this.bdDeviceId = bdDeviceId;
        this.project = project;
        this.macInfoDO = macInfoDO;
        this.isDisplay = isDisplay;
        this.createdTime = createdTime;
        this.updatedTime = updatedTime;
    }

    public void setId(Double id) {
        this.id = id;
    }

    public Double getId() {
        return id;
    }

    public void setBdUserId(Double bdUserId) {
        this.bdUserId = bdUserId;
    }

    public Double getBdUserId() {
        return bdUserId;
    }

    public void setBdDeviceId(Double bdDeviceId) {
        this.bdDeviceId = bdDeviceId;
    }

    public Double getBdDeviceId() {
        return bdDeviceId;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getProject() {
        return project;
    }

    public void setMacInfoDO(MacInfoDO macInfoDO) {
        this.macInfoDO = macInfoDO;
    }

    public MacInfoDO getMacInfoDO() {
        return macInfoDO;
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
