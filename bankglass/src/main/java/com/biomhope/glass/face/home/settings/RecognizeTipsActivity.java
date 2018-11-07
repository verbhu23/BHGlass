package com.biomhope.glass.face.home.settings;

import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.biomhope.glass.face.R;
import com.biomhope.glass.face.global.BaseActivity;
import com.biomhope.glass.face.utils.LogUtil;
import com.biomhope.glass.face.utils.SharedPreferencesUtils;

import butterknife.BindView;
import butterknife.OnClick;

public class RecognizeTipsActivity extends BaseActivity {

    private boolean isOpenShowAll;
    private boolean isOpenWarningTone;

    @BindView(R.id.ib_back)
    RelativeLayout ib_back;

    @BindView(R.id.tv_center_title)
    TextView tv_center_title;

    @BindView(R.id.tv_first_line)
    TextView tv_first_line;

    @BindView(R.id.switch_compat_first_line)
    SwitchCompat switch_compat_first_line;

    @BindView(R.id.tv_second_line)
    TextView tv_second_line;

    @BindView(R.id.tv_show_all_tips)
    TextView tv_show_all_tips;

    @BindView(R.id.switch_compat_second_line)
    SwitchCompat switch_compat_second_line;

    @BindView(R.id.rl_second_layout)
    RelativeLayout rl_second_layout;
    private boolean recog_result_tips;
    private boolean recog_result_voice;

    @OnClick(R.id.ib_back)
    void back() {
        finish();
    }

    @Override
    protected void setContentView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_recognize_set);
    }

    @Override
    protected void initialize() {
        ib_back.setVisibility(View.VISIBLE);
        tv_center_title.setText(getResources().getString(R.string.set_recog_result_tips));
        tv_first_line.setText(getResources().getString(R.string.set_glass_show_all));
        tv_second_line.setText(getResources().getString(R.string.set_warning_tone));
        tv_show_all_tips.setVisibility(View.VISIBLE);
        rl_second_layout.setVisibility(View.VISIBLE);

        recog_result_tips = (boolean) SharedPreferencesUtils.get(this, "recog_result_tips", true);
        switch_compat_first_line.setChecked(recog_result_tips);
        isOpenShowAll = recog_result_tips;
        switch_compat_first_line.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isOpenShowAll = isChecked;
                LogUtil.v(TAG, "眼镜端提示" + isChecked);
            }
        });
        recog_result_voice = (boolean) SharedPreferencesUtils.get(this, "recog_result_voice", false);
        switch_compat_second_line.setChecked(recog_result_voice);
        isOpenWarningTone = recog_result_voice;
        switch_compat_second_line.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isOpenWarningTone = isChecked;
                LogUtil.v(TAG, "提示音" + isChecked);
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (recog_result_tips != isOpenShowAll) {
            SharedPreferencesUtils.put(this, "recog_result_tips", isOpenShowAll);
        }
        if (recog_result_voice != isOpenWarningTone) {
            SharedPreferencesUtils.put(this, "recog_result_voice", isOpenWarningTone);
        }
        super.onDestroy();
    }

    @Override
    protected boolean isEventSubscribe() {
        return false;
    }

}
