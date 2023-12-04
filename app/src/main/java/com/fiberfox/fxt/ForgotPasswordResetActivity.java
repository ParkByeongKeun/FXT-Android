package com.fiberfox.fxt;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;


import net.ijoon.auth.ResetPasswordRequest;
import net.ijoon.auth.ResetPasswordResponse;

public class ForgotPasswordResetActivity extends MainAppcompatActivity {

    Button mBtnForgotPw;
    EditText mEtPw;
    EditText mEtRePw;
    CheckBox cbPw;
    RelativeLayout mRlRoot;
    TextView mTvTitle;
    TextView mTvWeb;
    View view;
    CustomApplication customApplication;
    InputMethodManager imm;
    String mStrId;
    String mStrVerifyCode;
    Dialog custom_text_dialog;
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_reset);
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        view = getWindow().getDecorView();
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        customApplication = (CustomApplication)getApplication();
//        ActionBar actionBar = this.getSupportActionBar();
//        actionBar.setBackgroundDrawable(new ColorDrawable(0xffE56731));
//        if (actionBar != null) {
//            actionBar.setDisplayHomeAsUpEnabled(true);
//            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
//            actionBar.setTitle(Html.fromHtml("<font color='#ffffff'>" + "비밀번호 변경" + "</font>"));
//        }
        mStrId = getIntent().getStringExtra("email");
        mStrVerifyCode = getIntent().getStringExtra("VerifycationCode");
        mBtnForgotPw = findViewById(R.id.btnForgotPw);
        mEtPw = findViewById(R.id.etPw);
        mEtRePw = findViewById(R.id.etRePw);
        cbPw = findViewById(R.id.cbPw);
        mRlRoot = findViewById(R.id.rlRoot);
        mTvWeb = findViewById(R.id.tvWeb);
        mTvTitle = findViewById(R.id.tvTitle);
        mTvTitle.setText(mStrId);
        initTextDialog();
        cbPw.setOnClickListener(v -> {
            if(cbPw.isChecked()) {
                mEtPw.setInputType(0);
                mEtRePw.setInputType(0);
            }else {
                mEtPw.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
                mEtRePw.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
            }
        });

        mBtnForgotPw.setOnClickListener(v -> {
            try {
                if(mEtPw.getText().toString().equals(mEtRePw.getText().toString()) && !mEtPw.getText().toString().equals("") && !mEtRePw.getText().toString().equals("")) {
                    ResetPasswordRequest req = ResetPasswordRequest.newBuilder().setEmail(mStrId).setCode(mStrVerifyCode).setPassword(mEtPw.getText().toString()).build();
                    ResetPasswordResponse res = customApplication.authStub.resetPassword(req);
                    showTextDialog("Success");
                    Thread thread = new Thread(() -> {
                        try {
                            Thread.sleep(500);
                            ForgotPasswordResetActivity.super.activityFinish();
                            Intent intent = new Intent(ForgotPasswordResetActivity.this, LoginEmailActivity.class);
                            startActivity(intent);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    });
                    thread.start();
                }else {
                    showTextDialog("Check information");
                }
            }catch (RuntimeException e) {
                if(e.getMessage().contains("UNAVAILABLE")) {
                    showTextDialog("Server Error");
                }else {
                    showTextDialog("Check information");
                }
            }
        });

        mTvWeb.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.fiberfox.co.kr/"));
            startActivity(intent);
        });

        mRlRoot.setOnClickListener(v -> imm.hideSoftInputFromWindow(mEtPw.getWindowToken(), 0));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ForgotPasswordResetActivity.this,LoginEmailActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(0,0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(ForgotPasswordResetActivity.this,LoginEmailActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(0,0);
        return super.onOptionsItemSelected(item);
    }


    public void initTextDialog() {
        custom_text_dialog = new Dialog(this);
        custom_text_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        custom_text_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        custom_text_dialog.setContentView(R.layout.custom_dialog_completed);
        tv = custom_text_dialog.findViewById(R.id.tvTitle);
    }

    public void showTextDialog(String title) {
        tv.setText(title);
        custom_text_dialog.show();
        custom_text_dialog.findViewById(R.id.btnOk).setOnClickListener(v -> {
            custom_text_dialog.dismiss();
        });
    }
}