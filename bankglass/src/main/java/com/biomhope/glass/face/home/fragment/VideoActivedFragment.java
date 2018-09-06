package com.biomhope.glass.face.home.fragment;

import android.widget.TextView;

import com.biomhope.glass.face.R;
import com.biomhope.glass.face.base.BaseFragment;

import butterknife.BindView;
/**
 * author:BH
 * create at:2018/9/5
 * description:
 *
 */
public class VideoActivedFragment extends BaseFragment {

    @BindView(R.id.tv_center_title)
    TextView tv_center_title;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_video_actived;
    }

    @Override
    protected void initialize() {
        tv_center_title.setText(getResources().getString(R.string.tab_video_actived));
    }

    @Override
    protected boolean isEventSubscribe() {
        return false;
    }
}
