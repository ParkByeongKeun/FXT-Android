package com.example.fxt.ble.device.splicer.bean;

import java.io.Serializable;
import java.util.Date;

/**
 * 设备信息
 */
public class SpliceDataBean implements Serializable {

    private static final long serialVersionUID = 7946091730863005408L;

    /**
     * 上报的数据id
     */
    private String id;

    /**
     * 设备序列号
     */
    private String sn;

    /**
     * 设备当前熔接次数
     */
    private String currentArcCount;

    /**
     * 设备总熔接次数
     */
    private String totalArcCount;

    /**
     * 软件版本 SW Version
     */
    private String appVer;

    /**
     * FPGA版本
     */
    private String fpgaVer;

    /**
     * 制造商
     */
    private String manufacturer;

    /**
     * 设备型号
     */
    private String model;

    /**
     * 设备品牌
     */
    private String brand;

    /**
     * 熔接模式 Splice Mode
     */
    private String spliceName;

    /**
     * 熔接时间
     */
    private String dataTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 熔接信息
     */
    private FiberBean fiberBean;

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSn() {
        return this.sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getCurrentArcCount() {
        return currentArcCount;
    }

    public void setCurrentArcCount(String currentArcCount) {
        this.currentArcCount = currentArcCount;
    }

    public String getTotalArcCount() {
        return totalArcCount;
    }

    public void setTotalArcCount(String totalArcCount) {
        this.totalArcCount = totalArcCount;
    }

    public String getAppVer() {
        return this.appVer;
    }

    public void setAppVer(String appVer) {
        this.appVer = appVer;
    }

    public String getFpgaVer() {
        return this.fpgaVer;
    }

    public void setFpgaVer(String fpgaVer) {
        this.fpgaVer = fpgaVer;
    }

    public String getManufacturer() {
        return this.manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        return this.model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getBrand() {
        return this.brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getSpliceName() {
        return this.spliceName;
    }

    public void setSpliceName(String spliceName) {
        this.spliceName = spliceName;
    }

    public String getDataTime() {
        return this.dataTime;
    }

    public void setDataTime(String dataTime) {
        this.dataTime = dataTime;
    }

    public Date getUpdateTime() {
        return this.updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Date getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public FiberBean getFiberBean() {
        return fiberBean;
    }

    public void setFiberBean(FiberBean fiberBean) {
        this.fiberBean = fiberBean;
    }
}
