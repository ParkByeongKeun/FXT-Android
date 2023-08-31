package com.example.fxt;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.fxt.database.OFIDatabase;
import com.example.fxt.database.SpliceDataDao;
import com.example.fxt.utils.FNMSData;
import com.example.fxt.utils.ForegroundService;
import com.example.fxt.utils.FusedList;
import com.example.fxt.utils.MacList;
import com.example.fxt.utils.VideoDO;
import com.google.gson.internal.LinkedTreeMap;
import com.kongzue.dialogx.DialogX;

import net.ijoon.auth.AuthGrpc;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class CustomApplication extends Application {

    public ArrayList<VideoDO> arrVideoList;
    ArrayList<MacList> arrMac;
    ArrayList<FusedList> arrFused;
    ArrayList<String> arrBleAddress;
    ArrayList<String> arrBleSerial;
    HashMap<String,String> arrMapSerial;
    HashMap<String,String> arrMapSpliceSerial;
    public String connectSerial;
    private static CustomApplication instance;
    public String connectBLEAddress;
    public OFIDatabase ofiDatabase;
    public List<FNMSData> fnmsDataList;
    public boolean isFNMSCheck;
    public String selectCore = "";
    public boolean isLevelCheck;
    public boolean isLogin;
    public float lossThreshold;
    public float angleThreshold;
    public float coreAngleThreshold;
    public float coreOffsetThreshold;

    public ArrayList<String> arrSpliceBleAddress;
    public ArrayList<String> arrSpliceBleSerial;
    public ArrayList<String> arrSpliceBleVersion;
    public SpliceDataDao database;
    ForegroundService foregroundService;
    public final String SERVER = "http://118.67.142.85:8000";
    public String swVersion;

    InputStream authIS;
    AuthGrpc.AuthBlockingStub authStub;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            authIS = getResources().getAssets().open("server.crt");
            ManagedChannel auth_channel = ManagedChannelBuilder.forAddress("192.168.13.40", 9001).usePlaintext().build();
//            ManagedChannel auth_channel = ChannelBuilder.buildTls("192.168.13.30", 8090, authIS);
            authStub = AuthGrpc.newBlockingStub(auth_channel);
            authIS.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        DialogX.init(this);
        ofiDatabase = new OFIDatabase(this);
        arrVideoList = new ArrayList<>();
        arrMac = new ArrayList<>();
        arrFused = new ArrayList<>();
        instance = this;
        arrBleAddress = getStringArrayPref(this,"arrBleAddress");
        arrBleSerial = getStringArrayPref(this,"arrBleSerial");
        arrMapSerial = getMap(this);
        arrMapSpliceSerial = getMap(this);
        fnmsDataList = new ArrayList<>();
        SharedPreferences sharedPreferences = this.getSharedPreferences("login",MODE_PRIVATE);
        isLogin = sharedPreferences.getBoolean("login",false);

        SharedPreferences sharedPreferencesLoss = this.getSharedPreferences("loss",MODE_PRIVATE);
        lossThreshold = sharedPreferencesLoss.getFloat("loss",0.2f);

        SharedPreferences sharedPreferencesAngle = this.getSharedPreferences("angle",MODE_PRIVATE);
        angleThreshold = sharedPreferencesAngle.getFloat("angle",1.0f);

        SharedPreferences sharedPreferencesCoreAngle = this.getSharedPreferences("coreAngle",MODE_PRIVATE);
        coreAngleThreshold = sharedPreferencesCoreAngle.getFloat("coreAngle",1.0f);

        SharedPreferences sharedPreferencesCoreOffset = this.getSharedPreferences("coreOffset",MODE_PRIVATE);
        coreOffsetThreshold = sharedPreferencesCoreOffset.getFloat("coreOffset",0.2f);

        foregroundService = new ForegroundService(this);
        database = new SpliceDataDao(this);
        arrSpliceBleAddress = getStringArrayPref(this,"arrSpliceBleAddress");
        arrSpliceBleSerial = getStringArrayPref(this,"arrSpliceBleSerial");
        arrSpliceBleVersion = getStringArrayPref(this,"arrSpliceBleVersion");
    }

    public static Context getCurrentContext(){
        return instance;
    }

    private ArrayList<String> getStringArrayPref(Context context, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String json = prefs.getString(key, null);
        ArrayList<String> urls = new ArrayList<String>();
        if (json != null) {
            try {
                JSONArray a = new JSONArray(json);
                for (int i = 0; i < a.length(); i++) {
                    String url = a.optString(i);
                    urls.add(url);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return urls;
    }

    public void setMap(Context context, HashMap<String, String> hashMapData) {
        SharedPreferences mmPref = context.getSharedPreferences("arrMapSerial", Context.MODE_PRIVATE);
        if (mmPref != null) {
            JSONObject jsonObject = new JSONObject(hashMapData);
            String jsonString = jsonObject.toString();
            SharedPreferences.Editor editor = mmPref.edit();
            editor.remove("hashMapName").commit();
            editor.putString("hashMapName", jsonString);
            editor.commit();
        }
    }

    public HashMap<String, String> getMap(Context context) {
        HashMap<String, String> outputMap = new HashMap<String, String>();
        SharedPreferences mmPref = context.getSharedPreferences("arrMapSerial", Context.MODE_PRIVATE);
        try {
            if (mmPref != null) {
                String jsonString = mmPref.getString("hashMapName", (new JSONObject()).toString());
                JSONObject jsonObject = new JSONObject(jsonString);

                Iterator<String> keysItr = jsonObject.keys();
                while (keysItr.hasNext()) {
                    String key = keysItr.next();
                    String value = (String) jsonObject.get(key);
                    outputMap.put(key, value);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outputMap;
    }

    public void setVideoList(ArrayList<Object> videoList) {
        for(int i = 0 ; i < videoList.size() ; i++) {
            LinkedTreeMap treeMap = (LinkedTreeMap) videoList.get(i);
            VideoDO videoDO = new VideoDO((Double) treeMap.get("id"),treeMap.get("voTitle").toString(),treeMap.get("enTitle").toString(),
                    treeMap.get("voType").toString(),treeMap.get("voImgUrl").toString(), treeMap.get("voVideoUrl").toString(),treeMap.get("enVideoUrl").toString(),
                    treeMap.get("voDetails").toString(),treeMap.get("enDetails").toString(),treeMap.get("isDisplay").toString(), (String)treeMap.get("createdTime"), (String)treeMap.get("updatedTime"));
            arrVideoList.add(videoDO);
        }
    }

    public ArrayList<VideoDO> getVideoList() {
        return arrVideoList;
    }
}