package com.fiberfox.fxt;

import static com.fiberfox.fxt.ble.api.util.ByteUtil.getAsciiString;
import static com.fiberfox.fxt.utils.ConstantUtil.StrConstant.BEAN;

import android.Manifest;
import android.app.Dialog;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.fiberfox.fxt.R;
import com.fiberfox.fxt.ble.api.BleAPI;
import com.fiberfox.fxt.ble.api.bean.BleResultBean;
import com.fiberfox.fxt.ble.api.callback.BleConnectionCallBack;
import com.fiberfox.fxt.ble.api.util.ArrayUtils;
import com.fiberfox.fxt.ble.api.util.BleCmdUtil;
import com.fiberfox.fxt.ble.api.util.BleHexConvert;
import com.fiberfox.fxt.ble.api.util.ByteUtil;
import com.fiberfox.fxt.ble.device.BleDeviceFactory;
import com.fiberfox.fxt.ble.device.splicer.BleSplicerCallback;
import com.fiberfox.fxt.ble.device.splicer.bean.SpliceDataBean;
import com.fiberfox.fxt.ble.util.SpliceDataParseUtil;
import com.fiberfox.fxt.ble.util.USBSpliceDataParseUtil;
import com.fiberfox.fxt.utils.CustomHistoryList;
import com.fiberfox.fxt.utils.MyValueFormatter;
import com.fiberfox.fxt.utils.SpliceDataAdapter;
import com.fiberfox.fxt.utils.ToastUtil;
import com.fiberfox.fxt.utils.UsbService;
import com.fiberfox.fxt.widget.XListView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import net.ijoon.auth.UserRequest;
import net.ijoon.auth.UserResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class SpliceHistoryActivity extends MainAppcompatActivity implements XListView.IXListViewListener{

    private UsbService usbService;
    private MyHandler usbHandler;
    private Handler mHandler;
    private int mIndex = 0;
    private int mRefreshIndex = 0;
    private ArrayList<CustomHistoryList> mItems = new ArrayList<CustomHistoryList>();
    XListView listView;
    CustomApplication customApplication;
    ImageView menuItemShare;
    ImageView menuItemDelete;
    private Map<String, SpliceDataBean> mSpliceDataBeanMap;
    private List<SpliceDataBean> mSpliceDataBeanList;
    private SpliceDataAdapter spliceDataAdapter;
    ActionBar mTitle;
    List<String> fileList;
    SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    RelativeLayout rlProgress;
    Dialog custom_dialog;
    Dialog custom_delete_dialog;
    boolean isFirstStart = false;
    private LineChart chart;

    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(usbHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splice_history);
        requestReadExternalStoragePermission();
        usbHandler = new MyHandler(this);
        customApplication = (CustomApplication) getApplication();
        menuItemShare = findViewById(R.id.iv_share);
        menuItemDelete = findViewById(R.id.iv_delete);
        menuItemDelete.setVisibility(View.INVISIBLE);
        menuItemShare.setVisibility(View.INVISIBLE);
        initDialog();
        initDeleteDialog();
        menuItemDelete.setOnClickListener(v -> {
            SparseBooleanArray checkedItems1 = listView.getCheckedItemPositions();
            boolean isCheck = false;
            for(int i = 0 ; i < checkedItems1.size() ; i++) {
                if (checkedItems1.valueAt(i)) {
                    isCheck = true;
                }
            }
            if(!isCheck) {
                Toast.makeText(getApplicationContext(),"checked data to delete",Toast.LENGTH_SHORT).show();
            }
            SparseBooleanArray checkedItems = listView.getCheckedItemPositions();
            spliceDataAdapter.setCheckBoxVisible(false);
//            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//            menuItemShare.setVisibility(View.INVISIBLE);
//            menuItemDelete.setVisibility(View.INVISIBLE);
//            mSpliceDataBeanList.clear();

            boolean check = false;
            for(int i = 0 ; i < checkedItems.size() ; i ++) {
                if(checkedItems.valueAt(i)) {
                    check = true;
                }
            }
            if(check) {
                showDeleteDialog();
            }
        });
        menuItemShare.setOnClickListener(v -> {
            SparseBooleanArray checkedItems1 = listView.getCheckedItemPositions();
            boolean isCheck = false;
            for(int i = 0 ; i < checkedItems1.size() ; i++) {
                if (checkedItems1.valueAt(i)) {
                    isCheck = true;
                }
            }
            if(!isCheck) {
                Toast.makeText(getApplicationContext(),"checked data to share",Toast.LENGTH_SHORT).show();
            }
            showDialog();
//            showAlertDialog("Would you like to share?");
        });
        listView = findViewById(R.id.listView_);
        mSpliceDataBeanMap = new HashMap<>();
        mSpliceDataBeanList = new ArrayList<>();
        TextView mFusionDataTextTip = findViewById(R.id.fusion_data_list_tv_tip);
        listView.setEmptyView(mFusionDataTextTip);
        rlProgress = findViewById(R.id.rlProgress);
        rlProgress.bringToFront();
        spliceDataAdapter = new SpliceDataAdapter(mSpliceDataBeanList, getApplicationContext());
        listView.setAdapter(spliceDataAdapter);
        mTitle = getSupportActionBar();
        mTitle.setCustomView(null);
        mTitle.setDisplayShowCustomEnabled(true);
        mTitle.setTitle("Splice History List");
        mTitle.setBackgroundDrawable(new ColorDrawable(0xffE56731));
        initView();

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
                    SpliceHistoryActivity.super.activityFinish();
                    Intent intent = new Intent(SpliceHistoryActivity.this, OFIFNMSActivity.class);
                    startActivity(intent);
                    finish();
                });
            }
        });
        BleDeviceFactory.getSplicerDevice(this,customApplication.connectBLEAddress,"aa").setReceiveCallback(new BleSplicerCallback() {
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
                }else if (bleResultBean.getType() == 2){
                    mSpliceDataBeanMap.put(id, SpliceDataParseUtil.parseSpliceData(getApplicationContext(),mSpliceDataBeanMap.get(id), bleResultBean));
                }else if (bleResultBean.getType() == 4) {
                    if(!isFirstStart) {
                        String strJson = getAsciiString(bleResultBean.getPayload(),0,bleResultBean.getPayload().length);
                        try {
                            // 最外层的JSONObject对象
                            JSONObject object = new JSONObject(strJson);
                            String SN = object.getString("SN");
                            customApplication.swVersion = object.getString("machineSoftVersion");
                            rlProgress.setVisibility(View.GONE);
                            customApplication.connectSerial = SN;
                            mSpliceDataBeanList.clear();
                            for (Map.Entry<String, SpliceDataBean> map : mSpliceDataBeanMap.entrySet()) {
                                mSpliceDataBeanList.add(map.getValue());
                            }
                            showData(customApplication.connectSerial);
                            isFirstStart = true;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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
                setDoc();
            }
            @Override
            public void onFailed(int code, String msg) {
                ToastUtil.showToastLong(getApplicationContext(), msg);
            }
        });
        showData(customApplication.connectSerial);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mSpliceDataBeanList.clear();
        showData(customApplication.connectSerial);
        setDoc();
        setFilters();  // Start listening notifications from UsbService
        startService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);
    }

    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }

            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }
    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        filter.addAction(UsbService.ACTION_USB_READY);
        registerReceiver(mUsbReceiver, filter);
    }

    public void setDoc() {
        fileList = FileList("Download");
        for(int i = 0 ; i < mSpliceDataBeanList.size() ; i ++) {
            int check = 0;
            String date = dateformat.format(mSpliceDataBeanList.get(i).getUpdateTime());
            for(int j = 0 ; j < fileList.size() ; j ++) {
                String[] pathName = date.split(" ");
                String pathTime= pathName[1].replaceAll(":", "_");
                String excelPath = mSpliceDataBeanList.get(i).getSn() + "_" + pathName[0]+"_"+pathTime +".xls";
                String pdfPath = mSpliceDataBeanList.get(i).getSn() + "_" + pathName[0]+"_"+pathTime +".pdf";
                if(pdfPath.equals(fileList.get(j))) {
                    check = check +1;
                }
                if(excelPath.equals(fileList.get(j))) {
                    check = check +10;
                }
            }
            if(check == 1) {
                mSpliceDataBeanList.get(i).setFpgaVer("0");
            }
            if(check >= 10) {
                mSpliceDataBeanList.get(i).setFpgaVer("1");
            }
            if(check >= 11) {
                mSpliceDataBeanList.get(i).setFpgaVer("2");
            }
        }
        spliceDataAdapter.notifyDataSetChanged();
    }

    protected void initView() {
        mItems.clear();
        mHandler = new Handler();
        listView.setPullRefreshEnable(true);
        listView.setPullLoadEnable(true);
        listView.setAutoLoadEnable(true);
        listView.setXListViewListener(this);
        listView.setRefreshTime(getTime());
        listView.setOnItemLongClickListener((adapterView, view, i, l) ->  {
            for( int j = 1 ; j <= mSpliceDataBeanList.size() ; j ++) {
                listView.setItemChecked(j, false);
            }
            menuItemShare.setVisibility(View.VISIBLE);
            menuItemDelete.setVisibility(View.VISIBLE);
            spliceDataAdapter.setCheckBoxVisible(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            mSpliceDataBeanList.clear();
            showData(customApplication.connectSerial);
            setDoc();
            return true;
        });
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if(!spliceDataAdapter.getCheckBox()) {
                Intent intent = new Intent(getApplicationContext(), FusionSpliceDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(BEAN, mSpliceDataBeanList.get(position-1));
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
            }
        });
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
        mSpliceDataBeanList.clear();
        showData(customApplication.connectSerial);
        setDoc();
        listView.stopRefresh();
        listView.stopLoadMore();
        listView.setRefreshTime(getTime());
    }

    private String getTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).format(new Date());
    }

//    public void showAlertDialog(String message, String position) {
//        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
//        alertDialog.setTitle("Alarm");
//        alertDialog.setMessage(message);
//        alertDialog.setPositiveButton("OK", (dialog, which) -> {
//            SparseBooleanArray checkedItems = listView.getCheckedItemPositions();
//            for(int i = 0 ; i < checkedItems.size() ; i++) {
//                if(checkedItems.valueAt(i)) {
//                    String pos = mSpliceDataBeanList.get(checkedItems.keyAt(i) - 1).getId();
//                    customApplication.database.deleteById(Integer.parseInt(pos));
//                }
//            }
//            for( int i = 1 ; i <= mSpliceDataBeanList.size() ; i ++) {
//                listView.setItemChecked(i, false);
//            }
//            mSpliceDataBeanList.clear();
//            showData(customApplication.connectSerial);
//            setDoc();
//            dialog.cancel();
//        });
//        runOnUiThread(() -> alertDialog.show());
//    }

    public void showData(String serial) {
        UserRequest req = UserRequest.newBuilder().build();
        UserResponse res = customApplication.authStub.getUser(req);

        List<SpliceDataBean> dataList = new ArrayList<>();
        dataList.addAll(customApplication.database.selectAllSpliceData());
        for(int i = 0 ; i < dataList.size() ; i ++) {
            if(dataList.get(i).getSn().equals(customApplication.connectSerial)) {
                Log.d("yot132","? = " + dataList.get(i).getUser());
                if(dataList.get(i).getUser().equals(res.getUsers().getId())) {
                    mSpliceDataBeanList.add(dataList.get(i));
                }
            }
        }
        setGraphData();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SparseBooleanArray checkedItems1 = listView.getCheckedItemPositions();
        boolean isCheck = false;
        for(int i = 0 ; i < checkedItems1.size() ; i++) {
            if (checkedItems1.valueAt(i)) {
                isCheck = true;
            }
        }

        switch(item.getItemId()){
            case android.R.id.home: {
                menuItemShare.setVisibility(View.INVISIBLE);
                menuItemDelete.setVisibility(View.INVISIBLE);
                spliceDataAdapter.setCheckBoxVisible(false);
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                mSpliceDataBeanList.clear();
                showData(customApplication.connectSerial);
                for( int i = 1 ; i <= mSpliceDataBeanList.size() ; i ++) {
                    listView.setItemChecked(i, false);
                }
                setDoc();
                return true;
            }
            default: {
                finish();
                return true;
            }
        }
    }

    public List<String> FileList(String strFolderName) {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+strFolderName;
        File directory = new File(path);
        File[] files = directory.listFiles();
        List<String> fileNameList = new ArrayList<>();
        for(int i = 0 ; i < files.length ; i++) {
            fileNameList.add(files[i].getName());
        }
        return fileNameList;
    }

//    public void showDialog() {
//        Dialog bottomDialog = new Dialog(this, R.style.BottomDialog);
//        View contentView = LayoutInflater.from(this).inflate(R.layout.dialog_content_normal, null);
//        bottomDialog.setContentView(contentView);
//        ViewGroup.LayoutParams layoutParams = contentView.getLayoutParams();
//        layoutParams.width = getResources().getDisplayMetrics().widthPixels;
//        contentView.setLayoutParams(layoutParams);
//        bottomDialog.getWindow().setGravity(Gravity.BOTTOM);
//        bottomDialog.setCanceledOnTouchOutside(true);
//        bottomDialog.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
//        contentView.findViewById(R.id.tvALL).setVisibility(View.GONE);
//        contentView.findViewById(R.id.tvPDF).setOnClickListener(v -> {
//            String someFile = "";
//            SparseBooleanArray checkedItems = listView.getCheckedItemPositions();
//            ArrayList<Uri> uriList = new ArrayList<>();
//            for(int i = 0 ; i < checkedItems.size() ; i++) {
//                if(checkedItems.valueAt(i)) {
//                    String date = dateformat.format(mSpliceDataBeanList.get(i).getUpdateTime());
//                    String[] pathName = date.split(" ");
//                    String pathTime= pathName[1].replaceAll(":", "_");
//                    String pdfPath = mSpliceDataBeanList.get(checkedItems.keyAt(i) -1).getSn() + "_" + pathName[0]+"_"+pathTime +".pdf";
//                    someFile = pdfPath;
//                    File filelocation = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), someFile);
//                    Uri path = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", filelocation);
//                    if(!filelocation.canRead()) {
//                        Toast.makeText(getApplicationContext(),"create a doc",Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                    uriList.add(path);
//                }
//            }
//            Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
//            emailIntent.setType("application/pdf");
//            emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList);
//            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "send fiberfox-pdf file");
//            emailIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            startActivity(Intent.createChooser(emailIntent , "share"));
//            mSpliceDataBeanList.clear();
//            showData(customApplication.connectSerial);
//            setDoc();
//        });
//
//        contentView.findViewById(R.id.tvExcel).setOnClickListener(v -> {
//            String someFile = "";
//            SparseBooleanArray checkedItems = listView.getCheckedItemPositions();
//            ArrayList<Uri> uriList = new ArrayList<>();
//
//            for(int i = 0 ; i < checkedItems.size() ; i++) {
//                if(checkedItems.valueAt(i)) {
//                    String date = dateformat.format(mSpliceDataBeanList.get(i).getUpdateTime());
//                    String[] pathName = date.split(" ");
//                    String pathTime= pathName[1].replaceAll(":", "_");
//                    String pdfPath = mSpliceDataBeanList.get(checkedItems.keyAt(i) -1).getSn() + "_" + pathName[0]+"_"+pathTime +".xls";
//                    someFile = pdfPath;
//                    File filelocation = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), someFile);
//                    Uri path = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", filelocation);
//                    if(!filelocation.canRead()) {
//                        Toast.makeText(getApplicationContext(),"create a doc",Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                    uriList.add(path);
//                }
//            }
//            Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
//            emailIntent.setType("application/excel");
//            emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList);
//            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "send fiberfox-excel file");
//            emailIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            startActivity(Intent.createChooser(emailIntent , "share"));
//            mSpliceDataBeanList.clear();
//            showData(customApplication.connectSerial);
//            setDoc();
//        });
//        bottomDialog.show();
//    }

    private void requestReadExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            }
        }
    }


    @Override
    public void onBackPressed() {
        if(spliceDataAdapter.getCheckBox()) {
            spliceDataAdapter.setCheckBoxVisible(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            menuItemShare.setVisibility(View.INVISIBLE);
            menuItemDelete.setVisibility(View.INVISIBLE);
            mSpliceDataBeanList.clear();
            showData(customApplication.connectSerial);
            setDoc();
        }else {
            onDisconnectClick();
            customApplication.connectSerial = null;
            this.finish();
        }
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

//    public void showAlertDialog(String message) {
//        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
//        alertDialog.setTitle("Alarm");
//        alertDialog.setMessage(message);
//        alertDialog.setPositiveButton("Yes", (dialog, which) -> {
//            spliceDataAdapter.setCheckBoxVisible(false);
//            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//            menuItemShare.setVisibility(View.INVISIBLE);
//            menuItemDelete.setVisibility(View.INVISIBLE);
//            mSpliceDataBeanList.clear();
//            showData(customApplication.connectSerial);
//            showDialog();
//            setDoc();
//            dialog.cancel();
//        });
//        alertDialog.setNegativeButton("No", (dialog, which) -> {
//            dialog.cancel();
//        });
//        runOnUiThread(() -> alertDialog.show());
//    }


    public void initDialog() {
        custom_dialog = new Dialog(this);
        custom_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        custom_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        custom_dialog.setContentView(R.layout.custom_dialog_share);
    }

    public void showDialog() {
        custom_dialog.show();
        custom_dialog.findViewById(R.id.btnNo).setOnClickListener(v -> {
            custom_dialog.dismiss();
        });
        custom_dialog.findViewById(R.id.btnPDF).setOnClickListener(v -> {
            String someFile = "";
            SparseBooleanArray checkedItems = listView.getCheckedItemPositions();
            ArrayList<Uri> uriList = new ArrayList<>();
            for(int i = 0 ; i < checkedItems.size() ; i++) {
                if(checkedItems.valueAt(i)) {
                    String date = dateformat.format(mSpliceDataBeanList.get(i).getUpdateTime());
                    String[] pathName = date.split(" ");
                    String pathTime= pathName[1].replaceAll(":", "_");
                    String pdfPath = mSpliceDataBeanList.get(checkedItems.keyAt(i) -1).getSn() + "_" + pathName[0]+"_"+pathTime +".pdf";
                    someFile = pdfPath;
                    File filelocation = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), someFile);
                    Uri path = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", filelocation);
                    if(!filelocation.canRead()) {
                        Toast.makeText(getApplicationContext(),"create a doc",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    uriList.add(path);
                }
            }
            Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            emailIntent.setType("application/pdf");
            emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList);
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "send fiberfox-pdf file");
            emailIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(emailIntent , "share"));
            mSpliceDataBeanList.clear();
            showData(customApplication.connectSerial);
            setDoc();
            custom_dialog.dismiss();
        });
        custom_dialog.findViewById(R.id.btnExcel).setOnClickListener(v -> {

            String someFile = "";
            SparseBooleanArray checkedItems = listView.getCheckedItemPositions();
            ArrayList<Uri> uriList = new ArrayList<>();

            for(int i = 0 ; i < checkedItems.size() ; i++) {
                if(checkedItems.valueAt(i)) {
                    String date = dateformat.format(mSpliceDataBeanList.get(i).getUpdateTime());
                    String[] pathName = date.split(" ");
                    String pathTime= pathName[1].replaceAll(":", "_");
                    String pdfPath = mSpliceDataBeanList.get(checkedItems.keyAt(i) -1).getSn() + "_" + pathName[0]+"_"+pathTime +".xls";
                    someFile = pdfPath;
                    File filelocation = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), someFile);
                    Uri path = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", filelocation);
                    if(!filelocation.canRead()) {
                        Toast.makeText(getApplicationContext(),"create a doc",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    uriList.add(path);
                }
            }
            Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            emailIntent.setType("application/excel");
            emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList);
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "send fiberfox-excel file");
            emailIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(emailIntent , "share"));
            mSpliceDataBeanList.clear();
            showData(customApplication.connectSerial);
            setDoc();
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
        tv.setText("Delete History");
        subTv.setText("Would you like to delete history datas?");
    }

    public void showDeleteDialog() {
        custom_delete_dialog.show();
        custom_delete_dialog.findViewById(R.id.btnNo).setOnClickListener(v -> {
            custom_delete_dialog.dismiss();
        });
        custom_delete_dialog.findViewById(R.id.btnOk).setOnClickListener(v -> {
            SparseBooleanArray checkedItems = listView.getCheckedItemPositions();
            for(int i = 0 ; i < checkedItems.size() ; i++) {
                if(checkedItems.valueAt(i)) {
                    String pos = mSpliceDataBeanList.get(checkedItems.keyAt(i) - 1).getId();
                    customApplication.database.deleteById(Integer.parseInt(pos));
                }
            }
            for( int i = 1 ; i <= mSpliceDataBeanList.size() ; i ++) {
                listView.setItemChecked(i, false);
            }
            mSpliceDataBeanList.clear();
            showData(customApplication.connectSerial);
            setDoc();
            setGraphData();
            custom_delete_dialog.dismiss();
        });
    }

    private void setGraphData() {
        runOnUiThread(() -> {
            chart = findViewById(R.id.chart1);
            chart.setBackgroundColor(Color.rgb(255, 255, 255));
            chart.getDescription().setEnabled(false);
            chart.setTouchEnabled(true);
            chart.setDragEnabled(true);
            chart.setScaleEnabled(true);
            chart.setPinchZoom(false);
            chart.setDrawGridBackground(false);
            YAxis right = chart.getAxisRight();
            right.setTextColor(Color.WHITE);
            XAxis x = chart.getXAxis();
            x.setLabelCount(10,false);
            x.setGranularity(1f);
            x.setTextColor(getResources().getColor(R.color.purple_700));
            x.setAxisLineColor(getResources().getColor(R.color.purple_700));
            x.setPosition(XAxis.XAxisPosition.BOTTOM);
            YAxis y = chart.getAxisLeft();
            y.setLabelCount(6, false);
            y.setTextColor(getResources().getColor(R.color.purple_700));
            y.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
            y.setDrawGridLines(true);
            y.setAxisLineColor(getResources().getColor(R.color.purple_700));

            Legend l = chart.getLegend();
            l.setForm(Legend.LegendForm.LINE);
            l.setTextSize(11f);
            l.setTextColor(Color.BLACK);
            l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
            l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
            l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
            l.setDrawInside(false);

            chart.invalidate();
            ArrayList<Entry> values = new ArrayList<>();
            ArrayList<Entry> averageValues = new ArrayList<>();
            List<SpliceDataBean> temp = mSpliceDataBeanList;
            Collections.reverse(temp);
            float avg = 0;
            for (int i = 0; i < temp.size(); i++) {
                values.add(new Entry(i+1, Float.parseFloat(temp.get(i).getFiberBean().getLoss())));
                avg = avg + Float.parseFloat(temp.get(i).getFiberBean().getLoss());
                averageValues.add(new Entry(i+1, Float.parseFloat(String.format("%.3f", avg / (i+1)))));
            }
            Collections.reverse(temp);
            LineDataSet set1, set2;
            if (chart.getData() != null && chart.getData().getDataSetCount() > 0) {
                set1 = (LineDataSet) chart.getData().getDataSetByIndex(0);
                set2 = (LineDataSet) chart.getData().getDataSetByIndex(1);
                set1.setValues(values);
                set2.setValues(averageValues);
                chart.getData().notifyDataChanged();
                chart.notifyDataSetChanged();
            } else {
                set1 = new LineDataSet(values, "loss");
                set1.setMode(LineDataSet.Mode.LINEAR);


                set1.setCircleColor(getResources().getColor(R.color.purple_700));
                set1.setLineWidth(1);
                set1.setColor(getResources().getColor(R.color.purple_700));

                set2 = new LineDataSet(averageValues, "average");
                set2.setMode(LineDataSet.Mode.LINEAR);
                set2.setCircleColor(getResources().getColor(R.color.blue));
                set2.setLineWidth(1);
                set2.setColor(getResources().getColor(R.color.blue));

                LineData data = new LineData(set1,set2);
                data.setValueTextSize(9f);
                data.setDrawValues(true);
                data.setValueFormatter(new MyValueFormatter());
                chart.setData(data);
                chart.invalidate();
            }
        });
    }


    class MyHandler extends Handler {
        byte[] payload;
        byte[] length;
        int lengthInt;
        byte[] header;
        byte type;
        byte[] id;
        String idStr;
        byte[] totalPackage;
        byte[] currentPackage;
        byte[] receiveBytes = null;
        final WeakReference<Context> mActivity;

        public MyHandler(Context activity) {
            mActivity = new WeakReference<>(activity);
        }

        private boolean isJSONValid(String test) {
            try {
                new JSONObject(test);
            } catch (JSONException e) {
                return false;
            }
            return true;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    receiveBytes = null;
                    byte[] data = (byte[]) msg.obj;
                    if (data == null || data.length <= 0) {
                        return;
                    }
                    Log.i("yot132", "prev：" + BleHexConvert.bytesToHexString(data));

                    receiveBytes = ArrayUtils.addAll(receiveBytes, data);

                    String res = BleHexConvert.bytesToHexString(receiveBytes);
                    byte[] realData = new byte[data.length];
                    System.arraycopy(receiveBytes, 0, realData, 0, data.length);

//                        byte[] test = new byte[]{receiveBytes[0],receiveBytes[1],receiveBytes[2],receiveBytes[3],receiveBytes[4],receiveBytes[5],receiveBytes[6],receiveBytes[7],receiveBytes[8],receiveBytes[9],receiveBytes[10],receiveBytes[11],receiveBytes[12],receiveBytes[13],receiveBytes[14]};
//                        String testData = BleHexConvert.bytesToHexString(test);
//                        Log.d("yot132","test = " + testData);

                    this.header = new byte[]{receiveBytes[1]};
                    this.id = new byte[8];
                    this.id =  Integer.toHexString(1).getBytes();
                    this.idStr = String.valueOf(id);

                    this.totalPackage = new byte[]{receiveBytes[6], receiveBytes[7]};
                    this.currentPackage = new byte[]{receiveBytes[8], receiveBytes[9]};
                    byte[] t = new byte[]{receiveBytes[2],receiveBytes[3]};
                    String encodedData = BleHexConvert.bytesToHexString(t);

                    if(encodedData.contains("0001")) {
                        this.type = 4;
                    }else if (encodedData.contains("1101")){
                        this.type = 2;
                    }else {
                        this.type = 1;
                    }

                    Log.d("yot132","encodedData = " + encodedData);
                    String strData = null;
//                    strData = new String(receiveBytes, StandardCharsets.UTF_8);
//                    mActivity.get().display.append(strData);

                    this.length = new byte[]{receiveBytes[4], receiveBytes[5]};

                    this.lengthInt = ByteUtil.byteArrayToHex(length);



                    if(encodedData.equals("1102")) {
                        byte[] newByte = lastElementRemove(receiveBytes);
                        this.payload = newByte;
//                            String aa = new String(payload, StandardCharsets.UTF_8);
//                            Log.d("yot132","aa = " + aa);
                    }else {
                        this.payload = new byte[lengthInt -4];
                        System.arraycopy(receiveBytes, 10, payload, 0, lengthInt -4);
//                            String dd = new String(payload, StandardCharsets.UTF_8);
//                            Log.d("yot132","bb = " + dd);
                    }


                    boolean isLock = false;
                    if (isLock) {
                        if (res != null && !res.startsWith("aa19")) {
                            if (res.contains("aa19")) {
                                int index = res.indexOf("aa19");
                                receiveBytes = Arrays.copyOfRange(receiveBytes, index / 2, receiveBytes.length);
                            } else {
                                return;
                            }
                        }

                        while (receiveBytes != null && receiveBytes.length >= 5) {
                            short length = ByteBuffer.wrap(receiveBytes, 3, 2).getShort();
                            if (receiveBytes.length >= 7 + length) {
                                receiveBytes = null;
                                usbService.write(BleHexConvert.parseHexStringToBytes("aa11b8000000c9"));
                            } else {
                                break;
                            }
                        }
                    } else {
                        if (res.contains("be11")) {
                            int index = res.indexOf("BE11");
                            receiveBytes = Arrays.copyOfRange(receiveBytes, index / 2, receiveBytes.length);
                            while (receiveBytes != null && receiveBytes.length >= 5) {
                                short length = ByteBuffer.wrap(receiveBytes, 3, 2).getShort();
                                if (receiveBytes.length >= 8 + length) {
                                    String cmd = "";
                                    String cmdType = BleHexConvert.bytesToHexString(new byte[]{receiveBytes[2], receiveBytes[3]});
                                    cmd = cmd + cmdType + "0004" + "0006";
                                    String currentPackage = BleHexConvert.bytesToHexString(new byte[]{receiveBytes[8], receiveBytes[9]});
                                    cmd = cmd + currentPackage;

                                    receiveBytes = null;
                                    Log.d("yot132","img call  =" + currentPackage);
                                    usbService.write(BleHexConvert.parseHexStringToBytes("BE" + cmd + BleCmdUtil.getCRCStr(cmd) + "EB"));
                                } else {
                                    break;
                                }
                            }
                        }
                    }

                    BleResultBean bleResultBean = new BleResultBean();
                    bleResultBean.setCommand(data);
                    bleResultBean.setHeader(header);
                    bleResultBean.setCurrentPackage(currentPackage);
                    bleResultBean.setId(id);
                    bleResultBean.setLength(length);
                    bleResultBean.setIdStr(idStr);
                    bleResultBean.setTotalPackage(totalPackage);
                    bleResultBean.setPayload(payload);

                    rlProgress.setVisibility(View.VISIBLE);
                    String id = idStr;
                    if (type == 1){
                        mSpliceDataBeanMap.put(id, USBSpliceDataParseUtil.parseSpliceImage(getApplicationContext(), mSpliceDataBeanMap.get(id), bleResultBean));
                    }else if (type == 0){
                    }else if (type == 2){
                        mSpliceDataBeanMap.put(id, USBSpliceDataParseUtil.parseSpliceData(getApplicationContext(),mSpliceDataBeanMap.get(id), bleResultBean));
                    }else if (type == 4) {
                        if(!isFirstStart) {
                            String strJson = getAsciiString(payload,0,payload.length);
                            try {
                                // 最外层的JSONObject对象
                                JSONObject object = new JSONObject(strJson);
                                String SN = object.getString("SN");
                                customApplication.swVersion = object.getString("machineSoftVersion");
                                rlProgress.setVisibility(View.GONE);
                                customApplication.connectSerial = SN;
                                mSpliceDataBeanList.clear();
                                for (Map.Entry<String, SpliceDataBean> map : mSpliceDataBeanMap.entrySet()) {
                                    mSpliceDataBeanList.add(map.getValue());
                                }
                                showData(customApplication.connectSerial);
                                isFirstStart = true;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
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
                    setDoc();



                    break;
                case UsbService.CTS_CHANGE:
                    Toast.makeText(mActivity.get(), "CTS_CHANGE",Toast.LENGTH_LONG).show();
                    break;
                case UsbService.DSR_CHANGE:
                    Toast.makeText(mActivity.get(), "DSR_CHANGE",Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }

    public static byte[] lastElementRemove(byte[] srcArray) {
        byte[] newArray = new byte[srcArray.length - 3]; //2byte = CRC16 //1byte = tail
        for(int index = 0; index < srcArray.length - 3; index++) {
            newArray[index] = srcArray[index];
        }
        byte[] tempArray = new byte[newArray.length - 11]; //1byte = header, 2byte cmd, 2byte = length, 2byte = total num, 2byte = current num, 2byte = img index
        for(int i = 11 ; i < newArray.length ; i ++) {
            tempArray[i-11] = newArray[i];
        }
        return tempArray;
    }


    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show();
                    break;

                case UsbService.ACTION_USB_READY:
                    Log.d("yot132","usb connected");
                    break;
            }
        }
    };
}