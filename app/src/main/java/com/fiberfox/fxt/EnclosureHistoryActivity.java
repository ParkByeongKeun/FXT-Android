package com.fiberfox.fxt;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.fiberfox.fxt.ble.device.splicer.bean.EnclosureDataBean;
import com.fiberfox.fxt.ble.device.splicer.bean.OFIDataBean;
import com.fiberfox.fxt.utils.ConstantUtil;
import com.fiberfox.fxt.utils.CustomHistoryList;
import com.fiberfox.fxt.utils.EnclosureDataAdapter;
import com.fiberfox.fxt.utils.OfiDataAdapter;
import com.fiberfox.fxt.widget.XListView;
import com.github.mikephil.charting.charts.LineChart;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EnclosureHistoryActivity extends MainAppcompatActivity implements XListView.IXListViewListener {

    private Handler mHandler;
    private int mIndex = 0;
    private int mRefreshIndex = 0;
    private ArrayList<CustomHistoryList> mItems = new ArrayList<CustomHistoryList>();
    XListView listView;
    CustomApplication customApplication;
    ImageView menuItemShare;
    ImageView menuItemDelete;
    private Map<String, EnclosureDataBean> mSpliceDataBeanMap;
    private List<EnclosureDataBean> mOFIDataBeanList;
    private EnclosureDataAdapter spliceDataAdapter;

    ActionBar mTitle;
    List<String> fileList;
    SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    RelativeLayout rlProgress;
    Dialog custom_dialog;
    Dialog custom_delete_dialog;
    Button btn_camera;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enclosure_history);
        requestReadExternalStoragePermission();
        btn_camera = findViewById(R.id.btn_camera);
        menuItemShare = findViewById(R.id.iv_share);
        menuItemDelete = findViewById(R.id.iv_delete);
        menuItemDelete.setVisibility(View.INVISIBLE);
        menuItemShare.setVisibility(View.INVISIBLE);
        initDialog();
        initDeleteDialog();
        btn_camera.setOnClickListener(v -> {
            Intent intent = new Intent(EnclosureHistoryActivity.this,EnclosureActivity.class);
            startActivity(intent);
        });

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
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            menuItemShare.setVisibility(View.INVISIBLE);
            menuItemDelete.setVisibility(View.INVISIBLE);
            mOFIDataBeanList.clear();
            showData(customApplication.connectSerial);
            setDoc();
            boolean check = false;
            for(int i = 0 ; i < checkedItems.size() ; i ++) {
                if(checkedItems.valueAt(i)) {
                    check = true;
                }
            }
            if(check) {
//                showAlertDialog("Delete history datas.","0");
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
        customApplication = (CustomApplication) getApplication();
        listView = findViewById(R.id.listView_);
        mSpliceDataBeanMap = new HashMap<>();
        mOFIDataBeanList = new ArrayList<>();
        TextView mFusionDataTextTip = findViewById(R.id.fusion_data_list_tv_tip);
        listView.setEmptyView(mFusionDataTextTip);
        rlProgress = findViewById(R.id.rlProgress);
        rlProgress.bringToFront();
        spliceDataAdapter = new EnclosureDataAdapter(mOFIDataBeanList, getApplicationContext());
        listView.setAdapter(spliceDataAdapter);
        mTitle = getSupportActionBar();
        mTitle.setCustomView(null);
        mTitle.setDisplayShowCustomEnabled(true);
        mTitle.setTitle("Enclosure History List");
        mTitle.setBackgroundDrawable(new ColorDrawable(0xffE56731));
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mOFIDataBeanList.clear();
        showData(customApplication.connectSerial);
        setDoc();
    }

    public void setDoc() {
        fileList = FileList("Download");
        for(int i = 0; i < mOFIDataBeanList.size() ; i ++) {
            int check = 0;
            String date = mOFIDataBeanList.get(i).getDataTime();
            for(int j = 0 ; j < fileList.size() ; j ++) {
                String[] pathName = date.split(" ");
                String pathTime= pathName[1].replaceAll(":", "_");
                String excelPath =  customApplication.login_id + "_" + pathName[0]+"_"+pathTime +".xls";
                String pdfPath = customApplication.login_id + "_" + pathName[0]+"_"+pathTime +".pdf";
                if(pdfPath.equals(fileList.get(j))) {
                    check = check +1;
                }
                if(excelPath.equals(fileList.get(j))) {
                    check = check +10;
                }
            }
            if(check == 1) {
                mOFIDataBeanList.get(i).setNote("0");
            }
            if(check >= 10) {
                mOFIDataBeanList.get(i).setNote("1");
            }
            if(check >= 11) {
                mOFIDataBeanList.get(i).setNote("2");
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
            for(int j = 1; j <= mOFIDataBeanList.size() ; j ++) {
                listView.setItemChecked(j, false);
            }
            menuItemShare.setVisibility(View.VISIBLE);
            menuItemDelete.setVisibility(View.VISIBLE);
            spliceDataAdapter.setCheckBoxVisible(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
            mOFIDataBeanList.clear();
            showData(customApplication.connectSerial);
            setDoc();
            return true;
        });
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if(!spliceDataAdapter.getCheckBox()) {
                Intent intent = new Intent(getApplicationContext(), EnclosureDetailActivity.class);
                intent.putExtra("img",mOFIDataBeanList.get(position - 1).getImagePath());
                intent.putExtra("coordinate",mOFIDataBeanList.get(position - 1).getCoordinate());
                intent.putExtra("date",mOFIDataBeanList.get(position - 1).getDataTime());
                startActivity(intent);
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
        mOFIDataBeanList.clear();
        showData(customApplication.connectSerial);
        setDoc();
        listView.stopRefresh();
        listView.stopLoadMore();
        listView.setRefreshTime(getTime());
    }

    private String getTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA).format(new Date());
    }

//    public void showAlertDialog(String message, String position) {
//        MessageDialog.show("Alarm", message, "YES")
//                .setOkButtonClickListener((dialog, v) -> {
//                    SparseBooleanArray checkedItems = listView.getCheckedItemPositions();
//                    for(int i = 0 ; i < checkedItems.size() ; i++) {
//                        if(checkedItems.valueAt(i)) {
//                            String pos = mOFIDataBeanList.get(checkedItems.keyAt(i) - 1).getId();
//                            customApplication.ofiDatabase.deleteById(Integer.parseInt(pos));
//                        }
//                    }
//                    for(int i = 1; i <= mOFIDataBeanList.size() ; i ++) {
//                        listView.setItemChecked(i, false);
//                    }
//                    mOFIDataBeanList.clear();
//                    showData(customApplication.connectSerial);
//                    setDoc();
//                    return false;
//                });
//    }

    public void showData(String serial) {
        List<EnclosureDataBean> dataList = new ArrayList<>();
        dataList.addAll(customApplication.enclosureDatabase.selectAllSpliceData());
        Log.d("yot132","dataList.size() = " + dataList.size());
        for(int i = 0 ; i < dataList.size() ; i ++) {
            Log.d("yot132","?1 = " + dataList.get(i).getCoordinate());
            if(dataList.get(i).getUser().equals(customApplication.login_id)) {
                mOFIDataBeanList.add(dataList.get(i));
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home: {
                menuItemShare.setVisibility(View.INVISIBLE);
                menuItemDelete.setVisibility(View.INVISIBLE);
                spliceDataAdapter.setCheckBoxVisible(false);
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                mOFIDataBeanList.clear();
                showData(customApplication.connectSerial);
                for(int i = 1; i <= mOFIDataBeanList.size() ; i ++) {
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
            mOFIDataBeanList.clear();
            showData(customApplication.connectSerial);
            setDoc();
        }else {
            this.finish();
        }
    }

//    public void showAlertDialog(String message) {
//        MessageDialog.show("Alarm", message, "YES","NO")
//                .setOkButtonClickListener((dialog, v) -> {
//                    spliceDataAdapter.setCheckBoxVisible(false);
//                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//                    menuItemShare.setVisibility(View.INVISIBLE);
//                    menuItemDelete.setVisibility(View.INVISIBLE);
//                    mOFIDataBeanList.clear();
//                    showData(customApplication.connectSerial);
//                    showDialog();
//                    setDoc();
//                    return false;
//                });
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
                    String date = mOFIDataBeanList.get(i).getDataTime();
                    String[] pathName = date.split(" ");
                    String pathTime= pathName[1].replaceAll(":", "_");
                    String pdfPath = customApplication.login_id + "_" + pathName[0]+"_"+pathTime +".pdf";
                    someFile = pdfPath;
                    File filelocation = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), someFile);
                    Uri path = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", filelocation);
                    if(!filelocation.canRead()) {
                        Toast.makeText(getApplicationContext(),"No documentation",Toast.LENGTH_SHORT).show();
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
            mOFIDataBeanList.clear();
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
                    String date = mOFIDataBeanList.get(i).getDataTime();
                    String[] pathName = date.split(" ");
                    String pathTime= pathName[1].replaceAll(":", "_");
                    String pdfPath = customApplication.login_id + "_" + pathName[0]+"_"+pathTime +".xls";
                    someFile = pdfPath;
                    File filelocation = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), someFile);
                    Uri path = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", filelocation);
                    if(!filelocation.canRead()) {
                        Toast.makeText(getApplicationContext(),"No documentation",Toast.LENGTH_SHORT).show();
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
            mOFIDataBeanList.clear();
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
                    String pos = mOFIDataBeanList.get(checkedItems.keyAt(i) - 1).getId();
                    customApplication.enclosureDatabase.deleteById(Integer.parseInt(pos));
                }
            }
            for(int i = 1; i <= mOFIDataBeanList.size() ; i ++) {
                listView.setItemChecked(i, false);
            }
            mOFIDataBeanList.clear();
            showData(customApplication.connectSerial);
            setDoc();
            custom_delete_dialog.dismiss();
        });
    }
}