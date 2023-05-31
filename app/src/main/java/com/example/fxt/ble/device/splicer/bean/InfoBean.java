package com.example.fxt.ble.device.splicer.bean;

import java.io.Serializable;

/**
 * 设备信息
 */
public class InfoBean implements Serializable {

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
    private String machineTypeMarket;

    /**
     * 设备总熔接次数
     */
    private String machineSoftVersion;

    /**
     * 软件版本 SW Version
     */
    private String blueToothMAC;

    private String activateStatus;
    /**
     * FPGA版本
     */


    public void setId(String id) {
        this.id = id;
    }


    public void setBlueToothMAC(String blueToothMAC) {
        this.blueToothMAC = blueToothMAC;
    }

    public void setMachineSoftVersion(String machineSoftVersion) {
        this.machineSoftVersion = machineSoftVersion;
    }

    public void setMachineTypeMarket(String machineTypeMarket) {
        this.machineTypeMarket = machineTypeMarket;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public void setActivateStatus(String activateStatus) {
        this.activateStatus = activateStatus;
    }

    public String getId() {
        return this.id;
    }

    public String getBlueToothMAC() {
        return blueToothMAC;
    }

    public String getMachineSoftVersion() {
        return machineSoftVersion;
    }

    public String getMachineTypeMarket() {
        return machineTypeMarket;
    }

    public String getSn() {
        return sn;
    }

    public String getActivateStatus() {
        return activateStatus;
    }
}
