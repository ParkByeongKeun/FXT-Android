package com.example.fxt;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.fxt.login.RetrofitClient;
import com.example.fxt.login.UnRegPushTokenRequest;
import com.example.fxt.login.UnRegPushTokenResponse;
import com.example.fxt.utils.NetworkStatus;
import com.example.fxt.utils.UUIDManager;
import net.ijoon.auth.LogoutRequest;
import net.ijoon.auth.LogoutResponse;
import net.ijoon.auth.UpdateUserRequest;
import net.ijoon.auth.UpdateUserResponse;
import net.ijoon.auth.UserRequest;
import net.ijoon.auth.UserResponse;
import net.ijoon.auth.WithdrawalRequest;
import net.ijoon.auth.WithdrawalResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingActivity extends MainAppcompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    CustomApplication customApplication;
    SwipeRefreshLayout mSwipeRefreshLayout;
    View view;
    RelativeLayout mRlEmail;
    RelativeLayout mRlNickname;
    RelativeLayout mRlPasswordChange;
    RelativeLayout mRlLogout;
    RelativeLayout mRlWithdrawal;
    RelativeLayout mRlKakaoSharing;
    TextView mTvEmail;
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
    String Nickname;
    boolean isCheck = false;
    Dialog custom_text_dialog;
    TextView tv;

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
        mRlEmail = findViewById(R.id.rlEmail);
        mRlNickname = findViewById(R.id.rlNickname);
        mRlPasswordChange = findViewById(R.id.rlPasswordChange);
        mRlLogout = findViewById(R.id.rlLogout);
        mRlWithdrawal = findViewById(R.id.rlWithdrawal);
        mRlKakaoSharing = findViewById(R.id.rlKakaotalk);
        mTvEmail = findViewById(R.id.tvEmail);
        mTvId = findViewById(R.id.tvId);
        mTvNickname = findViewById(R.id.tvNickname);
        mTvNickname.setText(mNickname);
        mTvKaKao = findViewById(R.id.tvKakao);
        initTextDialog();
        mTvId.setText(mId);
        view = getWindow().getDecorView();
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
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
    }

    void delete(String pw) {
        if(pw.equals("")) {
            showAlertDialog("Failed to withdrawal");
            return;
        }

        try {
            WithdrawalRequest request = WithdrawalRequest.newBuilder().setPassword(pw).build();
            WithdrawalResponse response = customApplication.authStub.withdrawal(request);
        }catch (RuntimeException e) {
            Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
            return;
        }
        unRegPushToken(UUIDManager.getDeviceUUID(getApplicationContext()));
        SharedPreferences preferences = getSharedPreferences("preferences",MODE_PRIVATE);
        preferences.edit().clear().apply();
        preferences.edit().apply();
        customApplication.token = "";
        customApplication.loginKey = "";
        customApplication.login_id = "";
        SettingActivity.super.activityFinish();
        Intent intent = new Intent(SettingActivity.this, LoginEmailActivity.class);
        startActivity(intent);
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
            try {
                LogoutRequest request = LogoutRequest.newBuilder().setLoginKey(customApplication.loginKey).build();
                LogoutResponse response = customApplication.authStub.logout(request);
            }catch (RuntimeException e) {
                Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();

            }

            unRegPushToken(UUIDManager.getDeviceUUID(getApplicationContext()));
            SharedPreferences preferences = getSharedPreferences("preferences",MODE_PRIVATE);
            preferences.edit().clear().apply();
            preferences.edit().apply();
            customApplication.token = "";
            customApplication.loginKey = "";
            customApplication.login_id = "";
            SettingActivity.super.activityFinish();
            Intent intent = new Intent(SettingActivity.this, LoginEmailActivity.class);
            startActivity(intent);
            custom_dialog.dismiss();
        });
        custom_dialog.show();
    }

    public void showAlertDialog(String message) {
        Dialog custom_dialog = new Dialog(this);
        custom_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        custom_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        custom_dialog.setContentView(R.layout.custom_dialog_completed);
        TextView tv = custom_dialog.findViewById(R.id.tvTitle);
        custom_dialog.findViewById(R.id.btnOk).setOnClickListener(v -> {
            custom_dialog.dismiss();
        });
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
        });
        custom_dialog.findViewById(R.id.btnNo).setOnClickListener(v -> {
            custom_dialog.dismiss();
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
            custom_change_password_dialog.dismiss();
        });
        custom_change_password_dialog.show();
    }

    public void showNicknameChangeDialog() {
        custom_password_dialog = new Dialog(this);
        custom_password_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        custom_password_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        custom_password_dialog.setContentView(R.layout.custom_dialog_nickname);
        EditText etNickname = custom_password_dialog.findViewById(R.id.etNickname);
        EditText etPassword = custom_password_dialog.findViewById(R.id.etPassword);
        Button btnOk = custom_password_dialog.findViewById(R.id.btnOk);
        Button btnNo = custom_password_dialog.findViewById(R.id.btnNo);
        etNickname.setSingleLine(true);
        etNickname.setLines(1);
        btnOk.setOnClickListener(v -> {
            updateNickName(etNickname.getText().toString(),etPassword.getText().toString());
            custom_password_dialog.dismiss();
        });
        btnNo.setOnClickListener(v -> {
            custom_password_dialog.dismiss();
        });
        custom_password_dialog.show();
    }

    void updateNickName(final String id,final String pw) {
        int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
        if(status == NetworkStatus.TYPE_NOT_CONNECTED){
            Toast.makeText(getApplicationContext(),"Connecting to server",Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            UpdateUserRequest req = UpdateUserRequest.newBuilder()
                    .setName(id)
                    .setPassword(pw)
                    .setOldPassword(pw)
                    .build();
            UpdateUserResponse res = customApplication.authStub.updateUser(req);
            mTvNickname.setText(id);
            Nickname = id;
            showTextDialog("Success");
        } catch (RuntimeException e) {
            Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
        }
    }

    void updatePassword(String pw, String pwNew, String pwConfirmNew) {
        int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
        if(status == NetworkStatus.TYPE_NOT_CONNECTED){
            Toast.makeText(getApplicationContext(),"Connecting to server",Toast.LENGTH_SHORT).show();
            return;
        }
        if(!pwNew.equals(pwConfirmNew)) {
            Toast.makeText(getApplicationContext(),"check information",Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            UpdateUserRequest req = UpdateUserRequest.newBuilder()
                    .setName(Nickname)
                    .setPassword(pwNew)
                    .setOldPassword(pw)
                    .build();
            UpdateUserResponse loginResponse = customApplication.authStub.updateUser(req);
            custom_change_password_dialog.dismiss();
            showTextDialog("Success");
        } catch (RuntimeException e) {
            Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onResume() {
        super.onResume();
        try {
            UserRequest req = UserRequest.newBuilder().build();
            UserResponse res = customApplication.authStub.getUser(req);
            mTvId.setText(res.getUsers().getId());
            mTvEmail.setText(res.getUsers().getEmail());
            mTvNickname.setText(res.getUsers().getName());
            Nickname = res.getUsers().getName();
//            email = res.getUser().getEmail();
//            name = res.getUser().getName();
//            phoneNumber = res.getUser().getPhoneNumber();
//            updateList();
        }catch (RuntimeException e) {
            Log.d("yot132","e = " + e);
            Toast.makeText(getApplicationContext(),"Connecting to server",Toast.LENGTH_SHORT).show();
        }
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