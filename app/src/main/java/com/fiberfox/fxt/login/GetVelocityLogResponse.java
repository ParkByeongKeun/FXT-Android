package com.fiberfox.fxt.login;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class GetVelocityLogResponse {

    public List<data> data = new ArrayList<>();
    String err;
    String req_count;
    String count;
    public List<data> getData(){return data;}
    public String getReqCount(){return req_count;}
    public String getCount(){return count;}
    public String getErr(){return err;}

    public class data{
        @SerializedName("id") String id;
        @SerializedName("serial") String serial;
        @SerializedName("at") String at;
        @SerializedName("velocity") String velocity;
        @SerializedName("msg") String msg;
        public String getId(){return id;}
        public String getSerial(){return serial;}
        public String getAt(){return at;}
        public String getVelocity(){return velocity;}
        public String getMsg(){return msg;}
    }
}
