package com.example.fxt;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.AppCompatButton;

import com.example.fxt.ble.api.BleAPI;
import com.example.fxt.ble.api.bean.BleResultBean;
import com.example.fxt.ble.api.callback.BleConnectionCallBack;
import com.example.fxt.ble.api.util.ByteUtil;
import com.example.fxt.ble.device.BleDeviceFactory;
import com.example.fxt.ble.device.splicer.BleSplicerCallback;
import com.example.fxt.ble.device.splicer.bean.SpliceDataBean;
import com.example.fxt.ble.util.SpliceDataParseUtil;
import com.example.fxt.utils.CustomDevice;
import com.example.fxt.utils.DeviceInfoAdapter;
import com.example.fxt.utils.ToastUtil;
import com.example.fxt.widget.XListView;

import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SpliceInfoActivity extends MainAppcompatActivity implements XListView.IXListViewListener {

    private Handler mHandler;
    private int mIndex = 0;
    private int mRefreshIndex = 0;
    private ArrayList<CustomDevice> mItems = new ArrayList<CustomDevice>();
    Button btnHistory;
    ActionBar mTitle;
    XListView listView;
    String serial;
    String project;
    CustomApplication customApplication;
    RelativeLayout rlProgress;
    private Map<String, SpliceDataBean> mSpliceDataBeanMap;
    private List<SpliceDataBean> mSpliceDataBeanList;
//    AppCompatButton btnDelete;
    boolean isFirstStart = false;
    Dialog custom_dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_info);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        customApplication = (CustomApplication) getApplication();
        Intent intent = getIntent();
        mSpliceDataBeanMap = new HashMap<>();
        mSpliceDataBeanList = new ArrayList<>();
        mSpliceDataBeanList = customApplication.database.selectAllSpliceData();
        rlProgress = findViewById(R.id.rlProgress);
        serial = intent.getStringExtra("serial");
        project = intent.getStringExtra("project");
        mTitle = getSupportActionBar();
        mTitle.setCustomView(null);
        mTitle.setDisplayShowCustomEnabled(true);
        mTitle.setTitle("Device Information");
        mTitle.setBackgroundDrawable(new ColorDrawable(0xffE56731));
        listView = findViewById(R.id.layout_listview);
        btnHistory = findViewById(R.id.btnHistory);
        btnHistory.setOnClickListener(v -> {
            Intent historyintent = new Intent(SpliceInfoActivity.this, SpliceHistoryActivity.class);
            historyintent.putExtra("serial",serial);
            historyintent.putExtra("project",project);
            startActivity(historyintent);
            finish();
        });
//        btnDelete.setOnClickListener(v -> {
//            showAlertDialog("The Device Data can't be accessed when the Device is deleted");
//        });
        initView();
        btnHistory.setClickable(true);

        if(customApplication.connectSerial != null) {
            rlProgress.setVisibility(View.GONE);
            mSpliceDataBeanList.clear();
            for (Map.Entry<String, SpliceDataBean> map : mSpliceDataBeanMap.entrySet()) {
                mSpliceDataBeanList.add(map.getValue());
            }
            showData(customApplication.connectSerial);
            String swVersion;
            String totalCount;
            if(mSpliceDataBeanList.size() <= 0) {
                swVersion = "";
                totalCount = "0";
            }else {
                swVersion = mSpliceDataBeanList.get(0).getAppVer();
                totalCount = mSpliceDataBeanList.size() + "";
            }
            CustomDevice customDevice1 = new CustomDevice(0, "Serial Number", customApplication.connectSerial, " ", "", "");
            CustomDevice customDevice3 = new CustomDevice(0, "Total Count", totalCount, " ", "", "");
            CustomDevice customDevice6 = new CustomDevice(0, "SW Version", swVersion, " ", "", "");
            CustomDevice customDevice8 = new CustomDevice(0, "Model", "MINI 100HA+", " ", "", "");
            mItems.add(customDevice1);
            mItems.add(customDevice3);
            mItems.add(customDevice6);
            mItems.add(customDevice8);
            DeviceInfoAdapter mAdapter = new DeviceInfoAdapter(SpliceInfoActivity.this, R.layout.vw_device_list_item, mItems);
            listView.setAdapter(mAdapter);
        }
        onConnectClick();
        if(mItems.size() > 0) {
            mSpliceDataBeanList.clear();
            showData(customApplication.connectSerial);
            String swVersion;
            String totalCount;
            if(mSpliceDataBeanList.size() <= 0) {
                swVersion = "";
                totalCount = "0";
            }else {
                swVersion = mSpliceDataBeanList.get(0).getAppVer();
                totalCount = mSpliceDataBeanList.size() + "";
            }
            CustomDevice customDevice3 = new CustomDevice(0, "Total Count", totalCount, " ", "","");
            CustomDevice customDevice6 = new CustomDevice(0, "SW Version", swVersion, " ", "","");
            mItems.set(1,customDevice3);
            mItems.set(2,customDevice6);
            DeviceInfoAdapter mAdapter = new DeviceInfoAdapter(SpliceInfoActivity.this, R.layout.vw_device_list_item, mItems);
            listView.setAdapter(mAdapter);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    protected void initView() {
        mItems.clear();
        mHandler = new Handler();
        listView.setPullRefreshEnable(true);
        listView.setPullLoadEnable(true);
        listView.setAutoLoadEnable(true);
        listView.setXListViewListener(this);
        listView.setRefreshTime(getTime());
    }

    @Override
    public void onRefresh() {
        mHandler.postDelayed(() -> {
            mIndex = ++mRefreshIndex;
            onLoad();
        }, 800);
    }

    @Override
    public void onLoadMore() {
        mHandler.postDelayed(() -> onLoad(), 800);
    }

    private void onLoad() {
        if(mItems.size() > 0) {
            mSpliceDataBeanList.clear();
            showData(customApplication.connectSerial);
            String swVersion;
            String totalCount;
            if (mSpliceDataBeanList.size() <= 0) {
                swVersion = "";
                totalCount = "0";
            } else {
                swVersion = mSpliceDataBeanList.get(0).getAppVer();
                totalCount = mSpliceDataBeanList.size() + "";
            }
            CustomDevice customDevice3 = new CustomDevice(0, "Total Count", totalCount, " ", "", "");
            CustomDevice customDevice6 = new CustomDevice(0, "SW Version", swVersion, " ", "", "");
            mItems.set(1, customDevice3);
            mItems.set(2, customDevice6);
            DeviceInfoAdapter mAdapter = new DeviceInfoAdapter(SpliceInfoActivity.this, R.layout.vw_device_list_item, mItems);
            listView.setAdapter(mAdapter);
            listView.stopRefresh();
            listView.stopLoadMore();
            listView.setRefreshTime(getTime());
        }else {
            rlProgress.setVisibility(View.VISIBLE);
            onConnectClick();
        }
    }

    private String getTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA).format(new Date());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_delete_device,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.menu_delete: {
//                showAlertDialog("The Device Data can't be accessed when the Device is deleted");
                return true;
            }
            default: {
                finish();
                return true;
            }
        }
    }

    public void onConnectClick(){
        BleAPI.startConnectBle(customApplication.connectBLEAddress, new BleConnectionCallBack() {
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
                });
            }
            @Override
            public void onDisconnect(boolean isActive) {
                runOnUiThread(() -> {
                    ToastUtil.showToast(getApplicationContext(), "Server disconnected");
                    rlProgress.setVisibility(View.GONE);
                    SpliceInfoActivity.super.activityFinish();
                    Intent intent = new Intent(SpliceInfoActivity.this, MainActivity.class);
                    startActivity(intent);
                });
            }
        });
        if(customApplication.arrSpliceBleAddress.size() == 0) {
            Log.d("yot132","bluetoothDevice null");
            return;
        }
        BleDeviceFactory.getSplicerDevice(this,customApplication.connectBLEAddress,"aa").setReceiveCallback(new BleSplicerCallback() {
            @Override
            public void onSuccess(BleResultBean resultBean) {
                btnHistory.setClickable(true);
            }

            @Override
            public void onReceiveSuccess(BleResultBean bleResultBean) {
                rlProgress.setVisibility(View.VISIBLE);
                Log.i("yot132", "onReceiveSuccess-id："+ bleResultBean.getIdStr());
                Log.i("yot132", "onReceiveSuccess-type："+ bleResultBean.getType());
                String id = bleResultBean.getIdStr();
                if (bleResultBean.getType() == 1){
                    mSpliceDataBeanMap.put(id, SpliceDataParseUtil.parseSpliceImage(getApplicationContext(), mSpliceDataBeanMap.get(id), bleResultBean));
                }else if (bleResultBean.getType() == 0){
                    mSpliceDataBeanMap.put(id, SpliceDataParseUtil.parseSpliceData(getApplicationContext(),mSpliceDataBeanMap.get(id), bleResultBean));
                }else if (bleResultBean.getType() == 2){
                    if(!isFirstStart) {
                        String SN = ByteUtil.getAsciiString(bleResultBean.getPayload(),0,bleResultBean.getPayload().length);
                        rlProgress.setVisibility(View.GONE);
                        customApplication.connectSerial = SN;
                        mSpliceDataBeanList.clear();
                        for (Map.Entry<String, SpliceDataBean> map : mSpliceDataBeanMap.entrySet()) {
                            mSpliceDataBeanList.add(map.getValue());
                        }
                        showData(customApplication.connectSerial);
                        String swVersion;
                        String totalCount;
                        if(mSpliceDataBeanList.size() <= 0) {
                            swVersion = "";
                            totalCount = "0";
                        }else {
                            swVersion = mSpliceDataBeanList.get(0).getAppVer();
                            totalCount = mSpliceDataBeanList.size() + "";
                        }
                        Log.d("yot132","1 = " + ByteUtil.getAsciiString(bleResultBean.getPayload(),0,bleResultBean.getPayload().length));
                        CustomDevice customDevice1 = new CustomDevice(0, "Serial Number", ByteUtil.getAsciiString(bleResultBean.getPayload(),0,bleResultBean.getPayload().length), " ", "", "");
                        CustomDevice customDevice3 = new CustomDevice(0, "Total Count", totalCount, " ", "", "");
                        CustomDevice customDevice6 = new CustomDevice(0, "SW Version", swVersion, " ", "", "");
                        CustomDevice customDevice8 = new CustomDevice(0, "Model", "MINI 100HA+", " ", "", "");
                        mItems.add(customDevice1);
                        mItems.add(customDevice3);
                        mItems.add(customDevice6);
                        mItems.add(customDevice8);
                        DeviceInfoAdapter mAdapter = new DeviceInfoAdapter(SpliceInfoActivity.this, R.layout.vw_device_list_item, mItems);
                        listView.setAdapter(mAdapter);
                        isFirstStart = true;
                    }
                }
                mSpliceDataBeanList.clear();
                for (Map.Entry<String, SpliceDataBean> map : mSpliceDataBeanMap.entrySet()) {
                    mSpliceDataBeanList.add(map.getValue());
                }
                if(mSpliceDataBeanList.size() != 0) {
                    if(mSpliceDataBeanList.get(0).getFiberBean() != null) {
                        if(mSpliceDataBeanList.get(0).getFiberBean().getFuseImagePath() != null) {
                            customApplication.database.insert(mSpliceDataBeanList.get(0));
                            mSpliceDataBeanList.clear();
                            mSpliceDataBeanMap.clear();
                            rlProgress.setVisibility(View.GONE);
                        }
                    }
                }
                showData(customApplication.connectSerial);
                String swVersion;
                String totalCount;
                if(mSpliceDataBeanList.size() <= 0) {
                    swVersion = "";
                    totalCount = "0";
                }else {
                    swVersion = mSpliceDataBeanList.get(0).getAppVer();
                    totalCount = mSpliceDataBeanList.size() + "";
                }
                CustomDevice customDevice3 = new CustomDevice(0, "Total Count", totalCount, " ", "", "");
                CustomDevice customDevice6 = new CustomDevice(0, "SW Version", swVersion, " ", "", "");
                mItems.set(1,customDevice3);
                mItems.set(2,customDevice6);
                DeviceInfoAdapter mAdapter = new DeviceInfoAdapter(SpliceInfoActivity.this, R.layout.vw_device_list_item, mItems);
                listView.setAdapter(mAdapter);
            }

            @Override
            public void onFailed(int code, String msg) {
                Log.i("yot132", "failed："+ msg);
//                onDisconnectClick();
            }
        });
    }

    public void onDisconnectClick(){
        if (customApplication.arrSpliceBleAddress.size() == 0){
            ToastUtil.showToast(getApplicationContext(),"No bluetooth device, please go back");
            return;
        }
        if (BleAPI.bleIsConnected()){
            BleAPI.disconnectBle();
        }
    }

    @Override
    protected void onDestroy() {
        Log.d("yot132","asd");
        super.onDestroy();
        onDisconnectClick();
        Log.d("yot132","??");
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

    public void showAlertDialog(String message) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Delete Device");
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton("OK", (dialog, which) -> {
            onDisconnectClick();
            for(int i = 0; i < customApplication.arrSpliceBleAddress.size() ; i ++) {
                if(customApplication.arrSpliceBleAddress.get(i).equals(customApplication.connectBLEAddress)) {
                    customApplication.arrSpliceBleAddress.remove(i);
                    customApplication.arrSpliceBleSerial.remove(i);
                    setStringArrayPref(getApplicationContext(),"arrSpliceBleAddress",customApplication.arrSpliceBleAddress);
                    setStringArrayPref(getApplicationContext(),"arrSpliceBleSerial",customApplication.arrSpliceBleSerial);
                    finish();
                }
            }
            dialog.cancel();
        });
        runOnUiThread(() -> alertDialog.show());
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


    public void initDialog() {
        custom_dialog = new Dialog(this);
        custom_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        custom_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        custom_dialog.setContentView(R.layout.custom_dialog_base);
    }

    public void showDialog() {
        custom_dialog.show();
        custom_dialog.findViewById(R.id.btnNo).setOnClickListener(v -> {
            custom_dialog.dismiss();
        });
        custom_dialog.findViewById(R.id.btnOk).setOnClickListener(v -> {
            custom_dialog.dismiss();
        });
    }
}