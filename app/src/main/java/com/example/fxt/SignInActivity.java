package com.example.fxt;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.dd.processbutton.iml.ActionProcessButton;
import com.example.fxt.utils.ProgressGenerator;
import com.google.android.material.textfield.TextInputEditText;
import com.kongzue.dialogx.dialogs.MessageDialog;

public class SignInActivity extends MainAppcompatActivity implements ProgressGenerator.OnCompleteListener {

    public static final String EXTRAS_ENDLESS_MODE = "EXTRAS_ENDLESS_MODE";
    String authId = "fiberfox";
    String authPw = "1234";
    ActionProcessButton btnSignIn;
    TextInputEditText editEmail;
    TextInputEditText editPassword;
    InputMethodManager imm;
    RelativeLayout mRlRoot;
    ActionBar mTitle;
    TextView tvJoin;
    CustomApplication customApplication;
    Dialog custom_dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        customApplication = (CustomApplication)getApplication();
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        mRlRoot = findViewById(R.id.rlMain);
        mRlRoot.setOnClickListener(v -> imm.hideSoftInputFromWindow(editEmail.getWindowToken(), 0));
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        mTitle = getSupportActionBar();
        mTitle.setCustomView(null);
        mTitle.setDisplayShowCustomEnabled(true);
        mTitle.hide();
        initDialog();
        getWindow().setStatusBarColor(Color.parseColor("#EA8235"));//statusBar
        getWindow().setNavigationBarColor(Color.parseColor("#EA8235"));//bottom
        final ProgressGenerator progressGenerator = new ProgressGenerator(this);
        btnSignIn = findViewById(R.id.btnSignIn);
        Bundle extras = getIntent().getExtras();
        if(extras != null && extras.getBoolean(EXTRAS_ENDLESS_MODE)) {
            btnSignIn.setMode(ActionProcessButton.Mode.ENDLESS);
        } else {
            btnSignIn.setMode(ActionProcessButton.Mode.PROGRESS);
        }

        findViewById(R.id.forgotEmail).setOnClickListener(v -> {
//            Intent intent = new Intent(SignInActivity.this, ForgotPasswordActivity.class);
//            startActivity(intent);
        });
//        findViewById(R.id.tvJoin).setOnClickListener(v -> {
//            Intent intent = new Intent(SignInActivity.this, JoinActivity.class);
//            startActivity(intent);
//        });

        btnSignIn.setOnClickListener(v -> {
            if(authId.equals(editEmail.getText().toString()) &&
            authPw.equals(editPassword.getText().toString())) {
                customApplication.isLogin = true;
                SharedPreferences sharedPreferences = this.getSharedPreferences("login",MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("login",true);
                editor.apply();
                progressGenerator.start(btnSignIn);
                btnSignIn.setEnabled(false);
                editEmail.setEnabled(false);
                editPassword.setEnabled(false);
            }else {
                showDialog();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        btnSignIn.setProgress(0);
        btnSignIn.setEnabled(true);
        editEmail.setEnabled(true);
        editPassword.setEnabled(true);
    }

    @Override
    public void onComplete() {
        Intent intent = new Intent(SignInActivity.this,OFIFNMSActivity.class);
        startActivity(intent);
        activityFinish();
    }

    public void initDialog() {
        custom_dialog = new Dialog(this);
        custom_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        custom_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        custom_dialog.setContentView(R.layout.custom_dialog_completed);
        TextView tv = custom_dialog.findViewById(R.id.tvTitle);
        tv.setText("Check your input information");
    }

    public void showDialog() {
        custom_dialog.show();
        custom_dialog.findViewById(R.id.btnOk).setOnClickListener(v -> {
            custom_dialog.dismiss();
        });
    }
}