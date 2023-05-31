package com.example.fxt.ble.device.splicer.bean;

import java.io.Serializable;
import java.util.Date;

/**
 * 设备信息
 */
public class OFIDataBean implements Serializable {

    private static final long serialVersionUID = 7946091730863005408L;

    /**
     * 上报的数据id
     */
    private String id;
    /**
     * 设备序列号
     */
    private String frequency;

    /**
     * 设备当前熔接次数
     */
    private String direction;

    /**
     * 设备总熔接次数
     */
    private String measure;

    /**
     * 软件版本 SW Version
     */
    private String location;

    /**
     * FPGA版本
     */
    private String memo;

    /**
     * 制造商
     */
    private String note;

    private String serial;

    private String dataTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建时间
     */
    private Date createTime;

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getMeasure() {
        return measure;
    }

    public void setMeasure(String measure) {
        this.measure = measure;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
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

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getSerial() {
        return serial;
    }
}
