package com.biomhope.glass.face.bean;

import java.io.Serializable;

/**
 * @author $USER_NAME
 * create at : 2018-10-18
 * description : 封装人脸库用户数据
 */
public class GroupUserInfo implements Serializable {

    public static final String USER_TYPE_COMMON = "0";
    public static final String USER_TYPE_VIP = "1";
    public static final String USER_TYPE_BLACK = "2";

    public String name;
    public String idCard;
    public String clientId;
    public String gender;
    public String address;
    public String viplevel;
    public String img1; //封装来自服务器的图片路径 或者是上传给服务器的Base64图片
    public String img2; //封装本地图片路径
    public String phoneNumber;
    public String family;//分组标识
    public String userId;

    public String toString1() {
        return "GroupUserInfo{" +
                "name='" + name + '\'' +
                ", idCard='" + idCard + '\'' +
                ", clientId='" + clientId + '\'' +
                ", gender='" + gender + '\'' +
                ", address='" + address + '\'' +
                ", viplevel='" + viplevel + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", family='" + family + '\'' +
                ", userId='" + userId + '\'' +
                ", img2='" + img2 + '\'' +
                '}';
    }

    @Override
    public String toString() {
        return "GroupUserInfo{" +
                "name='" + name + '\'' +
                ", idCard='" + idCard + '\'' +
                ", clientId='" + clientId + '\'' +
                ", gender='" + gender + '\'' +
                ", address='" + address + '\'' +
                ", viplevel='" + viplevel + '\'' +
                ", img1='" + img1 + '\'' +
                ", img2='" + img2 + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", family='" + family + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }
}
