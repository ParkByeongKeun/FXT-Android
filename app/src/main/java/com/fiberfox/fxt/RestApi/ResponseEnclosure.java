package com.fiberfox.fxt.RestApi;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class ResponseEnclosure {

    @SerializedName("result")
    private List<List<Float>> result;

    public List<List<Float>> getResult() {
        return result;
    }

    public void setResult(List<List<Float>> result) {
        this.result = result;
    }
}