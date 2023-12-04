package com.fiberfox.fxt;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;

import com.fiberfox.fxt.R;
import com.fiberfox.fxt.utils.LogoutItem;
import com.fiberfox.fxt.utils.NetworkStatus;

import net.ijoon.auth.LoginRequest;
import net.ijoon.auth.LoginResponse;
import net.ijoon.auth.LogoutWithAccountRequest;
import net.ijoon.auth.LogoutWithAccountResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import io.grpc.StatusRuntimeException;

public class LoginPasswordActivity extends MainAppcompatActivity {

    Button mBtnLogin;
    EditText mEtPassword;
    View view;
    String mStrId;
    String mStrPassword;
    CustomApplication customApplication;
    InputMethodManager imm;
    RelativeLayout mRlRoot;
    CheckBox cbPw;
    RelativeLayout rlLoading;
    Dialog custom_dialog;
    Dialog custom_text_dialog;
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_password);
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        view = getWindow().getDecorView();
        cbPw = findViewById(R.id.cbPw);
        cbPw.setOnClickListener(v -> {
            if(cbPw.isChecked()) {
                mEtPassword.setInputType(0);
            }else {
                mEtPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
            }
        });
        initTextDialog();
        mStrId = getIntent().getStringExtra("email");
        rlLoading = findViewById(R.id.rlLoading);
        rlLoading.setOnTouchListener((v, event) -> true);
        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText(mStrId);
        ActionBar actionBar = this.getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(0xffE56731));
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        customApplication = (CustomApplication)getApplication();
        TextView forgot_button = findViewById(R.id.forgotPassword);
        forgot_button.setOnClickListener(v -> {
            try {
                Thread thread = new Thread(() -> {
                    try {
                        Thread.sleep(100);
//                        SendCodeRequest updatePasswordSendCodeRequest = SendCodeRequest.newBuilder().setEmail(mStrId).build();
//                        SendCodeResponse res = customApplication.authStub.sendResetCode(updatePasswordSendCodeRequest);
                        Intent intent = new Intent(LoginPasswordActivity.this, ForgotEmailActivity.class);
//                        intent.putExtra("email",mStrId);
                        startActivity(intent);
//                        runOnUiThread(() -> {
//                            rlLoading.setVisibility(View.GONE);
//                        });
                    } catch (StatusRuntimeException ee) {
                        Log.d("yot132","ee = " + ee);
                        Toast.makeText(getApplicationContext(),ee.toString(),Toast.LENGTH_SHORT).show();
                        ee.printStackTrace();
                    } catch (InterruptedException e) {
                        Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                });
                thread.start();
            }catch (RuntimeException e) {
                if(e.getMessage().contains("UNAVAILABLE")) {
                    showTextDialog("Connecting to server");
                }else {
                    showTextDialog("check information");
                }
                Thread thread = new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                           runOnUiThread(() -> {
                            rlLoading.setVisibility(View.GONE);
                        });
                    } catch (InterruptedException ee) {
                        ee.printStackTrace();
                    }
                });
                thread.start();
            }
        });
        mBtnLogin = findViewById(R.id.btnLogin);
        mEtPassword = findViewById(R.id.etPw);
        mRlRoot = findViewById(R.id.rlRoot);
        mBtnLogin.setOnClickListener(v -> {
            mStrPassword = mEtPassword.getText().toString();
            login(mStrId,mStrPassword);
        });
        mRlRoot.setOnClickListener(v -> imm.hideSoftInputFromWindow(mEtPassword.getWindowToken(), 0));
    }

    void login(String id, String pw) {
        int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
        if(status == NetworkStatus.TYPE_NOT_CONNECTED){
            showTextDialog("Connecting to server");
            return;
        }
        try {
            LoginRequest loginRequest = LoginRequest.newBuilder().setId(id).setPassword(pw).build();
            LoginResponse loginResponse = customApplication.authStub.login(loginRequest);
            SharedPreferences preferences = getSharedPreferences("preferences",MODE_PRIVATE);
            SharedPreferences.Editor editor =  preferences.edit();
            editor.putString("loginKey",loginResponse.getLoginKey());
            editor.putString("token", loginResponse.getAccessToken());
            editor.putString("email", id);
            editor.apply();
            customApplication.token = loginResponse.getAccessToken();
            customApplication.loginKey = loginResponse.getLoginKey();
            customApplication.login_id = id;
            customApplication.setMetaData();
            Intent intent = new Intent(LoginPasswordActivity.this,OFIFNMSActivity.class);
            startActivity(intent);
            finish();
        }catch (RuntimeException e) {
            Log.d("yot132","e = " + e.getMessage().substring(20));
            if(e.getMessage().contains("password not matched")) {
                showTextDialog("password not matched");
            }else if(e.getMessage().contains("RESOURCE_EXHAUSTED")) {
                JSONObject object = null;
                try {
                    object = new JSONObject(e.getMessage().substring(20));
                    JSONObject jsonObject = new JSONObject(object.getString("login_infos"));
                    Iterator<String> keys = jsonObject.keys();
                    LogoutItem logoutItem;
                    List<LogoutItem> logoutItems = new ArrayList<>();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        String value = jsonObject.getString(key);
                        JSONObject res = new JSONObject(value);
                        long unixTimestamp = Long.parseLong(res.getString("date"));
                        Date date = new Date(unixTimestamp * 1000L);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        String formattedDate = sdf.format(date);
                        logoutItem = new LogoutItem(key,res.getString("addr"),formattedDate);
                        logoutItems.add(logoutItem);
                    }
                    showLogoutDialog(id,pw,logoutItems);
                } catch (JSONException ex) {
                    ex.printStackTrace();
                    Log.d("yot132","e = " + ex);
                }
            }
            else {
                Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(LoginPasswordActivity.this,LoginEmailActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(0,0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(LoginPasswordActivity.this,LoginEmailActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(0,0);
        return super.onOptionsItemSelected(item);
    }

    public void showLogoutDialog(String id, String pw, List<LogoutItem> loginInfos) {
        custom_dialog = new Dialog(this);
        custom_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        custom_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        custom_dialog.setContentView(R.layout.custom_dialog_forcedlogout);
        TextView tv1 = custom_dialog.findViewById(R.id.tvLogout1);
        TextView tv2 = custom_dialog.findViewById(R.id.tvLogout2);
        TextView tv3 = custom_dialog.findViewById(R.id.tvLogout3);
        TextView tv4 = custom_dialog.findViewById(R.id.tvLogout4);
        TextView tv5 = custom_dialog.findViewById(R.id.tvLogout5);
        tv1.setText(loginInfos.get(0).getAddr() +"\n(" + loginInfos.get(0).getDate() + ")");
        tv2.setText(loginInfos.get(1).getAddr() +"\n(" + loginInfos.get(1).getDate() + ")");
        tv3.setText(loginInfos.get(2).getAddr() +"\n(" + loginInfos.get(2).getDate() + ")");
        tv4.setText(loginInfos.get(3).getAddr() +"\n(" + loginInfos.get(3).getDate() + ")");
        tv5.setText(loginInfos.get(4).getAddr() +"\n(" + loginInfos.get(4).getDate() + ")");
        custom_dialog.findViewById(R.id.btnOk1).setOnClickListener(v -> {
            try {
                LogoutWithAccountRequest logoutRequest = LogoutWithAccountRequest.newBuilder().setLoginKey(loginInfos.get(0).getKey()).setId(id).setPassword(pw).build();
                LogoutWithAccountResponse logoutResponse = customApplication.authStub.logoutWithAccount(logoutRequest);
            }catch (RuntimeException e) {
                Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
            }
            custom_dialog.dismiss();
        });
        custom_dialog.findViewById(R.id.btnOk2).setOnClickListener(v -> {
            try {
                LogoutWithAccountRequest logoutRequest = LogoutWithAccountRequest.newBuilder().setLoginKey(loginInfos.get(1).getKey()).setId(id).setPassword(pw).build();
                LogoutWithAccountResponse logoutResponse = customApplication.authStub.logoutWithAccount(logoutRequest);
            }catch (RuntimeException e) {
                Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
            }
            custom_dialog.dismiss();
        });
        custom_dialog.findViewById(R.id.btnOk3).setOnClickListener(v -> {
            try {
                LogoutWithAccountRequest logoutRequest = LogoutWithAccountRequest.newBuilder().setLoginKey(loginInfos.get(2).getKey()).setId(id).setPassword(pw).build();
                LogoutWithAccountResponse logoutResponse = customApplication.authStub.logoutWithAccount(logoutRequest);
            }catch (RuntimeException e) {
                Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
            }
            custom_dialog.dismiss();
        });
        custom_dialog.findViewById(R.id.btnOk4).setOnClickListener(v -> {
            try {
                LogoutWithAccountRequest logoutRequest = LogoutWithAccountRequest.newBuilder().setLoginKey(loginInfos.get(3).getKey()).setId(id).setPassword(pw).build();
                LogoutWithAccountResponse logoutResponse = customApplication.authStub.logoutWithAccount(logoutRequest);
            }catch (RuntimeException e) {
                Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
            }
            custom_dialog.dismiss();
        });
        custom_dialog.findViewById(R.id.btnOk5).setOnClickListener(v -> {
            try {
                LogoutWithAccountRequest logoutRequest = LogoutWithAccountRequest.newBuilder().setLoginKey(loginInfos.get(4).getKey()).setId(id).setPassword(pw).build();
                LogoutWithAccountResponse logoutResponse = customApplication.authStub.logoutWithAccount(logoutRequest);
            }catch (RuntimeException e) {
                Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
            }
            custom_dialog.dismiss();
        });
        custom_dialog.show();
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