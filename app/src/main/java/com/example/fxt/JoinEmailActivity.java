
package com.example.fxt;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.InputFilter;
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
import com.example.fxt.login.ResendRequest;
import com.example.fxt.login.ResendResponse;
import com.example.fxt.login.VerifyJoinRequest;
import com.example.fxt.login.VerifyJoinResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class JoinEmailActivity extends MainAppcompatActivity {

    Button mBtnRetryEmail;
    String mId;
    View view;
    Button mBtnAuthOk;
    EditText mEtAuth;
    InputMethodManager imm;
    RelativeLayout mRlRoot;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_email);
        mBtnRetryEmail = findViewById(R.id.btnRetryEmail);
        mBtnAuthOk = findViewById(R.id.btnAuthOk);
        mEtAuth = findViewById(R.id.etAuth);
        mEtAuth.setFilters(new InputFilter[] {new InputFilter.LengthFilter(15)});
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        mRlRoot = findViewById(R.id.rlRootView);
        mId = getIntent().getStringExtra("id");
        mRlRoot.setOnClickListener(v -> imm.hideSoftInputFromWindow(mEtAuth.getWindowToken(), 0));
        view = getWindow().getDecorView();
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(Color.parseColor("#3286EE"));
        }
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
            actionBar.setTitle(Html.fromHtml("<font color='#ffffff'>" + "Confirm" + "</font>"));
        }
        mBtnAuthOk.setOnClickListener(v -> {
            verifyJoin(mId,mEtAuth.getText().toString());
        });
        mBtnRetryEmail.setOnClickListener(v -> resend(mId));
    }

    void verifyJoin(String id,String verifyJoinNumber) {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(chain -> {
            Request request = chain.request().newBuilder().addHeader("Content-Type", "application/json").build();
            return chain.proceed(request);
        });
        Gson gson = new GsonBuilder().setLenient().create();
        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient.build())
                .baseUrl("http://1.246.219.189:20217")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        AuthService service = retrofit.create(AuthService.class);
        final Call<VerifyJoinResponse> verifyJoinResponseCall = service.verifyJoin(new VerifyJoinRequest(id,verifyJoinNumber));
        verifyJoinResponseCall.enqueue(new Callback<VerifyJoinResponse>() {
            @Override
            public void onResponse(Call<VerifyJoinResponse> call, Response<VerifyJoinResponse> response) {
                VerifyJoinResponse body = response.body();
                if(body == null) {// Todo: 로그인인증서버에서 HTTP Status Code에 따라 body가 null이 되는지 확인
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        switch (jObjError.getString("errno")) {
                            case "0" : {
                                Toast.makeText(getApplicationContext(), "Check Auth Number", Toast.LENGTH_LONG).show();
                                break;
                            }
                            case "1" : {
                                Toast.makeText(getApplicationContext(), "Check Auth Number", Toast.LENGTH_LONG).show();
                                break;
                            }
                            case "2" : {
                                Toast.makeText(getApplicationContext(), "Check Auth Number", Toast.LENGTH_LONG).show();
                                break;
                            }
                            case "3" : {
                                Toast.makeText(getApplicationContext(), "Please login again", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(JoinEmailActivity.this,SignInActivity.class);
                                startActivity(intent);
                                finish();
                                overridePendingTransition(0,0);
                                break;
                            }
                        }
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    return;
                }
                if(body.getSuccess()) {
                    Toast.makeText(getApplicationContext(), "인증되었습니다.\n로그인 후 사용해주세요.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(JoinEmailActivity.this, SignInActivity.class);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(0,0);
                }else {
                    Toast.makeText(getApplicationContext(), "인증번호를 확인해주세요.", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<VerifyJoinResponse> call, Throwable t) {
            }
        });
    }

    void resend(String id) {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(chain -> {
            Request request = chain.request().newBuilder().addHeader("Content-Type", "application/json").build();
            return chain.proceed(request);
        });
        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient.build())
                .baseUrl("http://1.246.219.189:20217")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        AuthService service = retrofit.create(AuthService.class);
        final Call<ResendResponse> resendResponseCall = service.resend(new ResendRequest(id));
        resendResponseCall.enqueue(new Callback<ResendResponse>() {
            @Override
            public void onResponse(Call<ResendResponse> call, Response<ResendResponse> response) {
                ResendResponse body = response.body();
                if(body == null) {
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.textCheckInformation),Toast.LENGTH_SHORT).show();
                    return;
                }
                if(response.body().getSuccess()) {
                    Toast.makeText(getApplicationContext(), "The email has been resent.", Toast.LENGTH_LONG).show();
                }else {
                    Toast.makeText(getApplicationContext(), "Resent failed.", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<ResendResponse> call, Throwable t) {
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(JoinEmailActivity.this,SignInActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(0,0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(JoinEmailActivity.this,SignInActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(0,0);
        return super.onOptionsItemSelected(item);
    }
}