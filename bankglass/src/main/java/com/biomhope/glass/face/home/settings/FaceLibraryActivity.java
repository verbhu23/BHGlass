package com.biomhope.glass.face.home.settings;

import android.app.Fragment;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.biomhope.glass.face.R;
import com.biomhope.glass.face.global.BaseActivity;
import com.biomhope.glass.face.utils.CommonUtil;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author $USER_NAME
 * create at : 2018-10-12
 * description : 人脸库
 */
public class FaceLibraryActivity extends BaseActivity {

    @BindView(R.id.ib_back)
    RelativeLayout ib_back;

    @BindView(R.id.tv_center_title)
    TextView tv_center_title;

    @BindView(R.id.tv_event_my_create)
    TextView tv_event_my_create;

    @BindView(R.id.tv_event_sys_push)
    TextView tv_event_sys_push;

    @BindView(R.id.content)
    FrameLayout content;

    private String[] mFrgTags = new String[]{"my", "sys"};
    private Fragment mLastFragment;
    private int mCurrentPagePosition;

    @OnClick({R.id.ib_back, R.id.tv_event_my_create, R.id.tv_event_sys_push})
    void click(View v) {
        if (CommonUtil.isFastDoubleClick()) return;
        switch (v.getId()) {
            case R.id.ib_back:
                finish();
                break;
            case R.id.tv_event_my_create:
                switchMenu(0);
                break;
            case R.id.tv_event_sys_push:
                switchMenu(1);
                break;
        }
    }

    private void switchMenu(int position) {
        if (position == mCurrentPagePosition) return;
        if (position == 0) {
            tv_event_my_create.setSelected(true);
            tv_event_sys_push.setSelected(false);
        } else if (position == 1) {
            tv_event_my_create.setSelected(false);
            tv_event_sys_push.setSelected(true);
        }
        mCurrentPagePosition = position;
        Fragment fragmentByTag = getFragmentManager().findFragmentByTag(mFrgTags[mCurrentPagePosition]);
        if (fragmentByTag == null) {
            if (mCurrentPagePosition == 0) {
                fragmentByTag = new GroupMyFragment();
            } else if (mCurrentPagePosition == 1) {
                fragmentByTag = new GroupSysPushFragment();
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
    protected void setContentView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_face_library);
    }

    @Override
    protected void initialize() {
        ib_back.setVisibility(View.VISIBLE);
        tv_center_title.setText(getResources().getString(R.string.set_face_lib));

        if (mLastFragment == null) {
            mLastFragment = new GroupMyFragment();
            mCurrentPagePosition = 0;
            tv_event_my_create.setSelected(true);
        }
        getFragmentManager().beginTransaction()
                .replace(R.id.content, mLastFragment, mFrgTags[mCurrentPagePosition]).commit();
    }

    @Override
    protected boolean isEventSubscribe() {
        return false;
    }

}
