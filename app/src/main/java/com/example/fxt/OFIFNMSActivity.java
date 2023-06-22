package com.example.fxt;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.ActionBar;
import com.example.fxt.utils.BackPressCloseHandler;
import com.example.fxt.utils.C_Permission;
import com.example.fxt.utils.CustomDevice;
import com.example.fxt.widget.XScrollView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class OFIFNMSActivity extends MainAppcompatActivity implements XScrollView.IXScrollViewListener {

    private Handler mHandler;
    private ArrayList<CustomDevice> mItems = new ArrayList<CustomDevice>();
    ActionBar mTitle;
    CustomApplication customApplication;
    private BackPressCloseHandler mBackPressCloseHandler;
    XScrollView mScrollView;
    Dialog custom_dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ofi_fnms);
        mBackPressCloseHandler = new BackPressCloseHandler(this);
        C_Permission.checkPermission(this);
        mBackPressCloseHandler = new BackPressCloseHandler(this);
        customApplication = (CustomApplication)getApplication();
        mTitle = getSupportActionBar();
        getWindow().setStatusBarColor(Color.parseColor("#FFE2D1"));//statusBar
        mTitle.hide();
        initView();
    }

    private String getTime() {
        return new SimpleDateFormat("MM-dd HH:mm", Locale.KOREA).format(new Date());
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
        content.findViewById(R.id.rl_splice).setOnTouchListener((view, motionEvent) -> {
            if(customApplication.isLogin) {
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
                    Intent intent = new Intent(OFIFNMSActivity.this, SignInActivity.class);
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
            if(customApplication.isLogin) {
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
                        Intent intent = new Intent(OFIFNMSActivity.this, SignInActivity.class);
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
            if(customApplication.isLogin) {
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
                        Intent intent = new Intent(OFIFNMSActivity.this, SignInActivity.class);
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
        mScrollView.setView(content);
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
}