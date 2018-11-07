package com.biomhope.glass.face.bean;

import java.io.Serializable;

/**
 * @author $USER_NAME
 * create at : 2018-10-18
 * description : 详情页面数据模型
 */
public class UserInfo implements Serializable {

    // 本地人脸图路径
    public String imgCropPath;
    // 区分数据来自服务器还是本地 方便加载图片
    public boolean isServerPic = false;

    // 服务器返回数据封装
    public String name;
    public String idCard;
    public String gender;
    public String address;
    public String viplevel;
    public String sim;
    public String defaultSim;
    public String family;//分组标识
    public String faceDefine;//人脸图

    public String duendemand;//活期余额
    public String managemoney;//理财余额
    public String totalassets;//用户总资产
    public String terminal;//定期资产
    public String funds;//基金余额
    public String risk;//风险值

    @Override
    public String toString() {
        return "UserInfo{" +
                "imgCropPath='" + imgCropPath + '\'' +
                ", name='" + name + '\'' +
                ", idCard='" + idCard + '\'' +
                ", gender='" + gender + '\'' +
                ", address='" + address + '\'' +
                ", viplevel='" + viplevel + '\'' +
                ", sim='" + sim + '\'' +
                ", defaultSim='" + defaultSim + '\'' +
                ", family='" + family + '\'' +
                ", faceDefine='" + faceDefine + '\'' +
                ", duendemand='" + duendemand + '\'' +
                ", managemoney='" + managemoney + '\'' +
                ", totalassets='" + totalassets + '\'' +
                ", terminal='" + terminal + '\'' +
                ", funds='" + funds + '\'' +
                ", risk='" + risk + '\'' +
                '}';
    }
}
