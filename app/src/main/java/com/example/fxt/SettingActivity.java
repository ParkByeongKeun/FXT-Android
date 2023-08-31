package com.example.fxt;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.fxt.login.AuthService;
import com.example.fxt.login.DeleteRequest;
import com.example.fxt.login.DeleteResponse;
import com.example.fxt.login.RetrofitClient;
import com.example.fxt.login.UnRegPushTokenRequest;
import com.example.fxt.login.UnRegPushTokenResponse;
import com.example.fxt.login.UpdateDispNameRequest;
import com.example.fxt.login.UpdateDispNameResponse;
import com.example.fxt.login.UpdatePasswordRequest;
import com.example.fxt.login.UpdatePasswordResponse;
import com.example.fxt.utils.UUIDManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SettingActivity extends MainAppcompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    CustomApplication customApplication;
    SwipeRefreshLayout mSwipeRefreshLayout;
    View view;
    RelativeLayout mRlNickname;
    RelativeLayout mRlPasswordChange;
    RelativeLayout mRlLogout;
    RelativeLayout mRlWithdrawal;
    RelativeLayout mRlKakaoSharing;
    TextView mTvId;
    TextView mTvNickname;
    TextView mTvKaKao;
    String mToken;
    String mNickname;
    String mId;
    String mKakao;
    private long mLastClickTime = 0;
    Dialog custom_change_password_dialog;
    Dialog custom_password_dialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        customApplication = (CustomApplication) getApplicationContext();
        brManager();
        Intent intent = getIntent();
        mNickname = intent.getStringExtra("nickname");
        mSwipeRefreshLayout = findViewById(R.id.swipe_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        SharedPreferences preferences = getSharedPreferences("preferences",MODE_PRIVATE);
        mToken = preferences.getString("token","");
        mId = preferences.getString("id","");
        mKakao = preferences.getString("kakao","");
        mRlNickname = findViewById(R.id.rlNickname);
        mRlPasswordChange = findViewById(R.id.rlPasswordChange);
        mRlLogout = findViewById(R.id.rlLogout);
        mRlWithdrawal = findViewById(R.id.rlWithdrawal);
        mRlKakaoSharing = findViewById(R.id.rlKakaotalk);
        mTvId = findViewById(R.id.tvId);
        mTvNickname = findViewById(R.id.tvNickname);
        mTvNickname.setText(mNickname);
        mTvKaKao = findViewById(R.id.tvKakao);
//        if(mKakao.equals("")) {
//            mTvKaKao.setText(getResources().getString(R.string.no_pairing));
//        }else {
//            mTvKaKao.setText(getResources().getString(R.string.pairing));
//        }
        mTvId.setText(mId);
        view = getWindow().getDecorView();
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
//        mTitle.setBackgroundDrawable(new ColorDrawable(0xffE56731));
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            getWindow().setNavigationBarColor(Color.parseColor("#3286EE"));//bottom
//            getWindow().setStatusBarColor(Color.parseColor("#56CBF2"));//statusBar
//        }
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xffE56731));//title
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
            actionBar.setTitle(Html.fromHtml("<font color='#ffffff'>" + "Information" + "</font>"));
        }
        mRlLogout.setOnClickListener(v -> {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                return;
            }
            customApplication.isLogin = false;
            SharedPreferences sharedPreferences = this.getSharedPreferences("login",MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("login",false);
            editor.apply();
            mLastClickTime = SystemClock.elapsedRealtime();
            showLogoutAlertDialog("Are you sure you want to logout?");
        });
        mRlNickname.setOnClickListener(v -> {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();
            showNicknameChangeDialog();
        });
        mRlPasswordChange.setOnClickListener(v -> {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();
            initChangePasswordDialog();
        });
        mRlWithdrawal.setOnClickListener(v -> {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                return;
            }
            SharedPreferences sharedPreferences = this.getSharedPreferences("login",MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("login",false);
            editor.apply();
            mLastClickTime = SystemClock.elapsedRealtime();
            showWidthdrawalEditTextAlertDialog();
        });
//        mRlKakaoSharing.setOnClickListener(v -> {
//            SharedPreferences preferences1 = getSharedPreferences("preferences",MODE_PRIVATE);
//            if(preferences1.getString("kakao","").equals("")) {
//                if(UserApiClient.getInstance().isKakaoTalkLoginAvailable(getApplicationContext())) {
//                    UserApiClient.getInstance().loginWithKakaoTalk(getApplicationContext(), (oAuthToken, throwable) -> {
//                        if(throwable != null) {
//                            UserApiClient.getInstance().loginWithKakaoAccount(getApplicationContext(), (oAuthToken1, throwable1) -> {
//                                if(throwable1 != null) {
//                                } else if(oAuthToken1 != null) {
//                                    preferences1.edit().putString("kakao",String.valueOf(oAuthToken1));
//                                    preferences1.edit().apply();
//                                }
//                                return null;
//                            });
//                        } else if(oAuthToken != null) {
//                            preferences1.edit().putString("kakao",String.valueOf(oAuthToken));
//                            preferences1.edit().apply();
//                        }
//                        return null;
//                    });
//                } else {
//                    UserApiClient.getInstance().loginWithKakaoAccount(getApplicationContext(), (oAuthToken, throwable) -> {
//                        if(throwable != null) {
//                        } else if(oAuthToken != null) {
//                            preferences1.edit().putString("kakao",String.valueOf(oAuthToken));
//                            preferences1.edit().apply();
//                        }
//                        return null;
//                    });
//                }
//            }
//        });
    }

    void delete(String pw) {
        if(pw.equals("")) {
            showAlertDialog("Failed to withdrawal");
            return;
        }
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(chain -> {
            Request request = chain.request().newBuilder().addHeader("x-access-token", mToken).build();
            return chain.proceed(request);
        });
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://1.246.219.189:20217")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();
        AuthService service = retrofit.create(AuthService.class);
        final Call<DeleteResponse> deleteResponseCall = service.delete(new DeleteRequest(pw));
        deleteResponseCall.enqueue(new Callback<DeleteResponse>() {
            @Override
            public void onResponse(Call<DeleteResponse> call, Response<DeleteResponse> response) {
                if(response.body() == null) {
                    showAlertDialog("Failed to withdrawal");
                    return;
                }
                if(response.body().getSuccess()) {
                    SharedPreferences preferences = getSharedPreferences("preferences",MODE_PRIVATE);
                    preferences.edit().clear().apply();
                    preferences.edit().putString("id","");
                    preferences.edit().putString("token","");
                    preferences.edit().apply();
                    SettingActivity.super.activityFinish();
                    Intent intent = new Intent(SettingActivity.this, SignInActivity.class);
                    startActivity(intent);
                }else {
                    showAlertDialog("Failed to withdrawal");
                }
            }
            @Override
            public void onFailure(Call<DeleteResponse> call, Throwable t) {
                Log.d("yot132", "onFailure");
            }
        });
    }

    public void showLogoutAlertDialog(String message) {
        Dialog custom_dialog = new Dialog(this);
        custom_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        custom_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        custom_dialog.setContentView(R.layout.custom_dialog_completed);
        TextView tv = custom_dialog.findViewById(R.id.tvTitle);
        tv.setText(message);
        Button btnOk = custom_dialog.findViewById(R.id.btnOk);
        btnOk.setOnClickListener(v -> {
            unRegPushToken(UUIDManager.getDeviceUUID(getApplicationContext()));
            SharedPreferences preferences = getSharedPreferences("preferences",MODE_PRIVATE);
            preferences.edit().clear().apply();
            preferences.edit().putString("id","");
            preferences.edit().putString("token","");
            preferences.edit().apply();
            SettingActivity.super.activityFinish();
            Intent intent = new Intent(SettingActivity.this, DefaultActivity.class);
            startActivity(intent);
        });
        custom_dialog.show();
    }

    public void showAlertDialog(String message) {
        Dialog custom_dialog = new Dialog(this);
        custom_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        custom_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        custom_dialog.setContentView(R.layout.custom_dialog_completed);
        TextView tv = custom_dialog.findViewById(R.id.tvTitle);
        tv.setText(message);
        custom_dialog.show();
    }

    public void showWidthdrawalEditTextAlertDialog() {
        Dialog custom_dialog = new Dialog(this);
        custom_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        custom_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        custom_dialog.setContentView(R.layout.custom_dialog_withdrawal);
        EditText etPassword = custom_dialog.findViewById(R.id.etPassword);
        Button btnOk = custom_dialog.findViewById(R.id.btnOk);
        btnOk.setOnClickListener(v -> {
            etPassword.setFilters(new InputFilter[] {new InputFilter.LengthFilter(15)});
            etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
            etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            delete(etPassword.getText().toString());

            unRegPushToken(UUIDManager.getDeviceUUID(getApplicationContext()));
            SharedPreferences preferences = getSharedPreferences("preferences",MODE_PRIVATE);
            preferences.edit().clear().apply();
            preferences.edit().putString("id","");
            preferences.edit().putString("token","");
            preferences.edit().apply();
            SettingActivity.super.activityFinish();
            Intent intent = new Intent(SettingActivity.this, DefaultActivity.class);
            startActivity(intent);
        });
        custom_dialog.show();
    }

    public void initChangePasswordDialog() {
        custom_change_password_dialog = new Dialog(this);
        custom_change_password_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        custom_change_password_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        custom_change_password_dialog.setContentView(R.layout.custom_dialog_change_password);
        ImageView iv = custom_change_password_dialog.findViewById(R.id.iv);
        iv.setImageResource(R.drawable.ic_save);
        Button btnOk = custom_change_password_dialog.findViewById(R.id.btnOk);
        Button btnNo = custom_change_password_dialog.findViewById(R.id.btnNo);
        EditText existingEditText = custom_change_password_dialog.findViewById(R.id.etPassword);
        EditText changeEditText = custom_change_password_dialog.findViewById(R.id.etChangePassword);
        EditText confirmChangeEditText = custom_change_password_dialog.findViewById(R.id.etChangePasswordConfirm);


        existingEditText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(15)});
        changeEditText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(15)});
        confirmChangeEditText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(15)});
        btnOk.setOnClickListener(v -> {
            updatePassword(existingEditText.getText().toString(),changeEditText.getText().toString(),confirmChangeEditText.getText().toString());
        });
        btnNo.setOnClickListener(v -> {

        });
        custom_change_password_dialog.show();
    }

    public void showNicknameChangeDialog() {
        custom_password_dialog = new Dialog(this);
        custom_password_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        custom_password_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        custom_password_dialog.setContentView(R.layout.custom_dialog_nickname);
//        ImageView iv = custom_password_dialog.findViewById(R.id.iv);
//        iv.setImageResource(R.drawable.ic_pop_w);
//        TextView tv = custom_password_dialog.findViewById(R.id.tvTitle);
//        TextView subTv = custom_password_dialog.findViewById(R.id.tvSubTitle);
        EditText etNickname = custom_password_dialog.findViewById(R.id.etNickname);
        Button btnOk = custom_password_dialog.findViewById(R.id.btnOk);
        Button btnNo = custom_password_dialog.findViewById(R.id.btnNo);
        etNickname.setSingleLine(true);
        etNickname.setLines(1);
        btnOk.setOnClickListener(v -> {
            updateNickName(etNickname.getText().toString());
        });
        btnNo.setOnClickListener(v -> {

        });
        custom_password_dialog.show();
    }

    void updateNickName(final String id) {
        if(id.length() > 15) {
            showAlertDialog("Nicknames can contain up to 15 characters.");
            return;
        }
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(chain -> {
            Request request = chain.request().newBuilder().addHeader("x-access-token", mToken).build();
            return chain.proceed(request);
        });
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://1.246.219.189:20217")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();
        AuthService service = retrofit.create(AuthService.class);
        final Call<UpdateDispNameResponse> loginResponseCall = service.updateDispName(new UpdateDispNameRequest(id));
        loginResponseCall.request().header(mToken);
        loginResponseCall.enqueue(new Callback<UpdateDispNameResponse>() {
            @Override
            public void onResponse(Call<UpdateDispNameResponse> call, Response<UpdateDispNameResponse> response) {
                UpdateDispNameResponse body = response.body();
                if(body == null) {
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.textCheckInformation),Toast.LENGTH_SHORT).show();
                    return;
                }
                if(response.body().getSuccess()) {
                    String accessToken = response.body().getAccessToken();
                    showAlertDialog("Nickname changed successfully.\\nChanged nickname"+ "\" " + id + "\"");
                    mTvNickname.setText(id);
                    SharedPreferences preferences = getSharedPreferences("preferences",MODE_PRIVATE);
                    SharedPreferences.Editor editor =  preferences.edit();
                    editor.putString("token", accessToken);
                    editor.putString("nickname", id);
                    editor.apply();
                }else {
                    showAlertDialog("Nickname change failed.");
                }
            }

            @Override
            public void onFailure(Call<UpdateDispNameResponse> call, Throwable t) {
                Log.d("logdinobei", "onFailure" + t.getMessage());
            }
        });
    }

    void updatePassword(String pw, String pwNew, String pwConfirmNew) {
        if(!pwNew.equals("") && pwNew.equals(pwConfirmNew)) {
            if(pwNew.length() >= 6 && pwNew.length() <= 15) {
                OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
                httpClient.addInterceptor(chain -> {
                    Request request = chain.request().newBuilder().addHeader("x-access-token", mToken).build();
                    return chain.proceed(request);
                });
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("http://1.246.219.189:20217")
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(httpClient.build())
                        .build();
                AuthService service = retrofit.create(AuthService.class);
                final Call<UpdatePasswordResponse> updatePasswordResponseCall = service.updatePassword(new UpdatePasswordRequest(pw, pwNew));
                updatePasswordResponseCall.enqueue(new Callback<UpdatePasswordResponse>() {
                    @Override
                    public void onResponse(Call<UpdatePasswordResponse> call, Response<UpdatePasswordResponse> response) {
                        if(response.body() != null) {
                            if(response.body().getSuccess()) {
                                showAlertDialog("Nickname change success");
                            }else {
                                showAlertDialog("Password change failed");
                            }
                        }else {
                            showAlertDialog("Please check your existing password");
                        }
                    }
                    @Override
                    public void onFailure(Call<UpdatePasswordResponse> call, Throwable t) {
                        Log.d("logdinobei", "onFailure");
                    }
                });
            }else {
                //6~ 15자로 입력해주세요
                showAlertDialog("Password 6 to 15 characters");
            }
        }else {
            //정보를 확인하세요
            showAlertDialog(getResources().getString(R.string.textCheckInformation));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(1400);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            runOnUiThread(() -> mSwipeRefreshLayout.setRefreshing(false));
        });
        thread.start();
    }

    void unRegPushToken(final String UUID) {
        new Thread(() -> {
            Call<UnRegPushTokenResponse> call = RetrofitClient
                    .getApiService(mToken)
                    .unRegPushToken(new UnRegPushTokenRequest(UUID));
            call.enqueue(new Callback<UnRegPushTokenResponse>() {
                @Override
                public void onResponse(Call<UnRegPushTokenResponse> call, Response<UnRegPushTokenResponse> response) {
                    Log.d("yot132","pushtoken reg response = " + response.message());
                }

                @Override
                public void onFailure(Call<UnRegPushTokenResponse> call, Throwable t) {
                    Log.d("yot132", "onFailure...");
                }
            });
        }).start();
    }

    public void brManager() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("delete");
        registerReceiver(mBroadcastReceiver, filter);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            SharedPreferences preferences = getSharedPreferences("preferences",MODE_PRIVATE);
            preferences.edit().clear().apply();
            preferences.edit().putString("id","");
            preferences.edit().putString("token","");
            preferences.edit().apply();
            SettingActivity.super.activityFinish();
            Intent intent1 = new Intent(SettingActivity.this, MainActivity.class);
            startActivity(intent1);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }
}