package com.biomhope.glass.face.home.master;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.biomhope.glass.face.R;
import com.biomhope.glass.face.global.BaseFragment;
import com.biomhope.glass.face.bean.FaceRecogHistoryResult;
import com.biomhope.glass.face.bean.FaceRecogListVO;
import com.biomhope.glass.face.bean.FaceRecogRequestBean;
import com.biomhope.glass.face.bean.FaceRecogResult;
import com.biomhope.glass.face.bean.GroupUserInfo;
import com.biomhope.glass.face.bean.eventvo.GlassMessage;
import com.biomhope.glass.face.bean.eventvo.GlassTipsMessage;
import com.biomhope.glass.face.global.Constants;
import com.biomhope.glass.face.utils.Base64;
import com.biomhope.glass.face.utils.CommonUtil;
import com.biomhope.glass.face.utils.LogUtil;
import com.biomhope.glass.face.utils.OkHttpUtil;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * author:BH
 * create at:2018/9/5
 * description: 识别列表
 */
public class MainListFragment extends BaseFragment {

    @BindView(R.id.tv_center_title)
    TextView tv_center_title;

    @BindView(R.id.ib_action_end)
    ImageButton ib_action_end;

    private static final int MSG_NO_DELAY_INSERT_FACE = 3;
    private static final int MSG_GLASS_CANCEL_LOADING_DIALOG = 4;
    private static final int MSG_ClEAR_GLASS_SHOW_TIPS = 5;
    private static final int MSG_GET_FACE_RECOG_HISTORY = 6;

    private List<FaceRecogListVO> userInfos = new ArrayList<>();
    private FaceDetectionAdapter faceDetectionAdapter;
    private String userId;

    @BindView(R.id.face_swipe_refresh)
    SwipeRefreshLayout face_swipe_refresh;

    @BindView(R.id.face_vs_crop)
    ViewSwitcher face_vs_crop;

    @BindView(R.id.tv_no_pic_tips)
    TextView tv_no_pic_tips;

    @BindView(R.id.face_decet_list)
    RecyclerView faceRecyclerView;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_NO_DELAY_INSERT_FACE:
                    if (face_vs_crop.getDisplayedChild() == 0) {
                        face_vs_crop.showNext();
                    }
                    FaceRecogListVO userInfoResult = (FaceRecogListVO) msg.obj;
                    faceDetectionAdapter.insertHeaderData(userInfoResult);
                    faceRecyclerView.scrollToPosition(0);
                    break;
                case MSG_GLASS_CANCEL_LOADING_DIALOG:
                    cancelDialog();
                    break;
                case msg_glass_pause_recognize:
                    setGlassTipsText(new GlassTipsMessage(getResources().getString(R.string.glass_tips_pause_recognize), ""));
                    break;
                case MSG_ClEAR_GLASS_SHOW_TIPS:
                    setGlassTipsText(null);
                    break;
                case MSG_GET_FACE_RECOG_HISTORY:
                    List<FaceRecogListVO> userInfoHistory = (List<FaceRecogListVO>) msg.obj;
                    if (userInfoHistory != null && !userInfoHistory.isEmpty()) {
                        for (FaceRecogListVO user : userInfoHistory) {
                            user.isServerPic = true;
                        }
                        userInfos.clear();
                        userInfos.addAll(userInfoHistory);
                        if (userInfos.size() > 0) {
                            face_vs_crop.showNext();
                        }
                        faceDetectionAdapter.notifyData(userInfos);
                    }
                    break;
            }
        }
    };

    @OnClick(R.id.ib_action_end)
    void click(View v) {
        if (CommonUtil.isFastDoubleClick()) return;
        switch (v.getId()) {
            case R.id.ib_action_end:
                CommonUtil.skipAnotherActivity(getActivity(), VIPListActivity.class);
                break;
        }
    }

    @Subscribe
    public void getGlassCropImg(final GlassMessage message) {
        if (message != null && message.position == 0) {
            LogUtil.i(TAG, TAG + " Subscribe接收到眼镜识别信息: " + message.toString());
            requestFaceRecogintion(message.imgCropPath);
        }
    }

    @Override
    protected boolean isEventSubscribe() {
        return true;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_main_list;
    }

    @SuppressLint("InflateParams")
    @Override
    protected void initialize() {

        userId = getArguments().getString("userId", "");
        LogUtil.i(TAG, "当前登录返回userId是: " + userId);
        tv_center_title.setText(getResources().getString(R.string.title_page_3));
        ib_action_end.setBackgroundResource(R.drawable.action_vip_n);
        ib_action_end.setVisibility(View.VISIBLE);

        showLoadingDialog();
        requestFaceHistory();

        faceDetectionAdapter = new FaceDetectionAdapter(context, userInfos, new FaceDetectionAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(FaceRecogListVO userInfo) {
                Intent simIntent = new Intent(context, FaceSimilarityListActivity.class);
                simIntent.putExtra("userId", userId);
                simIntent.putExtra("userInfo", userInfo);
                startActivity(simIntent);
                getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                // 在不可见时关闭眼镜功能
                // onPause处理关闭眼镜
            }
        });
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        faceRecyclerView.setLayoutManager(linearLayoutManager);
        faceRecyclerView.setItemAnimator(new DefaultItemAnimator());
        faceRecyclerView.setAdapter(faceDetectionAdapter);

        face_swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                face_swipe_refresh.setRefreshing(false);
            }
        });
        faceRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {

            private int lastVisibleItemPosition;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (face_swipe_refresh.isRefreshing()) return;
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItemPosition == faceDetectionAdapter.getItemCount() - 1) {
                    LogUtil.i(TAG, "RecyclerView滑动到底部了");
                } else {
                    LogUtil.e(TAG, "RecyclerView尚未滑动到底部");
                }

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        // 首次进入
        // 或者从其他activity返回时调用
        if (getCurrentPagePosition(context) == 0) {
            LogUtil.i(TAG, "MainListFragment onResume open camera.");
            openLLCamera();
        }
    }

    @Override
    public void onPause() {
        // 跳转vip activity
        super.onPause();
        hiddenForCloseCamera();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        // 切换其他tab时触发
        // 开启关闭camera由MainActivity切换item时控制
    }

    @Override
    public void onDestroy() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
//        LLCameraUtil.getInstance(context).release(true);
        super.onDestroy();
    }

    private void hiddenForCloseCamera() {
        try {
            showPauseRecognize(mHandler);
            if (mHandler != null) {
                mHandler.removeCallbacksAndMessages(null);
            }
            LogUtil.i(TAG, "main onPause close camera.");
            if (getCurrentPagePosition(context) == 0) {
                closeLLCamera();
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "hiddenForCloseCamera: failed " + e.getMessage());
        }
    }

    private void requestFaceHistory() {
        OkHttpUtil.getInstance().doAsyncOkHttpPost(
                Constants.REQUEST_HOST_MAIN_URL + Constants.REQUEST_DO_LISTRECOGNITIONHISTORY,
                new Gson().toJson(new FaceRecogRequestBean(userId)),
                new OkHttpUtil.CallBack() {
                    @Override
                    public void onFailure() {
                        mHandler.sendEmptyMessage(MSG_GLASS_CANCEL_LOADING_DIALOG);
                    }

                    @Override
                    public void onResponseSuccess(String json) {
                        mHandler.sendEmptyMessage(MSG_GLASS_CANCEL_LOADING_DIALOG);
                        FaceRecogHistoryResult faceRecogHistoryResult = new Gson().fromJson(json, FaceRecogHistoryResult.class);
                        if (Constants.RESPONSE_STATUS_SUCCESSFUL.equals(faceRecogHistoryResult.exCode)) {
                            Message msg = Message.obtain();
                            msg.obj = faceRecogHistoryResult.result;
                            msg.what = MSG_GET_FACE_RECOG_HISTORY;
                            mHandler.sendMessage(msg);
                        } else {
                            LogUtil.e(TAG, "获取UserId[" + userId + "]历史识别记录失败: " + faceRecogHistoryResult.exMsg);
                        }
                    }

                    @Override
                    public void onResponseFailed() {
                        mHandler.sendEmptyMessage(MSG_GLASS_CANCEL_LOADING_DIALOG);
                    }
                });
    }

    private void insertItemData(FaceRecogListVO userInfo, String imgCropPath) {
        // 更新人脸列表
        if (userInfo == null) {
            userInfo = new FaceRecogListVO();
            userInfo.clientName = getResources().getString(R.string.user_name_not_match);
            userInfo.idCard = getResources().getString(R.string.user_idcard_not_match);
        }
        userInfo.imgCropPath = imgCropPath;
        userInfo.isServerPic = false;
        Message msg = Message.obtain();
        msg.what = MSG_NO_DELAY_INSERT_FACE;
        msg.obj = userInfo;
        mHandler.sendMessage(msg);
    }

    private void updateGlassTipsInfo(GlassTipsMessage glassMsg) {
        // 更新眼镜提示信息
        mHandler.removeMessages(MSG_ClEAR_GLASS_SHOW_TIPS);
        // 发送给 LLCameraUtil getGlassTipsText 更新眼镜提示信息
        EventBus.getDefault().post(glassMsg);

        mHandler.sendEmptyMessageDelayed(MSG_ClEAR_GLASS_SHOW_TIPS, 5000);
    }

    private void requestFaceRecogintion(final String imgCropPath) {
        OkHttpUtil.getInstance().doAsyncOkHttpPost(Constants.REQUEST_HOST_MAIN_URL + Constants.REQUEST_DO_FACERECOGNIZES,
                new Gson().toJson(new FaceRecogRequestBean(userId, Base64.encodeToString(CommonUtil.readLocalFile(imgCropPath), Base64.NO_WRAP))),
                new OkHttpUtil.CallBack() {
                    @Override
                    public void onFailure() {
                        showConnectHttpError();
                    }

                    @Override
                    public void onResponseSuccess(String json) {
                        FaceRecogResult faceRecogResult = new Gson().fromJson(json, FaceRecogResult.class);
                        if (Constants.RESPONSE_STATUS_SUCCESSFUL.equals(faceRecogResult.exCode)) {
                            FaceRecogListVO userInfo = faceRecogResult.result;
                            if (userInfo != null) {
                                // 有相似度返回
                                // 眼镜显示名字以及星级
                                insertItemData(userInfo, imgCropPath);
                                if (GroupUserInfo.USER_TYPE_VIP.equals(userInfo.family)) {
                                    updateGlassTipsInfo(new GlassTipsMessage(userInfo.clientName, userInfo.vipLevel));
                                } else if (GroupUserInfo.USER_TYPE_BLACK.equals(userInfo.family)) {
                                    updateGlassTipsInfo(new GlassTipsMessage(String.format("命中名单,请关注%s", userInfo.clientName), ""));
                                } else {
                                    updateGlassTipsInfo(new GlassTipsMessage(userInfo.clientName, ""));
                                }
                            } else {
                                insertItemData(null, imgCropPath);
                                updateGlassTipsInfo(new GlassTipsMessage(getResources().getString(R.string.user_idcard_not_match), ""));
                            }
                        } else {
                            // 请求成功 但数据错误 exCode != 0
                            insertItemData(null, imgCropPath);
                            updateGlassTipsInfo(new GlassTipsMessage(getResources().getString(R.string.user_idcard_not_match), ""));
                        }
                    }

                    @Override
                    public void onResponseFailed() {
                        insertItemData(null, imgCropPath);
                        updateGlassTipsInfo(new GlassTipsMessage(getResources().getString(R.string.net_status_error), ""));
                    }
                });

    }


}