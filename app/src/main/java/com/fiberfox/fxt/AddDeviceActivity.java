package com.fiberfox.fxt;

import android.Manifest;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
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

import com.fiberfox.fxt.ble.api.BleAPI;
import com.fiberfox.fxt.ble.api.bean.BleResultBean;
import com.fiberfox.fxt.ble.api.bean.BleScanBean;
import com.fiberfox.fxt.ble.api.callback.BleConnectionCallBack;
import com.fiberfox.fxt.ble.api.callback.BleScanCallback;
import com.fiberfox.fxt.ble.api.util.ByteUtil;
import com.fiberfox.fxt.ble.device.BleDeviceFactory;
import com.fiberfox.fxt.ble.device.splicer.BleSplicerCallback;
import com.fiberfox.fxt.ble.device.splicer.bean.OFIDataBean;
import com.fiberfox.fxt.ble.util.BleUtil;
import com.fiberfox.fxt.ble.util.OfiDataParseUtil;
import com.fiberfox.fxt.utils.BleListAdapter;
import com.fiberfox.fxt.utils.C_Permission;
import com.fiberfox.fxt.utils.CustomDevice;
import com.fiberfox.fxt.utils.ToastUtil;
import com.fiberfox.fxt.widget.XListView;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddDeviceActivity extends MainAppcompatActivity implements XListView.IXListViewListener {

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
    private Map<String, OFIDataBean> mSpliceDataBeanMap;
    private List<OFIDataBean> mOFIDataBeanList;
    RelativeLayout rlProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (this.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

                C_Permission.checkPermission(this);
                finish();
            }
        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            if (this.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
////                C_Permission.checkPermission(this);
//                finish();
//            }
//        }

        customApplication = (CustomApplication) getApplicationContext();
        bleDevices = new ArrayList<>();
        mSpliceDataBeanMap = new HashMap<>();
        mOFIDataBeanList = new ArrayList<>();
        mOFIDataBeanList = customApplication.ofiDatabase.selectAllSpliceData();
        rlProgress = findViewById(R.id.rlProgress);
        rlProgress.setVisibility(View.GONE);
        listView = findViewById(R.id.listView_);
        tvNoDevice = findViewById(R.id.ble_list_tv_tip);
        progressBar = findViewById(R.id.progress_circular);
        isFirst = true;
        mTitle = getSupportActionBar();
        mTitle.setCustomView(null);
        mTitle.setDisplayShowCustomEnabled(true);
        mTitle.setTitle("Connect Devices");
        mTitle.setBackgroundDrawable(new ColorDrawable(0xffE56731));
        bleListAdapter = new BleListAdapter(bleDevices, getApplication());
        listView.setEmptyView(tvNoDevice);
        listView.setAdapter(bleListAdapter);
        initView();
    }

    protected void initView() {
        mItems.clear();
        mHandler = new Handler();
        customApplication.arrBleAddress.clear();
        customApplication.arrBleSerial.clear();
        customApplication.arrBleAddress = getStringArrayPref(this,customApplication.login_id+"ofi");
        customApplication.arrBleSerial = getStringArrayPref(this,customApplication.login_id+"ofi_serial");
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
            for (int i = 0 ; i < customApplication.arrBleAddress.size() ; i++) {
                if(bleDevices.get(position - 1).getAddress().equals(customApplication.arrBleAddress.get(i))) {
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
                        for(int j = 0 ; j < bleScanBeanList.size() ; j ++) {
                            boolean isCheck = false;
                            for(int i = 0 ; i < customApplication.arrBleAddress.size() ; i ++) {
                                if(bleScanBeanList.get(j).getAddress().equals(customApplication.arrBleAddress.get(i))){
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
                        for(int i = 0 ; i < customApplication.arrBleAddress.size() ; i ++) {
                            if(bleScanBeanList.get(j).getAddress().equals(customApplication.arrBleAddress.get(i))){
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
                rlProgress.setVisibility(View.GONE);
            }
            @Override
            public void onConnectFail(String errMsg) {
                ToastUtil.showToast(getApplicationContext(), errMsg);
                rlProgress.setVisibility(View.GONE);
            }
            @Override
            public void onConnectSuccess(BluetoothGatt bluetoothGatt) {
                runOnUiThread(() -> {
                    rlProgress.setVisibility(View.GONE);
                    String SN = "serial";
                    customApplication.arrBleAddress.add(connectBLE);
                    customApplication.arrBleSerial.add(SN);
                    setStringArrayPref(AddDeviceActivity.this,customApplication.login_id+"ofi",customApplication.arrBleAddress);
                    setStringArrayPref(AddDeviceActivity.this,customApplication.login_id+"ofi_serial",customApplication.arrBleSerial);
                    onDisconnectClick();
                    Intent intent = new Intent(AddDeviceActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                });
            }
            @Override
            public void onDisconnect(boolean isActive) {
                runOnUiThread(() -> {
                    ToastUtil.showToast(getApplicationContext(), "Server disconnected");
                    rlProgress.setVisibility(View.GONE);
                    AddDeviceActivity.super.activityFinish();
                    if(customApplication.isFNMSCheck) {
                        finish();
                    }else {
                        Intent intent = new Intent(AddDeviceActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
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
                }else if (bleResultBean.getType() == 0){
                    mSpliceDataBeanMap.put(id, OfiDataParseUtil.parseSpliceData(getApplicationContext(),mSpliceDataBeanMap.get(id), bleResultBean));
                }else if (bleResultBean.getType() == 2){
                    String SN = ByteUtil.getAsciiString(bleResultBean.getPayload(),0,bleResultBean.getPayload().length);
                    rlProgress.setVisibility(View.GONE);
                    customApplication.arrBleAddress.add(connectBLE);
                    customApplication.arrBleSerial.add(SN);
                    setStringArrayPref(AddDeviceActivity.this,customApplication.login_id+"ofi",customApplication.arrBleAddress);
                    setStringArrayPref(AddDeviceActivity.this,customApplication.login_id+"ofi_serial",customApplication.arrBleSerial);
                    onDisconnectClick();
                    finish();
                }
            }

            @Override
            public void onFailed(int code, String msg) {
                Log.i("yot132", "failed："+ msg);
                onDisconnectClick();
            }
        });
    }

    public void onDisconnectClick(){
        if (BleAPI.bleIsConnected()){
            BleAPI.disconnectBle();
        }
    }

    @Override
    public void onBackPressed() {
        if(customApplication.arrBleAddress.size() < 1) {
            finish();
        }else {
            Intent intent = new Intent(AddDeviceActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}