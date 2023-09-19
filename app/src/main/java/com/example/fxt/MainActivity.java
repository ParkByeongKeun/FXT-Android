package com.example.fxt;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
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
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;

import com.example.fxt.ble.device.splicer.bean.OFIDataBean;
import com.example.fxt.ofi.GlobalVariable;
import com.example.fxt.utils.C_Permission;
import com.example.fxt.utils.CustomDevice;
import com.example.fxt.utils.DeviceAdapter;
import com.example.fxt.widget.XListView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.kongzue.dialogx.dialogs.MessageDialog;

import net.ijoon.auth.UserRequest;
import net.ijoon.auth.UserResponse;

import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends MainAppcompatActivity implements XListView.IXListViewListener {

    private Handler mHandler;
    private int mRefreshIndex = 0;
    private ArrayList<CustomDevice> mItems = new ArrayList<CustomDevice>();
    ActionBar mTitle;
    XListView listView;
    ExtendedFloatingActionButton fab;
    CustomApplication customApplication;
    RelativeLayout rlProgress;
    TextView tvNoDevice;
    boolean isDelete;
    MenuItem menuCancel;
    MenuItem menuDelete;
    Dialog custom_dialog;
    Dialog custom_delete_dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        customApplication = (CustomApplication)getApplication();
        mTitle = getSupportActionBar();
        mTitle.setCustomView(null);
        mTitle.setDisplayShowCustomEnabled(true);
        mTitle.setTitle("Optical Fiber Identifier");
        initDialog();
        initDeleteDialog();
        listView = findViewById(R.id.listView_);
        rlProgress = findViewById(R.id.rlProgress);
        rlProgress.bringToFront();
        fab = findViewById(R.id.fab);
        C_Permission.checkPermission(this);
        initView();
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddDeviceActivity.class);
            startActivity(intent);
            finish();
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
            customApplication.connectBLEAddress = customApplication.arrBleAddress.get(position -1);
            if(isDelete) {
                showDeleteDialog();
//                showAlertDialog("The Device Data can't be accessed when the Device is deleted");
            }else {
                rlProgress.setVisibility(View.VISIBLE);
                Bundle b = new Bundle();
                b.putString(BluetoothDevice.EXTRA_DEVICE, customApplication.arrBleAddress.get(position -1));
                GlobalVariable.gDeviceIs = true;
                GlobalVariable.gDeviceAdd = customApplication.arrBleAddress.get(position -1);
                Intent result = new Intent();
                result.putExtras(b);
                setResult(Activity.RESULT_OK, result);
                Intent intent = new Intent(MainActivity.this, OfiInfoActivity.class);
                startActivity(intent);
                if(customApplication.isFNMSCheck) {
                    finish();
                }
            }
        });
        listView.setOnItemLongClickListener((adapterView, view, i, l) -> {
            customApplication.connectBLEAddress = customApplication.arrBleAddress.get(i-1);
            Intent intent = new Intent(MainActivity.this, SerialNameActivity.class);
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
        DeviceAdapter mAdapter = new DeviceAdapter(MainActivity.this, R.layout.vw_list_item, mItems);
        if(customApplication.arrBleAddress.size() != 0) {
            for(int i = 0 ; i < customApplication.arrBleAddress.size() ; i ++) {
                String serial = customApplication.arrMapSerial.get(customApplication.arrBleAddress.get(i));
                if(serial == null) {
                    serial = "No Device Name";
                }
                CustomDevice customDevice2 = new CustomDevice(R.drawable.ic_ofi,"Optical Fiber Identifier","SFI-10B",""+customApplication.arrBleAddress.get(i),serial, "");
                mItems.add(customDevice2);
                mAdapter = new DeviceAdapter(MainActivity.this, R.layout.vw_list_item, mItems);
                listView.setAdapter(mAdapter);
            }
        }else {
            mAdapter = new DeviceAdapter(MainActivity.this, R.layout.vw_list_item, mItems);
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
//        onLoad();
        isDelete = false;
        mItems.clear();
        rlProgress.setVisibility(View.GONE);
        DeviceAdapter mAdapter = new DeviceAdapter(MainActivity.this, R.layout.vw_list_item, mItems);
        if(customApplication.arrBleAddress.size() >=1) {
            for(int i = 0 ; i < customApplication.arrBleAddress.size() ; i ++) {
                String serial = customApplication.arrMapSerial.get(customApplication.arrBleAddress.get(i));
                if(serial == null) {
                    serial = "No Device Name";
                }
                CustomDevice customDevice2 = new CustomDevice(R.drawable.ic_ofi,"Optical Fiber Identifier","SFI-10B",""+customApplication.arrBleAddress.get(i),""+serial, "");
                mItems.add(customDevice2);
                mAdapter = new DeviceAdapter(MainActivity.this, R.layout.vw_list_item, mItems);
                listView.setAdapter(mAdapter);
            }
        }else {
            mAdapter = new DeviceAdapter(MainActivity.this, R.layout.vw_list_item, mItems);
            listView.setAdapter(mAdapter);
        }
        if(customApplication.arrBleAddress.size() < 1) {
            Intent intent = new Intent(MainActivity.this, AddDeviceActivity.class);
            startActivity(intent);
            finish();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#ffE56731"));//statusBar
        }
        if(menuDelete != null) {
            mTitle.setTitle("Optical Fiber Identifier");
            mTitle.setBackgroundDrawable(new  ColorDrawable(0xffE56731));
            menuDelete.setVisible(true);
            menuCancel.setVisible(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public int getCount(String serial) {
        int retCount = 0;
        List<OFIDataBean> dataList = new ArrayList<>();
        dataList.addAll(customApplication.ofiDatabase.selectAllSpliceData());
        for(int i = 0 ; i < dataList.size() ; i ++) {
//            if(dataList.get(i).getSn().equals(serial)) {
//                retCount += 1;
//            }
        }
        return retCount;
    }

//    @Override
//    public void onItemClick(int position) {
//        customApplication.connectBLEAddress = customApplication.arrBleAddress.get(position);
//        Intent intent = new Intent(MainActivity.this, SerialNameActivity.class);
//        startActivity(intent);
//    }

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
                mTitle.setTitle("Optical Fiber Identifier");
                mTitle.setBackgroundDrawable(new  ColorDrawable(0xffE56731));
                menuDelete.setVisible(true);
                menuCancel.setVisible(false);
                return true;
            }
        }
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
            UserRequest req = UserRequest.newBuilder().build();
            UserResponse res = customApplication.authStub.getUser(req);
            List<OFIDataBean> dataList = new ArrayList<>();
            dataList.addAll(customApplication.ofiDatabase.selectAllSpliceData());
            for(int i = 0 ; i < dataList.size() ; i ++) {
                if(dataList.get(i).getSerial().equals(customApplication.connectBLEAddress)) {
                    if(dataList.get(i).getUser().equals(res.getUsers().getId())) {
                        customApplication.ofiDatabase.deleteById(Integer.parseInt(dataList.get(i).getId()));
                    }
                }
            }
            for(int i = 0 ; i < customApplication.arrBleAddress.size() ; i ++) {
                if(customApplication.arrBleAddress.get(i).equals(customApplication.connectBLEAddress)) {
                    customApplication.arrBleAddress.remove(i);
                    customApplication.arrBleSerial.remove(i);
                    setStringArrayPref(getApplicationContext(),customApplication.login_id+"ofi",customApplication.arrBleAddress);
                    setStringArrayPref(getApplicationContext(),customApplication.login_id+"ofi_serial",customApplication.arrBleSerial);
                    mHandler.postDelayed(() -> {
                        onLoad();
                    }, 800);
                }
            }
            custom_delete_dialog.dismiss();
        });
    }
}