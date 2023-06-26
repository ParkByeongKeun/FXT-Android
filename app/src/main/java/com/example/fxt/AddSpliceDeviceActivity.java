package com.example.fxt;

import static com.example.fxt.ble.api.util.ByteUtil.getAsciiString;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;

import com.example.fxt.ble.api.BleAPI;
import com.example.fxt.ble.api.bean.BleResultBean;
import com.example.fxt.ble.api.bean.BleScanBean;
import com.example.fxt.ble.api.callback.BleConnectionCallBack;
import com.example.fxt.ble.api.callback.BleScanCallback;
import com.example.fxt.ble.api.util.ByteUtil;
import com.example.fxt.ble.device.BleDeviceFactory;
import com.example.fxt.ble.device.splicer.BleSplicerCallback;
import com.example.fxt.ble.device.splicer.bean.SpliceDataBean;
import com.example.fxt.ble.util.BleUtil;
import com.example.fxt.ble.util.SpliceDataParseUtil;
import com.example.fxt.utils.BleListAdapter;
import com.example.fxt.utils.CustomDevice;
import com.example.fxt.utils.ToastUtil;
import com.example.fxt.widget.XListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddSpliceDeviceActivity extends MainAppcompatActivity implements XListView.IXListViewListener {

    private BleListAdapter bleListAdapter;
    CustomApplication customApplication;
    boolean isFirst;
    TextView tvNoDevice;
    XListView listView;
    private ArrayList<CustomDevice> mItems = new ArrayList<CustomDevice>();
    private Handler mHandler;
    ProgressBar progressBar;
    ArrayList<BleScanBean> bleDevices;
    ActionBar mTitle;
    private Map<String, SpliceDataBean> mSpliceDataBeanMap;
    private List<SpliceDataBean> mSpliceDataBeanList;
    RelativeLayout rlProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);
        customApplication = (CustomApplication) getApplicationContext();
        bleDevices = new ArrayList<>();
        mSpliceDataBeanMap = new HashMap<>();
        mSpliceDataBeanList = new ArrayList<>();
        mSpliceDataBeanList = customApplication.database.selectAllSpliceData();
        rlProgress = findViewById(R.id.rlProgress);
        rlProgress.setVisibility(View.GONE);
        listView = findViewById(R.id.listView_);
        tvNoDevice = findViewById(R.id.ble_list_tv_tip);
        progressBar = findViewById(R.id.progress_circular);
        isFirst = true;
        mTitle = getSupportActionBar();
        mTitle.setCustomView(null);
        mTitle.setDisplayShowCustomEnabled(true);
        mTitle.setBackgroundDrawable(new ColorDrawable(0xffE56731));
        mTitle.setTitle("Connect Devices");
        bleListAdapter = new BleListAdapter(bleDevices, getApplication());
        listView.setEmptyView(tvNoDevice);
        listView.setAdapter(bleListAdapter);
        initView();
    }

    protected void initView() {
        mItems.clear();
        mHandler = new Handler();
        customApplication.arrSpliceBleAddress.clear();
        customApplication.arrSpliceBleSerial.clear();
        customApplication.arrSpliceBleVersion.clear();
        customApplication.arrSpliceBleAddress = getStringArrayPref(this,"arrSpliceBleAddress");
        customApplication.arrSpliceBleSerial = getStringArrayPref(this,"arrSpliceBleSerial");
        customApplication.arrSpliceBleVersion = getStringArrayPref(this,"arrSpliceBleVersion");
        listView.setPullRefreshEnable(true);
        listView.setPullLoadEnable(true);
        listView.setAutoLoadEnable(true);
        listView.setXListViewListener(this);
        listView.setRefreshTime(getTime());
        findViewById(R.id.tvNeedHelp).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.fiberfox.co.kr/"));
            startActivity(intent);
        });
        listView.setOnItemClickListener((parent, view, position, id) -> {
            boolean isConnect = false;
            for (int i = 0; i < customApplication.arrSpliceBleAddress.size() ; i++) {
                if(bleDevices.get(position - 1).getAddress().equals(customApplication.arrSpliceBleAddress.get(i))) {
                    isConnect = true;
                }
            }
            if(isConnect) {
                Toast.makeText(getApplicationContext(),"already prepared",Toast.LENGTH_SHORT).show();
            }else {
                BleAPI.stopScan();
                if (progressBar.getVisibility() == View.VISIBLE){
                    progressBar.setVisibility(View.GONE);
                }
                rlProgress.setVisibility(View.VISIBLE);
                onConnectClick(bleDevices.get(position - 1).getAddress());
            }
        });
    }

    @Override
    public void onRefresh() {
        BleAPI.stopScan();
        new Handler().postDelayed(() -> {
            onLoad();
            if (BleUtil.makeSureEnable(this)) {
                BleAPI.startScan(new BleScanCallback() {
                    @Override
                    public void onStart(List<BleScanBean> bleScanBeanList) {
                        bleDevices.clear();
                        progressBar.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onStop(List<BleScanBean> bleScanBeanList) {
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onDeviceFound(BleScanBean bleScanBean, List<BleScanBean> bleScanBeanList) {
                        bleDevices.clear();
                        bleDevices.addAll(bleScanBeanList);
                        bleListAdapter.notifyDataSetChanged();
                    }
                });
            }
        }, 2000);
    }

    @Override
    public void onLoadMore() {
        mHandler.postDelayed(() -> onLoad(), 800);
    }

    private void onLoad() {
        listView.stopRefresh();
        listView.stopLoadMore();
        listView.setRefreshTime(getTime());
    }

    private String getTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA).format(new Date());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (BleUtil.makeSureEnable(this)) {
            BleAPI.startScan(new BleScanCallback() {
                @Override
                public void onStart(List<BleScanBean> bleScanBeanList) {
                    bleDevices.clear();
                    progressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onStop(List<BleScanBean> bleScanBeanList) {
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onDeviceFound(BleScanBean bleScanBean, List<BleScanBean> bleScanBeanList) {
                    bleDevices.clear();
                    for(int j = 0 ; j < bleScanBeanList.size() ; j ++) {
                        boolean isCheck = false;
                        for(int i = 0 ; i < customApplication.arrSpliceBleAddress.size() ; i ++) {
                            if(bleScanBeanList.get(j).getAddress().equals(customApplication.arrSpliceBleAddress.get(i))){
                                isCheck = true;
                                break;
                            }
                        }
                        if(!isCheck)
                            bleDevices.add(bleScanBeanList.get(j));
                    }
                    bleListAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        BleAPI.stopScan();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void setStringArrayPref(Context context, String key, ArrayList<String> values) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        JSONArray a = new JSONArray();
        for (int i = 0; i < values.size(); i++) {
            a.put(values.get(i));
        }
        if (!values.isEmpty()) {
            editor.putString(key, a.toString());
        } else {
            editor.putString(key, null);
        }
        editor.apply();
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


    public void onConnectClick(String connectBLE){
        if (BleAPI.bleIsConnected()){
            return;
        }
        BleAPI.startConnectBle(connectBLE, new BleConnectionCallBack() {
            @Override
            public void onReceive(BluetoothGattCharacteristic data_char) {
                Log.d("yot132","22");
                rlProgress.setVisibility(View.GONE);
            }
            @Override
            public void onConnectFail(String errMsg) {
                ToastUtil.showToast(getApplicationContext(), errMsg);
                Log.d("yot132","33");
                rlProgress.setVisibility(View.GONE);
            }
            @Override
            public void onConnectSuccess(BluetoothGatt bluetoothGatt) {
                runOnUiThread(() -> {
                    rlProgress.setVisibility(View.GONE);
                });
            }
            @Override
            public void onDisconnect(boolean isActive) {
                runOnUiThread(() -> {
                    ToastUtil.showToast(getApplicationContext(), "Server disconnected");
                    rlProgress.setVisibility(View.GONE);
                    AddSpliceDeviceActivity.super.activityFinish();
                    Intent intent = new Intent(AddSpliceDeviceActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                });
            }
        });
        BleDeviceFactory.getSplicerDevice(this,connectBLE,"aa").setReceiveCallback(new BleSplicerCallback() {
            @Override
            public void onSuccess(BleResultBean resultBean) {

            }

            @Override
            public void onReceiveSuccess(BleResultBean bleResultBean) {
                rlProgress.setVisibility(View.VISIBLE);
                String id = bleResultBean.getIdStr();
                if (bleResultBean.getType() == 1){
                    mSpliceDataBeanMap.put(id, SpliceDataParseUtil.parseSpliceImage(getApplicationContext(), mSpliceDataBeanMap.get(id), bleResultBean));
                }else if (bleResultBean.getType() == 0){
                    mSpliceDataBeanMap.put(id, SpliceDataParseUtil.parseSpliceData(getApplicationContext(),mSpliceDataBeanMap.get(id), bleResultBean));
                }else if (bleResultBean.getType() == 2){
                }else {
                    String strJson = getAsciiString(bleResultBean.getPayload(),0,bleResultBean.getPayload().length);
                    try {
                        // 最外层的JSONObject对象
                        JSONObject object = new JSONObject(strJson);

                        Log.d("yot132","sn = " + object.getString("SN"));
                        String SN = object.getString("SN");
                        String SWVersion = object.getString("machineSoftVersion");
                        rlProgress.setVisibility(View.GONE);
                        customApplication.arrSpliceBleAddress.add(connectBLE);
                        customApplication.arrSpliceBleSerial.add(SN);
                        customApplication.arrSpliceBleVersion.add(SN +"," +SWVersion);
                        setStringArrayPref(AddSpliceDeviceActivity.this,"arrSpliceBleAddress",customApplication.arrSpliceBleAddress);
                        setStringArrayPref(AddSpliceDeviceActivity.this,"arrSpliceBleSerial",customApplication.arrSpliceBleSerial);
                        setStringArrayPref(AddSpliceDeviceActivity.this,"arrSpliceBleVersion",customApplication.arrSpliceBleVersion);
                        onDisconnectClick();
                        finish();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailed(int code, String msg) {
                Log.i("yot132", "failed："+ msg);
                onDisconnectClick();
            }
        });
    }

    public void showData(String serial) {
        List<SpliceDataBean> dataList = new ArrayList<>();
        dataList.addAll(customApplication.database.selectAllSpliceData());
        for(int i = 0 ; i < dataList.size() ; i ++) {
            if(dataList.get(i).getSn().equals(serial)) {
                mSpliceDataBeanList.add(dataList.get(i));
            }
        }
    }

    public void onDisconnectClick(){
        if (BleAPI.bleIsConnected()){
            BleAPI.disconnectBle();
        }
    }
}