package com.biomhope.glass.face.home.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.biomhope.glass.face.R;
import com.biomhope.glass.face.base.BaseFragment;
import com.biomhope.glass.face.bean.CompareFaceBean;
import com.biomhope.glass.face.bean.CompareFaceResult;
import com.biomhope.glass.face.global.Constants;
import com.biomhope.glass.face.utils.Base64;
import com.biomhope.glass.face.utils.CommonUtil;
import com.biomhope.glass.face.utils.DatabaseManager;
import com.biomhope.glass.face.utils.LLCameraUtil;
import com.biomhope.glass.face.utils.LocalFaceUtils;
import com.biomhope.glass.face.utils.OkHttpUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.kernal.passport.sdk.utils.Devcode;
import com.kernal.passport.sdk.utils.SharedPreferencesHelper;
import com.kernal.passportreader.sdk.CameraActivity;
import com.kernal.passportreader.sdk.RecogResultBean;
import com.llvision.face.dtr.glxss.FaceDtrClient;
import com.llvision.face.dtr.glxss.IFaceDtrClient;
import com.llvision.face.dtr.glxss.security.listener.IOnFaceDetectListener;
import com.llvision.face.dtr.model.IndicatorColor;
import com.llvision.face.dtr.model.RecognizeResult;
import com.llvision.glass3.framework.LLVisionGlass3SDK;
import com.llvision.glass3.framework.camera.CameraStatusListener;
import com.llvision.glass3.framework.camera.ICameraClient;
import com.llvision.glass3.framework.lcd.ILCDClient;

import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;

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
 */
public class OcrFunctionFragment extends BaseFragment {

    @BindView(R.id.tv_center_title)
    TextView tv_center_title;

    @BindView(R.id.iv_show_ocr_result)
    ImageView iv_show_ocr_result;

    @BindView(R.id.iv_show_glass_result)
    ImageView iv_show_glass_result;

    @BindView(R.id.tv_is_self)
    TextView tv_is_self;

    @BindView(R.id.tv_approve_sim_pct)
    TextView tv_approve_sim_pct;

    @BindView(R.id.tv_ocr_recog_name)
    TextView tv_ocr_recog_name;

    @BindView(R.id.tv_ocr_recog_sex)
    TextView tv_ocr_recog_sex;

    @BindView(R.id.tv_ocr_recog_birth_date)
    TextView tv_ocr_recog_birth_date;

    @BindView(R.id.tv_ocr_recog_family_address)
    TextView tv_ocr_recog_family_address;

    @BindView(R.id.tv_ocr_recog_idcard)
    TextView tv_ocr_recog_idcard;

    @OnClick({R.id.iv_show_glass_result, R.id.iv_show_ocr_result})
    void click(View v) {
        if (CommonUtil.isFastDoubleClick()) return;
        switch (v.getId()) {
            case R.id.iv_show_ocr_result:
                requestOcrCamera();
                break;
        }
    }

    private final static int msg_get_ocr_reog_result = 1;
    private static final int msg_delay_open_lcd = 2;
    private static final int msg_delay_start_detec = 3;
    private final static int msg_cancel_dialog = 4;
    private final static int msg_show_result_toast = 5;
    private Dialog dialog;

    private View mGlxssView;
    private TextView tv_receive_msg;
    private int mIdentifyCount = 0;
    private IFaceDtrClient mFaceDtrClient;
    private ILCDClient mLCDClient;
    private ICameraClient mCameraClient;
    private String headJpgPath;
    private boolean isHasOcrResult = false; // OCR成功返回结果才能开启眼镜

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == msg_get_ocr_reog_result) {
                RecogResultBean resultBean = (RecogResultBean) msg.obj;
                if (resultBean != null) {
                    isHasOcrResult = true;
                    String recogResult = resultBean.recogResultString;
                    String exception = resultBean.exception;
                    headJpgPath = resultBean.HeadJpgPath;
                    Log.i(TAG, "handleMessage: recogResult " + recogResult);
                    if (exception != null && !exception.equals("")) {
                        // 错误异常
                        Log.i(TAG, "handleMessage: ocr reognize exception " + exception);
                    } else {
                        StringBuilder result = new StringBuilder();
                        String[] splite_Result = recogResult.split(",");
                        // 切割顺序 姓名 性别 民族 出生 住址 公民身份号码
                        for (String aSplite_Result : splite_Result) {
                            if (result.toString().equals("")) {
                                result = new StringBuilder(aSplite_Result + "\n");
                            } else {
                                result.append(aSplite_Result).append("\n");
                            }
                        }
                        for (int i = 0; i < splite_Result.length; i++) {
                            String aSplite_Result = splite_Result[i].substring(splite_Result[i].indexOf(":") + 1);
                            if (i == 0) {
                                tv_ocr_recog_name.setText(aSplite_Result);
                            } else if (i == 1) {
                                tv_ocr_recog_sex.setText(aSplite_Result);
                            } else if (i == 3) {
                                tv_ocr_recog_birth_date.setText(aSplite_Result);
                            } else if (i == 4) {
                                tv_ocr_recog_family_address.setText(aSplite_Result);
                            } else if (i == 5) {
                                tv_ocr_recog_idcard.setText(aSplite_Result);
                            }
                        }

                    }
                    Glide.with(context)
                            .load("file://" + headJpgPath)
                            .transition(new DrawableTransitionOptions().crossFade(500))
                            .apply(new RequestOptions().placeholder(R.drawable.pictures_no).error(R.drawable.remote_refresh))
                            .into(iv_show_ocr_result);
                    // OCR成功才开启眼镜
                    if (!isHasOcrResult) return;
                    initGlassConfiguration();
                    showToast("等待眼镜识别..");
                    openCamera();
                } else {
                    isHasOcrResult = false;
                    showToast("请重新进行证件识别!");
                }
            } else if (msg.what == msg_delay_open_lcd) {
                openLCD();
                Log.i(TAG, "handleMessage: 已经开启LCD 0.5s后开启人脸检测..");
                mHandler.sendEmptyMessageDelayed(msg_delay_start_detec, 500);
            } else if (msg.what == msg_delay_start_detec) {
                startDetectFace();
            } else if (msg.what == msg_cancel_dialog) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
            } else if (msg.what == msg_show_result_toast) {
                String result = (String) msg.obj;
                tv_receive_msg.setText(result);
                // 停止检测
                if (mFaceDtrClient != null && mFaceDtrClient.isRunning()) {
                    mFaceDtrClient.stopDetect();
                }
                // hiddenForCloseCamera();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tv_receive_msg.setText("");
                    }
                }, 3000);
            }
        }
    };

    private IOnFaceDetectListener mIOnFaceDetectListener = new IOnFaceDetectListener() {

        @Override
        public void onRecognize(RecognizeResult recognizeResult) {
            mIdentifyCount++;
            // 发送状态参考  FaceTrackPhase
            mFaceDtrClient.setTrackPhase(mIdentifyCount % 3 == 0 ? IndicatorColor.PHASE_TRACK_IDENTIFIED : IndicatorColor.PHASE_TRACK_UNIDENTIFIED);
            String currentTimeMillis = CommonUtil.formatMills(System.currentTimeMillis());
            String cropImgPath = LLCameraUtil.getCropImgPath(currentTimeMillis);
            boolean imwrite = LLCameraUtil.handleRecognizeResult(TAG, LLCameraUtil.getSourceImgPath(currentTimeMillis), cropImgPath, recognizeResult);

            if (imwrite) {
                compareFace(headJpgPath, cropImgPath);
            }
        }

        // 返回比较相似的前几条数据
        @Override
        public int getTopKFaceNum() {
            // 识别后，比对时返回的人脸数目
            return 1;
        }
    };

    private CameraStatusListener mCameraStatusListener = new CameraStatusListener() {
        @Override
        public void onCameraConnected() {
            Log.i(TAG, "onCameraConnected: Camera连接上 1.5s后开启LCD...");
            if (mHandler != null)
                mHandler.sendEmptyMessageDelayed(msg_delay_open_lcd, 1500);
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

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_show_ocr_result;
    }

    @Override
    protected void initialize() {
        tv_center_title.setText(context.getResources().getString(R.string.tab_ocr_function));
    }

    @SuppressLint("InflateParams")
    private void initGlassConfiguration() { // 初始化眼镜界面
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

    private void hiddenForCloseCamera() {
        try {
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

    private void compareFace(final String headJpgPath, final String keepCropImgPath) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (dialog == null) {
                    dialog = new Dialog(context, R.style.LoadDialogStyle);
                    dialog.setContentView(R.layout.dialog_common_loading);
                    dialog.setCancelable(false);
                }
                dialog.show();

                Glide.with(context)
                        .load("file://" + keepCropImgPath)
                        .transition(new DrawableTransitionOptions().crossFade(500))
                        .apply(new RequestOptions().error(R.drawable.remote_refresh))
                        .into(iv_show_glass_result);
            }
        });
        // 进行比对请求
        CompareFaceBean compareFaceBean = new CompareFaceBean();
        compareFaceBean.img1 = Base64.encodeToString(CommonUtil.readLocalFile(headJpgPath), Base64.NO_WRAP);
        compareFaceBean.img2 = Base64.encodeToString(CommonUtil.readLocalFile(keepCropImgPath), Base64.NO_WRAP);
        OkHttpUtil.getInstance().doAsyncOkHttpPost(
                Constants.REQUEST_HOST_MAIN_URL + Constants.REQUEST_DO_COMPAREFACES,
                new Gson().toJson(compareFaceBean), new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        mHandler.sendEmptyMessage(msg_cancel_dialog);
                        Log.e(TAG, "onFailure: " + e.getMessage());

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        mHandler.sendEmptyMessage(msg_cancel_dialog);
                        Log.i(TAG, "onResponse: code = " + response.code());
                        String result = getResources().getString(R.string.net_status_error);
                        Message msg = Message.obtain();
                        msg.what = msg_show_result_toast;
                        if (response.code() == Constants.REQUEST_STATUS_SUCCESSFUL) {
                            ResponseBody responseBody = response.body();
                            if (responseBody != null) {
                                String content = responseBody.string();
                                Log.i(TAG, "onResponse: content = " + content);
                                CompareFaceResult compareFaceResult = new Gson().fromJson(content, CompareFaceResult.class);
                                result = "不是本人!";
                                if (compareFaceResult.result == 1 && (compareFaceResult.sim > compareFaceResult.defaultSim)) {
                                    result = "确认是本人！";
                                }
                            }
                        }
                        msg.obj = result;
                        mHandler.sendMessage(msg);
                    }
                });
    }

    @Override
    public void onPause() {
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
            if (getCurrentPagePosition(context) == 1 && isHasOcrResult)
                openCamera();
        }
    }

    @Override
    public void onDestroy() {
        release();
        if (mHandler != null)
            mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private void requestOcrCamera() {
        Intent intent = new Intent(context, CameraActivity.class);
        intent.putExtra("nMainId", SharedPreferencesHelper.getInt(
                context.getApplicationContext(), "nMainId", 2));
        intent.putExtra("devcode", Devcode.devcode);
        intent.putExtra("flag", 0);
        startActivity(intent);
    }

    @Subscribe
    public void getRecogResult(RecogResultBean resultBean) {
        Message msg = Message.obtain();
        msg.what = msg_get_ocr_reog_result;
        msg.obj = resultBean;
        mHandler.sendMessage(msg);
    }


    @Override
    protected boolean isEventSubscribe() {
        return true;
    }
}
