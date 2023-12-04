package com.fiberfox.fxt.ofi;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class OFIData {
    private final int PARSER_ST = 0;
    private final int PARSER_IN = 1;
    private final int PARSER_MSG = 2;
    private final int PARSER_PAYSIZE = 3;
    private final int PARSER_PAYLOAD = 4;
    private final int PARSER_CRC = 1;
    private final int PARSER_OK = 0;
    private final int PARSER_NOK = 1;
    private final short CMD_OFI = 0x0001;

    public String mDateTime;
    public int mDetect;
    public int mMode;
    public String mSigSt;
    public int mDir;
    public int mWaveLength;

    public boolean mPasingOK;

    // 시간을 나타냇 포맷을 정한다 ( yyyy/MM/dd 같은 형태로 변형 가능 )
    private SimpleDateFormat sdfNow = new SimpleDateFormat("MM-dd HH:mm:ss");
    private Date date = new Date();

    public OFIData()
    {
        mDateTime = "";
        mDetect = 0;
        mMode = 0;
        mSigSt = "";
        mDir = 0;
        mWaveLength = 0;

        mPasingOK = false;

    }

    public String toStr()
    {
        String data;

        data = mDateTime + ",";
        data += mDetect + ",";
        data += mMode + ",";
        data += mSigSt + ",";
        data += mDir + ",";
        data += mWaveLength;

        return data;
    }

    public void setData(String line) {
        mPasingOK = true;

        Log.d("OFIData", "setData = " + line);

        String[] strPar = line.split(",");

        mDateTime = strPar[0];
        mDetect = Integer.parseInt(strPar[1]);
        mMode = Integer.parseInt(strPar[2]);
        mSigSt = strPar[3];
        mDir = Integer.parseInt(strPar[4]);
        mWaveLength = Integer.parseInt(strPar[5]);

    }
    // 1 1 3 0
    /*       Log.d("OFIData", msg);
        String [] msgpart = msg.split(" ");


        Log.d("OFIData", msgpart[0]);
        int iCommand = Integer.parseInt(msgpart[0]);
        if (iCommand == 1) {
            mDetect = Integer.parseInt(msgpart[1]);
            Log.d("OFIData", msgpart[1]);
            if (mDetect == 1)
            {
                mWaveLength = Integer.parseInt(msgpart[2]);
                mMode = Integer.parseInt(msgpart[3]);
                mDir = Integer.parseInt(msgpart[4]);

                if(msgpart[6] != null)
                    mSigSt = msgpart[5].trim() + msgpart[6].trim();
                else
                    mSigSt = msgpart[5].trim();

                Log.d("OFIData", "WaveLength " +mWaveLength + "Mode " +mMode + "Dir " + mDir+ "SigSt "+ mSigSt);

                mPasingOK = true;

                return true;
            }
        }*/
    public boolean parsing(String msg)
    {
        byte[]  msgbytes = msg.getBytes();
        int     msgSize = msg.length();
        int     parseS = PARSER_ST;
        short   payloadSize = 0;
        short   msgType = 0;

        String [] msgpart = msg.split(" ");

        Log.d("OFIData", msgpart[0]);
        int iCommand = Integer.parseInt(msgpart[0]);
        if (iCommand == 1) {
            mDetect = Integer.parseInt(msgpart[1]);
            if (mDetect == 1)
            {
                mWaveLength = 0;
                mMode = Integer.parseInt(msgpart[2]);
                mDir = Integer.parseInt(msgpart[3]);
                mSigSt = msgpart[4].trim();
                /*mWaveLength = Integer.parseInt(msgpart[2]);
                mMode = Integer.parseInt(msgpart[3]);
                mDir = Integer.parseInt(msgpart[4]);
                if(msgpart[6] != null)
                    mSigSt = msgpart[5].trim() + msgpart[6].trim();
                else
                    mSigSt = msgpart[5].trim();*/

                Log.d("OFIData", "wavelength " + mWaveLength +
                        " mMode " + mMode +
                        " mDir " + mDir +
                        " mSigSt " + mSigSt );


                mPasingOK = true;

                return true;
            }
            else
            {
                mWaveLength = 0;
                mDir = 2;
                mSigSt = "0";
            }
        }
        return false;
        /*
        if (msgSize < 26)
        {
            mPasingOK = false;
            return false;
        }

        for(int i=0;i<msgSize;i++)
        {
            if(parseS == PARSER_ST)
            {
                // Header Check
                if (msgbytes[i] == 0xFF && msgbytes[i+1] == 0xFF)
                {
                    parseS = PARSER_IN;
                    i = i+1;
                }
            }
            else if (parseS == PARSER_IN)
            {
                msgType = (short) (msgbytes[i] + msgbytes[i+1] * 256);
                if (msgType == CMD_OFI)
                {
                    parseS = PARSER_MSG;
                    i = i+1;
                }
                else
                {
                    mPasingOK = false;
                    return false;
                }
            }
            else if (parseS == PARSER_MSG)
            {
                payloadSize = (short) (msgbytes[i] + msgbytes[i+1] * 256);

                i = i + 1;
                parseS = PARSER_PAYSIZE;
            }
            else if (parseS == PARSER_PAYSIZE)
            {
                mToneDetect = msgbytes[i];
                mToneFq = (short) (msgbytes[i+1] + msgbytes[i+2] * 256);
                mToneSigSt = (int) (msgbytes[i+3] + msgbytes[i+4] * 256 + msgbytes[i+5] * 256 * 256 + msgbytes[i+6] * 256 * 256 * 256 );
                mToneDir = msgbytes[i+7];
                mCWDetect = msgbytes[i+8];
                mCWFq = (short) (msgbytes[i+9] + msgbytes[i+10] * 256);
                mCWSigSt = (int) (msgbytes[i+11] + msgbytes[i+12] * 256 + msgbytes[i+13] * 256 * 256 + msgbytes[i+14] * 256 * 256 * 256 );
                mCWDir = msgbytes[i+15];
                i = i+15;
            }
            else if (parseS == PARSER_PAYLOAD)
            {

            }
            else
            {

            }
        }

        if (parseS == PARSER_CRC)
        {
            SetCurTime();
            mPasingOK = true;
            return true;
        }
        mPasingOK = false;
        return false;
        */
    };

    public void SetCurTime()
    {
        // 현재시간을 date 변수에 저장한다.
        date.setTime(System.currentTimeMillis());

        // nowDate 변수에 값을 저장한다.
        mDateTime = sdfNow.format(date);
    }


}
