package com.biomhope.glass.face.home.settings;

import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.biomhope.glass.face.R;
import com.biomhope.glass.face.base.BaseActivity;

import butterknife.BindView;
import butterknife.OnClick;

public class RecognizeModeActivity extends BaseActivity {

    private boolean isOpenAutoMode = false;

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
        switch_compat_first_line.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isOpenAutoMode = isChecked;
                Log.i(TAG, "onCheckedChanged: isChecked = " + isChecked);
            }
        });
    }

    @Override
    protected boolean isEventSubscribe() {
        return false;
    }

}
