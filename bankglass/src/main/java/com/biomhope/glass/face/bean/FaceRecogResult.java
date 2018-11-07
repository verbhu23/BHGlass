package com.biomhope.glass.face.bean;

import java.util.List;

/**
 * author:BH
 * create at:2018-10-18
 * description: 人脸识别结果数据模型
 *
 */
public class FaceRecogResult {

    public String exCode;
    public String exMsg;
    public String logId;
    public FaceRecogListVO result;

    @Override
    public String toString() {
        return "FaceRecogResult{" +
                "exCode='" + exCode + '\'' +
                ", exMsg='" + exMsg + '\'' +
                ", logId='" + logId + '\'' +
                ", result=" + result.toString() +
                '}';
    }

}
