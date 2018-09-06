package com.biomhope.glass.face.utils;

import android.text.TextUtils;
import android.util.Log;

import com.biomhope.glass.face.global.Constants;
import com.llvision.face.dtr.model.Area;
import com.llvision.face.dtr.model.Colors;
import com.llvision.face.dtr.model.DetectThreshold;
import com.llvision.face.dtr.model.FaceInitParamter;
import com.llvision.face.dtr.model.IndicatorColor;
import com.llvision.face.dtr.model.RecognizeResult;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;

public class LLCameraUtil {

    // 检测类型 结果如下
    // DTR 检测追踪 识别
    public static final int SERVICE_FACE_DETECT_TRACK_RECOGNIZE = 1;
    // DT 单脸检测追踪
    public static final int SERVICE_FACE_DETECT_TRACK = 2;
    // MD 多脸
    public static final int SERVICE_DETECT = 4;

    // 初始化识别参数
    public static FaceInitParamter buildFaceParamter() {
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
        discrimatorn.yaw = 35;
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
    public static boolean handleRecognizeResult(String TAG, String sourceImgPath, String cropImgPath, RecognizeResult recognizeResult) {
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
            Log.i(TAG, "保存原图" + sourceImgPath + (imwriteSource ? "成功" : "失败"));
            if (!imwriteSource) return false;
            if (recognizeResult.notification.areaList != null && recognizeResult.notification.areaList.size() > 0) {
                Area area = recognizeResult.notification.areaList.get(0);
                if (area != null) {
                    Mat faceMat = rgb.submat(area.toBoundingbox());
                    File cropFile = new File(Constants.FACE_DECECTION_PIC_CROP_FILE);
                    if (!cropFile.exists())
                        cropFile.mkdirs();
                    boolean imdecodeCrop = Imgcodecs.imwrite(cropImgPath, faceMat);
                    Log.i(TAG, "保存人脸" + sourceImgPath + (imdecodeCrop ? "成功" : "失败"));
                    return imdecodeCrop;
                }
            }
        }
        return false;
    }

    // 存储完整原图路径
    public static String getSourceImgPath(String currentTimeMillis) {
        return Constants.FACE_DECECTION_PIC_SOURCE_FILE + File.separator
                + currentTimeMillis
                + "_source.png";
    }

    // 存储人脸区域剪裁路径
    public static String getCropImgPath(String currentTimeMillis) {
        return Constants.FACE_DECECTION_PIC_CROP_FILE + File.separator
                + currentTimeMillis
                + "_crop.png";
    }

}
