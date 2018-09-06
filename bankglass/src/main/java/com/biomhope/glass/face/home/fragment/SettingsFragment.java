package com.biomhope.glass.face.home.fragment;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.biomhope.glass.face.R;
import com.biomhope.glass.face.base.BaseFragment;
import com.biomhope.glass.face.home.settings.RecognizeModeActivity;
import com.biomhope.glass.face.home.settings.RecognizeTipsActivity;
import com.biomhope.glass.face.home.settings.RecoilRegisterActivity;
import com.biomhope.glass.face.utils.CommonUtil;

import butterknife.BindView;
import butterknife.OnClick;

public class SettingsFragment extends BaseFragment {

    @BindView(R.id.iv_user_header)
    ImageView iv_user_header;

    @BindView(R.id.tv_user_name)
    TextView tv_user_name;

    @BindView(R.id.tv_center_title)
    TextView tv_center_title;

    @OnClick({R.id.iv_user_header, R.id.rl_set_recog_mode, R.id.rl_set_recog_result_tips
            , R.id.rl_set_recoil_register, R.id.rl_set_about, R.id.rl_login_out})
    void click(View v) {
        if (CommonUtil.isFastDoubleClick()) return;
        switch (v.getId()) {
            case R.id.iv_user_header:
                break;
            case R.id.rl_set_recog_mode:
                CommonUtil.skipAnotherActivity(getActivity(), RecognizeModeActivity.class);
                break;
            case R.id.rl_set_recog_result_tips:
                CommonUtil.skipAnotherActivity(getActivity(), RecognizeTipsActivity.class);
                break;
            case R.id.rl_set_recoil_register:
                CommonUtil.skipAnotherActivity(getActivity(), RecoilRegisterActivity.class);
                break;
            case R.id.rl_set_about:
                break;
            case R.id.rl_login_out:
                break;
        }
    }


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_settings;
    }

    @Override
    protected void initialize() {
        tv_center_title.setText(getResources().getString(R.string.tab_settings));
    }

    @Override
    protected boolean isEventSubscribe() {
        return false;
    }
}
