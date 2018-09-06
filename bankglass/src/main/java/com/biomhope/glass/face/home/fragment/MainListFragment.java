package com.biomhope.glass.face.home.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.biomhope.glass.face.R;
import com.biomhope.glass.face.base.BaseFragment;
import com.biomhope.glass.face.bean.FaceDecetListBean;
import com.biomhope.glass.face.bean.SearchPersonsBean;
import com.biomhope.glass.face.bean.SearchPersonsResultTemp;
import com.biomhope.glass.face.global.Constants;
import com.biomhope.glass.face.home.adapter.CropFaceAdapter;
import com.biomhope.glass.face.home.master.FaceSimListActivity;
import com.biomhope.glass.face.home.master.VIPListActivity;
import com.biomhope.glass.face.utils.Base64;
import com.biomhope.glass.face.utils.CommonUtil;
import com.biomhope.glass.face.utils.DatabaseManager;
import com.biomhope.glass.face.utils.LLCameraUtil;
import com.biomhope.glass.face.utils.LocalFaceUtils;
import com.biomhope.glass.face.utils.OkHttpUtil;
import com.google.gson.Gson;
import com.llvision.face.dtr.glxss.FaceDtrClient;
import com.llvision.face.dtr.glxss.IFaceDtrClient;
import com.llvision.face.dtr.glxss.security.listener.IOnFaceDetectListener;
import com.llvision.face.dtr.model.IndicatorColor;
import com.llvision.face.dtr.model.RecognizeResult;
import com.llvision.glass3.framework.LLVisionGlass3SDK;
import com.llvision.glass3.framework.camera.CameraStatusListener;
import com.llvision.glass3.framework.camera.ICameraClient;
import com.llvision.glass3.framework.lcd.ILCDClient;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;
/**
 * author:BH
 * create at:2018/9/5
 * description:
 *
 */
public class MainListFragment extends BaseFragment {

    @BindView(R.id.tv_center_title)
    TextView tv_center_title;

    @BindView(R.id.ib_action_end)
    ImageButton ib_action_end;

    private final String[] names = {"张杰", "刘闯", "马冬冬", "James", "郭晨",
            "赵鹏程", "路易", "周三", "秦思", "昊仁", "李达", "钱数", "孙思",
            "李佳佳", "刘浩", "赵蕈", "陈丽", "陈思远", "伍元柒", "伍全", "刘昊", "张媛", "陈萍",
            "涵亮", "天佑", "逸豪", "玺东", "祥民", "楚铭", "豪伦", "伏冰", "泓杰", "德元", "增冰", "冰刑", "乐康", "贤铭"
    };
    private static final int MSG_DELAY_OPEN_LCD = 1;
    private static final int MSG_DELAY_START_DETEC = 2;
    private static final int MSG_NO_DELAY_INSERT_FACE = 3;
    private static final int MSG_GLASS_SHOW_TIPS = 4;
    private static final int MSG_ClEAR_GLASS_SHOW_TIPS = 5;

    private View mGlxssView;
    private TextView tv_receive_msg;
    private int mIdentifyCount = 0;
    private IFaceDtrClient mFaceDtrClient;
    private ILCDClient mLCDClient;
    private ICameraClient mCameraClient;
    private List<FaceDecetListBean> faceDecetListBeans;
    private CropFaceAdapter cropFaceAdapter;

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
                case MSG_DELAY_OPEN_LCD:
                    openLCD();
                    Log.i(TAG, "handleMessage: 已经开启LCD 0.5s后开启人脸检测..");
                    mHandler.sendEmptyMessageDelayed(MSG_DELAY_START_DETEC, 500);
                    break;
                case MSG_DELAY_START_DETEC:
                    startDetectFace();
                    break;
                case MSG_NO_DELAY_INSERT_FACE:
                    if (face_vs_crop.getDisplayedChild() == 0) {
                        face_vs_crop.showNext();
                    }
                    String imgCreateTime = (String) msg.obj;
                    int sim = new Random().nextInt(99);
                    int nameP = new Random().nextInt(names.length);
                    FaceDecetListBean faceDecetListBean = new FaceDecetListBean(
                            Constants.FACE_DECECTION_PIC_CROP_FILE + File.separator + imgCreateTime + "_crop.png",
                            names[nameP],
                            imgCreateTime,
                            String.valueOf(sim),
                            (sim >= 50 ? "检测成功" : "未知的"));
                    if (nameP > 18 && nameP < names.length) {
                        faceDecetListBean.userType = "vip";
                    }
                    cropFaceAdapter.insertHeaderData(faceDecetListBean);
                    faceRecyclerView.scrollToPosition(0);
                    break;
                case MSG_GLASS_SHOW_TIPS:
                    tv_receive_msg.setText((String) msg.obj);
                    break;
                case msg_glass_pause_recognize:
                    tv_receive_msg.setText(getResources().getString(R.string.glass_tips_pause_recognize));
                    break;
                case MSG_ClEAR_GLASS_SHOW_TIPS:
                    tv_receive_msg.setText("");
                    break;
            }
        }
    };

    private CameraStatusListener mCameraStatusListener = new CameraStatusListener() {
        @Override
        public void onCameraConnected() {
            Log.i(TAG, "onCameraConnected: Camera连接上 1.5s后开启LCD...");
            if (mHandler != null)
                mHandler.sendEmptyMessageDelayed(MSG_DELAY_OPEN_LCD, 1500);
        }

        @Override
        public void onCameraDisconnected() {
            Log.i(TAG, "onCameraDisconnected: Camera断开连接!!");
        }

        @Override
        public void onCameraServiceIsOccupied() {
            showToast("Camera被占用了");
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

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_main_list;
    }

    @SuppressLint("InflateParams")
    @Override
    protected void initialize() {
        tv_center_title.setText(getResources().getString(R.string.title_page_3));
        ib_action_end.setBackgroundResource(R.drawable.tab_vip_selector);
        ib_action_end.setVisibility(View.VISIBLE);

        // temp function
        initFaceData();
        if (faceDecetListBeans.size() > 0) {
            face_vs_crop.showNext();
        }
        cropFaceAdapter = new CropFaceAdapter(context, faceDecetListBeans, new CropFaceAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(FaceDecetListBean faceDecetListBean) {
                Intent simIntent = new Intent(context, FaceSimListActivity.class);
                simIntent.putExtra("userType", faceDecetListBean.userType);
                simIntent.putExtra("name", faceDecetListBean.name);
                simIntent.putExtra("imgCropPath", faceDecetListBean.imgCropPath);
                startActivity(simIntent);
                getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                // 在不可见时关闭眼镜功能
            }
        });
        faceRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        faceRecyclerView.setItemAnimator(new DefaultItemAnimator());
        faceRecyclerView.setAdapter(cropFaceAdapter);

        // 初始化眼镜界面
        mGlxssView = LayoutInflater.from(context).inflate(R.layout.layout_lcd_screen, null);
        tv_receive_msg = mGlxssView.findViewById(R.id.tv_receive_msg);

        new Thread() {
            @Override
            public void run() {
                //1、如果需要在本地识别，那么需要将人脸的feature加载到数据库
                //做一次就行了，不需要多次同步人脸feature
                //2、如果只做DT 或者D（多脸检测）那么不需要同步feature
                DatabaseManager.init(context, R.raw.db_sqlite3);
                LocalFaceUtils.getInstance().syncFeatures();
            }
        }.start();
    }

    // temp方法 检测本地相册
    private void initFaceData() {
        if (faceDecetListBeans == null) {
            faceDecetListBeans = new ArrayList<>();
        }
        // <<<<<<<<<<<<<<6.0读取文件权限问题
        File file = new File(Constants.FACE_DECECTION_PIC_CROP_FILE);
        if (file.exists()) {
            String[] list = file.list();
            if (list != null && list.length > 0) {
                for (int i = 0; i < list.length; i++) {
                    int sim = new Random().nextInt(99);
                    FaceDecetListBean bean = new FaceDecetListBean(
                            Constants.FACE_DECECTION_PIC_CROP_FILE + File.separator + list[i],
                            names[i],
                            CommonUtil.getTimeStr(list[i]),
                            String.valueOf(sim),
                            (sim >= 50 ? "检测成功" : "未知的"));
                    if (sim > 35 && sim < 60) {
                        bean.userType = "vip";
                    }
                    faceDecetListBeans.add(bean);
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // 首次进入或者从其他activity返回时调用
        // 且当前tab position是该界面才开启
        if (getCurrentPagePosition(context) == 2)
            openCamera();
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
        // 点击其他tab
        if (hidden) {
            hiddenForCloseCamera();
        } else {
            openCamera();
        }
    }

    @Override
    public void onDestroy() {
        release();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        super.onDestroy();
    }

    private void openCamera() {
        Log.i(TAG, "openCamera: open camera");
        try {
            if (!LLVisionGlass3SDK.getInstance().isDeviceConnected()) {
                Log.i(TAG, "openCamera: open camera failed ,glass not connected!!");
                return;
            }
            mCameraClient = LLVisionGlass3SDK.getInstance().getCameraClient();
            if (mCameraClient == null) {
                return;
            }
            mCameraClient.open(mCameraStatusListener);
            mCameraClient.setPreviewSize(1280, 720);
            mCameraClient.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openLCD() {
        if (!LLVisionGlass3SDK.getInstance().isConnected()) {
            showToast("服务未绑定，请重启应用同时重新插拔USB");
            return;
        }
        try {
            mLCDClient = LLVisionGlass3SDK.getInstance().getLcdClient();
            if (mLCDClient == null) {
                return;
            }
            if (mGlxssView.getParent() != null) {
                ((ViewGroup) mGlxssView.getParent()).removeView(mGlxssView);
            }
            mLCDClient.createCaptureScreen(context, mGlxssView);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void hiddenForCloseCamera() {
        try {
            showPauseRecognize(mHandler);
            if (mHandler != null)
                mHandler.removeCallbacksAndMessages(null);
            if (mFaceDtrClient != null) {
                mFaceDtrClient.stopDetect();
            }
            if (mCameraClient != null) {
                mCameraClient.disconnect();
            }
            if (mLCDClient != null) {
                mLCDClient.stopCaptureScreen();
            }
        } catch (Exception e) {
            Log.e(TAG, "hiddenForCloseCamera: failed " + e.getMessage());
        }
    }

    // fragment跳转被hidden
    private void release() {
        if (mHandler != null)
            mHandler.removeCallbacksAndMessages(null);
        // 需要按照顺序关闭
        // 1.关闭人脸
        // 2.关闭Camera和LCD
        if (mFaceDtrClient != null) {
            mFaceDtrClient.stopDetect();
            // 完全退出才销毁
            mFaceDtrClient.destroyFaceDtr();
        }
        try {
            if (mCameraClient != null) {
                mCameraClient.disconnect();
            }
            if (mLCDClient != null) {
                mLCDClient.stopCaptureScreen();
            }
        } catch (Exception e) {
            Log.e(TAG, "release: failed " + e.getMessage());
        }
    }

    private IOnFaceDetectListener mIOnFaceDetectListener = new IOnFaceDetectListener() {

        @Override
        public void onRecognize(RecognizeResult recognizeResult) {
            mIdentifyCount++;
            // 发送状态参考  FaceTrackPhase
            mFaceDtrClient.setTrackPhase(mIdentifyCount % 3 == 0 ? IndicatorColor.PHASE_TRACK_IDENTIFIED : IndicatorColor.PHASE_TRACK_UNIDENTIFIED);

            String currentTimeMillis = CommonUtil.formatMills(System.currentTimeMillis());
            String cropImgPath = LLCameraUtil.getCropImgPath(currentTimeMillis);
            boolean imwrite = LLCameraUtil.handleRecognizeResult(TAG, LLCameraUtil.getSourceImgPath(currentTimeMillis), cropImgPath, recognizeResult);
            // 输出成功进行网络请求检测人脸
            if (imwrite) {
                Message msg = Message.obtain();
                msg.what = MSG_NO_DELAY_INSERT_FACE;
                msg.obj = currentTimeMillis;
                mHandler.sendMessage(msg);
                requestSearchPerson(CommonUtil.readLocalFile(cropImgPath));
            }
        }

        // 返回比较相似的前几条数据
        @Override
        public int getTopKFaceNum() {
            // 识别后，比对时返回的人脸数目
            return 1;
        }
    };

    private void requestSearchPerson(byte[] base64Img1) {
        String requestUrl = Constants.REQUEST_HOST_MAIN_URL + Constants.REQUEST_DO_SEARCHPERSONS;
        Log.i(TAG, "requestSearchPerson: requestUrl = " + requestUrl);

        SearchPersonsBean searchPersonsBean = new SearchPersonsBean(Base64.encodeToString(base64Img1, Base64.NO_WRAP), Constants.CHANNEL);

        OkHttpUtil.getInstance().doAsyncOkHttpPost(requestUrl, new Gson().toJson(searchPersonsBean), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                int code = response.code();
                Log.i(TAG, "onResponse: code = " + code);
                Message msg = Message.obtain();
                msg.what = MSG_GLASS_SHOW_TIPS;
                String result = getResources().getString(R.string.net_status_error);
                if (code == Constants.REQUEST_STATUS_SUCCESSFUL) {
                    ResponseBody responseBody = response.body();
                    if (responseBody != null) {
                        String content = responseBody.string();
                        Log.i(TAG, "onResponse: content = " + content);
                        SearchPersonsResultTemp resultTemp = new Gson().fromJson(content, SearchPersonsResultTemp.class);
                        List<SearchPersonsResultTemp.UserInfo> userInfos;
                        if (resultTemp.result == 1) {
                            result = "不在库中";
                            userInfos = resultTemp.userInfos;
                            if (userInfos != null && userInfos.size() > 0) {
                                if (userInfos.get(0).sim > resultTemp.defaultSim) {
                                    result = "本人";
                                }
                            }
                        } else {
                            Log.e(TAG, "onResponse: error exCode = " + resultTemp.exCode + " exMsg is " + resultTemp.exMsg);
                        }
                    }
                }
                msg.obj = result;
                mHandler.sendMessage(msg);
                mHandler.sendEmptyMessageDelayed(MSG_ClEAR_GLASS_SHOW_TIPS, 5000);
            }
        });
    }

    private void startDetectFace() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!LLVisionGlass3SDK.getInstance().isConnected()) {
                        return;
                    }
                    mFaceDtrClient = FaceDtrClient.getInstance(LLVisionGlass3SDK.getInstance().getHostAIDL());
                    mFaceDtrClient.loadFaceDetectionModel(context);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mFaceDtrClient.setOnFaceDetectListener(mIOnFaceDetectListener);

                if (mFaceDtrClient != null) {
                    mFaceDtrClient.startDetect(context, LLCameraUtil.buildFaceParamter());
                }
            }
        }).start();
    }

    @Override
    protected boolean isEventSubscribe() {
        return false;
    }

}
