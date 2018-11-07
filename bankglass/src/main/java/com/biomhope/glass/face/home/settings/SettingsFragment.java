package com.biomhope.glass.face.home.settings;

import android.app.Dialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.biomhope.glass.face.R;
import com.biomhope.glass.face.global.BaseFragment;
import com.biomhope.glass.face.home.MainActivity;
import com.biomhope.glass.face.home.login.LoginActivity;
import com.biomhope.glass.face.utils.CommonUtil;
import com.biomhope.glass.face.utils.SharedPreferencesUtils;

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
                CommonUtil.skipAnotherActivity(getActivity(), FaceLibraryActivity.class);
                break;
            case R.id.rl_set_about:
                break;
            case R.id.rl_login_out:
                showLogoutDialog();
                break;
        }
    }

    private Dialog delDialog;

    private void showLogoutDialog() {
        if (delDialog == null) {
            delDialog = new Dialog(context);
            View view = View.inflate(context, R.layout.dialog_exit_app, null);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.width = CommonUtil.getScreenWidth(context) * 3 / 4;
            delDialog.setContentView(view, layoutParams);
            delDialog.setCanceledOnTouchOutside(true);
            ((TextView) view.findViewById(R.id.tv_title_tips)).setText("提示");
            ((TextView) view.findViewById(R.id.tv_title_content)).setText("确定退出当前登录账号?");

            view.findViewById(R.id.bind_email_tv_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    delDialog.dismiss();
                }
            });
            view.findViewById(R.id.bind_email_tv_ok).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    delDialog.dismiss();
                    // 清空本地userId
                    SharedPreferencesUtils.put(context, "userId", "");
                    CommonUtil.skipAnotherActivity((MainActivity) context, LoginActivity.class, true);
                }
            });
        }
        delDialog.show();
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
