package com.fiberfox.fxt;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.fiberfox.fxt.R;
import com.fiberfox.fxt.utils.BackPressCloseHandler;
import com.fiberfox.fxt.utils.C_Permission;
import com.fiberfox.fxt.utils.CustomDevice;
import com.fiberfox.fxt.widget.XScrollView;

import net.ijoon.auth.UserRequest;
import net.ijoon.auth.UserResponse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import io.grpc.StatusRuntimeException;

public class OFIFNMSActivity extends MainAppcompatActivity implements XScrollView.IXScrollViewListener {

    private Handler mHandler;
    private ArrayList<CustomDevice> mItems = new ArrayList<CustomDevice>();
    ActionBar mTitle;
    CustomApplication customApplication;
    private BackPressCloseHandler mBackPressCloseHandler;
    XScrollView mScrollView;
    Dialog custom_dialog;
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ofi_fnms);
        mBackPressCloseHandler = new BackPressCloseHandler(this);
        C_Permission.checkPermission(this);
        mBackPressCloseHandler = new BackPressCloseHandler(this);
        customApplication = (CustomApplication)getApplication();
        SharedPreferences preferences = getSharedPreferences("preferences",MODE_PRIVATE);
        SharedPreferences.Editor editor =  preferences.edit();
        requestReadExternalStoragePermission();
        try {
            UserRequest req = UserRequest.newBuilder().build();
            UserResponse res = customApplication.authStub.getUser(req);
            editor.putString("email", res.getUsers().getId());
            editor.apply();
            customApplication.token = preferences.getString("token","");
            customApplication.login_id = res.getUsers().getId();
            customApplication.loginKey = preferences.getString("loginKey","");
        }  catch (StatusRuntimeException ee) {

            Log.d("yot132","ee = " + ee.getStatus().toString());
            ee.printStackTrace();
        }
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
//        } else {
//            // 권한이 이미 허용된 경우
//            // 여기에 파일 저장 등의 코드 추가
//        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if(shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, "Permission denied. App cannot save files.", Toast.LENGTH_SHORT).show();
                }

                requestPermissions(new String[]
                        {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
            }
        }
        requestCameraPermission();
        //splice
        customApplication.arrSpliceBleAddress = customApplication.getStringArrayPref(this,customApplication.login_id);
        customApplication.arrSpliceBleSerial = customApplication.getStringArrayPref(this,customApplication.login_id+"serial");
        customApplication.arrSpliceBleVersion = customApplication.getStringArrayPref(this,customApplication.login_id+"version");
        //ofi
        customApplication.arrBleAddress = customApplication.getStringArrayPref(this,customApplication.login_id+"ofi");
        customApplication.arrBleSerial = customApplication.getStringArrayPref(this,customApplication.login_id+"ofi_serial");

        Log.d("yot132","customApplication.token = " + customApplication.token);
        mTitle = getSupportActionBar();
        getWindow().setStatusBarColor(Color.parseColor("#FFE2D1"));//statusBar
        mTitle.hide();
        initView();
    }

    private String getTime() {
        return new SimpleDateFormat("MM-dd HH:mm", Locale.KOREA).format(new Date());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 허용된 경우
                // 여기에 파일 저장 등의 코드 추가
            } else {
                // 권한이 거부된 경우
                Toast.makeText(this, "Permission denied. App cannot save files.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    protected void initView() {
        mHandler = new Handler();
        mScrollView = findViewById(R.id.scrollView);
        mScrollView.setPullRefreshEnable(true);
        mScrollView.setPullLoadEnable(true);
        mScrollView.setAutoLoadEnable(true);
        mScrollView.setIXScrollViewListener(this);
        mScrollView.setRefreshTime(getTime());
        View content = LayoutInflater.from(this).inflate(R.layout.vw_scroll_view_main, null);
        content.findViewById(R.id.tvInfo).setOnClickListener(v -> {
            Intent intent = new Intent(OFIFNMSActivity.this,SettingActivity.class);
            startActivity(intent);
        });

        content.findViewById(R.id.rl_splice).setOnTouchListener((view, motionEvent) -> {
            if(!customApplication.token.equals("")) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_UP: {
                        Intent intent = new Intent(OFIFNMSActivity.this, SpliceActivity.class);
                        startActivity(intent);
                        break;
                    }
                    case MotionEvent.ACTION_DOWN: {
                        break;
                    }
                    default:
                        break;
                }
            }else {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_UP:{
                    Intent intent = new Intent(OFIFNMSActivity.this, LoginEmailActivity.class);
                    startActivity(intent);
                    break;
                }
                case MotionEvent.ACTION_DOWN: {
                    break;
                }
                default:
                    break;
            }
        }
            return true;
        });
        content.findViewById(R.id.rl_ofi).setOnTouchListener((view, motionEvent) -> {
            if(!customApplication.token.equals("")) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_UP: {
                        Intent intent = new Intent(OFIFNMSActivity.this, MainActivity.class);
                        startActivity(intent);
                        break;
                    }
                    case MotionEvent.ACTION_DOWN: {
                        break;
                    }
                    default:
                        break;
                }
            }else {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_UP:{
                        Intent intent = new Intent(OFIFNMSActivity.this, LoginEmailActivity.class);
                        startActivity(intent);
                        break;
                    }
                    case MotionEvent.ACTION_DOWN: {
                        break;
                    }
                    default:
                        break;
                }
            }
            return true;
        });

        content.findViewById(R.id.rl_fnms).setOnTouchListener((view, motionEvent) -> {
            if(!customApplication.token.equals("")) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_UP:{
                        Intent intent = new Intent(OFIFNMSActivity.this,FNMSTAGActivity.class);
                        startActivity(intent);
                        break;
                    }
                    case MotionEvent.ACTION_DOWN: {
                        break;
                    }
                    default:
                        break;
                }
            }else {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_UP:{
                        Intent intent = new Intent(OFIFNMSActivity.this, LoginEmailActivity.class);
                        startActivity(intent);
                        break;
                    }
                    case MotionEvent.ACTION_DOWN: {
                        break;
                    }
                    default:
                        break;
                }
            }

            return true;
        });

        content.findViewById(R.id.rl_settings).setOnTouchListener((view, motionEvent) -> {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_UP:{
                    Intent intent = new Intent(OFIFNMSActivity.this,TutorialActivity.class);
                    startActivity(intent);
                    break;
                }
                case MotionEvent.ACTION_DOWN: {
                    break;
                }
                default:
                    break;
            }

            return true;
        });

        content.findViewById(R.id.rl_otdr).setOnTouchListener((view, motionEvent) -> {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_UP:{
                    Intent intent = new Intent(OFIFNMSActivity.this,EnclosureHistoryActivity.class);
                    startActivity(intent);
                    break;
                }
                case MotionEvent.ACTION_DOWN: {
                    break;
                }
                default:
                    break;
            }

            return true;
        });
        mScrollView.setView(content);
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
    public void onRefresh() {
        mHandler.postDelayed(() -> {
            mItems.clear();
            onLoad();
        }, 2500);
    }

    @Override
    public void onLoadMore() {
        mHandler.postDelayed(() -> onLoad(), 2500);
    }

    private void onLoad() {
        mScrollView.stopRefresh();
        mScrollView.stopLoadMore();
        mScrollView.setRefreshTime(getTime());
    }

    @Override
    public void onBackPressed() {
        mBackPressCloseHandler.onBackPressed();
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

    // 권한 요청 메서드
    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            // 이미 권한이 부여되어 있음
            // 카메라 초기화 또는 다른 관련 작업 수행
        }
    }
}