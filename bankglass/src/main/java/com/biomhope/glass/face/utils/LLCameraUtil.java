package com.biomhope.glass.face.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.AppCompatRatingBar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.biomhope.glass.face.R;
import com.biomhope.glass.face.bean.eventvo.GlassMessage;
import com.biomhope.glass.face.bean.eventvo.GlassTipsMessage;
import com.biomhope.glass.face.global.Constants;
import com.llvision.face.dtr.glxss.FaceDtrClient;
import com.llvision.face.dtr.glxss.IFaceDtrClient;
import com.llvision.face.dtr.glxss.security.listener.IOnFaceDetectListener;
import com.llvision.face.dtr.model.Area;
import com.llvision.face.dtr.model.Colors;
import com.llvision.face.dtr.model.DetectThreshold;
import com.llvision.face.dtr.model.FaceInitParamter;
import com.llvision.face.dtr.model.IndicatorColor;
import com.llvision.face.dtr.model.RecognizeResult;
import com.llvision.glass3.framework.LLVisionGlass3SDK;
import com.llvision.glass3.framework.camera.CameraStatusListener;
import com.llvision.glass3.framework.camera.ICameraClient;
import com.llvision.glass3.framework.lcd.ILCDClient;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;

public class LLCameraUtil {

    private static final String TAG = LLCameraUtil.class.getSimpleName();
    // 检测类型 结果如下
    // DTR 检测追踪 识别
    public static final int SERVICE_FACE_DETECT_TRACK_RECOGNIZE = 1;
    // DT 单脸检测追踪
    public static final int SERVICE_FACE_DETECT_TRACK = 2;
    // MD 多脸
    public static final int SERVICE_DETECT = 4;

    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;

    private static volatile LLCameraUtil instance;
    private ICameraClient mCameraClient;
    private ILCDClient mLCDClient;
    private IFaceDtrClient mFaceDtrClient;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    openLCD();
                    LogUtil.i(TAG, "onCameraConnected:0.5s后开启检测.");
                    mHandler.sendEmptyMessageDelayed(2, 500);
                case 2:
                    startDetectFace(mIOnFaceDetectListener);
                    break;
                case 3:
                    GlassTipsMessage glassTipsMessage = (GlassTipsMessage) msg.obj;
                    setGlassTipsMsg(glassTipsMessage);
                    break;
            }
        }
    };
    private Context mContext;
    private TextView mReceiveMsgView;
    private LinearLayout mTipsAeraView;
    private AppCompatRatingBar mVipLevelRatbar;
    private int mIdentifyCount;
    private int mCurrentPosition;

    private IOnFaceDetectListener mIOnFaceDetectListener = new IOnFaceDetectListener() {

        @Override
        public void onRecognize(RecognizeResult recognizeResult) {
            mIdentifyCount++;
            // 发送状态参考  FaceTrackPhase
            if (mFaceDtrClient != null) {
                mFaceDtrClient.setTrackPhase(mIdentifyCount % 3 == 0 ? IndicatorColor.PHASE_TRACK_IDENTIFIED : IndicatorColor.PHASE_TRACK_UNIDENTIFIED);
            }

            String currentTimeMillis = CommonUtil.formatMills(System.currentTimeMillis());
            String imgCropPath = getCropImgPath(currentTimeMillis);
            boolean imwrite = handleRecognizeResult(TAG, getSourceImgPath(currentTimeMillis), imgCropPath, recognizeResult);
            // 输出成功进行网络请求检测人脸
            if (imwrite) {
                EventBus.getDefault().post(new GlassMessage(mCurrentPosition, imgCropPath));
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
            LogUtil.i(TAG, "onCameraConnected: Camera连接上 0.5s后开启LCD.");
            if (mHandler != null) {
                mHandler.sendEmptyMessageDelayed(1, 500);
            }
        }

        @Override
        public void onCameraDisconnected() {
            LogUtil.i(TAG, "onCameraDisconnected: Camera断开连接.");
        }

        @Override
        public void onCameraServiceIsOccupied() {
            LogUtil.i(TAG, "onCameraServiceIsOccupied: Camera被占用了.");
        }
    };

    private LLCameraUtil(Context mContext) {
        this.mContext = mContext;
        EventBus.getDefault().register(this);
    }

    public static LLCameraUtil getInstance(Context mContext) {
        if (instance == null) {
            synchronized (LLCameraUtil.class) {
                if (instance == null) {
                    instance = new LLCameraUtil(mContext);
                }
            }
        }
        return instance;
    }

    public void loadSyncFeatures() {
        new Thread() {
            @Override
            public void run() {
                //1、如果需要在本地识别，那么需要将人脸的feature加载到数据库
                //做一次就行了，不需要多次同步人脸feature
                //2、如果只做DT 或者D（多脸检测）那么不需要同步feature
                DatabaseManager.init(mContext, R.raw.db_sqlite3);
                LocalFaceUtils.getInstance().syncFeatures();
            }
        }.start();
    }

    public String openCamera() {
        LogUtil.i(TAG, "enter open camera.");
        try {
            if (!LLVisionGlass3SDK.getInstance().isDeviceConnected()) {
                LogUtil.i(TAG, "openCamera: open camera failed ,glass not connected!!");
                return "glass not connected.";
            }
            if (isCameraOpened()) {
                LogUtil.i(TAG, "camera already opened.");
                return "";
            }
            if (mCameraClient == null) {
                mCameraClient = LLVisionGlass3SDK.getInstance().getCameraClient();
            }
            if (mCameraClient == null) {
                return "glass connect failed.";
            }
            mCameraClient.open(mCameraStatusListener);
            mCameraClient.setPreviewSize(WIDTH, HEIGHT);
            mCameraClient.connect();
            LogUtil.i(TAG, "start open camera.");
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e(TAG, e.getMessage());
        }
        return "";
    }

    private boolean isCameraOpened() {
        if (mCameraClient != null)
            return mCameraClient.isCameraOpened();
        return false;
    }

    private String openLCD() {
        LogUtil.i(TAG, "enter open lcd.");
        if (!LLVisionGlass3SDK.getInstance().isConnected()) {
            return "服务未绑定，请重启应用同时重新插拔USB";
        } else {
            try {
                mLCDClient = LLVisionGlass3SDK.getInstance().getLcdClient();
                if (mLCDClient == null || mContext == null) {
                    return "服务未绑定，请重启应用同时重新插拔USB";
                }
                View mGlxssView = LayoutInflater.from(mContext).inflate(R.layout.layout_lcd_screen, null);
                mReceiveMsgView = mGlxssView.findViewById(R.id.tv_receive_msg);
                mTipsAeraView = mGlxssView.findViewById(R.id.tips_aera);
                mVipLevelRatbar = mGlxssView.findViewById(R.id.rat_vip_level);
                if (mGlxssView.getParent() != null) {
                    ((ViewGroup) mGlxssView.getParent()).removeView(mGlxssView);
                }
                LogUtil.i(TAG, "start open lcd.");
                mLCDClient.createCaptureScreen(mContext, mGlxssView);
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.e(TAG, e.getMessage());
            }
            return "";
        }
    }

    @Subscribe
    public void getGlassTipsText(GlassTipsMessage glassTipsMessage) {
        LogUtil.i(TAG, "接收到眼镜LCD需要更新信息: " + glassTipsMessage.toString());
        Message msg = Message.obtain();
        msg.what = 3;
        msg.obj = glassTipsMessage;
        mHandler.sendMessage(msg);
    }

    public void setmCurrentPosition(int mCurrentPosition) {
        this.mCurrentPosition = mCurrentPosition;
        if (mCurrentPosition == 0) {
            openCamera();
        } else {
            release(false);
        }
    }

    public void setGlassTipsMsg(GlassTipsMessage glassTipsMessage) {
        if (mReceiveMsgView != null && glassTipsMessage != null) {
            if (TextUtils.isEmpty(glassTipsMessage.content)) {
                // 姓名为空
                mTipsAeraView.setVisibility(View.GONE);
            } else {
                mTipsAeraView.setVisibility(View.VISIBLE);
                mReceiveMsgView.setText(glassTipsMessage.content);
                mTipsAeraView.setBackgroundResource(R.drawable.bg_glass_tips_stroke);

                if (TextUtils.isEmpty(glassTipsMessage.viplevel)) {
                    mVipLevelRatbar.setVisibility(View.GONE);
                } else {
                    // 是VIP 显示星级
                    mVipLevelRatbar.setVisibility(View.VISIBLE);
                    mVipLevelRatbar.setRating(Float.valueOf(glassTipsMessage.viplevel));
                }
            }
        } else {
            if (mTipsAeraView != null) {
                mTipsAeraView.setVisibility(View.GONE);
            }
        }
    }

    // 需要在子线程中启用
    private void startDetectFace(IOnFaceDetectListener mIOnFaceDetectListener) {
        LogUtil.i(TAG, "enter detect face.");
        try {
            if (!LLVisionGlass3SDK.getInstance().isConnected()) {
                return;
            }
            mFaceDtrClient = FaceDtrClient.getInstance(LLVisionGlass3SDK.getInstance().getHostAIDL());
            if (mFaceDtrClient != null && mContext != null) {
                mFaceDtrClient.loadFaceDetectionModel(mContext);
                mFaceDtrClient.setOnFaceDetectListener(mIOnFaceDetectListener);
                LogUtil.i(TAG, "start detect face.");
                mFaceDtrClient.startDetect(mContext, buildFaceParamter());
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e(TAG, e.getMessage());
        }

    }

    /**
     * 关闭camera or 完全销毁并释放资源
     *
     * @param doDestroy
     */
    public void release(boolean doDestroy) {
        LogUtil.i(TAG, TAG + "关闭camera 释放资源.");
        // 需要按照顺序关闭
        // 1.关闭人脸
        // 2.关闭Camera和LCD
        try {
            setGlassTipsMsg(null);
            mHandler.removeCallbacksAndMessages(null);
            if (mFaceDtrClient != null) {
                mFaceDtrClient.stopDetect();
                // 完全退出才销毁
                if (doDestroy) {
                    mFaceDtrClient.destroyFaceDtr();
                }
            }
            if (mLCDClient != null) {
                mLCDClient.stopCaptureScreen();
            }
            if (mCameraClient != null) {
                mCameraClient.disconnect();
            }
            if (doDestroy) {
                mFaceDtrClient = null;
                mLCDClient = null;
                mCameraClient = null;
                mContext = null;
                EventBus.getDefault().unregister(this);
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "release: failed " + e.getMessage());
        }
    }

    // 初始化识别参数
    private FaceInitParamter buildFaceParamter() {
        FaceInitParamter mFaceInitParamter = new FaceInitParamter();

        mFaceInitParamter.serviceType = FaceInitParamter.SERVICE_FACE_DETECT_TRACK_RECOGNIZE;

        // 检测类型 结果如下
        if (mFaceInitParamter.serviceType == FaceInitParamter.SERVICE_DETECT) {
            mFaceInitParamter.multipleFaceDetectDelayTime = 500;
        }
        DetectThreshold discrimatorn = new DetectThreshold();
        // 清晰度
        discrimatorn.countOfFast = 40;
        // 人像角度
        discrimatorn.yaw = 40;
        mFaceInitParamter.detectDiscrimatorn = discrimatorn;
        // 识别框颜色
        // 白色：正在识别
        // 红色：识别成功
        // 绿色：无法识别
        IndicatorColor[] indicatorColors = {
                new IndicatorColor(IndicatorColor.PHASE_TRACK_IDENTIFYING, Colors.WHITE.getColor())
                , new IndicatorColor(IndicatorColor.PHASE_TRACK_IDENTIFIED, Colors.RED.getColor())
                , new IndicatorColor(IndicatorColor.PHASE_TRACK_UNIDENTIFIED, Colors.GREEN.getColor())
        };
        mFaceInitParamter.indicatorColors = indicatorColors;
        mFaceInitParamter.indicatorColorSize = indicatorColors.length;

        return mFaceInitParamter;
    }

    /**
     * @param TAG
     * @param sourceImgPath
     * @param cropImgPath
     * @param recognizeResult
     * @return 返回人脸保存本地结果
     */
    private boolean handleRecognizeResult(String TAG, String sourceImgPath, String cropImgPath, RecognizeResult recognizeResult) {
        if (recognizeResult != null && recognizeResult.notification != null && recognizeResult.notification.notificationData != null
                && !TextUtils.isEmpty(sourceImgPath) && !TextUtils.isEmpty(cropImgPath)) {
            Mat rgb = new Mat(720, 1280, CvType.CV_8UC3);
            Mat y420 = new Mat(1080, 1280, CvType.CV_8UC1);
            y420.put(0, 0, recognizeResult.notification.notificationData);
            Imgproc.cvtColor(y420, rgb, Imgproc.COLOR_YUV2BGRA_NV21);

            File sourceFile = new File(Constants.FACE_DECECTION_PIC_SOURCE_FILE);
            if (!sourceFile.exists())
                sourceFile.mkdirs();

            boolean imwriteSource = Imgcodecs.imwrite(sourceImgPath, rgb);
            LogUtil.i(TAG, "保存原图路径" + (imwriteSource ? "成功" : "失败") + sourceImgPath);
            if (!imwriteSource) return false;
            if (recognizeResult.notification.areaList != null && recognizeResult.notification.areaList.size() > 0) {
                Area area = recognizeResult.notification.areaList.get(0);
                if (area != null) {
                    Mat faceMat = rgb.submat(area.toBoundingbox());
                    File cropFile = new File(Constants.FACE_DECECTION_PIC_CROP_FILE);
                    if (!cropFile.exists())
                        cropFile.mkdirs();
                    boolean imdecodeCrop = Imgcodecs.imwrite(cropImgPath, faceMat);
                    LogUtil.i(TAG, "保存剪切人脸" + (imdecodeCrop ? "成功" : "失败") + cropImgPath);
                    if (imdecodeCrop) {
                        // 压缩图片
                        BitmapUtil.compressImage(cropImgPath);
                    }
                    return imdecodeCrop;
                }
            }
        }
        return false;
    }

    // 存储完整原图路径
    private String getSourceImgPath(String currentTimeMillis) {
        return Constants.FACE_DECECTION_PIC_SOURCE_FILE + File.separator
                + currentTimeMillis
                + "_source.jpeg";
    }

    // 存储人脸区域剪裁路径
    private String getCropImgPath(String currentTimeMillis) {
        return Constants.FACE_DECECTION_PIC_CROP_FILE + File.separator
                + currentTimeMillis
                + "_crop.jpeg";
    }

}
