package com.example.fxt;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.kongzue.dialogx.dialogs.TipDialog;
import com.kongzue.dialogx.dialogs.WaitDialog;

public class SpliceSerialNameActivity extends Activity {

    CustomApplication customApplication;
    TextView tvName;
    EditText etNewName;
    TextView tvWeb;
    Button btnUpdateName;
    InputMethodManager imm;
    Dialog custom_dialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serial_name);
        customApplication = (CustomApplication)getApplication();
        initView();
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        findViewById(R.id.rlRoot).setOnClickListener(v -> imm.hideSoftInputFromWindow(etNewName.getWindowToken(), 0));
        String serial = customApplication.arrMapSpliceSerial.get(customApplication.connectBLEAddress);
        if(serial == null) {
            serial = "No Device Name";
        }

        tvName.setText(serial);
        tvWeb.setOnClickListener(v -> {
            customApplication.isLogin = false;
            SharedPreferences sharedPreferences = this.getSharedPreferences("login",MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("login",false);
            editor.apply();
            TipDialog.show("FNMS Logout Success!", WaitDialog.TYPE.SUCCESS,2000);
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
}