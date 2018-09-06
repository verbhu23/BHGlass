package com.biomhope.glass.face.home;

import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import com.biomhope.glass.face.R;
import com.biomhope.glass.face.base.BaseActivity;
import com.biomhope.glass.face.home.login.LoginActivity;
import com.biomhope.glass.face.utils.CommonUtil;
import com.llvision.glass3.framework.LLVisionGlass3SDK;

public class SplashActivity extends BaseActivity {

    private Handler mHandler = null;

    @Override
    protected void setContentView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_splash);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);// 去掉信息栏
    }

    @Override
    protected void initialize() {
        // 加载sdk引擎
        LLVisionGlass3SDK.getInstance().init(this);
        mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                CommonUtil.skipAnotherActivity(SplashActivity.this, LoginActivity.class, true);
            }
        }, 2000);

    }

    @Override
    protected void onDestroy() {
        if (mHandler != null) mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    protected boolean isEventSubscribe() {
        return false;
    }

}
