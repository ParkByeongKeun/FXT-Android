package com.fiberfox.fxt;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.fiberfox.fxt.database.EnclosureDatabase;
import com.fiberfox.fxt.database.OFIDatabase;
import com.fiberfox.fxt.database.SpliceDataDao;
import com.fiberfox.fxt.utils.FNMSData;
import com.fiberfox.fxt.utils.ForegroundService;
import com.fiberfox.fxt.utils.FusedList;
import com.fiberfox.fxt.utils.MacList;
import com.fiberfox.fxt.utils.UsbService;
import com.fiberfox.fxt.utils.VideoDO;
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
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

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
    public EnclosureDatabase enclosureDatabase;
    public List<FNMSData> fnmsDataList;
    public boolean isFNMSCheck;
    public String selectCore = "";
    public boolean isLevelCheck;
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
    public AuthGrpc.AuthBlockingStub authStub;
    Metadata header;
    String token;
    String login_id;
    String loginKey;

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences preferences = getSharedPreferences("preferences",MODE_PRIVATE);
        token = preferences.getString("token","");
        login_id = preferences.getString("email","");
        loginKey = preferences.getString("loginKey","");
        Log.d("yot132","token = " + token);
        try {
            authIS = getResources().getAssets().open("server.crt");
            ManagedChannel auth_channel = ManagedChannelBuilder.forAddress("ijoon.iptime.org", 33919).usePlaintext().build();
//            ManagedChannel auth_channel = ManagedChannelBuilder.forAddress("123.142.5.131", 23915).usePlaintext().build(); //fiberfox
//            ManagedChannel auth_channel = ChannelBuilder.buildTls("192.168.13.30", 8090, authIS);
            authStub = AuthGrpc.newBlockingStub(auth_channel);
            authIS.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        DialogX.init(this);
        ofiDatabase = new OFIDatabase(this);
        enclosureDatabase = new EnclosureDatabase(this);
        arrVideoList = new ArrayList<>();
        arrMac = new ArrayList<>();
        arrFused = new ArrayList<>();
        instance = this;
        arrBleAddress = getStringArrayPref(this,login_id+"ofi");
        arrBleSerial = getStringArrayPref(this,login_id+"ofi_serial");
        arrMapSerial = getMap(this);
        arrMapSpliceSerial = getMap(this);
        fnmsDataList = new ArrayList<>();

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
        Log.d("yot132","loginid = "+ login_id);
        arrSpliceBleAddress = getStringArrayPref(this,login_id);
        arrSpliceBleSerial = getStringArrayPref(this,login_id+"serial");
        arrSpliceBleVersion = getStringArrayPref(this,login_id+"version");
    }

    public static Context getCurrentContext(){
        return instance;
    }

    public ArrayList<String> getStringArrayPref(Context context, String key) {
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

    public void setMetaData() {
        header=new Metadata();
        Metadata.Key<String> key =
                Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);
        header.put(key, token);
//        stub = MetadataUtils.attachHeaders(stub,header);
        authStub = MetadataUtils.attachHeaders(authStub,header);
    }
}