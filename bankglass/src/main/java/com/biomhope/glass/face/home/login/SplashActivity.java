package com.biomhope.glass.face.home.login;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.RelativeLayout;

import com.biomhope.glass.face.R;
import com.biomhope.glass.face.global.BaseActivity;
import com.biomhope.glass.face.utils.CheckPermissionUtil;
import com.biomhope.glass.face.utils.CommonUtil;
import com.biomhope.glass.face.utils.LLCameraUtil;
import com.biomhope.glass.face.utils.LogUtil;
import com.llvision.glass3.framework.LLVisionGlass3SDK;

import butterknife.BindView;

public class SplashActivity extends BaseActivity {

    @BindView(R.id.splash_root_view)
    RelativeLayout splash_root_view;

    private Handler mHandler = new Handler();

    private static final int REQUEST_PERMISSION_OVERLAY = 100;
    private static final int FOR_OCR_PERMISSION_CODE = 1;
    private static final String[] OCR_NEED_PERMISSION = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,//  Write access
            Manifest.permission.READ_EXTERNAL_STORAGE, //  Read access
            Manifest.permission.READ_PHONE_STATE, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO,
            Manifest.permission.VIBRATE, Manifest.permission.INTERNET
    };

    @Override
    protected void setContentView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_splash);
//        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);// 去掉信息栏
        // 隐藏虚拟键盘并且全屏
        View decorView = getWindow().getDecorView();
        // Hide both the navigation bar and the status bar.
        // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
        // a general rule, you should design your app to hide the status bar whenever you
        // hide the navigation bar.
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    protected void initialize() {
        // 加载sdk引擎
        LLVisionGlass3SDK.getInstance().init(this);
        LLCameraUtil.getInstance(this).loadSyncFeatures();

        checkOverlayPermission();

        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
        alphaAnimation.setDuration(3000);
        alphaAnimation.setFillAfter(true);
        splash_root_view.setAnimation(alphaAnimation);

//        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                CommonUtil.skipAnotherActivity(SplashActivity.this, LoginActivity.class, true);
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//            }
//        });

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FOR_OCR_PERMISSION_CODE) {
            boolean hasPermissionDismiss = false;
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_DENIED) {
                    hasPermissionDismiss = true;
                }
            }
            LogUtil.i(TAG, hasPermissionDismiss ? "还有权限未开启." : "权限全部开启.");

            // 为true说明仍有权限没打开
            if (hasPermissionDismiss) {
                // 勾选不再询问
//                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) ||
//                        !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//                }
                new AlertDialog.Builder(this)
                        .setMessage("App需要存储及相机等多个权限,不开启将无法正常工作.")
                        .setPositiveButton("去授权", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                // 引导用户至设置页手动授权
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getApplicationContext().getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                                finish();
                            }
                        })
//                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.cancel();
//                            }
//                        })
                        .setCancelable(false)
                        .create().show();
            } else {
                allPermissionGranted();
            }
        }
    }

    private void allPermissionGranted() {
        // 权限都通过
        LogUtil.i(TAG, "权限已全部开启 start跳转至LoginActivity.");
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                CommonUtil.skipAnotherActivity(SplashActivity.this, LoginActivity.class, true);
            }
        }, 1500);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PERMISSION_OVERLAY) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    LogUtil.i(TAG, "悬浮窗权限仍未开启.");
                } else {
                    LogUtil.i(TAG, "悬浮窗权限已开启.");
                }
            }
            // 检查必要权限
            if (CheckPermissionUtil.permissionSet(this, OCR_NEED_PERMISSION)) {
                ActivityCompat.requestPermissions(this, OCR_NEED_PERMISSION, FOR_OCR_PERMISSION_CODE);
            } else {
                allPermissionGranted();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        super.onDestroy();
    }

    @Override
    protected boolean isEventSubscribe() {
        return false;
    }

    // 悬浮窗权限
    private void checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_PERMISSION_OVERLAY);
            } else {
                Log.i(TAG, "悬浮窗权限已开启 检查其他权限.");
                // 检查必要权限
                if (CheckPermissionUtil.permissionSet(this, OCR_NEED_PERMISSION)) {
                    ActivityCompat.requestPermissions(this, OCR_NEED_PERMISSION, FOR_OCR_PERMISSION_CODE);
                } else {
                    allPermissionGranted();
                }
            }
        } else {
            allPermissionGranted();
        }
    }

}
