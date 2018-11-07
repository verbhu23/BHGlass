package com.biomhope.glass.face.bean;

/**
 * @author $USER_NAME
 * create at : 2018-10-23
 * description :
 */
public class FaceDelRequestBean {

    public String userId;
    public String clientId;

    public FaceDelRequestBean(String userId, String clientId) {
        this.userId = userId;
        this.clientId = clientId;
    }

    @Override
    public String toString() {
        return "FaceDelRequestBean{" +
                "userId='" + userId + '\'' +
                ", clientId='" + clientId + '\'' +
                '}';
    }
}
