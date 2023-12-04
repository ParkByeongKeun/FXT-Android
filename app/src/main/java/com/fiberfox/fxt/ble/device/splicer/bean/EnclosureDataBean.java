package com.fiberfox.fxt.ble.device.splicer.bean;

import java.io.Serializable;
import java.util.Date;

/**
 * 设备信息
 */
public class EnclosureDataBean implements Serializable {

    private static final long serialVersionUID = 7946091730863005408L;

    /**
     * 上报的数据id
     */
    private String id;
    /**
     * 设备序列号
     */
    private String coordinate;

    private String imagePath;

    private String note;

    private String dataTime;

    private String user;

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

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getCoordinate() {
        return coordinate;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setCoordinate(String coordinate) {
        this.coordinate = coordinate;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
