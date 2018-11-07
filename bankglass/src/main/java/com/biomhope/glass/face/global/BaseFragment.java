package com.biomhope.glass.face.global;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.biomhope.glass.face.R;
import com.biomhope.glass.face.bean.eventvo.GlassTipsMessage;
import com.biomhope.glass.face.home.MainActivity;
import com.biomhope.glass.face.utils.LLCameraUtil;
import com.biomhope.glass.face.utils.LogUtil;

import org.greenrobot.eventbus.EventBus;

import butterknife.ButterKnife;

/**
 * author:BH
 * create at:2018/9/6
 * description:
 */
public abstract class BaseFragment extends com.llvision.glass3.framework.core.BaseFragment {

    protected final String TAG = this.getClass().getSimpleName();
    protected final static int msg_glass_pause_recognize = 101;
    protected Context context;
    protected Dialog mDialog;

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
        LogUtil.i(TAG, "onAttach: API 23以上 ");
    }

    // Deprecated on API 23
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        LogUtil.i(TAG, "onAttach: ");
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
        LogUtil.i(TAG, "onCreateView: ");
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
        LogUtil.i(TAG, "onActivityCreated: ");
        initialize();
    }

    protected void showToast(String content) {
        showToast(Toast.LENGTH_SHORT, content);
    }

    protected void showToast(int time, String content) {
        Toast.makeText(context, content, time).show();
    }

    protected void showConnectHttpError() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showToast(getResources().getString(R.string.http_request_connect_failed));
            }
        }, 0);
    }

    protected void showResponseHttpError() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showToast(getResources().getString(R.string.http_request_response_failed));
            }
        }, 0);
    }

    // 眼镜端UI显示暂停识别text
    protected void showPauseRecognize(Handler handler) {
        if (handler != null) {
            handler.sendEmptyMessage(msg_glass_pause_recognize);
        }
    }

    protected void setGlassTipsText(GlassTipsMessage glassTipsMessage) {
        if (context != null && context instanceof MainActivity) {
            ((MainActivity) context).setGlassTipsMsg(glassTipsMessage);
        }
    }

    protected void openLLCamera(){
        if (context != null && context instanceof MainActivity) {
            LLCameraUtil.getInstance(context).openCamera();
        }
    }

    protected void closeLLCamera(){
        if (context != null && context instanceof MainActivity) {
            LLCameraUtil.getInstance(context).release(false);
        }
    }

    protected void showLoadingDialog() {
        if (mDialog == null) {
            mDialog = new Dialog(context, R.style.LoadDialogStyle);
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
    public void onResume() {
        super.onResume();
        LogUtil.i(TAG, "onResume: ");
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        LogUtil.i(TAG, "onHiddenChanged: " + (hidden ? "隐藏" : "显示"));
    }

    @Override
    public void onStart() {
        super.onStart();
        LogUtil.i(TAG, "onStart: ");
    }

    @Override
    public void onPause() {
        super.onPause();
        LogUtil.i(TAG, "onPause: ");
    }

    @Override
    public void onStop() {
        super.onStop();
        LogUtil.i(TAG, "onStop: ");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LogUtil.i(TAG, "onDestroyView: ");
    }

    @Override
    public void onDestroy() {
        context = null;
        if (isEventSubscribe())
            EventBus.getDefault().unregister(this);
        super.onDestroy();
        LogUtil.i(TAG, "onDestroy: ");
    }

}
