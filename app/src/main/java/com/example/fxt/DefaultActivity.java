package com.example.fxt;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;

public class DefaultActivity extends MainAppcompatActivity {

    Button mBtnJoin;
    Button mBtnLogin;
    TextView mTvVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        mBtnJoin = findViewById(R.id.btnJoin);
        mBtnLogin = findViewById(R.id.btnLogin);
        mTvVersion = findViewById(R.id.tvVersion);
        getWindow().setStatusBarColor(Color.parseColor("#EA8235"));//statusBar
        getWindow().setNavigationBarColor(Color.parseColor("#EA8235"));//bottom
        mBtnJoin.setOnClickListener(v -> {
            Intent intent = new Intent(DefaultActivity.this,JoinActivity.class);
            startActivity(intent);
        });

        mBtnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(DefaultActivity.this,SignInActivity.class);
            startActivity(intent);
        });

        mTvVersion.setText(getAppVersionName());
    }

    public String getAppVersionName(){
        PackageInfo packageInfo;
        try{
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        }catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
            return "";
        }
        return packageInfo.versionName;
    }
}