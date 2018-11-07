package com.biomhope.glass.face.bean;

/**
 * author:BH
 * create at:2018-10-18
 * description:人脸识别请求bean
 */
public class FaceRecogDetailRequestBean {

    public String userId;
    public String logId;

    public FaceRecogDetailRequestBean(String userId, String logId) {
        this.userId = userId;
        this.logId = logId;
    }

    @Override
    public String toString() {
        return "FaceRecogRequestBean{" +
                "userId='" + userId + '\'' +
                ", logId='" + logId + '\'' +
                '}';
    }
}
