package com.biomhope.glass.face.home.master;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatRatingBar;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.biomhope.glass.face.R;
import com.biomhope.glass.face.bean.GroupUserInfo;
import com.biomhope.glass.face.bean.UserInfo;
import com.biomhope.glass.face.home.settings.FaceLibGroupDetailActivity;
import com.biomhope.glass.face.utils.CommonUtil;
import com.biomhope.glass.face.utils.LogUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * author:BH
 * create at:2018/9/5
 * description: Dialog形式显示人物详情
 */
public class FaceSimilarityDetailActivity extends Activity {

    private static final String TAG = FaceLibGroupDetailActivity.class.getSimpleName();

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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_activity_user_detail);
        ButterKnife.bind(this);
        initGetIntentAndViews();
    }

    private void initGetIntentAndViews() {
        UserInfo userInfo = (UserInfo) getIntent().getExtras().get("userInfo");
        if (userInfo != null) {
            LogUtil.i(TAG, "userInfo: " + userInfo.toString());
            if (GroupUserInfo.USER_TYPE_VIP.equals(userInfo.family) && !TextUtils.isEmpty(userInfo.viplevel)) {
                rat_vip_level.setRating(Float.valueOf(userInfo.viplevel));
            } else {
                rat_vip_level.setRating(0);
            }
            tv_user_name.setText(userInfo.name);
            tv_user_info_idcard.setText(userInfo.idCard);
            tv_user_info_family_address.setText(userInfo.address);
            tv_user_info_total_assets.setText(format(userInfo.totalassets));
            tv_user_assets_terminal.setText(getResources().getString(R.string.user_assets_terminal) + format(userInfo.terminal));
            tv_user_assets_due_on_demand.setText(getResources().getString(R.string.user_assets_due_on_demand) + format(userInfo.duendemand));
            tv_user_assets_funds.setText(getResources().getString(R.string.user_assets_funds) + format(userInfo.funds));
            tv_user_assets_manage_money.setText(getResources().getString(R.string.user_assets_manage_money) + format(userInfo.managemoney));
            tv_user_assets_risk.setText(getResources().getString(R.string.user_assets_risk) + format(userInfo.risk));

            CommonUtil.loadOnlinePic(this, userInfo.faceDefine, iv_user_header);

        }
    }

    private String format(String value) {
        if (TextUtils.isEmpty(value)) {
            return "**";
        }
        return value;
    }

}
