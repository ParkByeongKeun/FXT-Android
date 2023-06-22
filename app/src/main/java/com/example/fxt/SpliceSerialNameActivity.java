package com.example.fxt;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.kongzue.dialogx.dialogs.TipDialog;
import com.kongzue.dialogx.dialogs.WaitDialog;

public class SpliceSerialNameActivity extends MainAppcompatActivity {

    CustomApplication customApplication;
    TextView tvName;
    EditText etNewName;
    TextView tvWeb;
    Button btnUpdateName;
    InputMethodManager imm;
    Dialog custom_dialog;
    ActionBar mTitle;
    Dialog custom_delete_dialog;
    ExtendedFloatingActionButton fab;
    Dialog custom_mmode_dialog;
    Dialog custom_password_dialog;
    Dialog custom_text_dialog;
    EditText etPassword;
    TextView tv;
    EditText etLoss;
    EditText etAngle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serial_name);
        customApplication = (CustomApplication)getApplication();
        initView();
        initDeleteDialog();
        initMModeDialog();
        initMModePasswordDialog();
        initTextDialog();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        findViewById(R.id.rlRoot).setOnClickListener(v -> imm.hideSoftInputFromWindow(etNewName.getWindowToken(), 0));
        String serial = customApplication.arrMapSpliceSerial.get(customApplication.connectBLEAddress);
        if(serial == null) {
            serial = "No Device Name";
        }
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            showMModePasswordDialog();
        });
        tvName.setText(serial);
        tvWeb.setOnClickListener(v -> {
            showDeleteDialog();
        });
        btnUpdateName.setOnClickListener(v -> {
            if(!etNewName.getText().toString().equals("")) {
                tvName.setText(etNewName.getText().toString());
                customApplication.arrMapSpliceSerial.put(customApplication.connectBLEAddress,etNewName.getText().toString());
                customApplication.setMap(this,customApplication.arrMapSpliceSerial);
                Toast.makeText(getApplicationContext(),"Success Edit Serial",Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(getApplicationContext(),"Failed Edit Serial",Toast.LENGTH_SHORT).show();
            }
        });

        mTitle = getSupportActionBar();
        mTitle.setCustomView(null);
        mTitle.setDisplayShowCustomEnabled(true);
        mTitle.setTitle("Edit Device Name");
        mTitle.setBackgroundDrawable(new ColorDrawable(0xffE56731));
    }

    public void initView() {
        tvName = findViewById(R.id.tvTitle);
        etNewName = findViewById(R.id.etRename);
        tvWeb = findViewById(R.id.tvWeb);
        btnUpdateName = findViewById(R.id.btnUpdateUser);
    }


    public void initDialog() {
        custom_dialog = new Dialog(this);
        custom_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        custom_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        custom_dialog.setContentView(R.layout.custom_dialog_base);
    }

    public void showDialog() {
        custom_dialog.show();
        custom_dialog.findViewById(R.id.btnNo).setOnClickListener(v -> {
            custom_dialog.dismiss();
        });
        custom_dialog.findViewById(R.id.btnOk).setOnClickListener(v -> {
            custom_dialog.dismiss();
        });
    }

    public void initDeleteDialog() {
        custom_delete_dialog = new Dialog(this);
        custom_delete_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        custom_delete_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        custom_delete_dialog.setContentView(R.layout.custom_dialog_base);
        ImageView iv = custom_delete_dialog.findViewById(R.id.iv);
        iv.setImageResource(R.drawable.ic_pop_w);
        TextView tv = custom_delete_dialog.findViewById(R.id.tvTitle);
        TextView subTv = custom_delete_dialog.findViewById(R.id.tvSubTitle);
        tv.setText("Logout");
        subTv.setText("Would you like to logout?");
    }

    public void showDeleteDialog() {
        custom_delete_dialog.show();
        custom_delete_dialog.findViewById(R.id.btnNo).setOnClickListener(v -> {
            custom_delete_dialog.dismiss();
        });
        custom_delete_dialog.findViewById(R.id.btnOk).setOnClickListener(v -> {
            customApplication.isLogin = false;
            SharedPreferences sharedPreferences = this.getSharedPreferences("login",MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("login",false);
            editor.apply();
            custom_delete_dialog.dismiss();
            Intent intent = new Intent(SpliceSerialNameActivity.this,OFIFNMSActivity.class);
            startActivity(intent);
            activityFinish();
        });
    }


    public void initMModeDialog() {
        custom_mmode_dialog = new Dialog(this);
        custom_mmode_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        custom_mmode_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        custom_mmode_dialog.setContentView(R.layout.custom_dialog_edit);
        ImageView iv = custom_mmode_dialog.findViewById(R.id.iv);
        iv.setImageResource(R.drawable.ic_save);
        TextView tv = custom_mmode_dialog.findViewById(R.id.tvTitle);
        TextView subTv = custom_mmode_dialog.findViewById(R.id.tvSubTitle);
        etLoss = custom_mmode_dialog.findViewById(R.id.etLoss);
        etAngle = custom_mmode_dialog.findViewById(R.id.etAngle);
        etLoss.setText(customApplication.lossThreshold + "");
        etAngle.setText(customApplication.angleThreshold + "");
        tv.setText("M Mode");
        subTv.setText("write the value to be modified");
    }

    public void showMModeDialog() {
        custom_mmode_dialog.show();
        custom_mmode_dialog.findViewById(R.id.btnNo).setOnClickListener(v -> {
            custom_mmode_dialog.dismiss();
        });
        custom_mmode_dialog.findViewById(R.id.btnOk).setOnClickListener(v -> {
            if (etLoss.getText().toString().equals("") || etAngle.getText().toString().equals("")) {
                showTextDialog("Check your input information");
            }else {
                customApplication.lossThreshold = Float.parseFloat(etLoss.getText().toString());
                customApplication.angleThreshold = Float.parseFloat(etAngle.getText().toString());

                SharedPreferences sharedPreferences = this.getSharedPreferences("loss",MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putFloat("loss",customApplication.lossThreshold);
                editor.apply();

                SharedPreferences sharedPreferences1 = this.getSharedPreferences("angle",MODE_PRIVATE);
                SharedPreferences.Editor editor1 = sharedPreferences1.edit();
                editor1.putFloat("angle",customApplication.angleThreshold);
                editor1.apply();
                Toast.makeText(getApplicationContext(),"Success M Mode",Toast.LENGTH_SHORT).show();
                custom_mmode_dialog.dismiss();
            }
        });
    }

    public void initMModePasswordDialog() {
        custom_password_dialog = new Dialog(this);
        custom_password_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        custom_password_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        custom_password_dialog.setContentView(R.layout.custom_dialog_password);
        ImageView iv = custom_password_dialog.findViewById(R.id.iv);
        iv.setImageResource(R.drawable.ic_pop_w);
        TextView tv = custom_password_dialog.findViewById(R.id.tvTitle);
        TextView subTv = custom_password_dialog.findViewById(R.id.tvSubTitle);
        etPassword = custom_password_dialog.findViewById(R.id.etPassword);
        tv.setText("M Mode");
        subTv.setText("input user password");
    }

    public void showMModePasswordDialog() {
        custom_password_dialog.show();
        custom_password_dialog.findViewById(R.id.btnNo).setOnClickListener(v -> {
            custom_password_dialog.dismiss();
        });
        custom_password_dialog.findViewById(R.id.btnOk).setOnClickListener(v -> {
            if(etPassword.getText().toString().equals("1234")) {
                showMModeDialog();
                etNewName.setText("");
            }else {
                showTextDialog("Check your input information");
            }
            custom_password_dialog.dismiss();
        });
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