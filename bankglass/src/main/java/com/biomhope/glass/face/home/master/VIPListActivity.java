package com.biomhope.glass.face.home.master;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.biomhope.glass.face.R;
import com.biomhope.glass.face.base.BaseActivity;
import com.biomhope.glass.face.bean.FaceDecetListBean;
import com.biomhope.glass.face.global.Constants;
import com.biomhope.glass.face.home.adapter.CropFaceAdapter;
import com.biomhope.glass.face.home.adapter.VipUserAdapter;
import com.biomhope.glass.face.utils.CommonUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.OnClick;

public class VIPListActivity extends BaseActivity {

    private final String[] names = {"张杰", "刘闯", "马冬冬", "James", "郭晨",
            "赵鹏程", "路易", "周三", "秦思", "昊仁", "李达", "钱数", "孙思",
            "李佳佳", "刘浩", "赵蕈", "陈丽", "陈思远", "伍元柒", "伍全", "刘昊", "张媛", "陈萍",
            "涵亮", "天佑", "逸豪", "玺东", "祥民", "楚铭", "豪伦", "伏冰", "泓杰", "德元", "增冰", "冰刑", "乐康", "贤铭"
    };

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

    private List<FaceDecetListBean> faceDecetListBeans;
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
        initFaceData();
        if (faceDecetListBeans.size() > 0) {
            face_vs_vip.showNext();
        }
        vipUserAdapter = new VipUserAdapter(this, faceDecetListBeans, new VipUserAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(FaceDecetListBean faceDecetListBean) {
                Intent simDetailIntent = new Intent(VIPListActivity.this, FaceSimDetailActivity.class);
                simDetailIntent.putExtra("imgUrl", faceDecetListBean.imgCropPath);
                simDetailIntent.putExtra("name", faceDecetListBean.name);
                simDetailIntent.putExtra("vipLevel", faceDecetListBean.level);
                startActivity(simDetailIntent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
        recyclerview.setLayoutManager(new LinearLayoutManager(this));
        recyclerview.setItemAnimator(new DefaultItemAnimator());
        recyclerview.setAdapter(vipUserAdapter);

    }

    // temp方法 检测本地相册
    private void initFaceData() {
        if (faceDecetListBeans == null) {
            faceDecetListBeans = new ArrayList<>();
        }
        // <<<<<<<<<<<<<<6.0读取文件权限问题 主动打开应用权限
        File file = new File(Constants.FACE_DECECTION_PIC_CROP_FILE);
        if (file.exists()) {
            String[] list = file.list();
            if (list != null && list.length > 0) {
                for (int i = 0; i < list.length; i++) {
                    int sim = new Random().nextInt(99);
                    FaceDecetListBean bean = new FaceDecetListBean(
                            Constants.FACE_DECECTION_PIC_CROP_FILE + File.separator + list[i],
                            names[i],
                            CommonUtil.getTimeStr(list[i]),
                            String.valueOf(sim),
                            (sim >= 50 ? "检测成功" : "未知的"));
                    bean.userType = "vip";
                    int level = new Random().nextInt(6);
                    if (level == 0)
                        level += 1;
                    bean.level = "" + level;
                    faceDecetListBeans.add(bean);
                    Log.i(TAG, "initFaceData: bean " + bean.toString());
                }
                Collections.reverse(faceDecetListBeans);
            }
        }
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
