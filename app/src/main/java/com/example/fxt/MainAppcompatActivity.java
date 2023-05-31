package com.example.fxt;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainAppcompatActivity extends AppCompatActivity {

    public static ArrayList<Activity> activityList = new ArrayList<Activity>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityList.add(this);
        getWindow().setStatusBarColor(Color.parseColor("#E56731"));//statusBar
        getWindow().setNavigationBarColor(Color.parseColor("#E56731"));//bottom
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        activityList.remove(this);
    }

    public void activityFinish(){
        for(int i = 0; i < activityList.size(); i++)
            activityList.get(i).finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}