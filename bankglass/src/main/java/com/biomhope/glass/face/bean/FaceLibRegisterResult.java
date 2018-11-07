package com.biomhope.glass.face.bean;

/**
 * @author $USER_NAME
 * create at : 2018-10-18
 * description :
 */
public class FaceLibRegisterResult {
    public int result;
    public String faceId;
    public String serialNumber;
    public String exCode;
    public String exMsg;
    public String clientId;

    @Override
    public String toString() {
        return "FaceLibRegisterResult{" +
                "result=" + result +
                ", faceId='" + faceId + '\'' +
                ", serialNumber='" + serialNumber + '\'' +
                ", exCode='" + exCode + '\'' +
                ", exMsg='" + exMsg + '\'' +
                ", clientId='" + clientId + '\'' +
                '}';
    }
}
