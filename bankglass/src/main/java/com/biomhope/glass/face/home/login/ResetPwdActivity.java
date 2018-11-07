package com.biomhope.glass.face.home.login;

import android.os.Bundle;

import com.biomhope.glass.face.R;
import com.biomhope.glass.face.global.BaseActivity;

/**
 * @author $USER_NAME
 * create at : 2018-10-17
 * description : 重置密码
 */
public class ResetPwdActivity extends BaseActivity {

    @Override
    protected void setContentView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_user_register);
    }

    @Override
    protected void initialize() {

    }

    @Override
    protected boolean isEventSubscribe() {
        return false;
    }
}
