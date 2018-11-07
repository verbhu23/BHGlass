package com.biomhope.glass.face.home;

import android.app.Dialog;
import android.app.Fragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.biomhope.glass.face.R;
import com.biomhope.glass.face.global.BaseActivity;
import com.biomhope.glass.face.bean.eventvo.GlassTipsMessage;
import com.biomhope.glass.face.home.master.MainListFragment;
import com.biomhope.glass.face.home.ocr.OcrForICBCFragment;
import com.biomhope.glass.face.home.settings.SettingsFragment;
import com.biomhope.glass.face.home.session.ExpertSessionFragment;
import com.biomhope.glass.face.utils.CommonUtil;
import com.biomhope.glass.face.utils.LLCameraUtil;
import com.llvision.glass3.framework.LLVisionGlass3SDK;

import butterknife.BindView;

/**
 * author:BH
 * create at:2018/9/5
 * description:
 */
public class MainActivity extends BaseActivity {

    @BindView(R.id.radiogroup_main)
    RadioGroup radiogroup_main;

    private Dialog delDialog;
    private Fragment mLastFragment;
    private int mCurrentPagePosition;
    private String[] mFrgTags = null;

    @Override
    protected void setContentView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void initialize() {
        allPermissionGranted();
    }

    private void allPermissionGranted() {

        String userId = getIntent().getExtras().getString("userId", "");
        mFrgTags = new String[]{
                this.getResources().getString(R.string.tab_main_list),
                this.getResources().getString(R.string.tab_ocr_function),
                this.getResources().getString(R.string.tab_video_actived),
                this.getResources().getString(R.string.tab_settings)};
        if (mLastFragment == null) {
            mLastFragment = new MainListFragment();
            Bundle bundle = new Bundle();
            bundle.putString("userId", userId);
            mLastFragment.setArguments(bundle);
            mCurrentPagePosition = 0;
        }
        getFragmentManager().beginTransaction()
                .replace(R.id.content, mLastFragment, mFrgTags[mCurrentPagePosition]).commit();
        LLCameraUtil.getInstance(this).setmCurrentPosition(mCurrentPagePosition);
        radiogroup_main.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_tab_main_list:
                        switchFragment(0);
                        break;
                    case R.id.rb_tab_ocr_function:
                        switchFragment(1);
                        break;
                    case R.id.rb_tab_video_actived:
                        switchFragment(2);
                        break;
                    case R.id.rb_tab_settings:
                        switchFragment(3);
                        break;
                }
            }
        });
    }

    public void setBottomMenuStatus(boolean shouldHide) {
        radiogroup_main.setVisibility(shouldHide ? View.GONE : View.VISIBLE);
    }

    public void setGlassTipsMsg(GlassTipsMessage glassTipsMessage) {
        LLCameraUtil.getInstance(this).setGlassTipsMsg(glassTipsMessage);
    }

    private void switchFragment(int position) {
        // position == 0时开启 非0时关闭
        LLCameraUtil.getInstance(this).setmCurrentPosition(position);
        if (position == mCurrentPagePosition) return;
        mCurrentPagePosition = position;

        Fragment fragmentByTag = getFragmentManager().findFragmentByTag(mFrgTags[mCurrentPagePosition]);
        if (fragmentByTag == null) {
            if (mCurrentPagePosition == 0) {
                fragmentByTag = new MainListFragment();
            } else if (mCurrentPagePosition == 1) {
//                fragmentByTag = new OcrFunctionFragment();
                fragmentByTag = new OcrForICBCFragment();
            } else if (mCurrentPagePosition == 2) {
                fragmentByTag = new ExpertSessionFragment();
            } else if (mCurrentPagePosition == 3) {
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
//        LLVisionGlass3SDK.getInstance().destroy();
        super.onDestroy();
    }

    public int getCurrentPagePosition() {
        return mCurrentPagePosition;
    }

    @Override
    protected boolean isEventSubscribe() {
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            if (delDialog == null) {
                delDialog = new Dialog(this);
                View view = View.inflate(this, R.layout.dialog_exit_app, null);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.width = CommonUtil.getScreenWidth(this) * 3 / 4;
                delDialog.setContentView(view, layoutParams);
                delDialog.setCanceledOnTouchOutside(true);

                ((TextView) view.findViewById(R.id.tv_title_tips)).setText("提示");
                ((TextView) view.findViewById(R.id.tv_title_content)).setText("确定退出?");
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

                        finish();
                        // 释放llcamera资源
                        LLCameraUtil.getInstance(MainActivity.this).release(true);
                        // SplashActivity中已经初始化LLSDK
                        LLVisionGlass3SDK.getInstance().destroy();
                        int pid = android.os.Process.myPid();
                        android.os.Process.killProcess(pid);

                        System.exit(0);
                    }
                });
            }
            delDialog.show();

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}