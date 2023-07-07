package com.example.fxt;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;

import com.example.fxt.ble.device.splicer.bean.OFIDataBean;
import com.example.fxt.ble.device.splicer.bean.SpliceDataBean;
import com.example.fxt.utils.BackPressCloseHandler;
import com.example.fxt.utils.CustomDevice;
import com.example.fxt.utils.DeviceAdapter;
import com.example.fxt.utils.DeviceSpliceAdapter;
import com.example.fxt.widget.XListView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SpliceActivity extends MainAppcompatActivity implements XListView.IXListViewListener {

    private Handler mHandler;
    private int mRefreshIndex = 0;
    private ArrayList<CustomDevice> mItems = new ArrayList<CustomDevice>();
    ActionBar mTitle;
    XListView listView;
    ExtendedFloatingActionButton fab;
    CustomApplication customApplication;
    private BackPressCloseHandler mBackPressCloseHandler;
    RelativeLayout rlProgress;
    TextView tvNoDevice;
    Dialog custom_dialog;
    Dialog custom_delete_dialog;
    MenuItem menuCancel;
    MenuItem menuDelete;
    boolean isDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBackPressCloseHandler = new BackPressCloseHandler(this);
        customApplication = (CustomApplication)getApplication();
        mTitle = getSupportActionBar();
        mTitle.setCustomView(null);
        mTitle.setDisplayShowCustomEnabled(true);
        mTitle.setTitle("Devices");
        mTitle.setBackgroundDrawable(new ColorDrawable(0xffE56731));
        listView = findViewById(R.id.listView_);
        rlProgress = findViewById(R.id.rlProgress);
        rlProgress.bringToFront();
        fab = findViewById(R.id.fab);
        initView();
        initDialog();
        initDeleteDialog();
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(SpliceActivity.this, AddSpliceDeviceActivity.class);
            startActivity(intent);
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestPermissions(
                    new String[]{
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_ADVERTISE,
                            Manifest.permission.BLUETOOTH_CONNECT
                    },
                    1);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                    new String[]{
                            Manifest.permission.BLUETOOTH
                    },
                    1);
        }
    }

    protected void initView() {
        mItems.clear();
        mHandler = new Handler();
        tvNoDevice = findViewById(R.id.ble_list_tv_tip);
        listView.setEmptyView(tvNoDevice);
        listView.setPullRefreshEnable(true);
        listView.setPullLoadEnable(true);
        listView.setAutoLoadEnable(true);
        listView.setXListViewListener(this);
        listView.setRefreshTime(getTime());
        listView.setOnItemClickListener((parent, view, position, id) -> {
            customApplication.connectSerial = customApplication.arrSpliceBleSerial.get(position -1);
            customApplication.connectBLEAddress = customApplication.arrSpliceBleAddress.get(position -1);
            if(isDelete) {
                showDeleteDialog();
            }else {
                rlProgress.setVisibility(View.VISIBLE);
                Intent intent = new Intent(SpliceActivity.this, SpliceHistoryActivity.class);
                startActivity(intent);
            }
        });
        listView.setOnItemLongClickListener((adapterView, view, i, l) -> {
            customApplication.connectBLEAddress = customApplication.arrSpliceBleAddress.get(i-1);
            customApplication.connectSerial = customApplication.arrSpliceBleSerial.get(i-1);
            Intent intent = new Intent(SpliceActivity.this, SpliceSerialNameActivity.class);
            startActivity(intent);
            return true;
        });
    }

    @Override
    public void onRefresh() {
        mHandler.postDelayed(() -> {
            onLoad();
        }, 800);
    }

    @Override
    public void onLoadMore() {
        mHandler.postDelayed(() -> onLoad(), 800);
    }

    private void onLoad() {
        mItems.clear();
        rlProgress.setVisibility(View.GONE);
        DeviceAdapter mAdapter = new DeviceAdapter(SpliceActivity.this, R.layout.vw_list_item, mItems);
        if(customApplication.arrSpliceBleAddress.size() != 0) {
            for(int i = 0; i < customApplication.arrSpliceBleAddress.size() ; i ++) {
                String serial = customApplication.arrMapSpliceSerial.get(customApplication.arrSpliceBleAddress.get(i));
                if(serial == null) {
                    serial = "No Device Name";
                }
                if(getFirstCharacter(customApplication.arrSpliceBleSerial.get(i)).equals("1")) {
                    CustomDevice customDevice2 = new CustomDevice(R.drawable.ic_mini6s,"core Alignment Splicer","MINI 6S+",""+customApplication.arrSpliceBleSerial.get(i),""+serial, "Total Count : " + getCount(customApplication.arrSpliceBleSerial.get(i)));
                    mItems.add(customDevice2);
                    mAdapter = new DeviceAdapter(SpliceActivity.this, R.layout.vw_list_item, mItems);
                    listView.setAdapter(mAdapter);
                }else {
                    CustomDevice customDevice2 = new CustomDevice(R.drawable.ic_mini,"core Alignment Splicer","MINI 100CA+",""+customApplication.arrSpliceBleSerial.get(i),""+serial, "Total Count : " + getCount(customApplication.arrSpliceBleSerial.get(i)));
                    mItems.add(customDevice2);
                    mAdapter = new DeviceAdapter(SpliceActivity.this, R.layout.vw_list_item, mItems);
                    listView.setAdapter(mAdapter);
                }
            }
        }else {
            mAdapter = new DeviceAdapter(SpliceActivity.this, R.layout.vw_list_item, mItems);
            listView.setAdapter(mAdapter);
        }
        listView.stopRefresh();
        listView.stopLoadMore();
        listView.setRefreshTime(getTime());
    }

    private String getTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA).format(new Date());
    }

    @Override
    protected void onResume() {
        super.onResume();
        mItems.clear();
        isDelete = false;
        rlProgress.setVisibility(View.GONE);
        DeviceSpliceAdapter mAdapter = new DeviceSpliceAdapter(SpliceActivity.this, R.layout.vw_list_item, mItems);
        if(customApplication.arrSpliceBleAddress.size() >=1 && customApplication.arrSpliceBleSerial.size() >=1) {
            for(int i = 0; i < customApplication.arrSpliceBleAddress.size() ; i ++) {
                String serial = customApplication.arrMapSpliceSerial.get(customApplication.arrSpliceBleAddress.get(i));
                if(serial == null) {
                    serial = "No Device Name";
                }
                if(getFirstCharacter(customApplication.arrSpliceBleSerial.get(i)).equals("1")) {
                    CustomDevice customDevice2 = new CustomDevice(R.drawable.ic_mini6s,"core Alignment Splicer","MINI 6S+",""+customApplication.arrSpliceBleSerial.get(i),""+serial, "Total Count : " + getCount(customApplication.arrSpliceBleSerial.get(i)));
                    mItems.add(customDevice2);
                    mAdapter = new DeviceSpliceAdapter(SpliceActivity.this, R.layout.vw_list_item, mItems);
                    listView.setAdapter(mAdapter);
                }else {
                    CustomDevice customDevice2 = new CustomDevice(R.drawable.ic_mini,"core Alignment Splicer","MINI 100CA+",""+customApplication.arrSpliceBleSerial.get(i),""+serial, "Total Count : " + getCount(customApplication.arrSpliceBleSerial.get(i)));
                    mItems.add(customDevice2);
                    mAdapter = new DeviceSpliceAdapter(SpliceActivity.this, R.layout.vw_list_item, mItems);
                    listView.setAdapter(mAdapter);
                }
            }
        }else {
            mAdapter = new DeviceSpliceAdapter(SpliceActivity.this, R.layout.vw_list_item, mItems);
            listView.setAdapter(mAdapter);
        }

        if(customApplication.arrSpliceBleAddress.size() < 1) {
            Intent intent = new Intent(SpliceActivity.this, AddSpliceDeviceActivity.class);
            startActivity(intent);
            finish();
        }
        if(menuDelete != null) {
            mTitle.setTitle("Fusion Splicer");
            mTitle.setBackgroundDrawable(new  ColorDrawable(0xffE56731));
            menuDelete.setVisible(true);
            menuCancel.setVisible(false);
        }
    }

    public static String getFirstCharacter(String s) {
        if(s == null || s.length() == 0)
            return null;
        else
            return s.substring(0, 1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_delete_device,menu);
        menuDelete = menu.findItem(R.id.menu_delete);
        menuCancel = menu.findItem(R.id.menu_cancel);
        menuCancel.setVisible(false);
        menuDelete.setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.menu_delete: {
//                showChoiseAlertDialog("Select device to delete");
                showDialog();
                return true;
            }
            default: {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getWindow().setStatusBarColor(Color.parseColor("#EA8235"));//statusBar
                }
                isDelete = false;
                mTitle.setTitle("Fusion Splicer");
                mTitle.setBackgroundDrawable(new  ColorDrawable(0xffE56731));
                menuDelete.setVisible(true);
                menuCancel.setVisible(false);
                return true;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public int getCount(String serial) {
        int retCount = 0;
        List<SpliceDataBean> dataList = new ArrayList<>();
        dataList.addAll(customApplication.database.selectAllSpliceData());
        for(int i = 0 ; i < dataList.size() ; i ++) {
            if(dataList.get(i).getSn().equals(serial)) {
                retCount += 1;
            }
        }
        return retCount;
    }


    public void initDialog() {
        custom_dialog = new Dialog(this);
        custom_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        custom_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        custom_dialog.setContentView(R.layout.custom_dialog_base);
        ImageView iv = custom_dialog.findViewById(R.id.iv);
        iv.setImageResource(R.drawable.ic_pop_w);
        TextView tv = custom_dialog.findViewById(R.id.tvTitle);
        TextView subTv = custom_dialog.findViewById(R.id.tvSubTitle);
        tv.setText("Delete Device");
        subTv.setText("Select device to delete");
    }

    public void showDialog() {
        custom_dialog.show();
        custom_dialog.findViewById(R.id.btnNo).setOnClickListener(v -> {
            custom_dialog.dismiss();
        });
        custom_dialog.findViewById(R.id.btnOk).setOnClickListener(v -> {
            isDelete = true;
            menuDelete.setVisible(false);
            menuCancel.setVisible(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(Color.parseColor("#FF0000"));//statusBar
            }
            mTitle.setTitle("Select device to delete");
            mTitle.setBackgroundDrawable(new  ColorDrawable(0xFFFF0000));
            custom_dialog.dismiss();
        });
    }

    public void initDeleteDialog() {
        custom_delete_dialog = new Dialog(this);
        custom_delete_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        custom_delete_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        custom_delete_dialog.setContentView(R.layout.custom_dialog_base);
        ImageView iv = custom_delete_dialog.findViewById(R.id.iv);
        iv.setImageResource(R.drawable.ic_pop_w);
        TextView tv = custom_delete_dialog.findViewById(R.id.tvTitle);
        TextView subTv = custom_delete_dialog.findViewById(R.id.tvSubTitle);
        tv.setText("Delete Device");
        subTv.setText("The Device Data can't be accessed when the Device is deleted");
    }

    public void showDeleteDialog() {
        custom_delete_dialog.show();
        custom_delete_dialog.findViewById(R.id.btnNo).setOnClickListener(v -> {
            custom_delete_dialog.dismiss();
        });
        custom_delete_dialog.findViewById(R.id.btnOk).setOnClickListener(v -> {
            List<SpliceDataBean> dataList = new ArrayList<>();
            dataList.addAll(customApplication.database.selectAllSpliceData());
            for(int i = 0 ; i < dataList.size() ; i ++) {
                if(dataList.get(i).getSn().equals(customApplication.connectSerial)) {
                    customApplication.database.deleteById(Integer.parseInt(dataList.get(i).getId()));
                }
            }
            for(int i = 0 ; i < customApplication.arrSpliceBleAddress.size() ; i ++) {
                if(customApplication.arrSpliceBleAddress.get(i).equals(customApplication.connectBLEAddress)) {
                    customApplication.arrSpliceBleAddress.remove(i);
                    customApplication.arrSpliceBleSerial.remove(i);
                    if(customApplication.arrSpliceBleVersion.size() >= i+1) {
                        customApplication.arrSpliceBleVersion.remove(i);
                    }

                    setStringArrayPref(getApplicationContext(),"arrSpliceBleAddress",customApplication.arrSpliceBleAddress);
                    setStringArrayPref(getApplicationContext(),"arrSpliceBleSerial",customApplication.arrSpliceBleSerial);
                    setStringArrayPref(getApplicationContext(),"arrSpliceBleVersion",customApplication.arrSpliceBleVersion);
                    mHandler.postDelayed(() -> {
                        onLoad();
                    }, 800);
                }
            }
            custom_delete_dialog.dismiss();
        });
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

}