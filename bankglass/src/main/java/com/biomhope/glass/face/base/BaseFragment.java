package com.biomhope.glass.face.base;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.biomhope.glass.face.R;
import com.biomhope.glass.face.home.MainActivity;

import org.greenrobot.eventbus.EventBus;

import butterknife.ButterKnife;

public abstract class BaseFragment extends com.llvision.glass3.framework.core.BaseFragment {

    protected final String TAG = this.getClass().getSimpleName();
    protected final static int msg_glass_pause_recognize = 101;
    protected Context context;

    protected abstract int getLayoutId();

    /**
     * 填充数据
     */
    protected abstract void initialize();

    @TargetApi(23)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onAttachToContext(context);
        Log.i(TAG, "onAttach: API 23以上 ");
    }

    // Deprecated on API 23
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.i(TAG, "onAttach: ");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            onAttachToContext(activity);
        }
    }

    private void onAttachToContext(Context context) {
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutId(), container, false);
        ButterKnife.bind(this, view);
        if (isEventSubscribe())
            EventBus.getDefault().register(this);
        Log.i(TAG, "onCreateView: ");
        return view;
    }

    /**
     * EventBus订阅类需要注册 发布者不用！！
     *
     * @return 默认返回false
     */
    protected abstract boolean isEventSubscribe();

    protected int getCurrentPagePosition(Context context) {
        if (context instanceof MainActivity) {
            return ((MainActivity) context).getCurrentPagePosition();
        }
        return -1;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i(TAG, "onActivityCreated: ");
        initialize();
    }

    protected void showToast(String content) {
        showToast(Toast.LENGTH_SHORT, content);
    }

    protected void showToast(int time, String content) {
        Toast.makeText(context, content, time).show();
    }

    protected void showPauseRecognize(Handler handler) {
        if (handler != null) {
            handler.sendEmptyMessage(msg_glass_pause_recognize);
            Log.i(TAG, "showPauseRecognize: 暂停识别..");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: ");
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.i(TAG, "onHiddenChanged: " + (hidden ? "隐藏" : "显示"));
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "onStart: ");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause: ");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "onStop: ");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i(TAG, "onDestroyView: ");
    }

    @Override
    public void onDestroy() {
        context = null;
        if (isEventSubscribe())
            EventBus.getDefault().unregister(this);
        super.onDestroy();
        Log.i(TAG, "onDestroy: ");
    }

}
