package com.biomhope.glass.face.bean;

import java.util.List;

/**
 * author:BH
 * create at:2018-10-18
 * description: 人脸识别只返回一张图
 */
public class FaceRecogHistoryResult {

    public String exCode;
    public String exMsg;
    public List<FaceRecogListVO> result;

    @Override
    public String toString() {
        return "FaceRecogHistoryResult{" +
                "exCode='" + exCode + '\'' +
                ", exMsg='" + exMsg + '\'' +
                ", result=" + result +
                '}';
    }
}
