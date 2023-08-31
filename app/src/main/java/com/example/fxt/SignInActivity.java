package com.example.fxt;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.dd.processbutton.iml.ActionProcessButton;
import com.example.fxt.login.AuthService;
import com.example.fxt.login.RegPushTokenRequest;
import com.example.fxt.login.RegPushTokenResponse;
import com.example.fxt.login.RetrofitClient;
import com.example.fxt.utils.ProgressGenerator;
import com.example.fxt.utils.UUIDManager;
import com.google.android.gms.common.SignInButton;
import com.google.android.material.textfield.TextInputEditText;
import com.kongzue.dialogx.dialogs.MessageDialog;

import net.ijoon.auth.LoginRequest;
import net.ijoon.auth.LoginResponse;

import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

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
    String mToken;
    String mStrId;

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
            login(editEmail.getText().toString(),editPassword.getText().toString());
//            if(authId.equals(editEmail.getText().toString()) &&
//            authPw.equals(editPassword.getText().toString())) {
//                customApplication.isLogin = true;
//                SharedPreferences sharedPreferences = this.getSharedPreferences("login",MODE_PRIVATE);
//                SharedPreferences.Editor editor = sharedPreferences.edit();
//                editor.putBoolean("login",true);
//                editor.apply();
//                progressGenerator.start(btnSignIn);
//                btnSignIn.setEnabled(false);
//                editEmail.setEnabled(false);
//                editPassword.setEnabled(false);
//            }else {
//                showDialog();
//            }
        });
        findViewById(R.id.btnCreateAccount).setOnClickListener(v -> {
            Intent intent = new Intent(SignInActivity.this, JoinActivity.class);
            startActivity(intent);
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

    void login(String id, String pw) {
//        int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
//        if(status == NetworkStatus.TYPE_NOT_CONNECTED){
//            Toast.makeText(getApplicationContext(),"네트워크 연결을 확인해주세요.",Toast.LENGTH_SHORT).show();
//            return;
//        }
        try {
            LoginRequest loginRequest = LoginRequest.newBuilder().setEmail(id).setPassword(pw).build();
            LoginResponse loginResponse = customApplication.authStub.login(loginRequest);
//            customApplication.token = loginResponse.getAccessToken();
//            customApplication.email = id;
            SharedPreferences preferences = getSharedPreferences("preferences",MODE_PRIVATE);
            SharedPreferences.Editor editor =  preferences.edit();
            editor.putString("token", loginResponse.getAccessToken());
            editor.putString("email", id);
            editor.apply();
//            customApplication.setMetaData();
            Intent intent = new Intent(SignInActivity.this,MainActivity.class);
            startActivity(intent);
            finish();

        }catch (RuntimeException e) {
            if(e.getMessage().contains("password not matched")) {
                Toast.makeText(getApplicationContext(),"password not matched",Toast.LENGTH_SHORT).show();
            }else {
                Log.d("yot132","Connecting to Server " + e);
                Toast.makeText(getApplicationContext(),"Connecting to Server " + e,Toast.LENGTH_SHORT).show();
            }
        }
    }

    void regPushToken(final String UUID, final String pushToken) {
        new Thread(() -> {
            Call<RegPushTokenResponse> call = RetrofitClient
                    .getApiService(mToken)
                    .regPushToken(new RegPushTokenRequest(UUID, pushToken));
            call.enqueue(new Callback<RegPushTokenResponse>() {
                @Override
                public void onResponse(Call<RegPushTokenResponse> call, Response<RegPushTokenResponse> response) {
                }
                @Override
                public void onFailure(Call<RegPushTokenResponse> call, Throwable t) {
                }
            });
        }).start();
    }
}