package com.example.fxt;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.example.fxt.utils.NetworkStatus;

public class LoginEmailActivity extends Activity {

    Button mBtnId;
    EditText mEtId;
    TextView mTvJoin;
    TextView mTvForgotEmail;
    TextView mTvWeb;
    View view;
    String mStrId;
    CustomApplication customApplication;
    InputMethodManager imm;
    RelativeLayout mRlRoot;
    Dialog custom_text_dialog;
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_email);
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        view = getWindow().getDecorView();
        mRlRoot = findViewById(R.id.rlRoot);
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        customApplication = (CustomApplication)getApplication();
        mTvJoin = findViewById(R.id.tvJoin);
        mTvWeb = findViewById(R.id.tvWeb);
        mTvForgotEmail = findViewById(R.id.forgotEmail);
        Button button = findViewById(R.id.btnLogin);
        initTextDialog();
        mTvWeb.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.fiberfox.co.kr/"));
            startActivity(intent);
        });
        button.setOnClickListener(v -> {
            Intent intent = new Intent(LoginEmailActivity.this, LoginPasswordActivity.class);
            startActivity(intent);
        });
//        TextView forgot_button = findViewById(R.id.forgotEmail);
//        forgot_button.setOnClickListener(v -> {
//            Intent intent = new Intent(LoginEmailActivity.this, ForgotEmailActivity.class);
//            startActivity(intent);
//        });

        mBtnId = findViewById(R.id.btnLogin);
        mEtId = findViewById(R.id.etId);
        mBtnId.setOnClickListener(v -> {
            mStrId = mEtId.getText().toString();
            login(mStrId);
        });
//        mTvForgotEmail.setOnClickListener(v -> {
//            Intent intent = new Intent(LoginEmailActivity.this,ForgotEmailActivity.class);
//            startActivity(intent);
//        });
        mTvJoin.setOnClickListener(v -> {
            Intent intent = new Intent(LoginEmailActivity.this,JoinActivity.class);
            startActivity(intent);
        });
        mRlRoot.setOnClickListener(v -> imm.hideSoftInputFromWindow(mEtId.getWindowToken(), 0));
    }

    void login(String id) {
        int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
        if(status == NetworkStatus.TYPE_NOT_CONNECTED){
            showTextDialog("Connecting to server");
            return;
        }
        Intent intent = new Intent(LoginEmailActivity.this, LoginPasswordActivity.class);
        intent.putExtra("email",id);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(LoginEmailActivity.this,LoginEmailActivity.class);
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