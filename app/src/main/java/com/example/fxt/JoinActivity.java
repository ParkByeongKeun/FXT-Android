package com.example.fxt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.InputFilter;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fxt.login.AuthService;
import com.example.fxt.login.JoinRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.ijoon.auth.JoinResponse;

import org.json.JSONObject;

import io.grpc.StatusRuntimeException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class JoinActivity extends MainAppcompatActivity {

    EditText mEtId;
    EditText mEtPassword;
    EditText mEtNickName;
    EditText mEtRePassword;
    EditText mEtEmail;
    Button mBtnJoin;
    String mStrId;
    String mStrPassword;
    String mStrRePassword;
    String mStrName;
    View view;
    RelativeLayout mRlRoot;
    InputMethodManager imm;
    CustomApplication customApplication;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);
        customApplication = (CustomApplication)getApplication();
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        view = getWindow().getDecorView();
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        ActionBar actionBar = this.getSupportActionBar();
        getWindow().setStatusBarColor(Color.parseColor("#EA8235"));//statusBar
        getWindow().setNavigationBarColor(Color.parseColor("#EA8235"));//bottom
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
            actionBar.hide();
//            actionBar.setTitle(Html.fromHtml("<font color='#ffffff'>" + getResources().getString(R.string.textJoin) + "</font>"));
        }
        mEtId = findViewById(R.id.etId);
        mEtPassword = findViewById(R.id.etPassword);
        mEtNickName = findViewById(R.id.etNick);
        mEtRePassword = findViewById(R.id.etRePassword);
        mEtEmail = findViewById(R.id.etEmail);
        mRlRoot = findViewById(R.id.rlRoot);
        mBtnJoin = findViewById(R.id.btnJoin);
        mEtNickName.setOnKeyListener((v, keyCode, event) -> {
            if(keyCode == event.KEYCODE_ENTER) {
                hideKeyboard();
                return true;
            }
            return false;
        });
        mEtId.setFilters(new InputFilter[] {new InputFilter.LengthFilter(64)});
        mEtEmail.setFilters(new InputFilter[] {new InputFilter.LengthFilter(64)});
        mEtPassword.setFilters(new InputFilter[] {new InputFilter.LengthFilter(15)});
        mEtNickName.setFilters(new InputFilter[] {new InputFilter.LengthFilter(15)});
        mEtRePassword.setFilters(new InputFilter[] {new InputFilter.LengthFilter(15)});

        mRlRoot.setOnClickListener(v -> imm.hideSoftInputFromWindow(mEtId.getWindowToken(), 0));
        mBtnJoin.setOnClickListener(v -> {
            mStrId = mEtId.getText().toString();
            mStrPassword = mEtPassword.getText().toString();
            mStrRePassword = mEtRePassword.getText().toString();
            mStrName = mEtNickName.getText().toString();
//            if(android.util.Patterns.EMAIL_ADDRESS.matcher(mStrId).matches()) {
                if(mStrPassword.equals(mStrRePassword)) {
                    if (mStrPassword.length() >= 6 && mStrPassword.length() <= 15) {
//                        if (true) {
                        if(!mEtNickName.getText().toString().equals("")) {
                            if(mEtNickName.getText().toString().length() > 15) {
                                Toast.makeText(getApplicationContext(), "Nicknames can contain up to 15 characters", Toast.LENGTH_SHORT).show();
                            }else {//성공
                                SharedPreferences preferences = getSharedPreferences("preferences",MODE_PRIVATE);
                                preferences.edit().putString("nickname","");
                                preferences.edit().apply();
                                join(mStrId,mStrPassword, mStrRePassword, mStrName);
                            }
                        }else {
                            Toast.makeText(getApplicationContext(), "Nickname can not be empty", Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Toast.makeText(getApplicationContext(), "Password 6 to 15 characters", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(getApplicationContext(), "Password do not match Please check your password", Toast.LENGTH_SHORT).show();
                }
//            }else {
//                Toast.makeText(getApplicationContext(), "It is not in email format", Toast.LENGTH_SHORT).show();
//            }
        });
    }

    private void hideKeyboard()
    {
        imm.hideSoftInputFromWindow(mEtNickName.getWindowToken(), 0);
    }

    void join(String id, String pw, String rePw, String name) {
        try {
            Thread thread = new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    net.ijoon.auth.JoinRequest joinRequest = net.ijoon.auth.JoinRequest.newBuilder().setEmail(id).setPassword(pw).setName(name).build();
                    JoinResponse joinResponse = customApplication.authStub.join(joinRequest);
//                    Intent intent = new Intent(JoinActivity.this, JoinVerifyCodeActivity.class);
//                    intent.putExtra("verifyEmail",id);
//                    startActivity(intent);
//                    runOnUiThread(() -> {
//                        rlLoading.setVisibility(View.GONE);
//                    });
                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (StatusRuntimeException er) {
                    Log.d("yot132","StatusRuntimeException = " + er);
                }
            });
            thread.start();
        }catch (RuntimeException e) {
//            rlLoading.setVisibility(View.GONE);
            if(e.getMessage().contains("UNAVAILABLE")) {
                Toast.makeText(getApplicationContext(),"Connecting to server.",Toast.LENGTH_SHORT).show();
            }else {
                Log.d("yot132","??? = " + e);
                Toast.makeText(getApplicationContext(),"Check the Information",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(JoinActivity.this,SignInActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(0,0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(JoinActivity.this,SignInActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(0,0);
        return super.onOptionsItemSelected(item);
    }
}