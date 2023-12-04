package com.fiberfox.fxt.RestApi;

public class ResponseImage {

    private boolean anomaly;
    private int loss;

    public boolean getAnomaly() {
        return anomaly;
    }

    public int getLoss() {
        return loss;
    }

    public void setAnomaly(boolean anomaly) {
        this.anomaly = anomaly;
    }

    public void setLoss(int loss) {
        this.loss = loss;
    }
}