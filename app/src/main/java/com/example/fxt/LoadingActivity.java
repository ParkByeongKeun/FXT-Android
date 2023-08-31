package com.example.fxt;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;

import java.util.List;

public class LoadingActivity extends Activity {

    View view;
    Thread loadingThread;
    CustomApplication customApplication;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        customApplication = (CustomApplication)getApplication();
        view = getWindow().getDecorView();
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        TedPermission.create()
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE).check();

        loadingThread = new Thread(() -> {
            try {
                Thread.sleep(2000);
                if(customApplication.isLogin) {
                    Intent intent = new Intent(LoadingActivity.this, OFIFNMSActivity.class);
                    startActivity(intent);
                    finish();
                }else {
                    Intent intent = new Intent(LoadingActivity.this, DefaultActivity.class);
                    startActivity(intent);
                    finish();
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        loadingThread.start();
        getWindow().setStatusBarColor(Color.parseColor("#EA8235"));//statusBar
        getWindow().setNavigationBarColor(Color.parseColor("#EA8235"));//bottom
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(loadingThread != null) {
            if(loadingThread.isAlive()) {
                loadingThread.interrupt();
            }
        }
    }

    PermissionListener permissionlistener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            loadingThread = new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    if(customApplication.isLogin) {
                        Intent intent = new Intent(LoadingActivity.this, OFIFNMSActivity.class);
                        startActivity(intent);
                        finish();
                    }else {
                        Intent intent = new Intent(LoadingActivity.this, DefaultActivity.class);
                        startActivity(intent);
                        finish();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            loadingThread.start();
        }

        @Override
        public void onPermissionDenied(List<String> deniedPermissions) {
            TedPermission.create()
                    .setPermissionListener(permissionlistener)
                    .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                    .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE).check();
        }
    };
}