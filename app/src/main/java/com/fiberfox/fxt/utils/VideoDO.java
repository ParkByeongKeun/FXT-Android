package com.fiberfox.fxt.utils;

public class VideoDO {
    Double id;
    String voTitle;
    String enTitle;
    String voType;
    String voImgUrl;
    String voVideoUrl;
    String enVideoUrl;
    String voDetails;
    String enDetails;
    String isDisplay;
    String createdTime;
    String updatedTime;

    public VideoDO(Double id, String voTitle, String enTitle, String voType, String voImgUrl,String voVideoUrl
            , String enVideoUrl, String voDetails, String enDetails,String isDisplay, String createdTime, String updatedTime) {
        this.id = id;
        this.voTitle = voTitle;
        this.enTitle = enTitle;
        this.voType = voType;
        this.voImgUrl = voImgUrl;
        this.voVideoUrl = voVideoUrl;
        this.enVideoUrl = enVideoUrl;
        this.voDetails = voDetails;
        this.enDetails = enDetails;
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

    public void setVoTitle(String voTitle) {
        this.voTitle = voTitle;
    }

    public String getVoTitle() {
        return voTitle;
    }

    public void setEnTitle(String enTitle) {
        this.enTitle = enTitle;
    }

    public String getEnTitle() {
        return enTitle;
    }

    public void setVoType(String voType) {
        this.voType = voType;
    }

    public String getVoType() {
        return voType;
    }

    public void setVoImgUrl(String voImgUrl) {
        this.voImgUrl = voImgUrl;
    }

    public String getVoImgUrl() {
        return voImgUrl;
    }

    public void setVoVideoUrl(String voVideoUrl) {
        this.voVideoUrl = voVideoUrl;
    }

    public String getVoVideoUrl() {
        return voVideoUrl;
    }

    public void setEnVideoUrl(String enVideoUrl) {
        this.enVideoUrl = enVideoUrl;
    }

    public String getEnVideoUrl() {
        return enVideoUrl;
    }

    public void setVoDetails(String voDetails) {
        this.voDetails = voDetails;
    }

    public String getVoDetails() {
        return voDetails;
    }

    public void setEnDetails(String enDetails) {
        this.enDetails = enDetails;
    }

    public String getEnDetails() {
        return enDetails;
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