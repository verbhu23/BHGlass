package com.biomhope.glass.face.home.master;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.biomhope.glass.face.R;
import com.biomhope.glass.face.global.BaseActivity;
import com.biomhope.glass.face.bean.UserInfo;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class VIPListActivity extends BaseActivity {

    @BindView(R.id.ib_back)
    RelativeLayout ib_back;

    @BindView(R.id.tv_center_title)
    TextView tv_center_title;

    @BindView(R.id.face_vs_vip)
    ViewSwitcher face_vs_vip;

    @BindView(R.id.tv_no_pic_tips)
    TextView tv_no_pic_tips;

    @BindView(R.id.recyclerview)
    RecyclerView recyclerview;

    private List<UserInfo> userInfos;
    private VipUserAdapter vipUserAdapter;

    @OnClick(R.id.ib_back)
    void finished() {
        finish();
    }

    @Override
    protected void setContentView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_vip_list);
    }

    @Override
    protected void initialize() {
        tv_center_title.setText("要客识别");
        ib_back.setVisibility(View.VISIBLE);
        if (userInfos == null) {
            userInfos = new ArrayList<>();
        }
        if (userInfos.size() > 0) {
            face_vs_vip.showNext();
        }
        vipUserAdapter = new VipUserAdapter(this, userInfos, new VipUserAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(UserInfo userInfo) {
                Intent simDetailIntent = new Intent(VIPListActivity.this, FaceSimilarityDetailActivity.class);
                simDetailIntent.putExtra("userInfo", userInfo);
                startActivity(simDetailIntent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
        recyclerview.setLayoutManager(new LinearLayoutManager(this));
        recyclerview.setItemAnimator(new DefaultItemAnimator());
        recyclerview.setAdapter(vipUserAdapter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected boolean isEventSubscribe() {
        return false;
    }

}
