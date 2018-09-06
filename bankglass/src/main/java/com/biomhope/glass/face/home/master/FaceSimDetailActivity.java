package com.biomhope.glass.face.home.master;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.AppCompatRatingBar;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.biomhope.glass.face.R;
import com.biomhope.glass.face.base.BaseActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

import java.util.Random;

import butterknife.BindView;
/**
 * author:BH
 * create at:2018/9/5
 * description: Dialog形式显示人物详情
 *
 */
public class FaceSimDetailActivity extends BaseActivity {

    @BindView(R.id.iv_user_header)
    ImageView iv_user_header;

    @BindView(R.id.tv_user_name)
    TextView tv_user_name;

    @BindView(R.id.rat_vip_level)
    AppCompatRatingBar rat_vip_level;

    @BindView(R.id.tv_user_info_idcard)
    TextView tv_user_info_idcard;

    @BindView(R.id.tv_user_info_family_address)
    TextView tv_user_info_family_address;

    @BindView(R.id.tv_user_info_total_assets)
    TextView tv_user_info_total_assets;

    @BindView(R.id.tv_user_assets_terminal)
    TextView tv_user_assets_terminal;

    @BindView(R.id.tv_user_assets_due_on_demand)
    TextView tv_user_assets_due_on_demand;

    @BindView(R.id.tv_user_assets_funds)
    TextView tv_user_assets_funds;

    @BindView(R.id.tv_user_assets_manage_money)
    TextView tv_user_assets_manage_money;

    @BindView(R.id.tv_user_assets_risk)
    TextView tv_user_assets_risk;

    @Override
    protected void setContentView(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_activity_user_detail);
    }

    @Override
    protected void initialize() {
        initGetIntentAndViews();
    }

    private void initGetIntentAndViews() {
        Intent intent = getIntent();
        String imgUrl = intent.getStringExtra("imgUrl");
        String idCard = intent.getStringExtra("idCard");
        String name = intent.getStringExtra("name");
        String vipLevel = intent.getStringExtra("vipLevel");
        String imgUrlType = intent.getStringExtra("imgUrlType");
        String familyAddress = intent.getStringExtra("familyAddress");
        if (TextUtils.isEmpty(idCard)) {
            idCard = getResources().getString(R.string.user_detail_error);
        }
        if (TextUtils.isEmpty(familyAddress)) {
            familyAddress = getResources().getString(R.string.user_detail_error);
        }
        if (TextUtils.isEmpty(name)) {
            name = getResources().getString(R.string.user_detail_error);
        }
        if (!"net".equals(imgUrlType)) {
            imgUrl = "file://" + imgUrl;
        }

        if (TextUtils.isEmpty(vipLevel)) {
            int level = new Random().nextInt(6);
            vipLevel = String.valueOf(level);
        }
        rat_vip_level.setRating(Float.valueOf(vipLevel));
        tv_user_name.setText(name);
        tv_user_info_idcard.setText(idCard);
        tv_user_info_family_address.setText(familyAddress);
        tv_user_info_total_assets.setText("***");
        tv_user_assets_terminal.setText(getResources().getString(R.string.user_assets_terminal) + "***");
        tv_user_assets_due_on_demand.setText(getResources().getString(R.string.user_assets_due_on_demand) + "***");
        tv_user_assets_funds.setText(getResources().getString(R.string.user_assets_funds) + "***");
        tv_user_assets_manage_money.setText(getResources().getString(R.string.user_assets_manage_money) + "***");
        tv_user_assets_risk.setText(getResources().getString(R.string.user_assets_risk) + "*");

        Glide.with(this)
                .load(imgUrl)
                .transition(new DrawableTransitionOptions().crossFade(250))
                .apply(new RequestOptions().bitmapTransform(new CircleCrop()).error(R.drawable.remote_refresh))
                .into(iv_user_header);
    }

    @Override
    protected boolean isEventSubscribe() {
        return false;
    }
}
