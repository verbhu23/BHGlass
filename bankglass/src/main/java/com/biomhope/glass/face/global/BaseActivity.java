package com.biomhope.glass.face.global;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.biomhope.glass.face.R;

import org.greenrobot.eventbus.EventBus;

import butterknife.ButterKnife;

/**
 * author:BH
 * create at:2018/9/6
 * description:
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected final String TAG = getClass().getSimpleName();
    protected Toast mToast;
    protected Dialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initSystemBarTint();
        // no title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(savedInstanceState);
        ButterKnife.bind(this);
        if (isEventSubscribe())
            EventBus.getDefault().register(this);
        initialize();
    }

    /**
     * 设置状态栏颜色
     * AR眼镜要求系统最少支持5.0以上
     */
    protected void initSystemBarTint() {
        Window window = getWindow();
        if (translucentStatusBar()) {
            // 设置状态栏全透明
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // APi 21及以上
                // 设置状态栏颜色透明与adjust调整布局冲突
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.TRANSPARENT);
            }
            return;
        }
        // 沉浸式状态栏 APi 21
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // 5.0以上使用原生方法
//            View decorView = window.getDecorView();
//            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
//            decorView.setSystemUiVisibility(option);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(setStatusBarColor());
        }
    }

    /**
     * 子类可以重写决定是否使用透明状态栏
     */
    protected boolean translucentStatusBar() {
        return false;
    }

    /**
     * 子类可以重写改变状态栏颜色
     */
    protected int setStatusBarColor() {
        return R.color.ratingbar_activated;
    }

    protected abstract void setContentView(Bundle savedInstanceState);

    protected abstract void initialize();

    protected abstract boolean isEventSubscribe();

    protected void showToast(String content) {
        showToast(Toast.LENGTH_SHORT, content);
    }

    @SuppressLint("ShowToast")
    protected void showToast(int time, String content) {
        if (mToast == null) {
            mToast = Toast.makeText(this, content, time);
        } else {
            mToast.setText(content);
        }
        mToast.show();
    }

    protected void showConnectHttpError() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showToast(getResources().getString(R.string.http_request_connect_failed));
            }
        });
    }

    protected void showLoadingDialog() {
        if (mDialog == null) {
            mDialog = new Dialog(this, R.style.LoadDialogStyle);
            mDialog.setContentView(R.layout.dialog_common_loading);
            mDialog.setCancelable(false);
        }
        mDialog.show();
    }

    protected void cancelDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {

//		ButterKnife.unbind(this);
        if (isEventSubscribe())
            EventBus.getDefault().unregister(this);
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.cancel();
            mDialog = null;
        }
        super.onDestroy();
    }

}
