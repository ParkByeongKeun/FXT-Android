package com.example.fxt.utils;

public class HistoryData {
    public int mIndex;
    public int mHist;
    public String mStrTag;

    public String getTagPayload()
    {
        String m;
        String strHist = "";

        if (mHist == 0) strHist = "P";
        else if(mHist == 1) strHist = "H1";
        else if(mHist == 2) strHist = "H2";

        m= mIndex + "," +strHist +","+mStrTag;

        return m;
    }
}
