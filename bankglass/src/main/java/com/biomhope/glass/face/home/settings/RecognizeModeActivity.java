package com.biomhope.glass.face.home.settings;

import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.biomhope.glass.face.R;
import com.biomhope.glass.face.global.BaseActivity;
import com.biomhope.glass.face.utils.SharedPreferencesUtils;

import butterknife.BindView;
import butterknife.OnClick;

public class RecognizeModeActivity extends BaseActivity {

    private boolean isOpenAutoMode;
    private boolean recog_mode;

    @BindView(R.id.ib_back)
    RelativeLayout ib_back;

    @BindView(R.id.tv_center_title)
    TextView tv_center_title;

    @BindView(R.id.tv_first_line)
    TextView tv_first_line;

    @BindView(R.id.switch_compat_first_line)
    SwitchCompat switch_compat_first_line;

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
        tv_center_title.setText(getResources().getString(R.string.set_recog_mode));
        tv_first_line.setText(getResources().getString(R.string.set_auto_mode));
        recog_mode = (boolean) SharedPreferencesUtils.get(this, "recog_mode", true);
        isOpenAutoMode = recog_mode;
        switch_compat_first_line.setChecked(isOpenAutoMode);
        switch_compat_first_line.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isOpenAutoMode = isChecked;
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (isOpenAutoMode != recog_mode) {
            SharedPreferencesUtils.put(this, "recog_mode", isOpenAutoMode);
        }
        super.onDestroy();
    }

    @Override
    protected boolean isEventSubscribe() {
        return false;
    }

}
