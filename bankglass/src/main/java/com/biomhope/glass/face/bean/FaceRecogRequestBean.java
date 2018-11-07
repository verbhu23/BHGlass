package com.biomhope.glass.face.bean;

/**
 * author:BH
 * create at:2018-10-18
 * description:人脸识别请求bean
 */
public class FaceRecogRequestBean {

    public String userId;
    public String img1;

    public FaceRecogRequestBean(String userId) {
        this.userId = userId;
    }

    public FaceRecogRequestBean(String userId, String img1) {
        this.userId = userId;
        this.img1 = img1;
    }

    @Override
    public String toString() {
        return "FaceRecogRequestBean{" +
                "userId='" + userId + '\'' +
                ", img1='" + img1 + '\'' +
                '}';
    }
}
