package com.fiberfox.fxt.ble.device.splicer.bean;

import java.io.Serializable;

/**
 * 熔接信息
 */
public class FNMSDataBean implements Serializable {

    private static final long serialVersionUID = 4698282396308241577L;

    private String id;

    /**
     * 熔接结果 1成功，0失败
     */
    private String user;

    /**
     * 错误值
     */
    private String salt;

    /**
     * 熔接损耗
     */
    private String hash;

    /**
     * 左切割角度
     */
    private String note;

    /**
     * 右切割角度
     */
    private String data_time;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getSalt() {
        return salt;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getHash() {
        return hash;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getNote() {
        return note;
    }

    public void setData_time(String data_time) {
        this.data_time = data_time;
    }

    public String getData_time() {
        return data_time;
    }
}
