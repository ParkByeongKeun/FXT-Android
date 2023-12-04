package com.fiberfox.fxt.ble.device.splicer.bean;

import java.io.Serializable;

/**
 * 熔接信息
 */
public class FiberBean implements Serializable {

    private static final long serialVersionUID = 4698282396308241577L;

    /**
     * 熔接结果 1成功，0失败
     */
    private String spliceResult;

    /**
     * 错误值
     */
    private String errorValue;

    /**
     * 熔接损耗
     */
    private String loss;

    /**
     * 左切割角度
     */
    private Float leftAngle;

    /**
     * 右切割角度
     */
    private Float rightAngle;

    /**
     * 芯纤角度
     */
    private Float coreAngle;

    /**
     * 芯纤轴向偏移
     */
    private Float coreOffset;

    /**
     * 熔接图片路径
     */
    private String fuseImagePath;

    public String getSpliceResult() {
        return this.spliceResult;
    }

    public void setSpliceResult(String spliceResult) {
        this.spliceResult = spliceResult;
    }

    public String getErrorValue() {
        return this.errorValue;
    }

    public void setErrorValue(String errorValue) {
        this.errorValue = errorValue;
    }

    public String getLoss() {
        return this.loss;
    }

    public void setLoss(String loss) {
        this.loss = loss;
    }

    public Float getLeftAngle() {
        return this.leftAngle;
    }

    public void setLeftAngle(Float leftAngle) {
        this.leftAngle = leftAngle;
    }

    public Float getRightAngle() {
        return this.rightAngle;
    }

    public void setRightAngle(Float rightAngle) {
        this.rightAngle = rightAngle;
    }

    public Float getCoreAngle() {
        return this.coreAngle;
    }

    public void setCoreAngle(Float coreAngle) {
        this.coreAngle = coreAngle;
    }

    public Float getCoreOffset() {
        return this.coreOffset;
    }

    public void setCoreOffset(Float coreOffset) {
        this.coreOffset = coreOffset;
    }

    public String getFuseImagePath() {
        return fuseImagePath;
    }

    public void setFuseImagePath(String fuseImagePath) {
        this.fuseImagePath = fuseImagePath;
    }
}
