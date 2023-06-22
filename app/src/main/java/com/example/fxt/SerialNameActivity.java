package com.example.fxt;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serial_name);
        customApplication = (CustomApplication)getApplication();
        initView();
        initDeleteDialog();
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
            Intent intent = new Intent(SerialNameActivity.this,OFIFNMSActivity.class);
            startActivity(intent);
            activityFinish();
        });
    }
}