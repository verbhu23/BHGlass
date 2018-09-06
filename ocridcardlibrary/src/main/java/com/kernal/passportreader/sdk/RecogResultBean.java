package com.kernal.passportreader.sdk;

public class RecogResultBean {

    public String recogResultString;
    public String devcode;
    public String picPathString1;
    public String picPathString;
    public String HeadJpgPath;
    public int nCropType;
    public String exception;
    public int VehicleLicenseflag;

    @Override
    public String toString() {
        return "RecogResultBean{" +
                "recogResultString='" + recogResultString + '\'' +
                ", devcode='" + devcode + '\'' +
                ", picPathString1='" + picPathString1 + '\'' +
                ", picPathString='" + picPathString + '\'' +
                ", HeadJpgPath='" + HeadJpgPath + '\'' +
                ", nCropType=" + nCropType +
                ", exception='" + exception + '\'' +
                ", VehicleLicenseflag=" + VehicleLicenseflag +
                '}';
    }
}
