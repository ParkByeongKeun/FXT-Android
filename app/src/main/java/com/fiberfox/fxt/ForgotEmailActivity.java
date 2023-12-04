package com.fiberfox.fxt;

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

import com.fiberfox.fxt.utils.NetworkStatus;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ForgotEmailActivity extends Activity {

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
        setContentView(R.layout.activity_forgot_email);
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        view = getWindow().getDecorView();
        mRlRoot = findViewById(R.id.rlRoot);
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        customApplication = (CustomApplication)getApplication();
        mTvJoin = findViewById(R.id.tvJoin);
        mTvWeb = findViewById(R.id.tvWeb);
        mTvForgotEmail = findViewById(R.id.forgotEmail);
        initTextDialog();
        Button button = findViewById(R.id.btnLogin);
        mTvWeb.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.fiberfox.co.kr/"));
            startActivity(intent);
        });
        button.setOnClickListener(v -> {
            Intent intent = new Intent(ForgotEmailActivity.this, LoginPasswordActivity.class);
            startActivity(intent);
        });

        mBtnId = findViewById(R.id.btnLogin);
        mEtId = findViewById(R.id.etId);
        mBtnId.setOnClickListener(v -> {

            mStrId = mEtId.getText().toString();
            boolean isEmail = checkEmail(mStrId);
            if(isEmail) {
                login(mStrId);
            }else {
                showTextDialog("check Email");
            }

        });
        mTvJoin.setOnClickListener(v -> {
            Intent intent = new Intent(ForgotEmailActivity.this,JoinActivity.class);
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
        Intent intent = new Intent(ForgotEmailActivity.this, ForgotPasswordActivity.class);
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
        Intent intent = new Intent(ForgotEmailActivity.this, ForgotEmailActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(0,0);
        return super.onOptionsItemSelected(item);
    }

    public static boolean checkEmail(String email){

        String regex = "^[_a-zA-Z0-9-\\.]+@[\\.a-zA-Z0-9-]+\\.[a-zA-Z]+$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(email);
        boolean isNormal = m.matches();
        return isNormal;

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