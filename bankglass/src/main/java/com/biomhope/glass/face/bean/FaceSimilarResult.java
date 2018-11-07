package com.biomhope.glass.face.bean;

import java.util.List;

/**
 * author:BH
 * create at:2018-10-18
 * description: 人脸相似比对结果
 */
public class FaceSimilarResult {

    public String exCode;
    public String exMsg;
    public List<UserInfo> clientList;

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        if (clientList != null && clientList.size() > 0) {
            for (UserInfo userInfo : clientList) {
                sb.append(userInfo.toString());
            }
        }
        return "FaceSimilarResult{" +
                "exCode='" + exCode + '\'' +
                ", exMsg='" + exMsg + '\'' +
                ", clientList=" + sb.toString() +
                '}';
    }
}
