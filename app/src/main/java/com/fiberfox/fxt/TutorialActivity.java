package com.fiberfox.fxt;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;

import com.fiberfox.fxt.R;
import com.fiberfox.fxt.RestApi.ResponseGetVideoList;
import com.fiberfox.fxt.RestApi.RetrofitClient;
import com.fiberfox.fxt.fragment.Fragment0;
import com.fiberfox.fxt.fragment.Fragment1;
import com.fiberfox.fxt.fragment.Fragment2;
import com.fiberfox.fxt.fragment.Fragment3;
import com.google.android.material.tabs.TabLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TutorialActivity extends MainAppcompatActivity {

    Fragment fragment0, fragment1, fragment2, fragment3;
    CustomApplication customApplication;
    ActionBar mTitle;
    final String TYPE_ALL = "0";
    InputMethodManager imm;
    EditText etSerch;
    Button btnSearch;
    Fragment selected = null;
    public String strSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorials);
        customApplication = (CustomApplication) getApplication();
        mTitle = getSupportActionBar();
        mTitle.setCustomView(null);
        mTitle.setDisplayShowCustomEnabled(true);
        mTitle.hide();
        initView();
        getWindow().setStatusBarColor(Color.parseColor("#EA8235"));//statusBar
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        findViewById(R.id.clMain).setOnClickListener(v -> imm.hideSoftInputFromWindow(etSerch.getWindowToken(), 0));
        getVideoList();
        fragment0 = new Fragment0(this);
        fragment1 = new Fragment1(this);
        fragment2 = new Fragment2(this);
        fragment3 = new Fragment3(this);
        selected = fragment0;
        btnSearch.setOnClickListener(v -> {
            imm.hideSoftInputFromWindow(etSerch.getWindowToken(), 0);

            if(selected != null){
                if(selected == fragment0) {
                    ((Fragment0)getSupportFragmentManager().findFragmentByTag("fragment0")).onLoad();
                }else if(selected == fragment1) {
                    ((Fragment1)getSupportFragmentManager().findFragmentByTag("fragment1")).onLoad();
                }else if(selected == fragment2) {
                    ((Fragment2)getSupportFragmentManager().findFragmentByTag("fragment2")).onLoad();
                }else {
                    ((Fragment3)getSupportFragmentManager().findFragmentByTag("fragment3")).onLoad();
                }
                strSearch = etSerch.getText().toString();
            }
        });

        getSupportFragmentManager().beginTransaction().add(R.id.frame, fragment0,"fragment0").commit();
        TabLayout tabs = findViewById(R.id.tab_layout);
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                imm.hideSoftInputFromWindow(etSerch.getWindowToken(), 0);
                String tag = "";
                if(position == 0){
                    selected = fragment0;
                    tag = "fragment0";
                }else if (position == 1){
                    selected = fragment1;
                    tag = "fragment1";
                }else if (position == 2){
                    selected = fragment2;
                    tag = "fragment2";
                }else if (position == 3){
                    selected = fragment3;
                    tag = "fragment3";
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.frame, selected,tag).commit();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    protected void initView() {
        etSerch = findViewById(R.id.etSerch);
        btnSearch = findViewById(R.id.btnSearch);
    }

    void getVideoList() {
        new Thread(() -> {
            Call<ResponseGetVideoList> call = RetrofitClient
                    .getApiService()
                    .getVideoList(TYPE_ALL,"");
            call.enqueue(new Callback<ResponseGetVideoList>() {
                @Override
                public void onResponse(Call<ResponseGetVideoList> call, Response<ResponseGetVideoList> response) {
                    ResponseGetVideoList body = response.body();
                    if(body == null) {
                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.textCheckInformation),Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(body.getCode() == 0) {
                        customApplication.setVideoList(body.getVideoList());
                        for(int i = 0 ; i < customApplication.getVideoList().size() ; i++) {
                        }

                    }else {
                        Toast.makeText(getApplicationContext(),body.getEnmsg(),Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
                @Override
                public void onFailure(Call<ResponseGetVideoList> call, Throwable t) {
                    Log.d("yot132","failed = " + t);
                }
            });
        }).start();
    }
}