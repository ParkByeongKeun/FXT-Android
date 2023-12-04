package com.fiberfox.fxt.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fiberfox.fxt.CustomApplication;
import com.fiberfox.fxt.R;
import com.fiberfox.fxt.RestApi.ResponseGetVideoList;
import com.fiberfox.fxt.RestApi.RetrofitClient;
import com.fiberfox.fxt.TutorialActivity;
import com.fiberfox.fxt.utils.CustomVideoList;
import com.fiberfox.fxt.utils.StripHtml;
import com.fiberfox.fxt.utils.VideoListAdapter;
import com.fiberfox.fxt.widget.XListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Fragment2 extends Fragment implements XListView.IXListViewListener{

    private Handler mHandler;
    private int mIndex = 0;
    private int mRefreshIndex = 0;
    private ArrayList<CustomVideoList> mItems = new ArrayList<CustomVideoList>();
    XListView listView;
    CustomApplication customApplication;
    final String TYPE_ALL = "2";
    Context context;
    public Fragment2(Context context) {
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tutorial, container, false);
        customApplication = (CustomApplication)getActivity().getApplication();
        listView = view.findViewById(R.id.listView_);
        initView();
        getVideoList();
        return view;
    }

    protected void initView() {
        mItems.clear();
        mHandler = new Handler();
        listView.setPullRefreshEnable(false);
        listView.setPullLoadEnable(false);
        listView.setAutoLoadEnable(false);
        listView.setXListViewListener(this);
        listView.setRefreshTime(getTime());
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(customApplication.SERVER + customApplication.getVideoList().get(position-1).getEnVideoUrl()));
            startActivity(intent);
        });
    }

    @Override
    public void onRefresh() {
        mHandler.postDelayed(() -> {
            mIndex = ++mRefreshIndex;
//            onLoad();
        }, 800);
    }

    @Override
    public void onLoadMore() {
        mHandler.postDelayed(() -> onLoad(), 800);
    }

    public void onLoad() {
        listView.stopRefresh();
        listView.stopLoadMore();
        listView.setRefreshTime(getTime());
        getVideoList();
    }

    private String getTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA).format(new Date());
    }

    void getVideoList() {
        mItems.clear();
        customApplication.arrVideoList.clear();
        new Thread(() -> {
            Call<ResponseGetVideoList> call = RetrofitClient
                    .getApiService()
                    .getVideoList(TYPE_ALL,((TutorialActivity)context).strSearch);
            call.enqueue(new Callback<ResponseGetVideoList>() {
                @Override
                public void onResponse(Call<ResponseGetVideoList> call, Response<ResponseGetVideoList> response) {
                    ResponseGetVideoList body = response.body();
                    if(body == null) {
                        Toast.makeText(getActivity(),getResources().getString(R.string.textCheckInformation),Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(body.getCode() == 0) {
//                        customApplication.arrVideoList.clear();
                        customApplication.setVideoList(body.getVideoList());

                        for(int i = 0 ; i < customApplication.getVideoList().size() ; i++) {
                            if(customApplication.getVideoList().get(i).getVoType().equals("2")) {
                                CustomVideoList customDevice1 = new CustomVideoList(
                                        customApplication.SERVER + customApplication.getVideoList().get(i).getVoImgUrl(),
                                        customApplication.getVideoList().get(i).getEnTitle(),
                                        StripHtml.split(customApplication.getVideoList().get(i).getUpdatedTime()));
                                mItems.add(customDevice1);
                            }
                        }
                        VideoListAdapter mAdapter = new VideoListAdapter(getActivity(), R.layout.vw_video_list_item,
                                mItems);
                        listView.setAdapter(mAdapter);
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