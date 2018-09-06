package com.biomhope.glass.face.home;

import android.Manifest;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.RadioGroup;

import com.biomhope.glass.face.R;
import com.biomhope.glass.face.base.BaseActivity;
import com.biomhope.glass.face.home.fragment.FaceRegisterFragment;
import com.biomhope.glass.face.home.fragment.MainListFragment;
import com.biomhope.glass.face.home.fragment.OcrFunctionFragment;
import com.biomhope.glass.face.home.fragment.SettingsFragment;
import com.biomhope.glass.face.home.fragment.VideoActivedFragment;
import com.biomhope.glass.face.utils.CheckPermissionUtil;
import com.biomhope.glass.face.utils.CommonUtil;
import com.llvision.glass3.framework.LLVisionGlass3SDK;

import butterknife.BindView;
/**
 * author:BH
 * create at:2018/9/5
 * description:
 *
 */
public class MainActivity extends BaseActivity {

    @BindView(R.id.radiogroup_main)
    RadioGroup radiogroup_main;

    private static final int FOR_OCR_PERMISSION_CODE = 1;
    private static final String[] OCR_NEED_PERMISSION = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,//  Write access
            Manifest.permission.READ_EXTERNAL_STORAGE, //  Read access
            Manifest.permission.READ_PHONE_STATE, Manifest.permission.CAMERA,
            Manifest.permission.VIBRATE, Manifest.permission.INTERNET
    };
    private Fragment mLastFragment;
    private int mCurrentPagePosition;
    private String[] mFrgTags = null;

    @Override
    protected void setContentView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void initialize() {
        // 检查必要权限
        if (CheckPermissionUtil.permissionSet(this, OCR_NEED_PERMISSION)) {
            ActivityCompat.requestPermissions(this, OCR_NEED_PERMISSION, FOR_OCR_PERMISSION_CODE);
        } else {
            allPermissionGranted();
        }
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
            Log.i(TAG, "onRequestPermissionsResult: hasPermissionDismiss = " + hasPermissionDismiss);
            allPermissionGranted();
            // 勾选不再询问
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) ||
                    !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                CommonUtil.showRequestPermissionDialog("该App需要存储及相机等多个权限，不开启将无法正常工作！", this);
            }
        }
    }

    private void allPermissionGranted() {
        mFrgTags = new String[]{
                this.getResources().getString(R.string.tab_face_register),
                this.getResources().getString(R.string.tab_ocr_function),
                this.getResources().getString(R.string.tab_main_list),
                this.getResources().getString(R.string.tab_video_actived),
                this.getResources().getString(R.string.tab_settings)};
        if (mLastFragment == null) {
            mLastFragment = new MainListFragment();
            mCurrentPagePosition = 2;
        }
        getFragmentManager().beginTransaction()
                .replace(R.id.content, mLastFragment, mFrgTags[mCurrentPagePosition]).commit();

        radiogroup_main.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_tab_face_register:
                        switchFragment(0);
                        break;
                    case R.id.rb_tab_ocr_function:
                        switchFragment(1);
                        break;
                    case R.id.rb_tab_main_list:
                        switchFragment(2);
                        break;
                    case R.id.rb_tab_video_actived:
                        switchFragment(3);
                        break;
                    case R.id.rb_tab_settings:
                        switchFragment(4);
                        break;
                }
            }
        });
    }

    private void switchFragment(int position) {
        if (position == mCurrentPagePosition) return;
        mCurrentPagePosition = position;

        Fragment fragmentByTag = getFragmentManager().findFragmentByTag(mFrgTags[mCurrentPagePosition]);
        if (fragmentByTag == null) {
            if (mCurrentPagePosition == 0) {
                fragmentByTag = new FaceRegisterFragment();
            } else if (mCurrentPagePosition == 1) {
                fragmentByTag = new OcrFunctionFragment();
            } else if (mCurrentPagePosition == 2) {
                fragmentByTag = new MainListFragment();
            } else if (mCurrentPagePosition == 3) {
                fragmentByTag = new VideoActivedFragment();
            } else if (mCurrentPagePosition == 4) {
                fragmentByTag = new SettingsFragment();
            }
            if (fragmentByTag != null && !fragmentByTag.isAdded()) {
                getFragmentManager().beginTransaction()
                        .hide(mLastFragment).add(R.id.content, fragmentByTag, mFrgTags[mCurrentPagePosition])
                        .commit();
            }
        } else {
            if (fragmentByTag.isAdded()) {
                getFragmentManager().beginTransaction()
                        .hide(mLastFragment).show(fragmentByTag).commit();
            }
        }
        mLastFragment = fragmentByTag;
    }

    @Override
    protected void onDestroy() {
        // SplashActivity中已经初始化LLSDK
        LLVisionGlass3SDK.getInstance().destroy();
        super.onDestroy();
    }

    public int getCurrentPagePosition() {
        return mCurrentPagePosition;
    }

    @Override
    protected boolean isEventSubscribe() {
        return false;
    }
}