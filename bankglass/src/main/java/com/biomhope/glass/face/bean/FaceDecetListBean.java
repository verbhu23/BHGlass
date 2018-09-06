package com.biomhope.glass.face.bean;

public class FaceDecetListBean  {

    public String imgCropPath;
    public String name;
    public String decetTime;
    public String sim;
    public String checkResult;
    public String userType;
    public String level;

    public FaceDecetListBean(String imgCropPath, String name, String decetTime, String sim, String checkResult) {
        this.imgCropPath = imgCropPath;
        this.name = name;
        this.decetTime = decetTime;
        this.sim = sim;
        this.checkResult = checkResult;
    }

    @Override
    public String toString() {
        return "FaceDecetListBean{" +
                "imgCropPath='" + imgCropPath + '\'' +
                ", name='" + name + '\'' +
                ", decetTime='" + decetTime + '\'' +
                ", sim='" + sim + '\'' +
                ", checkResult='" + checkResult + '\'' +
                ", userType='" + userType + '\'' +
                ", level='" + level + '\'' +
                '}';
    }
}
