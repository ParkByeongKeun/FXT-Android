package com.fiberfox.fxt;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;


import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;

import java.util.List;

public class LoadingActivity extends Activity {

    View view;
    Thread loadingThread;
    CustomApplication customApplication;
    String mToken;

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
                .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION).check();

        loadingThread = new Thread(() -> {
            try {
                Thread.sleep(1000);
                if(!customApplication.token.equals("")) {
                    try {
//                        UpdateTokenRequest updateTokenRequest = net.ijoon.auth.UpdateTokenRequest.newBuilder().build();
//                        UpdateTokenResponse updateTokenResponse = customApplication.authStub.updateToken(updateTokenRequest);
//                        customApplication.token = updateTokenResponse.getAccessToken();
//                        SharedPreferences preferences = getSharedPreferences("preferences",MODE_PRIVATE);
//                        SharedPreferences.Editor editor =  preferences.edit();
//                        editor.putString("token", customApplication.token);
//                        editor.apply();
                        customApplication.setMetaData();
                        Intent intent = new Intent(LoadingActivity.this, OFIFNMSActivity.class);
                        startActivity(intent);
                        finish();
                    }catch (RuntimeException e) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(() -> Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show(), 0);

                        Intent intent = new Intent(LoadingActivity.this, LoginEmailActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }else {
                    Intent intent = new Intent(LoadingActivity.this, LoginEmailActivity.class);
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
                    Thread.sleep(1000);
                    if(!customApplication.token.equals("")) {
                        try {
                            Log.d("yot132","123 = " + customApplication.token);

//                            UpdateTokenRequest updateTokenRequest = net.ijoon.auth.UpdateTokenRequest.newBuilder().build();
//                            UpdateTokenResponse updateTokenResponse = customApplication.authStub.updateToken(updateTokenRequest);
//                            customApplication.token = updateTokenResponse.getAccessToken();
//                            SharedPreferences preferences = getSharedPreferences("preferences",MODE_PRIVATE);
//                            SharedPreferences.Editor editor =  preferences.edit();
//                            editor.putString("token", customApplication.token);
//                            editor.apply();
                            customApplication.setMetaData();
                            Intent intent = new Intent(LoadingActivity.this, OFIFNMSActivity.class);
                            startActivity(intent);
                            finish();
                        }catch (RuntimeException e) {
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.postDelayed(() -> Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show(), 0);

                            Intent intent = new Intent(LoadingActivity.this, LoginEmailActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }else {
                        Intent intent = new Intent(LoadingActivity.this, LoginEmailActivity.class);
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
                    .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION).check();
        }
    };
}