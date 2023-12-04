package com.fiberfox.fxt;

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
import android.widget.Toast;

import com.fiberfox.fxt.R;
import com.fiberfox.fxt.utils.NetworkStatus;
import net.ijoon.auth.SendCodeRequest;
import net.ijoon.auth.SendCodeResponse;
import net.ijoon.auth.VerifyResetCodeRequest;
import net.ijoon.auth.VerifyResetCodeResponse;

public class ForgotPasswordActivity extends MainAppcompatActivity {

    Button mBtnForgotPw;
    EditText mEtVerifyCode;
    RelativeLayout mRlRoot;
    TextView mTvTitle;
    TextView mTvWeb;
    View view;
    CustomApplication customApplication;
    InputMethodManager imm;
    String mStrId;
    Dialog custom_text_dialog;
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        view = getWindow().getDecorView();
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        customApplication = (CustomApplication)getApplication();
//        ActionBar actionBar = this.getSupportActionBar();
//        actionBar.setBackgroundDrawable(new ColorDrawable(0xffE56731));

        mStrId = getIntent().getStringExtra("email");
        mBtnForgotPw = findViewById(R.id.btnForgotPw);
        mEtVerifyCode = findViewById(R.id.etVerifyCode);
        mRlRoot = findViewById(R.id.rlRoot);
        mTvWeb = findViewById(R.id.tvWeb);
        mTvTitle = findViewById(R.id.tvTitle);
        mTvTitle.setText(mStrId);

        String body = "        <p style=\"font-size: 15px; margin-bottom: 8px\">" +
                "            Thank you for joining FXT. <br />" +
                "            Please verify your email address. <br />" +
                "            Certification Number <br />" +
                "            <br />" +
                "            $code <br />" +
                "            <br />" +
                "            Please return to the previous screen to enter the authentication number. <br />" +
                "            thank you<br />" +
                "            Dear FXT team." +
                "        </p>";
        try {
            SendCodeRequest sendCodeRequest = SendCodeRequest.newBuilder()
                    .setEmail(mStrId)
                    .setFrom("fiberfox.noreply")
                    .setSubject("Send FXT authentication number")
                    .setContentType("text/html")
                    .setCharset("UTF-8")
                    .setBody(body)
                    .build();
            SendCodeResponse sendCodeResponse = customApplication.authStub.sendResetCode(sendCodeRequest);

        }catch (RuntimeException e) {
            Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
            return;
        }



        mBtnForgotPw.setOnClickListener(v -> {
            forgotPw(mStrId, mEtVerifyCode.getText().toString());
        });

        mTvWeb.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.fiberfox.co.kr/"));
            startActivity(intent);
        });

        mRlRoot.setOnClickListener(v -> imm.hideSoftInputFromWindow(mEtVerifyCode.getWindowToken(), 0));
    }

    void forgotPw(String id, String verifyCode) {
        int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
        if(status == NetworkStatus.TYPE_NOT_CONNECTED){
            showTextDialog("Connecting to server");
            return;
        }
        try {
            VerifyResetCodeRequest req = VerifyResetCodeRequest.newBuilder().setCode(verifyCode).setEmail(id).build();
            VerifyResetCodeResponse res = customApplication.authStub.verifyResetCode(req);
            Intent intent = new Intent(ForgotPasswordActivity.this, ForgotPasswordResetActivity.class);
            intent.putExtra("email",mStrId);
            intent.putExtra("VerifycationCode", mEtVerifyCode.getText().toString());
            startActivity(intent);
            finish();
        }catch (RuntimeException e) {
            if(e.getMessage().contains("UNAVAILABLE")) {
                showTextDialog("Server Error");
            }else {
                showTextDialog("Check information");
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ForgotPasswordActivity.this,LoginEmailActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(0,0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(ForgotPasswordActivity.this,LoginEmailActivity.class);
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