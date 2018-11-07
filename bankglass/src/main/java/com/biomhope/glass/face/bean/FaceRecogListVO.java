package com.biomhope.glass.face.bean;

import java.io.Serializable;
import java.util.List;

/**
 * author:BH
 * create at:2018-10-18
 * description: 人脸识别结果数据模型 与历史记录保存一致
 */
public class FaceRecogListVO implements Serializable {

    // 本地人脸图路径
    public String imgCropPath;
    // 区分数据来自服务器还是本地 方便加载图片
    public boolean isServerPic = false;

    // 对应服务端
    public int id;
    public String clientName;
    public String sim;
    public String idCard;
    public String family;
    public String img1;
    public String vipLevel;

}
