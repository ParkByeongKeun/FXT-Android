package com.fiberfox.fxt;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

import com.fiberfox.fxt.utils.C_Permission;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

public class SerialNameActivity extends MainAppcompatActivity {

    CustomApplication customApplication;
    TextView tvName;
    EditText etNewName;
    TextView tvWeb;
    Button btnUpdateName;
    InputMethodManager imm;
    Dialog custom_dialog;
    ActionBar mTitle;
    Dialog custom_delete_dialog;
    Dialog custom_mmode_dialog;
    Dialog custom_password_dialog;
    ExtendedFloatingActionButton fab;
    Dialog custom_text_dialog;
    EditText etPassword;
    TextView tv;

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
        fab = findViewById(R.id.fab);
        C_Permission.checkPermission(this);
        initView();
        fab.setVisibility(View.GONE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        findViewById(R.id.rlRoot).setOnClickListener(v -> imm.hideSoftInputFromWindow(etNewName.getWindowToken(), 0));
        String serial = customApplication.arrMapSerial.get(customApplication.connectBLEAddress);
        if(serial == null) {
            serial = "No Device Name";
        }
        tvName.setText(serial);
        tvWeb.setOnClickListener(v -> {
            showDeleteDialog();
        });
        btnUpdateName.setOnClickListener(v -> {
            if(!etNewName.getText().toString().equals("")) {
                tvName.setText(etNewName.getText().toString());
                customApplication.arrMapSerial.put(customApplication.connectBLEAddress,etNewName.getText().toString());
                customApplication.setMap(this,customApplication.arrMapSerial);
                showTextDialog("Success Edit Serial");
            }else {
                showTextDialog("Failed Edit Serial");
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
            SharedPreferences sharedPreferences = this.getSharedPreferences("login",MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("login",false);
            editor.apply();
            custom_delete_dialog.dismiss();
            Intent intent = new Intent(SerialNameActivity.this,OFIFNMSActivity.class);
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
        iv.setImageResource(R.drawable.ic_pop_w);
        TextView tv = custom_mmode_dialog.findViewById(R.id.tvTitle);
        TextView subTv = custom_mmode_dialog.findViewById(R.id.tvSubTitle);
        tv.setText("M Mode");
        subTv.setText("write the value to be modified");
    }

    public void showMModeDialog() {
        custom_mmode_dialog.show();
        custom_mmode_dialog.findViewById(R.id.btnNo).setOnClickListener(v -> {
            custom_mmode_dialog.dismiss();
        });
        custom_mmode_dialog.findViewById(R.id.btnOk).setOnClickListener(v -> {

        });
    }

    public void initMModePasswordDialog() {
        custom_password_dialog = new Dialog(this);
        custom_password_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        custom_password_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        custom_password_dialog.setContentView(R.layout.custom_dialog_edit);
        ImageView iv = custom_password_dialog.findViewById(R.id.iv);
        iv.setImageResource(R.drawable.ic_pop_w);
        TextView tv = custom_password_dialog.findViewById(R.id.tvTitle);
        TextView subTv = custom_password_dialog.findViewById(R.id.tvSubTitle);
        etPassword = custom_password_dialog.findViewById(R.id.etPassword);
        tv.setText("M Mode");
        subTv.setText("input user password");
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