package com.fiberfox.fxt.utils;

public class MacInfoDO {
    String macSerial;
    String project;
    String bind;
    String heartBeat;
    String appVer;
    String curArcs;
    String totArcs;
    String curSplcs;
    String curAvgLoss;

    String updatedTime;
    String dloc;
    String loc;


    public MacInfoDO(String macSerial, String project, String bind, String heartBeat
            , String appVer, String curArcs, String totArcs, String curSplcs
            , String curAvgLoss, String updatedTime, String dloc, String loc) {
        this.macSerial = macSerial;
        this.project = project;
        this.bind = bind;
        this.heartBeat = heartBeat;
        this.appVer = appVer;
        this.curArcs = curArcs;
        this.totArcs = totArcs;
        this.curSplcs = curSplcs;
        this.curAvgLoss = curAvgLoss;
        this.updatedTime = updatedTime;
        this.dloc = dloc;
        this.loc = loc;
    }

    public void setMacSerial(String macSerial) {
        this.macSerial = macSerial;
    }

    public String getMacSerial() {
        return macSerial;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getProject() {
        return project;
    }

    public void setBind(String bind) {
        this.bind = bind;
    }

    public String getBind() {
        return bind;
    }

    public void setHeartBeat(String heartBeat) {
        this.heartBeat = heartBeat;
    }

    public String getHeartBeat() {
        return heartBeat;
    }

    public void setAppVer(String appVer) {
        this.appVer = appVer;
    }

    public String getAppVer() {
        return appVer;
    }

    public void setCurArcs(String curArcs) {
        this.curArcs = curArcs;
    }

    public String getCurArcs() {
        return curArcs;
    }

    public void setCurAvgLoss(String curAvgLoss) {
        this.curAvgLoss = curAvgLoss;
    }

    public String getCurAvgLoss() {
        return curAvgLoss;
    }

    public void setCurSplcs(String curSplcs) {
        this.curSplcs = curSplcs;
    }

    public String getCurSplcs() {
        return curSplcs;
    }

    public void setTotArcs(String totArcs) {
        this.totArcs = totArcs;
    }

    public String getTotArcs() {
        return totArcs;
    }

    public void setUpdatedTime(String updatedTime) {
        this.updatedTime = updatedTime;
    }
    public String getUpdatedTime() {
        return updatedTime;
    }

    public void setDloc(String dloc) {
        this.dloc = dloc;
    }

    public String getDloc() {
        return dloc;
    }

    public void setLoc(String loc) {
        this.loc = loc;
    }

    public String getLoc() {
        return loc;
    }
}
