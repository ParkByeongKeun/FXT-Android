package com.example.fxt;

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
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import net.ijoon.auth.JoinRequest;
import net.ijoon.auth.JoinResponse;
import io.grpc.StatusRuntimeException;

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
    String mStrEmail;
    View view;
    RelativeLayout mRlRoot;
    InputMethodManager imm;
    CustomApplication customApplication;
    CheckBox cbPw;
    RelativeLayout rlLoading;
    Dialog custom_text_dialog;
    TextView tv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);
        customApplication = (CustomApplication)getApplication();
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        view = getWindow().getDecorView();
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        ActionBar actionBar = this.getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(0xffE56731));
//        if (actionBar != null) {
//            actionBar.setDisplayHomeAsUpEnabled(true);
//            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
//            actionBar.setTitle(Html.fromHtml("<font color='#ffffff'>" + "회원가입" + "</font>"));
//        }
        rlLoading = findViewById(R.id.rlLoading);
        mEtId = findViewById(R.id.etId);
        mEtPassword = findViewById(R.id.etPassword);
        mEtNickName = findViewById(R.id.etNick);
        mEtRePassword = findViewById(R.id.etRePassword);
        mEtEmail = findViewById(R.id.etEmail);
        mRlRoot = findViewById(R.id.rlRoot);
        mBtnJoin = findViewById(R.id.btnJoin);
        cbPw = findViewById(R.id.cbPw);
        initTextDialog();

        cbPw.setOnClickListener(v -> {
            if(cbPw.isChecked()) {
                mEtPassword.setInputType(0);
                mEtRePassword.setInputType(0);
            }else {
                mEtPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
                mEtRePassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
            }
        });
        mEtNickName.setOnKeyListener((v, keyCode, event) -> {
            if(keyCode == event.KEYCODE_ENTER) {
                hideKeyboard();
                return true;
            }
            return false;
        });
        mRlRoot.setOnClickListener(v -> imm.hideSoftInputFromWindow(mEtId.getWindowToken(), 0));
        rlLoading.setOnTouchListener((v, event) -> true);
        mBtnJoin.setOnClickListener(v -> {
            mStrId = mEtId.getText().toString();
            mStrPassword = mEtPassword.getText().toString();
            mStrRePassword = mEtRePassword.getText().toString();
            mStrName = mEtNickName.getText().toString();
            mStrEmail = mEtEmail.getText().toString();
            if(android.util.Patterns.EMAIL_ADDRESS.matcher(mStrEmail).matches()) {
                if(mStrPassword.equals(mStrRePassword)) {
                    if (mStrPassword.length() >= 6 && mStrPassword.length() <= 15) {
                        if(!mEtNickName.equals("")) {
                            if (mStrId.length() >= 1) {
                                SharedPreferences preferences = getSharedPreferences("preferences",MODE_PRIVATE);
                                preferences.edit().putString("nickname","");
                                preferences.edit().apply();
                                join(mStrId,mStrPassword, mStrName, mStrEmail);
                            }else {
                                showTextDialog("check information");
                            }
                        }else {
                            showTextDialog("check information");
                        }
                    }else {
                        showTextDialog("check information");
                    }
                }else {
                    showTextDialog("check information");
                }
            }else {
                showTextDialog("check information");
            }
        });
    }

    private void hideKeyboard()
    {
        imm.hideSoftInputFromWindow(mEtNickName.getWindowToken(), 0);
    }

    void join(String id, String pw, String name, String email) {
        rlLoading.setVisibility(View.VISIBLE);
        try {
            Thread thread = new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    JoinRequest joinRequest = JoinRequest.newBuilder().setEmail(email).setId(id).setPassword(pw).setName(name).build();
                    JoinResponse joinResponse = customApplication.authStub.join(joinRequest);
                    Intent intent = new Intent(JoinActivity.this, LoginEmailActivity.class);
                    intent.putExtra("verifyEmail",id);

                    runOnUiThread(() -> {
                        showTextDialog("Succcess");
                        rlLoading.setVisibility(View.GONE);
                    });

                    startActivity(intent);
                    finish();
                }  catch (StatusRuntimeException ee) {
                    if(ee.getStatus().toString().contains("parameter error")) {
                        runOnUiThread(() -> {
                            showTextDialog("ID or email is already being used by another account");
                            rlLoading.setVisibility(View.GONE);
                        });
                    }else {
                        runOnUiThread(() -> {
                            showTextDialog("Server Error");
                            rlLoading.setVisibility(View.GONE);
                        });
                    }
                    Log.d("yot132","ee = " + ee.getStatus().toString());
                    ee.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            thread.start();
        }catch (RuntimeException e) {
            rlLoading.setVisibility(View.GONE);
            if(e.getMessage().contains("UNAVAILABLE")) {
                showTextDialog("Connecting to server");
            }else {
                Log.d("yot132","??? = " + e);
                showTextDialog("check information");
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(JoinActivity.this,LoginEmailActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(0,0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(JoinActivity.this,LoginEmailActivity.class);
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