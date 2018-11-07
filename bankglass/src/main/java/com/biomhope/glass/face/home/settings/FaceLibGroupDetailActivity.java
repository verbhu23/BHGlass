package com.biomhope.glass.face.home.settings;

import android.os.Bundle;
import android.support.v7.widget.AppCompatRatingBar;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.biomhope.glass.face.R;
import com.biomhope.glass.face.global.BaseActivity;
import com.biomhope.glass.face.bean.GroupUserInfo;
import com.biomhope.glass.face.utils.CommonUtil;
import com.biomhope.glass.face.utils.LogUtil;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * author:BH
 * create at:2018/9/5
 * description: 点击进入列表详情
 */
public class FaceLibGroupDetailActivity extends BaseActivity {

    @BindView(R.id.ib_back)
    RelativeLayout ib_back;

    @BindView(R.id.tv_center_title)
    TextView tv_center_title;

    @BindView(R.id.iv_user_header)
    ImageView iv_user_header;

    @BindView(R.id.tv_user_name)
    TextView tv_user_name;

    @BindView(R.id.tv_user_sex)
    TextView tv_user_sex;

    @BindView(R.id.tv_user_idcard)
    TextView tv_user_idcard;

    @BindView(R.id.tv_user_phone_number)
    TextView tv_user_phone_number;

    @BindView(R.id.tv_user_family_address)
    TextView tv_user_family_address;

    @BindView(R.id.user_set_vip_level)
    AppCompatRatingBar user_set_vip_level;

    @BindView(R.id.show_vip_aera)
    RelativeLayout show_vip_aera;

    @OnClick(R.id.ib_back)
    void click(View v) {
        if (CommonUtil.isFastDoubleClick()) return;
        switch (v.getId()) {
            case R.id.ib_back:
                finish();
                break;
        }
    }

    @Override
    protected void setContentView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_face_lib_group_detail);
    }

    @Override
    protected void initialize() {
        ib_back.setVisibility(View.VISIBLE);

        tv_center_title.setText("身份详情");
        GroupUserInfo bean = (GroupUserInfo) getIntent().getExtras().get("bean");
        if (bean == null) {
            finish();
        }
        LogUtil.i(TAG, "当前身份详情信息:" + bean.toString());
        if (!TextUtils.isEmpty(bean.img2)) {
            CommonUtil.loadLocalPic(this, bean.img2, iv_user_header);
        } else {
            CommonUtil.loadOnlinePic(this, bean.img1, iv_user_header);
        }
        tv_user_name.setText(bean.name);
        tv_user_idcard.setText(bean.idCard);

//        tv_user_sex.setText("性别未知");
        tv_user_family_address.setText(bean.address);
        tv_user_phone_number.setText(bean.phoneNumber);

        String group_name = getIntent().getExtras().getString("group_name", "");
        if (getResources().getString(R.string.user_type_vip).equals(group_name) || GroupUserInfo.USER_TYPE_VIP.equals(bean.family)) {
            if (!TextUtils.isEmpty(bean.viplevel)) {
                show_vip_aera.setVisibility(View.VISIBLE);
                user_set_vip_level.setRating(Float.valueOf(bean.viplevel));
            }
        }
    }

    @Override
    protected boolean isEventSubscribe() {
        return false;
    }
}
